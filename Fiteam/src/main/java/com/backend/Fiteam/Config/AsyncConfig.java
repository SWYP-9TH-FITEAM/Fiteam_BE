package com.backend.Fiteam.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);   // 기본 실행 스레드 수
        executor.setMaxPoolSize(8);    // 최대 실행 스레드 수
        executor.setQueueCapacity(100); // 대기열 크기
        executor.setThreadNamePrefix("asyncExecutor");
        executor.initialize();
        return executor;
    }
}
