package com.cesgroup.api.sendsms;

/**
 * 短信发送连接器默认实现
 * 
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public class MockSendsmsConnector implements SendsmsConnector {

    @Override
    public void send(String to, String content, String tenantId) {
    }
}
