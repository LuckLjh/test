package com.cescloud.saas.archive.service.modular.archivetype.service;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.ColumnComputeRuleDTO;
import com.cescloud.saas.archive.api.modular.archivetype.entity.InnerRelation;
import com.cescloud.saas.archive.common.constants.FormStatusEnum;
import com.cescloud.saas.archive.common.constants.InnerRelationForwardEnum;
import com.cescloud.saas.archive.service.modular.common.core.constant.enums.InnerRelationTypeEnum;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.List;

/**
 * @author liwei
 */
public interface RuleComputeService {

	/**
	 * 获取规则 向下取值
	 * @param innerRelationTypeEnumList 关联关系集合
	 * @param storageLocate 本级表名
	 * @param storageLocate 上级表名
	 * @param forward 关联方向：down：向下，up：向上
	 * @return
	 */
	List<InnerRelation> getRuleCompute(List<InnerRelationTypeEnum> innerRelationTypeEnumList, String storageLocate, String upStorageLocate,
									   Long moduleId, InnerRelationForwardEnum forward) throws ArchiveBusinessException;

	/**
	 * 根据表名获取父表名
	 * @param storageLocate 表名
	 * @return
	 */
	String getUpStorageLocate(String storageLocate);

	/**
	 * 根据表名获取子表名
	 * @param storageLocate
	 * @return
	 */
	List<String> getDownStorageLocate(String storageLocate);

	/**
	 * 根据触发类型转化规则
	 * @param formStatusEnum 触发类型
	 * @return
	 */
	List<ColumnComputeRuleDTO> convertInnerRelation(FormStatusEnum formStatusEnum, List<InnerRelation> innerRelationList);

}
