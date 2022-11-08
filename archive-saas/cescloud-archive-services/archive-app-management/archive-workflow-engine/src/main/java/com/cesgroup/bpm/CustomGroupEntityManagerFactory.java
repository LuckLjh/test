package com.cesgroup.bpm;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.GroupEntityManager;
import org.activiti.engine.impl.persistence.entity.GroupIdentityManager;

/**
 * 自定义组实体管理器工厂
 * 
 * @author 国栋
 *
 */
public class CustomGroupEntityManagerFactory implements SessionFactory {

    private GroupEntityManager groupEntityManager;

    public void setGroupEntityManager(GroupEntityManager groupEntityManager) {
        this.groupEntityManager = groupEntityManager;
    }

    @Override
    public Class<?> getSessionType() {
        return GroupIdentityManager.class;
    }

    @Override
    public Session openSession() {
        return groupEntityManager;
    }
}
