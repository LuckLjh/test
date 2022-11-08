package com.cescloud.saas.archive.service.modular.businessconfig.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.businessconfig.dto.BusinessModelDefineDTO;
import com.cescloud.saas.archive.api.modular.businessconfig.entity.BusinessModelDefine;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.List;
import java.util.Map;

/**
 * @author liwei
 */
public interface BusinessModelDefineService extends IService<BusinessModelDefine> {

	/**
	 * 根据模板类型查询业务表字段定义
	 *
	 * @param modelType 模板类型
	 * @param keyword   检索关键字
	 * @return 结果集
	 */
	List<BusinessModelDefine> getBusinessModelDefines(Integer modelType, String keyword);


	List<BusinessModelDefine> getBusinessModelDefinesAll(Integer modelType);

	/**
	 * 根据模板类型分页查询业务表字段定义(应用启动完成后查询，无租户信息)
	 *
	 * @param current   当前页
	 * @param size      每页的条目数
	 * @param modelType 模板类型
	 * @return
	 */
	IPage<BusinessModelDefine> getPageBusinessModelDefines(long current, long size, Integer modelType);

	/**
	 * 根据模板类型和租户id分页查询业务表字段定义
	 * @param current 当前页
	 * @param size 每页的条目数
	 * @param tenantId 租户id
	 * @param modelType 模板类型
	 * @return
	 */
	IPage<BusinessModelDefine> getPageTenantBusinessModelDefines(long current, long size, Long tenantId, Integer modelType);

	/**
	 * 新增业务字段
	 *
	 * @param businessModelDefinedto 业务模板定义DTO
	 * @return R
	 */
	boolean saveBusinessModelDefine(BusinessModelDefineDTO businessModelDefinedto) throws ArchiveBusinessException;

	/**
	 * 修改业务字段
	 *
	 * @param businessModelDefinedto 业务模板定义DTO
	 * @return R
	 */
	boolean updateBusinessModelDefine(BusinessModelDefineDTO businessModelDefinedto) throws ArchiveBusinessException;

	/**
	 * 删除业务字段
	 *
	 * @param id 主键ID
	 * @return R
	 */
	boolean deleteBusinessModelDefineById(Long id) throws ArchiveBusinessException;

	/**
	 * 启用租户时，新建业务表（利用表、归档表......）
	 *
	 * @param tenantId 租户id
	 * @return boolean
	 */
	boolean createTable(Long tenantId) throws ArchiveBusinessException;

	/**
	 * 根据 模板类型 字段名称 获取 字典值
	 *
	 * @param modelType       模板ID
	 * @param metadataEnglish 英文字段名称
	 * @return List<Map < String, String>>
	 */
	List<Map<String, String>> getEditFormSelectOption(Integer modelType, String metadataEnglish);


	/**
	 * 设置 英文
	 * 原为私有方法，因 DynamicModelDefineService 需要放出
	 * @param businessModelDefine
	 * @return
	 * @throws ArchiveBusinessException
	 */
	BusinessModelDefine setMetadataEnglish(BusinessModelDefine businessModelDefine) throws ArchiveBusinessException;


	/**
	 * 根据 modelType 从模板表（apma_business_model_template）中获取所有的记录
	 * 王谷华 2021-03-31 为将雪虎 BusinessModelDefineServiceImpl createTable 做区分而做
	 * @param modelType
	 * @return
	 */
	List<BusinessModelDefine> getBusinessModelTemplateListByModelType(Integer modelType);


	/**
	 * 根据动态表参数删除表定义
	 * @param modelType
	 * @param modelCode
	 * @return
	 */
	int deleteByDynamicModel(Integer modelType, String modelCode);
}
