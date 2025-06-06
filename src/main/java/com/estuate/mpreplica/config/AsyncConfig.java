package com.estuate.mpreplica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Values can be externalized to application.properties
        executor.setCorePoolSize(5); // Number of threads to keep in the pool
        executor.setMaxPoolSize(10); // Maximum number of threads allowed
        executor.setQueueCapacity(25); // Queue capacity for holding tasks before they are executed
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }
}

