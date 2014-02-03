import com.typesafe.sbt.SbtGhPages.ghpages
import com.typesafe.sbt.SbtGit.git
import com.typesafe.sbt.SbtSite.site

name := "zeebox-core-lang"

version := "1.0-SNAPSHOT"

organization := "com.zeebox"

scalaVersion := "2.10.3"

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-reflect" % _)

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "2.2.2" % "test"
)

site.settings

ghpages.settings

git.remoteRepo := "git@github.com:zeebox/zeebox.core.lang.git"

site.includeScaladoc()

autoAPIMappings := true

apiURL := Some(url("http://zeebox.github.io/zeebox.core.lang/latest/api/"))

javacOptions in (Compile,doc) ++= Seq("-linksource")