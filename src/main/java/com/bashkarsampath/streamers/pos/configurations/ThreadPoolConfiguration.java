package com.bashkarsampath.streamers.pos.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class ThreadPoolConfiguration {

	@Getter(value = AccessLevel.PUBLIC)
	@Setter(value = AccessLevel.PRIVATE)
	private static int corePoolSize;
	@Getter(value = AccessLevel.PUBLIC)
	@Setter(value = AccessLevel.PRIVATE)
	private static int maxPoolSize;
	@Getter(value = AccessLevel.PUBLIC)
	@Setter(value = AccessLevel.PRIVATE)
	private static int queueCapacity;

	@Autowired
	public void injectParallelismCapacity(@Value("${parallelism.corepoolsize}") int corePoolSize,
			@Value("${parallelism.maxpoolsize}") int maxPoolSize,
			@Value("${parallelism.queueCapacity}") int queueCapacity) {
		setCorePoolSize(corePoolSize);
		setMaxPoolSize(maxPoolSize);
		setQueueCapacity(queueCapacity);
	}

	@Bean(name = "processExecutor")
	public TaskExecutor workExecutor(@Value("${parallelism.corepoolsize}") int corePoolSize,
			@Value("${parallelism.maxpoolsize}") int maxPoolSize,
			@Value("${parallelism.queueCapacity}") int queueCapacity) {
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setThreadNamePrefix("Async-");
		threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
		threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
		threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
		threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
		threadPoolTaskExecutor.setAwaitTerminationSeconds(2);
		threadPoolTaskExecutor.afterPropertiesSet();
		log.info("ThreadPoolTaskExecutor set");
		return threadPoolTaskExecutor;
	}
}