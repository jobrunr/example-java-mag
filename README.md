# JobRunr example

This repository shows a simple example on how you can integrate JobRunr with [spring.io](https://spring.io/). In this example, Jobs are created via a web frontend.

A more advanced example using [spring.io](https://spring.io/) can be found [here](https://github.com/jobrunr/example-spring)

## About this project
This project has 2 packages:
- **org.jobrunr.examples.services**: this project contains [SampleJobService](src/main/java/org/jobrunr/examples/services/SampleJobService.java), a simple spring service with examples methods which you want to run in a backgroundserver.
    - Note that the method annotated with `@Recurring` will be automatically picked up by JobRunr and run every 2 minutes (specified by the `cron` parameter)
- **org.jobrunr.examples.api**: It contains a simple `RestController` called [JobController](src/main/java/org/jobrunr/examples/api/JobController.java) which contains some
  methods (= endpoints) to enqueue, schedule, or delete jobs.

## DB setup a Mongo database
```shell
docker run --name example-jobrunr-mongodb-multi-instances -p 27017:27017 -d mongo
```

Remove DB
```shell
docker rm -f example-jobrunr-mongodb-multi-instances
```

## How to run this project:
- clone the project and open it in your favorite IDE that supports gradle
- First, run the main method from the [JobServerApplication](src/main/java/org/jobrunr/examples/JobRunrApplication.java) 
  - Start a first instance with profile `main` (it'll enable the dashboard)
  - Start other instances with profile `worker` (it'll disable spring webserver)
- Open your favorite browser:
    - Navigate to the JobRunr dashboard located at http://localhost:8000/dashboard.
    - To enqueue a simple job, open a new tab and go to http://localhost:8080/enqueue-example-jobs to create by default 2000 jobs running for 1000ms.
      - The default values can be changed using the query params:
        - `amount` to change the amount of jobs to enqueue
        - `executionTime` to change the max run time of a job
        - `random` if `true` the execution time is selected at random between 1000 and 20000ms
      - See the [JobController](src/main/java/org/jobrunr/examples/api/JobController.java) for other endpoints!
    - Visit the dashboard again and see the jobs being processed!
