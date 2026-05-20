# JobRunr example

This repository shows a simple example on how you can integrate JobRunr with [spring.io](https://spring.io/). In this example, Jobs are created via a web frontend.

A more advanced example using [spring.io](https://spring.io/) can be found [here](https://github.com/jobrunr/example-spring)

## Cronometer performance issue

To reproduce the issue from Cronometer, we've added a `CronometerJobController`. It exposes a single endpoint to enqueue jobs in a similar way Cronometer does it:
1. All jobs are rate-limited
2. Some jobs re-use the same identifier

The endpoint can be triggered by calling: http://localhost:8080/enqueue-conflicting-jobs-stream?amount=500000&percentOfIdConflicts=25&distributionOfJobsAmongRateLimiters=uniform

Note on the different parameter:
- `amount` controls the number of jobs to create
- `percentOfIdConflicts` controls the number jobs created with an already existing id
- `distributionOfJobsAmongRateLimiters` controls how created jobs get assigned a rate-limiter. Use `uniform` to give every rate-limiter an equal chance of being picked, regardless of its limit. Use `byLimit` to weight the chance by each rate-limiter's capacity: a concurrent rate-limiter allowing 50 jobs has 50x more chance to be picked than one allowing 1. Sliding-window rate-limiters are normalized to a per-second rate first (e.g. 3000/hour ≈ 0.83/second), so they're weighted on the same scale as concurrent limiters.

The rest of the README describe how to run the project.

## About this project
This project has 2 packages:
- **org.jobrunr.examples.services**: this project contains [SampleJobService](src/main/java/org/jobrunr/examples/services/SampleJobService.java), a simple spring service with examples methods which you want to run in a backgroundserver.
    - Note that the method annotated with `@Recurring` will be automatically picked up by JobRunr and run every 2 minutes (specified by the `cron` parameter)
- **org.jobrunr.examples.api**: It contains a simple `RestController` called [JobController](src/main/java/org/jobrunr/examples/api/JobController.java) which contains some
  methods (= endpoints) to enqueue, schedule, or delete jobs.

## DB setup a Postgres database
```shell
docker run --name example-java-mag-pro-db -p 5432:5432 -e POSTGRES_PASSWORD=postgres -d postgres -c "shared_preload_libraries=pg_stat_statements"
```

Remove DB
```shell
docker rm -f example-java-mag-pro-db
```

## How to run this project:
- clone the project and open it in your favorite IDE that supports gradle
- First, run the main method from
  the [JobServerApplication](src/main/java/org/jobrunr/examples/JobRunrApplication.java)
- Open your favorite browser:
    - Navigate to the JobRunr dashboard located at http://localhost:8000/dashboard.
    - To enqueue a simple job, open a new tab and go to http://localhost:8080/enqueue-example-job to create the job.
      - See the [JobController](src/main/java/org/jobrunr/examples/api/JobController.java) for other endpoints!
    - Visit the dashboard again and see the jobs being processed!
