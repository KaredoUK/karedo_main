package karedo.entity

import com.mongodb.casbah.commons.MongoDBObject
import karedo.entity.dao.{DbMongoDAO, Keyable}
import salat.annotations._


case class Pref
(
  @Key("_id") id: String
  , sort: Int
  , description: String
  , value: Int
) extends Keyable[String]


trait DbPrefs extends DbMongoDAO[String,Pref] {
  def key(r:Pref) = r.id
  def load() = {
    dao.find(MongoDBObject()).sort(orderBy = MongoDBObject("sort"-> 1)).toList
  }
  def preload() = {
    var index = 10
    def add(code:String, desc:String) =
      insertNew(code,Pref(code,index,desc,0))
      index=index+10

    deleteAll()
    add("IAB1","Arts & Entertainment")
    add("IAB2","Automotive")
    add("IAB3","Business")
    add("IAB4","Careers")
    add("IAB5","Education")
    add("IAB6","Family & Parenting")
    add("IAB7","Health & Fitness")
    add("IAB8","Food & Drink")
    add("IAB9","Hobbies & Interests")
    add("IAB10","Home & Garden")
    add("IAB11","Law, Government, & Politics")
    add("IAB12","News")
    add("IAB13","Personal Finance")
    add("IAB14","Society")
    add("IAB15","Science")
    add("IAB16","Pets")
    add("IAB17","Sports")
    add("IAB18","Style & Fashion")
    add("IAB19","Technology & Computing")
    add("IAB20","Travel")
    add("IAB21","Real Estate")
    add("IAB22","Shopping")
    add("IAB23","Religion & Spirituality")

    dao.count()

  }
}





