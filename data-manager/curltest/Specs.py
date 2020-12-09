import unittest

import AccountSpec
import BrandSpec
import MediaSpec
import sys


if __name__ == '__main__':

    runner = unittest.TextTestRunner(verbosity=2)

    alltests = unittest.TestSuite([AccountSpec.suite,BrandSpec.suite,MediaSpec.suite])

    ret = not runner.run(alltests).wasSuccessful()
    sys.exit(ret)