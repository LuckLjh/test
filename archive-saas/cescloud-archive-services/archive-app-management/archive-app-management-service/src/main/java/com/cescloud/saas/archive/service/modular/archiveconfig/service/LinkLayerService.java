
package com.cescloud.saas.archive.service.modular.archiveconfig.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.service.modular.archiveconfig.dto.LinkLayerTreeNode;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveLinkColumnRuleMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.LinkLayer;
import com.cescloud.saas.archive.api.modular.archivedict.dto.CopyPostDTO;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * 挂接目录规则配置
 *
 * @author liudong1
 * @date 2019-05-14 10:33:56
 */
public interface LinkLayerService extends IService<LinkLayer> {

	/**
	 * 修改或新增挂接规则配置根文件夹
	 *
	 * @param linkLayer
	 * @return
	 * @throws ArchiveBusinessException
	 */
	LinkLayer saveRoot(LinkLayer linkLayer) throws ArchiveBusinessException;

	/**
	 * 保存或修改挂接层次字段组成规则
	 *
	 * @param saveLinkColumnRuleMetadata
	 * @return
	 * @throws ArchiveBusinessException
	 */
	R saveDirLinkColumnRule(SaveLinkColumnRuleMetadata saveLinkColumnRuleMetadata) throws ArchiveBusinessException;

	/**
	 * 保存文件命名字段组成规则
	 *
	 * @param saveLinkColumnRuleMetadata
	 * @return
	 * @throws ArchiveBusinessException
	 */
	R saveFileLinkColumnRule(SaveLinkColumnRuleMetadata saveLinkColumnRuleMetadata) throws ArchiveBusinessException;

	/**
	 * 保存文件下载命名设置规则
	 *
	 * @param saveLinkColumnRuleMetadata
	 * @return
	 * @throws ArchiveBusinessException
	 */
	boolean saveDocLinkColumnRule(SaveLinkColumnRuleMetadata saveLinkColumnRuleMetadata) throws ArchiveBusinessException;

	/**
	 * 保存批量挂接设置规则
	 *
	 * @param saveLinkColumnRuleMetadata
	 * @return
	 * @throws ArchiveBusinessException
	 */
	R saveBatchAttachLinkColumnRule(SaveLinkColumnRuleMetadata saveLinkColumnRuleMetadata) throws ArchiveBusinessException;

	/**
	 * 挂接根文件夹
	 *
	 * @param typeCode     档案类型编码
	 * @param templateTableId 档案模板表id
	 * @param moduleId 模块id
	 * @return
	 * @throws ArchiveBusinessException
	 */
	LinkLayer getRoot(String typeCode, Long templateTableId,Long moduleId) throws ArchiveBusinessException;
	/**
	 * 挂接根文件夹 （业务使用）
	 *
	 * @param typeCode     档案类型编码
	 * @param templateTableId 档案模板表id
	 * @param moduleId 模块id
	 * @return
	 * @throws ArchiveBusinessException
	 */
	LinkLayer getBusinessRoot(String typeCode, Long templateTableId,Long moduleId) throws ArchiveBusinessException;

	/**
	 * 得到租户下所有的挂接根文件夹
	 *
	 * @param tenantId 租户id
	 * @return
	 */
	List<LinkLayer> getRootsByTenantId(Long tenantId);

	/**
	 * 挂接层级树
	 *
	 * @param storageLocate
	 * @return
	 */
	List<LinkLayerTreeNode> tree(String storageLocate,Long moduleId);

	/**
	 * 删除挂接层次
	 *
	 * @param id 文件夹id
	 * @return
	 */
	R deleteDirLink(Long id);

	/**
	 * 获取文件命名
	 *
	 * @param storageLocate 存储表名
	 * @return
	 */
	LinkLayer getFile(String storageLocate,Long moduleId);

	/**
	 * 获取文件下载命名设置
	 *
	 * @param storageLocate 存储表名
	 * @return
	 */
	LinkLayer getDoc(String storageLocate,Long moduleId);

	List<LinkLayer> getLinkRule(String storageLocate,Long moduleId);

	/**
	 * 根据表名获取文件名规则
	 *
	 * @param storageLocate 存储表名
	 * @param moduleId 模块id
	 * @return com.cescloud.saas.archive.api.modular.archiveconfig.entity.LinkLayer
	 */
	LinkLayer getFileNameLinkRule(String storageLocate,Long moduleId);

	/**
	 * 根据表名获取文件下载命名规则
	 *
	 * @param storageLocate 存储表名
	 * @return com.cescloud.saas.archive.api.modular.archiveconfig.entity.LinkLayer
	 */
	LinkLayer getDocNameLinkRule(String storageLocate,Long moduleId);

	/**
	 * 根据表名获取批量挂接规则
	 *
	 * @param storageLocate 存储表名
	 * @return com.cescloud.saas.archive.api.modular.archiveconfig.entity.LinkLayer
	 */
	LinkLayer getBatchAttachByTable(String storageLocate, Long moduleId);

	/**
	 * 根据档案类型获取批量挂接规则
	 *
	 * @param typeCode 档案类型
	 * @return com.cescloud.saas.archive.api.modular.archiveconfig.entity.LinkLayer
	 */
	LinkLayer getBatchAttachByTypeCode(String typeCode, Long moduleId);

	/**
	 * 根据存储表名删除挂接目录规则配置
	 *
	 * @param storageLocate 存储表名
	 */
	boolean deleteByStorageLocate(String storageLocate);

	/**
	 * 挂接导航条
	 *
	 * @param id 目录id
	 * @return
	 */
	Collection<LinkLayer> getParentList(Long id);

	/**
	 * 根据模块id和storageLocate 删除 配置信息
	 * @param storageLocate 物理表名
	 * @param moduleId 模块id
	 * @return
	 */
	Boolean removeByModuleId(String storageLocate,Long moduleId);

	/**
	 * 复制到另一模块
	 * @param copyPostDTO
	 * @return
	 */
	R copy(CopyPostDTO copyPostDTO);

	void copyByStorageLocate(String srcStorageLocate, String destStorageLocate, Map<Long,Long> srcDestMetadataMap);

	/**
	 * 获取服务端挂接默认文件夹
	 * @return 挂接默认文件夹
	 */
	Map<String,Object> getBatchAttachDir();
}
