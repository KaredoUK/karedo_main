
import logging
import os
from datetime import datetime, date, time, timedelta

logging.basicConfig(filename='python.log', level=logging.DEBUG)


import util
util.DEBUG=int(os.getenv("DEBUG","0"))


from commonrest import *
from commondb import *


