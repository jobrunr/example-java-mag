package org.jobrunr.examples;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sqlite.SQLiteDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class JobRunrExampleConfiguration {

    @Bean
    public SQLiteDataSource dataSource() throws IOException {
        final Path path = Paths.get(System.getProperty("java.io.tmpdir"), "jobrunr-example.db");
        Files.delete(path);
        final SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + path);
        return dataSource;
    }
}
