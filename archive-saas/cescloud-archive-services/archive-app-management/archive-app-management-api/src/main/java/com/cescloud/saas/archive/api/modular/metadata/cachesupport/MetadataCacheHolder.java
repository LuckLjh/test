package com.cescloud.saas.archive.api.modular.metadata.cachesupport;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.cescloud.saas.archive.service.modular.common.core.constant.RedisKeyConstants;
import com.cescloud.saas.archive.service.modular.common.tableoperation.constants.MetadataTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import java.util.Iterator;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class MetadataCacheHolder {

	private final RedisTemplate redisTemplate;

	/**
	 * 根据表名和字段名称获取字段类型
	 * @param storageLocate 表名
	 * @param metadataEnglish 字段名
	 * @return
	 */
	public String getMetadataType(String storageLocate, String metadataEnglish) {
		redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));
		String value = (String)redisTemplate.opsForHash().get(RedisKeyConstants.METADATA_KEY + StrUtil.COLON + storageLocate, metadataEnglish);
		if (StrUtil.isEmpty(value)) {
			return "";
		}
		String[] arr = StrUtil.split(value, StrUtil.COLON);
		return StrUtil.isBlank(arr[0]) ? "" : MetadataTypeEnum.getEnum(Integer.valueOf(arr[0])).getValue();
	}


	/**
	 * 根据表名获取所有字段类型
	 * @param storageLocate 表名
	 * @return
	 */
	public Map<String,String> getMetadataTypeMapByStorage(String storageLocate) {
		redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));
		Map<String,String> map = redisTemplate.opsForHash().entries(RedisKeyConstants.METADATA_KEY + StrUtil.COLON + storageLocate);
		Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, String> next = iterator.next();
			String key = next.getKey();
			String value = next.getValue();
			if (StrUtil.isNotBlank(value)) {
				String[] arr = StrUtil.split(value, StrUtil.COLON);
				String metadataType = StrUtil.isBlank(arr[0]) ? "" : MetadataTypeEnum.getEnum(Integer.valueOf(arr[0])).getValue();
				map.put(key, metadataType);
			}
		}
		return map;
	}



	/**
	 * 根据表名和字段名称获取字段长度
	 * @param storageLocate 表名
	 * @param metadataEnglish 字段名
	 * @return
	 */
	public Integer getMetadataLength(String storageLocate, String metadataEnglish) {
		redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));
		String value = (String)redisTemplate.opsForHash().get(RedisKeyConstants.METADATA_KEY + StrUtil.COLON + storageLocate, metadataEnglish);
		if (StrUtil.isEmpty(value)) {
			return null;
		}
		String[] arr = StrUtil.split(value, StrUtil.COLON);
		return StrUtil.isEmpty(arr[1]) ? null : Integer.valueOf(arr[1]);
	}

	/**
	 * 根据表名获取所有字段长度
	 * @param storageLocate 表名
	 * @return
	 */
	public Map<String, Integer> getMetadataLengthMapByStorage(String storageLocate) {
		final Map<String,Integer> result = CollectionUtil.newHashMap();
		redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));
		Map<String,String> map = redisTemplate.opsForHash().entries(RedisKeyConstants.METADATA_KEY + StrUtil.COLON + storageLocate);
		Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, String> next = iterator.next();
			String key = next.getKey();
			String value = next.getValue();
			if (StrUtil.isNotBlank(value)) {
				String[] arr = StrUtil.split(value, StrUtil.COLON);
				result.put(key,StrUtil.isEmpty(arr[1]) ? null : Integer.valueOf(arr[1]));
			}
		}
		return result;
	}

}
