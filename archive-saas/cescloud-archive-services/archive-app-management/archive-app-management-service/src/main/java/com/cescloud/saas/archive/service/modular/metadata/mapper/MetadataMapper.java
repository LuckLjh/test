
package com.cescloud.saas.archive.service.modular.metadata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.metadata.dto.MetadataDTO;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 档案元数据
 *
 * @author liudong1
 * @date 2019-03-28 09:42:53
 */
public interface MetadataMapper extends BaseMapper<Metadata> {

	Integer getMaxEnglishNoByHidden(@Param("metadataEnglish") String metadataEnglish, @Param("tableId") Long tableId);

	void addColumn(@Param("tableName") String tableName, @Param("metadata") Metadata metadata);

	void modifyColumn(@Param("tableName") String tableName, @Param("metadata") Metadata metadata);

	void changeColumn(@Param("tableName") String tableName, @Param("oldColumn") String oldColumn, @Param("metadata") Metadata metadata);

	void dropColumn(@Param("tableName") String tableName, @Param("metadata") Metadata metadata);

	List<MetadataDTO> getMetadataDTOList(@Param("storageLocate") String storageLocate);

//	void aotuBindingTag(@Param("metadataTagLayer") MetadataTagLayer metadataTagLayer, @Param("archiveLayer") String archiveLayer);

	Integer saveBatch(@Param("list") List<Metadata> metadataList);

}
