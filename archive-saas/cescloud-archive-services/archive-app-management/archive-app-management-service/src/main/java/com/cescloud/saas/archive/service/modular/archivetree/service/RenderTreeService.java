package com.cescloud.saas.archive.service.modular.archivetree.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archivetree.dto.RenderTreeDTO;
import com.cescloud.saas.archive.api.modular.archivetree.entity.ArchiveTree;

import java.util.List;

/**
 * 档案树定义
 *
 * @author qiucs
 * @date 2019-04-12 13:36:59
 */
public interface RenderTreeService extends IService<ArchiveTree> {

    /**
     * 档案树节点保存
     *
     * @param parentId
     *            档案树父节点ID
     * @param menuId
     *            菜单ID
     * @return boolean
     */
	List<RenderTreeDTO> getTreeData(String parentId, Long menuId, String filter, String path, String fondsCode);

    /**
     * 获取全宗号绑定的对应档案树
     *
     * @param fondsCode
     *            全宗号
     * @return
     */
    List<ArchiveTree> getDefaultTreeListByFondsCode(String fondsCode);

	List<ArchiveTree> getRootNodeListByFondsCode(String fondsCode);

	List<RenderTreeDTO> getModuleTreeData(String parentId, Long menuId, String filter, String path, Integer shouGroupAndDynamic, Integer shouLayer, String fondsCode);
}
