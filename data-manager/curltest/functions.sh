#!/bin/bash
ENDPOINT="http://localhost:8080"

clearBrands() {
    mongo >pk <<EOF
    use wallet_data
    db.Brand.remove({})
EOF
}
#
# $1: route path
# $2: json post
#
curlpost() {

    curl -v -H "Content-Type:application/json" $ENDPOINT/$1 --data "$2" >pk 2>pk.err
}

curlget(){

    curl -v -H "Content-Type:application/json" $ENDPOINT/$1  >pk 2>pk.err
}

curldelete(){

    curl -v -X DELETE -H "Content-Type:application/json" $ENDPOINT/$1  >pk 2>pk.err
}
getid(){

    echo $(python -c 'import sys, json; print json.load(sys.stdin)[sys.argv[1]]' id < pk)

}

clearBrands

failed=false
