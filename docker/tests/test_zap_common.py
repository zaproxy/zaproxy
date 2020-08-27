import unittest
from datetime import datetime
from unittest.mock import Mock, PropertyMock, patch

import zap_common
import zapv2

class TestZapCommon(unittest.TestCase):

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

        zap.spider.scan.assert_called_once_with(target)
        zap.spider.status.assert_called_with(scan_id)
        self.assertEqual(3, zap.spider.status.call_count)

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

        zap.ajaxSpider.scan.assert_called_once_with(target)
        status.assert_called_with()
        self.assertEqual(3, status.call_count)
        number_of_results.assert_called_with()
        self.assertEqual(2, number_of_results.call_count)

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

        zap.ascan.scan.assert_called_once_with(target, recurse=True, scanpolicyname=scan_policy_name)
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

        zap.ascan.scan.assert_called_once_with(target, recurse=True, scanpolicyname=scan_policy_name)
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

        imported_context_id = zap_common.zap_import_context(zap, context_file)

        zap.context.import_context.assert_called_once_with(context_file)
        self.assertEqual(context_id, imported_context_id)

    def test_zap_import_context_returns_none_if_not_imported(self):
        """Context not imported returns none."""
        context_id = "does_not_exist"
        zap = Mock()
        zap.context.import_context.return_value = context_id
        context_file = "MyContext.context"

        imported_context_id = zap_common.zap_import_context(zap, context_file)

        zap.context.import_context.assert_called_once_with(context_file)
        self.assertIsNone(imported_context_id)

    def test_zap_spider_does_not_use_imported_context(self):
        """Spider does not use imported context."""
        zap = Mock()
        zap.context.import_context.return_value = "1"
        context_file = "MyContext.context"

        scan_id = 1
        zap.spider.scan.return_value = scan_id
        zap.spider.status.side_effect = ["100"]
        target = "http://target.example.com"

        zap_common.zap_import_context(zap, context_file)
        with patch("time.sleep"):
            zap_common.zap_spider(zap, target)

        zap.spider.scan.assert_called_once_with(target)

    def test_zap_ajax_spider_does_not_use_imported_context(self):
        """AJAX Spider does not use imported context."""
        zap = Mock()
        zap.context.import_context.return_value = "1"
        context_file = "MyContext.context"

        zap.ajaxSpider.scan.return_value = "OK"
        type(zap.ajaxSpider).status = PropertyMock(side_effect=Mock(side_effect=["stopped"]))
        target = "http://target.example.com"
        max_time = None

        zap_common.zap_import_context(zap, context_file)
        with patch("time.sleep"):
            zap_common.zap_ajax_spider(zap, target, max_time)

        zap.ajaxSpider.scan.assert_called_once_with(target)

    def test_zap_active_scan_does_not_use_imported_context(self):
        """Active Scan does not use imported context."""
        zap = Mock()
        zap.context.import_context.return_value = "1"
        context_file = "MyContext.context"

        zap.ascan.scan.return_value = 1
        zap.ascan.status.side_effect = ["100"]
        target = "http://target.example.com"
        scan_policy_name = "MyScanPolicy.policy"

        zap_common.zap_import_context(zap, context_file)
        with patch("time.sleep"):
            zap_common.zap_active_scan(zap, target, scan_policy_name)

        zap.ascan.scan.assert_called_once_with(target, recurse=True, scanpolicyname=scan_policy_name)
