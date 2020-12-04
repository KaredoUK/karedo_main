import sbt._
import Keys._

object Common {

  val settings: Seq[Setting[_]] = Seq (
    version := "1.0",
    scalaVersion := "2.11.2",
    organization := "parallelai.wallet"
  )

  val akkaVersion = "2.3.6"

  val sl4jVersion = "1.7.5"

  val sprayVersion = "1.3.2"
  val sprayJsonVersion = "1.3.0" 

  val sprayResolvers = Seq (
    "spray repo" at "http://repo.spray.io",
    "spray nightlies" at "http://nightlies.spray.io"
  )

  val conjarResolver = "conjars.org" at "http://conjars.org/repo"

  val jodaTime = Seq( "joda-time" % "joda-time" % "2.0", "org.joda" % "joda-convert" % "1.2" )

  val specs2 = "org.specs2" %% "specs2" % "2.3.13"
  val sprayJson = "io.spray" %% "spray-json" % sprayJsonVersion
  val sprayClient = "io.spray" %% "spray-client" % sprayVersion
  val subcutExt = "com.pragmasoft" %% "subcut_ext" % "2.0" exclude("org.scala-lang", "scala-compiler")
  val scalaAsync = "org.scala-lang.modules" %% "scala-async" % "0.9.2"
  val walletCommon = "parallelai.wallet" %% "common" % "1.0" changing()
}
