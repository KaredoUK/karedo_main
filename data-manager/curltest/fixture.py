from common import *
import util
import json
import sys


global applicationId,userId,sessionId, DEBUG

brandId=newUUID()
brandId2=newUUID()
userId=newUUID()
merchantId=newUUID()
applicationId=newUUID()
applicationId2=newUUID()
sessionId=newUUID()
merchantSessionId=newUUID()
advId=newUUID()
app2=newUUID()

clearDB()

oldDEBUG=util.DEBUG
util.DEBUG=0

###################### Create Account



def mkAccount(applicationId,userType,telephone,email):
    r = post("account", {"applicationId": applicationId, "userType":userType, "msisdn": telephone,
                         "email": email})
    if (r.status_code != HTTP_OK): sys.exit(101)
    doc = ua.find_one({"email": email})
    activationCode = doc["applications"][0]["activationCode"]
    r = post("account/application/validation", {"applicationId": applicationId, "validationCode": activationCode, "password":"PASS"})
    if (r.status_code != HTTP_OK): sys.exit(102)
    js = json.loads(r.text)
    userId = js["userID"]
    return userId

userId=mkAccount(applicationId,"CUSTOMER","1234","pakkio@gmail.com")
userId2=mkAccount(applicationId2,"CUSTOMER","9876","oikkap@gmail.com")
merchantId=mkAccount(app2,"MERCHANT","12345","merchant@gmail.com")


#################### Login account having back sessionID
def loginAccount(accountId,applicationId):
    r = post("account/"+accountId+"/application/"+applicationId+"/login",
             {"password" : "PASS"})
    if (r.status_code != HTTP_OK): sys.exit(103)
    js = json.loads(r.text)
    sessionId = js["sessionId"]
    return sessionId

sessionId=loginAccount(userId,applicationId)
merchantSessionId=loginAccount(merchantId,app2)
################################# Update Merchant with some name for Sale API we need to have a name
def nameMerchant(merchantId,session):
    data={
        "info":
            {
                "userId": merchantId,
                "userType": "CUSTOMER",
                "fullName": "Customer",
                "email": "customer@gmail.com",
                "msisdn": "004476543210",
                "postCode": "EC1",
                "country": "UK",
                #"birthDate": "",
                "gender": "M"
            },
        "settings":
            {
                "maxAdsPerWeek": 500
            },
        "totalPoints": 100 # Question004: not present in documentation and NOT actually working
    }


    r=put("account/"+merchantId,data,session=session)
    if (r.status_code != HTTP_OK): sys.exit(104)

nameMerchant(merchantId,merchantSessionId)

# creating some brand with some ads
def mkBrand(session):
    iconId="iconId"
    r = post("brand", {"name": "brandX", "iconId": iconId, 
                       "startDate":   ISONow(),
                       "endDate":  ISONow(10)},
              sessionId)
    if (r.status_code != HTTP_OK): sys.exit(105)
    js = json.loads(r.text)
    brandId=js["id"]
    return brandId

brandId=mkBrand(sessionId)

def mkOffer(brandId, session):

    data={ "shortText":"adtext",
           "detailedText":"longtext",
           "termsAndConditions":"T&C",
           "summaryImages": [
               { "imageId":"aaa", "imageType": 3 },
               { "imageId":"omega", "imageType": 4 }],
           "startDate":   ISONow(),
           "endDate":   ISONow(10),
           "imageIds": [{ "imageId" : "iconId" }, { "imageId" : "iconId" } ], "karedos":5}
    r=post("brand/"+brandId+"/advert",data, session)
    if (r.status_code != HTTP_OK): sys.exit(106)

    js = json.loads(r.text)
    advId=js["offerId"]
    return advId

advId=mkOffer(brandId,sessionId)
advId2=mkOffer(brandId,sessionId)
advId3=mkOffer(brandId,sessionId)



def mkSale(merchantId,session):
    r=post("sale/"+merchantId+"/create",
           {  "accountId" : merchantId, "points" : 500, "expireInSeconds": 0 },
           session)

    if (r.status_code != HTTP_OK): sys.exit(107)
    js = json.loads(r.text)
    saleId=js["saleId"]
    return saleId

util.DEBUG=oldDEBUG