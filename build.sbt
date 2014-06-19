import com.typesafe.sbt.SbtGhPages.ghpages
import com.typesafe.sbt.SbtGit.git
import com.typesafe.sbt.SbtSite.site

name := "beamly-core-lang"

version := "1.0-SNAPSHOT"

organization := "com.beamly"

scalaVersion := "2.11.1"

crossScalaVersions := Seq("2.11.1", "2.10.4")

libraryDependencies <++= scalaVersion { sv =>
  Seq(
    "org.specs2" %% "specs2" % "2.3.11" % "test",
    "org.scala-lang" % "scala-reflect" % sv,
    compilerPlugin("org.scalamacros" %% "paradise" % "2.0.0" cross CrossVersion.full)
  ) ++ (CrossVersion partialVersion sv collect {
    case (2, 10) => "org.scalamacros" %% "quasiquotes" % "2.0.0"
  })
}

incOptions := CrossVersion partialVersion scalaVersion.value collect {
  case (2, scalaMajor) if scalaMajor >= 11 => incOptions.value withNameHashing true
} getOrElse incOptions.value // name hashing causes StackOverflowError under sbt 0.13.5 & scala 2.10.4, see sbt#1237 & SI-8486

site.settings

ghpages.settings

git.remoteRepo := "git@github.com:zeebox/zeebox.core.lang.git"

site.includeScaladoc()

autoAPIMappings := true

apiURL := Some(url("http://zeebox.github.io/beamly.core.lang/latest/api/"))

javacOptions in (Compile,doc) ++= Seq("-linksource")

