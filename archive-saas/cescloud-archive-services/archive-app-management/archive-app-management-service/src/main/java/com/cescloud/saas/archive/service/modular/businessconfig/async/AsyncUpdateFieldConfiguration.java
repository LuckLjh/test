
package com.cescloud.saas.archive.service.modular.businessconfig.async;

import com.cescloud.saas.archive.service.modular.businessconfig.config.AsyncUpdateFieldProperties;
import com.cescloud.saas.archive.service.modular.common.core.constant.enums.TaskPolicy;
import com.cescloud.saas.archive.service.modular.common.core.threadpool.CesCloudThreadPoolTaskExecutor;
import com.cescloud.saas.archive.service.modular.common.data.async.ContextCopyingDecorator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.Resource;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步 更新字段到工作流
 * @author zhangxuehu
 */
@Configuration
@EnableAsync
@EnableConfigurationProperties({AsyncUpdateFieldProperties.class})
@Slf4j
@SuppressWarnings(value = "Duplicates")
public class AsyncUpdateFieldConfiguration {
	@Resource
	private AsyncUpdateFieldProperties properties;

	public static final String ASYNC_EXECUTOR_NAME = "updateFieldAsyncExecutor";

	@Bean(name = ASYNC_EXECUTOR_NAME)
	public Executor pushFileAsyncExecutor() {
		CesCloudThreadPoolTaskExecutor executor = new CesCloudThreadPoolTaskExecutor();
		//设置CPU密集型任务
		executor.setPolicy(TaskPolicy.CPU);
		//设置显示线程池信息
		executor.setShowThreadPoolInfo(Boolean.FALSE);
		//是否预热核心线程
		executor.setAllowPreInitCore(Boolean.FALSE);
		//拷贝线程上下文
		executor.setTaskDecorator(new ContextCopyingDecorator());
		executor.setCorePoolSize(properties.getCorePoolSize());
		executor.setMaxPoolSize(properties.getMaxPoolSize());
		executor.setQueueCapacity(properties.getQueueCapacity());
		executor.setWaitForTasksToCompleteOnShutdown(properties.isWaitForJobsToCompleteOnShutdown());
		executor.setThreadNamePrefix(properties.getThreadNamePrefix());
		/**
		 * 当pool中已经达到max size时,如何处理新任务。
		 * AbortPolicy:直接抛出拒绝策略
		 */
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
		/*
        备用线程池满时执行策略
		executor.setRejectedExecutionHandler((Runnable r,ThreadPoolExecutor taskExecutor) ->{
			if(!taskExecutor.isShutdown()){
				try {
					Thread.sleep(properties.getWaitTime());
					taskExecutor.getQueue().put(r);
				}catch (InterruptedException e){
                    log.error(e.toString(),e);
                    Thread.currentThread().interrupt();
				}
			}
		});
		*/

		executor.initialize();
		return executor;
	}

}

