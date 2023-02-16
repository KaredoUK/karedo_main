package karedo.rtb.dsp

import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream._
import karedo.rtb.model.AdModel.{AdUnit, DeviceRequest}
import karedo.entity._
import karedo.rtb.model.BidRequestCommon._
import karedo.rtb.util.DeviceMake
import karedo.util.Util.now
import com.typesafe.config.Config

import scala.concurrent.Future
import karedo.rtb.util.LoggingSupport

import scala.concurrent.{Await, ExecutionContext, Future}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import karedo.rtb.model.DbCollections

import scala.concurrent.duration._
import scala.collection.immutable.Map
import scala.util.{Failure, Success}

trait HttpDispatcher {
  implicit val actor_system = ActorSystem("rtb")
  implicit val actor_materializer = ActorMaterializer()

  val httpDispatcher = Http()
  val httpsDispatcher = Http()

  def getInternetDispatcher(scheme: HttpScheme) = {
    scheme match {
      case HTTP => httpDispatcher
      case HTTPS => httpsDispatcher
    }
  }
}

abstract class DspBidDispather(config: DspBidDispatcherConfig) extends LoggingSupport with HttpDispatcher with DbCollections  {

  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  def getAds(count: Int, user: User, device: Device, iabCatMap: Map[String, UserPrefData], make: DeviceMake, deviceRequest: DeviceRequest): List[AdUnit]

  case class HttpAdResponse
  (
    headers: Seq[HttpHeader],
    content: String
  )

  def responseEntityToHttpAdResponse(r: HttpResponse): HttpAdResponse = {
    logger.debug(s"IN: ${config.name}.responseEntityToHttpAdResponse. Response: ${r}" )

    val hResp = HttpAdResponse(
      headers = r.headers,
      content = Await.result(Unmarshal(r).to[String], 100 milliseconds)
        // Await.result(r.entity.toStrict(50 milliseconds).map(_.data.decodeString("UTF-8")), 100 milliseconds)
    )



    logger.info(s"${config.name}: HTTP Ad Response: ${hResp}")

    hResp
  }

  def singleRequestCall(uri_path: String, params: Map[String, String], headers: List[HttpHeader]): Future[HttpResponse] = {
    val uri = Uri(uri_path).withQuery(Query(params))

    logger.info(s"URI: ${uri}")

    val out_request = HttpRequest(
      GET,
      uri = uri,
      headers = headers
    )

    logger.info(s"Request (${config.name}) => ${out_request}")

    val dispatcher = getHttpDispatcher

    singleRequest(out_request)
  }

  def deserialize[T](r: HttpResponse)(implicit um: Unmarshaller[ResponseEntity, T]): Future[Option[T]] = {
    r.status match {
      case OK => {
        logger.debug(s"${config.name}: Successful Response 200 OK. ::: ${r}")
        Unmarshal(r.entity).to[T] map Some.apply
      }
      case _ => {
        logger.error(s"${config.name}: Failed Response: ${r}")
        Future(None)
      }
    }
  }

  def singleRequest(request: HttpRequest): Future[HttpResponse] = {
    val out = getHttpDispatcher.singleRequest(request)

    out.onComplete {
      case Success(response) => {
        println("&&&&&&&&&&&&&&&&&&&&&&&&&&&")

        implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

        Future {
          println("%%%%%%%%%%%%%%%%%%%%%")


          dbRTBMessages.insertNew(
            RTBMessage(
              id = s"${now} | ${config.name}",
              request = RequestMessage(
                source = Some(config.name),
                request = Some(request.toString()),
                entity = Some(request.entity.toString),
                headers = Some(request.headers.map(e => e.name() -> e.value()).toMap),
                protocol = Some(request.protocol.value),
                uri = Some(request.uri.toString()),
                method = Some(request.method.value)
              ),
              response = ResponseMessage(
                source = Some(config.name),
                response = Some(response.toString()),
                entity = Some(response.entity.toString),
                headers = Some(response.headers.map(e => e.name() -> e.value()).toMap),
                protocol = Some(response.protocol.value),
                status = Some(response.status.toString())
              )
            )
          )
        } onComplete {
          case Success(s) => logger.debug(s"${config.name} - Database Save Success: ${s}")
          case Failure(f) => logger.debug(s"${config.name} - Database Save Failure: ${f}")
        }
      }
      case Failure(f) => logger.error(s"${config.name} : Request failed. Received Failure: ${f}")
    }

    out
  }

  def getHttpDispatcher() = getInternetDispatcher(config.scheme)
}
