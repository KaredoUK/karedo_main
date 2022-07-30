from common import *
import unittest, json

#
# This is testing many things in the
# "User Profile" section contained in
# https://java.net/projects/parallelai/pages/RestAPISpecification
# Look for Question### for doubts
#
#from fixture import *

class TestMerchant(unittest.TestCase):
    
    def test01_P109_CreateAccountMerchant(self):
        global userId, applicationId
        title("PARALLELAI-109API: Create Account Merchant")

        r = post("account", {"applicationId": applicationId,
                             "msisdn": "004479097386290", # need to specify a different number here too
                             "email": "merchant@gmail.com",
                             "userType": "MERCHANT"
                             ""})

        self.assertEqual(r.status_code, HTTP_OK)

        js = json.loads(r.text)
        self.assertEqual(js["channel"], "email")

        doc = ua.find_one({"email": "merchant@gmail.com"})
        activationCode = doc["applications"][0]["activationCode"]
        userType=doc["userType"]
        self.assertEqual(userType,"MERCHANT")

        title("PARALLELAI-53API: Validate/Activate Account Application")
        r = post("account/application/validation",
                 {"applicationId": applicationId, "validationCode": activationCode, "password": "newPass"})

        self.assertEqual(r.status_code, HTTP_OK)

        js = json.loads(r.text)
        userId = js["userID"]
        info("UserId returned: " + js["userID"])



    def test01d_P102_LoginMerchant(self):
            global userId, applicationId, sessionId
            title("PARALLELAI-102API: Login Merchant")
            #  POST (JavaUUID / "application" / JavaUUID / "login"){
            r = post("account/"+userId+"/application/"+applicationId+"/login",
                     {"password" : "newPass"})
            self.assertEqual(r.status_code, HTTP_OK)
            js = json.loads(r.text)
            sessionId = js["sessionId"]

            self.assertTrue(valid_uuid(sessionId))
            info("login passed sessionId "+sessionId)

    def test02_P49_ResetApplicationMerchant(self):
        global userId, applicationId, sessionId
        applicationId = newUUID()
        title("PARALLELAI-49API: Reset Application for Merchant")

        info("app: " + applicationId)

        info("Question001: /reset is needed? doc is specifying it but original implementation didn't")
        r = put("account/" + userId + "/application/" + applicationId + "/reset")

        self.assertEqual(r.status_code, HTTP_OK)


        doc = ua.find_one({"email": "merchant@gmail.com"})
        activationCode = doc["applications"][1]["activationCode"]

        info("activationCode: "+activationCode)

        r = post("account/application/validation", {"applicationId": applicationId, "validationCode": activationCode})

        self.assertEqual(r.status_code, HTTP_OK)



    def test03_P52_deleteMerchantAccount(self):
        global userId, sessionId
        title("PARALLELAI-52API: Delete Account")
        r=delete("account/"+userId,session=sessionId)
        self.assertEqual(r.status_code, HTTP_OK)
        found=ua.find_one({ "email" : "merchant@gmail.com" })
        self.assertEqual(found,None)
        r=delete("account/"+userId,session=newUUID())
        self.assertEqual(r.status_code, HTTP_AUTH_ERR)





suite = unittest.TestLoader().loadTestsFromTestCase(TestMerchant)


if __name__ == '__main__':
    unittest.main()

