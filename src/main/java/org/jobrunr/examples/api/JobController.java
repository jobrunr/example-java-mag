package org.jobrunr.examples.api;

import org.jobrunr.examples.services.SampleJobInput;
import org.jobrunr.examples.services.SampleJobService;
import org.jobrunr.jobs.JobId;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.time.Instant.now;
import static org.jobrunr.scheduling.JobBuilder.aJob;

@RestController
public class JobController {

    private static String HIGH_PRIO_QUEUE = "HighPrio";

    private final JobScheduler jobScheduler;
    private final SampleJobService sampleService;

    public JobController(JobScheduler jobScheduler, SampleJobService sampleService) {
        this.jobScheduler = jobScheduler;
        this.sampleService = sampleService;
    }

    @GetMapping("/enqueue-example-job")
    public String enqueueExampleJob(@RequestParam(value = "name", defaultValue = "World") String name) {
        final JobId enqueuedJobId = jobScheduler.enqueue(() -> sampleService.executeSampleJob("Hello " + name));
        return "Job Enqueued: " + enqueuedJobId.toString();
    }

    @GetMapping("/enqueue-example-job-with-record")
    public String enqueueExampleJobWithRecord(@RequestParam(value = "name", defaultValue = "World") String name) {
        SampleJobInput sampleJobInput = new SampleJobInput(UUID.randomUUID(), name);
        final JobId enqueuedJobId = jobScheduler.enqueue(() -> sampleService.sampleJobWithRecordInput(sampleJobInput));
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

    @GetMapping("/enqueue-rate-limited-jobs")
    public String enqueueRateLimitedJobs(@RequestParam(value = "name", defaultValue = "World") String name) {
        int totalAmount = 1000;
        Stream<Integer> items = IntStream.range(0, totalAmount).boxed();
        jobScheduler.enqueue(items, x -> sampleService.executeRateLimitedJob("Hello " + name));
        return "Enqueued " + totalAmount + " rate limited job";
    }

    @GetMapping("/enqueue-job-on-dynamic-queue")
    public String enqueueDynamicQueueJob(@RequestParam(value = "tenant") String tenant) {
        final JobId enqueuedJobId = jobScheduler.create(aJob()
                .withLabels("tenant: " + tenant)
                .withQueue(HIGH_PRIO_QUEUE)
                .withDeleteOnSuccess(Duration.ofHours(2), Duration.ofMinutes(5))
                .withDetails(x -> sampleService.executeJobForTenant(tenant)));
        return "Job Enqueued for tenant '" + tenant + "': " + enqueuedJobId.toString();
    }
}
