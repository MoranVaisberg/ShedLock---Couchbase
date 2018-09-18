package com.demo;

import com.demo.entity.LockRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.spring.ScheduledLockConfiguration;
import net.javacrumbs.shedlock.spring.ScheduledLockConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.Instant;


@Slf4j
@Configuration
@AllArgsConstructor
public class ScheduleConfig {

    private LockRepository lockRepository;
    private AsyncJob asyncJob;

    private static final String LOCK_ID = "lock_01";
    private static long FIVE_MINUTES = 5;

    @Scheduled(cron = "0 */1 * * * *") // every 1 min
    public void performSomeTask () throws InterruptedException {
        log.info("task-started");
        log.info("Lock Object: {}", lockRepository.findById(LOCK_ID));
        asyncJob.runTask(lockProvider(),lockConfig(LOCK_ID));
        log.info("task-finished");
    }

    @Bean
    public ScheduledLockConfiguration taskScheduler(LockProvider lockProvider) {
        return ScheduledLockConfigurationBuilder
                .withLockProvider(lockProvider)
                .withPoolSize(10)
                .withDefaultLockAtMostFor(Duration.ofMinutes(FIVE_MINUTES))
                .build();
    }


    @Bean
    public LockProvider lockProvider() {
        return new CouchbaseLockProvider(lockRepository.getCouchbaseOperations().getCouchbaseBucket(), LOCK_ID);
    }

    private static LockConfiguration lockConfig(String name) {
        return lockConfig(name, Duration.ofMinutes(FIVE_MINUTES), Duration.ZERO);
    }

    private static LockConfiguration lockConfig(String name, Duration lockAtMostFor, Duration lockAtLeastFor) {
        Instant now = Instant.now();
        return new LockConfiguration(name, now.plus(lockAtMostFor), now.plus(lockAtLeastFor));
    }

}
