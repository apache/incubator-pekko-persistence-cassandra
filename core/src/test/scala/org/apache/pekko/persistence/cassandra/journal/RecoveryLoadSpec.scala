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

package org.apache.pekko.persistence.cassandra.journal

import org.apache.pekko
import pekko.actor._
import pekko.persistence.{ PersistentActor, RecoveryCompleted, SnapshotOffer }
import pekko.persistence.cassandra.{ CassandraLifecycle, CassandraSpec }
import pekko.persistence.journal.Tagged
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

object RecoveryLoadSpec {
  val config = ConfigFactory.parseString(s"""
      pekko.loglevel = INFO
      pekko.persistence.cassandra.events-by-tag.enabled = on
      pekko.persistence.cassandra.events-by-tag.scanning-flush-interval = 2s
      pekko.persistence.cassandra.journal.replay-filter.mode = off
      pekko.persistence.cassandra.log-queries = off
    """).withFallback(CassandraLifecycle.config)

  final case class Init(numberOfEvents: Int)
  case object InitDone
  private final case class Next(remaining: Int)
  final case class Delete(seqNr: Long)
  case object GetMetrics
  final case class Metrics(
      snapshotDuration: FiniteDuration,
      replayDuration1: FiniteDuration,
      replayDuration2: FiniteDuration,
      replayedEvents: Int,
      totalDuration: FiniteDuration)

  def props(persistenceId: String, snapshotEvery: Int, tagging: Long => Set[String]): Props =
    Props(new ProcessorA(persistenceId, snapshotEvery, tagging))

  class ProcessorA(val persistenceId: String, snapshotEvery: Int, tagging: Long => Set[String])
      extends PersistentActor {
    val startTime = System.nanoTime()
    var snapshotEndTime = startTime
    var replayStartTime = startTime
    var replayEndTime = startTime
    var replayedEvents = 0

    def receiveRecover: Receive = {
      case s: SnapshotOffer =>
        snapshotEndTime = System.nanoTime()
        replayStartTime = snapshotEndTime
      case _: String =>
        replayedEvents += 1

        if (replayStartTime == snapshotEndTime)
          replayStartTime = System.nanoTime() // first event
      case RecoveryCompleted =>
        replayEndTime = System.nanoTime()
    }

    def receiveCommand: Receive = {
      case Init(numberOfEvents) =>
        self ! Next(numberOfEvents)
        context.become(init(sender()))
      case Delete(seqNr) =>
        deleteMessages(seqNr)
      case GetMetrics =>
        sender() ! Metrics(
          snapshotDuration = (snapshotEndTime - startTime).nanos,
          replayDuration1 = (replayStartTime - snapshotEndTime).nanos,
          replayDuration2 = (replayEndTime - replayStartTime).nanos,
          replayedEvents,
          totalDuration = (replayEndTime - startTime).nanos)
    }

    def init(replyTo: ActorRef): Receive = {
      case Next(remaining) =>
        if (remaining == 0)
          replyTo ! InitDone
        else {
          val tags = tagging(lastSequenceNr)
          val event =
            if (tags.isEmpty) s"event-$lastSequenceNr"
            else Tagged(s"event-$lastSequenceNr", tags)

          persist(event) { _ =>
            if (lastSequenceNr % snapshotEvery == 0) {
              saveSnapshot(s"snap-$lastSequenceNr")
            }
            self ! Next(remaining - 1)
          }
        }
    }

  }

}

class RecoveryLoadSpec extends CassandraSpec(RecoveryLoadSpec.config) {

  import RecoveryLoadSpec._

  private def printMetrics(metrics: Metrics): Unit = {
    println(s"  snapshot recovery took ${metrics.snapshotDuration.toMillis} ms")
    println(s"  replay init took ${metrics.replayDuration1.toMillis} ms")
    println(s"  replay of ${metrics.replayedEvents} events took ${metrics.replayDuration2.toMillis} ms")
    println(s"  total recovery took ${metrics.totalDuration.toMillis} ms")
  }

  "Recovery" must {

    "have some reasonable performance" in {
      val pid = "a1"
      val snapshotEvery = 1000
      val tagging: Long => Set[String] = { _ =>
        Set.empty
      }
      //      val tagging: Long => Set[String] = { seqNr =>
      //        if (seqNr % 10 == 0) Set("blue")
      //        else if (seqNr % 17 == 0) Set("blue", "green")
      //        else Set.empty
      //      }
      val prps = props(persistenceId = pid, snapshotEvery, tagging)

      val p1 = system.actorOf(prps)
      p1 ! Init(numberOfEvents = 9999)
      expectMsg(20.seconds, InitDone)
      //            p1 ! Delete(seqNr = 2000000)
      //            Thread.sleep(3000)
      system.stop(p1)

      // wait > 2 * scanning-flush-interval
      Thread.sleep(4500)

      (1 to 3).foreach { n =>
        val p2 = system.actorOf(prps)
        p2 ! GetMetrics
        val metrics = expectMsgType[Metrics](3.seconds)
        println(s"iteration #$n")
        printMetrics(metrics)
        system.stop(p2)
      }
    }
  }

}
