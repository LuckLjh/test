/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.service</p>
 * <p>文件名:WorkflowMonitorService.java</p>
 * <p>创建时间:2019年11月13日 下午2:23:51</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.service;

import com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowSearchDTO;
import com.cescloud.saas.archive.common.search.Page;
import com.cesgroup.workflow.persistence.domain.BusinessModel;

import java.util.List;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年11月13日
 */
public interface WorkflowMonitorService {

    /**
     * 获取所有工作流业务模型
     *
     * @param tenantId
     *            租户ID
     * @return
     */
    List<BusinessModel> getTenantBusinessModelList(Long tenantId);

    /**
     * 获取租户下所有的流程实例列表（不包含发起节点未提交的流程）
     *
     * @param page
     *            分页信息
     * @param tenantId
     *            租户ID
     * @param searchDTO
     *            条件
     * @return
     */
    Page<?> getTenantProcessInstancePageByTenantId(Page<?> page, Long tenantId, WorkflowSearchDTO searchDTO);

}
