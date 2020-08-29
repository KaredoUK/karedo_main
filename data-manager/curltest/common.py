import requests, json, pymongo, uuid
import unittest

from pymongo import MongoClient

client = MongoClient()
db = client.wallet_data
ua = db.UserAccount
br = db.Brand
ad = db.AdvertisementDetail
ua.remove()
br.remove()
ad.remove()


def newUUID(): return str(uuid.uuid1())


userId = ""
applicationId = newUUID()


def title(x): print(
    "==========================================================================\n=======> " + x + "\n==========================================================================")


def info(x):  print("         " + x)

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


def put(route, data={}):
    info("METHOD: PUT")
    r=requests.put(httproute(route), data=json.dumps(data),
                        headers={'content-type': 'application/json'})
    printr(r)
    return r

def get(route, params={}):
    info("METHOD: GET")
    r=requests.get(httproute(route), #data=json.dumps(data),
                        headers={'content-type': 'application/json'})
    printr(r)
    return r

def delete(route, params={}):

    info("METHOD: DELETE")
    r=requests.delete(httproute(route), #data=json.dumps(data),
                           headers={'content-type': 'application/json'})
    printr(r)
    return r



