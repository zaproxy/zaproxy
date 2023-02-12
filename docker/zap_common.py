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

# This script provides a set of common functions for other scripts

import json
import logging
import os
import re
import shlex
import socket
import subprocess
import sys
import time
import traceback
import errno
import zapv2
from types import ModuleType
from importlib.machinery import SourceFileLoader
from random import randint
from six.moves.urllib.request import urlopen
from six import binary_type

try:
    import pkg_resources
except ImportError:
    # don't hard fail since it's just used for the version check
    logging.warning('Error importing pkg_resources. Is setuptools installed?')


class ScanNotStartedException(Exception):
    pass

class UserInputException(Exception):
    pass

OLD_ZAP_CLIENT_WARNING = '''A newer version of python_owasp_zap_v2.4
 is available. Please run \'pip install -U python_owasp_zap_v2.4\' to update to
 the latest version.'''.replace('\n', '')

zap_conf_lvls = ["PASS", "IGNORE", "INFO", "WARN", "FAIL"]
zap_hooks = None
context_id = None
context_name = None
context_users = None
scan_user = None

def load_custom_hooks(hooks_file=None):
    """ Loads a custom python module which modifies zap scripts behaviour
    hooks_file - a python file which defines custom hooks
    """

    provided_hooks = True if hooks_file or os.environ.get('ZAP_HOOKS') else False

    global zap_hooks
    hooks_file = hooks_file if hooks_file else os.environ.get('ZAP_HOOKS', '~/.zap_hooks.py')
    hooks_file = os.path.expanduser(hooks_file)

    if not os.path.exists(hooks_file):
        # Check to see if its in the wrk directory
        hooks_file2 = os.path.expanduser('wrk/' + hooks_file)
        if os.path.exists(hooks_file2):
            hooks_file = hooks_file2
        
    if not os.path.exists(hooks_file):
        if provided_hooks:
            logging.warning('Could not find custom hooks file at %s ' % os.path.abspath(hooks_file))
        return

    loader = SourceFileLoader("zap_hooks", hooks_file)
    hooks_module = ModuleType(loader.name)
    loader.exec_module(hooks_module)
    zap_hooks = hooks_module


def hook(hook_name=None, **kwargs):
    """
    Decorator method for calling hook before/after method.
    Always adds a hook that runs before intercepting args and if wrap=True will create
    another hook to intercept the return.
    hook_name - name of hook for interactions, if None will use the name of the method it wrapped
    """
    after_hook = kwargs.get('wrap', False)
    def _decorator(func):
        name = func.__name__
        _hook_name = hook_name if hook_name else name
        def _wrap(*args, **kwargs):
            hook_args = list(args)
            hook_kwargs = dict(kwargs)
            args = trigger_hook(_hook_name, *hook_args, **hook_kwargs)
            args_list = list(args)
            return_data = func(*args_list, **kwargs)

            if after_hook:
                return trigger_hook('%s_wrap' % _hook_name, return_data, **hook_kwargs)
            return return_data
        return _wrap
    return _decorator


def trigger_hook(name, *args, **kwargs):
    """ Trigger execution of custom hook method if found """
    global zap_hooks
    arg_length = len(args)
    args_list = list(args)
    args = args[0] if arg_length == 1 else args

    logging.debug('Trigger hook: %s, args: %s' %  (name, arg_length))

    if not zap_hooks:
        return args
    elif not hasattr(zap_hooks, name):
        return args

    hook_fn = getattr(zap_hooks, name)
    if not callable(hook_fn):
        return args

    response = hook_fn(*args_list, **kwargs)

    # The number of args returned should match arguments passed
    if not response:
        return args
    elif arg_length == 1:
      return args
    elif (isinstance(response, list) or isinstance(response, tuple)) and len(response) != arg_length:
        return args
    return response


