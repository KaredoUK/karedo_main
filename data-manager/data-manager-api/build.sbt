name := """data-manager-api"""

Common.settings

version := "1.0"

resolvers ++= Common.sprayResolvers 

libraryDependencies ++= Seq(
  "com.typesafe.akka"  %% "akka-actor"       % Common.akkaVersion,
  "com.typesafe.akka"  %% "akka-slf4j"       % Common.akkaVersion,
  "ch.qos.logback"      % "logback-classic"  % "2.2.3",
  "io.spray"            % "spray-can"        % Common.sprayVersion,
  "io.spray"            % "spray-routing"    % Common.sprayVersion,
  "commons-lang"        % "commons-lang"     % "2.3",
  Common.sprayClient,
  Common.sprayJson,
  Common.scalaAsync,
  Common.specs2 % "test",
  "io.spray"            % "spray-testkit"    % Common.sprayVersion    % "test",
  "com.typesafe.akka"  %% "akka-testkit"     % Common.akkaVersion     % "test",
  "com.novocode"        % "junit-interface"  % "0.7"                  % "test->default",
  "parallelai.wallet" %% "model" % "1.0" changing(),
  "com.github.tomakehurst" % "wiremock" % "1.38" % "test",


  Common.walletCommon
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

// test

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")
