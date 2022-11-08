/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.service.impl</p>
 * <p>文件名:WorkflowMonitorServiceImpl.java</p>
 * <p>创建时间:2019年11月13日 下午2:42:09</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.service.impl;

import com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowSearchDTO;
import com.cescloud.saas.archive.common.search.Page;
import com.cescloud.saas.archive.service.modular.workflow.service.WorkflowMonitorService;
import com.cesgroup.humantask.persistence.manager.TaskInfoManager;
import com.cesgroup.workflow.persistence.domain.BusinessModel;
import com.cesgroup.workflow.persistence.manager.BusinessModelManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author qiucs
 * @version 1.0.0 2019年11月13日
 */
@Component
public class WorkflowMonitorServiceImpl implements WorkflowMonitorService {

	@Autowired
	private TaskInfoManager taskInfoManager;

	@Autowired
	private BusinessModelManager businessModelManager;

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowMonitorService#getTenantBusinessModelList(Long) ()
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<BusinessModel> getTenantBusinessModelList(Long tenantId) {
        return businessModelManager.find("from BusinessModel t where t.tenantId=?0 order by t.sortNo",
            tenantId.toString());
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowMonitorService#getTenantProcessInstancePageByTenantId(com.cescloud.saas.archive.common.search.Page,
     *      java.lang.Long,
     *      com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowSearchDTO)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Page<?> getTenantProcessInstancePageByTenantId(Page<?> page, Long tenantId,
        WorkflowSearchDTO searchDTO) {
        final com.cesgroup.core.page.Page p = taskInfoManager.findTenantProcessList(tenantId.toString(),
            (int) page.getCurrent(),
            (int) page.getSize(), searchDTO.getStatus(), searchDTO.getBusinessCode(), searchDTO.getStartTime(),
            searchDTO.getEndTime(),searchDTO.getWorkflowName());
        page.setTotal(p.getTotalCount());
        page.setRecords((List) p.getResult());
        return page;
    }

}
