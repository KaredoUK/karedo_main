import api.Api
import core.{BootedCore, CoreActors}
import parallelai.wallet.persistence.mongodb.MongoAppSupport
import web.Web

object Rest extends App 
	with BootedCore // attached to spray lifecycle
	with CoreActors // Instantiate main actors to take care of requests
	with Api 		// REST api 
	with Web 		// provide HTTP container and LINK to actors
	with MongoAppSupport // some help for mongo conversions