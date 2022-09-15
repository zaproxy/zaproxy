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

import getopt
import json
import logging
import os
import os.path
import sys
import time
import yaml
from datetime import datetime
from pathlib import Path
from shutil import copyfile
from zapv2 import ZAPv2
from zap_common import *


config_dict = {}
config_msg = {}
out_of_scope_dict = {}
min_level = 0

# Pscan rules that aren't really relevant, e.g. the examples rules in the alpha set
ignore_scan_rules = ['-1', '50003', '60000', '60001']

# Pscan rules that are being addressed
in_progress_issues = {}

logging.basicConfig(level=logging.INFO, format='%(asctime)s %(message)s')
# Hide "Starting new HTTP connection" messages
logging.getLogger("requests").setLevel(logging.WARNING)


def usage():
    print('Usage: zap-baseline.py -t <target> [options]')
    print('    -t target         target URL including the protocol, e.g. https://www.example.com')
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
    print('    -D secs           delay in seconds to wait for passive scanning ')
    print('    -i                default rules not in the config file to INFO')
    print('    -I                do not return failure on warning')
    print('    -j                use the Ajax spider in addition to the traditional one')
    print('    -l level          minimum level to show: PASS, IGNORE, INFO, WARN or FAIL, use with -s to hide example URLs')
    print('    -n context_file   context file which will be loaded prior to spidering the target')
    print('    -p progress_file  progress file which specifies issues that are being addressed')
    print('    -s                short output format - dont show PASSes or example URLs')
    print('    -T mins           max time in minutes to wait for ZAP to start and the passive scan to run')
    print('    -U user           username to use for authenticated scans - must be defined in the given context file')
    print('    -z zap_options    ZAP command line options e.g. -z "-config aaa=bbb -config ccc=ddd"')
    print('    --hook            path to python file that define your custom hooks')
    print('    --auto            use the automation framework if supported for the given parameters (this is now the default)')
    print('    --autooff         do not use the automation framework even if supported for the given parameters')
    print('')
    print('For more details see https://www.zaproxy.org/docs/docker/baseline-scan/')

