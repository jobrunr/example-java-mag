package org.jobrunr.examples.services;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PreciselyService {

    private static final AtomicLong atomicLong = new AtomicLong();
    public static final String JOB_SEPARATOR = "!";
    public static final String ONCE_JOB_SOFT_DELETE_ON_SUCCESS = "PT10M";
    public static final String JOB_HARD_DELETE = "PT5M";
    public static final String HOURLY_JOB_SOFT_DELETE_ON_SUCCESS = "PT11H";
    public static final String DAILY_JOB_SOFT_DELETE_ON_SUCCESS = "PT11D";
    public static final String WEEKLY_JOB_SOFT_DELETE_ON_SUCCESS = "PT77D";


//    @Job(deleteOnSuccess = ONCE_JOB_SOFT_DELETE_ON_SUCCESS + JOB_SEPARATOR + JOB_HARD_DELETE,
//            deleteOnFailure = ONCE_JOB_SOFT_DELETE_ON_SUCCESS + JOB_SEPARATOR + JOB_HARD_DELETE)
    public void executeOnceOrImmediate(String id, Instant now) throws ExecutionException, InterruptedException {
        execute(id);
        System.out.println("Test id: " + id + " given instant = " + now);
    }

//    @Job(deleteOnSuccess = HOURLY_JOB_SOFT_DELETE_ON_SUCCESS + JOB_SEPARATOR + JOB_HARD_DELETE,
//            deleteOnFailure = HOURLY_JOB_SOFT_DELETE_ON_SUCCESS + JOB_SEPARATOR + JOB_HARD_DELETE)
    public void executeHourly(String id) throws ExecutionException, InterruptedException {
        execute(id);
    }

//    @Job(deleteOnSuccess = DAILY_JOB_SOFT_DELETE_ON_SUCCESS + JOB_SEPARATOR + JOB_HARD_DELETE,
//            deleteOnFailure = DAILY_JOB_SOFT_DELETE_ON_SUCCESS + JOB_SEPARATOR + JOB_HARD_DELETE)
    public void executeDaily(String id) throws ExecutionException, InterruptedException {
        execute(id);
    }

//    @Job(deleteOnSuccess = WEEKLY_JOB_SOFT_DELETE_ON_SUCCESS + JOB_SEPARATOR + JOB_HARD_DELETE,
//            deleteOnFailure = WEEKLY_JOB_SOFT_DELETE_ON_SUCCESS + JOB_SEPARATOR + JOB_HARD_DELETE)
    public void executeWeeklyAndCron(String id) throws ExecutionException, InterruptedException {
        execute(id);
    }

    void execute(String id) {
        System.out.println("Test id: " + id);
    }

    public void doFastWork(UUID id, Instant startTime) throws InterruptedException {
        System.out.println(atomicLong.incrementAndGet() + " (after " + Duration.between(startTime, Instant.now()) + ") - this is test for id " + id);
    }

    public void doSlowWork() throws InterruptedException {
        System.out.println("Starting work");
        Thread.sleep(10000);
        System.out.println("Part 1 finished");
        Thread.sleep(10000);
        System.out.println("Part 2 finished");
        Thread.sleep(10000);
        System.out.println("Part 3 finished");
        Thread.sleep(10000);
        System.out.println("Part 4 finished");
        Thread.sleep(10000);
        System.out.println("Part 5 finished");
        Thread.sleep(10000);
        System.out.println("Part 6 finished");
        Thread.sleep(10000);
        System.out.println("Work is done");
    }
}
