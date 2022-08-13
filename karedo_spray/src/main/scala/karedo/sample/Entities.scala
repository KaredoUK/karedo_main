package karedo.sample

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
  * Created by gerrit on 8/15/16.
  */
trait Entities extends SprayJsonSupport with DefaultJsonProtocol {

  case class Record(a: Int, b: String)
  implicit val jsonRecord = jsonFormat2(Record)


}
