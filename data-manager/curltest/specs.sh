#!/bin/sh
py.test --junitxml AccountSpec.xml  -v AccountSpec.py
py.test --junitxml BrandSpec.xml  -v BrandSpec.py
py.test --junitxml MediaSpec.xml  -v MediaSpec.py
#py.test --junitxml  MockSpec.py

