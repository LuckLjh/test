
package com.cescloud.saas.archive.service.modular.archiveconfig.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedSortMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveSort;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 档案排序配置
 *
 * @author liudong1
 * @date 2019-04-18 21:18:05
 */
public interface ArchiveSortMapper extends BaseMapper<ArchiveSort> {

	List<DefinedSortMetadata> listOfDefined(@Param("storageLocate") String storageLocate,@Param("moduleId") Long moduleId,@Param("userId") Long userId);

	List<DefinedSortMetadata> listOfBaseDefined(String storageLocate);

	List<DefinedSortMetadata> listOfUnDefined(@Param("storageLocate") String storageLocate,@Param("moduleId") Long moduleId,@Param("userId") Long userId);

	List<DefinedSortMetadata> listOfBaseUnDefined(String storageLocate);


	/**
	 * 专题排序获取未使用的列
	 * @param storageLocate
	 * @param moduleId
	 * @param userId
	 * @param moduleType
	 * @param moduleCode
	 * @return
	 */
	List<DefinedSortMetadata> listSpecialOfUnDefined(@Param("storageLocate") String storageLocate,
													 @Param("moduleId") Long moduleId,@Param("userId") Long userId,@Param("moduleType") Integer moduleType,@Param("moduleCode") String moduleCode);

	/**
	 * 专题排序获取使用的列
	 * @param storageLocate
	 * @param moduleId
	 * @param userId
	 * @return
	 */
	List<DefinedSortMetadata> listSpecialOfDefined(@Param("storageLocate") String storageLocate,
													 @Param("moduleId") Long moduleId,@Param("userId") Long userId);


	/**
	 * 元数据排序未使用的列
	 * @param moduleId
	 * @param userId
	 * @return
	 */
	List<DefinedSortMetadata> listMetadataOfUnDefined(@Param("moduleId") Long moduleId,@Param("userId") Long userId);

	/**
	 * 元数据排序使用的列
	 * @param storageLocate
	 * @param moduleId
	 * @param userId
	 * @return
	 */
	List<DefinedSortMetadata> listMetadataOfDefined(@Param("storageLocate") String storageLocate,@Param("moduleId") Long moduleId,@Param("userId") Long userId);

}
