from common import *
import unittest, json

#
# This is testing many things in the
# "My Stores: brands" section contained in
# https://java.net/projects/parallelai/pages/RestAPISpecification
# Look for Question### for doubts
#

brandId=newUUID()
advertId=newUUID()
userId=newUUID()
offerId=newUUID()



class TestOther(unittest.TestCase):



    def test70(self):

        title("PARALLELAI-70API: List User Suggested Brands")
        r = get("account/"+userId+"/suggestedBrands")
        self.assertEqual(r.status_code,200)
        js = json.loads(r.text)
        for i in range(0,len(js)):
            info("id: %s, name: %s, iconId: %s" %
                 (js[i]["id"], js[i]["name"], js[i]["iconId"]))

    def test79(self):
        title("PARALLELAI-79API: Show Pending Ads Per User Per Brand")
        r = get("account/"+userId+"/brand/"+brandId+"/pendingAds")
        self.assertEqual(r.status_code,200)
        js = json.loads(r.text)
        for i in range(0,len(js)):
            info("id: %s, text: %s, imageId: %s, value: %s" %
                 (js[i]["id"], js[i]["text"], js[i]["imageId"],js[i]["value"]))



    def test71(self):
        title("PARALLELAI-71API: Remove User Brand")
        r = delete("account/"+userId+"/brand/"+brandId)
        self.assertEqual(r.status_code,200)
        self.assertEquals(r.text,"{}")

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
        offerId = newUUID()
        r = post("user/"+userId+"/interaction/offer/"+offerId, { "interactionType":  "LIKE"})
        self.assertEqual(r.status_code,200)
        js = json.loads(r.text)
        info("userId: {0,s}, userTotalPoints: {1,s}"
            .format(js["userId"],js["userTotalPoints"]))




    def test82(self):
        title("PARALLELAI-82API: Get Offer Details")

        offerId = newUUID()
        r = get("offer/"+offerId)
        self.assertEqual(r.status_code,200)
        js = json.loads(r.text)
        info("name: {0:s}, brandId: {1:s}, desc: {2:s}, imageId: {3,s}, qrCodeId: {4,s}, value: {5,s}"
             .format(of["name"],
                     of["brandId"],
                     of["desc"],
                     of["imageId"],
                     of["qrCodeId"],
                     of["value"]))


    def test63(self):
        title("PARALLELAI-63API: User Buy Offer")
        r = post("user/"+userId+"/interaction/offer/"+offerId, { "interactionType":  "BUY"})
        self.assertEqual(r.status_code,200)
        js = json.loads(r.text)
        info("userId: %s, userTotalPoints: %s" %
             (js["userId"],js["userTotalPoints"]))



    def test92(self):
        title("PARALLELAI-92API: Disable Offer")

        offerId = newUUID()
        r = delete("offer/"+offerId)
        self.assertEqual(r.status_code,200)
        self.assertEqual(r.text,"{}")



    def test55(self):
        title("PARALLELAI-55API: User Brand Interaction")

        brandId=newUUID()
        r = post("user/"+userId+"/interaction/brand/"+brandId, { "interactionType":  "BUY"})
        self.assertEqual(r.status_code,200)
        js = json.loads(r.text)
        info("userId: %s, userTotalPoints: %s" % (js["userId"],js["userTotalPoints"]))


    def test57(self):
        title("PARALLELAI-57API: User Share Ad")
        self.fail("???")


    def test56(self):
        title("PARALLELAI-56API: User Ads Interaction")

        advertId = newUUID()
        r = post("user/"+userId+"/interaction/advert/"+advertId)

        self.assertEqual(r.status_code,200)
        js = json.loads(r.text)
        info("userId: %s, userTotalPoints: %s" %
             (js["userId"],js["userTotalPoints"]))




suite = unittest.TestLoader().loadTestsFromTestCase(TestOther)

if __name__ == '__main__':
    unittest.main()

