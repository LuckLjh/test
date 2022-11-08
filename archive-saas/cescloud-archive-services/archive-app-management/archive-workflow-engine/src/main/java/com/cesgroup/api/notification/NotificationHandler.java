package com.cesgroup.api.notification;

/**
 * 通知处理器
 * 
 * @author 国栋
 *
 */
public interface NotificationHandler {

    String getType();

    /**
     * 通知处理
     * 
     * @param notificationDto
     *            通知dto对象
     * @param tenantId
     *            租户id
     */
    void handle(NotificationDTO notificationDto, String tenantId);
}
