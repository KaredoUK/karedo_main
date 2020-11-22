from common import *
import unittest, json

#
# This is testing many things in the
# "User Profile" section contained in
# https://java.net/projects/parallelai/pages/RestAPISpecification
# Look for Question### for doubts
#

userId=newUUID()
applicationId=newUUID()
sessionId=newUUID()


class TestAccount(unittest.TestCase):
    

    def test01_CreateAccount(self):
        global userId, applicationId
        title("PARALLELAI-77API: Create Account")

        r = post("account", {"applicationId": applicationId, "msisdn": "00447909738629", "email": "pakkio@gmail.com"})

        self.assertEqual(r.status_code, HTTP_OK)

        js = json.loads(r.text)
        self.assertEqual(js["channel"], "msisdn")

        doc = ua.find_one({"email": "pakkio@gmail.com"})
        activationCode = doc["applications"][0]["activationCode"]

        title("PARALLELAI-53API: Validate/Activate Account Application")
        r = post("account/application/validation",
                 {"applicationId": applicationId, "validationCode": activationCode, "password": "newPass"})

        self.assertEqual(r.status_code, HTTP_OK)

        js = json.loads(r.text)
        userId = js["userID"]
        info("UserId returned: " + js["userID"])


    def test01a_Login(self):
        global userId, applicationId, sessionId
        title("PARALLELAI-102API: Login")
        #  POST (JavaUUID / "application" / JavaUUID / "login"){
        r = post("account/"+userId+"/application/"+applicationId+"/login",
                 {"password" : "newPass"})
        self.assertEqual(r.status_code, HTTP_OK)
        js = json.loads(r.text)
        sessionId = js["sessionId"]

        self.assertTrue(valid_uuid(sessionId))
        info("login passed sessionId "+sessionId)

    def test02_ResetApplication(self):
        global userId, applicationId, sessionId
        applicationId = newUUID()
        title("PARALLELAI-49API: Reset Application for Account")

        info("app: " + applicationId)

        info("Question001: /reset is needed? doc is specifying it but original implementation didn't")
        r = put("account/" + userId + "/application/" + applicationId + "/reset")

        self.assertEqual(r.status_code, HTTP_OK)


        doc = ua.find_one({"email": "pakkio@gmail.com"})
        activationCode = doc["applications"][1]["activationCode"]

        info("activationCode: "+activationCode)

        r = post("account/application/validation", {"applicationId": applicationId, "validationCode": activationCode})

        self.assertEqual(r.status_code, HTTP_OK)

    def assertNotIn(self, member, container, msg=None):
        super(TestAccount, self).assertNotIn(member, container, msg)

    def test03_UpdateInfo(self):
        global userId, sessionId,applicationId
        title("PARALLELAI-50API: Update Account Settings")

        info("Question002: not present in documentation")
        info("Question003: which date format is valid in plain rest?")
        info("Question004: undocumented way to change totalPoints? or error?")

        data={
            "info":
                {
                    "userId": userId, # Question002: not present in documentation
                    "fullName": "Claudio Pacchiega",
                    "email": "claudio.pacchiega@gmail.com",
                    "msisdn": "004476543210",
                    "postCode": "EC1",
                    "country": "UK",
                    #"birthDate": "", <==== Question003: here which format is valid?
                    "gender": "M"
                },
            "settings":
                {
                    "maxAdsPerWeek": 500
                },
            "totalPoints": 100 # Question004: not present in documentation and NOT actually working
        }
        r=put("account/"+userId,data,session=sessionId)
        self.assertEqual(r.status_code, HTTP_OK)

        # tryng to do the same without authentication should be blocked
        r=put("account/"+userId,data,session=newUUID())
        self.assertEqual(r.status_code, HTTP_AUTH_ERR)



    def test04_GetInfo(self):
        global userId, sessionId
        title("PARALLELAI-51API: Get Account Settings ")

        r=get("account/"+userId,sessionId)
        self.assertEqual(r.status_code, HTTP_OK)
        js = json.loads(r.text)
        self.assertEqual(js["info"]["postCode"],"EC1")
        self.assertEqual(js["info"]["fullName"],"Claudio Pacchiega")
        self.assertEqual(js["settings"]["maxAdsPerWeek"],500)

        # called without authentication should fail
        r=get("account/"+userId,session=newUUID())
        self.assertEqual(r.status_code, HTTP_AUTH_ERR)

        # this is failing (!)
        #self.assertEqual(js["totalPoints"],100) # Question005: cfr question004 how can we change this value from rest APIs?

    def test05_getUserPoints(self):
        global userId, sessionId
        title("PARALLELAI-54API: Get User Points")

        r=get("account/"+userId+"/points",sessionId)
        self.assertEqual(r.status_code, HTTP_OK)
        js = json.loads(r.text)
        self.assertEqual(js["totalPoints"],0) # Question006: cfr Questions 004 and 005: how can we read something different from 0?

        r=get("account/"+userId+"/points",session=newUUID())
        self.assertEqual(r.status_code, HTTP_AUTH_ERR)

    def test06_deleteAccount(self):
        global userId, sessionId
        title("PARALLELAI-52API: Delete Account")
        r=delete("account/"+userId,session=sessionId)
        self.assertEqual(r.status_code, HTTP_OK)
        found=ua.find_one({ "email" : "pakkio@gmail.com" })
        self.assertEqual(found,None)
        r=delete("account/"+userId,session=newUUID())
        self.assertEqual(r.status_code, HTTP_AUTH_ERR)

suite = unittest.TestLoader().loadTestsFromTestCase(TestAccount)


if __name__ == '__main__':
    unittest.main()

