name := "model"

Common.settings

val phantomVersion = "0.5.0"

parallelExecution in Test := false

resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases/" 

libraryDependencies ++= Seq(
  Common.scalaAsync,
  "com.novus"               %% "salat"                  % "1.9.9",
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
