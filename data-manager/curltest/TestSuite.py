import unittest

import TestAccount
import TestBrand
import TestMedia


if __name__ == '__main__':

    runner = unittest.TextTestRunner(verbosity=2)

    alltests = unittest.TestSuite([TestAccount.suite,TestBrand.suite,TestMedia.suite])

    runner.run(alltests)