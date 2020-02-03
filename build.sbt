import ProjectPlugin.autoImport._
val scalaExercisesV = "0.6.0-SNAPSHOT"

def dep(artifactId: String) = "org.scala-exercises" %% artifactId % scalaExercisesV

lazy val doobie = (project in file("."))
  .enablePlugins(ExerciseCompilerPlugin)
  .settings(
    name         := "exercises-doobie",
    libraryDependencies ++= Seq(
      dep("exercise-compiler"),
      dep("definitions"),
      %%("doobie-core", V.doobie),
      %%("doobie-h2", V.doobie),
      %%("cats-core", V.cats),
      %%("shapeless", V.shapeless),
      %%("scalatest", V.scalatest),
      %%("scalacheck", V.scalacheck),
      "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % V.scalacheckShapeless,
      "org.scalatestplus"          %% "scalatestplus-scalacheck"  % V.scalatestplusScheck
    )
  )

// Distribution

pgpPassphrase := Some(getEnvVar("PGP_PASSPHRASE").getOrElse("").toCharArray)
pgpPublicRing := file(s"$gpgFolder/pubring.gpg")
pgpSecretRing := file(s"$gpgFolder/secring.gpg")
