package com.demo;

import com.couchbase.client.java.Bucket;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Slf4j
@Service
public class AsyncJob {
    @Async
    public void runTask (LockProvider lockProvider, LockConfiguration lockConfig) throws InterruptedException {

        Optional<SimpleLock> lock = lockProvider.lock(lockConfig);

        if (lock.isPresent()) {
            log.info("Locked");
            Thread.sleep(5000);
            lock.get().unlock();
            log.info("Unlocked");
        }

    }
}
