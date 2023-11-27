# example-java-mag
An example on how to use JobRunr for Java Magazine

# Reproduce issue https://github.com/jobrunr/jobrunr/issues/884

> docker run --name example-jobrunr -p 27017:27017 -d mongo
> 
> mvn clean install
> 
> java -jar target/example-java-mag-1.0-SNAPSHOT.jar
