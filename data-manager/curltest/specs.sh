#!/bin/sh
py.test --junitxml AccountSpec.xml  AccountSpec.py
py.test --junitxml BrandSpec.xml  BrandSpec.py
py.test --junitxml MediaSpec.xml  MediaSpec.py
#py.test --junitxml  MockSpec.py

