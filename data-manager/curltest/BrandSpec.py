from common import *
import unittest, json
from fixture import *

class TestBrand(unittest.TestCase):

    def test01_P67_CreateBrand(self):
        global sessionId, brandId
        title("PARALLELAI-67API: Create Brand")

        iconId="iconId"
        r = post("brand", {"name": "brandX", "iconId": iconId}, sessionId)
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

        r = post("brand", {"name": "brandY", "iconId": "iconId"}, sessionId)
        assert r.status_code == HTTP_OK
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

        data={ "text":"adtext", "imageIds": [{ "imageId" : "iconId" }, { "imageId" : "iconId" } ], "value":5}
        r=post("brand/"+brandId+"/advert",data, sessionId)
        assert r.status_code == HTTP_OK

        js = json.loads(r.text)
        advId=js["id"]

        r=post("brand/"+brandId+"/advert",data, newUUID())
        assert r.status_code == HTTP_AUTH_ERR

    def test05_P66_DisableAdvert(self):
        global sessionId, brandId, advId, advId2

        title("PARALLELAI-66API: Disable Ad")
        data={ "text":"adtext1", "imageIds": [ { "imageId" : "iconId" }, { "imageId" : "iconId" } ], "value":5}
        r=post("brand/"+brandId+"/advert",data, sessionId)

        assert r.status_code == HTTP_OK
        js = json.loads(r.text)
        advId2=js["id"]

        r=delete("brand/"+brandId+"/advert/"+advId2, session=sessionId)
        assert r.status_code == HTTP_OK

        r=delete("brand/"+brandId+"/advert/"+advId2, session=newUUID())
        assert r.status_code == HTTP_AUTH_ERR

    def test051_P64_ListAdsByBrand(self):
        global sessionId, brandId

        title("PARALLELAI-64API: List Ads per Brand")

        data={ "text":"A", "imageIds": [{ "imageId" : "iconId" }, { "imageId" : "iconId" }], "value":6}
        r=post("brand/"+brandId+"/advert",data, sessionId)
        assert r.status_code == HTTP_OK

        data={ "text":"B", "imageIds": [ { "imageId" : "iconIdB" }, { "imageId" : "iconIdB" }], "value":7}
        r=post("brand/"+brandId+"/advert",data, sessionId)
        assert r.status_code == HTTP_OK


        r=get("brand/"+brandId+"/advert", sessionId)
        assert r.status_code == HTTP_OK

        js=json.loads(r.text)
        assert len(js) == 3

        assert js[2]['text'] == 'B'
        assert js[2]['value'] == 7

        r=get("brand/"+brandId+"/advert", newUUID())
        assert r.status_code == HTTP_AUTH_ERR

    def test06_P90_AddBrandtouser(self):
        global sessionId, brandId, brandId2
        title("PARALLELAI-90API: Add Brand to User")

        r = post("brand", {"name": "brandY", "iconId": "iconId"}, sessionId)
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


suite = unittest.TestLoader().loadTestsFromTestCase(TestBrand)


if __name__ == '__main__':
    unittest.main()

