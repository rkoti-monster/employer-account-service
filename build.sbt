import org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings
import sbt.Keys.{ libraryDependencies, mainClass }

name := "employer-account-service"
version := "0.0"
scalaVersion := "2.13.3"

credentials += Credentials(
  "Artifactory Realm",
  "monsternextsyd.jfrog.io",
  System.getenv("JFROG_USERNAME"),
  System.getenv("JFROG_PASSWORD")
)

resolvers += "jitpack".at("https://jitpack.io")
resolvers += "Artifactory releases".at("https://monsternextsyd.jfrog.io/artifactory/libs-release")

lazy val doobieVersion = "0.9.0"
lazy val http4sVersion = "0.21.7"
lazy val rhoVersion = "0.21.0-RC1"
lazy val zioVersion = "1.0.1"
lazy val zioInteropCatsVersion = "2.1.4.0"
lazy val circeVersion = "0.13.0"
lazy val pureConfigVersion = "0.13.0"
lazy val quillVersion = "3.5.2"
lazy val mysqlConnectorVersion = "8.0.13"
lazy val h2dbVersion = "1.4.200"
lazy val log4j2Version = "2.12.1"
lazy val scalaTestVersion = "3.1.2"
lazy val monocleVersion = "2.0.5"
lazy val jacksonVersion = "2.11.1"

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")

val CI = config("ci").extend(Compile)

lazy val root = (project in file("."))
  .settings(
    name := "employer-account-service",
    version in Docker := "latest",
    version := "0.0",
    dockerExposedPorts ++= Seq(8083),
    libraryDependencies ++= Seq(
      "org.tpolecat" %% "doobie-core",
      "org.tpolecat" %% "doobie-h2",
      "org.tpolecat" %% "doobie-hikari",
      "org.tpolecat" %% "doobie-quill"
    ).map(_ % doobieVersion),
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server",
      "org.http4s" %% "http4s-blaze-client",
      "org.http4s" %% "http4s-circe",
      "org.http4s" %% "http4s-dsl"
    ).map(_ % http4sVersion),
    libraryDependencies += "org.http4s"                      %% "rho-swagger"             % rhoVersion,
    libraryDependencies ++= Seq("dev.zio" %% "zio", "dev.zio" %% "zio-test-magnolia").map(_ % zioVersion),
    libraryDependencies ++= Seq("dev.zio" %% "zio-test", "dev.zio" %% "zio-test-sbt")
      .map(_ % zioVersion)
      .map(_ % Test),
    libraryDependencies ++= Seq("io.circe" %% "circe-generic", "io.circe" %% "circe-generic-extras").map(
      _ % circeVersion
    ),
    libraryDependencies += "mysql"                            % "mysql-connector-java"    % mysqlConnectorVersion,
    libraryDependencies += "dev.zio"                         %% "zio-interop-cats"        % zioInteropCatsVersion,
    libraryDependencies += "com.github.pureconfig"           %% "pureconfig"              % circeVersion,
    libraryDependencies += "io.getquill"                     %% "quill-jdbc"              % quillVersion,
    libraryDependencies += "com.h2database"                   % "h2"                      % h2dbVersion,
    libraryDependencies += "com.github.mlangc"               %% "zio-interop-log4j2"      % "1.0.0-RC21",
    libraryDependencies += "com.github.mlangc"               %% "slf4zio"                 % "1.0.0-RC21-2",
    libraryDependencies += "com.vlkan.log4j2"                 % "log4j2-logstash-layout"  % "1.0.3",
    libraryDependencies += "org.apache.logging.log4j"         % "log4j-slf4j-impl"        % log4j2Version,
    libraryDependencies += "org.apache.logging.log4j"         % "log4j-api"               % log4j2Version,
    libraryDependencies += "org.apache.logging.log4j"         % "log4j-core"              % log4j2Version,
    libraryDependencies += "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % jacksonVersion, // needed for yaml config
    libraryDependencies += "com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor" % "2.7.4", // Fix version because aws-sdk-java-core v1 brings in an older version that has cve-2016-3720
    libraryDependencies += "com.fasterxml.jackson.core"       % "jackson-databind"        % jacksonVersion // needed for yaml config
  )
  .enablePlugins(JavaAppPackaging)
  .configs(CI)
  .configs(IntegrationTest)
  .settings(inConfig(CI) {
    Defaults.compileSettings ++ wartremover.WartRemover.projectSettings ++
      Seq(
        sources in CI := {
          val old = (sources in CI).value
          old ++ (sources in Compile).value
        },
        wartremover.WartRemover.autoImport.wartremoverErrors ++= wartremoverRules
      )
  }: _*)
  .settings(scalacOptions in Compile := (scalacOptions in Compile).value.filterNot {
    _ contains "wartremover"
  })
  .settings(Defaults.itSettings)

