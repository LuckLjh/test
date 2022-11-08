
package com.cescloud.saas.archive.service.modular.archiveconfig.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedRepeatMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.MetadataCheckrepeat;

import java.util.List;


/**
 * 查重设置
 *
 * @author liudong1
 * @date 2019-04-23 12:08:06
 */
public interface MetadataCheckrepeatMapper extends BaseMapper<MetadataCheckrepeat> {

	List<DefinedRepeatMetadata> listOfDefined(String storageLocate);

	List<DefinedRepeatMetadata> listOfBaseDefined(String storageLocate);

	List<DefinedRepeatMetadata> listOfUnDefined(String storageLocate);

	List<DefinedRepeatMetadata> listOfBaseUnDefined(String storageLocate);
}
