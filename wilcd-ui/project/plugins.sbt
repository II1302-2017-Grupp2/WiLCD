// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.13")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.2.0-M8")

libraryDependencies += "org.vafer" % "jdeb" % "1.3" artifacts Artifact("jdeb", "jar", "jar")

resolvers += Resolver.bintrayIvyRepo("ii1302-2017-grupp2", "sbt-plugin-releases")

addSbtPlugin("com.typesafe.sbt" % "sbt-js-engine" % "1.2.0-g2-pre")

//addSbtPlugin("stejskal" % "sbt-webpack" % "0.4")
addSbtPlugin("com.github.stonexx.sbt" % "sbt-webpack" % "1.1.0")
