lazy val doobie = (project in file("."))
  .settings(publishSettings:_*)
  .enablePlugins(ExerciseCompilerPlugin)
  .settings(
    organization := "org.scala-exercises",
    name         := "exercises-doobie",
    scalaVersion := "2.11.8",
    parallelExecution in Test := false,
    version := "0.3.0-SNAPSHOT",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots"),
      Resolver.sonatypeRepo("releases")
    ),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "2.2.4",
      "org.scala-exercises" %% "exercise-compiler" % version.value,
      "org.scala-exercises" %% "definitions" % version.value,
      "org.scalacheck" %% "scalacheck" % "1.12.5",
      "com.github.alexarchambault" %% "scalacheck-shapeless_1.12" % "0.3.1",
      "org.tpolecat" %% "doobie-core" % "0.3.0",
      "org.tpolecat" %% "doobie-contrib-h2" % "0.3.0",
      compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.0")
    )
  )

// Distribution

lazy val gpgFolder = sys.env.getOrElse("PGP_FOLDER", ".")

lazy val publishSettings = Seq(
  organizationName := "Scala Exercises",
  organizationHomepage := Some(new URL("https://scala-exercises.org")),
  startYear := Some(2016),
  description := "Scala Exercises: The path to enlightenment",
  homepage := Some(url("https://scala-exercises.org")),
  pgpPassphrase := Some(sys.env.getOrElse("PGP_PASSPHRASE", "").toCharArray),
  pgpPublicRing := file(s"$gpgFolder/pubring.gpg"),
  pgpSecretRing := file(s"$gpgFolder/secring.gpg"),
  credentials += Credentials("Sonatype Nexus Repository Manager",  "oss.sonatype.org",  sys.env.getOrElse("PUBLISH_USERNAME", ""),  sys.env.getOrElse("PUBLISH_PASSWORD", "")),
  scmInfo := Some(ScmInfo(url("https://github.com/scala-exercises/exercises-doobie"), "https://github.com/scala-exercises/exercises-doobie.git")),
  licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := Function.const(false),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("Snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("Releases" at nexus + "service/local/staging/deploy/maven2")
  }
)
