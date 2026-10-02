package org.jobrunr.examples;

import org.jobrunr.storage.StorageProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.client.RestTestClient;

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

    @LocalServerPort
    int port;

    @Autowired
    StorageProvider storageProvider;

    private RestTestClient restClient;

    @BeforeEach
    void setUp() {
        restClient = RestTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

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
        return restClient.get().uri("/enqueue-example-job?name=" + input)
                .exchange()
                .returnResult(String.class)
                .getResponseBody();
    }

    private String scheduleJobViaRest(String input, Duration duration) {
        return restClient.get().uri("/schedule-example-job?name=" + input + "&when=" + duration.toString())
                .exchange()
                .returnResult(String.class)
                .getResponseBody();
    }
}
