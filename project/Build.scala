import sbt._

object wallet extends Build {

  lazy val root = 
    Project("wallet", file("."))
      .aggregate(profile, offer)

  lazy val profile = 
    Project("profile", file("./profile")) 

  lazy val offer = 
    Project("offer", file("./offer")) 
}

