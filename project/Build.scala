import sbt._
import Keys._

object KayBuild extends Build {

  lazy val project = Project (
    "akka-adapter",
    file("."),
    settings = Defaults.defaultSettings ++ Seq(
      organization := "com.kelt",
      name := "akka-adapter",
      version := "0.1.1",
      scalaVersion := "2.10.3",
      resolvers += Classpaths.typesafeReleases,
      libraryDependencies ++= Seq (
         "com.novus" % "salat_2.10" % "1.9.9",
         "com.typesafe.akka" % "akka-actor_2.10" % "2.3.5"
      ),
      publishTo := Some(Resolver.file("file", new File("/Users/keltonperson/Google Drive/KPMaven")))
    )
  )
}
