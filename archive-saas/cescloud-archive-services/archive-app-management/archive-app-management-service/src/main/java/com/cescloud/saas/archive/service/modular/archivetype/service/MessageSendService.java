package com.cescloud.saas.archive.service.modular.archivetype.service;

/**
 * 向mq中发消息的处理类
 * @author liwei
 */
public interface MessageSendService {

	/**
	 * 向mq推送消息，让动态数据源重新初始化
	 * @return
	 */
	boolean reloadDataSource();

}
