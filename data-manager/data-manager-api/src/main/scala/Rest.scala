import api.Api
import core.{BootedCore, CoreActors}
import parallelai.wallet.persistence.mongodb.MongoAppSupport
import web.Web

object Rest extends App with BootedCore with CoreActors with Api with Web with MongoAppSupport