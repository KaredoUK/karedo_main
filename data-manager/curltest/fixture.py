from common import *
import json
import sys


global applicationId,userId,sessionId

brandId=newUUID()
brandId2=newUUID()
userId=newUUID()
applicationId=newUUID()
sessionId=newUUID()
advId=newUUID()

clearDB()


r = post("account", {"applicationId": applicationId, "userType":"CUSTOMER", "msisdn": "0044712345678", "email": "pakkio@gmail.com"})
if (r.status_code != HTTP_OK): sys.exit()
doc = ua.find_one({"email": "pakkio@gmail.com"})
activationCode = doc["applications"][0]["activationCode"]
r = post("account/application/validation", {"applicationId": applicationId, "validationCode": activationCode, "password":"PASS"})
if (r.status_code != HTTP_OK): sys.exit()
js = json.loads(r.text)
userId = js["userID"]

r = post("account/"+userId+"/application/"+applicationId+"/login",
         {"password" : "PASS"})
if (r.status_code != HTTP_OK): sys.exit()
js = json.loads(r.text)
sessionId = js["sessionId"]

iconId="iconId"
r = post("brand", {"name": "brandX", "iconId": iconId}, sessionId)
if (r.status_code != HTTP_OK): sys.exit()
js = json.loads(r.text)
brandId=js["id"]

data={ "text":"adtext", "imageIds": [{ "imageId" : "iconId" }, { "imageId" : "iconId" } ], "value":5}
r=post("brand/"+brandId+"/advert",data, sessionId)
if (r.status_code != HTTP_OK): sys.exit()

js = json.loads(r.text)
advId=js["id"]

