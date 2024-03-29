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

import org.apache.pekko
import pekko.actor.ExtendedActorSystem
import pekko.persistence.journal.{ EventAdapter, EventSeq, Tagged }

sealed trait TestEvent[T] {
  def value: T
}

class TestEventAdapter(system: ExtendedActorSystem) extends EventAdapter {

  override def manifest(event: Any): String = ""

  override def toJournal(event: Any): Any = event match {
    case e: String if e.startsWith("tagged:") =>
      val taggedEvent = e.stripPrefix("tagged:")
      val tags = taggedEvent.takeWhile(_ != ':').split(",").toSet
      val payload = taggedEvent.dropWhile(_ != ':').drop(1)
      Tagged(payload, tags)
    case e => e
  }

  override def fromJournal(event: Any, manifest: String): EventSeq = event match {
    case e: String if e.contains(":") =>
      e.split(":").toList match {
        case "dropped" :: _ :: Nil            => EventSeq.empty
        case "duplicated" :: x :: Nil         => EventSeq(x, x)
        case "prefixed" :: prefix :: x :: Nil => EventSeq.single(s"$prefix-$x")
        case _                                => throw new IllegalArgumentException(e)
      }
    case _ => EventSeq.single(event)
  }
}
