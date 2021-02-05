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

class TestOffer(unittest.TestCase):



    def test01_GetCode(self):
        global sessionId, userId, code
        title("PARALLELAI-110: GETCODE")
        r=post("user/"+userId+"/getcode", { "userId": userId, "adId" : newUUID() },sessionId)
        assert r.status_code == HTTP_OK

        js=json.loads(r.text)
        code = js["code"]
        assert len(code) == 8

    def test01a_OfferCodeValidate(self):
        global userId, sessionId, code
        title("P110: Validate Offer Code")
        r=post("offer/validate",{ "offerCode": code},sessionId)

        assert r.status_code == HTTP_OK
        js=json.loads(r.text)
        offerid=js["offerId"]

        assert valid_uuid(offerid)

        r=post("offer/validate",{ "offerCode": "AAAABBBB"},sessionId)

        assert r.status_code == HTTP_BAD_REQUEST

    def test02_InteractionBrand(self):
        global sessionId, userId, brandId
        title("PARALLELAI-107: Brand Interaction")
        r=post("user/"+userId+"/interaction/brand",
            {  "userId" : userId, "brandId" : brandId, "interaction" : "share", "intType" : "facebook" },
               sessionId)

        assert r.status_code == HTTP_OK
        js=json.loads(r.text)
        userTotalPoints=js["userTotalPoints"]
        assert userTotalPoints == 10

    def test03_InteractionAd(self):
        global sessionId, userId, brandId, advId
        title("PARALLELAI-108: Ad Interaction")
        r=post("user/"+userId+"/interaction/offer",
               {  "userId" : userId, "offerId" : advId, "interaction" : "share", "intType" : "facebook" },
               sessionId)

        assert r.status_code == HTTP_OK
        js=json.loads(r.text)
        userTotalPoints=js["userTotalPoints"]
        assert userTotalPoints == 25



suite = unittest.TestLoader().loadTestsFromTestCase(TestOffer)


if __name__ == '__main__':
    unittest.main()

