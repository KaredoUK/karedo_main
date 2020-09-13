from common import *
import unittest
import json
import uuid

class TestBrand(unittest.TestCase):

    def test01_CreateOffer(self):
        global brandId

        title("PARALLELAI-91API: Create Offer")

        offer = {"name": "offerTest7", "brandId": str(uuid.uuid4())}

        r = post("offer", offer)

        self.assertEqual(r.status_code, 200)

        js = json.loads(r.text)

        doc = of.find_one({"_id": uuid.UUID(js["offerId"])})

        self.assertNotEqual(doc, None)

suite = unittest.TestLoader().loadTestsFromTestCase(TestBrand)

if __name__ == '__main__':
    unittest.main()