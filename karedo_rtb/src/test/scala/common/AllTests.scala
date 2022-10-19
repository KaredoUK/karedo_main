package common

import akka.http.scaladsl.testkit.ScalatestRouteTest
import karedo.entity.dao.MongoConnection
import karedo.rtb.model.DbCollections
import karedo.rtb.util.RtbConstants
import org.scalatest.{Matchers, WordSpec}
/**
  * Created by crajah on 07/12/2016.
  */
trait AllTests extends WordSpec
  with Matchers
  with MongoConnection
  with ScalatestRouteTest
  with RtbConstants
  with DbCollections
 {

}
