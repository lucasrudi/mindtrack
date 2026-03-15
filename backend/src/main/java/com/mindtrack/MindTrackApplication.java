package com.mindtrack;

import io.sentry.spring.jakarta.EnableSentry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableSentry(dsn = "${sentry.dsn:}")
@EnableAsync
@EnableScheduling
public class MindTrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(MindTrackApplication.class, args);
    }
}
