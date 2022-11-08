package com.cescloud.saas.archive.service.modular.workflow.mapper;

import org.apache.ibatis.annotations.Param;

public interface WorkflowMapper {

	/**
	 * 通过唯一标识更新流程的名字
	 * T_WF_BPM_CONF_BASE
	 */
	void updateNameByKey_Base(@Param("name") String name, @Param("key") String key);
	/**
	 * 通过唯一标识更新流程的名字
	 * T_WF_ACT_RE_PROCDEF
	 */
	void updateNameByKey_PROCDEF(@Param("name") String name, @Param("key") String key, @Param("tenantId") Long tenantId);


	void updateParallelStatus(@Param("processInstanceId") String processInstanceId, @Param("id") String id, @Param("tenantId") Long tenantId);

}
