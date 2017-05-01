import sbt.Keys._
import sbt._
import MyTasks._

object Settings {

  private val loggingDependencies = Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
    "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.8.2" % "provided",
    "org.apache.logging.log4j" % "log4j-api" % "2.8.2" % "provided",
    "org.apache.logging.log4j" % "log4j-core" % "2.8.2" % "provided"
  )

  val commonSettings = Seq(
    resolvers ++= Seq(
      Resolver.mavenLocal,
      Resolver.bintrayRepo("cuzfrog", "maven"),
      "Artima Maven Repository" at "http://repo.artima.com/releases",
      "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
      "spray repo" at "http://repo.spray.io"
    ),
    organization := "com.github.cuzfrog",
    scalaVersion := "2.12.2",
    crossScalaVersions := Seq("2.11.11", "2.12.2"),
    scalacOptions ++= Seq(
      "-Xlint",
      "-unchecked",
      "-deprecation",
      "-feature",
      "-language:postfixOps",
      "-language:implicitConversions",
      "-language:higherKinds",
      "-language:existentials"),
    libraryDependencies ++= loggingDependencies,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "utest" % "0.4.5" % "test"
    ),
    logBuffered in Test := false,
    testFrameworks += new TestFramework("utest.runner.Framework"),
    parallelExecution in Test := false,
    licenses += ("Apache-2.0", url("https://opensource.org/licenses/Apache-2.0"))
  )

  val publicationSettings = Seq(
    publishTo := Some("My Bintray" at s"https://api.bintray.com/maven/cuzfrog/maven/${name.value}/;publish=1")
  )

  val readmeVersionSettings = Seq(
    (compile in Compile) := ((compile in Compile) dependsOn versionReadme).value,
    versionReadme := {
      val contents = IO.read(file("README.md"))
      val regex =raw"""(?<=libraryDependencies \+= "com\.github\.cuzfrog" %% "${name.value}" % ")[\d\w\-\.]+(?=")"""
      val newContents = contents.replaceAll(regex, version.value)
      IO.write(file("README.md"), newContents)
    }
  )
}