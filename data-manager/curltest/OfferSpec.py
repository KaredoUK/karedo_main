from commontest import *
import unittest, json

#
# This is testing many things in the
# "My Stores: brands" section contained in
# https://java.net/projects/parallelai/pages/RestAPISpecification
# Look for Question### for doubts
#
code=""
from fixture import *

def almost_equal(value_1, value_2, accuracy = 10**-8):
    return abs(value_1 - value_2) < accuracy

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
        
        # be sure user is subscribed to that brand otherwise we get an error
        r=post("account/"+userId+"/brand",{ "brandId": brandId }, sessionId)
        assert r.status_code == HTTP_OK

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

    def test13_P119_CanSetAndRetrieveChange(self):
        global sessionId
        title("PARALLELAI-119: set and retrieve Karedos change")

        r=post("merchant/karedos/GBP", { "currency": "GBP", "change": 2310.0 }, sessionId)
        assert r.status_code == HTTP_OK

        r=get("merchant/karedos/GBP",sessionId)
        assert r.status_code == HTTP_OK

        js=json.loads(r.text)
        change=js["change"]
        assert almost_equal(change,2310) == True

    def test14_P115_DoMoneyConversion(self):
        global sessionId
        title("PARALLELAI-115: do money conversion")

        r=post("merchant/karedos/GBP", { "currency": "GBP", "change": 2310.0 }, sessionId)
        assert r.status_code == HTTP_OK

        r=put("merchant/convertmoney",{"currency":"GBP","amount":2},sessionId)
        assert r.status_code == HTTP_OK

        js=json.loads(r.text)
        amount=js["amount"]
        assert almost_equal(amount,2310*2) == True

    def test15_P124_GetAcceptedOffers(self):
        global sessionId, userId, advId, advId2, advId3
        title("PARALLELAI-124: which offers has been requested code")

        # create 3 codes for offers
        r=post("offer/"+userId+"/getcode", { "userId": userId, "adId" : advId },sessionId)
        assert r.status_code == HTTP_OK

        js=json.loads(r.text)
        code = js["code"]

        r=post("offer/"+userId+"/getcode", { "userId": userId, "adId" : advId2 },sessionId)
        assert r.status_code == HTTP_OK

        r=post("offer/"+userId+"/getcode", { "userId": userId, "adId" : advId3 },sessionId)
        assert r.status_code == HTTP_OK

        # consume 1
        r=post("offer/consume",{ "offerCode": code},sessionId)

        assert r.status_code == HTTP_OK

        # now read how many valid offers we have requested code for (should be 2 since 1 already consumed)
        r=get("account/"+userId+"/acceptedoffers",sessionId)
        assert r.status_code == HTTP_OK

        js=json.loads(r.text)
        assert len(js) == 2










suite = unittest.TestLoader().loadTestsFromTestCase(TestOffer)


if __name__ == '__main__':
    unittest.main()

