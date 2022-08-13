name := "karedo_persist"

version := "0.1_DEV"

scalaVersion := "2.11.8"

resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
	
	"com.github.salat" %% "salat" % "1.10.0"
  ,"com.typesafe" % "config" % "1.3.1"

  ,"org.specs2" %% "specs2-core" % "3.8.5" % "test"
)

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8",
  "-Yrangepos"
)

parallelExecution in Test := false

