/**
 *
 */
package com.cescloud.saas.archive.service.modular.archivetree.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.Padding;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archivetree.constant.ArchiveTreeNodeEnum;
import com.cescloud.saas.archive.api.modular.archivetree.dto.FondsArchiveTypeSyncTreeNode;
import com.cescloud.saas.archive.api.modular.archivetree.dto.RenderTreeDTO;
import com.cescloud.saas.archive.api.modular.archivetree.entity.ArchiveTree;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveType;
import com.cescloud.saas.archive.api.modular.common.constants.SysConstant;
import com.cescloud.saas.archive.api.modular.fonds.dto.FondsConstant;
import com.cescloud.saas.archive.api.modular.fonds.entity.Fonds;
import com.cescloud.saas.archive.common.constants.ArchiveModuleEnum;
import com.cescloud.saas.archive.common.constants.FieldConstants;
import com.cescloud.saas.archive.common.constants.NodeTypeEnum;
import com.cescloud.saas.archive.common.util.DigesterUtil;
import com.cescloud.saas.archive.common.util.IdGenerator;
import com.cescloud.saas.archive.service.modular.archivetree.mapper.ArchiveTreeMapper;
import com.cescloud.saas.archive.service.modular.archivetree.service.ArchiveTreeService;
import com.cescloud.saas.archive.service.modular.archivetree.service.RenderTreeService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTypeService;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.FondsContextHolder;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.MenuContextHolder;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.fonds.service.FondsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 档案树定义
 *
 * @author qiucs
 * @date 2019-04-22 13:36:59
 */
@Service
@Slf4j
public class RenderTreeServiceImpl extends ServiceImpl<ArchiveTreeMapper, ArchiveTree> implements RenderTreeService {

    @Autowired
    private ArchiveTreeService archiveTreeService;
    @Autowired
    private FondsService fondsService;
	@Value("${security.encode.key:cesgroupcesgroup}")
	private String encodeKey;
	@Autowired
	private ArchiveTypeService archiveTypeService;