@hook()
def load_config(config, config_dict, config_msg, out_of_scope_dict):
    """ Loads the config file specified into:
    config_dict - a dictionary which maps plugin_ids to levels (IGNORE, INFO, WARN, FAIL)
    config_msg - a dictionary which maps plugin_ids to optional user specified descriptions
    out_of_scope_dict - a dictionary which maps plugin_ids to out of scope regexes
    """
    for line in config:
        if not line.startswith('#') and len(line) > 1:
            (key, val, optional) = line.rstrip().split('\t', 2)
            if val == 'OUTOFSCOPE':
                for plugin_id in key.split(','):
                    if plugin_id not in out_of_scope_dict:
                        out_of_scope_dict[plugin_id] = []
                    out_of_scope_dict[plugin_id].append(re.compile(optional))
            elif val not in zap_conf_lvls:
                raise ValueError("Level {0} is not a supported level: {1}".format(val, zap_conf_lvls))
            else:
                config_dict[key] = val
                if '\t' in optional:
                    (ignore, usermsg) = optional.rstrip().split('\t')
                    config_msg[key] = usermsg
                else:
                    config_msg[key] = ''
    logging.debug('Loaded config: {0}'.format(config_dict))


def is_in_scope(plugin_id, url, out_of_scope_dict):
    """ Returns True if the url is in scope for the specified plugin_id """
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


def print_rule(zap, action, alert_list, detailed_output, user_msg, in_progress_issues):
    id = alert_list[0].get('pluginId')
    if id in in_progress_issues:
        print (action + '-IN_PROGRESS: ' + alert_list[0].get('alert') + ' [' + id + '] x ' + str(len(alert_list)) + ' ' + user_msg)
        if in_progress_issues[id]["link"]:
            print ('\tProgress link: ' + in_progress_issues[id]["link"])
    else:
        print (action + '-NEW: ' + alert_list[0].get('alert') + ' [' + id + '] x ' + str(len(alert_list)) + ' ' + user_msg)
    if detailed_output:
        # Show (up to) first 5 urls, along with the response code (which we have to perform another request for)
        for alert in alert_list[0:5]:
            msg = zap.core.message(alert.get('messageId'))
            respHeader = msg['responseHeader']
            code = respHeader[respHeader.index(' ') + 1 : respHeader.index('\r\n')]
            print ('\t' + alert.get('url') + ' (' + code + ')')


def print_rules(zap, alert_dict, level, config_dict, config_msg, min_level, inc_rule, inc_extra, detailed_output, in_progress_issues):
    count = 0
    inprog_count = 0
    for key, alert_list in sorted(alert_dict.items()):
        if inc_rule(config_dict, key, inc_extra):
            user_msg = ''
            if key in config_msg:
                user_msg = config_msg[key]
            if min_level <= zap_conf_lvls.index(level):
                print_rule(zap, level, alert_list, detailed_output, user_msg, in_progress_issues)
            if key in in_progress_issues:
                inprog_count += 1
            else:
                count += 1
    return trigger_hook('print_rules_wrap', count, inprog_count)


def inc_ignore_rules(config_dict, key, inc_extra):
    return (key in config_dict) and config_dict[key] == 'IGNORE'


def inc_info_rules(config_dict, key, inc_extra):
    return ((key in config_dict) and config_dict[key] == 'INFO') or ((key not in config_dict) and inc_extra)


def inc_warn_rules(config_dict, key, inc_extra):
    return ((key in config_dict) and config_dict[key] == 'WARN') or ((key not in config_dict) and inc_extra)


def inc_fail_rules(config_dict, key, inc_extra):
    return (key in config_dict) and config_dict[key] == 'FAIL'


def dump_log_file(cid):
    traceback.print_exc()
    # Unexpected issue - dump the zap.log file
    if running_in_docker():
        zap_log = '/zap/zap.out'
        if os.path.isfile(zap_log):
            with open(zap_log, 'r') as zlog:
                for line in zlog:
                    sys.stderr.write(line)
        else:
            logging.debug('Failed to find zap_log ' + zap_log)
    else:
        logging.debug('Dumping docker logs')
        subprocess.call(["docker", "logs", cid], stdout=sys.stderr)


def cp_to_docker(cid, file, dir):
    logging.debug ('Copy ' + file)
    params = ['docker', 'cp', file, cid + ':' + dir + file]
    logging.debug (subprocess.check_output(params))


def running_in_docker():
    return os.path.exists('/.dockerenv') or os.path.exists('/run/.containerenv') or os.environ.get("IS_CONTAINERIZED") == "true"


def add_zap_options(params, zap_options):
    if zap_options:
        for zap_opt in shlex.split(zap_options):
            params.append(zap_opt)