libraryDependencies ++= Seq("io.monster.adtech.commons" % "test" % "2.0.4").map {
  _ % "it"
}

libraryDependencies ++= Seq(
  "org.scalatest"              %% "scalatest"                % scalaTestVersion,
  "org.scalamock"              %% "scalamock"                % "4.4.0",
  "org.scalatestplus"          %% "scalatestplus-scalacheck" % "3.1.0.0-RC2",
  "dev.zio"                    %% "zio-test"                 % zioVersion,
  "dev.zio"                    %% "zio-test-sbt"             % zioVersion,
  "com.github.julien-truffaut" %% "monocle-law"              % monocleVersion,
  "mysql"                       % "mysql-connector-java"     % mysqlConnectorVersion
).map(_ % "it, test")

dependencyClasspath in IntegrationTest := (dependencyClasspath in IntegrationTest).value ++ (exportedProducts in Test).value

inConfig(IntegrationTest)(scalafmtConfigSettings)

mainClass in (Compile, run) := Some("io.monster.ecomm.account.app.Main")
javaOptions in reStart := Seq("-Dconfig.resource=dev.conf")

// Catches discarding values in a block that was supposed to take varargs
wartremoverErrors in (Test, compile) += Wart.NonUnitStatements

// static analysis that should pass all Warts checking for successful compilation except the following cases
// Reference: https://www.wartremover.org/doc/warts.html
lazy val wartremoverRules = Warts.allBut(
  Wart.DefaultArguments,
  Wart.Overloading,
  Wart.Any,
  Wart.Nothing,
  Wart.Serializable,
  Wart.JavaSerializable,
  Wart.StringPlusAny,
  Wart.Equals,
  Wart.MutableDataStructures
)

testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))

coverageEnabled in (Test, test) := true
coverageExcludedPackages := ".*Environments;.*Main;.*Server;io.monster.ecomm.account.client.*;.*Configuration;" // Endpoints not in use so don't include for coverage
commands += Command.command("unit-tests") { state =>
  "clean" ::
    "coverage" ::
    "test" ::
    "coverageReport" ::
    state
}

parallelExecution in Test := false
parallelExecution in IntegrationTest := false

scalacOptions ++= Seq(
  "-language:higherKinds",
  "-deprecation",
  "-encoding",
  "utf-8",
  "-explaintypes",
  "-feature",
  "-language:existentials",
  "-language:implicitConversions",
  "-unchecked",
  "-Xlint:inaccessible",
  "-Xlint:infer-any",
  "-Xlint:type-parameter-shadow",
  "-Ywarn-dead-code",
  "-Ywarn-extra-implicit",
  "-Ywarn-unused:implicits",
  "-Ywarn-unused:imports",
  "-Ywarn-unused:locals",
  "-Ywarn-unused:patvars",
  "-Ywarn-unused:privates",
  "-Ywarn-value-discard"
)

javaOptions += "-Dlog4j2.threadContextMap=com.github.mlangc.zio.interop.log4j2.FiberAwareThreadContextMap"
fork in run := true
