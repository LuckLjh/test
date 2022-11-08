
package com.cescloud.saas.archive.service.modular.archiveconfig.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedRepeatMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveRepeatMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.MetadataCheckrepeat;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cache.annotation.CacheEvict;

import java.util.List;


/**
 * 查重设置
 *
 * @author liudong1
 * @date 2019-04-23 12:08:06
 */
public interface MetadataCheckrepeatService extends IService<MetadataCheckrepeat> {

	List<DefinedRepeatMetadata> listOfDefined(String storageLocate);

	List<DefinedRepeatMetadata> listOfUnDefined(String storageLocate);

	R saveReportDefined(SaveRepeatMetadata saveRepeatMetadata);

	@CacheEvict(allEntries = true)
	boolean deleteByStorageLocate(String storageLocate);
}
