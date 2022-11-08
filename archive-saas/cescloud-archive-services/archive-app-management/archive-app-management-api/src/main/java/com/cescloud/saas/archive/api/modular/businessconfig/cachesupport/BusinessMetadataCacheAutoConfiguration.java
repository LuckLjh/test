package com.cescloud.saas.archive.api.modular.businessconfig.cachesupport;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class BusinessMetadataCacheAutoConfiguration {

	@Bean
	public BusinessMetadataCacheHolder businessMetadataCacheHolder(RedisTemplate redisTemplate) {
		return new BusinessMetadataCacheHolder(redisTemplate);
	}
}
