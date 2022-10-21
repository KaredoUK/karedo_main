#!/bin/bash

@echo on

account_id=f203d810-4c9f-4cdd-9d43-df776896c153
application_id=f203d810-4c9f-4cdd-9d43-df776896c153
session_id=d198278f-cb4e-4b91-88a1-080e169a98eb
ad_count=20
sleep_time=0.05

c=0

while true
do
	echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> $c"

	curl -v -H "Content-Type: application/json" -X POST -d '{"tmax":250,"bcat":["IAB25","IAB26"],"id":"f203d810-4c9f-4cdd-9d43-df776896c153-1481298032433","app":{"storeurl":"tbc","name":"Karedo","privacypolicy":1,"bundle":"uk.co.karedo","domain":"karedo.co.uk","id":"karedo","paid":0,"ver":"1.0","content":{"cat":["IAB9","IAB4","IAB17","IAB8","IAB23","IAB15","IAB3","IAB11","IAB2","IAB21","IAB7"]},"cat":["IAB9","IAB4","IAB17","IAB8","IAB23","IAB15","IAB3","IAB11","IAB2","IAB21","IAB7"]},"imp":[{"id":"f203d810-4c9f-4cdd-9d43-df776896c153-1481298032433","banner":{"pos":1,"id":18,"expdir":[1,2,3,4],"h":250,"btype":[1,3,4],"w":300,"topframe":1,"mimes":["image/jpg","image/png","image/gif"]},"secure":0}],"user":{"id":"f203d810-4c9f-4cdd-9d43-df776896c153","geo":{"lat":51.3081018,"lon":-0.565423,"country":"GB"}},"device":{"devicetype":1,"ip":"83.244.247.165","model":"iPhone","ua":"Mozilla/5.0 (iPhone; CPU iPhone OS 10_1_1 like Mac OS X) AppleWebKit/602.2.14 (KHTML, like Gecko) Version/10.0 Mobile/14B150 Safari/602.1","geo":{"lat":51.3081018,"lon":-0.565423,"country":"GB"},"os":"iOS","ifa":"f203d810-4c9f-4cdd-9d43-df776896c153","osv":"10.0.1","make":"Apple","js":0}}' "http://openrtb.axonix.com/supply/2.1/bid/54ebb41c-08f7-4d58-b47b-4e74b84e751f"
	
	echo "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< $c"
	(( c ++ ))
	sleep $sleep_time
done
