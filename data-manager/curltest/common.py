import requests
import json
import pymongo
import uuid
import unittest
import re
import logging
import httplib

httplib.HTTPConnection.debuglevel = 1

logging.basicConfig(filename='python.log', level=logging.DEBUG)

from pymongo import MongoClient
from bson.objectid import ObjectId

Root = "http://localhost:8090/"
client = MongoClient("localhost",12345)



# to enable extra printing from the tests
DEBUG=True

HTTP_OK=200
HTTP_AUTH_ERR=401
HTTP_BAD_REQUEST=400

db = client.wallet_data
JAVA=5 # uuid_type to properly understand UUIDS from DB

# Users collection
ua = db.UserAccount
ua.uuid_subtype=JAVA
ua.remove()

# Brand collection
br = db.Brand
br.uuid_subtype=JAVA
br.remove()

# Offer collection
of = db.Offer
of.uuid_subtype=JAVA

# media
fs = db["fs.files"]
#NO fs.uuid_subtype=JAVA
fs.remove

fs1 = db["fs.chunks"]
# NO fs1.uuid_subtype=JAVA
fs1.remove



def newUUID(): return str(uuid.uuid1())

def valid_uuid(uuid):
    regex = re.compile('^[a-f0-9]{8}-?[a-f0-9]{4}-?4[a-f0-9]{3}-?[89ab][a-f0-9]{3}-?[a-f0-9]{12}\Z', re.I)
    match = regex.match(uuid)
    return bool(match)

userId = newUUID()
applicationId = newUUID()


from requests.auth import AuthBase

class KaredoAuth(AuthBase):
    """Attaches HTTP SessionId Authentication to the given Request object."""
    def __init__(self, sessionId):
        # setup any auth-related data here
        self.sessionId = sessionId

    def __call__(self, r):
        # modify and return the request
        r.headers['X-SESSION-ID'] = self.sessionId
        return r

def title(x):
    if(DEBUG):print("==========================================================================\n=======> " + x + "\n==========================================================================")

def getHeaders(s):
    return {'content-type': 'application/json', 'X-SESSION-ID': s}
    
# debug functions
def info(x):
    if(DEBUG):print("         " + x)

def postdata(x):
    r=json.dumps(x)
    info("data: "+str(x))
    return r

def printr(r):
    info("r.status_code:"+str(r.status_code))
    info("r.text:"+r.text)
    info(" ")
    return r


# helping function to compose final URI
def httproute(x):
    r=(Root+x)
    info("routing to:"+r)
    return r


# POST
def post(route, data={},session=None):
    info("METHOD: POST")
    r=requests.post(httproute(route), data=postdata(data),
                         headers=getHeaders(session))
    printr(r)
    return r

# POST a file
def postfile(route, file, headers= {'X-Content-Type': 'image/png'}, session=None):
    info("METHOD: POST MULTIPART")

    r=requests.request("POST",
                       httproute(route),
                       headers = headers,
                       files=file,
                       auth=KaredoAuth(session))
    printr(r)
    return r


# PUT
def put(route, data={}, session=None):
    info("METHOD: PUT")
    r=requests.put(httproute(route), data=json.dumps(data),
                        headers=getHeaders(session))
    printr(r)
    return r

# GET
def get(route, session=None):
    info("METHOD: GET")
    r=requests.get(httproute(route), #data=json.dumps(data),
                        headers=getHeaders(session))
    printr(r)
    return r

# GET a stream
def getstream(route, session=None):
    info("METHOD: GETSTREAM")
    r=requests.get(httproute(route), auth=KaredoAuth(session), stream=True)
    #this can produce encoding errors printr(r)
    return r

# DELETE
def delete(route, params={}, session=None):

    info("METHOD: DELETE")
    r=requests.delete(httproute(route), #data=json.dumps(data),
                           headers=getHeaders(session))
    printr(r)
    return r



