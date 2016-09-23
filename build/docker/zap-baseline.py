#!/usr/bin/python
# Zed Attack Proxy (ZAP) and its related class files.
#
# ZAP is an HTTP/HTTPS proxy for assessing web application security.
#
# Copyright 2016 ZAP Development Team
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# This script runs a baseline scan against a target URL using ZAP
#
# It can either be run 'standalone', in which case depends on
# https://pypi.python.org/pypi/python-owasp-zap-v2.4 and Docker, or it can be run
# inside one of the ZAP docker containers. It automatically detects if it is
# running in docker so the parameters are the same.
#
# By default it will spider the target URL for one minute, but you can change
# that via the -m parameter.
# It will then wait for the passive scanning to finish - how long that takes
# depends on the number of pages found.
# It will exit with codes of:
#	0:	Success
#	1:	At least 1 FAIL
#	2:	At least one WARN and no FAILs
#	3:	Any other failure
# By default all alerts found by ZAP will be treated as WARNings.
# You can use the -c or -u parameters to specify a configuration file to override
# this.
# You can generate a template configuration file using the -g parameter. You will
# then need to change 'WARN' to 'FAIL', 'INFO' or 'IGNORE' for the rules you want
# to be handled differently.
# You can also add your own messages for the rules by appending them after a tab
# at the end of each line.

import getopt
import json
import logging
import os
import re
import socket
import subprocess
import sys
import time
import traceback
import urllib2
from datetime import datetime
from random import randint
from zapv2 import ZAPv2

timeout = 120
config_dict = {}
config_msg = {}
out_of_scope_dict = {}
running_in_docker = os.path.exists('/.dockerenv')
levels = ["PASS", "IGNORE", "INFO", "WARN", "FAIL"]
min_level = 0

# Pscan rules that aren't really relevant, eg example alpha rules
blacklist = ['-1', '50003', '60000', '60001']

logging.basicConfig(level=logging.INFO)

def usage():
    print ('Usage: zap-baseline.py -t <target> [options]')
    print ('    -t target         target URL including the protocol, eg https://www.example.com')
    print ('Options:')
    print ('    -c config_file    config file to use to INFO, IGNORE or FAIL warnings')
    print ('    -u config_url     URL of config file to use to INFO, IGNORE or FAIL warnings')
    print ('    -g gen_file       generate default config file (all rules set to WARN)')
    print ('    -m mins           the number of minutes to spider for (default 1)')
    print ('    -r report_html    file to write the full ZAP HTML report')
    print ('    -x report_xml     file to write the full ZAP XML report')
    print ('    -a                include the alpha passive scan rules as well')
    print ('    -d                show debug messages')
    print ('    -i                default rules not in the config file to INFO')
    print ('    -j                use the Ajax spider in addition to the traditional one')
    print ('    -l level          minimum level to show: PASS, IGNORE, INFO, WARN or FAIL, use with -s to hide example URLs')
    print ('    -s                short output format - dont show PASSes or example URLs')
    print ('    -z zap_options    ZAP command line options e.g. -z "-config aaa=bbb -config ccc=ddd"')

def load_config(config):
  for line in config:
    if not line.startswith('#') and len(line) > 1:
      (key, val, optional) = line.rstrip().split('\t', 2)
      if key == 'OUTOFSCOPE':
        for plugin_id in val.split(','):
          if not plugin_id in out_of_scope_dict:
            out_of_scope_dict[plugin_id] = []
          out_of_scope_dict[plugin_id].append(re.compile(optional))
      else:
        config_dict[key] = val
        if '\t' in optional:
          (ignore, usermsg) = optional.rstrip().split('\t')
          config_msg[key] = usermsg
        else:
          config_msg[key] = ''

def is_in_scope(plugin_id, url):
  if '*' in out_of_scope_dict:
    for oos_prog in out_of_scope_dict['*']:
      #print('OOS Compare ' + oos_url + ' vs ' + 'url)
      if oos_prog.match(url):
        #print('OOS Ignoring ' + str(plugin_id) + ' ' + url)
        return False
    #print 'Not in * dict'
  if plugin_id in out_of_scope_dict:
    for oos_prog in out_of_scope_dict[plugin_id]:
      #print('OOS Compare ' + oos_url + ' vs ' + 'url)
      if oos_prog.match(url):
        #print('OOS Ignoring ' + str(plugin_id) + ' ' + url)
        return False
    #print 'Not in ' + plugin_id + ' dict'
  return True  

