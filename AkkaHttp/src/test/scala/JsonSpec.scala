import org.specs2.mutable.Specification

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

trait TestingJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  case class Obj(name: String, value: BigDecimal)
  case class Testing(code: String, names: List[Obj])
  implicit val jsonObj = jsonFormat2(Obj)
  implicit val jsonTesting = jsonFormat2(Testing)
}


// json is working esactly as we expected
class JsonSpec extends Specification with TestingJsonSupport {

  "json" should {
    "serialize and deserialize" in {

      val obj = Testing("a",List(Obj("b",3.14),Obj("c",6.28)))

      val json = obj.toJson
      val serialized = json.toString
      serialized must beEqualTo("""{"code":"a","names":[{"name":"b","value":3.14},{"name":"c","value":6.28}]}""")

      val unserialized = serialized.parseJson
      unserialized.convertTo[Testing] must beEqualTo(obj)



    }
  }

}
