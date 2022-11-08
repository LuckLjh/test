package com.cesgroup.api.notification;

import java.util.Collection;

/**
 * 通知连接器接口默认实现
 * 
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public class MockNotificationConnector implements NotificationConnector {

    @Override
    public void send(NotificationDTO notificationDto, String tenantId) {
    }

    @Override
    public Collection<String> getTypes(String tenantId) {
        return null;
    }
}
