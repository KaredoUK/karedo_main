package karedo.entity

import com.mongodb.casbah.commons.MongoDBObject
import karedo.entity.dao._
import salat.annotations._
import karedo.util.{KO, OK, Result}


case class Pref
(
  @Key("_id") id: String
  , sort: Int
  , name: String
  , default: Double
  , order: Int
  , include: Boolean = true
) extends Keyable[String]


trait DbPrefs extends DbMongoDAO_Casbah[String,Pref] {
  def key(r:Pref) = r.id
  def load() = {
    dao.find(MongoDBObject()).sort(orderBy = MongoDBObject("sort"-> 1)).toList
    dao.find(MongoDBObject()).toList
  }

  def preload() = {

    val DEFAULT_VALUE = 0.5

    var index = 10
    def add(code:String, desc:String, order: Int, include:Boolean = true) =
      insertNew(Pref(code, index, desc, DEFAULT_VALUE, order, include))
      index=index+10

    deleteAll()

    add("IAB22",  "offers & discounts",       100)
    add("IAB18",  "fashion & style",          200)
    add("IAB8",   "food & drink",             300)
    add("IAB20",  "travel & holidays",        400)
    add("IAB17",  "sports",                   500)
    add("IAB6",   "family & children",        600)
    add("IAB7",   "health & fitness",         700)
    add("IAB19",  "computers & gadgets",      800)
    add("IAB4",   "jobs & careers",           900)
    add("IAB10",  "home & garden",            1000)
    add("IAB2",   "cars & bikes",             1100)
    add("IAB13",  "personal finance",         1200)
    add("IAB3",   "business & finance",       1300)
    add("IAB1",   "arts & entertainment",     1400)
    add("IAB14",  "community & society",      1500)
    add("IAB15",  "science",                  1600)
    add("IAB16",  "pets",                     1700)
    add("IAB5",   "education",                1800)
    add("IAB21",  "property & housing",       1900)
    add("IAB9",   "hobbies & interests",      2000)
    add("IAB11",  "law, Govt & politics",     2100)
    add("IAB12",  "news & current affairs",   2200)
    add("IAB23",  "religion & spirituality",  2300)

    // add("_EXCLUDE_",  "excluded",  20000, false)

    dao.count()

  }
}





