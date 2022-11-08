package com.cesgroup.api.msg;

/**
 * 消息连接器默认实现类
 * 
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public class MockMsgConnector implements MsgConnector {

    @Override
    public void send(String subject, String content, String receiverId, String senderId) {
    }
}
