
package com.cescloud.saas.archive.service.modular.archiveconfig.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedColumnRuleMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveLinkColumnRuleMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.LinkColumnRule;
import com.cescloud.saas.archive.service.modular.common.core.util.R;

import java.util.List;
import java.util.Map;


/**
 * 挂接字段组成规则
 *
 * @author liudong1
 * @date 2019-05-14 11:15:33
 */
public interface LinkColumnRuleService extends IService<LinkColumnRule> {
	/**
	 * 已定义的字段列表
	 * @param storageLocate 存储表名
	 * @param linkLayerId 层次ID
	 * @return
	 */
	List<DefinedColumnRuleMetadata> listOfDefined(String storageLocate, Long linkLayerId);

	/**
	 * 未定义的字段列表
	 * @param storageLocate 存储表名
	 * @param linkLayerId 层次ID
	 * @return
	 */
	List<DefinedColumnRuleMetadata> listOfUnDefined(String storageLocate, Long linkLayerId);

	/**
	 * 根据存储表名和层次ID获取层次名称
	 * @param storageLocate
	 * @param linkLayerId
	 * @return
	 */
	String getLinkLayerName(String storageLocate, Long linkLayerId);

	/**
	 * 保存挂接字段组成规则配置
	 * @param saveLinkColumnRuleMetadata
	 * @return
	 */
	R saveFileLinkColumnRule(SaveLinkColumnRuleMetadata saveLinkColumnRuleMetadata);

	/**
	 * 根据存储表名删除挂接字段组成规则配置
	 * @param storageLocate 存储表名
	 */
	boolean deleteByStorageLocate(String storageLocate);

	void copyByStorageLocate(String srcStorageLocate, String destStorageLocate, Map<Long,Long> srcDestMetadataMap, Map<Long,Long> srcDestLinkLayerIdMap);
}
