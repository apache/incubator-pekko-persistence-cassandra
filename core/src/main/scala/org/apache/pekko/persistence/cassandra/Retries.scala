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

package org.apache.pekko.persistence.cassandra

import org.apache.pekko
import pekko.actor.Scheduler
import pekko.annotation.InternalApi
import pekko.pattern.{ after, BackoffSupervisor }

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration.FiniteDuration
import scala.util.control.NonFatal

/**
 * INTERNAL API
 */
@InternalApi
private[cassandra] object Retries {
  def retry[T](
      attempt: () => Future[T],
      attempts: Int,
      onFailure: (Int, Throwable, FiniteDuration) => Unit,
      minBackoff: FiniteDuration,
      maxBackoff: FiniteDuration,
      randomFactor: Double)(implicit ec: ExecutionContext, scheduler: Scheduler): Future[T] = {
    require(attempts == -1 || attempts > 0, s"Attempts must be -1 or greater than 0. Got $attempts")
    retry(attempt, attempts, onFailure, minBackoff, maxBackoff, randomFactor, 0)
  }

  private def retry[T](
      attempt: () => Future[T],
      maxAttempts: Int,
      onFailure: (Int, Throwable, FiniteDuration) => Unit,
      minBackoff: FiniteDuration,
      maxBackoff: FiniteDuration,
      randomFactor: Double,
      attempted: Int)(implicit ec: ExecutionContext, scheduler: Scheduler): Future[T] = {

    def tryAttempt(): Future[T] = {
      try {
        attempt()
      } catch {
        case NonFatal(exc) => Future.failed(exc) // in case the `attempt` function throws
      }
    }

    if (maxAttempts == -1 || maxAttempts - attempted != 1) {
      tryAttempt().recoverWith {
        case NonFatal(exc) =>
          val nextDelay = BackoffSupervisor.calculateDelay(attempted, minBackoff, maxBackoff, randomFactor)
          onFailure(attempted + 1, exc, nextDelay)
          after(nextDelay, scheduler) {
            retry(attempt, maxAttempts, onFailure, minBackoff, maxBackoff, randomFactor, attempted + 1)
          }
      }
    } else {
      tryAttempt()
    }
  }
}
