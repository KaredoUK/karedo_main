from common import *
import pytest, json

#
# This is testing many things in the
# "My Stores: brands" section contained in
# https://java.net/projects/parallelai/pages/RestAPISpecification
# Look for Question### for doubts
#

brandId=newUUID()
brandId2=newUUID()
userId=newUUID()
applicationId=newUUID()
sessionId=newUUID()
advId=newUUID()

@pytest.mark.run(order=0)
def test00_CreateAndValidateUser():
    global applicationId,userId,sessionId
    title("Setting up an initial user...")

    r = post("account", {"applicationId": applicationId, "msisdn": "0044712345678", "email": "pakkio@gmail.com"})
    assert r.status_code == HTTP_OK
    doc = ua.find_one({"email": "pakkio@gmail.com"})
    activationCode = doc["applications"][0]["activationCode"]
    r = post("account/application/validation", {"applicationId": applicationId, "validationCode": activationCode, "password":"PASS"})
    assert r.status_code == HTTP_OK
    js = json.loads(r.text)
    userId = js["userID"]

    r = post("account/"+userId+"/application/"+applicationId+"/login",
             {"password" : "PASS"})
    assert r.status_code == HTTP_OK
    js = json.loads(r.text)
    sessionId = js["sessionId"]

    assert valid_uuid(sessionId)

@pytest.mark.run(order=1)
def test01_CreateBrand():
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

@pytest.mark.run(order=2)
def test02_FindBrands():
    global sessionId, brandId, brandId2
    title("PARALLELAI 95 API: Get Brands")

    r = post("brand", {"name": "brandY", "iconId": "iconId"}, sessionId)
    assert r.status_code == HTTP_OK
    js = json.loads(r.text)
    brandId2=js["id"]

    r=get("brand", sessionId)

    assert r.status_code == HTTP_OK

    js = json.loads(r.text)
    assert len(js) == 2

    title("PARALLELAI 95 API: Get a single brand") # not present in doc but tested by SG
    r=get("brand/"+brandId, sessionId)

    assert r.status_code == HTTP_OK

    js = json.loads(r.text)
    assert js["name"] == "brandX"

    r = post("brand", {"name": "brandY", "iconId": "iconId"}, newUUID())
    assert r.status_code == HTTP_AUTH_ERR

    r=get("brand/"+brandId, newUUID())
    assert r.status_code == HTTP_AUTH_ERR

@pytest.mark.run(order=3)
def test03_DeactivateBrand():
    global sessionId, brandId, brandId2
    title("PARALLELAI-68API: Deactivate Brand")

    r=delete("brand/"+brandId2, session=sessionId)
    assert r.status_code == HTTP_OK

    r=get("brand/"+brandId2, sessionId)
    assert r.status_code == HTTP_BAD_REQUEST

    r=delete("brand/"+brandId2, session=newUUID())
    assert r.status_code == HTTP_AUTH_ERR

@pytest.mark.run(order=4)
def test04_CreateAdvert():
    global sessionId, brandId, advId
    title("PARALLELAI-65API: Create Ad")

    data={ "text":"adtext", "imageIds": [{ "imageId" : "iconId" }, { "imageId" : "iconId" } ], "value":5}
    r=post("brand/"+brandId+"/advert",data, sessionId)
    assert r.status_code == HTTP_OK

    js = json.loads(r.text)
    advId=js["id"]

    r=post("brand/"+brandId+"/advert",data, newUUID())
    assert r.status_code == HTTP_AUTH_ERR

@pytest.mark.run(order=5)
def test05_DisableAdvert():
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

@pytest.mark.run(order=6)
def test051_ListAdsByBrand():
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

@pytest.mark.run(order=7)
def test06_AddBrandtouser():
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

@pytest.mark.run(order=8)
def test07_Showuserbrands():
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

