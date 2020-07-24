name := "Temporal RI"

version := "0.1"

scalaVersion := "2.12.12"

scalacOptions ++= Seq("-language:implicitConversions", "-deprecation")
libraryDependencies ++= Seq(
    ("net.sf.trove4j" % "trove4j" % "3.0.3"),
    ("org.scalactic" %% "scalactic" % "3.2.0"),
    ("org.scalatest" %% "scalatest" % "3.2.0" % Test)
  )