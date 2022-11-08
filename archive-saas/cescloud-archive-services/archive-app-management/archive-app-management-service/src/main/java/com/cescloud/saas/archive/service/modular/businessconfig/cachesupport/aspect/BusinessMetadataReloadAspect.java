package com.cescloud.saas.archive.service.modular.businessconfig.cachesupport.aspect;

import cn.hutool.core.util.StrUtil;
import com.cescloud.saas.archive.service.modular.businessconfig.cachesupport.annotation.BusinessMetadataReload;
import com.cescloud.saas.archive.service.modular.businessconfig.cachesupport.event.BusinessMetadataReloadEvent;
import com.cescloud.saas.archive.service.modular.common.core.util.CustomAnnotationParamBindUtil;
import com.cescloud.saas.archive.service.modular.common.security.tenantfilter.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 3)
@RequiredArgsConstructor
public class BusinessMetadataReloadAspect {

	private final ApplicationEventPublisher publisher;

	@SneakyThrows
	@Around("@annotation(businessMetadataReload)")
	public Object around(ProceedingJoinPoint pjp, BusinessMetadataReload businessMetadataReload) {
		Object obj = pjp.proceed();

		EvaluationContext context = CustomAnnotationParamBindUtil.bindParam(pjp);
		//根据spel表达式获取值
		Long tenantId = StrUtil.isEmpty(businessMetadataReload.tenantId()) ? TenantContextHolder.getTenantId() : CustomAnnotationParamBindUtil.getSpelValue(context, businessMetadataReload.tenantId(), Long.class);
		Integer modelType = StrUtil.isEmpty(businessMetadataReload.modelType()) ? null : CustomAnnotationParamBindUtil.getSpelValue(context, businessMetadataReload.modelType(), Integer.class);

		publisher.publishEvent(new BusinessMetadataReloadEvent(tenantId, modelType));
		return obj;
	}
}
