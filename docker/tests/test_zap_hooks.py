import unittest
from datetime import datetime
from contextlib import contextmanager
import zap_common


class _MockHooks(object):

    def __init__(self):
        self.called = 0
  

    def zap_started(self, zap, target):
        self.called += 1
        return zap,


class TestZapHooks(unittest.TestCase):

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

