/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.service.impl</p>
 * <p>文件名:WorkflowModelServiceImpl.java</p>
 * <p>创建时间:2019年10月15日 下午5:17:30</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.cescloud.saas.archive.api.modular.workflow.dto.KeyValueDTO;
import com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowModelDTO;
import com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowModelPurviewDTO;
import com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowModelPurviewPostDTO;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.workflow.mapper.WorkflowMapper;
import com.cescloud.saas.archive.service.modular.workflow.service.WorkflowModelService;
import com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService;
import com.cescloud.saas.archive.service.modular.workflow.utils.WorkflowUtil;
import com.cesgroup.bpm.persistence.domain.BpmConfBase;
import com.cesgroup.bpm.persistence.domain.BpmModelCategory;
import com.cesgroup.bpm.persistence.domain.BpmModelEntity;
import com.cesgroup.bpm.persistence.domain.BpmModelPurview;
import com.cesgroup.bpm.persistence.manager.BpmConfBaseManager;
import com.cesgroup.bpm.persistence.manager.BpmModelManager;
import com.cesgroup.bpm.persistence.manager.BpmModelPurviewManager;
import com.cesgroup.core.query.PropertyFilter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年10月15日
 */
@Component
public class WorkflowModelServiceImpl implements WorkflowModelService {

    @Autowired
    private BpmModelManager bpmModelManager;

    @Autowired
    private BpmConfBaseManager bpmConfBaseManager;

    @Autowired
    private WorkflowVersionService workflowVersionService;

    @Autowired
    private BpmModelPurviewManager bpmModelPurviewManager;

    @Autowired
	private WorkflowMapper workflowMapper;

	/**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowCategoryService#getBpmModelListByCatelogId(java.lang.String,
     *      java.lang.Long)
     */
    @Override
    public List<?> getBpmModelListByCatelogId(Long tenantId, Long categoryId,String type) {
        Assert.notNull(tenantId, "tenantId不能为空");
        Assert.notNull(categoryId, "categoryId不能为空");
        final List<PropertyFilter> propertyFilters = new ArrayList<PropertyFilter>();
        propertyFilters.add(new PropertyFilter("EQS_tenantId", String.valueOf(tenantId)));
        propertyFilters.add(new PropertyFilter("EQL_modelCategory.id", String.valueOf(categoryId)));
        /*if(type.equals("1")){
			type ="usingProcess";
		}else if(type.equals("2")){
			type ="appraise";
		}else if(type.equals("3")){
			type ="destory";
		}else if(type.equals("4")){
			type ="transfer";
		}else if(type.equals("5")){
			type ="transferReceiving";
		}else if(type.equals("6")){
			type ="filingProcess";
		}else if(type.equals("7")){
			type ="inStorage";
		}else if(type.equals("8")){
			type ="returnProcess";
		}else if(type.equals("9")){
			type ="safestorage";
		}*/
        if(!type.equals("0")) {
			propertyFilters.add(new PropertyFilter("EQS_businessCode", type));
		}
        final List<WorkflowModelDTO> resultList = Lists.newArrayList();
        final List<BpmModelEntity> bpmModelEntitieList = bpmModelManager.find(propertyFilters);
        if (null != bpmModelEntitieList) {
            final Map<String, List<WorkflowModelPurviewDTO>> allModelPurviewMap = getAllModelPurviewMap(
                tenantId.toString());
            WorkflowModelDTO modelDTO;
            for (final BpmModelEntity entity : bpmModelEntitieList) {
                modelDTO = WorkflowUtil.convert(new WorkflowModelDTO(), entity);
                if (allModelPurviewMap.containsKey(modelDTO.getCode())) {
                    modelDTO.setModelPerviewList(allModelPurviewMap.get(modelDTO.getCode()));
                } else {
                    modelDTO.setModelPerviewList(Lists.newArrayList());
                }
                resultList.add(modelDTO);
            }
        }
        return resultList;
    }

