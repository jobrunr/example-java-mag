package org.jobrunr.examples;

import org.jobrunr.jobs.states.StateName;
import org.jobrunr.storage.StorageProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = DEFINED_PORT, classes = JobRunrApplication.class)
public class JobRunrIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    StorageProvider storageProvider;

    @Test
    public void givenEndpoint_whenJobEnqueued_thenJobIsProcessedWithin30Seconds() {
        String response = enqueueJobViaRest("from-test");
        assertEquals("Job Enqueued", response);

        await()
                .atMost(30, TimeUnit.SECONDS)
                .until(() -> storageProvider.countJobs(StateName.SUCCEEDED) == 1);
    }

    @Test
    public void givenEndpoint_whenJobScheduled_thenJobIsScheduled() {
        String response = scheduleJobViaRest("from-test", Duration.ofHours(3));
        assertEquals("Job Enqueued", response);

        await()
                .atMost(30, TimeUnit.SECONDS)
                .until(() -> storageProvider.countJobs(StateName.SCHEDULED) == 1);
    }

    private String enqueueJobViaRest(String input) {
        return restTemplate.getForObject("http://localhost:8080/enqueue-example-job?name=" + input, String.class);
    }

    private String scheduleJobViaRest(String input, Duration duration) {
        return restTemplate.getForObject("http://localhost:8080/schedule-example-job?name=" + input + "&when=" + duration.toString(), String.class);
    }
}
