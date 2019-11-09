#!/usr/bin/env python
import sys
import getopt
import os
import os.path
from datetime import datetime
from pprint import pprint
from http import server
import logging
import zap_common
from zapv2 import ZAPv2
import time
import json

config_dict = {}
config_msg = {}
out_of_scope_dict = {}
min_level = 0


def usage():
    print('Options:')
    print('    -c config_file    config file to use to INFO, IGNORE or FAIL warnings')
    print('    -u config_url     URL of config file to use to INFO, IGNORE or FAIL warnings')
    print('    -g gen_file       generate default config file (all rules set to WARN)')
    print('    -m mins           the number of minutes to spider for (default 1)')
    print('    -d                show debug messages')
    print('    -P                specify listen port')
    print('    -n context_file   context file which will be loaded prior to spidering the target')
    print('    -p progress_file  progress file which specifies issues that are being addressed')
    print('    -T                max time in minutes to wait for ZAP to start and the passive scan to run')
    print('    -z zap_options    ZAP command line options e.g. -z "-config aaa=bbb -config ccc=ddd"')

    print('')


def wrong_arg(message=''):
    logging.warning(message)
    usage()
    exit(3)


def setup(config_file=None, generate=False, report_html='', report_xml=None, report_json='',
          progress_file=None, context_file=None, port=0, zap_alpha=False, zap_options=None, mins=0):
    base_dir = None
    print(1.1)
    print('Using port: ' + str(port))

    if config_file:
        # load config file from filestore
        with open(base_dir + config_file) as f:
            try:
                zap_common.load_config(
                    f, config_dict, config_msg, out_of_scope_dict)
            except ValueError as e:
                logging.warning("Failed to load config file " +
                                base_dir + config_file + " " + str(e))
                exit(3)

    if progress_file:
        # load progress file from filestore
        with open(base_dir + progress_file) as f:
            progress = json.load(f)
            # parse into something more useful...
            # in_prog_issues = map of vulnid -> {object with everything in}
            for issue in progress["issues"]:
                if issue["state"] == "inprogress":
                    in_progress_issues[issue["id"]] = issue

    try:
        params = [
            '-config', 'spider.maxDuration=' + str(mins),
            '-addonupdate',
            '-addoninstall', 'pscanrulesBeta']  # In case we're running in the stable container

        if zap_alpha:
            params.append('-addoninstall')
            params.append('pscanrulesAlpha')

        print('Starting Zap at 0.0.0.0:%s'%(port))
        zap_common.add_zap_options(params, zap_options)
        zap_common.start_zap(port, params)
    except OSError:
        logging.warning('Failed to start ZAP :(')
        exit(3)


def main(argv):
    global min_level
    cid = ''
    context_file = ''
    progress_file = ''
    config_file = ''
    config_url = ''
    generate = ''
    mins = 1
    port = 0
    detailed_output = True

    info_unspecified = False
    base_dir = ''
    zap_ip = '0.0.0.0'
    zap_options = ''
    delay = 0
    timeout = 2
    try:
        opts, args = getopt.getopt(argv, "c:u:g:m:hdaijp:sz:P:T")
    except getopt.GetoptError as exc:
        wrong_arg('Invalid option ' + exc.opt + ' : ' + exc.msg)

    for opt, arg in opts:
        print("Parsing args: %s %s" % (opt, arg))
        if opt == '-h':
            usage()
            exit(0)
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
            if port == 0:
                port = 8080
        elif opt == '-n':
            context_file = arg
        elif opt == '-p':
            progress_file = arg
        elif opt == '-z':
            zap_options = arg
    setup(config_file=config_file, generate=generate, progress_file=progress_file, port=port,
          zap_options=zap_options, mins=mins)
    try:
        print("connecting to zap at %s" % 'http://' + zap_ip + ':' + str(port))
        zap = ZAPv2(
            proxies={'http': 'http://' + zap_ip + ':' + str(port), 'https': 'http://' + zap_ip + ':' + str(port)})

        print('Will wait %s for zap to start' % timeout)

        zap_common.wait_for_zap_start(zap, timeout * 60)

        print('Zap started, configuring..')
        if context_file:
            # handle the context file, cant use base_dir as it might not have been set up
            res = zap.context.import_context(
                '/zap/wrk/' + os.path.basename(context_file))
            if res.startswith("ZAP Error"):
                logging.error('Failed to load context file ' +
                              context_file + ' : ' + res)
        f = open("/tmp/zap-ready", "a")
        f.write("zap is ready")
        f.close()
        while True:
            print("heartbeat: %s" % zap.core.version)
            time.sleep(1)
        os.remove("/tmp/zap-ready")
    except IOError as e:
        print("IOError occurred")
        if hasattr(e, 'args') and len(e.args) > 1:
            errno, strerror = e.args
            print("ERROR " + str(strerror))
            logging.warning('I/O error(' + str(errno) + '): ' + str(strerror))
        else:
            print("ERROR %s" % e)
            logging.warning('I/O error: ' + str(e))

    print("Trying to exit")
    exit(0)


if __name__ == "__main__":
    print("Called")
    main(sys.argv[1:])
