#!/bin/bash

ES='https://sl-eu-lon-2-portal.3.dblayer.com:16981/_cluster'
ES_USER='-u admin:NYVTVIDHWCRRCUAX'

M_DB='Karedo'
M_COLL='APIMessage'

read -r -d '' ES_DATA << EOM
'{
  "type": "mongodb",
  "mongodb": {
    "servers": [
      { "host": "127.0.0.1", "port": 27017 }
    ],
    "db": "$M_DB",
    "collection": "$M_COLL",
    "options": { "secondary_read_preference": true },
    "gridfs": false
  },
  "index": {
    "name": "ARBITRARY INDEX NAME",
    "type": "ARBITRARY TYPE NAME"
  }
}'
EOM


echo $ES_DATA


