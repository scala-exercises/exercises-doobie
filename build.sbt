val scalaExercisesV = "0.4.0-SNAPSHOT"

def dep(artifactId: String) = "org.scala-exercises" %% artifactId % scalaExercisesV

lazy val doobie = (project in file("."))
  .enablePlugins(ExerciseCompilerPlugin)
  .settings(
    name         := "exercises-doobie",
    libraryDependencies ++= Seq(
      dep("exercise-compiler"),
      dep("definitions"),
      %%("doobie-core"),
      %%("doobie-h2"),
      %%("scalatest"),
      %%("scalacheck"),
      %%("scheckShapeless")
    )
  )

// Distribution

pgpPassphrase := Some(getEnvVar("PGP_PASSPHRASE").getOrElse("").toCharArray)
pgpPublicRing := file(s"$gpgFolder/pubring.gpg")
pgpSecretRing := file(s"$gpgFolder/secring.gpg")
