package org.jobrunr.examples.api;

import org.jobrunr.examples.services.SampleJobService;
import org.jobrunr.jobs.JobId;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.UUID;
import java.util.stream.IntStream;

import static java.time.Instant.now;
import static org.jobrunr.scheduling.JobBuilder.aJob;
import static org.jobrunr.scheduling.RecurringJobBuilder.aRecurringJob;

@RestController
public class JobController {

    private final JobScheduler jobScheduler;
    private final SampleJobService sampleService;

    public JobController(JobScheduler jobScheduler, SampleJobService sampleService) {
        this.jobScheduler = jobScheduler;
        this.sampleService = sampleService;
    }

    @PostConstruct
    public void createRecurringBuilderJob() {
        IntStream.range(0, 30)
                .forEach((i) ->
                        jobScheduler.createRecurrently(aRecurringJob()
                                .withId("" + i)
                                .withDetails(sampleService::recurringJob)
                                .withName("A recurring job")
                                .withCron("*/15 * * * *")
                                .withLabels("recurring", "Builder based")));
    }

    @GetMapping("/enqueue-example-job")
    public String enqueueExampleJob(@RequestParam(value = "name", defaultValue = "World") String name) {
        final JobId enqueuedJobId = jobScheduler.enqueue(() -> sampleService.annotationExecuteSampleJob("Hello " + name));
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
        final JobId scheduledJobId = jobScheduler.schedule(now().plus(Duration.parse(when)), () -> sampleService.annotationExecuteSampleJob("Hello " + name));
        return "Job Scheduled: " + scheduledJobId.toString();
    }

    @GetMapping("/create-example-job")
    public String createExampleJob(
            @RequestParam(value = "name", defaultValue = "World") String name,
            @RequestParam(value = "when", defaultValue = "PT3H") String when) {
        String bla = "hello " + name;
        final JobId createJobId = jobScheduler.create(aJob()
                .withName("The sample job with variable Hello " + name)
                .withDetails(() -> sampleService.executeSampleJob(bla))
                .scheduleIn(Duration.parse(when))
                .withLabels("Builder based"));
        return "Job created: " + createJobId.toString();
    }
}
