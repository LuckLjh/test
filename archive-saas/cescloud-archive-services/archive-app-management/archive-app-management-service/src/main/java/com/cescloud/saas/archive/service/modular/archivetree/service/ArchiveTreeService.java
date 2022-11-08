
package com.cescloud.saas.archive.service.modular.archivetree.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archivetree.dto.ArchiveTreeGetDTO;
import com.cescloud.saas.archive.api.modular.archivetree.dto.ArchiveTreePutDTO;
import com.cescloud.saas.archive.api.modular.archivetree.dto.FondsArchiveTreeSyncTreeNode;
import com.cescloud.saas.archive.api.modular.archivetree.dto.RenderTreeDTO;
import com.cescloud.saas.archive.api.modular.archivetree.entity.ArchiveTree;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 档案树定义
 *
 * @author qiucs
 * @date 2019-04-12 13:36:59
 */
public interface ArchiveTreeService extends IService<ArchiveTree> {

    /**
     * 档案树节点保存
     *
     * @param entity
     *            档案树节点对象
     * @return boolean
     */
    List<ArchiveTree> save(ArchiveTreePutDTO entity);

    /**
     * 更新全宗号
     *
     * @param oldFondsCode
     *            旧的全宗号
     * @param newFondsCode
     *            新的全宗号
     * @return boolean
     */
    boolean updateFondsCode(String oldFondsCode, String newFondsCode);

    /**
     * 设置默认树
     *
     * @param id
     *            档案树ID
     * @return
     */
    boolean setDefaultTree(Long id);

    Boolean bindingArchiveType(String archiveTypeCode);

    /**
     * 查询
     *
     * @param archiveTree
     *            档案树检索实体
     * @return
     */
    List<ArchiveTree> getTreeList(ArchiveTreeGetDTO archiveTree);

    /**
     * 分页查询
     *
     * @param page
     *            分页参数
     * @param archiveTree
     *            档案树检索实体
     * @return
     */
    IPage<List<ArchiveTree>> getTreePage(Page page, ArchiveTreeGetDTO archiveTree);

    /**
     * 根据父节点id获取子节点(联表查询档案门类分类标识)
     *
     * @param parentId
     *            父节点id
     * @return com.cescloud.saas.archive.api.modular.archivetree.entity.ArchiveTree
     */
    List<ArchiveTree> getArchiveTreeByParentId(Long parentId);

    /**
     * 初始化档案树
     *
     * @param templateId
     *            模板id
     * @param tenantId
     *            租户id
     * @return
     * @throws ArchiveBusinessException
     */
    R initializeArchiveTree(Long templateId, Long tenantId);

    Boolean hasDynamicTreeNode(List<ArchiveTree> archiveTreeList);

    Metadata getDeptMetedata();

    /**
     * 根据全宗号得到树节点全部数据
     *
     * @param fondsCode
     *            全宗号
     * @return
     * @throws ArchiveBusinessException
     */
    List<ArchiveTree> getTreeDataList(String fondsCode);

    /**
     * 转换为具体数值的档案树动态节点
     *
     * @param archiveTreeList
     * @return
     */
    List<ArchiveTree> convertDynamicTreeNode(List<ArchiveTree> archiveTreeList, String filter, String fondsCode,String path);

    /**
     * 根据租户id 获取当前租户档案树信息
     *
     * @param tenantId
     * @return
     */
    List<ArrayList<String>> getArchivesTreeInfo(Long tenantId);

    /**
     * 根据租户id 获取当前租户档案树节点信息
     *
     * @param tenantId
     * @return
     */
    List<ArrayList<String>> getArchivesTreeNodeInfo(Long tenantId);

    /**
     * 是否显示层级设置
     *
     * @param id
     * @return
     */
    boolean switchShowLayer(Long id, String fondsCode);

	/**
	 * 排序
	 * @param ids 排序后ID
	 * @return
	 */
	boolean archiveTreeOrder(List<Long> ids);

	List<FondsArchiveTreeSyncTreeNode> getTreeGrid();

	List<FondsArchiveTreeSyncTreeNode> getFondsNode();

	List<FondsArchiveTreeSyncTreeNode> getArchiveTreeNode(String fondsCode, Long archiveTreeId);

	List<ArchiveTree> getArchiveTreeByFilingScopeOrArchiveType(List<String> archiveTypeCodeList,List<Long> filingScopeIdList);

	Boolean removeArchiveTree(Long[] ids);

	RenderTreeDTO getTreeById(Long id);

	//void autoGenerateParentNodes(Long id,String fondsCode ,Boolean inertOrDelete);

    /**
     * 获取档案树节点对应的数据值
     * @param id 树ID
     * @return 数据集合
     */
    Map<String, String> getArchiveTreeDataValues(Long id);
}
