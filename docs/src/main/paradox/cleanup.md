# Database Cleanup

@@@ warning

When running any operation for a persistence id the actor with that persistence id must not be running!

@@@

If possible, it is best to keep all events in an event sourced system. That way new [projections](https://pekko.apache.org/docs/pekko-projection/current/index.html) 
and the `tag_view` table can be re-built if it is corrupted (e.g. due to a two persistence ids writing events from two nodes in a split brain).

In some cases keeping all events is not possible. `EventSourcedBehavior`s can automatically snapshot state and delete events as described in the [Pekko docs](https://pekko.apache.org/docs/pekko/current/typed/persistence-snapshot.html#snapshot-deletion).
Snapshotting is useful even if events aren't deleted as it speeds up recovery.

The @apidoc[org.apache.pekko.persistence.cassandra.cleanup.Cleanup] tool can retrospectively clean up the journal. Its operations include:

* Delete all events for a persistence id
* Delete all events and tagged events for the `eventsByTag` query
* Delete all snapshots for a persistence id
* Delete all snapshots and events for a persistence id keeping the latest N snapshots and all the events after them. 

The cleanup tool can be combined with the @ref[query plugin](./read-journal.md) which has a query to get all persistence ids.


Scala
: @@snip [snapshot-keyspace](/docs/src/test/scala/doc/cleanup/CleanupDocExample.scala) { #cleanup } 

Java
: @@snip [snapshot-keyspace](/docs/src/test/java/jdoc/cleanup/CleanupDocExample.java) { #cleanup } 

By default, all operations only print what they were going to do. Once you're happy with what the cleanup tool is going to do set `pekko.persistence.cassandra.cleanup.dry-run = false`
