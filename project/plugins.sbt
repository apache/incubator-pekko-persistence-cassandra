/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, which was derived from Akka.
 */

addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16")

addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.9.0")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.0")
addSbtPlugin("com.lightbend.sbt" % "sbt-java-formatter" % "0.7.0")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.10")
addSbtPlugin("com.typesafe.sbt" % "sbt-multi-jvm" % "0.4.0")
addSbtPlugin("com.dwijnand" % "sbt-dynver" % "4.1.1")
addSbtPlugin("org.mdedetrich" % "sbt-apache-sonatype" % "0.1.10")
addSbtPlugin("com.github.pjfanning" % "sbt-source-dist" % "0.1.11")
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.1.3")

// Documentation
addSbtPlugin("com.github.sbt" % "sbt-unidoc" % "0.5.0")
addSbtPlugin(("com.github.sbt" % "sbt-site-paradox" % "1.5.0").excludeAll(
  "com.lightbend.paradox", "sbt-paradox"))

resolvers += Resolver.ApacheMavenSnapshotsRepo
addSbtPlugin("org.apache.pekko" % "pekko-sbt-paradox" % "1.0.1-RC1+5-13892678-SNAPSHOT")
