from commontest import *

import unittest, json
from fixture import *

class TestBrand(unittest.TestCase):

    def test01_P67_CreateBrand(self):
        global sessionId, brandId
        title("PARALLELAI-67API: Create Brand")

        iconId="iconId"
        r = post("brand", {
            "name": "brandX",
            "iconId": iconId,
            "startDate": ISONow(),
            "endDate": ISONow(10)
        }, sessionId)
        assert r.status_code == HTTP_OK
        js = json.loads(r.text)
        brandId=js["id"]

        #doc = br.find_one({"name": "brandX"})
        doc = br.find_one({"_id": uuid.UUID(brandId)})
        assert doc !=None

        r = post("brand", {"name": "brandX", "iconId": iconId}, newUUID())
        assert r.status_code == HTTP_AUTH_ERR

    def test02_P95_FindBrands(self):
        global sessionId, brandId, brandId2
        title("PARALLELAI 95 API: Get Brands")

        r = post("brand",
                 {"name": "brandY",
                  "iconId": "iconId",
                  "startDate": ISONow(),
                  "endDate": ISONow(10)},
                 sessionId)
        self.assertEqual(r.status_code,HTTP_OK)
        js = json.loads(r.text)
        brandId2=js["id"]

        r=get("brand", sessionId)

        assert r.status_code == HTTP_OK

        js = json.loads(r.text)
        assert len(js) == 3

        title("PARALLELAI 95 API: Get a single brand") # not present in doc but tested by SG
        r=get("brand/"+brandId, sessionId)

        assert r.status_code == HTTP_OK

        js = json.loads(r.text)
        assert js["name"] == "brandX"

        r = post("brand", {"name": "brandY", "iconId": "iconId"}, newUUID())
        assert r.status_code == HTTP_AUTH_ERR

        r=get("brand/"+brandId, newUUID())
        assert r.status_code == HTTP_AUTH_ERR

    def test03_P68_DeactivateBrand(self):
        global sessionId, brandId, brandId2
        title("PARALLELAI-68API: Deactivate Brand")

        r=delete("brand/"+brandId2, session=sessionId)
        assert r.status_code == HTTP_OK

        r=get("brand/"+brandId2, sessionId)
        assert r.status_code == HTTP_BAD_REQUEST

        r=delete("brand/"+brandId2, session=newUUID())
        assert r.status_code == HTTP_AUTH_ERR

    def test04_P65_CreateAdvert(self):
        global sessionId, brandId, advId
        title("PARALLELAI-65API: Create Ad")

        data={
            "shortText":"adtext",
            "detailedText":"longtext",
            "termsAndConditions":"T&C",
            "shareDetails":"share details",
            "summaryImages": [ { "imageId":"aaa", "imageType":1 }],
            "startDate":ISONow(),
            "endDate":ISONow(10),
            "imageIds": [{ "imageId" : "iconId" }, { "imageId" : "iconId" } ],
            "karedos":5}

        r=post("brand/"+brandId+"/advert",data, sessionId)
        assert r.status_code == HTTP_OK

        js = json.loads(r.text)
        advId=js["offerId"]

        r=post("brand/"+brandId+"/advert",data, newUUID())
        assert r.status_code == HTTP_AUTH_ERR

    def test05_P66_DisableAdvert(self):
        global sessionId, brandId, advId, advId2

        title("PARALLELAI-66API: Disable Ad")
        data={
            "shortText":"adtext1",
            "detailedText":"longtext",
            "termsAndConditions":"T&C",
            "shareDetails":"share details",
            "summaryImages": [ { "imageId":"aaa", "imageType":1 }],
            "startDate":ISONow(),
            "endDate":ISONow(10),
            "imageIds": [{ "imageId" : "iconId" }, { "imageId" : "iconId" } ],
            "karedos":5}
        r=post("brand/"+brandId+"/advert",data, sessionId)

        assert r.status_code == HTTP_OK
        js = json.loads(r.text)
        advId2=js["offerId"]

        r=delete("brand/"+brandId+"/advert/"+advId2, session=sessionId)
        assert r.status_code == HTTP_OK

        r=delete("brand/"+brandId+"/advert/"+advId2, session=newUUID())
        assert r.status_code == HTTP_AUTH_ERR

    def test051_P64_ListAdsByBrand(self):
        global sessionId, brandId

        title("PARALLELAI-64API: List Ads per Brand")

        data={
            "shortText":"A",
            "detailedText":"longtext",
            "termsAndConditions":"T&C",
            "shareDetails":"share details",
            "summaryImages": [ { "imageId":"aaa", "imageType":1 }],
            "startDate":ISONow(),
            "endDate":ISONow(10),
            "imageIds": [{ "imageId" : "iconId" }, { "imageId" : "iconId" } ],
            "karedos":5}
        r=post("brand/"+brandId+"/advert",data, sessionId)
        assert r.status_code == HTTP_OK

        data={
            "shortText":"B",
            "detailedText":"longtext",
            "termsAndConditions":"T&C",
            "shareDetails":"share details",
            "summaryImages": [ { "imageId":"aaa", "imageType":1 }],
            "startDate":ISONow(),
            "endDate":ISONow(10),
            "imageIds": [{ "imageId" : "iconId" }, { "imageId" : "iconId" } ],
            "karedos":7}


        r=post("brand/"+brandId+"/advert",data, sessionId)
        assert r.status_code == HTTP_OK


        r=get("brand/"+brandId+"/advert", sessionId)
        assert r.status_code == HTTP_OK

        js=json.loads(r.text)
        assert len(js) == 3

        assert js[2]['shortText'] == 'B'
        assert js[2]['karedos'] == 7


        r=get("brand/"+brandId+"/advert", newUUID())
        assert r.status_code == HTTP_AUTH_ERR



    def test06_P90_AddBrandtouser(self):
        global sessionId, brandId, brandId2
        title("PARALLELAI-90API: Add Brand to User")

        r = post("brand",
                 {"name": "brandY",
                  "iconId": "iconId",
                  "startDate": ISONow(),
                  "endDate":  ISONow(10)},
                 sessionId)
        assert r.status_code == HTTP_OK
        js = json.loads(r.text)
        brandId2=js["id"]

        r=post("account/"+userId+"/brand",{ "brandId": brandId }, sessionId)
        assert r.status_code == HTTP_OK

        r=post("account/"+userId+"/brand",{ "brandId": brandId2 }, sessionId)
        assert r.status_code == HTTP_OK

        r=post("account/"+userId+"/brand",{ "brandId": brandId2 }, newUUID())
        assert r.status_code == HTTP_AUTH_ERR

    def test07_P69_Showuserbrands(self):
        global sessionId, userId
        title("PARALLELAI-69API: Show User Brands")

        r=get("account/"+userId+"/brand", sessionId)
        assert r.status_code == HTTP_OK

        js=json.loads(r.text)
        assert len(js) == 2

        assert js[0]["name"] == "brandX"
        assert js[1]["name"] == "brandY"

        r=get("account/"+userId+"/brand", newUUID())
        assert r.status_code == HTTP_AUTH_ERR

    def test08_P123_GetActiveOffersNumber(self):
        global sessionId, userId, brandId
        title("Testing P123: multiple offers for brand")
        r=get("account/"+userId+"/brand/"+brandId,sessionId)
        assert r.status_code == HTTP_OK

        js=json.loads(r.text)
        assert js["numValidOffers"] == 3

        data={
            "shortText":"A",
            "detailedText":"longtext",
            "termsAndConditions":"T&C",
            "shareDetails":"share details",
            "summaryImages": [ { "imageId":"aaa", "imageType":1 }],
            "startDate":ISONow(5),
            "endDate":ISONow(10),
            "imageIds": [{ "imageId" : "iconId" }, { "imageId" : "iconId" } ],
            "karedos":7}
        r=post("brand/"+brandId+"/advert",data, sessionId)
        assert r.status_code == HTTP_OK
        r=get("account/"+userId+"/brand/"+brandId,sessionId)
        assert r.status_code == HTTP_OK

        js=json.loads(r.text)
        assert js["numValidOffers"] == 3
        data={
            "shortText":"B",
            "detailedText":"longtext",
            "termsAndConditions":"T&C",
            "shareDetails":"share details",
            "summaryImages": [ { "imageId":"aaa", "imageType":1 }],
            "startDate":ISONow(),
            "endDate":ISONow(10),
            "imageIds": [{ "imageId" : "iconId" }, { "imageId" : "iconId" } ],
            "karedos":7}

        r=post("brand/"+brandId+"/advert",data, sessionId)
        assert r.status_code == HTTP_OK

        r=get("account/"+userId+"/brand/"+brandId,sessionId)
        assert r.status_code == HTTP_OK

        js=json.loads(r.text)
        assert js["numValidOffers"] == 4

    def test59(self):
        global userId, sessionId
        title("PARALLELAI-59API: Get Next N Ads For User For Brand")
        r = get("account/"+userId+"/brand/"+brandId+"/ads?max=5", session=sessionId)
        self.assertEqual( r.status_code,  HTTP_OK)
        js = json.loads(r.text)
        for i in range(0,len(js)):
            info("id: %s, name: %s, iconId: %s" %
                 (js[i]["id"], js[i]["name"], js[i]["iconId"]))



    def test09_P125_126_GetAd(self):
        global sessionId, brandId

        title("PARALLELAI-124/125 API: GetOffer for various cases of states")

        data={
            "shortText":"A",
            "detailedText":"longtext",
            "termsAndConditions":"T&C",
            "shareDetails":"share details",
            "summaryImages": [ { "imageId":"aaa", "imageType":1 }],
            "startDate":ISONow(),
            "endDate":ISONow(10),
            "imageIds": [{ "imageId" : "iconId1" }, { "imageId" : "iconId2" } ],
            "karedos":5}
        r=post("brand/"+brandId+"/advert",data, sessionId)
        assert r.status_code == HTTP_OK

        js=json.loads(r.text)
        ad1=js["offerId"]

        r=get("brand/"+brandId+"/advert/"+ad1+"/summary",sessionId)
        assert r.status_code == HTTP_OK

        js=json.loads(r.text)
        assert js["shortText"] == "A"
        assert js["status"] == False

        summaryImages=js["summaryImages"]
        assert len(summaryImages) == 1

        assert summaryImages[0]["imageId"] == "aaa"
        assert summaryImages[0]["imageType"] == 1


        r=get("brand/"+brandId+"/advert/"+ad1+"/detail",sessionId)
        assert r.status_code == HTTP_OK

        js=json.loads(r.text)

        assert js["detailedText"] == "longtext"
        assert js["termsAndConditions"]== "T&C"
        assert js["shareDetails"]== "share details"
        assert js["state"] == "NotCreated"
        assert js["code"] == ""
        imageIds=js["imageIds"]

        assert len(imageIds) == 2
        assert imageIds[0]["imageId"] == "iconId1"

        # now create a code for this offer (for a different user than userId otherwise offersSpec would get confused)
        r=post("offer/"+userId+"/getcode", { "userId": userId2, "adId" : ad1 },sessionId)
        assert r.status_code == HTTP_OK

        js=json.loads(r.text)
        code = js["code"]

        # now we should find True when asking for Summary of this ad
        r=get("brand/"+brandId+"/advert/"+ad1+"/summary",sessionId)
        assert r.status_code == HTTP_OK

        js=json.loads(r.text)
        assert js["status"] == True


        # and the state must be Created with code
        r=get("brand/"+brandId+"/advert/"+ad1+"/detail",sessionId)
        assert r.status_code == HTTP_OK

        js=json.loads(r.text)
        assert js["state"]=="Created"
        assert js["code"]==code

        # now if we consume the offer
        r=post("offer/consume",{ "offerCode": code},sessionId)

        assert r.status_code == HTTP_OK

        # summary must still be True
        r=get("brand/"+brandId+"/advert/"+ad1+"/summary",sessionId)
        assert r.status_code == HTTP_OK

        js=json.loads(r.text)
        assert js["status"] == True

        # and state must be Consumed
        r=get("brand/"+brandId+"/advert/"+ad1+"/detail",sessionId)
        assert r.status_code == HTTP_OK

        js=json.loads(r.text)
        assert js["state"]=="Consumed"
        assert js["code"]==code


suite = unittest.TestLoader().loadTestsFromTestCase(TestBrand)


if __name__ == '__main__':
    unittest.main()

