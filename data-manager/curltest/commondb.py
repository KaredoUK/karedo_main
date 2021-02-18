#import http.client
import os

from util import *
from pymongo import MongoClient

#Root = "http://api.karedo.co.uk:8080/"
Root = os.getenv("ROOT","http://localhost:8090/")
MongoHost = os.getenv("MONGO_HOST","localhost")
MongoPort = os.getenv("MONGO_PORT","12345")
MongoDb = os.getenv("MONGO_DB","wallet_data")
client = MongoClient(MongoHost,int(MongoPort))

def clearDB():
    global client,MongoDb
    client.drop_database(MongoDb)

clearDB()

userId=newUUID()
applicationId=newUUID()


db = client.wallet_data
JAVA=5 # uuid_type to properly understand UUIDS from DB

# Users collection
ua = db.UserAccount
ua.uuid_subtype=JAVA

# Brand collection
br = db.Brand
br.uuid_subtype=JAVA

# Offer collection
of = db.Offer
of.uuid_subtype=JAVA

# media
fs = db["fs.files"]
#NO fs.uuid_subtype=JAVA

fs1 = db["fs.chunks"]
# NO fs1.uuid_subtype=JAVA


