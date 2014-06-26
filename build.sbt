import com.typesafe.sbt.SbtGhPages.ghpages
import com.typesafe.sbt.SbtGit.git
import com.typesafe.sbt.SbtSite.site
import sbtrelease._
import sbtrelease.ReleasePlugin.ReleaseKeys._
import sbtrelease.ReleaseStateTransformations._

name := "beamly-core-lang"

organization := "com.beamly"

scalaVersion := "2.11.1"

crossScalaVersions := Seq("2.11.1", "2.10.4")

scalacOptions := Seq("-optimize", "-deprecation", "-unchecked", "-encoding", "utf8", "-Yinline-warnings", "-target:jvm-1.6", "-feature", "-Xlint", "-Ywarn-value-discard")

fork in Test := true

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

credentials += Credentials(Path.userHome / ".sbt" / ".zeebox_credentials")

publishTo <<= version apply { (v: String) =>
  if (v endsWith "SNAPSHOT") {
    Some("zeebox-nexus-snapshots" at "http://nexus.zeebox.com:8080/nexus/content/repositories/snapshots")
  } else {
    Some("zeebox-nexus" at "http://nexus.zeebox.com:8080/nexus/content/repositories/releases")
  }
}

aetherSettings

releaseSettings

releaseVersion := { ver => Version(ver).map(v => v.copy(bugfix = v.bugfix orElse Some(0)).withoutQualifier.string).getOrElse(versionFormatError) }

nextVersion    := { ver => Version(ver).map(_.bumpMinor.copy(bugfix = None).asSnapshot.string).getOrElse(versionFormatError) }

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  pushChanges
)

site.settings

ghpages.settings

git.remoteRepo := "git@github.com:zeebox/zeebox.core.lang.git"

site.includeScaladoc()

autoAPIMappings := true

apiURL := Some(url("http://zeebox.github.io/beamly.core.lang/latest/api/"))

javacOptions in (Compile,doc) ++= Seq("-linksource")
