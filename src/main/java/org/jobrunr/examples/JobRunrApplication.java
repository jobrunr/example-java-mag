package org.jobrunr.examples;

import org.jobrunr.examples.services.SampleJobService;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

import static org.jobrunr.scheduling.cron.Cron.every5minutes;

@SpringBootApplication
@Import(JobRunrExampleConfiguration.class)
public class JobRunrApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(JobRunrApplication.class, args);

        JobScheduler jobScheduler = applicationContext.getBean(JobScheduler.class);
        jobScheduler.<SampleJobService>scheduleRecurrently("recurring-sample-job", x -> x.executeSampleJob("Hello from recurring job"), every5minutes());
    }
}
