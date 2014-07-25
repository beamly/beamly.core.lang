resolvers += "jgit-repo" at "http://download.eclipse.org/jgit/maven"

addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.5.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.3")

addSbtPlugin("no.arktekk.sbt" % "aether-deploy" % "0.10")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.7.1")
