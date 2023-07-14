package org.jobrunr.examples;

import org.jobrunr.storage.StorageProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.jobrunr.jobs.states.StateName.SCHEDULED;
import static org.jobrunr.jobs.states.StateName.SUCCEEDED;
import static org.jobrunr.utils.StringUtils.substringAfter;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class JobRunrIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    StorageProvider storageProvider;

    @Test
    public void givenEndpoint_whenJobEnqueued_thenJobIsProcessedWithin30Seconds() {
        String responseBody = enqueueJobViaRest("from-test");
        assertThat(responseBody).startsWith("Job Enqueued");

        final UUID enqueuedJobId = UUID.fromString(substringAfter(responseBody, ": "));
        await()
                .atMost(30, TimeUnit.SECONDS)
                .until(() -> storageProvider.getJobById(enqueuedJobId).hasState(SUCCEEDED));
    }

    @Test
    public void givenEndpoint_whenJobScheduled_thenJobIsScheduled() {
        String responseBody = scheduleJobViaRest("from-test", Duration.ofHours(3));
        assertThat(responseBody).startsWith("Job Scheduled");

        final UUID scheduledJobId = UUID.fromString(substringAfter(responseBody, ": "));
        await()
                .atMost(30, TimeUnit.SECONDS)
                .until(() -> storageProvider.getJobById(scheduledJobId).hasState(SCHEDULED));
    }

    private String enqueueJobViaRest(String input) {
        return restTemplate.getForObject("/enqueue-example-job?name=" + input, String.class);
    }

    private String scheduleJobViaRest(String input, Duration duration) {
        return restTemplate.getForObject("/schedule-example-job?name=" + input + "&when=" + duration.toString(), String.class);
    }
}
