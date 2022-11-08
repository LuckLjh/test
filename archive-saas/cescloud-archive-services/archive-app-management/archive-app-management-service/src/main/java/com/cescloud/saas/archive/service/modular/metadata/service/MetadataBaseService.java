
package com.cescloud.saas.archive.service.modular.metadata.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataBase;
import com.cescloud.saas.archive.service.modular.common.core.util.R;

import java.util.List;
import java.util.Map;


/**
 * 基础元数据
 *
 * @author liudong1
 * @date 2019-03-27 14:33:25
 */
public interface MetadataBaseService extends IService<MetadataBase> {

	IPage<MetadataBase> page(IPage page, MetadataBase metadataBase);

//	/**
//	 * 批量拆入元数据字段
//	 * @param archiveTables
//	 */
//	void insertIntoMetadataFromBaseBatch(List<ArchiveTable> archiveTables);

	R checkUnique(String tableName, Map<String, Object> params, String columnChinese);

	List<MetadataBase> getSysMetadata();

	List<MetadataBase> getBakMetadata();
}
