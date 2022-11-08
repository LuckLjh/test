package com.cescloud.saas.archive.service.modular.metadata.cachesupport.event;

import org.springframework.context.ApplicationEvent;

public class MetadataReloadEvent extends ApplicationEvent {


	/**
	 * Create a new ApplicationEvent.
	 *
	 * @param source the object on which the event initially occurred (never {@code null})
	 */
	public MetadataReloadEvent(Object source) {
		super(source);
	}
}
