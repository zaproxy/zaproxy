#!/usr/bin/env python
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

import BaseHTTPServer
import getopt
import os.path
from BaseHTTPServer import BaseHTTPRequestHandler
from datetime import datetime
from pprint import pprint
from urllib import urlopen

from zapv2 import ZAPv2

from zap_common import *

config_dict = {}
config_msg = {}
out_of_scope_dict = {}
min_level = 0

# Pscan rules that aren't really relevant, eg the examples rules in the alpha set
blacklist = ['-1', '50003', '60000', '60001']

# Pscan rules that are being addressed
in_progress_issues = {}

logging.basicConfig(level=logging.INFO, format='%(asctime)s %(message)s')
# Hide "Starting new HTTP connection" messages
logging.getLogger("requests").setLevel(logging.WARNING)

http_alive = False


def launch_trigger_endpoint(trigger_host='0.0.0.0', trigger_port=0):
    """launch HTTP server waiting for a "tests finished start scanning" command
    when the command has been received, return
    """
    if trigger_port == 0:
        wrong_arg("You need to specify which port to listen to for the trigger command")
    run_http_server(trigger_host, trigger_port)


# HTTPRequestHandler class
class RequestHandler(BaseHTTPRequestHandler):
    # GET
    def do_GET(self):
        global http_alive
        if 'trigger' in self.path:
            # Send response status code
            http_alive = False
            self.send_response(200)
        else:
            self.send_response(404)


def run_http_server(address='0.0.0.0', port=8181):
    global http_alive
    try:
        logging.info('starting server...%s:%s' % (address, port))
        server_address = (address, int(port))
        httpd = BaseHTTPServer.HTTPServer(server_address, RequestHandler)
        logging.debug('Running Trigger server...')
        http_alive = True
        while http_alive:
            print(http_alive)
            httpd.handle_request()
        logging.debug('Killing httpd')
    except TypeError as te:
        pprint(te.message)
        traceback.print_exc()


def usage():
    print('Usage: zap-baseline.py -t <target> [options]')
    print('    -t target         target URL including the protocol, eg https://www.example.com')
    print('Options:')
    print('    -h                print this help message')
    print('    -c config_file    config file to use to INFO, IGNORE or FAIL warnings')
    print('    -u config_url     URL of config file to use to INFO, IGNORE or FAIL warnings')
    print('    -g gen_file       generate default config file (all rules set to WARN)')
    print('    -m mins           the number of minutes to spider for (default 1)')
    print('    -r report_html    file to write the full ZAP HTML report')
    print('    -w report_md      file to write the full ZAP Wiki (Markdown) report')
    print('    -x report_xml     file to write the full ZAP XML report')
    print('    -J report_json    file to write the full ZAP JSON document')
    print('    -a                include the alpha passive scan rules as well')
    print('    -d                show debug messages')
    print('    -P                specify listen port')
    print('    -D                delay in seconds to wait for passive scanning ')
    print('    -i                default rules not in the config file to INFO')
    print('    -I                do not return failure on warning')
    print('    -j                use the Ajax spider in addition to the traditional one')
    print(
        '    -l level          minimum level to show: PASS, IGNORE, INFO, WARN or FAIL, use with -s to hide example URLs')
    print('    -n context_file   context file which will be loaded prior to spidering the target')
    print('    -p progress_file  progress file which specifies issues that are being addressed')
    print('    -s                short output format - dont show PASSes or example URLs')
    print('    -T                max time in minutes to wait for ZAP to start and the passive scan to run')
    print('    -z zap_options    ZAP command line options e.g. -z "-config aaa=bbb -config ccc=ddd"')
    print(
        '    --noSpider no spider, just attack whatever is already in your history, useful for proxying e2e tests through zap"')
    print('    -sD spider_depth  spider depth to crawl"')
    print(
        '    --testMode prevents shutdown, expect external orchestration script to feed it commandes such as /attack or setup fuzzing etc.'
        'Will continuously print output"')
    print(
        '--triggerPort port  To be used in testMode, launches an HTTP server in the specified port and waits for a GET'
        'on that endpoint to start the attack')

    print('')
    print('For more details see https://github.com/zaproxy/zaproxy/wiki/ZAP-Baseline-Scan')


def print_ps_aux():
    try:
        import subprocess
        import shlex
        print('Calling ps aux')
        proc1 = subprocess.check_output(['ps', ' aux'], shell=True)
        pprint(proc1)
    except Exception as e:
        traceback.print_exc()


def check_if_zap_is_running_and_genocide():
    try:
        import subprocess
        import shlex
        proc1 = subprocess.check_output(['ps', 'aux'], shell=True)
        pprint(proc1)
        # proc2 = subprocess.check_output(['grep', ' zap'], stdin=io.BytesIO(proc1), shell=True)
        # pprint(proc2)
        if 'java' in proc1:
            subprocess.Popen(shlex.split('killall -9 java'))
    except Exception as e:
        traceback.print_exc()


