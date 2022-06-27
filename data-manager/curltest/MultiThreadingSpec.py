from common import *
from parse import *

import unittest, json
from fixture import *

import threading

class Worker(threading.Thread):
    def __init__(self,num):
        super(Worker,self).__init__()
        self.num = num


    def run(self):
        global sessionId
        print "Launching "+str(self.num)
        iconId = "iconId"+str(self.num)
        r = post("brand", {
            "name": "brandX",
            "iconId": iconId,
            "startDate": ISONow(),
            "endDate": ISONow(10)
        }, sessionId)
        assert r.status_code == HTTP_OK
        js = json.loads(r.text)
        brandId = js["id"]

        # doc = br.find_one({"name": "brandX"})
        doc = br.find_one({"_id": uuid.UUID(brandId)})
        assert doc != None



        r = post("brand", {"name": "brandX", "iconId": iconId}, newUUID())
        assert r.status_code == HTTP_AUTH_ERR
        print "Ending "+str(self.num)

        br.update_one({'iconId':iconId},{'$set': {'iconId': iconId+"."+str(self.num)}})


class TestMultiThreading(unittest.TestCase):
    def test01_P67_CreateBrand(self):
        global sessionId, brandId
        title("MULTITHREADING: Create multiple Brands")

        threads = []
        for i in range(1000):
            t=Worker(i)
            threads.append(t)
            t.start()
            
        for t in threads:
            t.join()

    def test02_CheckResults(self):
        print "test02 running"
        for doc in br.find():
            icon = doc["iconId"]
            parsed = parse("iconId{}.{}",icon)
            
            #print "\n"+str(doc)+"\n"
            assert icon == 'iconId' or parsed[0]==parsed[1]
           

suite = unittest.TestLoader().loadTestsFromTestCase(TestMultiThreading)

if __name__ == '__main__':
    unittest.main()
