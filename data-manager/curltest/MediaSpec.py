from common import *
import pytest, json, base64
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


@pytest.mark.run(order=0)
def test00_CreateAndValidateUser():
    global applicationId, userId, sessionId
    title("Setting up an initial user...")

    email="pakkio2@gmail.com"
    r = post("account", {"applicationId": applicationId, "msisdn": "0044712345679", "email": email})
    assert r.status_code == HTTP_OK
    doc = ua.find_one({"email": email})
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
def test01_CreateMedia():
    global sessionId, mediaId
    title("PARALLELAI-94: Create Media")

    r = postfile('media',
                 file={'file': ('media', open('image.png','rb'), 'image/png' )},
                 session=sessionId)

    assert r.status_code ==  HTTP_OK

    js = json.loads(r.text)
    mediaId=js["mediaId"]

    inserted=fs.find_one({"_id": ObjectId(mediaId)})
    assert inserted != None
    assert inserted["length"] == 183960 # length of my media
    assert inserted["contentType"] == "image/png"
    assert inserted["filename"] == "media"

    r = postfile('media',
                 file={'file': ('media', open('image.png','rb'), 'image/png' )},
                 session=newUUID())

    assert r.status_code == HTTP_AUTH_ERR



@pytest.mark.run(order=3)
def test02_GetMedia():
    global sessionId, mediaId

    title("PARALLELAI-97: API: Retrieve Media File")

    files = { 'file': ('media', open('image.png','rb'), 'image/png') }
    r = postfile('media', file=files, session=sessionId)
    assert r.status_code == HTTP_OK

    js = json.loads(r.text)
    mediaId=js["mediaId"]

    r = getstream("media/"+mediaId, session=sessionId)
    assert r.status_code == HTTP_OK
    import shutil

    try:
        os.remove('tmpFile')
    except OSError:
        pass

    f = open('tmpFile', 'wb')
    f.write(r.content)
    f.close()



    assert filecmp.cmp('tmpFile', 'image.png')

    os.remove('tmpFile')

    r = getstream("media/"+mediaId, session=newUUID())
    assert r.status_code == HTTP_AUTH_ERR

