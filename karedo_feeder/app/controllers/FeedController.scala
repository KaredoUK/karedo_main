package controllers

import java.net.URLDecoder
import java.security.MessageDigest
import java.util.{Base64, UUID}
import javax.inject._

import common.DbHelper
import play.api._
import play.api.mvc._
import karedo.persist.entity._
import karedo.common.result.{KO, OK, Result}
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.i18n.Messages.Implicits._

import org.joda.time.DateTime
import org.joda.time.DateTime.now

import scala.util.Try

/**
  * Created by charaj on 07/02/2017.
  */
class FeedController @Inject()(val messagesApi: MessagesApi) extends Controller with FormMechanic with I18nSupport {

  def getFeeds = play.api.mvc.Action {
    val feeds = FeedMechanic.getAll()

    val filledForm = feedForm.fill(Feed(id = UUID.randomUUID().toString, name = "name", url = "url", fallback_img = "url", prefs = List()))


    Ok(views.html.feeds(feeds, feedForm))
  }

  def saveFeeds(urlOrig: String) = play.api.mvc.Action {

    Try(URLDecoder.decode(urlOrig)).toOption match {
      case Some(url) => {
        val feed = Feed(
          id = Base64.getEncoder().encodeToString(MessageDigest.getInstance("MD5").digest(url.getBytes) ),
          name = "_",
          url = url,
          fallback_img = "http://karedo.co.uk/feeds/logo/O_alone.png",
          locale = "en_GB",
          prefs = List()
        )

        FeedMechanic.upsert(feed)

        Ok(views.html.message("Success"))
      }
      case None => Ok(views.html.message("Failure"))
    }
  }

  def postFeed = play.api.mvc.Action(parse.form(feedForm)) { implicit request =>
    val f = request.body

    println(f)

    val feed = f.copy(id = Base64.getEncoder().encodeToString(MessageDigest.getInstance("MD5").digest(f.url.getBytes) ))

    println(feed)

    FeedMechanic.upsert(feed)

    val feeds = FeedMechanic.getAll()

    Ok(views.html.feeds(feeds, feedForm))
  }

  def editFeed = play.api.mvc.Action(parse.form(basicForm)) { implicit request =>

    val feedId = request.body
    val feed = FeedMechanic.get(feedId)
    val prefs = FeedMechanic.getPrefs()

    Ok(views.html.feededit(feed, feedForm.fill(feed), prefs))
  }

  def updateFeed = play.api.mvc.Action(parse.form(feedForm)) { implicit request =>
    val feed = request.body
//    val feed = f.copy(id = Base64.getEncoder().encodeToString(MessageDigest.getInstance("MD5").digest(f.url.getBytes) ))
    FeedMechanic.update(feed)

    val feeds = FeedMechanic.getAll()

    Ok(views.html.feeds(feeds, feedForm))
  }

  def deleteFeed = play.api.mvc.Action {
    Ok("Your new application is ready.")
  }
}



object FeedMechanic extends DbHelper {
  def getAll():List[Feed] = {
    dbFeeds.findAll() match {
      case OK(r) => r
      case KO(_) => List()
    }
  }

  def get(id: String): Feed = {
    dbFeeds.find(id) match {
      case OK(f) => f
      case KO(_) => Feed(name = "", url = "", fallback_img = "")
    }
  }

  def insert(feed: Feed) = {
    dbFeeds.insertNew(feed)

    ()
  }

  def update(feed: Feed) = {
    dbFeeds.update(feed)

    ()
  }

  def upsert(feed: Feed) = {
    dbFeeds.find(feed.id) match {
      case OK(_) => dbFeeds.update(feed)
      case KO(_) => dbFeeds.insertNew(feed)
    }

    ()
  }

  def delete(feed: Feed) = {
    dbFeeds.delete(feed)

    ()
  }

  def getPrefs(): Map[String, String] = {
    dbPrefs.findAll() match {
      case OK(prefs) => prefs.map(p => p.id -> p.name).sortWith(_._1 < _._1).toMap
      case KO(_) => Map()
    }
  }
}

trait FormMechanic {
  import play.api.data._
  import play.api.data.Forms._

  val feedForm = Form(
    mapping(
      "id" -> default(text, UUID.randomUUID().toString),
      "name" -> nonEmptyText,
      "url" -> nonEmptyText,
      "fallback_img" -> nonEmptyText,
      "enabled" -> default(boolean, true),
      "locale" -> default(text, "en_GB"),
      "prefs" -> default(list(text), List())
    )(Feed.apply)(Feed.unapply)
  )

  val basicForm = Form(
    single(
    "id" -> nonEmptyText
    )
  )
}