def create_start_options(mode, port, extra_params):
    params = [
        '/zap/zap-x.sh', mode,
        '-port', str(port),
        '-host', '0.0.0.0',
        '-config', 'database.recoverylog=false',
        '-config', 'api.disablekey=true',
        '-config', 'api.addrs.addr.name=.*',
        '-config', 'api.addrs.addr.regex=true']
    params.extend(extra_params)
    logging.debug('Params: ' + str(params))
    return params

@hook()
def start_zap(port, extra_zap_params):
    logging.debug('Starting ZAP')
    with open('zap.out', "w") as outfile:
        subprocess.Popen(
            create_start_options('-daemon', port, extra_zap_params), 
            stdout=outfile, stderr=subprocess.DEVNULL)


def run_zap_inline(port, extra_zap_params):
    logging.debug('Starting ZAP')
    process = subprocess.run(
        create_start_options('-cmd', port, extra_zap_params),
        universal_newlines = True, stdout = subprocess.PIPE, stderr=subprocess.DEVNULL)
    return process.stdout


def wait_for_zap_start(zap, timeout_in_secs = 600):
    version = None
    if not timeout_in_secs:
        # if ZAP doesn't start in 10 mins then its probably not going to start
        timeout_in_secs = 600

    for x in range(0, timeout_in_secs):
        try:
            version = zap.core.version
            logging.debug('ZAP Version ' + version)
            logging.debug('Took ' + str(x) + ' seconds')
            break
        except IOError:
            time.sleep(1)

    if not version:
        raise IOError(
          errno.EIO,
          'Failed to connect to ZAP after {0} seconds'.format(timeout_in_secs))


@hook(wrap=True)
def start_docker_zap(docker_image, port, extra_zap_params, mount_dir):
    try:
        logging.debug('Pulling ZAP Docker image: ' + docker_image)
        ls_output = subprocess.check_output(['docker', 'pull', docker_image])
    except OSError as err:
        logging.warning('Failed to run docker - is it on your path?')
        raise err

    logging.debug('Starting ZAP')
    params = ['docker', 'run']

    if mount_dir:
        params.extend(['-v', mount_dir + ':/zap/wrk/:rw'])

    params.extend([
            '-u', 'zap',
            '-p', str(port) + ':' + str(port),
            '-d', docker_image,
            'zap-x.sh', '-daemon',
            '-port', str(port),
            '-host', '0.0.0.0',
            '-config', 'database.recoverylog=false',
            '-config', 'api.disablekey=true',
            '-config', 'api.addrs.addr.name=.*',
            '-config', 'api.addrs.addr.regex=true'])

    params.extend(extra_zap_params)

    logging.debug('Params: ' + str(params))

    cid = subprocess.check_output(params).rstrip().decode('utf-8')
    logging.debug('Docker CID: ' + cid)
    return cid


def get_free_port():
    while True:
        port = randint(32768, 61000)
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        if not (sock.connect_ex(('127.0.0.1', port)) == 0):
            return port


def ipaddress_for_cid(cid):
    insp_output = subprocess.check_output(['docker', 'inspect', cid]).strip().decode('utf-8')
    #logging.debug('Docker Inspect: ' + insp_output)
    insp_json = json.loads(insp_output)
    return str(insp_json[0]['NetworkSettings']['IPAddress'])


def stop_docker(cid):
    # Close container - ignore failures
    try:
        logging.debug('Stopping Docker container')
        subprocess.check_output(['docker', 'stop', cid])
        logging.debug('Docker container stopped')
    except OSError:
        logging.warning('Docker stop failed')

    # Remove container - ignore failures
    try:
        logging.debug('Removing Docker container')
        subprocess.check_output(['docker', 'rm', cid])
        logging.debug('Docker container removed')
    except OSError:
        logging.warning('Docker rm failed')


@hook()
def zap_access_target(zap, target):
    res = zap.urlopen(target)
    if res.startswith("ZAP Error"):
        raise IOError(errno.EIO, 'ZAP failed to access: {0}'.format(target))


def zap_tune(zap):
    logging.debug('Tune')
    logging.debug('Disable all tags')
    zap.pscan.disable_all_tags()
    logging.debug('Set max pscan alerts')
    zap.pscan.set_max_alerts_per_rule(10)


def raise_scan_not_started():
    raise ScanNotStartedException('Failed to start the scan, check the log/output for more details.')