def handle_results(zap=None, all_rules=None, alert_dict={}, zap_conf_lvls=None, detailed_output=False,
                   info_unspecified=False, report_html=None, report_json=None, report_md=None, report_xml=None,
                   base_dir=None):
    global blacklist
    # print out the passing rules
    pass_dict = {}
    for rule in all_rules:
        plugin_id = rule.get('id')
        if plugin_id in blacklist:
            continue
        if (plugin_id not in alert_dict):
            pass_dict[plugin_id] = rule.get('name')

    if min_level == zap_conf_lvls.index("PASS") and detailed_output:
        for key, rule in sorted(pass_dict.items()):
            print('PASS: ' + rule + ' [' + key + ']')

    pass_count = len(pass_dict)

    # print out the ignored rules
    ignore_count, not_used = print_rules(alert_dict, 'IGNORE', config_dict, config_msg, min_level,
                                         inc_ignore_rules, True, detailed_output, {})

    # print out the info rules
    info_count, not_used = print_rules(alert_dict, 'INFO', config_dict, config_msg, min_level,
                                       inc_info_rules, info_unspecified, detailed_output, in_progress_issues)

    # print out the warning rules
    warn_count, warn_inprog_count = print_rules(alert_dict, 'WARN', config_dict, config_msg, min_level,
                                                inc_warn_rules, not info_unspecified, detailed_output,
                                                in_progress_issues)

    # print out the failing rules
    fail_count, fail_inprog_count = print_rules(alert_dict, 'FAIL', config_dict, config_msg, min_level,
                                                inc_fail_rules, True, detailed_output, in_progress_issues)
    print('Writing reports')
    if report_html:
        # Save the report
        write_report(base_dir + report_html, zap.core.htmlreport())

    if report_json:
        # Save the report
        write_report(base_dir + report_json, zap.core.jsonreport())

    if report_md:
        # Save the report
        write_report(base_dir + report_md, zap.core.mdreport())

    if report_xml:
        # Save the report
        write_report(base_dir + report_xml, zap.core.xmlreport())

    print('FAIL-NEW: ' + str(fail_count) + '\tFAIL-INPROG: ' + str(fail_inprog_count) +
          '\tWARN-NEW: ' + str(warn_count) + '\tWARN-INPROG: ' + str(warn_inprog_count) +
          '\tINFO: ' + str(info_count) + '\tIGNORE: ' + str(ignore_count) + '\tPASS: ' + str(pass_count))


def wrong_arg(message=''):
    logging.warning(message)
    usage()
    sys.exit(3)


def setup(target=None, config_file=None, generate=False, report_html='', report_xml=None, report_json='',
          progress_file=None, context_file=None, port=0, config_url='', zap_alpha=False, zap_options=None, mins=0):
    base_dir = None

    # Check target supplied and ok
    if len(target) == 0:
        wrong_arg("You need to specify a target")

    if not (target.startswith('http://') or target.startswith('https://')):
        wrong_arg('Target must start with \'http://\' or \'https://\'')

    if running_in_docker():
        base_dir = '/zap/wrk/'
        if config_file or generate or report_html or report_xml or report_json or progress_file or context_file:
            # Check directory has been mounted
            if not os.path.exists(base_dir):
                wrong_arg('A file based option has been specified but the directory \'/zap/wrk\' is not mounted ')

    # Choose a random 'ephemeral' port and check its available if it wasn't specified with -P option
    if port == 0:
        port = get_free_port()

    logging.debug('Using port: ' + str(port))

    if config_file:
        # load config file from filestore
        with open(base_dir + config_file) as f:
            try:
                load_config(f, config_dict, config_msg, out_of_scope_dict)
            except ValueError as e:
                logging.warning("Failed to load config file " + base_dir + config_file + " " + str(e))
                sys.exit(3)
    elif config_url:
        # load config file from url
        try:
            load_config(urlopen(config_url).read().decode('UTF-8'), config_dict, config_msg, out_of_scope_dict)
        except ValueError as e:
            logging.warning("Failed to read configs from " + config_url + " " + str(e))
            sys.exit(3)
        except:
            logging.warning('Failed to read configs from ' + config_url)
            sys.exit(3)

    if progress_file:
        # load progress file from filestore
        with open(base_dir + progress_file) as f:
            progress = json.load(f)
            # parse into something more useful...
            # in_prog_issues = map of vulnid -> {object with everything in}
            for issue in progress["issues"]:
                if issue["state"] == "inprogress":
                    in_progress_issues[issue["id"]] = issue

    if running_in_docker():
        docker_setup(mins=mins, zap_alpha=zap_alpha, zap_options=zap_options, port=port)
    else:
        non_docker_setup(context_file=context_file, mins=mins, zap_alpha=zap_alpha, zap_options=zap_options, port=port)


