#!/bin/bash

. functions.sh


echo "Should be able to delete a brand..."

curlpost brand '{ "name":"brandX", "iconPath":"the_icon" }'


curldelete brand/$(getid)

curlget brand

if ! grep -q "\[\]" pk
then
	echo "!!!Should have found no brands.."
	failed=true
fi


if [ "$failed" == true ]; then
    echo "!!!!!Test FAILED!!!!!"
    exit 1
else
    echo "Test Passed"
fi

