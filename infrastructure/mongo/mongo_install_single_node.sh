#!/bin/bash

ec2-run-instances ami-e565ba8c -n 1 -g database -k aws1\@parallelai.com -t m1.large --region eu-west-1



