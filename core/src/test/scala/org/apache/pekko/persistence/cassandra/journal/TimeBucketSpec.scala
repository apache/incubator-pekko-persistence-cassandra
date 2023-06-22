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
import pekko.persistence.cassandra.Day
import pekko.persistence.cassandra.Hour
import pekko.persistence.cassandra.Minute
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class TimeBucketSpec extends AnyWordSpec with Matchers {
  "TimeBucket sizes" must {
    "support day" in {
      val epochTime = 1409545135047L
      val day = TimeBucket(epochTime, Day)
      day.key should equal(1409529600000L)
      day.inPast should equal(true)
      day.isCurrent should equal(false)
      day.key should equal(1409529600000L)
      day.next().key should equal(1409616000000L)
      day.next() > day should equal(true)
      day < day.next() should equal(true)
    }

    "support hour" in {
      val epochTime = 1409545135047L
      val hour = TimeBucket(epochTime, Hour)
      hour.key should equal(1409544000000L)
      hour.inPast should equal(true)
      hour.isCurrent should equal(false)
      hour.key should equal(1409544000000L)
      hour.next().key should equal(1409547600000L)
      hour.next() > hour should equal(true)
      hour < hour.next() should equal(true)
    }

    "support minute" in {
      val epochTime = 1409545135047L
      val minute = TimeBucket(epochTime, Minute)
      minute.key should equal(1409545080000L)
      minute.inPast should equal(true)
      minute.isCurrent should equal(false)
      minute.key should equal(1409545080000L)
      minute.next().key should equal(1409545140000L)
      minute.next() > minute should equal(true)
      minute < minute.next() should equal(true)
    }
  }
}
