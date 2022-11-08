package com.cescloud.saas.archive.service.modular.businessconfig.cachesupport.config;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cescloud.saas.archive.api.modular.businessconfig.entity.BusinessModelDefine;
import com.cescloud.saas.archive.common.constants.ModelTypeEnum;
import com.cescloud.saas.archive.service.modular.businessconfig.cachesupport.event.BusinessMetadataReloadEvent;
import com.cescloud.saas.archive.service.modular.businessconfig.service.BusinessModelDefineService;
import com.cescloud.saas.archive.service.modular.common.core.constant.RedisKeyConstants;
import com.cescloud.saas.archive.service.modular.common.data.cache.RedisUtil;
import com.cescloud.saas.archive.service.modular.common.tableoperation.constants.MetadataTypeEnum;
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
import java.util.stream.Stream;

/**
 * <p>系统启动时自动初始化元字段配置信息</p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class BusinessMetadataInitRunner implements ApplicationRunner {

	private final RedisTemplate redisTemplate;

	private final RedisUtil redisUtil;

	private final BusinessModelDefineService businessModelDefineService;

	@Override
	public void run(ApplicationArguments args) {
		Stream.of(ModelTypeEnum.values()).forEach(modelTypeEnum -> {
			long current = 1, size = 500;
			IPage<BusinessModelDefine> pageInfo;
			for (;;) {
				pageInfo = businessModelDefineService.getPageBusinessModelDefines(current, size, modelTypeEnum.getValue());
				List<BusinessModelDefine> records = pageInfo.getRecords();
				setCache(records);
				long pages = pageInfo.getPages();
				if (pages == 0 || pages == current) {
					break;
				}
				current++;
			}
		});
	}

	@Async
	@Order
	@EventListener(BusinessMetadataReloadEvent.class)
	public void reloadBusinessMetadata(BusinessMetadataReloadEvent event) {
		Long tenantId = (Long) event.getSource();
		Integer modelType = event.getModelType();
		if (ObjectUtil.isNotNull(tenantId)) {
			if (ObjectUtil.isNotNull(modelType)) {
				long current = 1, size = 500;
				String regKeys = RedisKeyConstants.BUSINESS_METADATA_KEY + StrUtil.COLON + tenantId + StrUtil.COLON + modelType;
				redisUtil.deleteKeys(regKeys,10);
				IPage<BusinessModelDefine> pageInfo;
				for (;;) {
					pageInfo = businessModelDefineService.getPageTenantBusinessModelDefines(current,size,tenantId,modelType);
					List<BusinessModelDefine> records = pageInfo.getRecords();
					setCache(records);
					long pages = pageInfo.getPages();
					if (pages == 0 || pages == current) {
						break;
					}
					current++;
				}
			} else {
				String regKeys = RedisKeyConstants.BUSINESS_METADATA_KEY + StrUtil.COLON + tenantId + StrUtil.COLON + "*";
				redisUtil.deleteKeys(regKeys,10);
				Stream.of(ModelTypeEnum.values()).forEach(modelTypeEnum -> {
					long current = 1, size = 500;
					IPage<BusinessModelDefine> pageInfo;
					for (;;) {
						pageInfo = businessModelDefineService.getPageTenantBusinessModelDefines(current, size, tenantId, modelTypeEnum.getValue());
						List<BusinessModelDefine> records = pageInfo.getRecords();
						setCache(records);
						long pages = pageInfo.getPages();
						if (pages == 0 || pages == current) {
							break;
						}
						current++;
					}
				});
			}
		}
	}

	private void setCache(List<BusinessModelDefine> records) {
		if (CollectionUtil.isEmpty(records)) {
			return;
		}
		final RedisSerializer keySerializer = redisTemplate.getKeySerializer();
		final RedisSerializer hashValueSerializer = new Jackson2JsonRedisSerializer<>(String.class);
		redisTemplate.executePipelined((RedisCallback<Boolean>) connection -> {
			records.forEach(businessModelDefine -> {
				String metadataLength = ObjectUtil.isNull(businessModelDefine.getMetadataLength()) ? "" : String.valueOf(businessModelDefine.getMetadataLength());
				String code = ObjectUtil.isNull(MetadataTypeEnum.getEnum(businessModelDefine.getMetadataType())) ? "" : String.valueOf(MetadataTypeEnum.getEnum(businessModelDefine.getMetadataType()).getCode());
				connection.hSet(keySerializer.serialize(RedisKeyConstants.BUSINESS_METADATA_KEY + StrUtil.COLON + businessModelDefine.getTenantId() + StrUtil.COLON + businessModelDefine.getModelType()),
						keySerializer.serialize(businessModelDefine.getMetadataEnglish()),
						hashValueSerializer.serialize(code + StrUtil.COLON + metadataLength));
			});
			return null;
		});
	}
}
