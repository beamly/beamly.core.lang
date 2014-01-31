
name := "zeebox-core-lang"

version := "1.0-SNAPSHOT"

organization := "com.zeebox"

scalaVersion := "2.10.3"

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-reflect" % _)
