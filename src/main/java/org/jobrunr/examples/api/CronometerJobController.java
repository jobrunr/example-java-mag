package org.jobrunr.examples.api;

import org.jobrunr.examples.services.SampleJobService;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.spring.autoconfigure.JobRunrProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static org.jobrunr.scheduling.JobBuilder.aJob;

@RestController
public class CronometerJobController {
    private final SampleJobService sampleService;
    private final JobScheduler jobScheduler;
    private final JobRunrProperties properties;

    public CronometerJobController(SampleJobService sampleService, JobScheduler jobScheduler, JobRunrProperties properties) {
        this.sampleService = sampleService;
        this.jobScheduler = jobScheduler;
        this.properties = properties;
    }


    @GetMapping("/enqueue-conflicting-jobs-stream")
    public String enqueueConflictingJobsStream(
            @RequestParam(value = "amount", defaultValue = "50000") int amount,
            @RequestParam(value = "percentOfIdConflicts", defaultValue = "25") int percentOfIdConflicts,
            @RequestParam(value = "distributionOfJobsAmongRateLimiters", defaultValue = "uniform") String distributionOfJobsAmongRateLimiters) {

        if (percentOfIdConflicts <= 0 || percentOfIdConflicts >= 100) {
            throw new IllegalArgumentException("percentOfIdConflicts must be between 0 and 100, excluding the bounds");
        }

        Random random = ThreadLocalRandom.current();

        NavigableMap<Double, String> rateLimitersDistribution = buildRateLimitersCumulativeDistribution(distributionOfJobsAmongRateLimiters);
        if (rateLimitersDistribution.isEmpty()) {
            throw new IllegalStateException("No rate limiters configured. Check your jobrunr.jobs.rate-limiter.* properties.");
        }

        List<UUID> existingJobIds = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            String rateLimiter = pick(rateLimitersDistribution);

            boolean reuseId = !existingJobIds.isEmpty() && random.nextInt(100) < percentOfIdConflicts;

            UUID jobId;
            if (reuseId) {
                jobId = existingJobIds.get(random.nextInt(existingJobIds.size()));
            } else {
                jobId = UUID.randomUUID();
                existingJobIds.add(jobId);
            }

            jobScheduler.create(aJob()
                .withId(jobId)
                .withJobLambda(() -> sampleService.executeSampleJob())
                .withRateLimiter(rateLimiter)
            );
        }

        return String.format("Enqueued %d jobs (%d%% conflict, distribution=%s)", amount, percentOfIdConflicts, distributionOfJobsAmongRateLimiters);
    }

    /**
     * Builds a cumulative distribution function over the configured rate limiters.
     * Keys are cumulative probabilities in (0, 1]; values are rate limiter names.
     * To sample: draw r ~ U[0, 1) and take the entry returned by higherEntry(r).
     */
    private NavigableMap<Double, String> buildRateLimitersCumulativeDistribution(String distribution) {
        Map<String, Double> capacityPerSecond = getRateLimitersCapacities();

        Map<String, Double> rawWeights = switch (distribution) {
            case "uniform" -> capacityPerSecond.keySet().stream()
                    .collect(Collectors.toMap(name -> name, name -> 1.0));
            case "byLimit" -> capacityPerSecond;
            default -> throw new IllegalArgumentException(
                    "Unknown distribution: " + distribution + " (expected 'uniform' or 'byLimit')");
        };

        double total = rawWeights.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total <= 0) return new TreeMap<>();

        NavigableMap<Double, String> cdf = new TreeMap<>();
        double cumulative = 0.0;
        for (Map.Entry<String, Double> e : rawWeights.entrySet()) {
            cumulative += e.getValue() / total;
            cdf.put(cumulative, e.getKey());
        }
        return cdf;
    }

    private Map<String, Double> getRateLimitersCapacities() {
        var rateLimiterProps = properties.getJobs().getRateLimiter();
        Map<String, Double> capacities = new LinkedHashMap<>();

        rateLimiterProps.getConcurrentJobRateLimiter().forEach((name, limit) ->
                capacities.put(name, (double) limit));

        // for sliding window rate limiters, we take the capacity per second
        rateLimiterProps.getSlidingTimeWindowRateLimiter().forEach((name, spec) -> {
            // Spec format: "amount/ISO-Duration", e.g. "3000/PT1H"
            String[] parts = spec.split("/", 2);
            int amount = Integer.parseInt(parts[0].trim());
            Duration window = Duration.parse(parts[1].trim());
            double seconds = window.toMillis() / 1000.0;
            capacities.put(name, amount / seconds);
        });

        return capacities;
    }

    private String pick(NavigableMap<Double, String> cdf) {
        double r = ThreadLocalRandom.current().nextDouble();
        return cdf.higherEntry(r).getValue();
    }
}
