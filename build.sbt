import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import play.sbt.PlayImport._
import scalariform.formatter.preferences._
import xerial.sbt.Sonatype._

//*******************************
// Play settings
//*******************************

name := "play-html-compressor"

version := "2.8.0"

libraryDependencies ++= Seq(
  "com.googlecode.htmlcompressor" % "htmlcompressor" % "1.5.2",
  "rhino" % "js" % "1.7R2",
  "org.easytesting" % "fest-assert" % "1.4" % Test,
  specs2 % Test,
  javaCore % Test,
  filters % Test
)

lazy val root = (project in file(".")).enablePlugins(play.sbt.PlayWeb)

//*******************************
// Maven settings
//*******************************

sonatypeSettings

organization := "com.github.fkoehler"

description := "Google's HTML Compressor for Play Framework 2"

homepage := Some(url("https://github.com/fkoehler/play-html-compressor/"))

licenses := Seq("BSD New" -> url("https://github.com/fkoehler/play-html-compressor/blob/master/LICENSE.md"))

homepage := Some(url("https://github.com/fkoehler/play-html-compressor"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/fkoehler/play-html-compressor"),
    "scm:git@github.com:fkoehler/play-html-compressor.git"
  )
)

developers := List(
  Developer(id="akkie", name="Christian Kaps", email="info@mohiva.com", url=url("http://mohiva.com")),
  Developer(id="fkoehler", name="Fabian Köhler", email="fab@fabiankoehler.de", url=url("http://fabiankoehler.de"))
)
publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

//*******************************
// Compiler settings
//*******************************

scalaVersion := "2.13.2"

crossScalaVersions := Seq("2.13.2", "2.12.8")

scalacOptions ++= PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion.value)) {
  case Some((2, v)) if v >= 11 && v <= 12 => Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-feature", // Emit warning and location for usages of features that should be imported explicitly.
    "-unchecked", // Enable additional warnings where generated code depends on assumptions.
    "-Xfatal-warnings", // Fail the compilation if there are any warnings.
    "-Xlint", // Enable recommended additional warnings.
    "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
    "-Ywarn-dead-code", // Warn when dead code is identified.
    "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
    "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
    "-Ywarn-numeric-widen" // Warn when numerics are widened.
  )
  case Some((2, 13)) =>
    Seq(
      "-deprecation", // Emit warning and location for usages of deprecated APIs.
      "-feature", // Emit warning and location for usages of features that should be imported explicitly.
      "-unchecked", // Enable additional warnings where generated code depends on assumptions.
      "-Xfatal-warnings", // Fail the compilation if there are any warnings.
      "-Xlint", // Enable recommended additional warnings.
      "-Ywarn-dead-code", // Warn when dead code is identified.
      "-Ywarn-numeric-widen" // Warn when numerics are widened.
    )
}.toList.flatten

// Allow dead code in tests (to support using mockito).
scalacOptions in Test ~= { (options: Seq[String]) => options filterNot (_ == "-Ywarn-dead-code") }

javacOptions ++= Seq(
  "-Xlint:deprecation"
)

//*******************************
// Scalariform settings
//*******************************

scalariformAutoformat := true

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(FormatXml, false)
  .setPreference(DoubleIndentConstructorArguments, false)
  .setPreference(DanglingCloseParenthesis, Preserve)

Global / onChangedBuildSource := ReloadOnSourceChanges