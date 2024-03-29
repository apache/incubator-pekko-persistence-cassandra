/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, which was derived from Akka.
 */

/*
 * Copyright (C) 2016-2020 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.persistence.cassandra.query

import java.time.{ LocalDate, ZoneOffset }

import org.apache.pekko
import pekko.cluster.Cluster
import pekko.persistence.cassandra.CassandraSpec
import pekko.persistence.cassandra.journal.JournalSettings
import pekko.persistence.query.{ EventEnvelope, NoOffset }
import pekko.stream.testkit.scaladsl.TestSink
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

object EventsByTagPubsubSpec {
  val today = LocalDate.now(ZoneOffset.UTC)

  val config = ConfigFactory.parseString(s"""
    pekko.actor.provider = "cluster"
    pekko.actor.serialize-messages = off
    pekko.actor.serialize-creators = off
    pekko.remote.netty.tcp.port = 0
    pekko.remote.artery.canonical.port = 0
    pekko.remote.netty.tcp.hostname = "127.0.0.1"
    pekko.persistence.cassandra {
      
      query.refresh-interval = 10s

      events-by-tag {
        pubsub-notification = on
        flush-interval = 0ms
        eventual-consistency-delay = 0s
      }
    }
    """).withFallback(EventsByTagSpec.config)
}

class EventsByTagPubsubSpec extends CassandraSpec(EventsByTagPubsubSpec.config) {

  val journalSettings = new JournalSettings(system, system.settings.config.getConfig("pekko.persistence.cassandra"))

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    Cluster(system).join(Cluster(system).selfAddress)
  }

  "Cassandra query getEventsByTag when running clustered with pubsub enabled" must {
    "present new events to an ongoing getEventsByTag stream long before polling would kick in" in {
      val actor = system.actorOf(TestActor.props("EventsByTagPubsubSpec_a"))

      val blackSrc = queries.eventsByTag(tag = "black", offset = NoOffset)
      val probe = blackSrc.runWith(TestSink.probe[Any])
      probe.request(2)
      probe.expectNoMessage(300.millis)

      actor ! "a black car"
      probe.within(5.seconds) { // long before refresh-interval, which is 10s
        probe.expectNextPF { case e @ EventEnvelope(_, _, _, "a black car") => e }
      }
    }
  }
}
