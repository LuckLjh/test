
package com.cescloud.saas.archive.service.modular.metadata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.metadata.dto.MetadataBaseDTO;
import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataBase;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 基础元数据
 *
 * @author liudong1
 * @date 2019-03-27 14:33:25
 */
public interface MetadataBaseMapper extends BaseMapper<MetadataBase> {
	/**
	 * 检查唯一
	 * @param tableName
	 * @param params
	 * @return
	 */
	Long checkUnique(@Param("tableName") String tableName, @Param("params") Map<String, Object> params);

	/**
	 *
	 * @return
	 */
	List<MetadataBaseDTO> getMetadataBaseDTOList(@Param("params") List<String> classTypeList);

}
