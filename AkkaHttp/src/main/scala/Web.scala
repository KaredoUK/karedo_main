import akka.http.scaladsl.{ConnectionContext, Http}


object Web extends ActorSystem {
  val doSsl=false
  val connContext = if(doSsl) Ssl.sslContext else ConnectionContext.noEncryption()
  Http().bindAndHandle(Routes.route,"0.0.0.0",8081,connContext)
}
