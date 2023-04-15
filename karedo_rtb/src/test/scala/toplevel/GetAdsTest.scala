package toplevel

import common.AllTests
import karedo.persist.entity._
import karedo.rtb.model.AdModel.{AdRequest, DeviceRequest}
import karedo.common.misc.Util._
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.{JUnit3Suite, JUnitRunner}
import karedo.rtb.actor._
import org.scalatest.{BeforeAndAfter, BeforeAndAfterEach}
import scala.collection.immutable.ListMap

/**
  * Created by crajah on 07/12/2016.
  */
@RunWith(classOf[JUnitRunner])
class GetAdsTest extends AllTests with BeforeAndAfterEach {
  val known_acctId = "fa9c4762-d1f8-48d8-94f8-1a68afa9bc6e"
  val known_appId = "fa9c4762-d1f8-48d8-94f8-1a68afa9bc6e"
  val known_msisdn = "+447711060452"
  val known_email = "chandan.rajah@gmail.com"
  val known_f_name = "Chandan"
  val known_l_name = "Rajah"

  val count = 1


  def deleteAllRelevantTables = {
    // Delete All
    dbUserAccount.deleteAll()
    dbUserProfile.deleteAll()
  }

  def setUpTestEnvironment = {
    // Set Up Knowns
    dbUserAccount.insertNew(UserAccount(id = known_acctId, userType = "CUSTOMER", temp = true, ts_created = now
      //      , mobile = List(Mobile(msisdn = known_msisdn, valid = true, ts_created = now, ts_validated = Some(now)))
      //      , email = List(Email(address = known_email, valid = true, ts_created = now, ts_validated = Some(now)) )
    ))

    dbUserProfile.insertNew(UserProfile(id = known_acctId, first_name = Some(known_f_name), last_name = Some(known_l_name)))

    def sortPrefMap(prefMap:Map[String, UserPrefData]): Map[String, UserPrefData] = {
      ListMap(prefMap.toSeq.sortWith(_._2.order < _._2.order):_*)
    }

    def getDefaultPrefMap():Map[String, UserPrefData] = {
      val prefMap = dbPrefs.load.map(x => x.id -> UserPrefData(x.default, x.name, x.order))(collection.breakOut): Map[String, UserPrefData]

      sortPrefMap(prefMap.filter(_._2.include))
    }

    val prefMap = getDefaultPrefMap

    val prefs = UserPrefs(known_acctId,
      prefMap, Some(now), now )

    dbUserPrefs.insertNew(prefs)

    }

  override def beforeEach {
    deleteAllRelevantTables
    setUpTestEnvironment
  }

  override def afterEach {
    deleteAllRelevantTables
  }


  "Get Ads" should {
    "* Ad Request" in {
      val devObj = DeviceRequest(
        ua = Some("Mozilla/5.0 (iPhone; CPU iPhone OS 10_1_1 like Mac OS X) AppleWebKit/602.2.14 (KHTML, like Gecko) Version/10.0 Mobile/14B150 Safari/602.1"),
        xff = None,
        ifa = Some(known_acctId),
        deviceType = Some(1),
        ip = Some("94.194.54.128"),
        make = Some("Apple"),
        model = Some("MKQK2B/A"),
        os = Some("iOS"),
        osv = Some("10.0.1"),
        did = None,
        dpid = None,
        mac = None,
        lat = Some(51.3081018),
        lon = Some(-0.565423),
        country = Some("US")
      )

      val adRequest = AdRequest(
        userId = known_acctId,
        count = count,
        device = devObj
      )

      val adActor = new AdActor()

      val ads = adActor.getAds(adRequest)

      ads.length shouldEqual count

    }
  }
}
