from common import *
import unittest, json

#
# This is mainly testing current mocked service features
# it serves as a guide to understand what it is still missing
# but how it should work
#

brandId=newUUID()
advertId=newUUID()
userId=newUUID()
offerId=newUUID()



class TestMock(unittest.TestCase):

    def test55(self):
        title("PARALLELAI-55API: User Brand Interaction")

        brandId=newUUID()
        r = post("user/"+userId+"/interaction/brand/"+brandId, { "interactionType":  "BUY"})
        self.assertEqual(r.status_code,200)
        js = json.loads(r.text)
        info("userId: %s, userTotalPoints: %s" % (js["userId"],js["userTotalPoints"]))



    def test56(self):
        title("PARALLELAI-56API: User Ads Interaction")

        advertId = newUUID()
        r = post("user/"+userId+"/interaction/advert/"+advertId)

        self.assertEqual(r.status_code,200)
        js = json.loads(r.text)
        info("userId: %s, userTotalPoints: %s" %
             (js["userId"],js["userTotalPoints"]))

    def test57(self):
        title("PARALLELAI-57API: User Share Ad")
        self.fail("???")



    def test59(self):
        title("PARALLELAI-59API: Get Next N Ads For User For Brand")
        r = get("account/"+userId+"/brand/"+brandId+"/ads?max=5")
        self.assertEqual(r.status_code,200)
        js = json.loads(r.text)
        for i in range(0,len(js)):
            info("id: %s, name: %s, iconId: %s" %
                 (js[i]["id"], js[i]["name"], js[i]["iconId"]))



    def test61(self):
        title("PARALLELAI-61API: Get Ad Details")

        r = get("brand/"+brandId+"/advert/"+advertId)
        self.assertEqual(r.status_code,200)
        js = json.loads(r.text)
        info("title: %s, text: %s, imageIds: %s" %
             (js["title"],js["text"],js["imageIds"]))

    def test63(self):
        title("PARALLELAI-63API: User Buy Offer")
        r = post("user/"+userId+"/interaction/offer/"+offerId, { "interactionType":  "BUY"})
        self.assertEqual(r.status_code,200)
        js = json.loads(r.text)
        info("userId: %s, userTotalPoints: %s" %
             (js["userId"],js["userTotalPoints"]))

    def test70(self):

        title("PARALLELAI-70API: List User Suggested Brands")
        r = get("account/"+userId+"/suggestedBrands")
        self.assertEqual(r.status_code,200)
        js = json.loads(r.text)
        for i in range(0,len(js)):
            info("id: %s, name: %s, iconId: %s" %
                 (js[i]["id"], js[i]["name"], js[i]["iconId"]))

    def test71(self):
        title("PARALLELAI-71API: Remove User Brand")
        r = delete("account/"+userId+"/brand/"+brandId)
        self.assertEqual(r.status_code,200)
        self.assertEquals(r.text,"{}")

    def test79(self):
        title("PARALLELAI-79API: Show Pending Ads Per User Per Brand")
        r = get("account/"+userId+"/brand/"+brandId+"/pendingAds")
        self.assertEqual(r.status_code,200)
        js = json.loads(r.text)
        for i in range(0,len(js)):
            info("id: %s, text: %s, imageId: %s, value: %s" %
                 (js[i]["id"], js[i]["text"], js[i]["imageId"],js[i]["value"]))

    def test80(self):
        title("PARALLELAI-80API: List Offers For User")
        r = get("user/"+userId+"/recommendedOffers?start=0&maxCount=5")
        self.assertEqual(r.status_code,200)
        js = json.loads(r.text)
        for i in range(0,len(js)):
            info("name: %s, brandId: %s, desc: %s, imageId: %s, qrCodeId: %s, value: %s" %
                 (js[i]["name"],
                  js[i]["brandId"],
                  js[i]["desc"],
                  js[i]["imageId"],
                  js[i]["qrCodeId"],
                  js[i]["value"]))

    def test81(self):
        title("PARALLELAI-81API: User Offer Interaction (like-dislike-share)")
        r = post("user/"+userId+"/interaction/offer/"+offerId, { "interactionType":  "LIKE"})
        self.assertEqual(r.status_code,200)
        js = json.loads(r.text)
        info("userId: %s, userTotalPoints: %s" %
            (js["userId"],js["userTotalPoints"]))




    def test82(self):
        title("PARALLELAI-82API: Get Offer Details")
        r = get("offer/"+offerId)
        self.assertEqual(r.status_code,200)
        js = json.loads(r.text)
        info("name: %s, brandId: %s, desc: %s, imageId: %s, qrCodeId: %s, value: %s" %
             (of["name"],
             of["brandId"],
             of["desc"],
             of["imageId"],
             of["qrCodeId"],
             of["value"]))





    def test92(self):
        title("PARALLELAI-92API: Disable Offer")
        r = delete("offer/"+offerId)
        self.assertEqual(r.status_code,200)
        self.assertEqual(r.text,"{}")







suite = unittest.TestLoader().loadTestsFromTestCase(TestMock)

if __name__ == '__main__':
    unittest.main()

