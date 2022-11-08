
package com.cescloud.saas.archive.service.modular.archivetype.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archivetree.dto.FondsArchiveTypeSyncTreeNode;
import com.cescloud.saas.archive.api.modular.archivetype.dto.*;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveType;
import com.cescloud.saas.archive.common.constants.NodeTypeEnum;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.ArrayList;
import java.util.List;


/**
 * 档案门类
 *
 * @author liudong1
 * @date 2019-03-18 09:14:11
 */
public interface ArchiveTypeService extends IService<ArchiveType> {

	/**
	 * 根据id获取档案类型
	 *
	 * @param id 主键id
	 * @return com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveType
	 */
	ArchiveType getArchiveTypeById(Long id);

	/**
	 * 获取异步档案门类树
	 *
	 * @param parentId 父节点ID，跟节点为0
	 * @param nodeType 节点类型
	 * @return
	 * @throws ArchiveBusinessException
	 */
	List<ArchiveTypeTreeNode> getTypeTreeNodes(Long parentId, NodeTypeEnum nodeType) throws ArchiveBusinessException;
	/**
	 * 获取范围全宗内的异步档案门类树
	 *
	 * @param parentId 父节点ID，跟节点为0
	 * @param nodeType 节点类型
	 * @param fondsCodes 范围全宗
	 * @return
	 * @throws ArchiveBusinessException
	 */
	List<ArchiveTypeTreeNode> getTypeTreeNodes(Long parentId, NodeTypeEnum nodeType,List<String> fondsCodes) throws ArchiveBusinessException;

	/**
	 * 获取四性检测配置的异步档案门类树
	 *
	 * @param parentId 父节点ID，跟节点为0
	 * @param nodeType 节点类型
	 * @param fondsCodes 范围全宗
	 * @return
	 * @throws ArchiveBusinessException
	 */
	List<ArchiveTypeTreeNode> getTypeTreeNodesForFourCheck(Long parentId, NodeTypeEnum nodeType,List<String> fondsCodes) throws ArchiveBusinessException;
	/**
	 * 修改档案门类
	 *
	 * @param type
	 * @return
	 * @throws ArchiveBusinessException
	 */
	ArchiveType updateArchiveType(ArchiveType type) throws ArchiveBusinessException;

	/**
	 * 新增档案门类
	 *
	 * @param type
	 * @return
	 * @throws ArchiveBusinessException
	 */
	ArchiveType saveArchiveType(ArchiveType type) throws ArchiveBusinessException;

	void bingdingTagAndDict(ArchiveTable archiveTable);

	/**
	 * 通过id删除档案门类
	 *
	 * @param id 档案门类id
	 * @return
	 */
	R deleteArchiveType(Long id) throws ArchiveBusinessException;

	/**
	 * 根据档案门类编码获取档案门类
	 *
	 * @param typeCode 档案门类编码
	 * @return
	 * @throws ArchiveBusinessException
	 */
	ArchiveType getByTypeCode(String typeCode) throws ArchiveBusinessException;

	/**
	 * 根据档案门类编码和租户id获取档案门类
	 *
	 * @param typeCode 档案门类编码
	 * @param tenantId 租户id
	 * @return
	 * @throws ArchiveBusinessException
	 */
	ArchiveType getByTypeCode(String typeCode, Long tenantId) throws ArchiveBusinessException;

	/**
	 * 获取节点类型为档案类型节点的档案门类集合
	 * @param fondsCodes 全宗范围 例如：[G， XXXX]
	 * @return com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveType
	 */
	List<ArchiveTypeDTO> getArchiveTypeRelationTree(List<String> fondsCodes);

	/**
	 * 获获取节点类型为档案类型节点的档案门类树集合 考虑全宗列表范围
	 * @param fondsCodes 全宗范围 例如：[G， XXXX]
	 * @return ArchiveTypeChildTreeNode
	 */
	List<ArchiveTypeChildTreeNode> getArchiveTypeRelationFondsTree(List<String> fondsCodes);
	/**
	 * 初始化门类
	 *
	 * @param templateId 模板id
	 * @param tenantId   租户id
	 * @return
	 * @throws ArchiveBusinessException
	 */
	R initializeHandle(Long templateId, Long tenantId) throws ArchiveBusinessException;

	/**
	 * 得到档案门类树
	 *
	 * @param isCatalog 是否目录 还是 全文
	 * @return
	 */
	List<ArchiveTypeTableSyncTreeNode> getArchiveTypeTableSyncTree(Boolean isCatalog, List<String> fondsCodes);

	/**
	 * 档案门类表同步树
	 *
	 * @return
	 */
	List<ArchiveTypeTableTree> getArchiveTypeTableSyncTree();

	/**
	 * 检查档案类型模板是否被使用
	 *
	 * @param templateTypeId 档案类型模板ID
	 * @return
	 */
	boolean checkTemplateType(Long templateTypeId);

	/**
	 * 获取档案门类信息
	 * @param tenantId 租户id
	 * @return
	 */
	List<ArrayList<String>> getArchivesClassInfor(Long tenantId);

	/**
	 * 复制档案类型
	 * @param archiveType
	 * @return
	 */
	Boolean copyArchiveType(ArchiveTypeCopyPostDTO archiveType) throws ArchiveBusinessException;

    List<ArchiveType> getArchiveTypes();

	/**
	 * 排序
	 * @param archiveTypeOrderDTO 排序实体
	 * @return
	 */
	Boolean archiveTypeOrder(ArchiveTypeOrderDTO archiveTypeOrderDTO);

	/**
	 * 获取列表主要信息 列表
	 * @param parentId
	 * @param result
	 */
	List<String> getAllMainInfoForChildren(Long parentId,List<String> result);

	List<FondsArchiveTypeSyncTreeNode> getFondsTypeTree(Long id, String fondsCode);

	List<FondsArchiveTypeSyncTreeNode> getFondsTypeTableTree(Long id, String fondsCode, String nodeClass, String typeCode, Integer showDocument);

	List<FondsArchiveTypeSyncTreeNode> getFondsAllTypeTableTree(String fondsCode);

	List<ArchiveType>  getTypeNameByTableIds( Long tenantId,String ids);

	void updateArchiveTypeTree(String fondsName , String fondsCode);
	/**
	 * 根据档案门类编码和租户id获取档案门类
	 *
	 * @param typeName 档案门类名称
	 * @param tenantId 租户id
	 * @return
	 * @throws ArchiveBusinessException
	 */
	ArchiveType getByTypeName(String typeName, Long tenantId) throws ArchiveBusinessException;

	List<FondsArchiveTypeSyncTreeNode> getFondsTreeNode1(String fondsCode);
}
