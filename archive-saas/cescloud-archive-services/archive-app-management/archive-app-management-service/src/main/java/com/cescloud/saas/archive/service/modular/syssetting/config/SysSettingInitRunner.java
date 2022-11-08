package com.cescloud.saas.archive.service.modular.syssetting.config;

import cn.hutool.core.util.ObjectUtil;
import com.cescloud.saas.archive.api.modular.syssetting.entity.SysSetting;
import com.cescloud.saas.archive.service.modular.common.core.constant.RedisKeyConstants;
import com.cescloud.saas.archive.service.modular.syssetting.service.SysSettingService;
import com.cescloud.saas.archive.service.modular.syssetting.support.SysSettingReloadEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.scheduling.annotation.Async;

/**
 * <p>系统启动时自动初始化租户系统配置信息</p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SysSettingInitRunner implements ApplicationRunner {
	private final RedisTemplate redisTemplate;
	private final SysSettingService sysSettingService;

	@Override
	public void run(ApplicationArguments args) {
		log.info("初始化系统配置信息");
		sysSettingService.getSystemSettingInfo().forEach(sysSetting -> {
			redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(SysSetting.class));
			try {
				if (log.isDebugEnabled()) {
					log.debug("加载租户系统配置信息：{},{}", sysSetting.getTenantId(), sysSetting);
				}
				redisTemplate.opsForHash().put(RedisKeyConstants.SYSSETTING_KEY + ":" + sysSetting.getTenantId(), sysSetting.getCode(), sysSetting);
			}catch (Exception e){
				log.error("加载租户系统配置信息出错：{},{}", sysSetting.getTenantId(), sysSetting,e);
			}

		});
		log.info("初始化系统配置信息结束 ");
	}

	@Async
	@Order
	@EventListener(SysSettingReloadEvent.class)
	public void reloadSysSeting(SysSettingReloadEvent event) {
		Long tenantid = (Long) event.getSource();
		if (ObjectUtil.isNotNull(tenantid)) {
			redisTemplate.delete(RedisKeyConstants.SYSSETTING_KEY + ":" + tenantid);
			sysSettingService.getSystemSettingInfo(tenantid).forEach(sysSetting -> {
				log.info("重新加载租户系统配置信息：{},{}", tenantid, sysSetting);
				redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(SysSetting.class));
				redisTemplate.opsForHash().put(RedisKeyConstants.SYSSETTING_KEY + ":" + tenantid, sysSetting.getCode(), sysSetting);
			});
		}
	}
}
