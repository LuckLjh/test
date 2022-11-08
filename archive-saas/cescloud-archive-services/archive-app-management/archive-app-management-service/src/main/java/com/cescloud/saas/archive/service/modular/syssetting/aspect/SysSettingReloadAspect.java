package com.cescloud.saas.archive.service.modular.syssetting.aspect;

import cn.hutool.core.util.StrUtil;
import com.cescloud.saas.archive.service.modular.common.core.util.CustomAnnotationParamBindUtil;
import com.cescloud.saas.archive.service.modular.common.security.tenantfilter.TenantContextHolder;
import com.cescloud.saas.archive.service.modular.syssetting.annotation.SysSettingReload;
import com.cescloud.saas.archive.service.modular.syssetting.support.SysSettingReloadEvent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
public class SysSettingReloadAspect {
	private final ApplicationEventPublisher publisher;

	@SneakyThrows
	@Around("@annotation(sysSettingReload)")
	public Object around(ProceedingJoinPoint pjp, SysSettingReload sysSettingReload) {
		Object obj = pjp.proceed();
		//根据spel表达式获取值
		Long tenantId = StrUtil.isEmpty(sysSettingReload.value()) ? TenantContextHolder.getTenantId() : CustomAnnotationParamBindUtil.getSpelValue(pjp, sysSettingReload.value(), Long.class);

		publisher.publishEvent(new SysSettingReloadEvent(tenantId));
		return obj;
	}
}