def docker_setup(mins=0, zap_alpha=False, zap_options=None, port=0):
    try:
        params = [
            '-config', 'spider.maxDuration=' + str(mins),
            '-addonupdate',
            '-addoninstall', 'pscanrulesBeta']  # In case we're running in the stable container

        if zap_alpha:
            params.append('-addoninstall')
            params.append('pscanrulesAlpha')

        add_zap_options(params, zap_options)
        start_zap(port, params)

    except OSError:
        logging.warning('Failed to start ZAP :(')
        sys.exit(3)


def non_docker_setup(context_file=None, mins=0, zap_alpha=False, zap_options=None, port=0):
    # Not running in docker, so start one
    mount_dir = ''
    if context_file:
        mount_dir = os.path.dirname(os.path.abspath(context_file))

    params = [
        '-config', 'spider.maxDuration=' + str(mins),
        '-addonupdate']

    if (zap_alpha):
        params.extend(['-addoninstall', 'pscanrulesAlpha'])

    add_zap_options(params, zap_options)

    try:
        cid = start_docker_zap('owasp/zap2docker-weekly', port, params, mount_dir)
        zap_ip = ipaddress_for_cid(cid)
        logging.debug('Docker ZAP IP Addr: ' + zap_ip)
    except OSError:
        logging.warning('Failed to start ZAP in docker :(')
        sys.exit(3)


