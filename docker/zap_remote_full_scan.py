#!/usr/bin/env python

import getopt
import os.path
from datetime import datetime
from pprint import pprint
from zapv2 import ZAPv2
import zap_common
import logging
import sys
import time
import argparse

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


def handle_results(zap=None, all_rules=None, alert_dict={}, zap_conf_lvls=None, detailed_output=False,
                   info_unspecified=False, report_html=None, report_json=None, report_md=None, report_xml=None,
                   ):
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
    ignore_count, not_used = zap_common.print_rules(alert_dict, 'IGNORE', config_dict, config_msg, min_level,
                                                    zap_common.inc_ignore_rules, True, detailed_output, {})

    # print out the info rules
    info_count, not_used = zap_common.print_rules(alert_dict, 'INFO', config_dict, config_msg, min_level,
                                                  zap_common.inc_info_rules, info_unspecified, detailed_output, in_progress_issues)

    # print out the warning rules
    warn_count, warn_inprog_count = zap_common.print_rules(alert_dict, 'WARN', config_dict, config_msg, min_level,
                                                           zap_common.inc_warn_rules, not info_unspecified, detailed_output,
                                                           in_progress_issues)

    # print out the failing rules
    fail_count, fail_inprog_count = zap_common.print_rules(alert_dict, 'FAIL', config_dict, config_msg, min_level,
                                                           zap_common.inc_fail_rules, True, detailed_output, in_progress_issues)
    print('Writing reports')
    if report_html:
        # Save the report
        zap_common.write_report(report_html, zap.core.htmlreport())

    if report_json:
        # Save the report
        report = zap.core.jsonreport()
        zap_common.write_report(report_json, report)
        from pprint import pprint
        pprint(report)

    if report_md:
        # Save the report
        zap_common.write_report(report_md, zap.core.mdreport())

    if report_xml:
        # Save the report
        zap_common.write_report(report_xml, zap.core.xmlreport())

    print('FAIL-NEW: ' + str(fail_count) + '\tFAIL-INPROG: ' + str(fail_inprog_count) +
          '\tWARN-NEW: ' + str(warn_count) + '\tWARN-INPROG: ' + str(warn_inprog_count) +
          '\tINFO: ' + str(info_count) + '\tIGNORE: ' + str(ignore_count) + '\tPASS: ' + str(pass_count))


def wrong_arg(message=''):
    logging.warning(message)
    sys.exit(3)


