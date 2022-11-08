package com.cescloud.saas.archive.api.modular.syssetting.support;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@AllArgsConstructor
@ConditionalOnWebApplication
public class SysSettingCacheAutoConfiguration {

	@ConditionalOnMissingBean(SysSettingCacheHolder.class)
	@Bean
	public SysSettingCacheHolder cacheHolder(RedisTemplate redisTemplate){
		return  new SysSettingCacheHolder(redisTemplate);
	}
}
