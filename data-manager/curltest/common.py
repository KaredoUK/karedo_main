import requests
import json
import pymongo
import uuid
import unittest

from pymongo import MongoClient
from bson.objectid import ObjectId

# to enable extra printing from the tests
DEBUG=True

client = MongoClient()
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

userId = newUUID()
applicationId = newUUID()

def title(x):
    global a
    if(DEBUG):print("==========================================================================\n=======> " + x + "\n==========================================================================")


def info(x):
    global a
    if(DEBUG):print("         " + x)

def httproute(x):
    r=("http://localhost:8080/"+x)
    info("routing to:"+r)
    return r

def postdata(x):
    r=json.dumps(x)
    info("data: "+str(x))
    return r

def printr(r):
    info("r.status_code:"+str(r.status_code))
    info("r.text:"+r.text)
    info(" ")
    return r

def post(route, data={}):
    info("METHOD: POST")
    r=requests.post(httproute(route), data=postdata(data),
                         headers={'content-type': 'application/json'})
    printr(r)
    return r

def postfile(route, file):
    info("METHOD: POST MULTIPART")
    r=requests.post(httproute(route), files=file)
    printr(r)
    return r



def put(route, data={}):
    info("METHOD: PUT")
    r=requests.put(httproute(route), data=json.dumps(data),
                        headers={'content-type': 'application/json'})
    printr(r)
    return r

def get(route):
    info("METHOD: GET")
    r=requests.get(httproute(route), #data=json.dumps(data),
                        headers={'content-type': 'application/json'})
    printr(r)
    return r

def getstream(route):
    info("METHOD: GETSTREAM")
    r=requests.get(httproute(route), stream=True)
    printr(r)
    return r

def delete(route, params={}):

    info("METHOD: DELETE")
    r=requests.delete(httproute(route), #data=json.dumps(data),
                           headers={'content-type': 'application/json'})
    printr(r)
    return r



