from common import *
import unittest, json


#
# This is testing something in the new API requested in September 2016
# this is currently just a [PROTOTYPE] not yet connected with persistent memory
# using just mocking data
# from fixture import *

class TestHttp2(unittest.TestCase):
    def test01_KAR126_1_AnonymousFirst(self):
        global userId, applicationId
        title("KAR-126/1: Anonymous access")

        r = post("account/0/suggestedOffers",
                 {"sessionId": "",
                  "deviceId": "cea099a8f5ac3e289e317d461beb9261"}  # predefined value
                    )

        self.assertEqual(r.status_code, HTTP_OK)

        js = json.loads(r.text)
        self.assertEqual(js["sessionId"], "ca8201bd-c9ea-42e7-ad16-4a7427f769cc")


    def test02_KAR126_2_AnonymousReturning(self):
        global userId, applicationId
        title("KAR-126/2: Returning Anonymous access")

        r = post("account/0/suggestedOffers",
                 {"sessionId": "ca8201bd-c9ea-42e7-ad16-4a7427f769cc",
                  "deviceId": "cea099a8f5ac3e289e317d461beb9261"}  # predefined value
                 )

        self.assertEqual(r.status_code, HTTP_OK)

        js = json.loads(r.text)
        self.assertEqual(js["sessionId"], "28850c9a-276a-44a5-b773-7b74f1afcfc2")

    def test03_KAR126_2_NonAnonymous(self):
        global userId, applicationId
        title("KAR-126/3: NON Anonymous access")

        r = post("account/adf959bd-d591-441b-931c-fcd426c4d923/suggestedOffers",
                 {"sessionId": "ca8201bd-c9ea-42e7-ad16-4a7427f769cc",
                  "deviceId": "cea099a8f5ac3e289e317d461beb9261"}  # predefined value
                 )

        self.assertEqual(r.status_code, HTTP_OK)

        js = json.loads(r.text)
        self.assertEqual(js["sessionId"], "28850c9a-276a-44a5-b773-7b74f1afcfc2")


suite = unittest.TestLoader().loadTestsFromTestCase(TestHttp2)

if __name__ == '__main__':
    unittest.main()
