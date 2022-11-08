/*
package com.cescloud.saas.archive.service.modular.archivetype.service.impl;

import com.cescloud.saas.archive.service.modular.archivetype.service.MessageSendService;
import com.cescloud.saas.archive.service.modular.archivetype.stream.producer.ShardingTaskProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageSendServiceImpl implements MessageSendService {

	@Autowired
	private ShardingTaskProducer shardingTaskProducer;

	@Override
	public boolean reloadDataSource() {
		return shardingTaskProducer.send("reloadDataSource");
	}
}
*/
