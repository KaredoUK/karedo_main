#import http.client
import os
from datetime import datetime, date, time, timedelta

from util import *
from pymongo import MongoClient
from bson.codec_options import CodecOptions

#Root = "http://api.karedo.co.uk:8080/"
Root = ("http://" +
    os.getenv("KAREDO_PORT_8080_TCP_ADDR","localhost")+
    ":"+os.getenv("KAREDO_PORT_8080_TCP_PORT","8090")+"/")
    
MongoHost = os.getenv("MONGO_PORT_27017_TCP_ADDR","localhost")
MongoPort = os.getenv("MONGO_PORT_27017_TCP_PORT","12345")
MongoDb = os.getenv("MONGO_DB","wallet_data")
client = MongoClient(MongoHost,int(MongoPort))


def ISONow(ndays=0):
    ret= (datetime.utcnow()+timedelta(days=ndays)).isoformat()
    return ret


def clearDB():
    global client,MongoDb
    client.drop_database(MongoDb)

userId=newUUID()
deviceId=newUUID()


db = client[MongoDb]
JAVA=5 # uuid_type to properly understand UUIDS from DB

# Users collection
ua = db.get_collection('UserAccount', codec_options=CodecOptions(uuid_representation=JAVA))

# Brand collection
#br = db.Brand
br=db.get_collection('Brand', codec_options=CodecOptions(uuid_representation=JAVA))


# Offer collection
of = db.get_collection('Offer', codec_options=CodecOptions(uuid_representation=JAVA))

# media
fs = db["fs.files"]
#NO fs.uuid_subtype=JAVA

fs1 = db["fs.chunks"]
# NO fs1.uuid_subtype=JAVA


