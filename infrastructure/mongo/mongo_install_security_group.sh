#!/bin/bash

ec2-create-group database --region eu-west-1 --description "security group for database"

ec2-authorize database --region eu-west-1 -p 22

ec2-authorize database --region eu-west-1 -o database -u 901118918060

ec2-authorize database --region eu-west-1 -p 28017

ec2-create-group application --region eu-west-1 --description "security group for application servers"

ec2-authorize database --region eu-west-1 -o application -u 901118918060


