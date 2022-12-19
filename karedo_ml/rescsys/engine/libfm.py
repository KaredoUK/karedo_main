__author__ = 'tja01'

import numpy as np
from sklearn.feature_extraction import DictVectorizer
from pyfm import pylibfm

class LibFm(object):

    def __init__(self, train_path, test_path):
        self.v = DictVectorizer()
        (train_data, y_train, train_users, train_items) = self.loadData(train_path)
        (test_data, self.y_test, test_users, test_items) = self.loadData(test_path)
        self.X_train = self.v.fit_transform(train_data)
        self.X_test = self.v.transform(test_data)
        self.y_train = y_train
        self.model = None

    def loadData(self, filename, path="/home/tja01/data/ml-100k/"):
        data = []
        y = []
        users=set()
        items=set()
        with open(path+filename) as f:
            for line in f:
                (user,movieid,rating,ts)=line.split('\t')
                data.append({ "user_id": str(user), "movie_id": str(movieid)})
                y.append(float(rating))
                users.add(user)
                items.add(movieid)

        return (data, np.array(y), users, items)


    def train(self):
        self.model = pylibfm.FM(num_factors=10, num_iter=10, verbose=True, task="regression", initial_learning_rate=0.01, learning_rate_schedule="constant")
        self.model.fit(self.X_train, self.y_train)

    def evaluate(self):
        preds = self.model.predict(self.X_test)
        from sklearn.metrics import mean_absolute_error, mean_squared_error
        print "FM MAE: %.4f" % mean_absolute_error(self.y_test, preds)
        print "FM MSE: %.4f" % mean_squared_error(self.y_test, preds)


if __name__ == '__main__':
    fm = LibFm("ub.base", "ub.test")
    fm.train()
    fm.evaluate()



