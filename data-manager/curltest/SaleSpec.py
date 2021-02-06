from common import *
import unittest, json

#
# This is testing many things in the
# "My Stores: brands" section contained in
# https://java.net/projects/parallelai/pages/RestAPISpecification
# Look for Question### for doubts
#
code=""
from fixture import *

saleId=1


class SaleTest(unittest.TestCase):




    def test01_P116_CanCreateASale(self):
        global merchantId, merchantSessionId, saleId
        title("PARALLELAI-116: Create a Sale")

        r=post("merchant/"+merchantId+"/sale",
              {  "accountId" : merchantId, "points" : 500, "expireInSeconds": 0 },
              merchantSessionId)

        self.assertEqual( r.status_code, HTTP_OK)
        js=json.loads(r.text)
        saleId=js["saleId"]
        assert valid_uuid(saleId)

    def test02_P117_CanReadASale(self):

            # apparently this needs to setup everything when called from python Specs.py otherwise we have some
            # concurrency issue
            application=newUUID()
            merchant=mkAccount(application,"MERCHANT","1111","merchant@gmail.com")
            sess=loginAccount(merchant,application)
            nameMerchant(merchant,sess)
            sale=mkSale(merchant,sess)

            title("PARALLELAI-117: get Sale Details")

            r=get("sale/"+sale,
                  sess)

            self.assertEqual( r.status_code, HTTP_OK)
            js=json.loads(r.text)
            merchantName=js["merchantName"]
            self.assertEqual(merchantName,"Customer")

    def test02a_P117_CantReadAnInexistentSale(self):
            global merchantId, merchantSessionId, saleId
            title("PARALLELAI-117: get Sale Details")

            r=get("sale/"+newUUID(),
                  merchantSessionId)

            self.assertEqual( r.status_code, HTTP_BAD_REQUEST)


    def test03_P118_CompleteASale(self):
            global merchantId, merchantSessionId, saleId
            title("PARALLELAI-118: Sale Completion")

            r=post("sale/complete",
                   {  "accountId" : merchantId, "saleId" : saleId},
                   merchantSessionId)


            self.assertEqual( r.status_code, HTTP_OK)





   

suite = unittest.TestLoader().loadTestsFromTestCase(SaleTest)


if __name__ == '__main__':
    unittest.main()

