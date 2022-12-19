from apt_pkg import init

__author__ = 'tja01'

from pymongo import MongoClient


class Data(object):

    def __init__(self):
        self.client = MongoClient('localhost', 30000)
        self.db = self.client.wallet_data

    def get_all_users(self):
        post = {},{"_id":1}
        return self.db.UserAccount.find(*post)