def main():
    parser = argparse.ArgumentParser(description='Description of your program',
                                     epilog='For more details see https://github.com/zaproxy/zaproxy/wiki/ZAP-Baseline-Scan')
    parser.add_argument(
        '-t', '--target', help='target URL including the protocol, eg https://www.example.com', required=True)
    parser.add_argument('-c', '--config_file',
                        help='config file to use to INFO, IGNORE or FAIL warnings', default=None)
    parser.add_argument(
        '-u', '--config_url', help='URL of config file to use to INFO, IGNORE or FAIL warnings', default=None)
    parser.add_argument(
        '-g', '--gen_file', help='generate default config file (all rules set to WARN)', default=False)
    parser.add_argument(
        '-m', '--mins', help='the number of minutes to spider for (default 1)', default=1)
    parser.add_argument('-r', '--report_html',
                        help='file to write the full ZAP HTML report', default=None)
    parser.add_argument(
        '-w', '--report_md', help='file to write the full ZAP Wiki (Markdown) report', default=None)
    parser.add_argument(
        '-x', '--report_xml', help='file to write the full ZAP XML report', default=None)
    parser.add_argument('-J', '--report_json',
                        help='file to write the full ZAP JSON document', default=None)
    parser.add_argument(
        '-d', '--debug', help='show debug messages', default=False)
    parser.add_argument(
        '--zPort', help='specify remote listen port', default=8080)
    parser.add_argument(
        '--zHost', help='host where zap listens on', required=True)
    parser.add_argument(
        '-D', '--delay', help='delay in seconds to wait for passive scanning ', default=0)
    parser.add_argument('-i', '--info_unspecified',
                        help='default rules not in the config file to INFO', default=False)
    parser.add_argument('-j', '--ajax_spider',
                        help='use the Ajax spider in addition to the traditional one', default=False)
    parser.add_argument(
        '-l', '--level', help='minimum level to show: PASS, IGNORE, INFO, WARN or FAIL, use with -s to hide example URLs', default="WARN")
    parser.add_argument('-n', '--context_file',
                        help='context file which will be loaded prior to spidering the target', default=None)
    parser.add_argument(
        '-s', '--short', help='short output format - dont show PASSes or example URLs', default=False)
    parser.add_argument(
        '--no_spider', help='no spider, just attack whatever is already in your history, useful for proxying e2e tests through zap"', default=False)
    parser.add_argument('--sD', '--spider_depth',
                        help='spider depth to crawl', default=0)
    args = parser.parse_args()

    target = args.target
    generate = args.gen_file
    if args.debug:
        logging.getLogger().setLevel(logging.DEBUG)
    else:
        logging.getLogger().setLevel(logging.INFO)
    mins = int(args.mins)
    port = int(args.zPort)
    zap_ip = args.zHost
    delay = int(args.delay)
    context_file = args.context_file
    report_html = args.report_html
    report_json = args.report_json
    report_md = args.report_md
    report_xml = args.report_xml
    info_unspecified = args.info_unspecified
    ajax = args.ajax_spider
    detailed_output = not args.short
    spider = not args.no_spider

    try:
        print("Connecting to zap at %s" %
              'https://' + zap_ip + ':' + str(port))
        zap = ZAPv2(
            proxies={'http': 'http://' + zap_ip + ':' + str(port), 'https': 'http://' + zap_ip + ':' + str(port)})

        print('Zap started, configuring..')
        if context_file:
            # handle the context file, cant use base_dir as it might not have been set up
            res = zap.context.import_context(
                '/zap/wrk/' + os.path.basename(context_file))
            if res.startswith("ZAP Error"):
                logging.error('Failed to load context file ' +
                              context_file + ' : ' + res)

                print('Accessing %s' % target)
        zap_common.zap_access_target(zap, target)
        print('Accessed target success!')
        if target.count('/') > 2:
            # The url can include a valid path, but always reset to spider the host
            target = target[0:target.index('/', 8) + 1]

        if spider:
            # Spider target
            print("Spidering....")
            zap_common.zap_spider(zap, target)

            if ajax:
                zap_common.zap_ajax_spider(zap, target, mins)
        else:
            print("Spider Turned off")

        if delay:
            start_scan = datetime.now()
            while (datetime.now() - start_scan).seconds < delay:
                time.sleep(5)
                print(
                    'Delay passive scan check ' + str(delay - (datetime.now() - start_scan).seconds) + ' seconds')

                zap_common.zap_wait_for_passive_scan(zap, delay * 60)

        print("passive done, active scanning...")
        zap.ascan.enable_all_scanners()
        zap.ascan.scan(target, recurse=True, inscopeonly=False)

        # Print out a count of the number of urls
        num_urls = len(zap.core.urls())
        if num_urls == 0:
            logging.warning(
                'No URLs found - is the target URL accessible? Local services may not be accessible from the pod')
        else:
            if detailed_output:
                print('Total of ' + str(num_urls) + ' URLs')

            alert_dict = zap_common.zap_get_alerts(
                zap, target, blacklist, out_of_scope_dict)

            all_rules = zap.pscan.scanners
            all_dict = {}
            for rule in all_rules:
                plugin_id = rule.get('id')
                if plugin_id in blacklist:
                    continue
                all_dict[plugin_id] = rule.get('name')

            if generate:
                # Create the config file
                with open(generate, 'w') as f:
                    f.write('# zap-baseline rule configuration file\n')
                    f.write(
                        '# Change WARN to IGNORE to ignore rule or FAIL to fail if rule matches\n')
                    f.write(
                        '# Only the rule identifiers are used - the names are just for info\n')
                    f.write(
                        '# You can add your own messages to each rule by appending them after a tab on each line.\n')
                    for key, rule in sorted(all_dict.items()):
                        f.write(key + '\tWARN\t(' + rule + ')\n')

        handle_results(zap=zap, all_rules=all_rules, alert_dict=alert_dict, zap_conf_lvls=zap_common.zap_conf_lvls,
                       detailed_output=detailed_output, info_unspecified=info_unspecified, report_html=report_html,
                       report_json=report_json, report_md=report_md, report_xml=report_xml)

        print('Shutting down ZAP')
        zap.core.shutdown()
        print('ZAP is down')

    except IOError as e:
        if hasattr(e, 'args') and len(e.args) > 1:
            errno, strerror = e.args
            print("ERROR " + str(strerror))
            logging.warning('I/O error(' + str(errno) + '): ' + str(strerror))
        else:
            print("ERROR %s" % e)
            logging.warning('I/O error: ' + str(e))

    except:
        print("ERROR " + str(sys.exc_info()[0]))
        logging.warning('Unexpected error: ' + str(sys.exc_info()[0]))
        raise
    print('exiting 0')
    exit(0)


if __name__ == "__main__":
    main()
