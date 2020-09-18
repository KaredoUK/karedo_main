from common import *
import unittest, json, base64
import re
import filecmp
import os
#
# This is testing Media upload and download
#

class TestMedia(unittest.TestCase):

    def test01_CreateMedia(self):
        title("PARALLELAI-94: Create Media")

        r = postfile('media',
                     file={'file': ('media', open('image.png','rb'), 'image/png' )})

        self.assertEqual(r.status_code, 200)

        js = json.loads(r.text)
        mediaId=js["mediaId"]

        inserted=fs.find_one({"_id": ObjectId(mediaId)})
        self.assertIsNotNone(inserted)
        self.assertEqual(inserted["length"],183960) # length of my media
        self.assertEqual(inserted["contentType"],"image/png")
        self.assertEqual(inserted["filename"],"media")




    def test02_GetMedia(self):

        title("PARALLELAI-97: API: Retrieve Media File")

        files = { 'file': ('media', open('image.png','rb'), 'image/png') }
        r = postfile('media', file=files)
        self.assertEqual(r.status_code, 200)

        js = json.loads(r.text)
        mediaId=js["mediaId"]

        r = getstream("media/"+mediaId)
        self.assertEqual(r.status_code, 200)
        import shutil

        try:
            os.remove('tmpFile')
        except OSError:
            pass

        f = open('tmpFile', 'w')
        f.write(r.content)
        f.close()



        self.assertTrue( filecmp.cmp('tmpFile', 'image.png') )

        os.remove('tmpFile')


suite = unittest.TestLoader().loadTestsFromTestCase(TestMedia)

if __name__ == '__main__':
    unittest.main()

