#!/bin/bash

@echo on

account_id=f203d810-4c9f-4cdd-9d43-df776896c153
application_id=f203d810-4c9f-4cdd-9d43-df776896c153
session_id=d198278f-cb4e-4b91-88a1-080e169a98eb
ad_count=20
sleep_time=1

c=0

while true
do
	echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> $c"

	curl -v --header "User-Agent: Mozilla/5.0 (iPhone; CPU iPhone OS 10_1_1 like Mac OS X) AppleWebKit/602.2.14 (KHTML, like Gecko) Version/10.0 Mobile/14B150 Safari/602.1" "http://api.karedo.co.uk:8080/account/$account_id/ads?p=$application_id&s=$session_id&c=$ad_count&lat=51.3081018&lon=-0.565423&ifa=$account_id&make=Apple&model=iPhone&os=iOS&osv=10.0.1&cc=GB"
	
	echo "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< $c"
	(( c ++ ))
	sleep $sleep_time
done
