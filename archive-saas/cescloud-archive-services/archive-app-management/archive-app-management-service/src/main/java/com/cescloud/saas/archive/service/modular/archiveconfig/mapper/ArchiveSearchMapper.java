
package com.cescloud.saas.archive.service.modular.archiveconfig.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedSearchMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveSearch;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 档案检索配置
 *
 * @author liudong1
 * @date 2019-05-27 16:52:00
 */
public interface ArchiveSearchMapper extends BaseMapper<ArchiveSearch> {

	List<DefinedSearchMetadata> listOfDefined(@Param("storageLocate") String storageLocate, @Param("userId") Long userId,@Param("searchType") Integer searchType,@Param("moduleId") Long moduleId);

	List<DefinedSearchMetadata> listOfBaseDefined(String storageLocate);

	List<DefinedSearchMetadata> listOfUnDefined(@Param("storageLocate") String storageLocate, @Param("userId") Long userId,@Param("searchType") Integer searchType,@Param("moduleId") Long moduleId);

	List<DefinedSearchMetadata> listOfBaseUnDefined(String storageLocate);


	/**
	 * 专题未定义搜索配置
	 * @param storageLocate
	 * @param userId
	 * @param searchType
	 * @param moduleId
	 * @return
	 */
	List<DefinedSearchMetadata> listSpecialOfUnDefined(@Param("storageLocate") String storageLocate, @Param("userId") Long userId,@Param("searchType") Integer searchType,@Param("moduleId") Long moduleId,@Param("modelCode") String modelCode);

	/**
	 * 专题定义搜索配置
	 * @param storageLocate
	 * @param userId
	 * @param searchType
	 * @param moduleId
	 * @return
	 */
	List<DefinedSearchMetadata> listSpecialOfDefined(@Param("storageLocate") String storageLocate, @Param("userId") Long userId,@Param("searchType") Integer searchType,@Param("moduleId") Long moduleId);
}
