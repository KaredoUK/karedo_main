name := "karedo_persist"

version := "0.1_DEV"

scalaVersion := "2.11.7"

resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  Common.scalaAsync,
  "com.novus"               %% "salat"                  % "1.9.8", // 1.9.9 doesnot work!
  "org.mongodb"             %% "casbah"                 % "2.7.1",
  Common.subcutExt,
  //  Common.walletCommon,
  "com.github.athieriot"    %% "specs2-embedmongo"      % "0.7.0" % "test"
)

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)

testOptions in Test += Tests.Setup( () => Embedder.startMongo)

testOptions in Test += Tests.Cleanup( () => println("After Tests"))
