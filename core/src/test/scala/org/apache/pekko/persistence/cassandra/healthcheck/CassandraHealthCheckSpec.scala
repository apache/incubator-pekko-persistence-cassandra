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

package org.apache.pekko.persistence.cassandra.healthcheck

import org.apache.pekko.persistence.cassandra.{ CassandraLifecycle, CassandraSpec }

class CassandraHealthCheckDefaultQuerySpec extends CassandraSpec with CassandraLifecycle {

  "CassandraHealthCheck" must {
    "reply with successful health check result when plugin uses default query" in {
      val healthCheckResult = new CassandraHealthCheck(system)()
      healthCheckResult.futureValue shouldBe true
    }
  }
}

class CassandraHealthCheckCustomQueryNonEmptyResultSpec extends CassandraSpec(s"""
       pekko.persistence.cassandra.healthcheck.health-check-cql="SELECT * FROM system.peers"
    """) with CassandraLifecycle {

  override def beforeAll(): Unit = {
    super.beforeAll()
    cluster.execute("INSERT INTO system.peers(peer, data_center) VALUES ('10.0.0.1', 'cassandra-dc')")
    cluster.execute("INSERT INTO system.peers(peer, data_center) VALUES ('10.0.0.2', 'cassandra-dc')")
  }

  override def afterAll(): Unit = {
    cluster.execute("DELETE FROM system.peers WHERE peer = '10.0.0.1'")
    cluster.execute("DELETE FROM system.peers WHERE peer = '10.0.0.2'")
    super.afterAll()
  }

  "CassandraHealthCheck" must {
    "reply with successful health check result when plugin executes custom query and result is non-empty" in {
      val healthCheckResult = new CassandraHealthCheck(system)()
      healthCheckResult.futureValue shouldBe true
    }
  }
}

class CassandraHealthCheckCustomQueryEmptyResultSpec extends CassandraSpec(s"""
       pekko.persistence.cassandra.healthcheck.health-check-cql="SELECT * FROM system.peers"
    """) with CassandraLifecycle {

  override def beforeAll(): Unit = {
    super.beforeAll()
    cluster.execute("TRUNCATE system.peers")
  }

  "CassandraHealthCheck" must {
    "reply with successful health check result when plugin executes custom query and result is empty" in {
      val healthCheckResult = new CassandraHealthCheck(system)()
      healthCheckResult.futureValue shouldBe true
    }
  }
}

class CassandraHealthCheckCustomFailingQuerySpec extends CassandraSpec(s"""
       pekko.persistence.cassandra.healthcheck.health-check-cql="SELECT * FROM non_existing_keyspace.non_existing_table"
    """) with CassandraLifecycle {

  "CassandraHealthCheck" must {
    "reply with failed health check result when plugin executes custom query and it fails" in {
      val healthCheckResult = new CassandraHealthCheck(system)()
      healthCheckResult.futureValue shouldBe false
    }
  }
}
