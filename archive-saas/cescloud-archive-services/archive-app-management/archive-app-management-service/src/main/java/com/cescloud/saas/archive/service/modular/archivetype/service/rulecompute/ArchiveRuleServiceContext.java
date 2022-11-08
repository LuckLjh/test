package com.cescloud.saas.archive.service.modular.archivetype.service.rulecompute;

import com.cescloud.saas.archive.common.annotation.RuleHandler;
import com.cescloud.saas.archive.common.constants.FormStatusEnum;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liwei
 */
@Component
public class ArchiveRuleServiceContext {

	private final Map<FormStatusEnum,ArchiveRuleService> handlerMap = new HashMap<>();

	public ArchiveRuleService getArchiveRuleService(FormStatusEnum formStatusEnum) {
		return handlerMap.get(formStatusEnum);
	}

	public void putArchiveRuleService(FormStatusEnum formStatusEnum,ArchiveRuleService archiveRuleService) {
		handlerMap.put(formStatusEnum,archiveRuleService);
	}

	/**
	 * spring容器启动时，通过注解，将数据规则处理类放在ArchiveRuleServiceContext的handlerMap中
	 * @param event ContextRefreshedEvent事件
	 */
	@EventListener
	public void register(ContextRefreshedEvent event) {
		//上下文
		ApplicationContext applicationContext = event.getApplicationContext();
		//获取所有RuleHandler注解的bean
		Map<String, Object> beans = applicationContext.getBeansWithAnnotation(RuleHandler.class);
		//把对应的处理类放在handlerMap中
		beans.forEach((name,bean) -> {
			RuleHandler ruleHandler = bean.getClass().getAnnotation(RuleHandler.class);
			putArchiveRuleService(ruleHandler.value(),(ArchiveRuleService) bean);
		});
	}
}
