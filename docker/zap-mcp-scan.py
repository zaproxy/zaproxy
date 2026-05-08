#!/usr/bin/env python
# Zed Attack Proxy (ZAP) and its related class files.
#
# ZAP is an HTTP/HTTPS proxy for assessing web application security.
#
# Copyright 2026 ZAP Development Team
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

# This script runs a scan against a target MCP server using ZAP.
#
# It must be run inside a ZAP Docker container and uses the Automation
# Framework with a plan built programmatically.
#
# It will exit with codes of:
#	0:	Success
#	1:	At least 1 FAIL
#	2:	At least one WARN and no FAILs
#	3:	Any other failure
# By default all alerts found by ZAP will be treated as WARNings.

import getopt
import json
import logging
import os
import sys
import yaml
from pathlib import Path
from shutil import copyfile
from zap_common import *


logging.basicConfig(level=logging.INFO, format='%(asctime)s %(message)s')
logging.getLogger("requests").setLevel(logging.WARNING)


def usage():
    print('Usage: zap-mcp-scan.py -t <target> [options]')
    print('    -t target         target MCP server URL, e.g. https://www.example.com/mcp')
    print('Options:')
    print('    -h                print this help message')
    print('    -K security_key   security key for the MCP server')
    print('    -r report_html    file to write the full ZAP HTML report')
    print('    -w report_md      file to write the full ZAP Wiki (Markdown) report')
    print('    -x report_xml     file to write the full ZAP XML report')
    print('    -J report_json    file to write the full ZAP JSON document')
    print('    -d                show debug messages')
    print('    -P                specify listen port')
    print('    -T mins           max time in minutes to wait for ZAP to start and the scan to run')
    print('    -I                do not return failure on warning')
    print('    -z zap_options    ZAP command line options e.g. -z "-config aaa=bbb -config ccc=ddd"')
    print('    --plan-only       generate an automation framework plan but do not run it')
    print('')
    print('For more details see https://www.zaproxy.org/docs/docker/mcp-scan/')


def main(argv):
    port = 0
    target = ''
    security_key = ''
    report_html = ''
    report_md = ''
    report_xml = ''
    report_json = ''
    zap_options = ''
    timeout = 0
    ignore_warn = False
    debug = False
    plan_only = False
    base_dir = '/zap/wrk/'

    try:
        opts, args = getopt.getopt(argv, "t:K:r:J:w:x:hdP:T:Iz:", ["plan-only"])
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
        elif opt == '-K':
            security_key = arg
        elif opt == '-r':
            report_html = arg
        elif opt == '-J':
            report_json = arg
        elif opt == '-w':
            report_md = arg
        elif opt == '-x':
            report_xml = arg
        elif opt == '-d':
            logging.getLogger().setLevel(logging.DEBUG)
            debug = True
        elif opt == '-P':
            port = int(arg)
        elif opt == '-T':
            timeout = int(arg)
        elif opt == '-I':
            ignore_warn = True
        elif opt == '-z':
            zap_options = arg
        elif opt == '--plan-only':
            plan_only = True

    if not running_in_docker() and not plan_only:
        logging.warning('This script must be run inside a ZAP Docker container')
        sys.exit(3)

    if len(target) == 0:
        usage()
        sys.exit(3)

    if not (target.startswith('http://') or target.startswith('https://')):
        logging.warning('Target must start with \'http://\' or \'https://\'')
        usage()
        sys.exit(3)

    if report_html or report_xml or report_json or report_md:
        if not os.path.exists(base_dir):
            logging.warning('A file based option has been specified but the directory \'/zap/wrk\' is not mounted ')
            usage()
            sys.exit(3)

    if port == 0:
        port = get_free_port()

    logging.debug('Using port: ' + str(port))

    home_dir = str(Path.home())
    summary_file = os.path.join(home_dir, 'zap_out.json')

    jobs = [
        get_af_pscan_config(),
        get_af_mcp_import(target, security_key),
        get_af_pscan_wait(0),
        get_af_active_scan('API'),
        get_af_output_summary('Long', summary_file, {}, {})
    ]

    if report_html:
        jobs.append(get_af_report('traditional-html', base_dir, report_html, 'ZAP MCP Scan Report', ''))
    if report_md:
        jobs.append(get_af_report('traditional-md', base_dir, report_md, 'ZAP MCP Scan Report', ''))
    if report_xml:
        jobs.append(get_af_report('traditional-xml', base_dir, report_xml, 'ZAP MCP Scan Report', ''))
    if report_json:
        jobs.append(get_af_report('traditional-json', base_dir, report_json, 'ZAP MCP Scan Report', ''))

    plan = {
        'env': {
            'contexts': [{'name': 'mcp-scan', 'urls': [target]}],
            'parameters': {
                'failOnError': True,
                'progressToStdout': debug
            }
        },
        'jobs': jobs
    }

    if plan_only:
        yaml_file = os.path.join(base_dir, 'zap-mcp.yaml')
        print('Generating the Automation Framework plan only: zap-mcp.yaml')
        with open(yaml_file, 'w') as f:
            yaml.dump(plan, f)
        sys.exit(0)

    yaml_file = os.path.join(home_dir, 'zap-mcp.yaml')
    with open(yaml_file, 'w') as f:
        yaml.dump(plan, f)

    if os.path.exists(base_dir):
        try:
            copyfile(yaml_file, os.path.join(base_dir, 'zap-mcp.yaml'))
        except OSError as err:
            logging.warning('Unable to copy plan to ' + base_dir + ' ' + str(err))

    try:
        if "-silent" not in zap_options:
            run_zap_inline(port, ['-addonupdate'])

        params = ['-autorun', yaml_file, '-config', 'stats.pkg.mcpscan=1']
        add_zap_options(params, zap_options)

        out = run_zap_inline(port, params)

        ignore_strs = ["Found Java version", "Available memory", "Using JVM args", "Add-on already installed",
                       "[main] INFO", "Automation plan succeeded"]

        for line in out.splitlines():
            if any(x in line for x in ignore_strs):
                continue
            print(line)

    except OSError:
        logging.warning('Failed to start ZAP :(')
        sys.exit(3)

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


if __name__ == "__main__":
    main(sys.argv[1:])
