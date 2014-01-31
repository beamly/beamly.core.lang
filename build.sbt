import com.typesafe.sbt.SbtGhPages.ghpages
import com.typesafe.sbt.SbtGit.git
import com.typesafe.sbt.SbtSite.site

name := "zeebox-core-lang"

version := "1.0-SNAPSHOT"

organization := "com.zeebox"

scalaVersion := "2.10.3"

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-reflect" % _)

site.settings

ghpages.settings

git.remoteRepo := "git@github.com:zeebox/zeebox.core.lang.git"

site.includeScaladoc()