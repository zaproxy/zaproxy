import os
import sys
import unittest
from glob import glob
from operator import itemgetter


def module_name_to_class(module_name):
    class_name = module_name.replace('_', ' ')
    class_name = ''.join(x for x in class_name.title() if not x.isspace())
    return class_name


def get_test_cases(directory):
    directory = directory[0:-1] if directory[-1:] is '/' else directory
    tests = glob(directory + '/test_*.py')
    test_list = []
    for module_path in tests:
        module_name = os.path.basename(module_path).replace('.py', '')
        class_name  = module_name_to_class(module_name)
        mod = __import__(module_name, fromlist=[class_name])
        klass = getattr(mod, class_name)

        # add a default priority
        if not hasattr(klass, 'priority'):
            klass.priority = 1000
        
        test_list.append(klass)
    # lower priority number ... the sooner it gets loaded
    return sorted(test_list, key=lambda k: k.priority, reverse=False)


def run_tests(directory="./"):
    test_list = get_test_cases(directory)
    test_load = unittest.TestLoader()
    cases = [test_load.loadTestsFromTestCase(t) for t in test_list]
    test_suite = unittest.TestSuite(cases)
    return unittest.TextTestRunner(verbosity=9).run(test_suite)

if __name__ == '__main__':
    tests_dir = os.path.dirname(os.path.realpath(__file__))
    # Include modules from parent directory
    sys.path.append("{}/..".format(tests_dir))
    sys.exit(run_tests(tests_dir).wasSuccessful() is False)
    