@hook(wrap=True)
def zap_spider(zap, target):
    if scan_user:
        logging.debug('Spider %s as user %s', target, scan_user['name'])
        spider_scan_id = zap.spider.scan_as_user(context_id, scan_user['id'])
    else:
        logging.debug('Spider %s', target)
        spider_scan_id = zap.spider.scan(target, contextname=context_name)

    if not str(spider_scan_id).isdigit():
        raise_scan_not_started()
    time.sleep(5)

    while (int(zap.spider.status(spider_scan_id)) < 100):
        logging.debug('Spider progress %: ' + zap.spider.status(spider_scan_id))
        time.sleep(5)
    logging.debug('Spider complete')


@hook(wrap=True)
def zap_ajax_spider(zap, target, max_time):
    if max_time:
        zap.ajaxSpider.set_option_max_duration(str(max_time))
    if scan_user:
        logging.debug('AjaxSpider %s as user %s', target, scan_user['name'])
        result = zap.ajaxSpider.scan_as_user(context_name, scan_user['name'], target)
    else:
        logging.debug('AjaxSpider %s', target)
        result = zap.ajaxSpider.scan(target, contextname=context_name)
    if result != "OK":
        raise_scan_not_started()
    time.sleep(5)

    while (zap.ajaxSpider.status == 'running'):
        logging.debug('Ajax Spider running, found urls: %s', zap.ajaxSpider.number_of_results)
        time.sleep(5)
    logging.debug('Ajax Spider complete')


@hook(wrap=True)
def zap_active_scan(zap, target, policy):
    if scan_user:
        logging.debug('Active Scan %s with policy %s as user %s', target, policy, scan_user['name'])
        ascan_scan_id = zap.ascan.scan_as_user(target, recurse=True, scanpolicyname=policy, contextid=context_id, userid=scan_user['id'])
    else:
        logging.debug('Active Scan %s with policy %s', target, policy)
        ascan_scan_id = zap.ascan.scan(target, recurse=True, scanpolicyname=policy, contextid=context_id)
    if not str(ascan_scan_id).isdigit():
        raise_scan_not_started()
    time.sleep(5)

    while(int(zap.ascan.status(ascan_scan_id)) < 100):
        logging.debug('Active Scan progress %: ' + zap.ascan.status(ascan_scan_id))
        time.sleep(5)
    logging.debug('Active Scan complete')
    logging.debug(zap.ascan.scan_progress(ascan_scan_id))


def zap_wait_for_passive_scan(zap, timeout_in_secs = 0):
    rtc = zap.pscan.records_to_scan
    logging.debug('Records to scan...')
    time_taken = 0
    timed_out = False
    while (int(zap.pscan.records_to_scan) > 0):
        logging.debug('Records to passive scan : ' + zap.pscan.records_to_scan)
        time.sleep(2)
        time_taken += 2
        if timeout_in_secs and time_taken > timeout_in_secs:
            timed_out = True
            break
    if timed_out:
      logging.debug('Exceeded passive scan timeout')
    else:
      logging.debug('Passive scanning complete')


@hook(wrap=True)
def zap_get_alerts(zap, baseurl, ignore_scan_rules, out_of_scope_dict):
    # Retrieve the alerts using paging in case there are lots of them
    st = 0
    pg = 5000
    alert_dict = {}
    alert_count = 0
    alerts = zap.core.alerts(baseurl=baseurl, start=st, count=pg)
    while len(alerts) > 0:
        logging.debug('Reading ' + str(pg) + ' alerts from ' + str(st))
        alert_count += len(alerts)
        for alert in alerts:
            plugin_id = alert.get('pluginId')
            if plugin_id in ignore_scan_rules:
                continue
            if not is_in_scope(plugin_id, alert.get('url'), out_of_scope_dict):
                continue
            if alert.get('risk') == 'Informational':
                # Ignore all info alerts - some of them may have been downgraded by security annotations
                continue
            if (plugin_id not in alert_dict):
                alert_dict[plugin_id] = []
            alert_dict[plugin_id].append(alert)
        st += pg
        alerts = zap.core.alerts(start=st, count=pg)
    logging.debug('Total number of alerts: ' + str(alert_count))
    return alert_dict


