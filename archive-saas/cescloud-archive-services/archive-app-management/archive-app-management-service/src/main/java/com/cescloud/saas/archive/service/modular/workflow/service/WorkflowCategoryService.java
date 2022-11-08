/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.service</p>
 * <p>文件名:WorkflowCategoryService.java</p>
 * <p>创建时间:2019年10月15日 下午1:37:03</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.service;

import com.cesgroup.bpm.persistence.domain.BpmModelCategory;

import java.util.List;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年10月15日
 */
public interface WorkflowCategoryService {

    /**
     * 获取工作流模型目录
     *
     * @param tenantId
     *            租户ID
     * @return
     */
    List<BpmModelCategory> getBmpModelCategoryListByTenantId(Long tenantId);

    /**
     * 保存模型目录
     *
     * @param modelCategory
     *            封装模型目录信息的对象
     * @param tenantId
     *            租户ID
     * @return BpmModelCategory
     */
    BpmModelCategory saveBmpModelCategory(BpmModelCategory modelCategory, Long tenantId);

    /**
     * 根据模型目录ID获取模型目录信息
     *
     * @param id
     *            模型目录ID
     * @return BpmModelCategory
     */
    BpmModelCategory getBmpModelCategoryById(Long id);

    /**
     * 根据模型目录ID删除模型目录信息
     *
     * @param id
     *            模型目录ID
     * @return boolean
     */
    boolean removeBmpModelCategoryById(Long id);
}
