#!/usr/bin/env python
# Zed Attack Proxy (ZAP) and its related class files.
#
# ZAP is an HTTP/HTTPS proxy for assessing web application security.
#
# Copyright 2017 ZAP Development Team
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

# This script runs a full scan against an API defined by OpenAPI/Swagger or SOAP
# using ZAP
#
# It can either be run 'standalone', in which case depends on
# https://pypi.python.org/pypi/python-owasp-zap-v2.4 and Docker, or it can be run
# inside one of the ZAP docker containers. It automatically detects if it is
# running in docker so the parameters are the same.
#
# It currently support APIS defined by:
#	OpenAPI/Swagger URL
#	OpenAPI/Swagger file
#	SOAP URL
#	SOAP File
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
# The active scan rules run are currently hardcoded in the API-Minimal.policy file
# the plan is to make these configurable in a future version of this script.

import getopt
import json
import logging
import os
import os.path
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

# Scan rules that aren't really relevant, eg the examples rules in the alpha set
blacklist = ['-1', '50003', '60000', '60001']

# Scan rules that are being addressed
in_progress_issues = {}

logging.basicConfig(level=logging.INFO, format='%(asctime)s %(message)s')
# Hide "Starting new HTTP connection" messages
logging.getLogger("requests").setLevel(logging.WARNING)

def usage():
    print ('Usage: zap-api-scan.py -t <target> -f <format> [options]')
    print ('    -t target         target API definition, OpenAPI or SOAP, local file or URL, eg https://www.example.com/openapi.json')
    print ('    -f format         either openapi or soap')
    print ('Options:')
    print ('    -c config_file    config file to use to INFO, IGNORE or FAIL warnings')
    print ('    -u config_url     URL of config file to use to INFO, IGNORE or FAIL warnings')
    print ('    -g gen_file       generate default config file (all rules set to WARN)')
    print ('    -r report_html    file to write the full ZAP HTML report')
    print ('    -w report_md      file to write the full ZAP Wiki (Markdown) report')
    print ('    -x report_xml     file to write the full ZAP XML report')
    print ('    -a                include the alpha passive scan rules as well')
    print ('    -d                show debug messages')
    print ('    -P                specify listen port')
    print ('    -D                delay in seconds to wait for passive scanning ')
    print ('    -i                default rules not in the config file to INFO')
    print ('    -l level          minimum level to show: PASS, IGNORE, INFO, WARN or FAIL, use with -s to hide example URLs')
    print ('    -n context_file   context file which will be loaded prior to scanning the target')
    print ('    -p progress_file  progress file which specifies issues that are being addressed')
    print ('    -s                short output format - dont show PASSes or example URLs')
    print ('    -z zap_options    ZAP command line options e.g. -z "-config aaa=bbb -config ccc=ddd"')
    print ('')
    print ('For more details see https://github.com/zaproxy/zaproxy/wiki/ZAP-API-Scan')

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

  id = alert_list[0].get('pluginId')
  if id in in_progress_issues:
    print (action + '-IN_PROGRESS: ' + alert_list[0].get('alert') + ' [' + id + '] x ' + str(len(alert_list)) + ' ' + user_msg)
    if in_progress_issues[id]["link"]:
      print ('\tProgress link: ' + in_progress_issues[id]["link"])
  else:
    print (action + '-NEW: ' + alert_list[0].get('alert') + ' [' + id + '] x ' + str(len(alert_list)) + ' ' + user_msg)
  if detailed_output:
    # Show (up to) first 5 urls
    for alert in alert_list[0:5]:
      print ('\t' + alert.get('url'))

def dump_log_file(cid):
  traceback.print_exc()
  # Unexpected issue - dump the zap.log file
  if running_in_docker:
    zap_log = '/zap/zap.out'
    if os.path.isfile(zap_log):
      with open(zap_log, 'r') as zlog:
        for line in zlog:
          sys.stderr.write(line)
    else:
      logging.debug ('Failed to find zap_log ' + zap_log)
  else:
    logging.debug ('Dumping docker logs')
    subprocess.call(["docker", "logs", cid], stdout = sys.stderr)

