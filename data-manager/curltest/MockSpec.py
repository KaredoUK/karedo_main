from common import *
import pytest, json

#
# This is mainly testing current mocked service features
# it serves as a guide to understand what it is still missing
# but how it should work
#

brandId=newUUID()
advertId=newUUID()
userId=newUUID()
offerId=newUUID()
deviceId=newUUID()
sessionId=newUUID()

@pytest.mark.run(order=0)
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

    assert valid_uuid(sessionId)

@pytest.mark.run(order=1)
def test55():
    title("PARALLELAI-55API: User Brand Interaction")

    brandId=newUUID()
    r = post("user/"+userId+"/interaction/brand/"+brandId, { "interactionType":  "BUY"}, session=sessionId)
    assert r.status_code == HTTP_OK
    js = json.loads(r.text)
    info("userId: %s, userTotalPoints: %s" % (js["userId"],js["userTotalPoints"]))

@pytest.mark.run(order=2)
def test56():
    title("PARALLELAI-56API: User Ads Interaction")

    advertId = newUUID()
    r = post("user/"+userId+"/interaction/advert/"+advertId, session=sessionId)

    assert r.status_code == HTTP_OK
    js = json.loads(r.text)
    info("userId: %s, userTotalPoints: %s" %
         (js["userId"],js["userTotalPoints"]))

@pytest.mark.run(order=3)
def test57():
    title("PARALLELAI-57API: User Share Ad")
    pass

@pytest.mark.run(order=4)
def test59():
    title("PARALLELAI-59API: Get Next N Ads For User For Brand")
    r = get("account/"+userId+"/brand/"+brandId+"/ads?max=5", session=sessionId)
    assert r.status_code == HTTP_OK
    js = json.loads(r.text)
    for i in range(0,len(js)):
        info("id: %s, name: %s, iconId: %s" %
             (js[i]["id"], js[i]["name"], js[i]["iconId"]))

@pytest.mark.run(order=5)
def test61():
    title("PARALLELAI-61API: Get Ad Details")

    r = get("brand/"+brandId+"/advert/"+advertId, session=sessionId)
    assert r.status_code == HTTP_OK
    js = json.loads(r.text)
    info("title: %s, text: %s, imageIds: %s" %
         (js["title"],js["text"],js["imageIds"]))

@pytest.mark.run(order=6)
def test63():
    title("PARALLELAI-63API: User Buy Offer")
    r = post("user/"+userId+"/interaction/offer/"+offerId, { "interactionType":  "BUY"}, session=sessionId)
    assert r.status_code == HTTP_OK
    js = json.loads(r.text)
    info("userId: %s, userTotalPoints: %s" %
         (js["userId"],js["userTotalPoints"]))

@pytest.mark.run(order=7)
def test70():

    title("PARALLELAI-70API: List User Suggested Brands")
    r = get("account/"+userId+"/suggestedBrands", session=sessionId)
    assert r.status_code == HTTP_OK
    js = json.loads(r.text)
    for i in range(0,len(js)):
        info("id: %s, name: %s, iconId: %s" %
             (js[i]["id"], js[i]["name"], js[i]["iconId"]))

@pytest.mark.run(order=8)
def test71():
    title("PARALLELAI-71API: Remove User Brand")
    r = delete("account/"+userId+"/brand/"+brandId, session=sessionId)
    assert r.status_code == HTTP_OK
    assert r.text == "{}"

@pytest.mark.run(order=9)
def test79():
    title("PARALLELAI-79API: Show Pending Ads Per User Per Brand")
    r = get("account/"+userId+"/brand/"+brandId+"/pendingAds", session=sessionId)
    assert r.status_code == HTTP_OK
    js = json.loads(r.text)
    for i in range(0,len(js)):
        info("id: %s, text: %s, imageId: %s, value: %s" %
             (js[i]["id"], js[i]["text"], js[i]["imageId"],js[i]["value"]))

@pytest.mark.run(order=10)
def test80():
    title("PARALLELAI-80API: List Offers For User")
    r = get("user/"+userId+"/recommendedOffers?start=0&maxCount=5", session=sessionId)
    assert r.status_code == HTTP_OK
    js = json.loads(r.text)
    for i in range(0,len(js)):
        info("name: %s, brandId: %s, desc: %s, imageId: %s, qrCodeId: %s, value: %s" %
             (js[i]["name"],
              js[i]["brandId"],
              js[i]["desc"],
              js[i]["imageId"],
              js[i]["qrCodeId"],
              js[i]["value"]))

@pytest.mark.run(order=11)
def test81():
    title("PARALLELAI-81API: User Offer Interaction (like-dislike-share)")
    r = post("user/"+userId+"/interaction/offer/"+offerId, { "interactionType":  "LIKE"}, session=sessionId)
    assert r.status_code == HTTP_OK
    js = json.loads(r.text)
    info("userId: %s, userTotalPoints: %s" %
        (js["userId"],js["userTotalPoints"]))

@pytest.mark.run(order=12)
def test82():
    title("PARALLELAI-82API: Get Offer Details")
    r = get("offer/"+offerId, session=sessionId)
    assert r.status_code == HTTP_OK
    js = json.loads(r.text)
    info("name: %s, brandId: %s, desc: %s, imageId: %s, qrCodeId: %s, value: %s" %
         (js["name"],
         js["brandId"],
         js["desc"],
         js["imageId"],
         js["qrCodeId"],
         js["value"]))

@pytest.mark.run(order=13)
def test92():
    title("PARALLELAI-92API: Disable Offer")
    r = delete("offer/"+offerId, session=sessionId)
    assert r.status_code == HTTP_OK
    assert r.text == "{}"