def main(argv):
    global min_level
    global in_progress_issues
    cid = ''
    context_file = ''
    progress_file = ''
    config_file = ''
    config_url = ''
    generate = ''
    mins = 1
    port = 0
    detailed_output = True
    report_html = ''
    report_md = ''
    report_xml = ''
    report_json = ''
    target = ''
    zap_alpha = False
    info_unspecified = False
    ajax = False
    base_dir = ''
    zap_ip = '0.0.0.0'
    zap_options = ''
    delay = 0
    timeout = 0
    ignore_warn = False
    ignore_fail = False
    spider = True
    testMode = False
    tests_trigger_port = 0
    active_scan = False

    pass_count = 0
    warn_count = 0
    fail_count = 0
    info_count = 0
    ignore_count = 0
    warn_inprog_count = 0
    fail_inprog_count = 0
    spider_depth = 0

    try:
        opts, args = getopt.getopt(argv, "t:c:u:g:m:n:r:J:w:x:l:hdaijp:sz:P:D:T:I:IF",
                                   ['spiderDepth=', 'noSpider', 'testMode', 'triggerPort=', 'activeScan'])
    except getopt.GetoptError as exc:
        wrong_arg('Invalid option ' + exc.opt + ' : ' + exc.msg)

    for opt, arg in opts:
        print("Parsing args: %s %s" % (opt, arg))
        if opt == '-h':
            usage()
            sys.exit(0)
        elif opt == '-t':
            target = arg
            logging.debug('Target: ' + target)
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
        elif opt == '-J':
            report_json = arg
        elif opt == '-w':
            report_md = arg
        elif opt == '-x':
            report_xml = arg
        elif opt == '-a':
            zap_alpha = True
        elif opt == '-i':
            info_unspecified = True
        elif opt == '-I':
            ignore_warn = True
        elif opt == '-IF':
            ignore_fail = True
        elif opt == '-j':
            ajax = True
        elif opt == '--spiderDepth':
            spider_depth = arg
        elif opt == '-l':
            try:
                min_level = zap_conf_lvls.index(arg)
            except ValueError:
                wrong_arg('Level must be one of ' + str(zap_conf_lvls))
        elif opt == '-z':
            zap_options = arg
        elif opt == '-s':
            detailed_output = False
        elif opt == '-T':
            timeout = int(arg)
        elif opt == '--noSpider':
            spider = False
        elif opt == '--testMode':
            testMode = True
        elif opt == '--triggerPort':
            tests_trigger_port = arg
        elif opt == '--activeScan':
            active_scan = True

    check_zap_client_version()

    setup(target=target, config_file=config_file, generate=generate, report_html=report_html, report_xml=report_xml,
          report_json=report_json, progress_file=progress_file, port=port,
          config_url=config_url, zap_alpha=zap_alpha, zap_options=zap_options, mins=mins)
    try:
        logging.debug("starting zap")
        zap = ZAPv2(
            proxies={'http': 'http://' + zap_ip + ':' + str(port), 'https': 'http://' + zap_ip + ':' + str(port)})

        logging.debug('Will wait %s for zap to start' % timeout)

        wait_for_zap_start(zap, timeout * 60)

        logging.debug('Zap started, configuring..')
        if context_file:
            # handle the context file, cant use base_dir as it might not have been set up
            res = zap.context.import_context('/zap/wrk/' + os.path.basename(context_file))
            if res.startswith("ZAP Error"):
                logging.error('Failed to load context file ' + context_file + ' : ' + res)

        logging.info('Accessing %s')
        zap_access_target(zap, target)

        if target.count('/') > 2:
            # The url can include a valid path, but always reset to spider the host
            target = target[0:target.index('/', 8) + 1]

        time.sleep(2)

        if testMode:
            # wait for the /attack command
            logging.debug("launching http server in port %s" % (tests_trigger_port))
            launch_trigger_endpoint(trigger_host=zap_ip, trigger_port=tests_trigger_port)

        if spider:
            # Spider target
            print("Spidering....")
            zap_spider(zap, target)
            # spider = zap.spider()
            # spider.set_option_max_depth(1,"")
            # spider.set_option_max_children(10,"")
            # spider.scan(url=target,maxchildren=10)

            if ajax:
                zap_ajax_spider(zap, target, mins)
        else:
            logging.debug("Spider Turned off")

        if delay:
            start_scan = datetime.now()
            while (datetime.now() - start_scan).seconds < delay:
                time.sleep(5)
                logging.debug(
                    'Delay passive scan check ' + str(delay - (datetime.now() - start_scan).seconds) + ' seconds')

        zap_wait_for_passive_scan(zap, timeout * 60)

        logging.debug("passive done, active scanning...")

        if active_scan:
            # unleash hell
            zap.ascan.enable_all_scanners()
            zap.ascan.scan(target, recurse=True, inscopeonly=False)

        # Print out a count of the number of urls
        num_urls = len(zap.core.urls())
        if num_urls == 0:
            logging.warning(
                'No URLs found - is the target URL accessible? Local services may not be accessible from the Docker container')
        else:
            if detailed_output:
                print('Total of ' + str(num_urls) + ' URLs')

            alert_dict = zap_get_alerts(zap, target, blacklist, out_of_scope_dict)

            all_rules = zap.pscan.scanners
            all_dict = {}
            for rule in all_rules:
                plugin_id = rule.get('id')
                if plugin_id in blacklist:
                    continue
                all_dict[plugin_id] = rule.get('name')

            if generate:
                # Create the config file
                with open(base_dir + generate, 'w') as f:
                    f.write('# zap-baseline rule configuration file\n')
                    f.write('# Change WARN to IGNORE to ignore rule or FAIL to fail if rule matches\n')
                    f.write('# Only the rule identifiers are used - the names are just for info\n')
                    f.write(
                        '# You can add your own messages to each rule by appending them after a tab on each line.\n')
                    for key, rule in sorted(all_dict.items()):
                        f.write(key + '\tWARN\t(' + rule + ')\n')

        handle_results(zap=zap, all_rules=all_rules, alert_dict=alert_dict, zap_conf_lvls=zap_conf_lvls,
                       detailed_output=detailed_output, info_unspecified=info_unspecified, report_html=report_html,
                       report_json=report_json, report_md=report_md, report_xml=report_xml, base_dir=base_dir)

        # Stop ZAP
        # print_ps_aux()
        print('Shutting down ZAP')
        zap.core.shutdown()
        print('ZAP is down')
        # time.sleep(10)
        # print_ps_aux()
        # check_if_zap_is_running_and_genocide()
        # print_ps_aux()

    except IOError as e:
        if hasattr(e, 'args') and len(e.args) > 1:
            errno, strerror = e.args
            print("ERROR " + str(strerror))
            logging.warning('I/O error(' + str(errno) + '): ' + str(strerror))
        else:
            print("ERROR %s" % e)
            logging.warning('I/O error: ' + str(e))
        dump_log_file(cid)

    except:
        print("ERROR " + str(sys.exc_info()[0]))
        logging.warning('Unexpected error: ' + str(sys.exc_info()[0]))
        # dump_log_file(cid)

    # print("stopping docker")
    # if running_in_docker():
    #     stop_docker(cid)
    print("Trying to exit")
    if (not ignore_fail) and fail_count > 0:
        print('exiting 1')
        sys.exit(1)
    elif (not ignore_warn) and warn_count > 0:
        print('exiting 2')
        sys.exit(2)
    elif pass_count > 0:
        print('exiting 0')
        sys.exit(0)
    else:
        pprint("pcount %s, ignore_warn %s, warn_count %s, ignore_fail %s, fail_count %s" % (
            str(pass_count), str(ignore_warn), str(warn_count),
            str(ignore_fail), str(fail_count)))
        print('exiting 3')
        sys.exit(3)


if __name__ == "__main__":
    main(sys.argv[1:])
