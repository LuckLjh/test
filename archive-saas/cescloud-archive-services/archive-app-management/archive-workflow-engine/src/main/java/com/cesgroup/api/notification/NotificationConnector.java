package com.cesgroup.api.notification;

import java.util.Collection;

/**
 * 通知连接器接口
 * 
 * @author 国栋
 *
 */
public interface NotificationConnector {

    /**
     * 发送通知
     * 
     * @param notificationDto
     *            通知dto
     * @param tenantId
     *            租户id
     */
    void send(NotificationDTO notificationDto, String tenantId);

    /**
     * 获取类型
     * 
     * @param tenantId
     *            租户ID
     * @return collection
     */
    Collection<String> getTypes(String tenantId);
}
