resolvers += Resolver.bintrayRepo("cakesolutions", "maven")
resolvers += Resolver.sonatypeRepo("releases")
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

val circeVersion = "0.9.3"
val circeDependencies = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

lazy val commonSettings = Seq(
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.6",
  libraryDependencies ++=
    Seq("org.scalatra" %% "scalatra" % "2.6.+",
      "net.cakesolutions" %% "scala-kafka-client-akka" % "1.1.0",
      "net.cakesolutions" %% "scala-kafka-client-testkit" % "1.1.0" % Test,
      "net.cakesolutions" %% "scala-kafka-client" % "1.1.0",
      "io.monix" %% "monix" % "3.0.0-RC1",
      "com.github.finagle" %% "finch-core" % "0.19.0",
      "com.github.finagle" %% "finch-circe" % "0.19.0",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
      "com.github.finagle" %% "finch-test" % "0.19.0" % Test,
      "org.scalatest" %% "scalatest" % "3.0.5" % Test) ++ circeDependencies
)

lazy val `meclador` = (project in file("."))
  .settings(
    commonSettings,
  )
