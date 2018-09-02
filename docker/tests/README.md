# Testing
`suite.py` automatically loads all `test_*.py` files and adds them to the test suite
and runs the test suite


#### Adding a test

Below is an example test for the `cat_meower` module. In the tests folder create the filename `test_cat_meower.py`


```python
import unittest

class TestCatMeower(unittest.TestCase):
   # Tests for `cat_meower.py`

    def setUp(self):
        pass

    def test_cats_meow(self):
        pass
       
    # If you want the test to occur sooner
    # lower the priority - defaults to 1000 
    priority = 88
```