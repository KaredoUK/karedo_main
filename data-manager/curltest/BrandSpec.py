from common import *
import unittest, json

#
# This is testing many things in the
# "My Stores: brands" section contained in
# https://java.net/projects/parallelai/pages/RestAPISpecification
# Look for Question### for doubts
#

brandId=""
sessionId=""

class TestBrand(unittest.TestCase):
    def test00_CreateAndValidateUser(self):
        title("Setting up an initial user...")
        global applicationId, userId, sessionId
        r = post("account", {"applicationId": applicationId, "msisdn": "0044712345678", "email": "pakkio@gmail.com"})
        self.assertEqual(r.status_code, 200)
        doc = ua.find_one({"email": "pakkio@gmail.com"})
        activationCode = doc["applications"][0]["activationCode"]
        r = post("account/application/validation", {"applicationId": applicationId, "validationCode": activationCode, "password":"PASS"})
        self.assertEqual(r.status_code, 200)
        js = json.loads(r.text)
        userId = js["userID"]

        r = post("account/"+userId+"/application/"+applicationId+"/login",
                 {"password" : "PASS"})
        self.assertEqual(r.status_code, 200)
        js = json.loads(r.text)
        sessionId = js["sessionId"]



    def test01_CreateBrand(self):
        global brandId, sessionId

        title("PARALLELAI-67API: Create Brand")

        iconId="iconId"
        r = post("brand", {"name": "brandX", "iconId": iconId}, sessionId)
        self.assertEqual(r.status_code, 200)

        js = json.loads(r.text)
        brandId=js["id"]

        #doc = br.find_one({"name": "brandX"})
        doc = br.find_one({"_id": uuid.UUID(brandId)})
        self.assertNotEqual(doc,None)

        # FIXME how to check for authorization?
        # r = post("brand", {"name": "brandX", "iconId": iconId}, newUUID())
        #self.assertEqual(r.status_code, 401)

    def test02_FindBrands(self):
        global brandId,brandId2, sessionId
        title("PARALLELAI 95 API: Get Brands")

        r = post("brand", {"name": "brandY", "iconId": "iconId"}, sessionId)
        self.assertEqual(r.status_code, 200)
        js = json.loads(r.text)
        brandId2=js["id"]

        r=get("brand", sessionId)

        self.assertEqual(r.status_code, 200)

        js = json.loads(r.text)
        self.assertEqual(len(js),2)

        title("PARALLELAI 95 API: Get a single brand") # not present in doc but tested by SG
        r=get("brand/"+brandId, sessionId)

        self.assertEqual(r.status_code, 200)

        js = json.loads(r.text)
        self.assertEqual(js["name"],"brandX")

        r = post("brand", {"name": "brandY", "iconId": "iconId"}, newUUID())
        self.assertEqual(r.status_code, 401)

        r=get("brand/"+brandId, newUUID())
        self.assertEqual(r.status_code, 401)

    def test03_DeactivateBrand(self):
        global brandId2, sessionId
        title("PARALLELAI-68API: Deactivate Brand")

        r=delete("brand/"+brandId2, sessionId)
        self.assertEqual(r.status_code, 200)

        r=get("brand/"+brandId2, sessionId)
        self.assertEqual(r.status_code, 400)

        r=delete("brand/"+brandId2, sessionId)
        self.assertEqual(r.status_code, 200)

    def test04_CreateAdvert(self):
        global brandId,advId, sessionId
        title("PARALLELAI-65API: Create Ad")

        data={ "text":"adtext", "imageIds": [{ "imageId" : "iconId" }, { "imageId" : "iconId" } ], "value":5}
        r=post("brand/"+brandId+"/advert",data, sessionId)
        self.assertEqual(r.status_code, 200)

        js = json.loads(r.text)
        advId=js["id"]

        # FIXME how to check for authorization?
        #r=post("brand/"+brandId+"/advert",data, newUUID())
        #self.assertEqual(r.status_code, 401)

    def test05_DisableAdvert(self):
        global brandId, sessionId

        title("PARALLELAI-66API: Disable Ad")
        data={ "text":"adtext1", "imageIds": [ { "imageId" : "iconId" }, { "imageId" : "iconId" } ], "value":5}
        r=post("brand/"+brandId+"/advert",data, sessionId)

        self.assertEqual(r.status_code, 200)
        js = json.loads(r.text)
        advId2=js["id"]

        r=delete("brand/"+brandId+"/advert/"+advId2, sessionId)
        self.assertEqual(r.status_code, 200)

        # FIXME how to check for authorization?
        # r=delete("brand/"+brandId+"/advert/"+advId2, newUUID())
        # self.assertEqual(r.status_code, 401)

    def test051_ListAdsByBrand(self):
        global brandId, sessionId

        title("PARALLELAI-64API: List Ads per Brand")

        data={ "text":"A", "imageIds": [{ "imageId" : "iconId" }, { "imageId" : "iconId" }], "value":6}
        r=post("brand/"+brandId+"/advert",data, sessionId)
        self.assertEqual(r.status_code, 200)

        data={ "text":"B", "imageIds": [ { "imageId" : "iconIdB" }, { "imageId" : "iconIdB" }], "value":7}
        r=post("brand/"+brandId+"/advert",data, sessionId)
        self.assertEqual(r.status_code, 200)


        r=get("brand/"+brandId+"/advert", sessionId)
        self.assertEqual(r.status_code, 200)

        js=json.loads(r.text)
        self.assertEqual(len(js),3)

        self.assertEqual(js[2]['text'],'B')
        self.assertEqual(js[2]['value'],7)

        # FIXME how to check for authorization?
        #r=get("brand/"+brandId+"/advert", newUUID())
        #self.assertEqual(r.status_code, 401)




    def test06_AddBrandtouser(self):
        global userId, brandId, brandId2, sessionId

        title("PARALLELAI-90API: Add Brand to User")

        r = post("brand", {"name": "brandY", "iconId": "iconId"}, sessionId)
        self.assertEqual(r.status_code, 200)
        js = json.loads(r.text)
        brandId2=js["id"]

        r=post("account/"+userId+"/brand",{ "brandId": brandId }, sessionId)
        self.assertEqual(r.status_code, 200)

        r=post("account/"+userId+"/brand",{ "brandId": brandId2 }, sessionId)
        self.assertEqual(r.status_code, 200)

        r=post("account/"+userId+"/brand",{ "brandId": brandId2 }, newUUID())
        self.assertEqual(r.status_code, 401)

    def test07_Showuserbrands(self):
        global userId,brandId,brandId2, sessionId

        title("PARALLELAI-69API: Show User Brands")

        r=get("account/"+userId+"/brand", sessionId)
        self.assertEqual(r.status_code, 200)

        js=json.loads(r.text)
        self.assertEqual(len(js),2)

        self.assertEqual(js[0]["name"],"brandX")
        self.assertEqual(js[1]["name"],"brandY")

        r=get("account/"+userId+"/brand", newUUID())
        self.assertEqual(r.status_code, 401)

suite = unittest.TestLoader().loadTestsFromTestCase(TestBrand)

if __name__ == '__main__':
    unittest.main()

