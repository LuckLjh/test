/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.service.impl</p>
 * <p>文件名:WorkflowCategoryServiceImpl.java</p>
 * <p>创建时间:2019年10月15日 下午2:38:36</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.service.impl;

import com.cescloud.saas.archive.service.modular.workflow.service.WorkflowCategoryService;
import com.cesgroup.bpm.persistence.domain.BpmModelCategory;
import com.cesgroup.bpm.persistence.manager.BpmModelManager;
import com.cesgroup.bpm.persistence.manager.ModelCategoryManager;
import com.cesgroup.core.mapper.BeanMapper;
import com.cesgroup.core.query.PropertyFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年10月15日
 */
@Component
public class WorkflowCategoryServiceImpl implements WorkflowCategoryService {

    @Autowired
    private ModelCategoryManager modelCategoryManager;

    @Autowired
    private BpmModelManager bpmModelManager;

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowCategoryService#getBmpModelCategoryListByTenantId(java.lang.String)
     */
    @Override
    public List<BpmModelCategory> getBmpModelCategoryListByTenantId(Long tenantId) {
        Assert.notNull(tenantId, "tenantId不能为空");
        final Map<String, Object> parameterMap = new LinkedHashMap<String, Object>();
        final List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQS_tenantId", String.valueOf(tenantId)));
        propertyFilters.add(new PropertyFilter("EQI_type", "1"));
        return modelCategoryManager.find(propertyFilters);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowCategoryService#saveBmpModelCategory(com.cesgroup.bpm.persistence.domain.BpmModelCategory,
     *      java.lang.String)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BpmModelCategory saveBmpModelCategory(BpmModelCategory modelCategory, Long tenantId) {
        Assert.notNull(tenantId, "tenantId不能为空");
        Assert.isTrue(checkUnique(modelCategory, String.valueOf(tenantId)), "目录名称重复，请检查");
        BpmModelCategory dest = null;
        final Long id = modelCategory.getId();
        if (id != null) { // 修改
            dest = modelCategoryManager.get(id);
            new BeanMapper().copy(modelCategory, dest);
        } else { // 新建
            dest = modelCategory;
            dest.setPriority(getMaxSortNo(tenantId.toString()));
            dest.setTenantId(String.valueOf(tenantId));
        }
        modelCategoryManager.save(dest);
        return dest;
    }

    private Integer getMaxSortNo(String tenantId) {
        Integer maxSort = 1;

        final Object max = modelCategoryManager
            .findUnique("select max(b.priority) from BpmModelCategory b where b.tenantId = ?0", tenantId);

        if (null != max) {
            maxSort = Integer.parseInt(max.toString()) + 1;
        }

        return maxSort;
    }

    private boolean checkUnique(BpmModelCategory bpmModelCategory, String tenantId) {
        BpmModelCategory entity = null;
        String hql = null;
        if (bpmModelCategory.getId() != null) {
            hql = " from BpmModelCategory b where b.name = ?0 and b.parentId = ?1 and b.tenantId = ?2 and b.id not in (?3)";
            entity = modelCategoryManager.findUnique(hql, bpmModelCategory.getName(),
                bpmModelCategory.getParentId(), tenantId, bpmModelCategory.getId());
        } else {
            hql = " from BpmModelCategory b where b.name = ?0 and b.parentId = ?1 and b.tenantId = ?2";
            entity = modelCategoryManager.findUnique(hql, bpmModelCategory.getName(),
                bpmModelCategory.getParentId(), tenantId);
        }
        return entity == null;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowCategoryService#getBmpModelCategoryById(java.lang.Long)
     */
    @Override
    public BpmModelCategory getBmpModelCategoryById(Long id) {
        Assert.notNull(id, "id不能为空");
        return modelCategoryManager.get(id);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowCategoryService#removeBmpModelCategoryById(java.lang.Long)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeBmpModelCategoryById(Long id) {
        Assert.notNull(id, "id不能为空");
        Assert.isTrue(checkRemoveBmpModelCategory(id), "该目录下有模型，不能删除");
        modelCategoryManager.removeById(id);
        return true;
    }

    private boolean checkRemoveBmpModelCategory(Long categoryId) {
        final BpmModelCategory bpmModelCategory = new BpmModelCategory(categoryId);
        final Integer count = bpmModelManager.getCount(
            "select count(*) from BpmModelEntity b where b.modelCategory = ?0 ",
            bpmModelCategory);
        return 0 == count;
    }
}
