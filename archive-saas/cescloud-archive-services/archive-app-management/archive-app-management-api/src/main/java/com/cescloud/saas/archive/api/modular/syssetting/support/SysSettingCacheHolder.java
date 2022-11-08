package com.cescloud.saas.archive.api.modular.syssetting.support;

import com.cescloud.saas.archive.api.modular.syssetting.entity.SysSetting;
import com.cescloud.saas.archive.service.modular.common.core.constant.RedisKeyConstants;
import com.cescloud.saas.archive.service.modular.common.security.tenantfilter.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class SysSettingCacheHolder {

	private  final  RedisTemplate redisTemplate;

	public Optional<String> getCacheStrByKey(String cacheKey) {
		redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(SysSetting.class));
		SysSetting sysSetting = (SysSetting) redisTemplate.opsForHash().get(RedisKeyConstants.SYSSETTING_KEY + ":" + TenantContextHolder.getTenantId(), cacheKey);
		return Optional.ofNullable(sysSetting).isPresent()?
				Optional.ofNullable(sysSetting.getValue()):Optional.empty();
	}

	public Optional<String> getCacheStrByKey(String cacheKey,Long tenantId) {
		redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(SysSetting.class));
		SysSetting sysSetting = (SysSetting) redisTemplate.opsForHash().get(RedisKeyConstants.SYSSETTING_KEY + ":" + tenantId, cacheKey);
		return Optional.ofNullable(sysSetting).isPresent()?
				Optional.ofNullable(sysSetting.getValue()):Optional.empty();

	}

	public Optional<SysSetting> getCacheEntityByKey(String cacheKey) {
		redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(SysSetting.class));
		SysSetting sysSetting = (SysSetting) redisTemplate.opsForHash().get(RedisKeyConstants.SYSSETTING_KEY + ":" + TenantContextHolder.getTenantId(), cacheKey);
		return Optional.ofNullable(sysSetting);
	}

	public Optional<List<SysSetting>> getCacheEntityListByKey() {
		redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(SysSetting.class));
		List<SysSetting> sysSettingList = (List<SysSetting>) redisTemplate.opsForHash()
				.entries(RedisKeyConstants.SYSSETTING_KEY + ":" + TenantContextHolder.getTenantId()).values()
				.stream().collect(Collectors.toList());
		return Optional.ofNullable(sysSettingList);
	}

	public Optional<List<SysSetting>> getCacheEntityListByKeyList(Long tenantId, List<String> keyList) {
		redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(SysSetting.class));
		List<SysSetting> sysSettingList = (List<SysSetting>) redisTemplate.opsForHash().multiGet(RedisKeyConstants.SYSSETTING_KEY + ":" + tenantId, keyList);
		return Optional.ofNullable(sysSettingList);
	}

	public Optional<SysSetting> getCacheEntityByTenantIdKey(Long tenantId,String cacheKey) {
		redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(SysSetting.class));
		SysSetting sysSetting = (SysSetting) redisTemplate.opsForHash().get(RedisKeyConstants.SYSSETTING_KEY + ":" + tenantId, cacheKey);
		return Optional.ofNullable(sysSetting);
	}
}
