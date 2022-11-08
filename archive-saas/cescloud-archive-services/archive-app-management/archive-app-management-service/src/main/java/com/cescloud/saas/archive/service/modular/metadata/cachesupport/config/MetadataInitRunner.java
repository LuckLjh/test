package com.cescloud.saas.archive.service.modular.metadata.cachesupport.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.service.modular.common.core.constant.RedisKeyConstants;
import com.cescloud.saas.archive.service.modular.common.data.cache.RedisUtil;
import com.cescloud.saas.archive.service.modular.common.tableoperation.constants.MetadataTypeEnum;
import com.cescloud.saas.archive.service.modular.metadata.cachesupport.event.MetadataReloadEvent;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * <p>系统启动时自动初始化元字段配置信息</p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MetadataInitRunner implements ApplicationRunner {

	private final RedisTemplate redisTemplate;

	private final MetadataService metadataService;

	private final RedisUtil redisUtil;

	private static final int size = 500;

	@Override
	public void run(ApplicationArguments args) {
		log.info("初始化系统元字段信息");
		// 分页获取，防止租户数多，数据量大，导致oom
		IPage<Metadata> pageInfoMax = metadataService.getAllTenantMetadatas(1, size);
		Long pageNo = (pageInfoMax.getTotal() -1) / size + 1;

		for (int current = 1; current <= pageNo; current++) {
			int currentPage = current;
			CompletableFuture cf = CompletableFuture.runAsync(()->{
				IPage<Metadata> pageInfo = metadataService.getAllTenantMetadatas(currentPage, size);
				List<Metadata> records = pageInfo.getRecords();
				setCache(records);
			});
		}
	}

	@Async
	@Order
	@EventListener(MetadataReloadEvent.class)
	public void reloadMetadata(MetadataReloadEvent event) {
		Long tenantId = (Long) event.getSource();
		if (ObjectUtil.isNotNull(tenantId)) {
			String regKeys = RedisKeyConstants.METADATA_KEY + StrUtil.COLON + "t_" + tenantId + "_*";
			redisUtil.deleteKeys(regKeys,10);
			IPage<Metadata> pageInfo;
			long current = 1, size = 500;
			for (;;) {
				pageInfo = metadataService.getTenantMetadatas(current, size, tenantId);
				List<Metadata> records = pageInfo.getRecords();
				if(CollUtil.isEmpty(records)){
					break;
				}
				setCache(records);
				if ( records.size()<size) {
					break;
				}
				current++;
			}
		}
	}


	private void setCache(List<Metadata> records) {
		if (CollectionUtil.isEmpty(records)) {
			return;
		}
		final RedisSerializer keySerializer = redisTemplate.getKeySerializer();
		final RedisSerializer hashValueSerializer = new Jackson2JsonRedisSerializer<>(String.class);
		redisTemplate.executePipelined((RedisCallback<Boolean>) connection -> {
			records.forEach(metadata -> {
				String metadataLength = ObjectUtil.isNull(metadata.getMetadataLength()) ? "" : String.valueOf(metadata.getMetadataLength());
				String code = ObjectUtil.isNull(MetadataTypeEnum.getEnum(metadata.getMetadataType())) ? "" : String.valueOf(MetadataTypeEnum.getEnum(metadata.getMetadataType()).getCode());
				connection.hSet(keySerializer.serialize(RedisKeyConstants.METADATA_KEY + StrUtil.COLON + metadata.getStorageLocate()),
						keySerializer.serialize(metadata.getMetadataEnglish()),
						hashValueSerializer.serialize(code + StrUtil.COLON + metadataLength));
			});
			return null;
		});
	}

}