    @Override
    public List<RenderTreeDTO> getTreeData(String parentId, Long menuId, String filter, String path, String fondsCode) {
		//解密filter
		if (filter != null){
			filter = DigesterUtil.decryptAES(filter, encodeKey, Padding.ZeroPadding);
		}
        if (log.isDebugEnabled()) {
            log.debug("父节点ID={},菜单ID={}", parentId, menuId);
        }
        // 动态节点id规则:来源ID-treeValue
        final String[] idArr = parentId.split("-");
        final Long id = Long.parseLong(idArr[0]);
	    //如果是资料库转档案，树的权限应该是整理归档的权限
	    if (ArchiveModuleEnum.DATA_MANAGEMENT.getModuleId().equals(menuId)){
		    MenuContextHolder.setMenuId(ArchiveModuleEnum.ARCHIVE_FILING.getModuleId());
	    }
        List<ArchiveTree> list = archiveTreeService.getArchiveTreeByParentId(id);
        if (archiveTreeService.hasDynamicTreeNode(list)) {
            list = archiveTreeService.convertDynamicTreeNode(list, filter, fondsCode,path);
        }
        final String preFilter = StrUtil.isNotBlank(filter) ? (filter + ";") : "";

        final List<RenderTreeDTO> renderTreeDTOList = CollUtil.newArrayList();
        for (final ArchiveTree archiveTree : list) {
	        String nodeType = archiveTree.getNodeType();
	        final RenderTreeDTO renderTreeDTO = new RenderTreeDTO();
            BeanUtil.copyProperties(archiveTree, renderTreeDTO);
	        renderTreeDTO.setDisableAdd(false);
            // 动态节点ID重复设置
            if (ArchiveTreeNodeEnum.DYNAMIC.getCode().equals(nodeType)) {
                renderTreeDTO.setId(archiveTree.getId() + "-" + archiveTree.getTreeValue());
            }
            renderTreeDTO.setParentId(parentId);
            if (StrUtil.isNotEmpty(archiveTree.getMetadataEnglish())) {
                String tempFilter = "";
                if (StrUtil.equals(archiveTree.getMetadataEnglish(), FieldConstants.DEPT_PATH)) {
                    // 如果preFilter中含有上级节点的 DEPT_PATH 条件，则舍弃掉
                    String condition = Arrays.stream(preFilter.split(";"))
                            .filter(e -> !e.contains(FieldConstants.DEPT_PATH)).collect(Collectors.joining(";"));
                    condition = StrUtil.isBlank(condition) ? condition : (condition + ";");
                    tempFilter = condition + archiveTree.getMetadataEnglish() + " like '"
                            + archiveTree.getTreeValue() + "%'";
                } else {
                    if (StrUtil.isEmpty(archiveTree.getTreeValue())) {
                        tempFilter = preFilter + archiveTree.getMetadataEnglish() + " is null";
                    } else {
                        tempFilter = preFilter + archiveTree.getMetadataEnglish() + "='" + archiveTree.getTreeValue()
                                + "'";
                    }
                }
                //加密filter,否则会判断为SQL注入
                renderTreeDTO.setFilter(DigesterUtil.encryptAES(tempFilter, encodeKey, Padding.ZeroPadding));
                renderTreeDTO.setPath(path);
            } else {
            	if (filter != null){
					renderTreeDTO.setFilter(DigesterUtil.encryptAES(filter, encodeKey, Padding.ZeroPadding));
				}
            }
            if (nodeType.equals(ArchiveTreeNodeEnum.FILING_SCOPE.getCode())) {
                String tempPath = "";
                if (StrUtil.isEmpty(path)) {
                    tempPath = archiveTree.getTreeValue() ;
                } else {
                    tempPath = path + "," + archiveTree.getTreeValue() ;
                }
                renderTreeDTO.setPath(tempPath);
            }
            if (StrUtil.isEmpty(renderTreeDTO.getPath())) {
                renderTreeDTO.setPath("");
            }
			//设置档案树节点是否屏蔽新增按钮
	        if(ArchiveTreeNodeEnum.DEPT.getCode().equals(nodeType) || ArchiveTreeNodeEnum.DYNAMIC.getCode().equals(nodeType)){
				renderTreeDTO.setDisableAdd(true);
	        }
			if (ArchiveTreeNodeEnum.FILING_SCOPE.getCode().equals(nodeType) || ArchiveTreeNodeEnum.ARCHIVE_TYPE.getCode().equals(nodeType)){
				//档案树管理，归档范围节点，档案门类节点下层节点包含动态节点或组织节点，这种情况下，不应该置灰，是其它节点，则置灰
				List<ArchiveTree> childList = archiveTreeService.list(Wrappers.<ArchiveTree>lambdaQuery().select(ArchiveTree::getNodeType)
						.eq(ArchiveTree::getParentId, archiveTree.getId()));
				if (CollUtil.isNotEmpty(childList)){
					Set<String> nodeTypes = childList.stream().map(ArchiveTree::getNodeType).collect(Collectors.toSet());
					if (!nodeTypes.contains(ArchiveTreeNodeEnum.DYNAMIC.getCode()) && !nodeTypes.contains(ArchiveTreeNodeEnum.DEPT.getCode())){
						renderTreeDTO.setDisableAdd(true);
					}
				}
			}
            renderTreeDTOList.add(renderTreeDTO);
        }
	    MenuContextHolder.clear();
        return renderTreeDTOList;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.archivetree.service.RenderTreeService#getDefaultTreeListByFondsCode(java.lang.String)
     */
    @Override
    public List<ArchiveTree> getDefaultTreeListByFondsCode(String fondsCode) {
	    //如果是资料库转档案，树的权限应该是整理归档的权限
	    if (ArchiveModuleEnum.DATA_MANAGEMENT.getModuleId().equals(MenuContextHolder.getMenuId())){
		    MenuContextHolder.setMenuId(ArchiveModuleEnum.ARCHIVE_FILING.getModuleId());
	    }
    	FondsContextHolder.setFondsCode(fondsCode);
        // 树权限统一由权限filter自动添加
	    List<ArchiveTree> list = this.list(Wrappers.<ArchiveTree>lambdaQuery()
			    .eq(ArchiveTree::getNodeType, ArchiveTreeNodeEnum.TREE_ROOT.getCode())
			    .in(ArchiveTree::getFondsCode, CollectionUtil.newArrayList(FondsConstant.GLOBAL_FONDS_CODE, fondsCode)));
	    MenuContextHolder.clear();
	    FondsContextHolder.clear();
	    return list;

    }

    @Override
    public List<ArchiveTree> getRootNodeListByFondsCode(String fondsCode) {
    	//如果是批量挂接树则要保持为数据导入形式的树
		Long menuid =  MenuContextHolder.getMenuId();
		Long batchAttachId = ArchiveModuleEnum.BATCH_ATTACH.getModuleId();
		if (menuid != null && menuid.equals(batchAttachId)){
			List<FondsArchiveTypeSyncTreeNode> data = archiveTypeService.getFondsTreeNode1(fondsCode);
			return convertFondsNode(data);
		}else{
			List<String> fondsCodeList = null;
			if (StrUtil.isBlank(fondsCode)) {
				fondsCodeList = fondsService.list().stream().map(Fonds::getFondsCode).collect(Collectors.toList());
			} else {
				FondsContextHolder.setFondsCode(fondsCode);
				fondsCodeList = CollectionUtil.newArrayList(fondsCode);
			}
			fondsCodeList.add(FondsConstant.GLOBAL_FONDS_CODE);
			// 树权限统一由权限filter自动添加
			return list(Wrappers.<ArchiveTree>lambdaQuery()
					.eq(ArchiveTree::getNodeType, ArchiveTreeNodeEnum.TREE_ROOT.getCode())
					.in(ArchiveTree::getFondsCode, fondsCodeList));
    	}
	}

	private List<ArchiveTree> convertFondsNode(List<FondsArchiveTypeSyncTreeNode> data) {
		return data.stream().map(fondsArchiveTypeSyncTreeNode -> {
			ArchiveTree treeNode = ArchiveTree.builder().id(IdGenerator.getId())
					.fondsCode(fondsArchiveTypeSyncTreeNode.getFondsCode())
					.fondsName(fondsArchiveTypeSyncTreeNode.getName())
					.nodeType(FondsConstant.ARCHIVE_TREE_CLASS).build();
			treeNode.setTreeName(fondsArchiveTypeSyncTreeNode.getName());
			treeNode.setTreeCode(fondsArchiveTypeSyncTreeNode.getFondsCode());
			treeNode.setParentId(-1L);
			treeNode.setIsLeaf(false);
			treeNode.setTreeLevel(1);
			treeNode.setTenantId(SecurityUtils.getUser().getTenantId());
		return treeNode;
		}).collect(Collectors.toList());
    }

	@Override
    public List<RenderTreeDTO> getModuleTreeData(String parentId, Long menuId, String filter, String path, Integer shouGroupAndDynamic, Integer shouLayer, String fondsCode) {
    	if(menuId == null){
			menuId = MenuContextHolder.getMenuId();
		}
        if(menuId.equals(ArchiveModuleEnum.BATCH_ATTACH.getModuleId())){
			List<ArchiveType> archiveTypeList = archiveTypeService.list(Wrappers.<ArchiveType>lambdaQuery().eq(ArchiveType::getParentId, 0l).eq(ArchiveType::getFondsCode, fondsCode));
			return convertTypeNode(archiveTypeList);
		}
    	if (log.isDebugEnabled()) {
            log.debug("父节点ID={},菜单ID={}", parentId, menuId);
        }
        // 动态节点id规则:来源ID-treeValue
        final String[] idArr = parentId.split("-");
        final Long id = Long.parseLong(idArr[0]);
        List<ArchiveTree> list = archiveTreeService.getArchiveTreeByParentId(id);
        if (0 == shouGroupAndDynamic) {
            list = list.stream().filter(archiveTree -> !ArchiveTreeNodeEnum.DEPT.getCode().equals(archiveTree.getNodeType())
                    && !ArchiveTreeNodeEnum.DYNAMIC.getCode().equals(archiveTree.getNodeType())).collect(Collectors.toList());
        }
        if (0 == shouLayer) {
            list = list.stream().filter(archiveTree -> !ArchiveTreeNodeEnum.LAYER.getCode().equals(archiveTree.getNodeType())).collect(Collectors.toList());
        }
        if (archiveTreeService.hasDynamicTreeNode(list)) {
            list = archiveTreeService.convertDynamicTreeNode(list, filter, fondsCode,path);
        }

        final String preFilter = StrUtil.isNotBlank(filter) ? (filter + ";") : "";

        final List<RenderTreeDTO> renderTreeDTOList = CollUtil.newArrayList();
        for (final ArchiveTree archiveTree : list) {
            final RenderTreeDTO renderTreeDTO = new RenderTreeDTO();
            BeanUtil.copyProperties(archiveTree, renderTreeDTO);
            // 动态节点ID重复设置
            if (ArchiveTreeNodeEnum.DYNAMIC.getCode().equals(archiveTree.getNodeType())) {
                renderTreeDTO.setId(archiveTree.getId() + "-" + archiveTree.getTreeValue());
            }
            renderTreeDTO.setParentId(parentId);
            if (StrUtil.isNotEmpty(archiveTree.getMetadataEnglish())) {
                String tempFilter = "";
                if (StrUtil.equals(archiveTree.getMetadataEnglish(), FieldConstants.DEPT_PATH)) {
                    // 如果preFilter中含有上级节点的 DEPT_PATH 条件，则舍弃掉
                    String condition = Arrays.stream(preFilter.split(";"))
                            .filter(e -> !e.contains(FieldConstants.DEPT_PATH)).collect(Collectors.joining(";"));
                    condition = StrUtil.isBlank(condition) ? condition : (condition + ";");
                    tempFilter = condition + archiveTree.getMetadataEnglish() + " like '"
                            + archiveTree.getTreeValue() + "%'";
                } else {
                    if (StrUtil.isEmpty(archiveTree.getTreeValue())) {
                        tempFilter = preFilter + archiveTree.getMetadataEnglish() + " is null";
                    } else {
                        tempFilter = preFilter + archiveTree.getMetadataEnglish() + "='" + archiveTree.getTreeValue()
                                + "'";
                    }
                }
                renderTreeDTO.setFilter(DigesterUtil.encryptAES(tempFilter, encodeKey, Padding.ZeroPadding));
                renderTreeDTO.setPath(path);
            } else {
                renderTreeDTO.setFilter(DigesterUtil.encryptAES(filter, encodeKey, Padding.ZeroPadding));
            }
            if (archiveTree.getNodeType().equals(ArchiveTreeNodeEnum.FILING_SCOPE.getCode())) {
                String tempPath = "";
                if (StrUtil.isEmpty(path)) {
                    tempPath = archiveTree.getTreeValue() ;
                } else {
                    tempPath = path + "," +archiveTree.getTreeValue() ;
                }
                renderTreeDTO.setPath(tempPath);
            }
            if (StrUtil.isEmpty(renderTreeDTO.getPath())) {
                renderTreeDTO.setPath("");
            }
            renderTreeDTOList.add(renderTreeDTO);
        }
        return renderTreeDTOList;
    }

	private List<RenderTreeDTO> convertTypeNode(List<ArchiveType> archiveTypeList) {
    	return archiveTypeList.stream().map(archiveType -> {
			RenderTreeDTO treeNode = new RenderTreeDTO();
			treeNode.setArchiveTypeCode(archiveType.getTypeCode().toUpperCase());
			treeNode.setClassName(archiveType.getTypeName());
			treeNode.setClassType(archiveType.getClassType());
			//treeNode.setFilingScopeId();
			treeNode.setFilingType(archiveType.getFilingType());
			treeNode.setFondsCode(archiveType.getFondsCode());
			treeNode.setIsLeaf(true);
			treeNode.setLayerCode(archiveType.getFilingType());
			treeNode.setNodeType(archiveType.getNodeType());
			treeNode.setParentId("0");
			treeNode.setShowLayer(false);
			treeNode.setTemplateTableId(archiveType.getTemplateTypeId());
			treeNode.setTreeName(archiveType.getTypeName());
			treeNode.setTreeValue(archiveType.getTypeCode().toUpperCase());
			treeNode.setId(archiveType.getId().toString());
			return treeNode;
		}).collect(Collectors.toList());
	}

}
