package com.cescloud.saas.archive.api.modular.metadata.cachesupport;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@AllArgsConstructor
public class MetadataCacheAutoConfiguration {


	@Bean
	public MetadataCacheHolder metadataCacheHolder(RedisTemplate redisTemplate) {
		return new MetadataCacheHolder(redisTemplate);
	}

}
