val scala3Version = "3.2.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "Shuar Scala",
    version := "1.0.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "org.junit.jupiter" % "junit-jupiter-api" % "5.9.1" % Test,
      "com.h2database" % "h2" % "2.1.214" % Test,
    )
  )
