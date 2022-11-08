
package com.cescloud.saas.archive.service.modular.archiveconfig.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.*;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveEditForm;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 档案表单定义内容
 *
 * @author liudong1
 * @date 2019-04-22 19:56:41
 */
public interface ArchiveEditFormService extends IService<ArchiveEditForm> {

	/**
	 * 查询档案表单定义内容
	 *
	 * @param storageLocate 存储表名
	 * @return
	 * @throws ArchiveBusinessException
	 */
	ArchiveEditForm getEditFormByStorageLocate(String storageLocate, Long moduleId) throws ArchiveBusinessException;

	/**
	 * 查询档案表单定义的文本框字段
	 *
	 * @param storageLocate 存储表名
	 * @return
	 */
	List<String> getEditFormColumnByStorageLocate(String storageLocate, Long moduleId);

	/**
	 * 初始化表单页面
	 *
	 * @param typeCode
	 * @param templateTableId
	 * @return
	 * @throws ArchiveBusinessException
	 */
	ArchiveEditForm initForm(String typeCode, Long templateTableId, Long moduleId) throws ArchiveBusinessException;

	/**
	 * 保存表单定义内容
	 *
	 * @param archiveEditForm
	 * @return
	 * @throws ArchiveBusinessException
	 */
	ArchiveEditForm saveEditForm(ArchiveEditForm archiveEditForm) throws ArchiveBusinessException;

	/**
	 * 查询档案表单定义的数据规则
	 *
	 * @param storageLocate 存储表名
	 * @param moduleId      模块id
	 * @return
	 * @throws ArchiveBusinessException
	 */
	AutovalueRuleDTO getRuleColumn(String storageLocate, Long moduleId, Integer compose) throws ArchiveBusinessException;

	/**
	 * 修改分组字段触发重新计算流水号值
	 *
	 * @param computeFlowNoDTO
	 * @return
	 */
	Map<String, Object> getFlownoValue(ComputeFlowNoDTO computeFlowNoDTO) throws ArchiveBusinessException;

	/**
	 * 获取表单初始化数据
	 *
	 * @param archiveInitFromDataDTO
	 * @return
	 * @throws ArchiveBusinessException
	 */
	Map<String, Object> initFormData(ArchiveInitFormDataDTO archiveInitFromDataDTO) throws ArchiveBusinessException;

	/**
	 * 根据表名删除表单定义内容
	 *
	 * @param storageLocate 存储表名
	 */
	boolean deleteByStorageLocate(String storageLocate);

	/**
	 * 租户初始化
	 *
	 * @param templateId 模板id
	 * @param tenantId   租户id
	 * @return
	 * @throws ArchiveBusinessException
	 */
	R initializeHandle(Long templateId, Long tenantId) throws ArchiveBusinessException;

	/**
	 * 根据关联关系触发条件获取字段规则
	 *
	 * @param storageLocate 存储条件
	 * @param type          触发条件 如save、update
	 * @return
	 * @throws ArchiveBusinessException
	 */
	Map<String, List<ColumnComputeRuleDTO>> getColumnRuleByType(Long moduleId, String storageLocate, String type) throws ArchiveBusinessException;

	/**
	 * 获取租户表单定义信息
	 *
	 * @param tenantId 租户id
	 * @return
	 */
	List<ArrayList<String>> getFormDefinitionInfo(Long tenantId) throws ArchiveBusinessException;

	/**
	 * 根据表名复制配置数据
	 *
	 * @param srcStorageLocate  源表名
	 * @param destStorageLocate 目标表名
	 */
	void copyByStorageLocate(String srcStorageLocate, String destStorageLocate);

	/**
	 * 查询字段绑定的下拉树
	 *
	 * @param treeType            树类型
	 * @param archiveTreeQueryDTO 下拉树查询条件
	 * @return List<ArchiveTreeResultDTO>
	 */
	List<ArchiveTreeResultDTO> getTreeByTreeType(Integer treeType, ArchiveTreeQueryDTO archiveTreeQueryDTO) throws ArchiveBusinessException;

	/**
	 * 计算最大的页号
	 * @param typeCode
	 * @param templateTableId
	 * @param ownerId
	 * @return
	 */
	Map<String, Object>  getMaxPageNoByOwnerId(String typeCode,Long templateTableId, Long ownerId) throws ArchiveBusinessException;
}
