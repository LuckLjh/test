/*
package com.cescloud.saas.archive.service.modular.archivetype.stream.producer;

import com.cescloud.saas.archive.service.modular.archivetype.stream.channel.ShardingTaskOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@EnableBinding(ShardingTaskOutput.class)
public class ShardingTaskProducer {

    @Autowired
    private ShardingTaskOutput shardingTaskOutput;

    public boolean send(String message){
        return shardingTaskOutput
                .output()
                .send(MessageBuilder.withPayload(message).setHeader("sendTime",System.currentTimeMillis()).build());
    }
}
*/
