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



    def test01_P110_GetCodeMustBeAValid8DigitString(self):
        global sessionId, userId, code, advId
        title("PARALLELAI-110: GETCODE")
        r=post("offer/"+userId+"/getcode", { "userId": userId, "adId" : advId },sessionId)
        assert r.status_code == HTTP_OK

        js=json.loads(r.text)
        code = js["code"]
        assert len(code) == 8

    def test02_P111_OfferCodeValidate(self):
        global userId, sessionId, code
        title("P111: Validate Offer Code")
        r=post("offer/validate",{ "offerCode": code},sessionId)

        assert r.status_code == HTTP_OK
        js=json.loads(r.text)
        offerid=js["offerId"]

        assert valid_uuid(offerid)

    def test02b_P111_OfferCodeInvalidMustBeRejected(self):
        global userId,sessionId,code

        r=post("offer/validate",{ "offerCode": "AAAABBBB"},sessionId)

        assert r.status_code == HTTP_BAD_REQUEST

    def test03_P112_OfferCodeConsumeAnOfferAndCheckThatUserHasDecrementedPoints(self):
            global userId, sessionId, code, ua
            title("P112: Consume Offer Code")
            r=post("offer/consume",{ "offerCode": code},sessionId)

            assert r.status_code == HTTP_OK
            js=json.loads(r.text)
            offerid=js["offerId"]

            assert valid_uuid(offerid)

            user=ua.find_one({"email": "pakkio@gmail.com"})
            assert user["totalPoints"] == -5

    def test03a_P112_OfferCodeCantBeConsumedTwice(self):
            global userId,sessionId,code
            # code can be consumed only ONCE so this should give error (!)
            r=post("offer/validate",{ "offerCode": code},sessionId)

            assert r.status_code == HTTP_BAD_REQUEST

    def test03b_P112_OfferCodeOnlyForAValidCode(self):
            global userId,sessionId,code
            # invalid code must be rejected
            r=post("offer/validate",{ "offerCode": "foo"},sessionId)

            assert r.status_code == HTTP_BAD_REQUEST


    def test11_P107_InteractionBrandAShareOnFacebookMustEarn10Points(self):
        global sessionId, userId, brandId
        title("PARALLELAI-107: Brand Interaction")

        user=ua.find_one({"email": "pakkio@gmail.com"})
        initialPoints = user["totalPoints"]

        r=post("user/"+userId+"/interaction/brand",
            {  "userId" : userId, "brandId" : brandId, "interaction" : "share", "intType" : "facebook" },
               sessionId)

        assert r.status_code == HTTP_OK
        js=json.loads(r.text)
        userTotalPoints=js["userTotalPoints"]
        assert userTotalPoints == initialPoints + 10

    def test12_P108_InteractionAdAShareOnFacebookMustEarn15Points(self):
        global sessionId, userId, brandId, advId
        title("PARALLELAI-108: Ad Interaction")
        user=ua.find_one({"email": "pakkio@gmail.com"})
        initialPoints = user["totalPoints"]
        r=post("user/"+userId+"/interaction/offer",
               {  "userId" : userId, "offerId" : advId, "interaction" : "share", "intType" : "facebook" },
               sessionId)

        assert r.status_code == HTTP_OK
        js=json.loads(r.text)
        userTotalPoints=js["userTotalPoints"]
        assert userTotalPoints == (initialPoints + 15)

   

suite = unittest.TestLoader().loadTestsFromTestCase(TestOffer)


if __name__ == '__main__':
    unittest.main()

