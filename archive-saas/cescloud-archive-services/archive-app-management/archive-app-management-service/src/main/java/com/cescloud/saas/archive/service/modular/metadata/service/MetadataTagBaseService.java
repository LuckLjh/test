
package com.cescloud.saas.archive.service.modular.metadata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataTagBase;


/**
 * 基础标签
 *
 * @author liudong1
 * @date 2019-09-16 10:14:57
 */
public interface MetadataTagBaseService extends IService<MetadataTagBase> {

	void insertIntoMetadataTagFromBase(Long tenantId);

}
