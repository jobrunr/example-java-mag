package org.jobrunr.examples.api;

import org.jobrunr.examples.services.PreciselyService;
import org.jobrunr.examples.services.SampleJobService;
import org.jobrunr.jobs.JobId;
//import org.jobrunr.scheduling.JobBuilder;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.scheduling.cron.Cron;
import org.jobrunr.storage.StorageProvider;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.HOURS;
//import static org.jobrunr.scheduling.JobBuilder.aJob;
//import static org.jobrunr.scheduling.RecurringJobBuilder.aRecurringJob;

@RestController
public class JobController {

    private final JobScheduler jobScheduler;
    private final SampleJobService sampleService;
    private final PreciselyService preciselyService;

    private final StorageProvider storageProvider;

    public JobController(StorageProvider storageProvider, JobScheduler jobScheduler, SampleJobService sampleService, PreciselyService preciselyService) {
        this.storageProvider = storageProvider;
        this.jobScheduler = jobScheduler;
        this.sampleService = sampleService;
        this.preciselyService = preciselyService;
    }

    @GetMapping("/enqueue-example-job")
    public String enqueueExampleJob(@RequestParam(value = "name", defaultValue = "World") String name) {
        final JobId enqueuedJobId = jobScheduler.enqueue(() -> sampleService.executeSampleJob("Hello " + name));
        return "Job Enqueued: " + enqueuedJobId.toString();
    }

    @GetMapping("/delete-job")
    public String deleteExampleJob(@RequestParam(value = "id") String jobId) {
        jobScheduler.delete(UUID.fromString(jobId));
        return "Job deleted: " + jobId;
    }

    @GetMapping("/schedule-example-job")
    public String scheduleExampleJob(
            @RequestParam(value = "name", defaultValue = "World") String name,
            @RequestParam(value = "when", defaultValue = "PT3H") String when) {
        final JobId scheduledJobId = jobScheduler.schedule(now().plus(Duration.parse(when)), () -> sampleService.executeSampleJob("Hello " + name));
        return "Job Scheduled: " + scheduledJobId.toString();
    }

    @GetMapping("/execute-once")
    public String executeOnce() {
        UUID randomId = UUID.randomUUID();
        final JobId enqueuedJob = jobScheduler.enqueue(() -> preciselyService.executeOnceOrImmediate(randomId.toString(), Instant.now()));
        return "Job Enqueued: " + enqueuedJob.toString();
    }

    @GetMapping("/execute-once-long")
    public String executeOnceLong() {
        final JobId enqueuedJob = jobScheduler.enqueue(() -> preciselyService.doSlowWork());
        return "Job Enqueued: " + enqueuedJob.toString();
    }

//    @GetMapping("/execute-every-15-sec")
//    public String executeHourly(@RequestParam(value = "minute", defaultValue = "55") String minute) {
//        return jobScheduler.createRecurrently(aRecurringJob()
//                .withId("my-precise-id")
//                .withCron(Cron.every15seconds())
//                .withDetails(preciselyService::doSlowWork)
//                //.withMaxConcurrentJobs(4)
//                .withAmountOfRetries(3)
//        );
//    }

//    @GetMapping("/a-lot-recurring-jobs")
//    public String createALotOfRecurringJobs() {
//        System.out.println("Creating hourly jobs precise");
//        for(int i = 0; i < 5000; i++) {
//            jobScheduler.createRecurrently(aRecurringJob()
//                    .withId("my-precise-id-" + i)
//                    .withName("my-precise-job-" + i)
//                    .withCron(Cron.hourly(30))
//                    .withDetails(() -> System.out.println("This is a precise test"))
//                    //.withScheduleJobsSkippedDuringDowntime()
//                    .withAmountOfRetries(3)
//            );
//            if(i % 1000 == 0) {
//                System.out.println("Created " + i + " recurring jobs with name starting with my-precise-job");
//            }
//        }
//
//        System.out.println("Creating hourly jobs ronald");
//        for(int i = 0; i < 10000; i++) {
//            jobScheduler.createRecurrently(aRecurringJob()
//                    .withId("my-ronald-id-" + i)
//                    .withName("my-ronald-job-" + i)
//                    .withCron(Cron.hourly())
//                    .withDetails(() -> System.out.println("This is a precise test"))
//                    .withAmountOfRetries(3)
//            );
//            if(i % 1000 == 0) {
//                System.out.println("Created " + i + " recurring jobs with name starting with my-ronald-job");
//            }
//        }
//
//        System.out.println("Creating really frequent recurring jobs");
//        for(int i = 0; i < 10000; i++) {
//            jobScheduler.createRecurrently(aRecurringJob()
//                    .withId("my-fast-id-" + i)
//                    .withName("my really frequent recurring job" + i)
//                    .withCron(Cron.every30seconds())
//                    .withDetails(() -> System.out.println("This is a precise test"))
//                    .withAmountOfRetries(3)
//                    //.withDeleteOnSuccess(Duration.ZERO, Duration.ofSeconds(30))
//            );
//            if(i % 1000 == 0) {
//                System.out.println("Created " + i + " recurring jobs with name my really frequent recurring job");
//            }
//        }
//        return "done";
//    }

    @GetMapping("/execute-daily")
    public String executeDaily() {
        final JobId enqueuedJob = jobScheduler.enqueue(() -> preciselyService.executeDaily(UUID.randomUUID().toString()));
        return "Job Enqueued: " + enqueuedJob.toString();
    }

    @GetMapping("/execute-weekly")
    public String executeWeekly() {
        final JobId enqueuedJob = jobScheduler.enqueue(() -> preciselyService.executeWeeklyAndCron(UUID.randomUUID().toString()));
        return "Job Enqueued: " + enqueuedJob.toString();
    }

    @GetMapping("/create-recurring/{id}/{minute}")
    public String createRecurring(@PathVariable String id, @PathVariable int minute) {
        final String createdRecurringJob = jobScheduler.scheduleRecurrently("my-job-" + id, Cron.hourly(minute), () -> System.out.println("My job " + id));
        return "RecurringJob Created: " + createdRecurringJob.toString();
    }

    @GetMapping("/a-lot-of-jobs")
    public String createAlotoFjobs(@RequestParam(value = "minute", defaultValue = "55") String minute) {
        Instant startTime = Instant.now();
        for(int i = 0; i < 1000; i++ ) {
            Stream<UUID> workStream = IntStream.range(0, 1000)
                    .mapToObj(j -> UUID.randomUUID());
            jobScheduler.enqueue(workStream, uuid -> preciselyService.doFastWork(uuid, startTime));
            System.out.println("==============================================================================");
            System.out.println(Duration.between(startTime, Instant.now()) + " - " + Instant.now() + " - saved " + (i * 1000) + " jobs.");
            System.out.println("==============================================================================");
        }
        return "Created 1000000 jobs";
    }
}
