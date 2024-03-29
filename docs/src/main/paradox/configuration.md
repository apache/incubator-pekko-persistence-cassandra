# Configuration

Make your edits/overrides in your `application.conf`.

## Default configuration

The reference configuration file with the default values:

@@snip [reference.conf](/core/src/main/resources/reference.conf)

Journal configuration is under `pekko.persistence.cassandra.journal`.

Snapshot configuration is under `pekko.persistence.cassandra.snapshot`.

Query configuration is under `pekko.persistence.cassandra.query`.

Events by tag configuration is under `pekko.persistence.cassandra.events-by-tag` and shared
b `journal` and `query`.

The settings that shared by the `journal`, `query`, and `snapshot` parts of the plugin and are under
`pekko.persistence.cassandra`.

## Cassandra driver configuration

All Cassandra driver settings are via its @extref:[standard profile mechanism](java-driver:manual/core/configuration/).

One important setting is to configure the database driver to retry the initial connection:

`datastax-java-driver.advanced.reconnect-on-init = true`

It is not enabled automatically as it is in the driver's reference.conf and is not overridable in a profile.

If the ip addresses of your cassandra nodes might change (e.g. if you use k8s) then 

`datastax-java-driver.advanced.resolve-contact-points = false`

should also be set (resolves a dns address again when new connections are created). This also implies disabling java's dns cache with `-Dnetworkaddress.cache.ttl=0`. 


### Cassandra driver overrides

@@snip [reference.conf](/core/src/main/resources/reference.conf) { #profile }

## Contact points configuration

The Cassandra server contact points can be defined with the @extref:[Cassandra driver configuration](java-driver:manual/core/configuration/)

```
datastax-java-driver {
  basic.contact-points = ["127.0.0.1:9042"]
  basic.load-balancing-policy.local-datacenter = "datacenter1"
}
```

Alternatively, Pekko Discovery can be used for finding the Cassandra server contact points as described
in the @extref:[Pekko Connectors Cassandra documentation](pekko-connectors:cassandra.html#using-pekko-discovery).

Without any configuration it will use `localhost:9042` as default.
