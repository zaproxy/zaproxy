import unittest
from datetime import datetime
from unittest.mock import Mock, PropertyMock, patch

import zap_common
import zapv2

class TestZapCommon(unittest.TestCase):

    def setUp(self):
        zap_common.context_id = None
        zap_common.context_name = None
        zap_common.context_users = None
        zap_common.scan_user = None

    def tearDown(self):
        zap_common.context_id = None
        zap_common.context_name = None
        zap_common.context_users = None
        zap_common.scan_user = None

    def test_load_config(self):
        pass


    def test_is_in_scope(self):
        pass


    def zap_get_alerts(self):
        pass


    def test_zap_spider(self):
        """Spider is started and waits until finished."""
        scan_id = 1
        zap = Mock()
        zap.spider.scan.return_value = scan_id
        zap.spider.status.side_effect = ["0", "50", "100"]
        target = "http://target.example.com"

        with patch("time.sleep"):
            zap_common.zap_spider(zap, target)

        zap.spider.scan.assert_called_once_with(target, contextname=None)
        zap.spider.status.assert_called_with(scan_id)
        self.assertEqual(3, zap.spider.status.call_count)

    def test_zap_spider_raises_exception_if_not_started(self):
        """Spider raises exception if not started."""
        zap = Mock()
        zap.spider.scan.return_value = "url_not_in_context"
        target = "http://target.example.com"

        with self.assertRaises(zap_common.ScanNotStartedException):
            zap_common.zap_spider(zap, target)

        zap.spider.scan.assert_called_once_with(target, contextname=None)
        zap.spider.status.assert_not_called()

    def test_zap_ajax_spider(self):
        """AJAX Spider is started and waits until finished."""
        zap = Mock()
        zap.ajaxSpider.scan.return_value = "OK"
        status = PropertyMock(side_effect=Mock(side_effect=["running", "running", "stopped"]))
        type(zap.ajaxSpider).status = status
        number_of_results = PropertyMock(return_value=10)
        type(zap.ajaxSpider).number_of_results = number_of_results
        target = "http://target.example.com"
        max_time = None

        with patch("time.sleep"):
            zap_common.zap_ajax_spider(zap, target, max_time)

        zap.ajaxSpider.scan.assert_called_once_with(target, contextname=None)
        status.assert_called_with()
        self.assertEqual(3, status.call_count)
        number_of_results.assert_called_with()
        self.assertEqual(2, number_of_results.call_count)

    def test_zap_ajax_spider_raises_exception_if_not_started(self):
        """AJAX Spider raises exception if not started."""
        zap = Mock()
        zap.ajaxSpider.scan.return_value = "url_not_in_context"
        status = PropertyMock()
        type(zap.ajaxSpider).status = status
        target = "http://target.example.com"
        max_time = None

        with self.assertRaises(zap_common.ScanNotStartedException):
            zap_common.zap_ajax_spider(zap, target, max_time)

        zap.ajaxSpider.scan.assert_called_once_with(target, contextname=None)
        status.assert_not_called()

    def test_zap_ajax_spider_with_max_time(self):
        """AJAX Spider is started with specified maximum time."""
        zap = Mock()
        zap.ajaxSpider.scan.return_value = "OK"
        zap.ajaxSpider.status = PropertyMock(side_effect=Mock(side_effect=["stopped"]))
        target = "http://target.example.com"
        max_time = 10

        with patch("time.sleep"):
            zap_common.zap_ajax_spider(zap, target, max_time)

        zap.ajaxSpider.set_option_max_duration.assert_called_once_with(str(max_time))

    def test_zap_active_scan(self):
        """Active Scan is started and waits until finished."""
        scan_id = 1
        zap = Mock()
        zap.ascan.scan.return_value = scan_id
        zap.ascan.status.side_effect = ["0", "50", "100"]
        target = "http://target.example.com"
        scan_policy_name = "MyScanPolicy.policy"

        with patch("time.sleep"):
            zap_common.zap_active_scan(zap, target, scan_policy_name)

        zap.ascan.scan.assert_called_once_with(target, recurse=True, scanpolicyname=scan_policy_name, contextid=None)
        zap.ascan.status.assert_called_with(scan_id)
        self.assertEqual(3, zap.ascan.status.call_count)

    def test_zap_active_scan_raises_exception_if_not_started(self):
        """Active Scan raises exception if not started."""
        zap = Mock()
        zap.ascan.scan.return_value = "url_not_found"
        target = "http://target.example.com"
        scan_policy_name = "MyScanPolicy.policy"

        with self.assertRaises(zap_common.ScanNotStartedException):
            zap_common.zap_active_scan(zap, target, scan_policy_name)

        zap.ascan.scan.assert_called_once_with(target, recurse=True, scanpolicyname=scan_policy_name, contextid=None)
        zap.ascan.status.assert_not_called()

    def test_zap_wait_for_passive_scan(self):
        """Waits for the passive scan to finish."""
        zap = Mock()
        records_to_scan = PropertyMock(side_effect=Mock(side_effect=["15", "10", "5", "0"]))
        type(zap.pscan).records_to_scan = records_to_scan
        timeout_in_secs = None

        with patch("time.sleep"):
            zap_common.zap_wait_for_passive_scan(zap, timeout_in_secs)

        records_to_scan.assert_called_with()
        self.assertEqual(4, records_to_scan.call_count)

    def test_zap_wait_for_passive_scan_until_timeout(self):
        """Waits for the passive scan to finish until timeout."""
        zap = Mock()
        records_to_scan = PropertyMock(return_value="10")
        type(zap.pscan).records_to_scan = records_to_scan
        timeout_in_secs = 10

        with patch("time.sleep"):
            zap_common.zap_wait_for_passive_scan(zap, timeout_in_secs)

        records_to_scan.assert_called_with()
        self.assertGreater(records_to_scan.call_count, 5)

    def test_zap_import_context(self):
        """Context is imported."""
        context_id = "1"
        zap = Mock()
        zap.context.import_context.return_value = context_id
        context_file = "MyContext.context"
        context_name = "My Context"
        type(zap.context).context_list = PropertyMock(return_value=["Default Context", context_name])

        imported_context_id = zap_common.zap_import_context(zap, context_file)

        zap.context.import_context.assert_called_once_with(context_file)
        self.assertEqual(context_id, imported_context_id)
        self.assertEqual(context_id, zap_common.context_id)
        self.assertEqual(context_name, zap_common.context_name)

    def test_zap_import_context_returns_none_if_not_imported(self):
        """Context not imported returns none."""
        context_id = "does_not_exist"
        zap = Mock()
        zap.context.import_context.return_value = context_id
        context_file = "MyContext.context"

        imported_context_id = zap_common.zap_import_context(zap, context_file)

        zap.context.import_context.assert_called_once_with(context_file)
        self.assertIsNone(imported_context_id)
        self.assertIsNone(zap_common.context_id)
        self.assertIsNone(zap_common.context_name)

    def test_zap_import_context_sets_users(self):
        """Context is imported."""
        context_id = "1"
        zap = Mock()
        zap.context.import_context.return_value = context_id

        context_file = "MyContext.context"
        context_name = "My Context"
        context_users = [{'name': 'user1', 'id': '1'}]
        type(zap.context).context_list = PropertyMock(return_value=["Default Context", context_name])
        zap.users.users_list.return_value = context_users

        imported_context_id = zap_common.zap_import_context(zap, context_file)

        zap.context.import_context.assert_called_once_with(context_file)
        self.assertEqual(context_id, imported_context_id)
        self.assertEqual(context_id, zap_common.context_id)
        self.assertEqual(context_name, zap_common.context_name)
        self.assertEqual(context_users, zap_common.context_users)

    def test_zap_spider_uses_imported_context(self):
        """Spider uses imported context."""
        context_name = "My Context"
        zap_common.context_name = context_name

        zap = Mock()
        scan_id = 1
        zap.spider.scan.return_value = scan_id
        zap.spider.status.side_effect = ["100"]
        target = "http://target.example.com"

        with patch("time.sleep"):
            zap_common.zap_spider(zap, target)

        zap.spider.scan.assert_called_once_with(target, contextname=context_name)

    def test_zap_spider_uses_user(self):
        """Spider uses specified user."""
        context_id = 11
        zap_common.context_id = context_id

        user = "user1"
        user_id = "12"
        zap_common.scan_user = {'name': user, 'id': user_id}

        zap = Mock()
        scan_id = 1
        zap.spider.scan_as_user.return_value = scan_id
        zap.spider.status.side_effect = ["100"]
        target = "http://target.example.com"

        with patch("time.sleep"):
            zap_common.zap_spider(zap, target)

        zap.spider.scan_as_user.assert_called_once_with(context_id, user_id)

    def test_zap_ajax_spider_uses_imported_context(self):
        """AJAX Spider uses imported context."""
        context_name = "My Context"
        zap_common.context_name = context_name

        zap = Mock()
        zap.ajaxSpider.scan.return_value = "OK"
        type(zap.ajaxSpider).status = PropertyMock(side_effect=Mock(side_effect=["stopped"]))
        target = "http://target.example.com"
        max_time = None

        with patch("time.sleep"):
            zap_common.zap_ajax_spider(zap, target, max_time)

        zap.ajaxSpider.scan.assert_called_once_with(target, contextname=context_name)

    def test_zap_ajax_spider_uses_user(self):
        """AJAX Spider uses specified user."""
        context_name = "My Context"
        zap_common.context_name = context_name

        user_name = "user1"
        user_id = "12"
        zap_common.scan_user = {'name': user_name, 'id': user_id}

        zap = Mock()
        zap.ajaxSpider.scan_as_user.return_value = "OK"
        type(zap.ajaxSpider).status = PropertyMock(side_effect=Mock(side_effect=["stopped"]))
        target = "http://target.example.com"
        max_time = None

        with patch("time.sleep"):
            zap_common.zap_ajax_spider(zap, target, max_time)

        zap.ajaxSpider.scan_as_user.assert_called_once_with(context_name, user_name, target)

    def test_zap_active_scan_uses_imported_context(self):
        """Active Scan uses imported context."""
        context_id = "1"
        zap_common.context_id = context_id

        zap = Mock()
        zap.ascan.scan.return_value = 1
        zap.ascan.status.side_effect = ["100"]
        target = "http://target.example.com"
        scan_policy_name = "MyScanPolicy.policy"

        with patch("time.sleep"):
            zap_common.zap_active_scan(zap, target, scan_policy_name)

        zap.ascan.scan.assert_called_once_with(target, recurse=True, scanpolicyname=scan_policy_name,
                                               contextid=context_id)

    def test_zap_active_scan_uses_user(self):
        """Active Scan uses specified user."""
        context_id = "1"
        zap_common.context_id = context_id

        user_id = "12"
        zap_common.scan_user = {'name': 'user1', 'id': user_id}

        zap = Mock()
        zap.ascan.scan_as_user.return_value = 1
        zap.ascan.status.side_effect = ["100"]
        target = "http://target.example.com"
        scan_policy_name = "MyScanPolicy.policy"

        with patch("time.sleep"):
            zap_common.zap_active_scan(zap, target, scan_policy_name)

        zap.ascan.scan_as_user.assert_called_once_with(target, recurse=True, scanpolicyname=scan_policy_name,
                                               contextid=context_id, userid=user_id)

    def test_zap_tune(self):
        """Tune makes expected API calls."""
        zap = Mock()
        
        zap.pscan.disable_all_tags.return_value = "OK"
        zap.pscan.set_max_alerts_per_rule.return_value = "OK"

        zap_common.zap_tune(zap)

        zap.pscan.disable_all_tags.assert_called_once_with()
        zap.pscan.set_max_alerts_per_rule.assert_called_once_with(10)

    def test_zap_set_scan_user_with_one_user(self):
        """Scan_user is set."""
        context_id = "1"
        zap = Mock()

        user_name = 'user1'
        user = {'name': user_name, 'id': '1'}
        zap_common.context_users = [user]

        zap_common.zap_set_scan_user(zap, user_name)

        self.assertEqual(user, zap_common.scan_user)

    def test_zap_set_scan_user_with_multiple_users(self):
        """Scan_user is set."""
        context_id = "1"
        zap = Mock()

        user_name = 'user2'
        user = {'name': user_name, 'id': '1'}
        zap_common.context_users = [{'name': 'user1', 'id': '1'}, user]

        zap_common.zap_set_scan_user(zap, user_name)

        self.assertEqual(user, zap_common.scan_user)

    def test_zap_set_scan_user_with_no_users(self):
        """Exception is raised."""
        context_id = "1"
        zap = Mock()
        zap_common.context_users = []

        with self.assertRaises(zap_common.UserInputException):
            zap_common.zap_set_scan_user(zap, 'user1')

        self.assertEqual(None, zap_common.scan_user)

    def test_zap_set_scan_user_with_bad_user(self):
        """Exception is raised."""
        context_id = "1"
        zap = Mock()

        user_name = 'user1'
        user = {'name': 'user2', 'id': '1'}
        zap_common.context_users = [user]

        with self.assertRaises(zap_common.UserInputException):
            zap_common.zap_set_scan_user(zap, user_name)

        self.assertEqual(None, zap_common.scan_user)