    /**
     * 获取可见范围
     *
     * @param tenantId
     * @return
     */
    private Map<String, List<WorkflowModelPurviewDTO>> getAllModelPurviewMap(String tenantId) {
        final Map<String, List<WorkflowModelPurviewDTO>> modelPurviewMap = Maps.newHashMap();
        final List<BpmModelPurview> list = bpmModelPurviewManager.findAll(tenantId);
        if (null != list) {
            WorkflowModelPurviewDTO purviewDTO;
            for (final BpmModelPurview entity : list) {
                purviewDTO = WorkflowUtil.convert(new WorkflowModelPurviewDTO(), entity);
                if (!modelPurviewMap.containsKey(entity.getBpmModelCode())) {
                    modelPurviewMap.put(entity.getBpmModelCode(), Lists.newArrayList(purviewDTO));
                } else {
                    modelPurviewMap.get(entity.getBpmModelCode()).add(purviewDTO);
                }
            }
        }
        return modelPurviewMap;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowCategoryService#getBpmModelEntityById(java.lang.Long)
     */
    @Override
    public BpmModelEntity getBpmModelEntityById(Long id) {
        Assert.notNull(id, "id不能为空");
        return bpmModelManager.get(id);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowCategoryService#saveBpmModelEntity(com.cesgroup.bpm.persistence.domain.BpmModelEntity,
     *      java.lang.String)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BpmModelEntity saveBpmModelEntity(BpmModelEntity bpmModelEntity, Long categoryId, Long tenantId) {
		String name = bpmModelEntity.getName();
		Assert.notNull(name, "模型名称不能为空");
        Assert.notNull(categoryId, "categoryId不能为空");
        Assert.isTrue(checkBpmModelNameUnique(bpmModelEntity, categoryId, String.valueOf(tenantId)), "模型名称重复，请检查");
        BpmModelEntity entity = null;
        final Long id = bpmModelEntity.getId();
        if (id != null) { // 修改
            entity = bpmModelManager.get(id);
            Assert.notNull(entity, "修改流程不存在或已被删除，请刷新后重试");
            entity.setName(name);
            entity.setDescription(bpmModelEntity.getDescription());
        } else { // 新增
            entity = bpmModelEntity;
            entity.setTenantId(String.valueOf(tenantId));
            entity.setPriority(getMaxSortNo(categoryId));
            entity.setModelCategory(new BpmModelCategory(categoryId));
            entity.setStatus(0);
            entity.setCreateTime(new Date());
            // 如果是有业务标识，则自动生成code
            if (StrUtil.isNotBlank(entity.getBusinessCode())) {
                generateBpmModelCodeWithBusinessCode(entity, tenantId);
            } else {
                Assert.notNull(bpmModelEntity.getCode(), "模型编码不能为空");
                Assert.isTrue(checkBpmModelCodeUnique(bpmModelEntity, String.valueOf(tenantId)), "模型编码重复，请检查");
            }
        }
        bpmModelManager.save(entity);
        if (null == id || checkEmptyBpmConfBaseByBpmModelId(entity.getId())) {
            //生成默认版本1.0
            final BpmConfBase bpmConfBase = new BpmConfBase();
            bpmConfBase.setVersion("1.0");
            bpmConfBase.setDescription("系统自动生成版本号");
            workflowVersionService.saveBpmConfBase(bpmConfBase, entity.getId());
        }
		String key = "";
        if (id != null) {
			key = workflowVersionService.getBpmConfBaseListByBpmModelId(id).get(0).getProcessDefinitionId();
		}
        if (StrUtil.isNotBlank(key)) {
        	//更新流程名字时也更新T_WF_ACT_RE_PROCDEF表的，在流程继续时会再次获取到这个新改的名字
			workflowMapper.updateNameByKey_PROCDEF(name, key, SecurityUtils.getUser().getTenantId());
			//这里不影响GDDA8-1764流程名字的获取，是在流程中点击版本管理获取的名字，顺便也改了
			workflowMapper.updateNameByKey_Base(name, key);
		}
		return entity;
    }

    /**
     * 检查版本信息
     *
     * @param id
     * @return
     */
    private boolean checkEmptyBpmConfBaseByBpmModelId(Long id) {
        final Integer count = bpmConfBaseManager.getCount("select count(*) from BpmConfBase t where t.bpmModel = ?0",
            new BpmModelEntity(id));
        return 0 == count;
    }

    /**
     * 生成流程编码
     *
     * @param bpmModelEntity
     * @param tenantId
     */
    private void generateBpmModelCodeWithBusinessCode(BpmModelEntity bpmModelEntity, Long tenantId) {
        final String randomStr4 = RandomUtil.randomString(4);
        bpmModelEntity.setCode(bpmModelEntity.getBusinessCode() + "." + randomStr4);
        if (!checkBpmModelCodeUnique(bpmModelEntity, String.valueOf(tenantId))) {
            generateBpmModelCodeWithBusinessCode(bpmModelEntity, tenantId);
        }
    }

    private Integer getMaxSortNo(Long categoryId) {
        Integer maxSort = 1;

        final Object max = bpmModelManager
            .findUnique("select max(b.priority) from BpmModelEntity b where b.modelCategory = ?0",
                new BpmModelCategory(categoryId));

        if (null != max) {
            maxSort = Integer.parseInt(max.toString()) + 1;
        }

        return maxSort;
    }

    private boolean checkBpmModelNameUnique(BpmModelEntity bpmModelEntity, Long categoryId, String tenantId) {
        final BpmModelCategory bpmModelCategory = new BpmModelCategory(categoryId);
        BpmModelEntity dbBpmModelEntity = null;
        String hql = null;
        if (bpmModelEntity.getId() != null) { //修改
            hql = " from BpmModelEntity b where b.name = ?0 and b.modelCategory = ?1 "
                + " and b.tenantId = ?2 and b.id not in (?3)";
            dbBpmModelEntity = bpmModelManager.findUnique(hql, bpmModelEntity.getName(), bpmModelCategory,
                tenantId, bpmModelEntity.getId());
        } else {
            hql = " from BpmModelEntity b where b.name = ?0 and b.modelCategory = ?1 and b.tenantId = ?2";
            dbBpmModelEntity = bpmModelManager.findUnique(hql, bpmModelEntity.getName(),
                bpmModelCategory, tenantId);
        }
        return null == dbBpmModelEntity;
    }

    private boolean checkBpmModelCodeUnique(BpmModelEntity bpmModelEntity, String tenantId) {
        BpmModelEntity dbBpmModelEntity = null;
        String hql = null;
        final String code = bpmModelEntity.getCode().toUpperCase();
        if (bpmModelEntity.getId() != null) {
            hql = " from BpmModelEntity b where upper(b.code) = ?0 and b.tenantId = ?1 "
                + "and b.id not in (?2)";
            dbBpmModelEntity = bpmModelManager.findUnique(hql, code, tenantId, bpmModelEntity.getId());
        } else {
            hql = " from BpmModelEntity b where upper(b.code) = ?0 and b.tenantId = ?1";
            dbBpmModelEntity = bpmModelManager.findUnique(hql, code, tenantId);
        }
        return null == dbBpmModelEntity;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowCategoryService#removeBpmModelEntityById(java.lang.Long)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeBpmModelEntityById(Long id) {
        Assert.notNull(id, "id不能为空");
        removeBpmConfBaseByBpmModelId(id);
        bpmModelManager.removeById(id);
        return true;
    }

    /**
     * 删除版本信息
     *
     * @param id
     * @return
     */
    private void removeBpmConfBaseByBpmModelId(Long id) {
        /*final Integer count = bpmConfBaseManager.getCount("select count(*) from BpmConfBase t where t.bpmModel = ?0",
            new BpmModelEntity(id));*/
        /*final List<BpmConfBase> bpmConfBaseList = bpmConfBaseManager.findByBpmModelId(id);
        for (final BpmConfBase entity : bpmConfBaseList) {
            workflowVersionService.removeBpmConfBaseById(entity.getId());
        }*/
        workflowVersionService.removeBpmConfBaseByBpmModelId(id);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowModelService#enableBpmModelEntityById(java.lang.Long)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableBpmModelEntityById(Long id) {
        final BpmModelEntity bpmModelEntity = bpmModelManager.get(id);
        if (0 == bpmModelEntity.getStatus()) {
            bpmModelEntity.setStatus(1);
            bpmConfBaseManager.save(bpmModelEntity);
        }
        return true;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowModelService#disableBpmModelEntityById(java.lang.Long)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableBpmModelEntityById(Long id) {

        final BpmModelEntity bpmModelEntity = bpmModelManager.get(id);
        if (1 == bpmModelEntity.getStatus()) {
            // 挂起流程版本
            workflowVersionService.suspendProcessByBpmModelId(id);
            // 禁用流程
            bpmModelEntity.setStatus(0);
            bpmConfBaseManager.save(bpmModelEntity);
        }

        return false;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowModelService#purview(com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowModelPurviewPostDTO,
     *      java.lang.String)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean purview(WorkflowModelPurviewPostDTO workflowModelPurviewDTO, String tenantId) {
        final List<BpmModelPurview> oldBpmModelPurviewList = getBpmModelPurviewListByBpmModelCode(
            workflowModelPurviewDTO.getBpmModelCode(), tenantId);
        if (null != oldBpmModelPurviewList) {
            bpmModelPurviewManager.removeAll(oldBpmModelPurviewList);
        }
        List<KeyValueDTO<Long, String>> objectList = workflowModelPurviewDTO.getDeptList();
        if (null != objectList) {
            for (final KeyValueDTO<Long, String> object : objectList) {
                final BpmModelPurview entity = new BpmModelPurview();
                entity.setBpmModelCode(workflowModelPurviewDTO.getBpmModelCode());
                entity.setBusinessCode(workflowModelPurviewDTO.getBusinessCode());
                entity.setObjectId(object.getId());
                entity.setParentId(object.getParentId());
                entity.setObjectName(object.getName());
                entity.setObjectType("d");
                entity.setTenantId(tenantId);
                bpmModelPurviewManager.save(entity);
            }
        }
        objectList = workflowModelPurviewDTO.getUserList();
        if (null != objectList) {
            for (final KeyValueDTO<Long, String> object : objectList) {
                final BpmModelPurview entity = new BpmModelPurview();
                entity.setBpmModelCode(workflowModelPurviewDTO.getBpmModelCode());
                entity.setBusinessCode(workflowModelPurviewDTO.getBusinessCode());
                entity.setObjectId(object.getId());
                entity.setParentId(object.getParentId());
                entity.setObjectName(object.getName());
                entity.setObjectType("u");
                entity.setTenantId(tenantId);
                bpmModelPurviewManager.save(entity);
            }
        }
        return true;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowModelService#getBpmModelPurviewListByBpmModelCode(java.lang.String,
     *      java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<BpmModelPurview> getBpmModelPurviewListByBpmModelCode(String bpmModelCode, String tenantId) {
        return bpmModelPurviewManager.find("from BpmModelPurview t where t.bpmModelCode=?0 and t.tenantId=?1",
            bpmModelCode, tenantId);
    }

}
