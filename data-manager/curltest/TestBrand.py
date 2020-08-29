from common import *
import unittest, json

#
# This is testing many things in the
# "My Stores: brands" section contained in
# https://java.net/projects/parallelai/pages/RestAPISpecification
# Look for Question### for doubts
#




class TestBrand(unittest.TestCase):
    def test00createAndValidateUser(self):
        title("Setting up an initial user...")
        global applicationId, userId
        r = post("account", {"applicationId": applicationId, "msisdn": "0044712345678", "email": "pakkio@gmail.com"})
        self.assertEqual(r.status_code, 200)
        doc = ua.find_one({"email": "pakkio@gmail.com"})
        activationCode = doc["applications"][0]["activationCode"]
        r = post("account/application/validation", {"applicationId": applicationId, "validationCode": activationCode})
        self.assertEqual(r.status_code, 200)
        js = json.loads(r.text)
        userId = js["userID"]



    def test01CreateBrand(self):
        global brandId

        title("PARALLELAI-67API: Create Brand")

        iconPath="icons/brandX.png"
        r = post("brand", {"name": "brandX", "iconPath": iconPath})


        self.assertEqual(r.status_code, 200)

        js = json.loads(r.text)
        brandId=js["id"]

        doc = br.find_one({"iconPath": iconPath})
        self.assertNotEqual(doc,None)

    def test02findBrands(self):
        global brandId,brandId2
        title("PARALLELAI 95 API: Get Brands")

        r = post("brand", {"name": "brandY", "iconPath": "/path2"})
        self.assertEqual(r.status_code, 200)
        js = json.loads(r.text)
        brandId2=js["id"]

        r=get("brand")

        self.assertEqual(r.status_code, 200)

        js = json.loads(r.text)
        self.assertEqual(len(js),2)

        title("PARALLELAI 95 API: Get a single brand") # not present in doc but tested by SG
        r=get("brand/"+brandId)

        self.assertEqual(r.status_code, 200)

        js = json.loads(r.text)
        self.assertEqual(js["name"],"brandX")

    def test03deactivateBrand(self):
        global brandId2
        title("PARALLELAI-68API: Deactivate Brand")

        r=delete("brand/"+brandId2)
        self.assertEqual(r.status_code, 200)

        r=get("brand/"+brandId2)

        self.assertEqual(r.status_code, 400)


    def test04createAdvert(self):
        global brandId,advId
        title("PARALLELAI-65API: Create Ad")

        data={ "text":"adtext", "imagePaths": ["path1","path2"], "value":5}
        r=post("brand/"+brandId+"/advert",data)
        self.assertEqual(r.status_code, 200)

        js = json.loads(r.text)
        advId=js["id"]

    def test05disableAdvert(self):
        global brandId

        title("PARALLELAI-66API: Disable Ad")
        data={ "text":"adtext1", "imagePaths": ["path11","path21"], "value":5}
        r=post("brand/"+brandId+"/advert",data)

        self.assertEqual(r.status_code, 200)
        js = json.loads(r.text)
        advId2=js["id"]

        r=delete("brand/"+brandId+"/advert/"+advId2)
        self.assertEqual(r.status_code, 200)


    def test06addBrandtouser(self):
        global userId, brandId, brandId2

        title("PARALLELAI-90API: Add Brand to User")

        r = post("brand", {"name": "brandY", "iconPath": "/path2"})
        self.assertEqual(r.status_code, 200)
        js = json.loads(r.text)
        brandId2=js["id"]

        r=post("account/"+userId+"/brand",{ "brandId": brandId })
        self.assertEqual(r.status_code, 200)

        r=post("account/"+userId+"/brand",{ "brandId": brandId2 })
        self.assertEqual(r.status_code, 200)


    def test07showuserbrands(self):
        global userId,brandId,brandId2

        title("PARALLELAI-69API: Show User Brands")

        r=get("account/"+userId+"/brand")
        self.assertEqual(r.status_code, 200)






if __name__ == '__main__':
    unittest.main()

