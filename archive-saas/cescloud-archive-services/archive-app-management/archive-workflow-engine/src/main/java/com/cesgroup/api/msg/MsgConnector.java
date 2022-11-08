package com.cesgroup.api.msg;

/**
 * 消息连接器
 * 
 * @author 国栋
 *
 */
public interface MsgConnector {

    /**
     * 发送消息
     * 
     * @param subject
     *            主题
     * @param content
     *            内容
     * @param receiverId
     *            收信人id
     * @param senderId
     *            发送人id
     */
    void send(String subject, String content, String receiverId, String senderId);
}
