from common import *
import unittest, json

#
# This is testing Media upload and download
#

brandId=""


class TestMedia(unittest.TestCase):



    def test01CreateMedia(self):
        global mediaId

        title("PARALLELAI-94: Create Media")

        r = postfile("media", {"media": open('image.png','rb') })

        self.assertEqual(r.status_code, 200)

        js = json.loads(r.text)
        mediaId=js["mediaId"]


if __name__ == '__main__':
    unittest.main()

