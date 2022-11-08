package com.cesgroup.api.notification;

/**
 * 通知登记接口
 * 
 * @author 国栋
 *
 */
public interface NotificationRegistry {

    /**
     * 登记通知处理器
     * 
     * @param notificationHandler 通知处理器
     */
    void register(NotificationHandler notificationHandler);

    /**
     * 移除通知处理器
     * 
     * @param notificationHandler 通知处理器
     */
    void unregister(NotificationHandler notificationHandler);
}
