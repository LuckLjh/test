
package com.cescloud.saas.archive.service.modular.businessconfig.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.businessconfig.dto.DynamicBusinessModelDefineDTO;
import com.cescloud.saas.archive.api.modular.businessconfig.entity.DynamicModelDefine;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.List;


/**
 * 动态表字段对应
 *
 * @author 王谷华
 * @date 2021-04-01 16:39:49
 */
public interface DynamicModelDefineService extends IService<DynamicModelDefine> {


	/**
	 * 根据动态表获取 BusinessModelDefine
	 *
	 * @param modelType
	 * @param tenantId
	 * @param code
	 * @param keyword
	 * @return
	 */
	List<DynamicBusinessModelDefineDTO> getBusinessModelDefinesByDynamic(Integer modelType, Long tenantId, String code, String keyword);


	/**
	 * 获取动态表字段
	 * @param modelType
	 * @param tenantId
	 * @param code
	 * @param keyword
	 * @return
	 */
	List<String> getDynamicFields(Integer modelType, Long tenantId, String code, String keyword);

	/**
	 * 新增动态表与 业务模板定义
	 * @param dynamicBusinessModelDefineDTO
	 * @return
	 * @throws ArchiveBusinessException
	 */
	boolean saveDynamicModelDefine(DynamicBusinessModelDefineDTO dynamicBusinessModelDefineDTO) throws ArchiveBusinessException;


	/**
	 * 只更新 业务模板定义
	 * @param dynamicBusinessModelDefineDTO
	 * @return
	 * @throws ArchiveBusinessException
	 */
	boolean updateDynamicModelDefine(DynamicBusinessModelDefineDTO dynamicBusinessModelDefineDTO) throws ArchiveBusinessException;

	/**
	 * 删除动态表与 业务模板定义
	 * @param dynamicBusinessModelDefineDTO
	 * @return
	 * @throws ArchiveBusinessException
	 */
	boolean deleteDynamicModelDefineById(Long id) throws ArchiveBusinessException;

	/**
	 * 创建动态表
	 * @param code
	 * @param modelType
	 * @param fondsCode
	 * @return
	 */
	boolean createTable(String code,Integer modelType,String fondsCode);


	/**
	 * 删除动态表
	 * @param code
	 * @param modelType
	 * @param fondsCode
	 * @return
	 */
	boolean dropTable(String code,Integer modelType);

	/**
	 * 获取所有字段信息
	 *
	 * @param modelType 业务类型
	 * @param modelCode 业务编码
	 * @return 字段信息
	 */
	List<DynamicBusinessModelDefineDTO> getAllBusinessModelDefines(Integer modelType, String modelCode);
}