def print_rule(action, alert_list, detailed_output, user_msg):
  if min_level > levels.index(action):
    return;

  print (action + ': ' + alert_list[0].get('alert') + ' [' + alert_list[0].get('pluginId') + '] x ' + str(len(alert_list)) + ' ' + user_msg)
  if detailed_output:
    # Show (up to) first 5 urls
    for alert in alert_list[0:5]:
      print ('\t' + alert.get('url'))

def main(argv):

  global min_level
  config_file = ''
  config_url = ''
  generate = ''
  mins = 1
  port = 0
  detailed_output = True
  report_html = ''
  report_xml = ''
  target = ''
  zap_alpha = False
  info_unspecified = False
  ajax = False
  base_dir = ''
  zap_ip = 'localhost'
  zap_options = ''

  pass_count = 0
  warn_count = 0
  fail_count = 0
  info_count = 0
  ignore_count = 0

  try:
    opts, args = getopt.getopt(argv,"t:c:u:g:m:r:x:l:daijsz:")
  except getopt.GetoptError, exc:
    logging.warning ('Invalid option ' + exc.opt + ' : ' + exc.msg)
    usage()
    sys.exit(3)

  for opt, arg in opts:
    if opt == '-t':
      target = arg
      logging.debug ('Target: ' + target)
    elif opt == '-c':
      config_file = arg
    elif opt == '-u':
      config_url = arg
    elif opt == '-g':
      generate = arg
    elif opt == '-d':
      logging.getLogger().setLevel(logging.DEBUG)
    elif opt == '-m':
      mins = int(arg)
    elif opt == '-r':
      report_html = arg
    elif opt == '-x':
      report_xml = arg
    elif opt == '-a':
      zap_alpha = True
    elif opt == '-i':
      info_unspecified = True
    elif opt == '-j':
      ajax = True
    elif opt == '-l':
      try:
        min_level = levels.index(arg)
      except ValueError:
        logging.warning ('Level must be one of ' + str(levels))
        usage()
        sys.exit(3)
    elif opt == '-z':
      zap_options = arg
      
    elif opt == '-s':
      detailed_output = False

  # Check target supplied and ok
  if len(target) == 0:
    usage()
    sys.exit(3)

  if not (target.startswith('http://') or target.startswith('https://')):
    logging.warning ('Target must start with \'http://\' or \'https://\'')
    usage()
    sys.exit(3)

  if running_in_docker:
    base_dir = '/zap/wrk/'
    if len(config_file) > 0 or len(generate) > 0 or len(report_html) > 0 or len(report_xml) > 0:
      # Check directory has been mounted
      if not os.path.exists(base_dir): 
        logging.warning ('A file based option has been specified but the directory \'/zap/wrk\' is not mounted ')
        usage()
        sys.exit(3)
    

  # Choose a random 'ephemeral' port and check its available
  while True:
    port = randint(32768, 61000)
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    if not (sock.connect_ex(('127.0.0.1', port)) == 0):
      # Its free:)
      break

  logging.debug ('Using port: ' + str(port))

  if len(config_file) > 0:
    # load config file from filestore
    with open(base_dir + config_file) as f:
      load_config(f)
  elif len(config_url) > 0:
    # load config file from url
    try:
      load_config(urllib2.urlopen(config_url))
    except:
      logging.warning ('Failed to read configs from ' + config_url)
      sys.exit(3)

  if running_in_docker:
    try:
      logging.debug ('Starting ZAP')
      params = ['zap-x.sh', '-daemon', 
                '-port', str(port), 
                '-host', '0.0.0.0', 
                '-config', 'api.disablekey=true', 
                '-config', 'spider.maxDuration=' + str(mins),
                '-addonupdate', 
                '-addoninstall', 'pscanrulesBeta']	# In case we're running in the stable container

      if (zap_alpha):
        params.append('-addoninstall')
        params.append('pscanrulesAlpha')

      if len(zap_options) > 0:
        for zap_opt in zap_options.split(" "):
          params.append(zap_opt)

      with open('zap.out', "w") as outfile:
        subprocess.Popen(params, stdout=outfile)

    except OSError:
      logging.warning ('Failed to start ZAP :(')
      sys.exit(3)
  
  else:
    # Not running in docker, so start one  
    try:
      logging.debug ('Pulling ZAP Weekly Docker image')
      ls_output = subprocess.check_output(['docker', 'pull', 'owasp/zap2docker-weekly'])
    except OSError:
      logging.warning ('Failed to run docker - is it on your path?')
      sys.exit(3)

    try:
      logging.debug ('Starting ZAP')
      params = ['docker', 'run', '-u', 'zap', 
                '-p', str(port) + ':' + str(port), 
                '-d', 'owasp/zap2docker-weekly', 
                'zap-x.sh', '-daemon', 
                '-port', str(port), 
                '-host', '0.0.0.0', 
                '-config', 'api.disablekey=true', 
                '-config', 'spider.maxDuration=' + str(mins),
                '-addonupdate']

      if (zap_alpha):
        params.append('-addoninstall')
        params.append('pscanrulesAlpha')
        
      if len(zap_options) > 0:
        for zap_opt in zap_options.split(" "):
          params.append(zap_opt)

      cid = subprocess.check_output(params).rstrip()
      logging.debug ('Docker CID: ' + cid)
      insp_output = subprocess.check_output(['docker', 'inspect', cid])
      #logging.debug ('Docker Inspect: ' + insp_output)
      insp_json = json.loads(insp_output)
      zap_ip = str(insp_json[0]['NetworkSettings']['IPAddress'])
      logging.debug ('Docker ZAP IP Addr: ' + zap_ip)
    except OSError:
      logging.warning ('Failed to start ZAP in docker :(')
      sys.exit(3)

  try:
    # Wait for ZAP to start
    zap = ZAPv2(proxies={'http': 'http://' + zap_ip + ':' + str(port), 'https': 'http://' + zap_ip + ':' + str(port)})
    for x in range(0, timeout):
      try:
        logging.debug ('ZAP Version ' + zap.core.version)
        break
      except IOError:
        time.sleep(1)

    # Access the target
    zap.urlopen(target)
    time.sleep(2)

    # Spider target
    logging.debug ('Spider ' + target)
    spider_scan_id = zap.spider.scan(target)
    time.sleep(5)

    while (int(zap.spider.status(spider_scan_id)) < 100):
      logging.debug ('Spider progress %: ' + zap.spider.status(spider_scan_id))
      time.sleep(5)
    logging.debug ('Spider complete')

    if (ajax):
      # Ajax Spider the target as well
      logging.debug ('AjaxSpider ' + target)
      zap.ajaxSpider.set_option_max_duration(str(mins))
      zap.ajaxSpider.scan(target)
      time.sleep(5)

      while (zap.ajaxSpider.status == 'running'):
        logging.debug ('Ajax Spider running, found urls: ' + zap.ajaxSpider.number_of_results)
        time.sleep(5)
      logging.debug ('Ajax Spider complete')

    # Wait for passive scanning to complete
    rtc = zap.pscan.records_to_scan
    logging.debug ('Records to scan...')
    while (int(zap.pscan.records_to_scan) > 0):
      logging.debug ('Records to passive scan : ' + zap.pscan.records_to_scan)
      time.sleep(2)
    logging.debug ('Passive scanning complete')

    # Print out a count of the number of urls
    num_urls = len(zap.core.urls)
    if (num_urls == 0):
      logging.warning('No URLs found - is the target URL accessible? Local services may not be accessible from the Docker container')
    else:
      if detailed_output:
        print ('Total of ' + str(len(zap.core.urls)) + ' URLs')
      # Retrieve the alerts using paging in case there are lots of them
      st = 0
      pg = 100
      alert_dict = {}
      alerts = zap.core.alerts(start=st, count=pg)
      while len(alerts) > 0:
        for alert in alerts:
          plugin_id = alert.get('pluginId')
          if plugin_id in blacklist:
            continue
          if not is_in_scope(plugin_id, alert.get('url')):
            continue
          if (not alert_dict.has_key(plugin_id)):
            alert_dict[plugin_id] = []
          alert_dict[plugin_id].append(alert)
        st += pg
        alerts = zap.core.alerts(start=st, count=pg)

      all_rules = zap.pscan.scanners
      all_dict = {}
      for rule in all_rules:
        plugin_id = rule.get('id')
        if plugin_id in blacklist:
          continue
        all_dict[plugin_id] = rule.get('name')

      if len(generate) > 0:
        # Create the config file
        with open(base_dir + generate, 'w') as f:
          f.write ('# zap-baseline rule configuration file\n')
          f.write ('# Change WARN to IGNORE to ignore rule or FAIL to fail if rule matches\n')
          f.write ('# Only the rule identifiers are used - the names are just for info\n')
          f.write ('# You can add your own messages to each rule by appending them after a tab on each line.\n')
          for key, rule in sorted(all_dict.iteritems()):
            f.write (key + '\tWARN\t(' + rule + ')\n')

      # print out the passing rules
      pass_dict = {}
      for rule in all_rules:
        plugin_id = rule.get('id')
        if plugin_id in blacklist:
          continue
        if (not alert_dict.has_key(plugin_id)):
          pass_dict[plugin_id] = rule.get('name')

      if min_level == levels.index("PASS") and detailed_output:
        for key, rule in sorted(pass_dict.iteritems()):
          print ('PASS: ' + rule + ' [' + key + ']')

      pass_count = len(pass_dict)

      # print out the ignored rules
      for key, alert_list in sorted(alert_dict.iteritems()):
        if (config_dict.has_key(key) and config_dict[key] == 'IGNORE'):
          user_msg = ''
          if key in config_msg:
            user_msg = config_msg[key]
          print_rule(config_dict[key], alert_list, detailed_output, user_msg)
          ignore_count += 1

      # print out the info rules
      for key, alert_list in sorted(alert_dict.iteritems()):
        if (config_dict.has_key(key) and config_dict[key] == 'INFO') or (not config_dict.has_key(key)) and info_unspecified:
          user_msg = ''
          if key in config_msg:
            user_msg = config_msg[key]
          print_rule('INFO', alert_list, detailed_output, user_msg)
          info_count += 1

      # print out the warning rules
      for key, alert_list in sorted(alert_dict.iteritems()):
        if (not config_dict.has_key(key) and not info_unspecified) or (config_dict.has_key(key) and config_dict[key] == 'WARN'):
          user_msg = ''
          if key in config_msg:
            user_msg = config_msg[key]
          print_rule('WARN', alert_list, detailed_output, user_msg)
          warn_count += 1

      # print out the failing rules
      for key, alert_list in sorted(alert_dict.iteritems()):
        if config_dict.has_key(key) and config_dict[key] == 'FAIL':
          user_msg = ''
          if key in config_msg:
            user_msg = config_msg[key]
          print_rule(config_dict[key], alert_list, detailed_output, user_msg)
          fail_count += 1
          
      if len(report_html) > 0:
        # Save the report
        with open(base_dir + report_html, 'w') as f:
          f.write (zap.core.htmlreport())

      if len(report_xml) > 0:
        # Save the report
        with open(base_dir + report_xml, 'w') as f:
          f.write (zap.core.xmlreport())

      print ('FAIL: ' + str(fail_count) + '\tWARN: ' + str(warn_count) + '\tINFO: ' + str(info_count) +  
        '\tIGNORE: ' + str(ignore_count) + '\tPASS: ' + str(pass_count))

    # Stop ZAP
    zap.core.shutdown()

  except IOError as (errno, strerror):
    logging.warning ('I/O error(' + str(errno) + '): ' + str(strerror))
    traceback.print_exc()
  except:
    logging.warning ('Unexpected error: ' + str(sys.exc_info()[0]))
    traceback.print_exc()

  if not running_in_docker:
    # Close container - ignore failures
    try:
      logging.debug ('Stopping Docker container')
      subprocess.check_output(['docker', 'stop', cid])
      logging.debug ('Docker container stopped')
    except OSError:
      logging.warning ('Docker stop failed')

    # Remove container - ignore failures
    try:
      logging.debug ('Removing Docker container')
      subprocess.check_output(['docker', 'rm', cid])
      logging.debug ('Docker container removed')
    except OSError:
      logging.warning ('Docker rm failed')

  if fail_count > 0:
    sys.exit(1)
  elif warn_count > 0:
    sys.exit(2)
  elif pass_count > 0:
    sys.exit(0)
  else:
    sys.exit(3)

if __name__ == "__main__":
  main(sys.argv[1:])

