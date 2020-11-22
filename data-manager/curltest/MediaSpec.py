from common import *
import unittest, json, base64
import re
import filecmp
import os
#
# This is testing Media upload and download
#
userId=newUUID()
applicationId=newUUID()
sessionId=newUUID()
mediaId=newUUID()

class TestMedia(unittest.TestCase):
    
    def test00_CreateAndValidateUser(self):
        global applicationId, userId, sessionId
        title("Setting up an initial user...")
        
        email="pakkio2@gmail.com"
        r = post("account", {"applicationId": applicationId, "msisdn": "0044712345679", "email": email})
        self.assertEqual(r.status_code, HTTP_OK)
        doc = ua.find_one({"email": email})
        activationCode = doc["applications"][0]["activationCode"]
        r = post("account/application/validation", {"applicationId": applicationId, "validationCode": activationCode, "password":"PASS"})
        self.assertEqual(r.status_code, HTTP_OK)
        js = json.loads(r.text)
        userId = js["userID"]

        r = post("account/"+userId+"/application/"+applicationId+"/login",
                 {"password" : "PASS"})
        self.assertEqual(r.status_code, HTTP_OK)
        js = json.loads(r.text)
        sessionId = js["sessionId"]
        self.assertTrue(valid_uuid(sessionId))


    def test01_CreateMedia(self):
        global sessionId, mediaId
        title("PARALLELAI-94: Create Media")

        r = postfile('media',
                     file={'file': ('media', open('image.png','rb'), 'image/png' )}, 
                     session=sessionId)

        self.assertEqual(r.status_code, HTTP_OK)

        js = json.loads(r.text)
        mediaId=js["mediaId"]

        inserted=fs.find_one({"_id": ObjectId(mediaId)})
        self.assertIsNotNone(inserted)
        self.assertEqual(inserted["length"],183960) # length of my media
        self.assertEqual(inserted["contentType"],"image/png")
        self.assertEqual(inserted["filename"],"media")

        r = postfile('media',
                     file={'file': ('media', open('image.png','rb'), 'image/png' )}, 
                     session=newUUID())

        self.assertEqual(r.status_code, HTTP_AUTH_ERR)



    def test02_GetMedia(self):
        global sessionId, mediaId

        title("PARALLELAI-97: API: Retrieve Media File")

        files = { 'file': ('media', open('image.png','rb'), 'image/png') }
        r = postfile('media', file=files, session=sessionId)
        self.assertEqual(r.status_code, HTTP_OK)

        js = json.loads(r.text)
        mediaId=js["mediaId"]

        r = getstream("media/"+mediaId, session=sessionId)
        self.assertEqual(r.status_code, HTTP_OK)
        import shutil

        try:
            os.remove('tmpFile')
        except OSError:
            pass

        f = open('tmpFile', 'wb')
        f.write(r.content)
        f.close()



        self.assertTrue( filecmp.cmp('tmpFile', 'image.png') )

        os.remove('tmpFile')
        
        r = getstream("media/"+mediaId, session=newUUID())
        self.assertEqual(r.status_code, HTTP_AUTH_ERR)


suite = unittest.TestLoader().loadTestsFromTestCase(TestMedia)

if __name__ == '__main__':
    unittest.main()