'''
    This script is in the process of being converted to use the Automation Framework.
    If you map a directory to /zap/wrk then the zap.yaml file generated will be copied to that directory.
    
    The following parameters are currently supported:
    
    -c config_file
    -u config_url
    -m mins
    -r report_html
    -w report_md
    -x report_xml
    -J report_json
    -a
    -d
    -P
    -I
    -j
    -s
    -T mins
    -z zap_options

    The following parameters are partially supported. 
    If you specify the '--auto' flag _before_ using them then the Automation Framework will be used:

    Currently none.
    
    If any of the next set of parameters are used then the existing code will be used instead:
    
    -D secs           need new delay/sleep job
    -i                need to support config files
    -l level          ditto
    -n context file   will need full context support in the AF
    -p progress_file  need to support config files
    -U user           will need full context support in the AF
    --hook            will need scripting support in the AF
    -g gen_file       may never support
    --autooff         will never support, may remove at some point
    
     
'''

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
    zap_ip = 'localhost'
    zap_options = ''
    delay = 0
    timeout = 0
    ignore_warn = False
    hook_file = ''
    user = ''
    use_af = True
    af_supported = True
    af_override = False

    pass_count = 0
    warn_count = 0
    fail_count = 0
    info_count = 0
    ignore_count = 0
    warn_inprog_count = 0
    fail_inprog_count = 0
    exception_raised = False
    debug = False

    try:
        opts, args = getopt.getopt(argv, "t:c:u:g:m:n:r:J:w:x:l:hdaijp:sz:P:D:T:IU:", ["hook=", "auto", "autooff"])
    except getopt.GetoptError as exc:
        logging.warning('Invalid option ' + exc.opt + ' : ' + exc.msg)
        usage()
        sys.exit(3)

    for opt, arg in opts:
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
            af_supported = False
        elif opt == '-d':
            logging.getLogger().setLevel(logging.DEBUG)
            debug = True
        elif opt == '-m':
            mins = int(arg)
        elif opt == '-P':
            port = int(arg)
        elif opt == '-D':
            delay = int(arg)
            af_supported = False
        elif opt == '-n':
            context_file = arg
            af_supported = False
        elif opt == '-p':
            progress_file = arg
            af_supported = False
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
            af_supported = False
        elif opt == '-I':
            ignore_warn = True
        elif opt == '-j':
            ajax = True
        elif opt == '-l':
            try:
                min_level = zap_conf_lvls.index(arg)
            except ValueError:
                logging.warning('Level must be one of ' + str(zap_conf_lvls))
                usage()
                sys.exit(3)
            af_supported = False
        elif opt == '-z':
            zap_options = arg
        elif opt == '-s':
            detailed_output = False
        elif opt == '-T':
            timeout = int(arg)
        elif opt == '-U':
            user = arg
            af_supported = False
        elif opt == '--hook':
            hook_file = arg
            af_supported = False
        elif opt == '--auto':
            use_af = True
            af_override = True
        elif opt == '--autooff':
            use_af = False

    check_zap_client_version()

    load_custom_hooks(hook_file)
    trigger_hook('cli_opts', opts)

    # Check target supplied and ok
    if len(target) == 0:
        usage()
        sys.exit(3)

    if not (target.startswith('http://') or target.startswith('https://')):
        logging.warning('Target must start with \'http://\' or \'https://\'')
        usage()
        sys.exit(3)

    if running_in_docker():
        base_dir = '/zap/wrk/'
        if config_file or generate or report_html or report_xml or report_json or report_md or progress_file or context_file:
            # Check directory has been mounted
            if not os.path.exists(base_dir):
                logging.warning('A file based option has been specified but the directory \'/zap/wrk\' is not mounted ')
                usage()
                sys.exit(3)

    if user and not context_file:
        logging.warning('A context file must be specified (and include the user) if the user option is selected')
        usage()
        sys.exit(3)

    # Choose a random 'ephemeral' port and check its available if it wasn't specified with -P option
    if port == 0:
        port = get_free_port()

    logging.debug('Using port: ' + str(port))

    if config_file:
        # load config file from filestore
        config_file = os.path.join(base_dir, config_file)
        with open(config_file) as f:
            try:
                load_config(f, config_dict, config_msg, out_of_scope_dict)
            except ValueError as e:
                logging.warning("Failed to load config file " + config_file + " " + str(e))
                sys.exit(3)
    elif config_url:
        # load config file from url
        try:
            config_data = urlopen(config_url).read().decode('UTF-8').splitlines()
            load_config(config_data, config_dict, config_msg, out_of_scope_dict)
        except ValueError as e:
            logging.warning("Failed to read configs from " + config_url + " " + str(e))
            sys.exit(3)
        except:
            logging.warning('Failed to read configs from ' + config_url)
            sys.exit(3)

    if progress_file:
        # load progress file from filestore
        with open(os.path.join(base_dir, progress_file)) as f:
            progress = json.load(f)
            # parse into something more useful...
            # in_prog_issues = map of vulnid -> {object with everything in}
            for issue in progress["issues"]:
                if issue["state"] == "inprogress":
                    in_progress_issues[issue["id"]] = issue

    if running_in_docker():
        if use_af and af_supported:
            print('Using the Automation Framework')

            # Generate the yaml file
            home_dir = str(Path.home())
            yaml_file = os.path.join(home_dir, 'zap.yaml')
            summary_file = os.path.join(home_dir, 'zap_out.json')
            
            with open(yaml_file, 'w') as yf:

                # Add the top level to the scope for backwards compatibility
                top_levels = [ target ]
                if target.count('/') > 2:
                    # The url can include a valid path, but always reset to spider the host (backwards compatibility)
                    t2 = target[0:target.index('/', 8)+1]
                    if not t2 == target:
                        target = t2
                        top_levels.append(target)
               
                yaml.dump(get_af_env(top_levels, out_of_scope_dict, debug), yf)
                
                alertFilters = []
                
                # Handle id specific alertFilters - rules that apply to all IDs are excluded from the env
                for id in out_of_scope_dict:
                    if id != '*':
                        for regex in out_of_scope_dict[id]:
                            alertFilters.append({'ruleId': id, 'newRisk': 'False Positive', 'url': regex.pattern, 'urlRegex': True})
                
                jobs = [get_af_pscan_config()]

                if len(alertFilters) > 0:
                    jobs.append(get_af_alertFilter(alertFilters))
                
                jobs.append(get_af_spider(target, mins))

                if ajax:
                    jobs.append(get_af_spiderAjax(target, mins))
                
                jobs.append(get_af_pscan_wait(timeout))
                jobs.append(get_af_output_summary(('Short', 'Long')[detailed_output], summary_file, config_dict, config_msg))
                
                if report_html:
                    jobs.append(get_af_report('traditional-html', base_dir, report_html, 'ZAP Scanning Report', ''))
            
                if report_md:
                    jobs.append(get_af_report('traditional-md', base_dir, report_md, 'ZAP Scanning Report', ''))
            
                if report_xml:
                    jobs.append(get_af_report('traditional-xml', base_dir, report_xml, 'ZAP Scanning Report', ''))
            
                if report_json:
                    jobs.append(get_af_report('traditional-json', base_dir, report_json, 'ZAP Scanning Report', ''))
            
                yaml.dump({'jobs': jobs}, yf)
                
                if os.path.exists('/zap/wrk'):
                    yaml_copy_file = '/zap/wrk/zap.yaml'
                    try:
                        # Write the yaml file to the mapped directory, if there is one
                        copyfile(yaml_file, yaml_copy_file)
                    except OSError as err:
                        logging.warning('Unable to copy yaml file to ' + yaml_copy_file + ' ' + str(err))
 
            try:
                # Run ZAP inline to update the add-ons
                install_opts = ['-addonupdate', '-addoninstall', 'pscanrulesBeta']
                if zap_alpha:
                    install_opts.append('-addoninstall')
                    install_opts.append('pscanrulesAlpha')
            
                run_zap_inline(port, install_opts)
                
                # Run ZAP inline with the yaml file
                params = ['-autorun', yaml_file]
    
                add_zap_options(params, zap_options)
    
                out = run_zap_inline(port, params)
                
                ignore_strs = ["Found Java version", "Available memory", "Using JVM args", "Add-on already installed", "[main] INFO", 
                               "Automation plan succeeded"]
                
                for line in out.splitlines():
                    if any(x in line for x in ignore_strs):
                        continue
                    print(line)
    
            except OSError:
                logging.warning('Failed to start ZAP :(')
                sys.exit(3)
    
            # Read the status file to find out what code we should exit with
            if not os.path.isfile(summary_file): 
                logging.warning('Failed to access summary file ' + summary_file)
                sys.exit(3)

            try:
                with open(summary_file) as f:
                    summary_data = json.load(f)
                    
                    if summary_data['fail'] > 0:
                        sys.exit(1)
                    elif (not ignore_warn) and summary_data['warn'] > 0:
                        sys.exit(2)
                    elif summary_data['pass'] > 0:
                        sys.exit(0)
                    else:
                        sys.exit(3)
            except IOError:
                logging.warning('Failed to read summary file ' + summary_file)

            sys.exit(3)
           
        else:
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

    else:
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

    try:
        zap = ZAPv2(proxies={'http': 'http://' + zap_ip + ':' + str(port), 'https': 'http://' + zap_ip + ':' + str(port)})

        wait_for_zap_start(zap, timeout * 60)
        trigger_hook('zap_started', zap, target)

        # Make suitable performance tweaks for running in this environment
        zap_tune(zap)
        trigger_hook('zap_tuned', zap)

        if context_file:
            # handle the context file, cant use base_dir as it might not have been set up
            zap_import_context(zap, os.path.join('/zap/wrk/', context_file))
            if (user):
                zap_set_scan_user(zap, user)

        zap_access_target(zap, target)

        if target.count('/') > 2:
            # The url can include a valid path, but always reset to spider the host
            target = target[0:target.index('/', 8)+1]

        time.sleep(2)

        # Spider target
        zap_spider(zap, target)

        if (ajax):
            zap_ajax_spider(zap, target, mins)

        if (delay):
            start_scan = datetime.now()
            while ((datetime.now() - start_scan).seconds < delay):
                time.sleep(5)
                logging.debug('Delay passive scan check ' + str(delay - (datetime.now() - start_scan).seconds) + ' seconds')

        zap_wait_for_passive_scan(zap, timeout * 60)

        # Print out a count of the number of urls
        num_urls = len(zap.core.urls())
        if num_urls == 0:
            logging.warning('No URLs found - is the target URL accessible? Local services may not be accessible from the Docker container')
        else:
            if detailed_output:
                print('Total of ' + str(num_urls) + ' URLs')

            alert_dict = zap_get_alerts(zap, target, ignore_scan_rules, out_of_scope_dict)

            all_rules = zap.pscan.scanners
            all_dict = {}
            for rule in all_rules:
                plugin_id = rule.get('id')
                if plugin_id in ignore_scan_rules:
                    continue
                all_dict[plugin_id] = rule.get('name')

            if generate:
                # Create the config file
                with open(os.path.join(base_dir, generate), 'w') as f:
                    f.write('# zap-baseline rule configuration file\n')
                    f.write('# Change WARN to IGNORE to ignore rule or FAIL to fail if rule matches\n')
                    f.write('# Only the rule identifiers are used - the names are just for info\n')
                    f.write('# You can add your own messages to each rule by appending them after a tab on each line.\n')
                    for key, rule in sorted(all_dict.items()):
                        f.write(key + '\tWARN\t(' + rule + ')\n')

            # print out the passing rules
            pass_dict = {}
            for rule in all_rules:
                plugin_id = rule.get('id')
                if plugin_id in ignore_scan_rules:
                    continue
                if (plugin_id not in alert_dict):
                    pass_dict[plugin_id] = rule.get('name')

            if min_level == zap_conf_lvls.index("PASS") and detailed_output:
                for key, rule in sorted(pass_dict.items()):
                    print('PASS: ' + rule + ' [' + key + ']')

            pass_count = len(pass_dict)

            # print out the ignored rules
            ignore_count, not_used = print_rules(zap, alert_dict, 'IGNORE', config_dict, config_msg, min_level,
                inc_ignore_rules, True, detailed_output, {})

            # print out the info rules
            info_count, not_used = print_rules(zap, alert_dict, 'INFO', config_dict, config_msg, min_level,
                inc_info_rules, info_unspecified, detailed_output, in_progress_issues)

            # print out the warning rules
            warn_count, warn_inprog_count = print_rules(zap, alert_dict, 'WARN', config_dict, config_msg, min_level,
                inc_warn_rules, not info_unspecified, detailed_output, in_progress_issues)

            # print out the failing rules
            fail_count, fail_inprog_count = print_rules(zap, alert_dict, 'FAIL', config_dict, config_msg, min_level,
                inc_fail_rules, True, detailed_output, in_progress_issues)

            if report_html:
                # Save the report
                write_report(os.path.join(base_dir, report_html), zap.core.htmlreport())

            if report_json:
                # Save the report
                write_report(os.path.join(base_dir, report_json), zap.core.jsonreport())

            if report_md:
                # Save the report
                write_report(os.path.join(base_dir, report_md), zap.core.mdreport())

            if report_xml:
                # Save the report
                write_report(os.path.join(base_dir, report_xml), zap.core.xmlreport())

            print('FAIL-NEW: ' + str(fail_count) + '\tFAIL-INPROG: ' + str(fail_inprog_count) +
                '\tWARN-NEW: ' + str(warn_count) + '\tWARN-INPROG: ' + str(warn_inprog_count) +
                '\tINFO: ' + str(info_count) + '\tIGNORE: ' + str(ignore_count) + '\tPASS: ' + str(pass_count))

        trigger_hook('zap_pre_shutdown', zap)
        # Stop ZAP
        zap.core.shutdown()

    except UserInputException as e:
        exception_raised = True
        print("ERROR %s" % e)

    except ScanNotStartedException:
        exception_raised = True
        dump_log_file(cid)

    except IOError as e:
        exception_raised = True
        print("ERROR %s" % e)
        logging.warning('I/O error: ' + str(e))
        dump_log_file(cid)

    except:
        exception_raised = True
        print("ERROR " + str(sys.exc_info()[0]))
        logging.warning('Unexpected error: ' + str(sys.exc_info()[0]))
        dump_log_file(cid)

    if not running_in_docker():
        stop_docker(cid)

    trigger_hook('pre_exit', fail_count, warn_count, pass_count)

    if exception_raised:
        sys.exit(3)
    elif fail_count > 0:
        sys.exit(1)
    elif (not ignore_warn) and warn_count > 0:
        sys.exit(2)
    elif pass_count > 0:
        sys.exit(0)
    else:
        sys.exit(3)


if __name__ == "__main__":
    main(sys.argv[1:])