def get_latest_zap_client_version():
    version_info = None

    try:
        version_info = urlopen('https://pypi.python.org/pypi/python-owasp-zap-v2.4/json', timeout=10)
    except Exception as e:
        logging.warning('Error fetching latest ZAP Python API client version: %s' % e)
        return None

    version_json = json.loads(version_info.read().decode('utf-8'))

    if 'info' not in version_json:
        logging.warning('No version found for latest ZAP Python API client.')
        return None
    if 'version' not in version_json['info']:
        logging.warning('No version found for latest ZAP Python API client.')
        return None

    return version_json['info']['version']


def check_zap_client_version():
    # No need to check ZAP Python API client while running in Docker
    if running_in_docker():
        return

    if 'pkg_resources' not in globals():  # import failed
        logging.warning('Could not check ZAP Python API client without pkg_resources.')
        return

    current_version = getattr(zapv2, '__version__', None)
    latest_version = get_latest_zap_client_version()
    parse_version = pkg_resources.parse_version
    if current_version and latest_version and \
       parse_version(current_version) < parse_version(latest_version):
        logging.warning(OLD_ZAP_CLIENT_WARNING)
    elif current_version is None:
        # the latest versions >= 0.0.9 have a __version__
        logging.warning(OLD_ZAP_CLIENT_WARNING)
    # else:
    # we're up to date or ahead / running a development build
    # or latest_version is None and the user already got a warning


def write_report(file_path, report):
    with open(file_path, mode='wb') as f:
        if not isinstance(report, binary_type):
            report = report.encode('utf-8')

        f.write(report)

@hook(wrap=True)
def zap_import_context(zap, context_file):
    global context_id
    global context_name
    global context_users
    res = context_id = zap.context.import_context(context_file)
    try:
        int(res)
        context_name = zap.context.context_list[-1]
        context_users = zap.users.users_list(context_id)
        
    except ValueError:
        context_id = None
        logging.error('Failed to load context file ' + context_file + ' : ' + res)
    return context_id

@hook(wrap=True)
def zap_set_scan_user(zap, username):
    global scan_user
    for usr in context_users:
        if usr['name'] == username:
            logging.debug('Found user ' + username)
            scan_user = usr
            return
    raise UserInputException('ZAP failed to find user: {0}'.format(username))

def get_af_env(targets, out_of_scope_dict, debug):
    exclude = []
    # '*' rules apply to all scan rules so can just be added to the context exclusions
    if '*' in out_of_scope_dict:
        for rule in out_of_scope_dict['*']:
            exclude.append(rule.pattern)
    
    return {
            'env': {
                'contexts': [{
                    'name': 'baseline',
                    'urls': targets,
                    'excludePaths': exclude
                    }],
                'parameters': {
                    'failOnError': True,
                    'progressToStdout': debug}
                }
        }

def get_af_pscan_config(max_alerts=10):
    return {
        'type': 'passiveScan-config',
        'parameters': {
            'enableTags': False,
            'maxAlertsPerRule': max_alerts}
        }

def get_af_pscan_wait(mins):
    return {
        'type': 'passiveScan-wait',
        'parameters': {
            'maxDuration': mins}
        }

def get_af_spider(target, mins):
    return {
        'type': 'spider',
        'parameters': {
            'url': target,
            'maxDuration': mins}
        }

def get_af_spiderAjax(target, mins):
    return {
        'type': 'spiderAjax',
        'parameters': {
            'url': target,
            'maxDuration': mins}
        }

def get_af_report(template, dir, file, title, description):
    return {
        'type': 'report',
        'parameters': {
            'template': template,
            'reportDir': dir,
            'reportFile': file,
            'reportTitle': title,
            'reportDescription': description}
        }

def get_af_output_summary(format, summaryFile, config_dict, config_msg):
    obj = {
        'type': 'outputSummary',
        'parameters': {
            'format': format,
            'summaryFile': summaryFile}
        }
    rules = []
    for id, action in config_dict.items():
        if id in config_msg:
            rules.append({'id': int(id), 'action': action, 'customMessage': config_msg[id]})
        else:
            rules.append({'id': int(id), 'action': action})
    obj['rules'] = rules
    return obj

def get_af_alertFilter(alertFilters):
    return {
        'type': 'alertFilter',
        'alertFilters': alertFilters
    }
