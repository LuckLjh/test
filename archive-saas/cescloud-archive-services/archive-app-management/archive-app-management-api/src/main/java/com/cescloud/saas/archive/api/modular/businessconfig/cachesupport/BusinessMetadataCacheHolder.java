package com.cescloud.saas.archive.api.modular.businessconfig.cachesupport;

import cn.hutool.core.util.StrUtil;
import com.cescloud.saas.archive.service.modular.common.core.constant.RedisKeyConstants;
import com.cescloud.saas.archive.service.modular.common.tableoperation.constants.MetadataTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

@Slf4j
@RequiredArgsConstructor
public class BusinessMetadataCacheHolder {

	private final RedisTemplate redisTemplate;


	public String getBusinessMetadataType(String cacheKey, String metadataEnglish) {
		redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));
		String value = (String)redisTemplate.opsForHash().get(RedisKeyConstants.BUSINESS_METADATA_KEY + StrUtil.COLON + cacheKey, metadataEnglish);
		if (StrUtil.isEmpty(value)) {
			return "";
		}
		String[] arr = StrUtil.split(value, StrUtil.COLON);
		return StrUtil.isBlank(arr[0]) ? "" : MetadataTypeEnum.getEnum(Integer.valueOf(arr[0])).getValue();
	}


	public String getBusinessMetadataType(Long tenantId,Integer modelType,String metadataEnglish) {
		redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));
		String value = (String)redisTemplate.opsForHash().get(RedisKeyConstants.BUSINESS_METADATA_KEY + StrUtil.COLON + tenantId + StrUtil.COLON + modelType, metadataEnglish);
		if (StrUtil.isEmpty(value)) {
			return "";
		}
		String[] arr = StrUtil.split(value, StrUtil.COLON);
		return StrUtil.isBlank(arr[0]) ? "" : MetadataTypeEnum.getEnum(Integer.valueOf(arr[0])).getValue();
	}


	public Integer getBusinessMetadataLength(String cacheKey,String metadataEnglish) {
		redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));
		String value = (String)redisTemplate.opsForHash().get(RedisKeyConstants.BUSINESS_METADATA_KEY + StrUtil.COLON + cacheKey, metadataEnglish);
		if (StrUtil.isEmpty(value)) {
			return null;
		}
		String[] arr = StrUtil.split(value, StrUtil.COLON);
		return StrUtil.isEmpty(arr[1]) ? null : Integer.valueOf(arr[1]);
	}


	public Integer getBusinessMetadataLength(Long tenantId,Integer modelType,String metadataEnglish) {
		redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));
		String value = (String)redisTemplate.opsForHash().get(RedisKeyConstants.BUSINESS_METADATA_KEY + StrUtil.COLON + tenantId + StrUtil.COLON + modelType, metadataEnglish);
		if (StrUtil.isEmpty(value)) {
			return null;
		}
		String[] arr = StrUtil.split(value, StrUtil.COLON);
		return StrUtil.isEmpty(arr[1]) ? null : Integer.valueOf(arr[1]);
	}

}
