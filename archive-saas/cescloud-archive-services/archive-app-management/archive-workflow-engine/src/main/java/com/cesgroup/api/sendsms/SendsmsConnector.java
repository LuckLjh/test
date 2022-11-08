package com.cesgroup.api.sendsms;

/**
 * 短信发送连接器
 * 
 * @author 国栋
 *
 */
public interface SendsmsConnector {

    /**
     * 发送短消息
     * 
     * @param to
     *            发送对象
     * @param content
     *            内容
     * @param tenantId
     *            租户ID
     */
    void send(String to, String content, String tenantId);
}
