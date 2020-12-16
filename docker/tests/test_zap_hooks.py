import contextlib
import os
import tempfile
import unittest
from datetime import datetime
from contextlib import contextmanager
from unittest.mock import Mock, PropertyMock
from unittest.mock import patch

import zap_common


@contextlib.contextmanager
def custom_hooks_file(content=None):
    hooks = content if content else "def custom_hook():\n    pass"
    with tempfile.NamedTemporaryFile() as file:
        file.write(hooks.encode())
        file.flush()
        yield file


@contextlib.contextmanager
def custom_hooks_file_malformed():
    content = """def custom_hook() # missing :\n    pass"""
    with custom_hooks_file(content) as file:
        yield file


class _MockHooks(object):

    def __init__(self):
        self.called = 0
  

    def zap_started(self, zap, target):
        self.called += 1
        return zap,


class TestZapHooks(unittest.TestCase):

    def setUp(self):
        zap_common.zap_hooks = None
        zap_common.context_id = None
        zap_common.context_name = None
        zap_common.scan_user = None

    def tearDown(self):
        zap_common.zap_hooks = None
        zap_common.context_id = None
        zap_common.context_name = None
        zap_common.scan_user = None

    def test_trigger_hook_mismatch_exception(self):
        """ If the hook signature doesn't match the hook the exception bubbles up """
        zap_common.zap_hooks = _MockHooks()
        with self.assertRaises(Exception):
          zap_common.trigger_hook('zap_started')
        self.assertEqual(zap_common.zap_hooks.called, 0)


    def test_trigger_hook_verify_calls(self):
        """ Verify the hook gets called if it matches signature """
        zap_common.zap_hooks = _MockHooks()
        args = ['zap', 'http://127.0.0.1']
        zap_common.trigger_hook('zap_started', *args)
        zap_common.trigger_hook('zap_started', *args)
        zap_common.trigger_hook('zap_started', *args)
        zap_common.trigger_hook('zap_started', *args)
        zap_common.trigger_hook('zap_started', *args)
        self.assertEqual(zap_common.zap_hooks.called, 5)


    def test_trigger_hook_maintain_signature(self):
        """ Should return original args if there is a mismatch on the return signature """
        zap_common.zap_hooks = _MockHooks()
        args = ['zap', 'http://127.0.0.1']
        # The defined hook method only returns 1 item
        return_direct = zap_common.zap_hooks.zap_started(*args)
        self.assertTrue(len(return_direct) == 1)
        self.assertNotEqual(len(return_direct), len(args))

        # However, when called in hook, if there is a different
        # return signature, ignore the hook return
        return_args = zap_common.trigger_hook('zap_started', *args)
        self.assertEqual(len(args), len(return_args))
        self.assertEqual(args, list(return_args))

    def test_load_custom_hooks_from_file_not_exists(self):
        """Hooks are not loaded when the file does not exist."""
        zap_common.load_custom_hooks(hooks_file="/some-dir/not-a-hooks-file")
        self.assertIsNone(zap_common.zap_hooks)

    def test_load_custom_hooks_from_file_exists(self):
        """Hooks are loaded when the file exists."""
        with custom_hooks_file() as file:
            zap_common.load_custom_hooks(hooks_file=file.name)
        self.assert_custom_hooks_loaded()

    def assert_custom_hooks_loaded(self):
        self.assertIsNotNone(zap_common.zap_hooks)
        self.assertTrue(callable(getattr(zap_common.zap_hooks, "custom_hook")))

    def test_load_custom_hooks_from_file_with_errors(self):
        """Hooks are not loaded and exception is raised when the file has errors."""
        with custom_hooks_file_malformed() as file, self.assertRaises(SyntaxError):
            zap_common.load_custom_hooks(hooks_file=file.name)
        self.assertIsNone(zap_common.zap_hooks)

    def test_load_custom_hooks_from_env_var_file_not_exists(self):
        """Hooks are not loaded from env var defined file when not exists."""
        os.environ['ZAP_HOOKS'] = "/some-dir/not-a-hooks-file"
        zap_common.load_custom_hooks()
        self.assertIsNone(zap_common.zap_hooks)

    def test_load_custom_hooks_from_env_var_file_exists(self):
        """Hooks are loaded from env var defined file when exists."""
        with custom_hooks_file() as file:
            os.environ['ZAP_HOOKS'] = file.name
            zap_common.load_custom_hooks()
        self.assert_custom_hooks_loaded()

    def test_load_custom_hooks_from_env_var_file_with_errors(self):
        """Hooks are not loaded and exception is raised when the env var defined file has errors."""
        with custom_hooks_file_malformed() as file, self.assertRaises(SyntaxError):
            os.environ['ZAP_HOOKS'] = file.name
            zap_common.load_custom_hooks()
        self.assertIsNone(zap_common.zap_hooks)

    def test_load_config_triggers_hook(self):
        """Hook is triggered when load_config is called."""
        hooks = Mock(load_config=Mock(return_value=[]))
        zap_common.zap_hooks = hooks

        config = ["#config"]
        config_dict = "config_dict"
        config_msg = "config_msg"
        out_of_scope_dict = "out_of_scope_dict"

        zap_common.load_config(config, config_dict, config_msg, out_of_scope_dict)

        hooks.load_config.assert_called_once_with(config, config_dict, config_msg, out_of_scope_dict)

    def test_print_rules_triggers_hook(self):
        """Hook is triggered when print_rules is called."""
        hooks = Mock(print_rules=Mock(return_value=[]))
        zap_common.zap_hooks = hooks

        zap = "zap"
        alert_dict = {}
        level = "level"
        config_dict = "config_dict"
        config_msg = "config_msg"
        min_level = "min_level"
        inc_rule = "inc_rule"
        inc_extra = "inc_extra"
        detailed_output = "detailed_output"
        in_progress_issues = "in_progress_issues"

        count = 0
        inprog_count = 0

        zap_common.print_rules(zap, alert_dict, level, config_dict, config_msg, min_level, inc_rule, inc_extra,
                               detailed_output, in_progress_issues)

        hooks.print_rules_wrap.assert_called_once_with(count, inprog_count)

    def test_start_zap_triggers_hook(self):
        """Hook is triggered when start_zap is called."""
        hooks = Mock(start_zap=Mock(return_value=[]))
        zap_common.zap_hooks = hooks

        port = 8080
        extra_zap_params = ["-config", "key=value"]

        with patch("builtins.open"), patch('subprocess.Popen'):
            zap_common.start_zap(port, extra_zap_params)

        hooks.start_zap.assert_called_once_with(port, extra_zap_params)

    def test_start_docker_zap_triggers_hook(self):
        """Hooks are triggered when start_docker_zap is called."""
        hooks = Mock(start_docker_zap=Mock(return_value=[]))
        zap_common.zap_hooks = hooks

        docker_image = "docker_image"
        port = 1234
        extra_zap_params = ["-config", "key=value"]
        mount_dir = "/some/dir"

        cid = "123"

        with patch('subprocess.check_output', new=Mock(return_value=cid.encode())):
            zap_common.start_docker_zap(docker_image, port, extra_zap_params, mount_dir)

        hooks.start_docker_zap.assert_called_once_with(docker_image, port, extra_zap_params, mount_dir)
        hooks.start_docker_zap_wrap.assert_called_once_with(cid)

    def test_zap_access_target_triggers_hook(self):
        """Hook is triggered when zap_access_target is called."""
        hooks = Mock(zap_access_target=Mock(return_value=[]))
        zap_common.zap_hooks = hooks

        zap = Mock(urlopen=Mock(return_value=""))
        target = "http://target.example.com"

        zap_common.zap_access_target(zap, target)

        hooks.zap_access_target.assert_called_once_with(zap, target)

    def test_zap_spider_triggers_hooks(self):
        """Hooks are triggered when zap_spider is called."""
        hooks = Mock(zap_spider=Mock(return_value=[]))
        zap_common.zap_hooks = hooks

        zap = Mock()
        zap.spider.scan.return_value = "1"
        zap.spider.status.side_effect = ["100"]
        target = "http://target.example.com"

        with patch("time.sleep"):
            zap_common.zap_spider(zap, target)

        hooks.zap_spider.assert_called_once_with(zap, target)
        hooks.zap_spider_wrap.assert_called_once_with(None)

    def test_zap_ajax_spider_triggers_hooks(self):
        """Hooks are triggered when zap_ajax_spider is called."""
        hooks = Mock(zap_ajax_spider=Mock(return_value=[]))
        zap_common.zap_hooks = hooks

        zap = Mock()
        zap.ajaxSpider.scan.return_value = "OK"
        target = "http://target.example.com"
        max_time = 10

        with patch("time.sleep"):
            zap_common.zap_ajax_spider(zap, target, max_time)

        hooks.zap_ajax_spider.assert_called_once_with(zap, target, max_time)
        hooks.zap_ajax_spider_wrap.assert_called_once_with(None)

    def test_zap_active_scan_triggers_hooks(self):
        """Hooks are triggered when zap_active_scan is called."""
        hooks = Mock(zap_active_scan=Mock(return_value=[]))
        zap_common.zap_hooks = hooks

        zap = Mock(ascan=Mock(scan=Mock(return_value=1), status=Mock(return_value="100")))
        target = "http://target.example.com"
        policy = "ScanPolicy"

        with patch("time.sleep"):
            zap_common.zap_active_scan(zap, target, policy)

        hooks.zap_active_scan.assert_called_once_with(zap, target, policy)
        hooks.zap_active_scan_wrap.assert_called_once_with(None)

    def test_zap_get_alerts_triggers_hooks(self):
        """Hooks are triggered when zap_get_alerts is called."""
        hooks = Mock(zap_get_alerts=Mock(return_value=[]))
        zap_common.zap_hooks = hooks

        zap = Mock(core=Mock(alerts=Mock(return_value=[])))
        baseurl = "http://target.example.com"
        ignore_scan_rules = "ignore_scan_rules"
        out_of_scope_dict = "out_of_scope_dict"

        alert_dict = {}

        zap_common.zap_get_alerts(zap, baseurl, ignore_scan_rules, out_of_scope_dict)

        hooks.zap_get_alerts.assert_called_once_with(zap, baseurl, ignore_scan_rules, out_of_scope_dict)
        hooks.zap_get_alerts_wrap.assert_called_once_with(alert_dict)

    def test_zap_import_context_triggers_hooks(self):
        """Hooks are triggered when zap_import_context is called."""
        hooks = Mock(zap_import_context=Mock(return_value=[]))
        zap_common.zap_hooks = hooks

        context_id = "123"

        zap = Mock()
        zap.context.import_context.return_value = context_id
        type(zap.context).context_list = PropertyMock(return_value=["Default Context", "My Context"])
        context_file = "/path/to/context"

        zap_common.zap_import_context(zap, context_file)

        hooks.zap_import_context.assert_called_once_with(zap, context_file)
        hooks.zap_import_context_wrap.assert_called_once_with(context_id)

    def test_zap_set_scan_user_triggers_hooks(self):
        """Hooks are triggered when zap_set_scan_user is called."""
        hooks = Mock(zap_set_scan_user=Mock(return_value=[]))
        zap_common.zap_hooks = hooks

        user = "user1"
        zap_common.context_users = [{'name': user, 'id': '1'}]

        zap = Mock()

        zap_common.zap_set_scan_user(zap, user)

        hooks.zap_set_scan_user.assert_called_once_with(zap, user)
        hooks.zap_set_scan_user_wrap.assert_called_once_with(None)

        zap_common.context_users = None
