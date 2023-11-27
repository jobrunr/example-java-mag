package org.jobrunr.examples.configuration;

import com.mongodb.client.MongoClient;
import org.jobrunr.jobs.mappers.JobMapper;
import org.jobrunr.storage.StorageProvider;
import org.jobrunr.storage.nosql.mongo.MongoDBStorageProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobRunrStorageConfig {
    @Bean
    public StorageProvider dataSource(JobMapper jobMapper, MongoClient mongoClient) {
        var storageProvider = new MongoDBStorageProvider(mongoClient, "example-jobrunr");
        storageProvider.setJobMapper(jobMapper);
        return storageProvider;
    }
}
