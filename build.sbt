import Settings._

shellPrompt in ThisBuild := { state => Project.extract(state).currentRef.project + "> " }

lazy val root = (project in file("."))
  .settings(commonSettings, publicationSettings, readmeVersionSettings)
  .settings(
    name := "excela",
    version := "0.1.1",
    libraryDependencies ++= Seq(
      "org.apache.poi" % "poi" % "3.16",
      "org.apache.poi" % "poi-ooxml" % "3.16"
    ),
    reColors := Seq("magenta")
  )

//lazy val utils = (project in file("./utils"))
//  .settings(commonSettings)
//  .settings(
//    name := "utils",
//    version := "0.0.3dev2",
//    libraryDependencies ++= Seq(
//      "org.scala-lang" % "scala-reflect" % scalaVersion.value % "compile"
//    )
//  )