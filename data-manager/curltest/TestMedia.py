from common import *
import unittest, json, base64
import re
import filecmp
from os import *
#
# This is testing Media upload and download
#


class TestMedia(unittest.TestCase):

    def test01CreateMedia(self):
        title("PARALLELAI-94: Create Media")

        files = {'file': ('media', open('image.png','rb'), 'image/png', {'X-Upload-Content-Type': 'image/png', 'X-Upload-Name': 'image'} ) } 
        r = postfile('media', files)

        self.assertEqual(r.status_code, 200)

        js = json.loads(r.text)
        mediaId=js["mediaId"]

        self.assertEqual(len(mediaId),24,"Should be a valid Id")

    def test02GetMedia(self):

        title("PARALLELAI-97: API: Retrieve Media File")

        files = { 'file': ('media', open('image.png','rb'), 'image/png', {'X-Upload-Content-Type': 'image/png', 'X-Upload-Name': 'image'} ) } 
        r = postfile('media', files)
        js = json.loads(r.text)
        mediaId=js["mediaId"]

        r = get("media/"+mediaId)
        self.assertEqual(r.status_code, 200)

        remove('tmpFile')

        f = open('tmpFile', 'w')
        f.write(r.content)
        f.close()
       
        self.assertTrue( filecmp.cmp('tmpFile', 'image.png') )

        remove('tmpFile')


suite = unittest.TestLoader().loadTestsFromTestCase(TestMedia)

if __name__ == '__main__':
    unittest.main()

