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
# It depends on https://pypi.python.org/pypi/python-owasp-zap-v2 and Docker.
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
# You can use the -c parameter to specify a configuration file to override this.
# You can generate a template configuration file using the -g parameter. You will
# then need to change 'WARN' to 'FAIL' or 'IGNORE' for the rules you want to be
# handled differently.

import getopt
import json
import logging
import socket
import subprocess
import sys
import time
import traceback
from datetime import datetime
from random import randint
from zapv2 import ZAPv2

timeout = 120

# Pscan rules that aren't really relevant, eg example alpha rules
blacklist = ['-1', '50003', '60000', '60001']

logging.basicConfig(level=logging.INFO)

def usage():
    print ('Usage: zap-baseline.py -t <target> [options]')
    print ('    -t target         target URL including the protocol, eg https://www.example.com')
    print ('Options:')
    print ('    -c config_file    config file to use to IGNORE or FAIL warnings')
    print ('    -g gen_file       generate default config file (all rules set to WARN)')
    print ('    -m mins           the number of minutes to spider for (default 1)')
    print ('    -r report         file to write the full ZAP HTML report')
    print ('    -a                include the alpha passive scan rules as well')
    print ('    -d                show debug messages')
    print ('    -s                short output format - dont show PASSes or example URLs')

def main(argv):
  config = ''
  generate = ''
  mins = 1
  port = 0
  detailed_output = True
  report = ''
  target = ''
  zap_alpha = False

  pass_count = 0
  warn_count = 0
  fail_count = 0
  ignore_count = 0

  try:
    opts, args = getopt.getopt(argv,"t:c:g:m:r:das")
  except getopt.GetoptError:
    usage()
    sys.exit(3)

  for opt, arg in opts:
    if opt == '-t':
      target = arg
      logging.debug ('Target: ' + target)
    elif opt == '-c':
      config = arg
    elif opt == '-g':
      generate = arg
    elif opt == '-d':
      logging.getLogger().setLevel(logging.DEBUG)
    elif opt == '-m':
      mins = int(arg)
    elif opt == '-r':
      report = arg
    elif opt == '-a':
      zap_alpha = True
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

  # Choose a random 'ephemeral' port and check its available
  while True:
    port = randint(32768, 61000)
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    if not (sock.connect_ex(('127.0.0.1', port)) == 0):
      # Its free:)
      break

  logging.debug ('Using port: ' + str(port))

  config_dict = {}
  if len(config) > 0:
    # load config file
    with open(config) as f:
      for line in f:
        if not line.startswith('#') and len(line) > 1:
          (key, val, ignore) = line.split('\t')
          config_dict[key] = val

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
              'zap.sh', '-daemon', 
              '-port', str(port), 
              '-host', '0.0.0.0', 
              '-config', 'api.disablekey=true', 
              '-config', 'spider.maxDuration=' + str(mins)]

    if (zap_alpha):
      params.append('-addoninstall')
      params.append('pscanrulesAlpha')

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

    start = datetime.now()
    while (int(zap.spider.status(spider_scan_id)) < 100):
      if (datetime.now() - start).seconds > ((mins * 60) + 10):
        # TODO HACK to cope with API not recognising when spider has finished due to exceeding maxDuration
        # Can be removed once the underlying fix is included in the ZAP Weekly release
        break
      logging.debug ('Spider progress %: ' + zap.spider.status(spider_scan_id))
      time.sleep(5)
    logging.debug ('Spider complete')

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
      # Retrieve the alerts
      alert_dict = {}
      alerts = zap.core.alerts()
      for alert in alerts:
        plugin_id = alert.get('pluginId')
        if plugin_id in blacklist:
          continue
        if (not alert_dict.has_key(plugin_id)):
          alert_dict[plugin_id] = []
        alert_dict[plugin_id].append(alert)

      all_rules = zap.pscan.scanners
      all_dict = {}
      for rule in all_rules:
        plugin_id = rule.get('id')
        if plugin_id in blacklist:
          continue
        all_dict[plugin_id] = rule.get('name')

      if len(generate) > 0:
        # Create the config file
        with open(generate, 'w') as f:
          f.write ('# zap-baseline rule configuraion file\n')
          f.write ('# change WARN to IGNORE to ignore rule or FAIL to fail if rule matches\n')
          f.write ('# only the rule identifiers are used - the names are just for info\n')
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

      if detailed_output:
        for key, rule in sorted(pass_dict.iteritems()):
          print ('PASS: ' + rule + ' [' + key + ']')

      pass_count = len(pass_dict)

      # print out the failing rules
      for key, alert_list in sorted(alert_dict.iteritems()):
        if config_dict.has_key(key) and config_dict[key] == 'IGNORE':
          action = 'IGNORE'
          ignore_count += 1
        elif config_dict.has_key(key) and config_dict[key] == 'FAIL':
          action = 'FAIL'
          fail_count += 1
        else:
          action = 'WARN'
          warn_count += 1
          
        print (action + ': ' + alert_list[0].get('alert') + ' [' + alert_list[0].get('pluginId') + '] x ' + str(len(alert_list)))
        if detailed_output:
          # Show (up to) first 5 urls
          for alert in alert_list[0:5]:
            print ('\t' + alert.get('url'))

      if len(report) > 0:
        # Save the report
        with open(report, 'w') as f:
          f.write (zap.core.htmlreport())

      print ('FAIL: ' + str(fail_count) + '\tWARN: ' + str(warn_count) + '\tIGNORE: ' + str(ignore_count) + '\tPASS: ' + str(pass_count))

    # Stop ZAP
    zap.core.shutdown()

  except IOError as (errno, strerror):
    logging.warning ('I/O error(' + str(errno) + '): ' + strerror)
    traceback.print_exc()
  except:
    logging.warning ('Unexpected error: ' + str(sys.exc_info()[0]))
    traceback.print_exc()

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

