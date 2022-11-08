package com.cescloud.saas.archive.service.modular.businessconfig.cachesupport.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class BusinessMetadataReloadEvent extends ApplicationEvent {

	@Getter
	private Integer modelType;

	/**
	 * Create a new ApplicationEvent.
	 *
	 * @param tenantId the object on which the event initially occurred (never {@code null})
	 */
	public BusinessMetadataReloadEvent(Long tenantId, Integer modelType) {
		super(tenantId);
		this.modelType = modelType;
	}
}
