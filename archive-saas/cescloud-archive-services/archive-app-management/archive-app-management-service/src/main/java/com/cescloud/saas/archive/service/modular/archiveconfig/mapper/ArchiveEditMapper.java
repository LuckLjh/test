
package com.cescloud.saas.archive.service.modular.archiveconfig.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedEditMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveEdit;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 档案录入配置
 *
 * @author liudong1
 * @date 2019-04-18 16:06:51
 */
public interface ArchiveEditMapper extends BaseMapper<ArchiveEdit> {

	List<DefinedEditMetadata> listOfDefined(@Param("storageLocate") String storageLocate,@Param("moduleId") Long moduleId);

	List<DefinedEditMetadata> listOfBaseDefined(String storageLocate);

	List<DefinedEditMetadata> listOfUnDefined(@Param("storageLocate") String storageLocate,@Param("moduleId") Long moduleId);

	List<DefinedEditMetadata> listOfBaseUnDefined(String storageLocate);
}
