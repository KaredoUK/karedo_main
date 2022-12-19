from common import *
import pytest
import json
import uuid

brandId=newUUID()
brandId2=newUUID()
userId=newUUID()
deviceId=newUUID()
sessionId=newUUID()

@pytest.mark.run(order=1)
def test00_CreateAndValidateUser():
    global deviceId,userId,sessionId
    title("Setting up an initial user...")

    r = post("account", {"deviceId": deviceId, "msisdn": "0044712345678", "email": "pakkio@gmail.com"})
    assert r.status_code == HTTP_OK
    doc = ua.find_one({"email": "pakkio@gmail.com"})
    activationCode = doc["applications"][0]["activationCode"]
    r = post("account/application/validation", {"deviceId": deviceId, "validationCode": activationCode, "password":"PASS"})
    assert r.status_code == HTTP_OK
    js = json.loads(r.text)
    userId = js["userID"]

    r = post("account/"+userId+"/application/"+deviceId+"/login",
             {"password" : "PASS"})
    assert r.status_code == HTTP_OK
    js = json.loads(r.text)
    sessionId = js["sessionId"]

    assert valid_uuid(sessionId) == True

@pytest.mark.run(order=2)
def test01_CreateOffer():
    global brandId

    title("PARALLELAI-91API: Create Offer")

    offer = {"name": "offerTest7", "brandId": str(uuid.uuid4())}

    r = post("offer", offer, session=sessionId)

    assert r.status_code == HTTP_OK



    js = json.loads(r.text)

    doc = of.find_one({"_id": uuid.UUID(js["offerId"])})

    assert doc != None

    assert str(doc["_id"]) == js['offerId']