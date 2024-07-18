package org.jobrunr.examples.services;

import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.annotations.Recurring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class SampleJobService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    //@Recurring(id = "my-recurring-job", cron = "*/2 * * * *")
    @Job(name = "A recurring job")
    public void recurringJob() throws InterruptedException {
        logger.info("The recurring job has begun.");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            logger.error("Error while executing recurring job", e);
            throw e;
        } finally {
            logger.info("Recurring job has finished...");
        }
    }

    @Job(name = "The sample job with variable %0", retries = 2)
    public void executeSampleJob(String input) throws InterruptedException {
        logger.info("The sample job has begun. The variable you passed is {}", input);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            logger.error("Error while executing sample job", e);
            throw e;
        } finally {
            logger.info("Sample job has finished...");
        }
    }

    @Job(name = "The sample job with variable %0", retries = 2)
    public void executeSampleJob(String input, int executionTime, boolean random) throws InterruptedException {
        try {
            int actualExecutionTime = random ? ThreadLocalRandom.current().nextInt(1000,executionTime + 1) : executionTime;
            logger.info("The sample job has begun and will take {}ms. The variable you passed is {}", actualExecutionTime, input);
            Thread.sleep(actualExecutionTime);
        } catch (InterruptedException e) {
            logger.error("Error while executing sample job", e);
            throw e;
        } finally {
            logger.debug("Sample job has finished after {}ms", executionTime);
        }
    }

    @Job
    public void sampleJobWithRecordInput(SampleJobInput sampleJobInput) throws InterruptedException {
        logger.info("The sample job has begun. The variable you passed is {}", sampleJobInput.id());
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            logger.error("Error while executing sample job", e);
            throw e;
        } finally {
            logger.info("Sample job has finished...");
        }
    }
}