def cp_to_docker(cid, file, dir):
  logging.debug ('Copy ' + file)
  params = ['docker', 'cp', file, cid + ':' + dir + file]
  print (subprocess.check_output(params))

def main(argv):

  global min_level
  global in_progress_issues
  cid = ''
  context_file = ''
  progress_file = ''
  config_file = ''
  config_url = ''
  generate = ''
  port = 0
  detailed_output = True
  report_html = ''
  report_md = ''
  report_xml = ''
  target = ''
  target_file = ''
  target_url = ''
  format = ''
  zap_alpha = False
  info_unspecified = False
  base_dir = ''
  zap_ip = 'localhost'
  zap_options = ''
  delay = 0

  pass_count = 0
  warn_count = 0
  fail_count = 0
  info_count = 0
  ignore_count = 0
  warn_inprog_count = 0
  fail_inprog_count = 0

  try:
    opts, args = getopt.getopt(argv,"t:f:c:u:g:m:n:r:w:x:l:daijp:sz:P:D:")
  except getopt.GetoptError as exc:
    logging.warning ('Invalid option ' + exc.opt + ' : ' + exc.msg)
    usage()
    sys.exit(3)

  for opt, arg in opts:
    if opt == '-t':
      target = arg
      logging.debug ('Target: ' + target)
    elif opt == '-f':
      format = arg
    elif opt == '-c':
      config_file = arg
    elif opt == '-u':
      config_url = arg
    elif opt == '-g':
      generate = arg
    elif opt == '-d':
      logging.getLogger().setLevel(logging.DEBUG)
    elif opt == '-P':
      port = int(arg)
    elif opt == '-D':
      delay = int(arg)
    elif opt == '-n':
      context_file = arg
    elif opt == '-p':
      progress_file = arg
    elif opt == '-r':
      report_html = arg
    elif opt == '-w':
      report_md = arg
    elif opt == '-x':
      report_xml = arg
    elif opt == '-a':
      zap_alpha = True
    elif opt == '-i':
      info_unspecified = True
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
  if format != 'openapi' and format != 'soap': 
    logging.warning ('Format must be either \'openapi\' or \'soap\'')
    usage()
    sys.exit(3)

  if target.startswith('http://') or target.startswith('https://'):
    target_url = target
  else:
    # assume its a file
    if not os.path.exists(target): 
      logging.warning ('Target must either start with \'http://\' or \'https://\' or be a local file')
      usage()
      sys.exit(3)
    else:
      target_file = target

  if running_in_docker:
    base_dir = '/zap/wrk/'
    if len(config_file) > 0 or len(generate) > 0 or len(report_html) > 0 or len(report_xml) > 0 or len(progress_file) > 0 or len(context_file) > 0 or len(target_file) > 0:
      # Check directory has been mounted
      if not os.path.exists(base_dir): 
        logging.warning ('A file based option has been specified but the directory \'/zap/wrk\' is not mounted ')
        usage()
        sys.exit(3)

  # Choose a random 'ephemeral' port and check its available if it wasn't specified with -P option
  if port == 0:
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

  if len(progress_file) > 0:
    # load progress file from filestore
    with open(base_dir + progress_file) as f:    
      progress = json.load(f)
      # parse into something more useful...
      # in_prog_issues = map of vulnid -> {object with everything in}
      for issue in progress["issues"]:
        if issue["state"] == "inprogress":
           in_progress_issues[issue["id"]] = issue

  if running_in_docker:
    try:
      logging.debug ('Starting ZAP')
      params = ['zap-x.sh', '-daemon', 
                '-port', str(port), 
                '-host', '0.0.0.0', 
                '-config', 'api.disablekey=true', 
                '-config', 'api.addrs.addr.name=.*', 
                '-config', 'api.addrs.addr.regex=true', 
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
      params = ['docker', 'run'] 

      if len(context_file) > 0:
        # Have to mount to directory containing the context file
        params.extend(['-v', os.path.dirname(os.path.abspath(context_file)) + ':/zap/wrk/:rw'])

      params.extend([
                '-u', 'zap', 
                '-p', str(port) + ':' + str(port),
                '-d', 'owasp/zap2docker-weekly', 
                'zap-x.sh', '-daemon', 
                '-port', str(port), 
                '-host', '0.0.0.0', 
                '-config', 'api.disablekey=true', 
                '-config', 'api.addrs.addr.name=.*', 
                '-config', 'api.addrs.addr.regex=true', 
                '-addonupdate'])

      if (zap_alpha):
        params.extend(['-addoninstall', 'pscanrulesAlpha'])

      if len(zap_options) > 0:
        for zap_opt in zap_options.split(" "):
          params.append(zap_opt)
          
      logging.info ('Params: ' + str(params))

      cid = subprocess.check_output(params).rstrip()
      logging.debug ('Docker CID: ' + cid)
      insp_output = subprocess.check_output(['docker', 'inspect', cid])
      #logging.debug ('Docker Inspect: ' + insp_output)
      insp_json = json.loads(insp_output)
      zap_ip = str(insp_json[0]['NetworkSettings']['IPAddress'])
      logging.debug ('Docker ZAP IP Addr: ' + zap_ip)

      # Copy across the files that may not be in all of the docker images
      try:
        subprocess.check_output(['docker', 'exec', '-t', cid, 'mkdir', '-p', '/home/zap/.ZAP_D/scripts/scripts/httpsender/'])
        cp_to_docker(cid, 'scripts/scripts/httpsender/Alert_on_HTTP_Response_Code_Errors.js', '/home/zap/.ZAP_D/')
        cp_to_docker(cid, 'scripts/scripts/httpsender/Alert_on_Unexpected_Content_Types.js', '/home/zap/.ZAP_D/')
        cp_to_docker(cid, 'policies/API-Minimal.policy', '/home/zap/.ZAP_D/')
        if len(target_file) > 0:
          cp_to_docker(cid, target_file, '/zap/')

      except OSError:
        logging.warning ('Failed to copy one of the required files')
        sys.exit(3)
      
    except OSError:
      logging.warning ('Failed to start ZAP in docker :(')
      sys.exit(3)

  try:
    # Wait for ZAP to start
    zap = ZAPv2(proxies={'http': 'http://' + zap_ip + ':' + str(port), 'https': 'http://' + zap_ip + ':' + str(port)})
    for x in range(0, timeout):
      try:
        logging.debug ('ZAP Version ' + zap.core.version)
        logging.debug ('Took ' + str(x) + ' seconds')
        break
      except IOError:
        time.sleep(1)

    if len(context_file) > 0:
      # handle the context file, cant use base_dir as it might not have been set up
      res = zap.context.import_context('/zap/wrk/' + os.path.basename(context_file))
      if res.startswith("ZAP Error"):
        logging.error ('Failed to load context file ' + context_file + ' : ' + res)

    # Enable scripts
    zap.script.load('Alert_on_HTTP_Response_Code_Errors.js', 'httpsender', 'Oracle Nashorn', '/home/zap/.ZAP_D/scripts/scripts/httpsender/Alert_on_HTTP_Response_Code_Errors.js')
    zap.script.enable('Alert_on_HTTP_Response_Code_Errors.js')
    zap.script.load('Alert_on_Unexpected_Content_Types.js', 'httpsender', 'Oracle Nashorn', '/home/zap/.ZAP_D/scripts/scripts/httpsender/Alert_on_Unexpected_Content_Types.js')
    zap.script.enable('Alert_on_Unexpected_Content_Types.js')

    # Import the API defn
    if format == 'openapi':
      if len(target_url) > 0:
        logging.debug ('Import OpenAPI URL ' + target_url)
        res = zap._request(zap.base + 'openapi/action/importUrl/', {'url':target})
        urls = zap.core.urls
      else:
        logging.debug ('Import OpenAPI File ' + target_file)
        res = zap._request(zap.base + 'openapi/action/importFile/', {'file': base_dir + target_file})
        urls = zap.core.urls
        if len(urls) > 0:
          # Choose the first one - will be striping off the path below
          target = urls[0]
        else:
          logging.error ('Failed to import any URLs')
    else:
      if len(target_url) > 0:
        logging.debug ('Import SOAP URL ' + target_url)
        res = zap._request(zap.base + 'soap/action/importUrl/', {'url':target})
        urls = zap.core.urls
      else:
        logging.debug ('Import SOAP File ' + target_file)
        res = zap._request(zap.base + 'soap/action/importFile/', {'file': base_dir + target_file})
        urls = zap.core.urls
        if len(urls) > 0:
          # Choose the first one - will be striping off the path below
          target = urls[0]
        else:
          logging.error ('Failed to import any URLs')
    
    logging.info ('Number of Imported URLs: ' + str(len(urls)))
    logging.debug ('Import warnings: ' + str(res))

    if target.count('/') > 2:
      # The url can include a valid path, but always reset to scan the host 
      target = target[0:target.index('/', 8)+1]
    
    # Wait for a delay if specified with -D option
    if (delay):
      start_scan = datetime.now()
      while ( (datetime.now() - start_scan).seconds < delay ):
        time.sleep(5)
        logging.debug ('Delay active scan ' + str(delay - (datetime.now() - start_scan).seconds) + ' seconds')

    # Set up the scan policy
    scan_policy = 'API-Minimal'
    if config_dict:
      # They have supplied a config file, use this to define the ascan rules
      # Use the default one as the script might not have write access to the one just copied across 
      scan_policy = 'Default Policy'
      zap.ascan.enable_all_scanners(scanpolicyname=scan_policy)
      for scanner, state in config_dict.items():
        if state == 'IGNORE':
          # Dont bother checking the result - this will fail for pscan rules
          zap.ascan.set_scanner_alert_threshold(id=scanner, alertthreshold='OFF', scanpolicyname=scan_policy)
    
    logging.debug ('Active Scan ' + target)
    ascan_scan_id = zap.ascan.scan(target, recurse=True, scanpolicyname=scan_policy)
    time.sleep(5)

    while (int(zap.ascan.status(ascan_scan_id)) < 100):
      logging.debug ('Active Scan progress %: ' + zap.ascan.status(ascan_scan_id))
      time.sleep(5)
    logging.debug ('Active Scan complete')
    logging.debug (zap.ascan.scan_progress(ascan_scan_id))

    # Wait for passive scanning to complete
    rtc = zap.pscan.records_to_scan
    logging.debug ('Records to scan...')
    while (int(zap.pscan.records_to_scan) > 0):
      logging.debug ('Records to passive scan : ' + zap.pscan.records_to_scan)
      time.sleep(2)
    logging.debug ('Passive scanning complete')

    # Print out a count of the number of urls
    num_urls = len(zap.core.urls)
    if num_urls == 0:
      logging.warning('No URLs found - is the target URL accessible? Local services may not be accessible from the Docker container')
    else:
      if detailed_output:
        print ('Total of ' + str(num_urls) + ' URLs')

      # Retrieve the alerts using paging in case there are lots of them
      st = 0
      pg = 5000
      alert_dict = {}
      alert_count = 0
      alerts = zap.core.alerts(start=st, count=pg)
      while len(alerts) > 0:
        logging.debug ('Reading ' + str(pg) + ' alerts from ' + str(st))
        alert_count += len(alerts)
        for alert in alerts:
          plugin_id = alert.get('pluginId')
          if plugin_id in blacklist:
            continue
          if not is_in_scope(plugin_id, alert.get('url')):
            continue
          if alert.get('risk') == 'Informational':
            # Ignore all info alerts - some of them may have been downgraded by security annotations
            continue
          if (not alert_dict.has_key(plugin_id)):
            alert_dict[plugin_id] = []
          alert_dict[plugin_id].append(alert)
        st += pg
        alerts = zap.core.alerts(start=st, count=pg)

      logging.debug ('Total number of alerts: ' + str(alert_count))

      all_ascan_rules = zap.ascan.scanners('Default Policy')
      all_pscan_rules = zap.pscan.scanners
      all_dict = {}
      for rule in all_pscan_rules:
        plugin_id = rule.get('id')
        if plugin_id in blacklist:
          continue
        all_dict[plugin_id] = rule.get('name') + ' - Passive/' + rule.get('quality')
      for rule in all_ascan_rules:
        plugin_id = rule.get('id')
        if plugin_id in blacklist:
          continue
        all_dict[plugin_id] = rule.get('name') + ' - Active/' + rule.get('quality')

      if len(generate) > 0:
        # Create the config file
        with open(base_dir + generate, 'w') as f:
          f.write ('# zap-api-scan rule configuration file\n')
          f.write ('# Change WARN to IGNORE to ignore rule or FAIL to fail if rule matches\n')
          f.write ('# Active scan rules set to IGNORE will not be run which will speed up the scan\n')
          f.write ('# Only the rule identifiers are used - the names are just for info\n')
          f.write ('# You can add your own messages to each rule by appending them after a tab on each line.\n')
          for key, rule in sorted(all_dict.iteritems()):
            f.write (key + '\tWARN\t(' + rule + ')\n')

      # print out the passing rules
      pass_dict = {}
      for rule in all_pscan_rules:
        plugin_id = rule.get('id')
        if plugin_id in blacklist:
          continue
        if (not alert_dict.has_key(plugin_id)):
          pass_dict[plugin_id] = rule.get('name')
      for rule in all_ascan_rules:
        plugin_id = rule.get('id')
        if plugin_id in blacklist:
          continue
        if not alert_dict.has_key(plugin_id) and not (config_dict.has_key(plugin_id) and config_dict[plugin_id] == 'IGNORE'):
          pass_dict[plugin_id] = rule.get('name')

      if min_level == levels.index("PASS") and detailed_output:
        for key, rule in sorted(pass_dict.iteritems()):
          print ('PASS: ' + rule + ' [' + key + ']')

      pass_count = len(pass_dict)

      if detailed_output:
        # print out the ignored ascan rules (there will be no alerts for these as they were not run)
        for rule in all_ascan_rules:
          plugin_id = rule.get('id')
          if plugin_id in blacklist:
            continue
          if config_dict.has_key(plugin_id) and config_dict[plugin_id] == 'IGNORE':
            print ('SKIP: ' + rule.get('name') + ' [' + plugin_id + ']')

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
          if key in in_progress_issues: 
            warn_inprog_count += 1
          else:
            warn_count += 1

      # print out the failing rules
      for key, alert_list in sorted(alert_dict.iteritems()):
        if config_dict.has_key(key) and config_dict[key] == 'FAIL':
          user_msg = ''
          if key in config_msg:
            user_msg = config_msg[key]
          print_rule(config_dict[key], alert_list, detailed_output, user_msg)
          if key in in_progress_issues: 
            fail_inprog_count += 1
          else:
            fail_count += 1
          
      if len(report_html) > 0:
        # Save the report
        with open(base_dir + report_html, 'w') as f:
          f.write (zap.core.htmlreport())

      if len(report_md) > 0:
        # Save the report
        with open(base_dir + report_md, 'w') as f:
          f.write (zap.core.mdreport())

      if len(report_xml) > 0:
        # Save the report
        with open(base_dir + report_xml, 'w') as f:
          f.write (zap.core.xmlreport())

      print ('FAIL-NEW: ' + str(fail_count) + '\tFAIL-INPROG: ' + str(fail_inprog_count) + 
        '\tWARN-NEW: ' + str(warn_count) + '\tWARN-INPROG: ' + str(warn_inprog_count) +
        '\tINFO: ' + str(info_count) + '\tIGNORE: ' + str(ignore_count) + '\tPASS: ' + str(pass_count))

    # Stop ZAP
    zap.core.shutdown()

  except IOError as (errno, strerror):
    print("ERROR " + str(strerror))
    logging.warning ('I/O error(' + str(errno) + '): ' + str(strerror))
    dump_log_file(cid)

  except:
    print("ERROR " + str(sys.exc_info()[0]))
    logging.warning ('Unexpected error: ' + str(sys.exc_info()[0]))
    dump_log_file(cid)

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

