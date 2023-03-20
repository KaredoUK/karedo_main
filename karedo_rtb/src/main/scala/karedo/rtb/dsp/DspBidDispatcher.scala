package karedo.rtb.dsp

import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.coding.{Deflate, Gzip, NoCoding}
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream._
import karedo.rtb.model.AdModel.{AdUnit, DeviceRequest}
import karedo.entity._
import karedo.rtb.model.BidRequestCommon._
import karedo.rtb.util.{DeviceMake, LoggingSupport, RtbConstants}
import karedo.util.Util.now
import com.typesafe.config.Config

import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.headers.HttpEncodings
import akka.stream.scaladsl.{Keep, Sink, Source}
import karedo.rtb.model.DbCollections

import scala.concurrent.duration._
import scala.collection.immutable.Map
import scala.util.{Failure, Success}
//import scala.concurrent.ExecutionContext.Implicits.global

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

abstract class DspBidDispather(config: DspBidDispatcherConfig)
  extends LoggingSupport
    with HttpDispatcher
    with DbCollections with RtbConstants  {

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

    singleFlowRequest(out_request)
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

  def singleFlowRequest(request: HttpRequest): Future[HttpResponse] = {
    val QueueSize = dsp_outbound_queue_size

    val authority = request.uri.authority
    val req_host = authority.host.address()
    val req_port = authority.port match { case 0 => 80 case x => x }

    logger.info(s"> ***** Host: ${req_host} and port: ${req_port}")

    // This idea came initially from this blog post:
    // http://kazuhiro.github.io/scala/akka/akka-http/akka-streams/2016/01/31/connection-pooling-with-akka-http-and-source-queue.html
    val poolClientFlow = Http().cachedHostConnectionPool[Promise[HttpResponse]](host = req_host)

    val queue =
      Source.queue[(HttpRequest, Promise[HttpResponse])](QueueSize, OverflowStrategy.dropNew)
        .via(poolClientFlow)
        .toMat(Sink.foreach({
          case ((Success(resp), p)) => p.success(resp)
          case ((Failure(e), p))    => p.failure(e)
        }))(Keep.left)
        .run()

    def queueRequest(request: HttpRequest): Future[HttpResponse] = {
      val responsePromise = Promise[HttpResponse]()
      queue.offer(request -> responsePromise).flatMap {
        case QueueOfferResult.Enqueued    => responsePromise.future
        case QueueOfferResult.Dropped     => Future.failed(new RuntimeException("Queue overflowed. Try again later."))
        case QueueOfferResult.Failure(ex) => Future.failed(ex)
        case QueueOfferResult.QueueClosed => Future.failed(new RuntimeException("Queue was closed (pool shut down) while running the request. Try again later."))
      }
    }

    queueRequest(request).map(decodeResponse).map(logTransaction(request, _))
  }

  private def decodeResponse(response: HttpResponse): HttpResponse = {
    val decoder = response.encoding match {
      case HttpEncodings.gzip ⇒
        Gzip
      case HttpEncodings.deflate ⇒
        Deflate
      case HttpEncodings.identity ⇒
        NoCoding
    }

    decoder.decode(response)
  }

  private def logTransaction(request: HttpRequest, response: HttpResponse): HttpResponse = {
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

    dbRTBMessages.insert_f(
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

    response
  }

  def singleRequest_NOTUSED(request: HttpRequest): Future[HttpResponse] = {

    implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

    val out = getHttpDispatcher.singleRequest(request).map(decodeResponse).map(logTransaction(request, _))

    out
  }

  def getHttpDispatcher() = getInternetDispatcher(config.scheme)
}
