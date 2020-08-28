import requests, json, pymongo, uuid
import unittest

from pymongo import MongoClient

client = MongoClient()
db = client.wallet_data
ua = db.UserAccount
ua.remove()


def newUUID(): return str(uuid.uuid1())


userId = ""
applicationId = newUUID()


def title(x): print(
    "==========================================================================\n=======> " + x + "\n==========================================================================")


def info(x):  print(".... " + x)


def post(route, data={}):
    return requests.post("http://localhost:8080/" + route, data=json.dumps(data),
                         headers={'content-type': 'application/json'})


def put(route, data={}):
    return requests.put("http://localhost:8080/" + route, data=json.dumps(data),
                        headers={'content-type': 'application/json'})

def get(route, params={}):
    return requests.get("http://localhost:8080/" + route, #data=json.dumps(data),
                        headers={'content-type': 'application/json'})

def delete(route, params={}):
    return requests.delete("http://localhost:8080/" + route, #data=json.dumps(data),
                           headers={'content-type': 'application/json'})



