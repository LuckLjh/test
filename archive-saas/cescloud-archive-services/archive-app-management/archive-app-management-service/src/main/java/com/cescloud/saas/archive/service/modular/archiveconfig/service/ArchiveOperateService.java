
package com.cescloud.saas.archive.service.modular.archiveconfig.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveOperate;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveOperate;
import com.cescloud.saas.archive.service.modular.common.core.util.R;

import java.util.List;


/**
 * 档案字段组成规则
 *
 * @author liudong1
 * @date 2019-04-21 19:30:10
 */
public interface ArchiveOperateService extends IService<ArchiveOperate> {
	List<ArchiveOperate> getOperateRule(Long businessId);

	R saveOperateDefined(SaveOperate saveOperate);

	boolean deleteByStorageLocate(String storageLocate);
}
