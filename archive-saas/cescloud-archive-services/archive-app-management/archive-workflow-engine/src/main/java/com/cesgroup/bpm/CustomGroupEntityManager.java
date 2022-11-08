package com.cesgroup.bpm;

import com.cesgroup.api.org.OrgConnector;
import com.cesgroup.api.org.OrgDTO;

import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.GroupEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义组实体管理器
 * 
 * @author 国栋
 *
 */
public class CustomGroupEntityManager extends GroupEntityManager {

    private static Logger logger = LoggerFactory.getLogger(CustomGroupEntityManager.class);

    private OrgConnector orgConnector;

    @Override
    public List<Group> findGroupsByUser(String userId) {
        logger.debug("根据用户id获取所属组 : {}", userId);

        List<Group> groups = new ArrayList<Group>();

        GroupEntity groupEntity;
        for (OrgDTO orgDto : orgConnector.getOrgsByUserId(userId)) {
            groupEntity = new GroupEntity(orgDto.getId());
            groupEntity.setName(orgDto.getName());
            groupEntity.setType(orgDto.getTypeName());
            groups.add(groupEntity);
        }

        return groups;
    }

    @Autowired
    public void setOrgConnector(OrgConnector orgConnector) {
        this.orgConnector = orgConnector;
    }
}
