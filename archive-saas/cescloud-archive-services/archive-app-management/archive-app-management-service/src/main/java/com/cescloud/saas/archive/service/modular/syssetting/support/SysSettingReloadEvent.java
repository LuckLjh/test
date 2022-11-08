package com.cescloud.saas.archive.service.modular.syssetting.support;

import org.springframework.context.ApplicationEvent;

/**
 * 租户配置刷新事件
 */
public class SysSettingReloadEvent extends ApplicationEvent {
	public SysSettingReloadEvent(Object source) {
		super(source);
	}
}
