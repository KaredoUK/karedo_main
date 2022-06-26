from commontest import *
import unittest, json

#
# This is testing many things in the
# "My Stores: brands" section contained in
# https://java.net/projects/parallelai/pages/RestAPISpecification
# Look for Question### for doubts
#

brandId=""


class TestAdExtraction(unittest.TestCase):
    def test00createSomeAds(self):
        title("Setting up initial ad in mongodb...")
        r = post("brand", {"name": "brandX", "iconId": newUUID()})
        self.assertEqual(r.status_code, 200)

        js = json.loads(r.text)
        brandId=js["id"]

        # create 10 ads for that brand
        for i in range(10):
            data={ "text":("adtext"+str(i)), "imageIds": [newUUID(), newUUID()], "value":5}
            r=post("brand/"+brandId+"/advert",data)
            self.assertEqual(r.status_code, 200)


if __name__ == '__main__':
    unittest.main()

