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

package org.apache.pekko.persistence.cassandra.compaction

import com.typesafe.config.Config

/*
 * https://github.com/apache/cassandra/blob/cassandra-2.2/src/java/org/apache/cassandra/db/compaction/LeveledCompactionStrategy.java
 */
class LeveledCompactionStrategy(config: Config)
    extends BaseCompactionStrategy(config, LeveledCompactionStrategy.ClassName,
      LeveledCompactionStrategy.propertyKeys) {
  val ssTableSizeInMB: Long =
    if (config.hasPath("sstable_size_in_mb"))
      config.getLong("sstable_size_in_mb")
    else 160

  require(ssTableSizeInMB > 0, s"sstable_size_in_mb must be larger than 0, but was $ssTableSizeInMB")

  override def asCQL: String =
    s"""{
       |'class' : '${LeveledCompactionStrategy.ClassName}',
       |${super.asCQL},
       |'sstable_size_in_mb' : $ssTableSizeInMB
       |}
     """.stripMargin.trim
}

object LeveledCompactionStrategy extends CassandraCompactionStrategyConfig[LeveledCompactionStrategy] {
  override val ClassName: String = "LeveledCompactionStrategy"

  override def propertyKeys: List[String] =
    (BaseCompactionStrategy.propertyKeys ++ List("sstable_size_in_mb")).sorted

  override def fromConfig(config: Config): LeveledCompactionStrategy =
    new LeveledCompactionStrategy(config)
}
