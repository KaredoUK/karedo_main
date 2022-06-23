import requests
import json
import pymongo
import uuid
import unittest
import re
import logging
#import http.client
import os
from util import *
from commondb import *
from bson.objectid import ObjectId
import requests

#requests.packages.urllib3.disable_warnings()
#if(DEBUG==1): httpclient.HTTPConnection.debuglevel = 1

try:
    import http.client as http_client
except ImportError:
    # Python 2
    import httplib as http_client
http_client.HTTPConnection.debuglevel = 1

HTTP_OK=200
HTTP_AUTH_ERR=401
HTTP_BAD_REQUEST=400


from requests.auth import AuthBase

class KaredoAuth(AuthBase):
    """Attaches HTTP SessionId Authentication to the given Request object."""
    def __init__(self, sessionId):
        # setup any auth-related data here
        self.sessionId = sessionId

    def __call__(self, r):
        # modify and return the request
        r.headers['X-Session-Id'] = self.sessionId
        return r


def getHeaders(s):
    return {'content-type': 'application/json', 'X-Session-Id': s}

def getHeadersJson():
    return {'content-type': 'application/json' }
    

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
                         headers=getHeaders(session),
                    verify=False)
    printr(r)
    return r

def postrtb(route,port,data):
    r=requests.post("http://rtb.karedo.co.uk:"+str(port)+route,json.dumps(data),getHeadersJson())
    printr(r) 
    return r

# POST a file
def postfile(route, file, headers= {'X-Content-Type': 'image/png'}, session=None):
    info("METHOD: POST MULTIPART")

    r=requests.request("POST",
                       httproute(route),
                       headers = headers,
                       files=file,
                       auth=KaredoAuth(session),
                       verify=False)
    printr(r)
    return r


# PUT
def put(route, data={}, session=None):
    info("METHOD: PUT")
    r=requests.put(httproute(route), data=json.dumps(data),
                        headers=getHeaders(session),  verify=False)
    printr(r)
    return r

# GET
def get(route, session=None):
    info("METHOD: GET")
    r=requests.get(httproute(route), #data=json.dumps(data),
                        headers=getHeaders(session),  verify=False)
    printr(r)
    return r

# GET a stream
def getstream(route, session=None):
    info("METHOD: GETSTREAM")
    r=requests.get(httproute(route), auth=KaredoAuth(session), stream=True,  verify=False)
    #this can produce encoding errors printr(r)
    return r

# DELETE
def delete(route, params={}, session=None):

    info("METHOD: DELETE")
    r=requests.delete(httproute(route), #data=json.dumps(data),
                           headers=getHeaders(session),  verify=False)
    printr(r)
    return r



