
package com.cescloud.saas.archive.service.modular.archiveconfig.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedListMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveList;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 档案列表配置
 *
 * @author liudong1
 * @date 2019-04-18 21:12:08
 */
public interface ArchiveListMapper extends BaseMapper<ArchiveList> {

	List<DefinedListMetadata> listOfDefined(@Param("storageLocate") String storageLocate,@Param("moduleId") Long moduleId,@Param("userId") Long userId);

	List<DefinedListMetadata> listOfBaseDefined(String storageLocate);

	List<DefinedListMetadata> listOfUnDefined(@Param("storageLocate") String storageLocate,@Param("moduleId") Long moduleId,@Param("userId") Long userId);

	List<DefinedListMetadata> listOfBaseUnDefined(String storageLocate);


	/**
	 * 专题的列表配置----------------------
	 * */

	/**
	 * @param storageLocate
	 * @param moduleId
	 * @param userId
	 * @return
	 */
	List<DefinedListMetadata> listSpecialOfDefined(@Param("storageLocate") String storageLocate,@Param("moduleId") Long moduleId,@Param("userId") Long userId);


	/**
	 *
	 * @param storageLocate
	 * @param moduleId
	 * @param userId
	 * @param moduleType
	 * @param moduleCode
	 * @return
	 */
	List<DefinedListMetadata> listSpecialOfUnDefined(@Param("storageLocate") String storageLocate,
			@Param("moduleId") Long moduleId,@Param("userId") Long userId,@Param("moduleType") Integer moduleType,@Param("moduleCode") String moduleCode);


	/**
	 * 建专题时获取默认显示的字段
	 * @param storageLocate
	 * @param moduleType
	 * @param moduleCode
	 * @return
	 */
	List<DefinedListMetadata> listDefaultSpecialToSave(@Param("storageLocate") String storageLocate,@Param("moduleType") Integer moduleType,@Param("moduleCode") String moduleCode);
}
