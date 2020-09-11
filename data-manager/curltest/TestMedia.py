from common import *
import unittest, json, base64
import re
#
# This is testing Media upload and download
#

mediaId=newUUID()


class TestMedia(unittest.TestCase):



    def test01CreateMedia(self):
        global mediaId

        title("PARALLELAI-94: Create Media")

        r = postfile("media", {"media": open('image.png','rb') })


        self.assertEqual(r.status_code, 200)

        js = json.loads(r.text)
        mediaId=js["mediaId"]

        self.assertEqual(len(mediaId),24,"Should be a valid Id")

    def test02GetMedia(self):
        global mediaId

        title("PARALLELAI-97: API: Retrieve Media File")

        r = get("media/"+mediaId)
        self.assertEqual(r.status_code, 200)

        js=json.loads(r.text)
        mediaencoded=js["content"]
        media=base64.b64decode(mediaencoded)

        theimage = open('image.png','rb').read()

        self.assertEqual(media,theimage)




suite = unittest.TestLoader().loadTestsFromTestCase(TestMedia)

if __name__ == '__main__':
    unittest.main()

