#!/bin/sh

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
clearBrands

failed=false


echo "Should find no brands..."
curlget brand

if ! grep -q "\[\]" pk
then
	echo "!!!Should have found no brands.."
	failed=true
fi


echo "Should not accept a brand without iconPath..."
curlpost brand '{ "name":"brandX", "iconPath":"" }'
if ! grep -q 400 pk.err
then
	echo "!!!Not answering bad request"
	failed=true

fi
if ! grep -q "IconPath must be not empty" pk
then
	echo "!!!Should have had errors because of iconPath empty"
	failed=true
fi


echo "Should not accept a brand without name..."
curlpost brand '{ "name":"", "iconPath":"icon" }'
if ! grep -q 400 pk.err
then
	echo "!!!Not answering bad request"
	failed=true

fi
if ! grep -q "Name must be not empty" pk
then
	echo "!!!Should have had errors because of name empty"
	failed=true
fi

echo "Should insert a brand..."
curlpost brand '{ "name":"brandX", "iconPath":"inconPath" }'

if ! grep -q 200 pk.err
then
	echo "!!!Should have answered a 200"
	failed=true
fi

if ! grep -q "\"id\"" pk
then
	echo "!!!Should have answered an Id"
	failed=true
fi

curlpost brand '{ "name":"brandY", "iconPath":"inconPath2" }'

echo "Should get the two brands..."
curlget brand

countid=$(grep -c "\"id\"" pk)
countname=$(grep -c "\"name\"" pk)
counticon=$(grep -c "\"iconPath\"" pk)

if [ "$countid" -ne "2" -o "$countname" -ne "2" -o "$counticon" -ne "2" ]
then
    echo "!!!Should have answered 2 entries"
    failed=true
fi



if [ "$failed" == true ]; then
    echo "!!!!!Test FAILED!!!!!"
    exit 1
else
    echo "Test Passed"
fi

