import uuid
import re
import logging
#import http.client
import os

DEBUG=0




def newUUID(): return str(uuid.uuid1())

def valid_uuid(uuid):
    regex = re.compile('^[a-f0-9]{8}-?[a-f0-9]{4}-?4[a-f0-9]{3}-?[89ab][a-f0-9]{3}-?[a-f0-9]{12}\Z', re.I)
    match = regex.match(uuid)
    return bool(match)


def title(x):
    if(DEBUG==1):
        print("==========================================================================\n=======> " + x + "\n==========================================================================")

# debug functions
def info(x):
    if(DEBUG==1):
        print(" " + x)

