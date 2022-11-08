package com.cesgroup.api.notification;

import com.cesgroup.api.template.TemplateConnector;
import com.cesgroup.api.template.TemplateDTO;
import com.cesgroup.core.template.TemplateService;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认通知连接器
 * 
 * @author 国栋
 *
 */
public class DefaultNotificationConnector implements NotificationConnector, NotificationRegistry {

    private Map<String, NotificationHandler> map = new HashMap<String, NotificationHandler>();

    private TemplateConnector templateConnector;

    private TemplateService templateService;

    /**
     * 
     * 
     * @see com.cesgroup.api.notification.NotificationConnector#send(
     * com.cesgroup.api.notification.NotificationDTO, java.lang.String)
     */
    @Override
    public void send(NotificationDTO notificationDto, String tenantId) {
        if (notificationDto.getTemplate() != null) {
            TemplateDTO templateDto = templateConnector.findByCode(notificationDto.getTemplate(),
                tenantId);
            String subject = this.processTemplate(templateDto.getField("subject"),
                notificationDto.getData());
            String content = this.processTemplate(templateDto.getField("content"),
                notificationDto.getData());

            if (subject != null) {
                notificationDto.setSubject(subject);
            }

            if (content != null) {
                notificationDto.setContent(content);
            }
        }

        List<String> types = notificationDto.getTypes();

        for (String type : types) {
            sendByType(type, notificationDto, tenantId);
        }
    }

    /**
     * 通过消息类型使用对应的handler发送消息。
     * 
     * @param type
     *            通知的类型
     * @param notificationDto
     *            通知对象
     * @param tenantId
     *            租户id
     */
    public void sendByType(String type, NotificationDTO notificationDto, String tenantId) {
        NotificationHandler notificationHandler = map.get(type);

        if (notificationHandler == null) {
            return;
        }

        notificationHandler.handle(notificationDto, tenantId);
    }

    public String processTemplate(String template, Map<String, Object> data) {
        return templateService.renderText(template, data);
    }

    @Override
    public void register(NotificationHandler notificationHandler) {
        map.put(notificationHandler.getType(), notificationHandler);
    }

    @Override
    public void unregister(NotificationHandler notificationHandler) {
        map.remove(notificationHandler.getType());
    }

    @Override
    public Collection<String> getTypes(String tenantId) {
        return map.keySet();
    }

    public void setMap(Map<String, NotificationHandler> map) {
        this.map = map;
    }

    public void setTemplateConnector(TemplateConnector templateConnector) {
        this.templateConnector = templateConnector;
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
}
