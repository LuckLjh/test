package com.cesgroup.api.sendmail;

/**
 * 邮件发送连接器
 * 
 * @author 国栋
 *
 */
public interface SendmailConnector {

    /**
     * 发送邮件
     * 
     * @param to
     *            收件人
     * @param subject
     *            主题
     * @param content
     *            内容
     * @param tenantId
     *            租户
     */
    void send(String to, String subject, String content, String tenantId);

    /**
     * 发送邮件
     * 
     * @param sendmailDto
     *            邮件dto对象
     * @param tenantId
     *            租户id
     */
    void send(SendmailDTO sendmailDto, String tenantId);
}
