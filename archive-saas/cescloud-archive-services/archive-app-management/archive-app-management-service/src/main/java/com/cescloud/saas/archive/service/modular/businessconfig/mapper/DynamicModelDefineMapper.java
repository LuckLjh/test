
package com.cescloud.saas.archive.service.modular.businessconfig.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.businessconfig.dto.DynamicBusinessModelDefineDTO;
import com.cescloud.saas.archive.api.modular.businessconfig.entity.DynamicModelDefine;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 动态表字段对应
 *
 * @author 王谷华
 * @date 2021-04-01 16:39:49
 */
public interface DynamicModelDefineMapper extends BaseMapper<DynamicModelDefine> {


	/**
	 * 根据动态表获取 BusinessModelDefine
	 * @param modelType
	 * @param modelCode
	 * @param tenantId
	 * @param keyword
	 * @return
	 */
	List<DynamicBusinessModelDefineDTO> getBusinessModelDefinesByDynamic(@Param("modelType") Integer modelType,
																		 @Param("modelCode") String modelCode, @Param("tenantId") Long tenantId, @Param("keyword") String keyword);

	int deleteByTypeAndCode(@Param("modelType") Integer modelType,
							@Param("modelCode") String modelCode, @Param("tenantId") Long tenantId);

	DynamicBusinessModelDefineDTO getDynamicBusinessModelDefineDTOByMetadataEnglish(@Param("modelType") Integer modelType,
																					@Param("modelCode") String modelCode, @Param("tenantId") Long tenantId, @Param("metadataEnglish") String metadataEnglish);

}
