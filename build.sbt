name := "PkuSimulate"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.8"
javacOptions ++= Seq("-encoding", "UTF-8")
crossScalaVersions := Seq("2.12.8", "2.11.12")

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.2" % Test

libraryDependencies ++= Seq(
  jdbc,
  "org.choco-solver" % "choco-solver" % "4.10.14",
  "org.playframework.anorm" %% "anorm" % "2.6.2",
  "org.hibernate" % "hibernate-entitymanager" % "5.0.12.Final",
  "org.playframework.anorm" %% "anorm" % "2.6.2",
  "mysql" % "mysql-connector-java" % "5.1.47",
  // https://mvnrepository.com/artifact/org.json4s/json4s-native
  "org.json4s" %% "json4s-native" % "3.6.6",
  // https://mvnrepository.com/artifact/com.google.code.gson/gson
  "com.google.code.gson" % "gson" % "2.8.5",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "ch.qos.logback" % "logback-core" % "1.2.3",
  "org.slf4j" % "slf4j-api" % "1.7.26",
  "com.google.guava" % "guava" % "28.0-jre",
  "commons-codec" % "commons-codec" % "1.12",
  "org.apache.commons" % "commons-lang3" % "3.9",
  "junit" % "junit" % "4.12",
  "com.jason-goodwin" %% "authentikat-jwt" % "0.4.5",
  "org.apache.commons" % "commons-lang3" % "3.9",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)
