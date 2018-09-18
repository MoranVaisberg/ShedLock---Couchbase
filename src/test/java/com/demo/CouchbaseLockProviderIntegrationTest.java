package com.demo;

import static com.demo.CouchbaseLockProvider.*;
import static java.time.temporal.ChronoUnit.MINUTES;

import com.couchbase.client.java.Bucket;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

public class CouchbaseLockProviderIntegrationTest {

    private CouchbaseLockProvider lockProvider;
    private Bucket bucket;

    private static final String LOCK_ID = "lock_01";
    private static Cluster cluster;

    @BeforeClass
    public static void beforeClass () {
        cluster = connect();
    }

    @AfterClass
    public static void afterClass () {
        disconnect(cluster);
    }

    @Before
    public void createLockProvider()  {
        bucket = getBucket(cluster);
        lockProvider = new CouchbaseLockProvider(bucket, LOCK_ID);
    }

    private LockProvider getLockProvider() {
        return lockProvider;
    }

    @Test
    public void testUnlockedDocument() {
        JsonDocument lockDocument = bucket.get(LOCK_ID);
        Assertions.assertThat(LocalDateTime.parse((String)lockDocument.content().get(LOCK_UNTIL)).isBefore(LocalDateTime.now()));
        Assertions.assertThat(LocalDateTime.parse((String)lockDocument.content().get(LOCKED_AT)).isBefore(LocalDateTime.now()));
        Assertions.assertThat(!((String) lockDocument.content().get(LOCKED_BY)).isEmpty());
    }

    @Test
    public void testLockedDocument() {

        Optional<SimpleLock> lock = getLockProvider().lock(lockConfig(LOCK_ID));
        Assertions.assertThat(lock).isNotEmpty();

        JsonDocument lockDocument = bucket.get(LOCK_ID);
        Assertions.assertThat(LocalDateTime.parse((String) lockDocument.content().get(LOCK_UNTIL)).isAfter(LocalDateTime.now()));
        Assertions.assertThat(LocalDateTime.parse((String) lockDocument.content().get(LOCKED_AT)).isBefore(LocalDateTime.now()));
        Assertions.assertThat(!((String) lockDocument.content().get(LOCKED_BY)).isEmpty());

        lock.get().unlock();
    }

    private static LockConfiguration lockConfig(String name) {
        return lockConfig(name, Duration.of(5, MINUTES), Duration.ZERO);
    }

    private static LockConfiguration lockConfig(String name, Duration lockAtMostFor, Duration lockAtLeastFor) {
        Instant now = Instant.now();
        return new LockConfiguration(name, now.plus(lockAtMostFor), now.plus(lockAtLeastFor));
    }

    private static Cluster connect(){
        return CouchbaseCluster.create("127.0.0.1");
    }

    private static void disconnect(Cluster cluster){
        cluster.disconnect();
    }

    private Bucket getBucket(Cluster cluster) {
        cluster.authenticate("bucket_1", "bucket_1");
        Bucket bucket = cluster.openBucket("bucket_1");

        return bucket;
    }

}