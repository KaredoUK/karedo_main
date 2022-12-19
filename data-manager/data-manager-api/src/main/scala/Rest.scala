import restapi.Apis
import core.{MongoPersistence, DependencyInjection, BootedCore, CoreActors}
import parallelai.wallet.persistence.mongodb.MongoAppSupport
import web.Web

object Rest extends App 
	with BootedCore // attached to spray lifecycle
  with DependencyInjection // read config
  with MongoPersistence
	with CoreActors // Instantiate main actors to take care of requests
	with Apis 		// REST api 
	with Web 		// provide HTTP container and LINK to actors
	with MongoAppSupport // some help for mongo conversions