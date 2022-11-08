package com.cescloud.saas.archive.service.modular.businessconfig.service.helper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cescloud.saas.archive.api.modular.businessconfig.entity.BusinessModelDefine;
import com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowBusinessMetadataDTO;
import com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowBusinessModelDTO;
import com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowBusinessModelSyncDTO;
import com.cescloud.saas.archive.common.constants.BoolEnum;
import com.cescloud.saas.archive.common.constants.ModelTypeEnum;
import com.cescloud.saas.archive.service.modular.businessconfig.async.AsyncUpdateFieldConfiguration;
import com.cescloud.saas.archive.service.modular.businessconfig.service.BusinessModelDefineService;
import com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 同步工作流帮助类
 *
 * @author liwei
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AsyncWorkflowServiceHelper {

	private final WorkflowOpenApiService workflowOpenApiService;

	@Autowired
	private BusinessModelDefineService businessModelDefineService;


	/**
	 * 同步工作流字段
	 *
	 * @return
	 */
	@Async(AsyncUpdateFieldConfiguration.ASYNC_EXECUTOR_NAME)
	public void businessSync(Integer modelType, Long tenantId, List<BusinessModelDefine> businessModelTemplateList) {
		if (CollUtil.isEmpty(businessModelTemplateList)){
			businessModelTemplateList = businessModelDefineService.list(Wrappers.<BusinessModelDefine>query().lambda().eq(BusinessModelDefine::getModelType, modelType).eq(BusinessModelDefine::getTenantId, tenantId));
		}
		//字段数据
		final List<WorkflowBusinessMetadataDTO> businessMetadataList = CollectionUtil.newArrayList();
		businessModelTemplateList.forEach(businessModelDefine -> {
			WorkflowBusinessMetadataDTO workflowBusinessMetadataDTO = new WorkflowBusinessMetadataDTO();
			BeanUtils.copyProperties(businessModelDefine, workflowBusinessMetadataDTO);
			//是否作为条件字段
			if (BoolEnum.YES.getCode().equals(businessModelDefine.getIsFilter())) {
				workflowBusinessMetadataDTO.setCondition(Boolean.TRUE);
			} else {
				workflowBusinessMetadataDTO.setCondition(Boolean.FALSE);
			}
			//是否为部门字段
			if (BoolEnum.YES.getCode().equals(businessModelDefine.getIsDept())) {
				workflowBusinessMetadataDTO.setDept(Boolean.TRUE);
			} else {
				workflowBusinessMetadataDTO.setDept(Boolean.FALSE);
			}
			workflowBusinessMetadataDTO.setTenantId(StrUtil.toString(businessModelDefine.getTenantId()));
			workflowBusinessMetadataDTO.setMetadataLength(businessModelDefine.getMetadataLength());
			businessMetadataList.add(workflowBusinessMetadataDTO);
		});

		//业务模型
		ModelTypeEnum modelTypeEnum = ModelTypeEnum.getEnum(modelType);
		WorkflowBusinessModelDTO businessModelDTO = new WorkflowBusinessModelDTO();
		BeanUtils.copyProperties(modelTypeEnum, businessModelDTO);
		businessModelDTO.setCommon(true);
		businessModelDTO.setTenantId(StrUtil.toString(tenantId));
		businessModelDTO.setSortNo(modelType);

		//组装业务模型同步DTO
		WorkflowBusinessModelSyncDTO businessModelSyncDTO = new WorkflowBusinessModelSyncDTO();
		businessModelSyncDTO.setBusinessModel(businessModelDTO);
		businessModelSyncDTO.setTenantId(StrUtil.toString(tenantId));
		businessModelSyncDTO.setBusinessMetadataList(businessMetadataList);

		//同步工作流字段
		workflowOpenApiService.businessSync(businessModelSyncDTO);
	}


}
