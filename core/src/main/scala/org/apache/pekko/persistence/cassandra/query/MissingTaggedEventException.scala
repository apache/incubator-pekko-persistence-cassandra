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

import java.util.UUID
import org.apache.pekko.annotation.ApiMayChange

/**
 * Events by tag query was unable all the events for some persistence ids.
 *
 * Consider restarting the query from the minOffset if downstream processing is idempotent
 * as it may re-deliver previously delivered events.
 *
 * @param tag the tag for the query
 * @param missing a map from persistence id to a set of tag pid sequence numbers that could
 *                 not be found
 * @param minOffset minimum offset was used when searching
 * @param maxOffset maximum offset used when searching
 */
@ApiMayChange
final class MissingTaggedEventException(
    val tag: String,
    val missing: Map[String, Set[Long]],
    val minOffset: UUID,
    val maxOffset: UUID)
    extends RuntimeException(s"Unable to find tagged events for tag [$tag]: $missing")
