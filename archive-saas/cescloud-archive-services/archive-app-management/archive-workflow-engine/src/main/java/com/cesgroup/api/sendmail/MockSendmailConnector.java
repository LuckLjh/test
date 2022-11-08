package com.cesgroup.api.sendmail;

/**
 * 模拟邮件发送连接器
 * 
 * @author 国栋
 *
 */
public class MockSendmailConnector implements SendmailConnector {

    @Override
    public void send(String to, String subject, String content, String tenantId) {
    }

    @Override
    public void send(SendmailDTO sendmailDto, String tenantId) {
    }
}
