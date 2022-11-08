
package com.cescloud.saas.archive.service.modular.archivetype.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.archivetype.dto.SourceDTO;
import com.cescloud.saas.archive.api.modular.archivetype.entity.MetadataSource;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 参照源数据表
 *
 * @author liwei
 * @date 2019-04-16 14:52:34
 */
public interface MetadataSourceMapper extends BaseMapper<MetadataSource> {
	List<SourceDTO> getMetaDataSources(@Param("storageLocate") String storageLocate, @Param("id") Long id);

	List<SourceDTO> getDefMetaDataSources(@Param("storageLocate") String storageLocate, @Param("id") Long id , @Param("metadataId") Long metadataId,@Param("moduleId") Long moduleId);

	/**
	 * 根据表名和目标字段id获取分组字段
	 * @param storageLocate 表名
	 * @param targetId 目标累加字段
	 * @return 分组字段集合
	 */
	List<SourceDTO> getMetadataSourcesByStorageAndTargetId(@Param("storageLocate") String storageLocate, @Param("targetId") Long targetId);
}
