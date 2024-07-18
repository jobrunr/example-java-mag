package org.jobrunr.examples.api;

import org.jobrunr.examples.services.SampleJobInput;
import org.jobrunr.examples.services.SampleJobService;
import org.jobrunr.jobs.JobId;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.UUID;
import java.util.stream.Stream;

import static java.time.Instant.now;

@RestController
public class JobController {

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

    @GetMapping("/enqueue-example-jobs")
    public String enqueueExampleJobs(
            @RequestParam(value = "amount", defaultValue = "2000") int amount,
            @RequestParam(value = "executionTime", defaultValue = "1000") int executionTime,
            @RequestParam(value = "random", defaultValue = "false") boolean random
    ) {
        jobScheduler.enqueue(
                Stream.iterate(0, i-> i+1).limit(amount).map(Object::toString),
                id -> sampleService.executeSampleJob(id, executionTime, random)
        );
        return "Enqueued " + amount + " jobs with max execution time of" + executionTime + "ms";
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
}
