package api

import spray.testkit.Specs2RouteTest
import spray.routing.Directives
import org.specs2.mutable.Specification
import spray.http.HttpResponse
import akka.testkit.{TestProbe, TestKit}
import parallelai.wallet.offer.services.RetailOfferService

class RegistrationServiceSpec extends Specification with Directives with Specs2RouteTest { //with TestKit {

  "The routing infrastructure should support" >> {
    "the most simple and direct route" in {
      Get() ~> complete(HttpResponse()) ~> (_.response) === HttpResponse()
    }
  }

//  val tp = TestProbe()
//
//  val c = new RetailOfferService( tp.ref )
//
//  "blah" >> {
//    "blah" in {
//      Post() ~> c.route ~> check {
//        responseAs[String] contains ""
//      }
//    }
//  }
}
