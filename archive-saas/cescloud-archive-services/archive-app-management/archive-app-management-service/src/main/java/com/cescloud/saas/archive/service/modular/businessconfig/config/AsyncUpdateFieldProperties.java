package com.cescloud.saas.archive.service.modular.businessconfig.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 异步更新字段 到工作流
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = "cescloud.async.business.field")
public class AsyncUpdateFieldProperties {

	/**
	 * 核心线程数，默认为1
	 */
	private int corePoolSize = 1;

	/**
	 * 最大线程数，默认为Integer.MAX_VALUE
	 */
	private int maxPoolSize = 5;

	/**
	 * 队列最大长度，默认为Integer.MAX_VALUE
	 */
	private int queueCapacity = 20;

	/**
	 * true表明等待所有线程执行完，默认为false
	 */
	private boolean waitForJobsToCompleteOnShutdown = false;

	/**
	 * 线程前缀名称
	 */
	private String threadNamePrefix = "asyncUpdateField-";


	/**
	 * 线程池满，等待放入队列的时间
	 */
	//private long waitTime = 1000L;

}
