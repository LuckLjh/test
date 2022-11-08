package com.cescloud.saas.archive.service.modular.archivetree.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archivetree.constant.ArchiveTreeNodeEnum;
import com.cescloud.saas.archive.api.modular.archivetree.dto.ArchiveTreeGetDTO;
import com.cescloud.saas.archive.api.modular.archivetree.dto.ArchiveTreePutDTO;
import com.cescloud.saas.archive.api.modular.archivetree.dto.FondsArchiveTreeSyncTreeNode;
import com.cescloud.saas.archive.api.modular.archivetree.dto.RenderTreeDTO;
import com.cescloud.saas.archive.api.modular.archivetree.entity.ArchiveTree;
import com.cescloud.saas.archive.api.modular.archivetree.entity.FondsArchiveTree;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveType;
import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateTable;
import com.cescloud.saas.archive.api.modular.authority.dto.ArchiveTreeAuthDTO;
import com.cescloud.saas.archive.api.modular.authority.entity.ArchiveTreeAuth;
import com.cescloud.saas.archive.api.modular.authority.feign.RemoteArchiveAuthorityService;
import com.cescloud.saas.archive.api.modular.authority.feign.RemoteSysAuthService;
import com.cescloud.saas.archive.api.modular.datasource.dto.DynamicArchiveDTO;
import com.cescloud.saas.archive.api.modular.datasource.feign.RemoteArchiveService;
import com.cescloud.saas.archive.api.modular.dept.dto.DeptTree;
import com.cescloud.saas.archive.api.modular.dept.dto.DeptTreeNode;
import com.cescloud.saas.archive.api.modular.filingscope.entity.FilingScope;
import com.cescloud.saas.archive.api.modular.fonds.dto.FondsConstant;
import com.cescloud.saas.archive.api.modular.fonds.entity.Fonds;
import com.cescloud.saas.archive.api.modular.menu.vo.TreeUtil;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.api.modular.role.feign.RemoteSysRoleAuthService;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantTemplate;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantTemplateService;
import com.cescloud.saas.archive.common.constants.*;
import com.cescloud.saas.archive.common.util.ArchiveUtil;
import com.cescloud.saas.archive.common.util.InitializeUtil;
import com.cescloud.saas.archive.service.dboperate.service.DbOperateService;
import com.cescloud.saas.archive.service.modular.archivetree.mapper.ArchiveTreeMapper;
import com.cescloud.saas.archive.service.modular.archivetree.service.ArchiveTreeService;
import com.cescloud.saas.archive.service.modular.archivetree.service.FondsArchiveTreeService;
import com.cescloud.saas.archive.service.modular.archivetype.mapper.ArchiveTypeMapper;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTypeService;
import com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTableService;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import com.cescloud.saas.archive.service.modular.common.security.tenantfilter.TenantContextHolder;
import com.cescloud.saas.archive.service.modular.filingscope.service.FilingScopeService;
import com.cescloud.saas.archive.service.modular.fonds.service.FondsService;
import com.github.pagehelper.util.StringUtil;
import com.google.common.collect.Maps;
import io.swagger.annotations.ApiModelProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 档案树定义
 *
 * @author qiucs
 * @date 2019-04-12 13:36:59
 */
@Service
@Slf4j
@RequiredArgsConstructor
@CacheConfig(cacheNames = "archive-tree")
public class ArchiveTreeServiceImpl extends ServiceImpl<ArchiveTreeMapper, ArchiveTree> implements ArchiveTreeService {

	private static final String FILING_DEPT_CHINESE = "归档部门";

	private static final String CREATE_DEPT_CHINESE = "录入部门";

	@Autowired
	private FilingScopeService filingScopeService;
	@Autowired
	private FondsArchiveTreeService fondsArchiveTreeService;
	@Autowired
	private ArchiveTypeService archiveTypeService;
	@Autowired
	private ArchiveTableService archiveTableService;
	@Autowired
	private TemplateTableService templateTableService;
	@Autowired(required = false)
	private RemoteTenantTemplateService remoteTenantTemplateService;
	@Autowired(required = false)
	private RemoteArchiveService remoteArchiveService;
	@Autowired
	private FondsService fondsService;
	@Autowired(required = false)
	private RemoteSysAuthService remoteSysAuthService;
	@Autowired
	private DbOperateService dbOperateService;
	@Autowired
	private RemoteSysRoleAuthService remoteSysRoleAuthService;
	@Autowired
	private ArchiveUtil archiveUtil;

	@Resource
	private ArchiveTypeMapper archiveTypeMapper;


	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(allEntries = true)
	public List<ArchiveTree> save(ArchiveTreePutDTO entityDTO) {
		List<ArchiveTree> list = new ArrayList<ArchiveTree>();
		final ArchiveTree entity = entityDTO.getEntity();
		final List<Map<String, Object>> extendValList = entityDTO.getExtendValList();
		if (CollUtil.isNotEmpty(extendValList)) {
			switch (ArchiveTreeNodeEnum.getEnum(entity.getNodeType())) {
				case ARCHIVE_TYPE:
					list = saveArchiveTypeNodes(entity, extendValList);
					break;
				case FILING_SCOPE:
					list = saveFilingScopeNodes(entity, extendValList);
					break;
				case DEPT:
					list = saveDeptNodes(entity, extendValList);
					break;
				case DYNAMIC:
					list = saveDynamicNodes(entity, extendValList);
					break;
				default:
					throw new ArchiveRuntimeException("该节点类型功能未实现！");
			}
		} else if (entity.getNodeType().equals(ArchiveTreeNodeEnum.DYNAMIC.getCode())) {
			list = saveDynamicNodes(entity, extendValList);
		} else {
			save(entity);
			list.add(entity);
		}
		//创建出来的list 的节点同步更新到权限中
		saveToArchiveTreeAuth(list);
		return list;
	}

	private void saveToArchiveTreeAuth(List<ArchiveTree> list) {
		//找一下那些树有这个创建出来的父节点，由于只添加叶节点下新增的节点 所以判断要加上.getParentId() != 0L
		List<ArchiveTree> archiveTrees = list.stream().filter(
				archiveTreeNode ->
					archiveTreeNode.getParentId() != -1L && archiveTreeNode.getIsLeaf() == true

		).collect(Collectors.toList());
		if (archiveTrees.size()>0){
			//查询下 权限这边有没有选中新增这棵树的父节点，还必须，有的话给他选择上
			archiveTrees.forEach( archiveTree -> {
				R<List<ArchiveTreeAuth>> result = remoteSysAuthService.findAuthTreeNodeByTreeId(archiveTree.getParentId());
				if(result.getData().size()>0){
					List<ArchiveTreeAuth> ArchiveTreeAuthLists = result.getData();
					ArchiveTreeAuthLists.stream().forEach(
						ArchiveTreeAuth -> {
							ArchiveTreeAuthDTO dto = new ArchiveTreeAuthDTO();
							BeanUtil.copyProperties(ArchiveTreeAuth,dto);
							Collection<Long> collection = new ArrayList<>();
							collection.add(archiveTree.getId());
							dto.setArchiveTreeIdList(collection);
							remoteSysAuthService.saveArchiveTreeAuth(dto);
						});
				}
			});
		}
	}

	@Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public boolean save(ArchiveTree entity) {
		// 检查节点名称
		if (StrUtil.isEmpty(entity.getTreeName())) {
			throw new ArchiveRuntimeException("树节点名称不能为空！");
		}
		// 检查节点名称在同层下是否唯一性
		if (!checkUnique(entity)) {
			throw new ArchiveRuntimeException("树节点名称已存在，请修改！");
		}
		final boolean isCreate = null == entity.getId();
		if (isCreate) {
			// 处理排序号
			processSortNo(entity);
			// 处理编码值
			processTreeCode(entity);
			// 新增时处理父节点
			processParentNodeInCreate(entity);
		}

		return super.save(entity);
	}

	/**
	 * 保存动态节点
	 *
	 * @param entity
	 * @param extendValList
	 * @return
	 */
	private List<ArchiveTree> saveDynamicNodes(ArchiveTree entity, List<Map<String, Object>> extendValList) {
		final List<ArchiveTree> list = new ArrayList<ArchiveTree>();
		final ArchiveTree parentEntity = getParentEntity(entity);
		int dynamicChildrenCount = this.count(Wrappers.<ArchiveTree>lambdaQuery().eq(ArchiveTree::getParentId, entity.getParentId())
				.eq(ArchiveTree::getNodeType, ArchiveTreeNodeEnum.DYNAMIC.getCode()));
		if (dynamicChildrenCount > 0) {
			throw new ArchiveRuntimeException("同一层只允许有一个动态节点！");
		}

		if (StrUtil.isBlank(entity.getMetadataEnglish())) {
			throw new ArchiveRuntimeException("动态节点不能为空！！！");
		}
		final ArchiveTree copyEntity = new ArchiveTree();
		BeanUtil.copyProperties(entity, copyEntity);
		copyEntity.setTreeName(getDynamicNodeName(entity.getTreeName()));
		copyEntity.setFilingType(parentEntity.getFilingType());
		copyEntity.setFilingType(parentEntity.getFilingType());
		copyEntity.setArchiveTypeCode(parentEntity.getArchiveTypeCode());
		copyEntity.setTemplateTableId(parentEntity.getTemplateTableId());
		copyEntity.setLayerCode(parentEntity.getLayerCode());
		save(copyEntity);
		list.add(copyEntity);

		return list;
	}

	private String getDynamicNodeName(String name) {
		return "[" + name + "]";
	}

	/**
	 * 保存部门节点
	 *
	 * @param entity
	 * @param extendValList
	 * @return
	 */
	private List<ArchiveTree> saveDeptNodes(ArchiveTree entity, List<Map<String, Object>> extendValList) {
		//保存之前，先把以前的组织架构节点删除
		final ArchiveTree parentEntity = getParentEntity(entity);
		this.remove(Wrappers.<ArchiveTree>lambdaQuery().likeRight(ArchiveTree::getTreeCode, parentEntity.getTreeCode())
				.ne(ArchiveTree::getTreeCode, parentEntity.getTreeCode()));
		//保存组织架构树
		final List<ArchiveTree> list = CollUtil.<ArchiveTree>newArrayList();
		entity.setFilingType(parentEntity.getFilingType());
		entity.setArchiveTypeCode(parentEntity.getArchiveTypeCode());
		entity.setTemplateTableId(parentEntity.getTemplateTableId());
		entity.setLayerCode(parentEntity.getLayerCode());
//		entity.setParentId(parentEntity.getId());
		//将数据组装成部门同步树，方便档案树按层级添加
		final List<DeptTree> selectDeptTree = deptSyncTree(extendValList);
		savaDeptTreeNode(selectDeptTree, entity, list, true);

		return list;
	}

	/**
	 * 保存部门树节点
	 *
	 * @param selectDeptTree
	 */
	private void savaDeptTreeNode(List<DeptTree> selectDeptTree, ArchiveTree entity, List<ArchiveTree> treeList,
	                              Boolean addToTree) {
		//final String metadataEnglish = getDeptFieldByPolicyType();
		selectDeptTree.stream().forEach(deptTree -> {
			final ArchiveTree copyEntity = new ArchiveTree();
			copyEntity.setNodeType(entity.getNodeType());
			copyEntity.setParentId(entity.getParentId());
			//根据规则动态获取部门字段，现在前台没传
			//copyEntity.setMetadataEnglish(entity.getMetadataEnglish());
			copyEntity.setMetadataEnglish(FieldConstants.DEPT_PATH);
			copyEntity.setFilingType(entity.getFilingType());
			copyEntity.setArchiveTypeCode(entity.getArchiveTypeCode());
			copyEntity.setTemplateTableId(entity.getTemplateTableId());
			copyEntity.setLayerCode(entity.getLayerCode());
			copyEntity.setFondsCode(entity.getFondsCode());
			copyEntity.setFondsName(entity.getFondsName());
			// 计算dept_path值
			final String parents = deptTree.getParentIds();
			//final String treeValue = parents.replaceAll(",", ".") + StrUtil.DOT + deptTree.getDeptId();
			final String treeValue = parents + StrUtil.COMMA + deptTree.getDeptId();
			copyEntity.setTreeValue(treeValue);
			copyEntity.setTreeName(String.valueOf(deptTree.getName()));
			//为了新增
			copyEntity.setId(null);
			save(copyEntity);
			if (addToTree) {
				treeList.add(copyEntity);
			}
			//递归子节点
			final List<DeptTreeNode> deptTreeChildren = deptTree.getChild();
			if (CollUtil.isNotEmpty(deptTreeChildren)) {
				//为了让子节点使用保存后的ID作为父节点
				//copyEntity.setParentId(copyEntity.getId());
				Long parentId = copyEntity.getId();
				final ArchiveTree copyEntity1 = new ArchiveTree();
				BeanUtil.copyProperties(copyEntity,copyEntity1);
				copyEntity1.setParentId(parentId);
				savaDeptTreeNode(convertDeptTree(deptTreeChildren), copyEntity1, treeList, true);
			}
		});
	}

	private List<DeptTree> convertDeptTree(List<DeptTreeNode> deptTreeList) {
		return deptTreeList.stream().map(deptTreeNode -> (DeptTree) deptTreeNode).collect(Collectors.toList());
	}

	/**
	 * 组装部门同步树
	 *
	 * @param extendValList
	 * @return
	 */
	private List<DeptTree> deptSyncTree(List<Map<String, Object>> extendValList) {
		final List<DeptTree> treeList = extendValList.stream()
				.map(extendValMap -> {
					final DeptTree node = new DeptTree();
					node.setDeptId(Long.parseLong(String.valueOf(extendValMap.get("id"))));
					node.setParentId(Long.parseLong(String.valueOf(extendValMap.get("parentId"))));
					node.setParentIds(String.valueOf(extendValMap.get("parentIds")));
					node.setName(String.valueOf(extendValMap.get("name")));
					node.setSort(Integer.parseInt(String.valueOf(extendValMap.get("sort"))));
					return node;
				}).collect(Collectors.toList());
		return TreeUtil.buildDept(treeList, DeptConstants.ROOT_PARENT_ID);
	}

	//保存自定义节点
	private List<ArchiveTree> saveSelfDeFineNodes(ArchiveTree entity, List<Map<String, Object>> extendValList) {
		final List<ArchiveTree> list = new ArrayList<ArchiveTree>();
		final ArchiveTree parentEntity = getParentEntity(entity);
		extendValList.forEach(extendValMap -> {
			final ArchiveTree copyEntity = new ArchiveTree();
			BeanUtil.copyProperties(entity, copyEntity);
			copyEntity.setTreeValue(String.valueOf(extendValMap.get("id")));
			copyEntity.setTreeName(String.valueOf(extendValMap.get("name")));
			copyEntity.setFilingType(parentEntity.getFilingType());
			save(copyEntity);
			list.add(copyEntity);
		});

		return list;
	}

	private List<ArchiveTree> saveFondsNodes(ArchiveTree entity, List<Map<String, Object>> extendValList) {
		final List<ArchiveTree> list = new ArrayList<ArchiveTree>();
		extendValList.forEach(extendValMap -> {
			final ArchiveTree copyEntity = new ArchiveTree();

			BeanUtil.copyProperties(entity, copyEntity);
			copyEntity.setTreeValue(String.valueOf(extendValMap.get("id")));
			copyEntity.setTreeName(String.valueOf(extendValMap.get("name")));

			save(copyEntity);
			list.add(copyEntity);
		});

		return list;
	}

	private List<ArchiveTree> saveArchiveTypeNodes(ArchiveTree entity, List<Map<String, Object>> extendValList) {
		final List<ArchiveTree> list = new ArrayList<ArchiveTree>();
		final ArchiveTree rootNode = getRootNodeById(entity.getParentId());
		final Map<Long, Map<Long, TemplateTable>> templateTypeTableMap = Maps.newHashMap();//档案门类模板ID对应所有表模板
		final Map<Long, TemplateTable> templateTypeTopTableMap = Maps.newHashMap(); //档案门类模板ID对应首层表模板
		extendValList.forEach(extendValMap -> {
			final ArchiveTree copyEntity = new ArchiveTree();
			BeanUtil.copyProperties(entity, copyEntity);
			final String id = String.valueOf(extendValMap.get("id"));
			ArchiveType archiveType = null;
			try {
				archiveType = archiveTypeService.getByTypeCode(id);
			} catch (final ArchiveBusinessException e) {
				e.printStackTrace();
			}
			if (!templateTypeTopTableMap.containsKey(archiveType.getTemplateTypeId())) {
				final List<TemplateTable> templateTableList = templateTableService.getByTemplateTypeId(archiveType.getTemplateTypeId());
				templateTypeTopTableMap.put(archiveType.getTemplateTypeId(), templateTableList.get(0));
				// 显示层级
				if (BooleanUtil.isTrue(rootNode.getShowLayer())) {
					if (!templateTypeTableMap.containsKey(archiveType.getTemplateTypeId())) {
						final Map<Long, TemplateTable> templateTableMap = templateTableList.stream()
								.collect(Collectors.toMap(TemplateTable::getId, templateTable -> templateTable));
						templateTypeTableMap.put(archiveType.getTemplateTypeId(), templateTableMap);
					}
				}
			}
			final TemplateTable templateTable = templateTypeTopTableMap.get(archiveType.getTemplateTypeId());
			copyEntity.setTreeValue(id);
			copyEntity.setTreeName(String.valueOf(extendValMap.get("name")));
			copyEntity.setFilingType(archiveType.getFilingType());
			copyEntity.setArchiveTypeCode(id);
			copyEntity.setTemplateTableId(templateTable.getId());
			copyEntity.setLayerCode(templateTable.getLayerCode());
			copyEntity.setShowLayer(rootNode.getShowLayer());
			if (BooleanUtil.isTrue(rootNode.getShowLayer())) {
				copyEntity.setIsLeaf(Boolean.FALSE);
			}
			save(copyEntity);
			list.add(copyEntity);
			//
			if (BooleanUtil.isTrue(rootNode.getShowLayer())) {
				list.addAll(saveLayerTableNodes(copyEntity, archiveType, templateTypeTableMap));
			}
		});

		return list;
	}

	private List<ArchiveTree> saveLayerTableNodes(ArchiveTree parent, ArchiveType archiveType, Map<Long, Map<Long, TemplateTable>> templateTypeTableMap) {
		final List<ArchiveTree> entityList = new ArrayList<ArchiveTree>();
		Map<Long, TemplateTable> templateTableMap;
		if (!templateTypeTableMap.containsKey(archiveType.getTemplateTypeId())) {
			final List<TemplateTable> templateTableList = templateTableService
					.getByTemplateTypeId(archiveType.getTemplateTypeId());
			templateTableMap = templateTableList.stream()
					.collect(Collectors.toMap(TemplateTable::getId, templateTable -> templateTable));
			templateTypeTableMap.put(archiveType.getTemplateTypeId(), templateTableMap);
		} else {
			templateTableMap = templateTypeTableMap.get(archiveType.getTemplateTypeId());
		}

		if (null == templateTableMap || templateTableMap.isEmpty()) {
			throw new ArchiveRuntimeException(String.format("档案门类[%s]对应的档案门类模板不存在！", archiveType.getTypeName()));
		}

		final List<ArchiveTable> tableList = archiveTableService.getTableListByTypeCode(archiveType.getTypeCode());
		int sortNo = 0;
		TemplateTable templateTable;
		for (final ArchiveTable table : tableList) {
			// 全文、过程信息、签名签章表不出现在档案树层级上
			if (ArchiveLayerEnum.DOCUMENT.getCode().equals(table.getArchiveLayer())
					|| ArchiveLayerEnum.INFO.getCode().equals(table.getArchiveLayer())
					|| ArchiveLayerEnum.SIGNATRUE.getCode().equals(table.getArchiveLayer())) {
				continue;
			}
			if (!templateTableMap.containsKey(table.getTemplateTableId())) {
				throw new ArchiveRuntimeException(String.format("档案门类表[%s]对应的档案门类表模板不存在！", table.getStorageName()));
			}
			final ArchiveTree entity = new ArchiveTree();
			BeanUtil.copyProperties(parent, entity);
			templateTable = templateTableMap.get(table.getTemplateTableId());
			entity.setId(null);
			entity.setParentId(parent.getId());
			entity.setTreeValue(StrUtil.toString(table.getTemplateTableId()));
			entity.setTreeName(templateTable.getName());
			entity.setNodeType(ArchiveTreeNodeEnum.LAYER.getCode());
			entity.setIsLeaf(true);
			entity.setSortNo(sortNo);
			entity.setTreeCode(parent.getTreeCode() + formatTreeCode(sortNo));
			entity.setTemplateTableId(templateTable.getId());
			entity.setLayerCode(templateTable.getLayerCode());
			entityList.add(entity);
			sortNo++;
		}

		super.saveBatch(entityList);

		return entityList;
	}

	/**
	 * @param id
	 * @return
	 */
	private ArchiveTree getRootNodeById(Long id) {
		final ArchiveTree entity = getById(id);
		if (ArchiveTreeNodeEnum.TREE_ROOT.getCode().equals(entity.getNodeType())) {
			return entity;
		}
		return getRootNodeById(entity.getParentId());
	}

	/**
	 * <p>保存归档范围节点</p>
	 *
	 * @param entity
	 * @param extendValList [{id: FilingScope.id, status: true(选中)/false(半选)}]
	 * @return
	 */
	private List<ArchiveTree> saveFilingScopeNodes(ArchiveTree entity, List<Map<String, Object>> extendValList) {
		final Map<Long, List<FilingScope>> allMap = new HashMap<Long, List<FilingScope>>();
		final List<ArchiveTree> list = new ArrayList<ArchiveTree>();
		List<Long> ids = extendValList.stream().map(map -> Long.valueOf(StrUtil.toString(map.get("id")))).collect(Collectors.toList());
		List<FilingScope> filingScopes = filingScopeService.list(Wrappers.<FilingScope>lambdaQuery().in(FilingScope::getId, ids));
		extendValList.forEach(extendValMap -> {
			long id = Long.parseLong(String.valueOf(extendValMap.get("id")));
			FilingScope filingScope = filingScopes.stream().filter(filingScope1 -> filingScope1.getId().equals(id)).findAny().orElse(null);
			if (null == filingScope) {
				log.error("归档范围ID={}不存在", String.valueOf(extendValMap.get("id")));
				throw new ArchiveRuntimeException(
						String.format("归档范围ID=%s不存在，请刷新后再试！", String.valueOf(extendValMap.get("id"))));
			}
			final Long parentId = filingScope.getParentClassId();
			if (!allMap.containsKey(parentId)) {
				allMap.put(parentId, new ArrayList<FilingScope>());
			}
			final List<FilingScope> childList = allMap.get(parentId);
			if (!childList.stream().anyMatch(f -> filingScope.getId().equals(f.getId()))) {
				childList.add(filingScope);
			}
			if (Boolean.parseBoolean(String.valueOf(extendValMap.get("status")))) {
				processFilingScopeNodes(filingScope.getId(), allMap);
			}
		});

		final ArchiveTree parentEntity = getParentEntity(entity);
		Long parentFilingScopeId = null;
		if (ArchiveTreeNodeEnum.FILING_SCOPE.getCode().equals(parentEntity.getNodeType())) {
			parentFilingScopeId = parentEntity.getFilingScopeId();
		}
		List<String> typeCodes;
		//如果只有根节点，则获取根节点所有子节点的档案类型
		if (filingScopeIsRoot(ids)) {
			List<FilingScope> allFilingScopes = allMap.get(ids.get(0));
			if (ObjectUtil.isEmpty(allFilingScopes)) {//没有下级节点不让保存
				log.error("归档范围树没有子节点");
				throw new ArchiveRuntimeException("归档范围树没有子节点，保存失败！");
			}
			typeCodes = allFilingScopes.stream().map(FilingScope::getTypeCode).filter(StrUtil::isNotBlank).collect(Collectors.toList());
		} else {
			typeCodes = filingScopes.stream().map(FilingScope::getTypeCode).filter(StrUtil::isNotBlank).collect(Collectors.toList());
		}
		Map<String, ArchiveType> archiveTypeMap = CollUtil.newHashMap();
		List<TemplateTable> templateTables = CollUtil.newArrayList();
		if (CollUtil.isNotEmpty(typeCodes)){
			final List<ArchiveType> archiveTypes = archiveTypeService.list(Wrappers.<ArchiveType>lambdaQuery().in(ArchiveType::getTypeCode, typeCodes));
			archiveTypeMap = archiveTypes.stream().collect(Collectors.toMap(ArchiveType::getTypeCode, archiveType -> archiveType));
			List<Long> templateTypeIds = archiveTypes.stream().map(ArchiveType::getTemplateTypeId).collect(Collectors.toList());
			templateTables = templateTableService.list(Wrappers.<TemplateTable>lambdaQuery().in(TemplateTable::getTemplateTypeId, templateTypeIds).orderByAsc(TemplateTable::getSortNo));
		}
		final Map<Long, TemplateTable> templateTypeTopTableMap = Maps.newHashMap(); //档案门类模板ID对应首层表模板
		List<FilingScope> topFilingScopeList = getTopFilingScopeList(allMap, parentFilingScopeId);
		for (FilingScope filingScope : topFilingScopeList) {
			final ArchiveTree copyEntity = new ArchiveTree();
			BeanUtil.copyProperties(entity, copyEntity);
			ArchiveType archiveType = archiveTypeMap.get(filingScope.getTypeCode());
			if (ObjectUtil.isNotNull(archiveType)){
				if (!templateTypeTopTableMap.containsKey(archiveType.getTemplateTypeId())) {
					List<TemplateTable> templateTableList = templateTables.stream().filter(e -> archiveType.getTemplateTypeId().equals(e.getTemplateTypeId())).collect(Collectors.toList());
					templateTypeTopTableMap.put(archiveType.getTemplateTypeId(), templateTableList.get(0));
				}
				TemplateTable templateTable = templateTypeTopTableMap.get(archiveType.getTemplateTypeId());
				copyEntity.setTemplateTableId(templateTable.getId());
				copyEntity.setLayerCode(templateTable.getLayerCode());
				copyEntity.setFilingType(archiveType.getFilingType());
			}
			copyEntity.setTreeName(filingScope.getClassName());
			copyEntity.setTreeValue(filingScope.getClassNo());
			if (StrUtil.isBlank(copyEntity.getFilingType())) {
				copyEntity.setFilingType(parentEntity.getFilingType());
			}
			copyEntity.setArchiveTypeCode(filingScope.getTypeCode());
			copyEntity.setFilingScopeId(filingScope.getId());
			copyEntity.setFondsCode(entity.getFondsCode());
			copyEntity.setFondsName(entity.getFondsName());
			save(copyEntity);
			list.add(copyEntity);
			saveFilingScopeChildNodes(copyEntity, filingScope.getId(), typeCodes, allMap, archiveTypeMap,templateTypeTopTableMap);
		}

		return list;
	}

	//判断节点信息中是否只有根节点
	private boolean filingScopeIsRoot(List<Long> ids) {
		if (CollUtil.isNotEmpty(ids) && ids.size() == 1) {
			if (filingScopeService.getById(ids.get(0)).getParentClassId() == 0L) {
				return true;
			}
		}
		return false;
	}

	private ArchiveTree getParentEntity(ArchiveTree entity) {
		final ArchiveTree parentEntity = baseMapper.selectById(entity.getParentId());
		if (null == parentEntity) {
			throw new ArchiveRuntimeException(entity.getTreeName() + "的父节点不存在或已被删除，请刷新后再操作！");
		}
		return parentEntity;
	}

	private void saveFilingScopeChildNodes(ArchiveTree archiveTree, Long filingScopeParentId, List<String> typeCodes,
	                                       Map<Long, List<FilingScope>> allMap, Map<String, ArchiveType> archiveTypeMap, Map<Long, TemplateTable> templateTypeTopTableMap) {
		final List<FilingScope> filingScopeList = allMap.get(filingScopeParentId);
		if (null == filingScopeList || filingScopeList.isEmpty()) {
			return;
		}
		typeCodes.addAll(filingScopeList.stream().map(FilingScope::getTypeCode).filter(ObjectUtil::isNotNull).collect(Collectors.toList()));
		List<TemplateTable> templateTables = CollUtil.newArrayList();
		if (CollUtil.isNotEmpty(typeCodes)){
			final List<ArchiveType> archiveTypes = archiveTypeService.list(Wrappers.<ArchiveType>lambdaQuery().in(ArchiveType::getTypeCode, typeCodes));
			archiveTypeMap.putAll(archiveTypes.stream().collect(Collectors.toMap(ArchiveType::getTypeCode, archiveType -> archiveType)));
			List<Long> templateTypeIds = archiveTypes.stream().map(ArchiveType::getTemplateTypeId).collect(Collectors.toList());
			templateTables = templateTableService.list(Wrappers.<TemplateTable>lambdaQuery().in(TemplateTable::getTemplateTypeId, templateTypeIds).orderByAsc(TemplateTable::getSortNo));
		}
		for (FilingScope filingScope : filingScopeList) {
			final ArchiveTree entity = new ArchiveTree();
			entity.setParentId(archiveTree.getId());
			entity.setNodeType(ArchiveTreeNodeEnum.FILING_SCOPE.getCode());
			entity.setTreeName(filingScope.getClassName());
			entity.setTreeValue(filingScope.getClassNo());
			entity.setArchiveTypeCode(filingScope.getTypeCode());
			entity.setFilingScopeId(filingScope.getId());
			ArchiveType archiveType = archiveTypeMap.get(filingScope.getTypeCode());
			if (ObjectUtil.isNotNull(archiveType)) {
				if (!templateTypeTopTableMap.containsKey(archiveType.getTemplateTypeId())) {
					List<TemplateTable> templateTableList = templateTables.stream().filter(e -> archiveType.getTemplateTypeId().equals(e.getTemplateTypeId())).collect(Collectors.toList());
					templateTypeTopTableMap.put(archiveType.getTemplateTypeId(), templateTableList.get(0));
				}
				TemplateTable templateTable = templateTypeTopTableMap.get(archiveType.getTemplateTypeId());
				entity.setTemplateTableId(templateTable.getId());
				entity.setLayerCode(templateTable.getLayerCode());
				entity.setFilingType(archiveType.getFilingType());
			}
			entity.setFondsCode(archiveTree.getFondsCode());
			entity.setFondsName(archiveTree.getFondsName());
			save(entity);
			// 保存子节点
			saveFilingScopeChildNodes(entity, filingScope.getId(), typeCodes, allMap, archiveTypeMap, templateTypeTopTableMap);
		}
	}

	private List<FilingScope> getTopFilingScopeList(Map<Long, List<FilingScope>> allMap, Long parentFilingScopeId) {
		final List<FilingScope> topFilingScopeList = new ArrayList<FilingScope>();
		// 0是归档范围树顶层父节点ID，如果归档范围模块顶层父节点ID发生变化，则要同时调整
		final Long key = null == parentFilingScopeId ? 0 : parentFilingScopeId;
		allMap.get(key).forEach(filingScope -> {
			if (0 == key) {
				//topFilingScopeList.add(filingScope); // 从根节点开始
				topFilingScopeList.addAll(allMap.get(filingScope.getId())); // 去掉根据节点
			} else {
				topFilingScopeList.add(filingScope);
			}
		});
		return topFilingScopeList;
	}

	private void processFilingScopeNodes(Long parentId, Map<Long, List<FilingScope>> allMap) {
		final List<FilingScope> filingScopeList = filingScopeService.getBaseMapper()
				.selectList(Wrappers.<FilingScope>lambdaQuery().eq(FilingScope::getParentClassId, parentId).orderByAsc(FilingScope::getId));
		if (null == filingScopeList || filingScopeList.isEmpty()) {
			return;
		}
		allMap.put(parentId, filingScopeList);
		filingScopeList.forEach(filingScope -> {
			processFilingScopeNodes(filingScope.getId(), allMap);
		});
	}

	private void processSortNo(ArchiveTree entity) {
		// 处理排序号
		Integer maxSortNo = baseMapper.getMaxSortNoByParentId(entity.getParentId());
		if (null == maxSortNo) {
			maxSortNo = 0;
		}
		entity.setSortNo(++maxSortNo);
	}

	private void processTreeCode(ArchiveTree entity) {
		final String maxTreeCode = baseMapper.getMaxTreeCodeByParentId(entity.getParentId());
		String curTreeCode = null;
		if (null == maxTreeCode) {
			if (-1L == entity.getParentId()) {
				curTreeCode = "000";
			} else {
				final ArchiveTree parentEntity = getParentEntity(entity);
				curTreeCode = StrBuilder.create(parentEntity.getTreeCode()).append("000").toString();
			}
		} else {
			if (!maxTreeCode.matches("\\d+")) {
				throw new ArchiveRuntimeException("同层节点中的最大树编码值中包含了非数字字符【" + maxTreeCode + "】，请联系管理员！");
			}
			final int len = maxTreeCode.length();
			final String prefixCode = maxTreeCode.substring(0, len - 3);
			final String suffixCode = maxTreeCode.substring(len - 3);
			final Integer suffixNo = Integer.parseInt(suffixCode);
			curTreeCode = getMaxTreeCode(entity.getParentId(), prefixCode, suffixNo);
		}
		entity.setTreeCode(curTreeCode);
	}

	private String getMaxTreeCode(Long parentId, String prefixCode, Integer suffixNo) {
		suffixNo++;
		if (suffixNo > 999) {
			suffixNo = 0;
		}
		final String treeCode = StrBuilder.create(prefixCode).append(formatTreeCode(suffixNo)).toString();
		if (0 == baseMapper.countByParentIdAndTreCode(parentId, treeCode)) {
			return treeCode;
		}
		log.debug("编码{}已存在", treeCode);
		return getMaxTreeCode(parentId, prefixCode, suffixNo++);
	}

	private String formatTreeCode(Integer suffixNo) {
		return String.format("%03d", suffixNo);
	}

	private boolean checkUnique(ArchiveTree entity) {
		final LambdaQueryWrapper<ArchiveTree> wrapper = Wrappers.<ArchiveTree>lambdaQuery();
		wrapper.eq(ArchiveTree::getParentId, entity.getParentId());
		wrapper.eq(ArchiveTree::getTreeName, entity.getTreeName());
		if (null != entity.getId()) {
			wrapper.ne(ArchiveTree::getId, entity.getId());
		}
		if (ObjectUtil.isNotNull(entity.getTenantId())) {
			wrapper.eq(ArchiveTree::getTenantId, entity.getTenantId());
		}

		return 0 == count(wrapper);
	}

	private void processParentNodeInCreate(ArchiveTree entity) {
		if (-1L == entity.getParentId()) {
			return;
		}
		final ArchiveTree parentEntity = getParentEntity(entity);
		// 处理是否为父节点
		if (parentEntity.getIsLeaf()) {
			parentEntity.setIsLeaf(false);
			getBaseMapper().updateById(parentEntity);
		}
		// 如果节点没有档案类型编码，同时父节点有档案类型编码，则继承父节点的档案类型编码
		if (StrUtil.isNotEmpty(parentEntity.getArchiveTypeCode()) && StrUtil.isEmpty(entity.getArchiveTypeCode())) {
			entity.setArchiveTypeCode(parentEntity.getArchiveTypeCode());
		}
	}

	/**
	 * @see com.baomidou.mybatisplus.extension.service.impl.ServiceImpl#updateById(java.lang.Object)
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(allEntries = true)
	public boolean updateById(ArchiveTree entity) {
		Optional.ofNullable(entity).orElseThrow(() -> new ArchiveRuntimeException("参数entity不能为null!"));
		// 检查节点名称在同层下是否唯一性
		if (!checkUnique(entity)) {
			throw new ArchiveRuntimeException("树节点名称已存在，请修改！");
		}
		final ArchiveTree oldEntity = getById(entity.getId());
		if (null == oldEntity) {
			throw new ArchiveRuntimeException("该节点已被删除，请刷新再操作！");
		}
		if (!oldEntity.getFondsCode().equals(entity.getFondsCode())) {
			judgeCanDelete(entity);
		}
		//修改只能修改树点名称
		oldEntity.setTreeName(entity.getTreeName());
		oldEntity.setFondsCode(entity.getFondsCode());
		oldEntity.setFondsName(entity.getFondsName());
		BeanUtil.copyProperties(oldEntity, entity);

		return super.updateById(entity);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(allEntries = true)
	public Boolean removeArchiveTree(Long[] ids) {
		for (Long id : ids) {
			// 级联删除所有子节点
			final ArchiveTree entity = getById(id);
			if (null == entity) {
				if (log.isDebugEnabled()) {
					log.debug("档案树ID[{}]不存在", id);
				}
				continue;
			}
			judgeCanDelete(entity);
			remove(Wrappers.<ArchiveTree>lambdaQuery().likeRight(ArchiveTree::getTreeCode, entity.getTreeCode()));
			// 处理是否为父节点
			processParentNodeAfterDelete(entity);
			// 如果根节点，则同步删除与全宗绑定关系
			if (ArchiveTreeNodeEnum.TREE_ROOT.getCode().equals(entity.getNodeType())) {
				fondsArchiveTreeService.remove(
						Wrappers.<FondsArchiveTree>lambdaQuery().eq(FondsArchiveTree::getArchiveTreeId, entity.getId()));
			}
		}
		return true;
	}

	@Override
	@Cacheable(
			key = "'archive-app-management:archive-tree:id:'+#id",
			unless = "#result == null"
	)
	public RenderTreeDTO getTreeById(Long id) {
		ArchiveTree archiveTree = this.getById(id);
		RenderTreeDTO renderTreeDTO = new RenderTreeDTO();
		BeanUtils.copyProperties(archiveTree, renderTreeDTO);
		if (ArchiveTreeNodeEnum.FILING_SCOPE.getCode().equals(archiveTree.getNodeType())) {
			FilingScope filingScope = filingScopeService.getById(archiveTree.getFilingScopeId());
			renderTreeDTO.setPath(filingScope.getPath());
			renderTreeDTO.setClassName(filingScope.getClassName());
		}
		return renderTreeDTO;
	}

	private void judgeCanDelete(ArchiveTree entity) {
		Map<String, Object> param = new HashMap<String, Object>() {
			{
				put("parent_id", entity.getId());
			}
		};
		// 存在子节点的时候不能删除
		Boolean has = dbOperateService.isExistDataByCondition("apma_archive_tree", param);
		if (has) {
			throw new ArchiveRuntimeException("存在子节点无法操作");
		}
		if (treeIsGrant(entity)) {
			throw new ArchiveRuntimeException(entity.getTreeName() + "已经被授权无法操作");
		}
	}

	private Boolean treeIsGrant(ArchiveTree entity) {
		R<Boolean> result = remoteSysAuthService.treeIsGrant(entity.getId());
		if (null == result || result.getCode() != CommonConstants.SUCCESS) {
			log.error("判断档案树ID[{}]是否被授权失败，{}", entity.getId(), null == result ? "" : result.getMsg());
			throw new ArchiveRuntimeException("判断档案树ID是否被授权失败");
		}
		return result.getData();
	}

	private void processParentNodeAfterDelete(ArchiveTree entity) {
		if (-1L == entity.getParentId()) {
			return;
		}
		if (count(Wrappers.<ArchiveTree>lambdaQuery().eq(ArchiveTree::getParentId, entity.getParentId())) > 0) {
			return;
		}
		final ArchiveTree parentEntity = getBaseMapper().selectById(entity.getParentId());
		// 处理是否为父节点
		if (null != parentEntity && !parentEntity.getIsLeaf()) {
			parentEntity.setIsLeaf(true);
			getBaseMapper().updateById(parentEntity);
		}
	}

	/*
	 *
	 * @see com.cescloud.saas.archive.service.modular.archivetree.service.ArchiveTreeService#updateFondsCode(java.lang.String, java.lang.String)
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateFondsCode(String oldFondsCode, String newFondsCode) {
		baseMapper.updateFondsCode(oldFondsCode, newFondsCode);
		return true;
	}

	/**
	 * @see com.cescloud.saas.archive.service.modular.archivetree.service.ArchiveTreeService#setDefaultTree(java.lang.Long)
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean setDefaultTree(Long id) {
		final List<ArchiveTree> entityList = getBaseMapper().selectList(Wrappers.<ArchiveTree>lambdaQuery()
				.eq(ArchiveTree::getNodeType, ArchiveTreeNodeEnum.TREE_ROOT.getCode()).eq(ArchiveTree::getIsDefault, true));
		if (null != entityList) {
			entityList.forEach(entity -> {
				entity.setIsDefault(false);
				getBaseMapper().updateById(entity);
			});
		}

		final ArchiveTree entity = getBaseMapper().selectById(id);
		if (null == entity) {
			throw new ArchiveRuntimeException(String.format("档案树（%d）已被删除，请刷新后再操作", id));
		}

		if (!ArchiveTreeNodeEnum.TREE_ROOT.getCode().equals(entity.getNodeType())) {
			throw new ArchiveRuntimeException(String.format("档案树（%s）不是树根节点", entity.getTreeName()));
		}
		entity.setIsDefault(true);

		getBaseMapper().updateById(entity);

		return true;
	}

	/**
	 * 判断档案树是否绑定了档案类型
	 *
	 * @param archiveTypeCode
	 * @return
	 */
	@Override
	public Boolean bindingArchiveType(String archiveTypeCode) {
		final int count = this.count(Wrappers.<ArchiveTree>query().lambda()
				.eq(ArchiveTree::getArchiveTypeCode, archiveTypeCode));
		return count == 0 ? Boolean.FALSE : Boolean.TRUE;
	}

	@Override
	public List<ArchiveTree> getTreeList(ArchiveTreeGetDTO archiveTree) {
		Long tenantId = TenantContextHolder.getTenantId();
		List<ArchiveType> fondLists = archiveTypeMapper.getFondsGroup(tenantId, archiveTree.getFondsCode());
		List<ArchiveTree> resList = new ArrayList<>();
		List<ArchiveTree> list = this.list(getLambdaQuery(archiveTree));
		if(list.size()>0){
			for(int i=0;i<list.size();i++){
				ArchiveTree resArchiveTree = list.get(i);
				if(StringUtil.isNotEmpty(resArchiveTree.getArchiveTypeCode())){
					for(int j=0;j<fondLists.size();j++){
						ArchiveType archiveType = fondLists.get(j);
						if(resArchiveTree.getArchiveTypeCode().equals(archiveType.getTypeCode())){
							resArchiveTree.setArchiveTypeName(archiveType.getTypeName());
							resList.add(resArchiveTree);
						}
					}

				}else {
					resList.add(resArchiveTree);
				}
			}
		}

		return resList;
	}

	@Override
	public IPage<List<ArchiveTree>> getTreePage(Page page, ArchiveTreeGetDTO archiveTree) {
		return this.page(page, getLambdaQuery(archiveTree));
	}

	/**
	 * 组装成查询过滤条件
	 *
	 * @param archiveTree
	 * @return
	 */
	private LambdaQueryWrapper<ArchiveTree> getLambdaQuery(ArchiveTreeGetDTO archiveTree) {
		final LambdaQueryWrapper<ArchiveTree> lambdaQuery = Wrappers.lambdaQuery();
		if (StrUtil.isNotBlank(archiveTree.getFondsCode())) {
			lambdaQuery.eq(ArchiveTree::getFondsCode, archiveTree.getFondsCode());
		}
		if (null != archiveTree.getParentId()) {
			lambdaQuery.eq(ArchiveTree::getParentId, archiveTree.getParentId());
		}
		if (StrUtil.isNotEmpty(archiveTree.getNodeType())) {
			lambdaQuery.eq(ArchiveTree::getNodeType, archiveTree.getNodeType());
		}
		if (StrUtil.isNotEmpty(archiveTree.getKeyword())) {
			lambdaQuery.like(ArchiveTree::getTreeName, archiveTree.getKeyword());
		}
		//默认父节点和序号排序
		lambdaQuery.orderByAsc(ArchiveTree::getParentId, ArchiveTree::getSortNo);
		return lambdaQuery;
	}

	@Override
	public List<ArchiveTree> getArchiveTreeByParentId(Long parentId) {
		return baseMapper.getArchiveTreeByParentId(parentId);
	}

	@Override
	public R initializeArchiveTree(Long templateId, Long tenantId) {
		//初始化档案树信息
		final R r = initializeTree(templateId, tenantId);
		//初始化档案节点信息
		final R r1 = initializeTreeNode(templateId, tenantId);
		return (r.getCode() == CommonConstants.SUCCESS && r1.getCode() == CommonConstants.SUCCESS) ? r
				: new R().fail(null, "初始化树信息失败！！！");
	}

	@Transactional(rollbackFor = Exception.class)
	public R initializeTree(Long templateId, Long tenantId) {
		ExcelReader excel = null;
		try {
			final InputStream inputStream = getDefaultTemplateStream(templateId);
			if (ObjectUtil.isNull(inputStream)) {
				return new R<>().fail("", "获取初始化文件异常");
			}
			//closeAfterReader设为true，创建工作簿后会关闭inputStream流
			excel = new ExcelReader(inputStream, TemplateFieldConstants.SHEET_NAMES.ARCHIVE_TREE_NAME);
			final List<List<Object>> read = excel.read();
			//循环行
			boolean batch = false;
			for (int i = 1, length = read.size(); i < length; i++) {
				//获取树名称
				final String treeName = StrUtil.toString(read.get(i).get(0));
				final String fondsCode = StrUtil.toString(read.get(i).get(1));
				final String fondsName = StrUtil.toString(read.get(i).get(2));

				final ArchiveTree archiveTree = ArchiveTree.builder().treeName(treeName).tenantId(tenantId)
						.nodeType(ArchiveTreeNodeEnum.TREE_ROOT.getCode()).fondsCode(fondsCode)
						.fondsName(fondsName).parentId(-1L).sortNo(i)
						.build();
				// 处理编码值
				processTreeCode(archiveTree);
				batch = this.save(archiveTree);
			}
			return batch ? new R("", "初始化档案树成功") : new R().fail(null, "初始化档案树失败！！");
		} finally {
			IoUtil.close(excel);
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public R initializeTreeNode(Long templateId, Long tenantId) {
		ExcelReader excel = null;
		try {
			final InputStream inputStream = getDefaultTemplateStream(templateId);
			if (ObjectUtil.isNull(inputStream)) {
				return new R<>().fail("", "获取初始化文件异常");
			}
			//closeAfterReader设为true，创建工作簿后会关闭inputStream流
			excel = new ExcelReader(inputStream, TemplateFieldConstants.SHEET_NAMES.ARCHIVE_TREE_NODE_NAME);
			final List<List<Object>> read = excel.read();
			//对表单表头校验
			final Map<Integer, String> map = InitializeUtil.checkHeader(TemplateFieldConstants.ARCHIVE_TREE_LIST, read.get(0));
			if (CollectionUtils.isEmpty(map)) {
				return new R<>().fail("", "模板表列数据不匹配！！！");
			}
			//新增数据
			final Boolean aBoolean = insertTreeNode(map, read, tenantId);
			if (aBoolean) {
				return new R("", "成功");
			} else {
				return new R().fail(null, "初始化树失败");
			}
		} finally {
			IoUtil.close(excel);
		}

	}

	/**
	 * 初始化树新增操作
	 *
	 * @param map      表单表头校验后数据集
	 * @param read     excel数据
	 * @param tenantId 租户id
	 * @return
	 */
	private Boolean insertTreeNode(Map<Integer, String> map, List<List<Object>> read, Long tenantId) {
		final List<ArchiveTree> archiveTrees = CollUtil.newArrayList();
		final List<ArchiveTree> archiveChildrenTrees = CollUtil.newArrayList();
		//获取所有的档案类型
		final List<ArchiveType> archiveTypes = archiveTypeService
				.list(Wrappers.<ArchiveType>lambdaQuery().eq(ArchiveType::getTenantId, tenantId));
		final List<FilingScope> filingScopes = filingScopeService
				.list(Wrappers.<FilingScope>lambdaQuery().eq(FilingScope::getTenantId, tenantId));
		final List<ArchiveTree> archiveRootTrees = this.list(Wrappers.<ArchiveTree>lambdaQuery()
				.eq(ArchiveTree::getTenantId, tenantId).eq(ArchiveTree::getParentId, -1));
		//获取档案模板信息
		final List<TemplateTable> templateTables = templateTableService
				.list(Wrappers.<TemplateTable>lambdaQuery().eq(TemplateTable::getTenantId, tenantId));

		//顶级档案类型树
		ArchiveTree topTree = null;
		//循环行
		for (int i = 1, length = read.size(); i < length; i++) {
			//树层级	节点名称	是否显示层级	上级节点	节点类型	档案门类表模板	层级编码
			//数据处理
			final Map<String, String> dataMap = InitializeUtil.dataTreating(map, TemplateFieldConstants.ARCHIVE_TREE_LIST, read.get(i));
			if (CollectionUtils.isEmpty(dataMap)) {
				throw new ArchiveRuntimeException("初始化树异常");
			}
			//获取上级节点
			final String superior = dataMap.get(TemplateFieldConstants.ARCHIVE_TREE.PARENT_ID);
			topTree = archiveRootTrees.stream().filter(archiveTree -> archiveTree.getTreeName().equals(superior)).findAny().orElse(null);
			//作为区分节点类型
			final String treeName = dataMap.get(TemplateFieldConstants.ARCHIVE_TREE.TREE_NAME);
			//获取层级
			final String layerCode = dataMap.get(TemplateFieldConstants.ARCHIVE_TREE.LAYER_CODE);
			final String tableTemplate = dataMap.get(TemplateFieldConstants.ARCHIVE_TREE.TABLE_TEMPLATE);
			if (ArchiveTreeNodeEnum.TREE_ROOT.getName().equals(dataMap.get(TemplateFieldConstants.ARCHIVE_TREE.NODE_TYPE))) {
				continue;
			} else {
				//新增节点
				final ArchiveTree archiveTree = new ArchiveTree();
				//先判断节点类型  获取编码
				final String nodeType = dataMap.get(TemplateFieldConstants.ARCHIVE_TREE.NODE_TYPE);
				final String fondsCode = dataMap.get(TemplateFieldConstants.ARCHIVE_TREE.FONDS_CODE);
				final String fondsName = dataMap.get(TemplateFieldConstants.ARCHIVE_TREE.FONDS_NAME);
				final String isLeaf = dataMap.get(TemplateFieldConstants.ARCHIVE_TREE.IS_LEAF);
				archiveTree.setIsLeaf("是".equals(isLeaf));
				archiveTree.setTreeName(treeName);
				archiveTree.setFondsCode(fondsCode);
				archiveTree.setFondsName(fondsName);
				archiveTree.setTenantId(tenantId);
				archiveTree.setSortNo(i);
				if (ArchiveTreeNodeEnum.CLAZZ.getName().equals(nodeType)) {
					//分类节点
					archiveTree.setNodeType(ArchiveTreeNodeEnum.CLAZZ.getCode());
				} else if (ArchiveTreeNodeEnum.ARCHIVE_TYPE.getName().equals(nodeType)) {
					//档案门类节点
					archiveTree.setNodeType(ArchiveTreeNodeEnum.ARCHIVE_TYPE.getCode());
					final ArchiveType archive = archiveTypes.parallelStream()
							.filter(at -> StrUtil.equals(at.getTypeName(), treeName)).findAny().orElseGet(()->new ArchiveType());
					archiveTree.setArchiveTypeCode(archive.getTypeCode());
					archiveTree.setTreeValue(archive.getTypeCode());
					archiveTree.setFilingType(archive.getFilingType());
					archiveTree.setTemplateTableId(archive.getTemplateTypeId());
					if (StrUtil.isNotBlank(layerCode)) {
						//过滤层级
						final TemplateTable templateTable1 = templateTables.parallelStream()
								.filter(
										templateTable -> templateTable.getTemplateTypeId().equals(archive.getTemplateTypeId())
												&& templateTable.getName().equals(tableTemplate))
								.findAny().orElseGet(()->new TemplateTable());
						archiveTree.setLayerCode(templateTable1.getLayerCode());
						archiveTree.setTemplateTableId(templateTable1.getId());
					}
				} else if (ArchiveTreeNodeEnum.FILING_SCOPE.getName().equals(nodeType)) {
					//归档范围节点
					archiveTree.setNodeType(ArchiveTreeNodeEnum.FILING_SCOPE.getCode());
					final FilingScope filingScope = filingScopes.parallelStream()
							.filter(fs -> StrUtil.equals(fs.getClassName(), treeName)).findAny().orElseGet(()-> new FilingScope());
					final ArchiveType archiveType1 = archiveTypes.parallelStream().filter(archiveType -> archiveType.getTypeCode().equals(filingScope.getTypeCode())).findAny().orElseGet(()->new ArchiveType());
					//过滤层级
					final TemplateTable templateTable1 = templateTables.parallelStream()
							.filter(templateTable -> templateTable.getTemplateTypeId().equals(archiveType1.getTemplateTypeId()) && templateTable.getName().equals(tableTemplate)).findAny().orElseGet(()->new TemplateTable());
					archiveTree.setFilingScopeId(filingScope.getId());
					archiveTree.setArchiveTypeCode(filingScope.getTypeCode());
					archiveTree.setTreeValue(filingScope.getClassNo());
					archiveTree.setFilingType(archiveType1.getFilingType());
					archiveTree.setLayerCode(templateTable1.getLayerCode());
					archiveTree.setTemplateTableId(templateTable1.getId());
				}
				if (ObjectUtil.isNotNull(topTree)) {
					archiveTree.setParentId(topTree.getId());
					archiveTree.setTreeCode(topTree.getTreeCode() + String.format("%03d", i - 1));
				} else {
					archiveTree.setParentTreeName(superior);
					archiveChildrenTrees.add(archiveTree);
					continue;
				}
				archiveTrees.add(archiveTree);
			}
		}
		boolean batch = Boolean.FALSE;
		if (CollUtil.isNotEmpty(archiveTrees)) {
			//批量新增操作
			batch = this.saveBatch(archiveTrees);
		}
		List<ArchiveTree> filingScopeTree = archiveTrees.stream().filter(e -> ArchiveTreeNodeEnum.FILING_SCOPE.getCode().equals(e.getNodeType())).collect(Collectors.toList());

		//保存归档范围树子节点
		if (CollUtil.isNotEmpty(archiveChildrenTrees)) {
			saveChildrenTreeNode(filingScopeTree, archiveChildrenTrees);
		}
		return batch;
	}

	private void saveChildrenTreeNode(List<ArchiveTree> filingScopeTree, List<ArchiveTree> archiveChildrenTrees) {
		List<ArchiveTree> childrenTreeNodes = new ArrayList<>();
		filingScopeTree.forEach(e -> {
			for (int i = 0, size = archiveChildrenTrees.size(); i < size; i++) {
				ArchiveTree childrenArchiveTree = archiveChildrenTrees.get(i);
				if (e.getTreeName().equals(childrenArchiveTree.getParentTreeName())) {
					childrenArchiveTree.setParentId(e.getId());
					childrenArchiveTree.setTreeCode(e.getTreeCode() + String.format("%03d", i));
					childrenTreeNodes.add(childrenArchiveTree);
				}
			}
		});
		this.saveBatch(childrenTreeNodes);
		List<ArchiveTree> lastChildrenTreeNodes = archiveChildrenTrees.stream().filter(e -> ObjectUtil.isNull(e.getId())).collect(Collectors.toList());
		if (CollUtil.isNotEmpty(lastChildrenTreeNodes)) {
			saveChildrenTreeNode(childrenTreeNodes, lastChildrenTreeNodes);
		}
	}

	/**
	 * 获取 初始化模板文件流
	 *
	 * @param templateId 模板id
	 * @return
	 */
	private InputStream getDefaultTemplateStream(Long templateId) {
		final TenantTemplate tenantTemplate = remoteTenantTemplateService.selectTemplateById(templateId).getData();
		final byte[] bytes = (byte[]) tenantTemplate.getTemplateContent();
		final InputStream inputStream = new ByteArrayInputStream(bytes);
		return inputStream;
	}

	@Override
	public Metadata getDeptMetedata() {
		final String type = getAuthorityPolicyType();
		if (StrUtil.isBlank(type)) {
			return Metadata.builder()
					.metadataChinese(FILING_DEPT_CHINESE)
					.metadataEnglish(FieldConstants.FILING_DEPT)
					.build();
		}
		Metadata deptMetadata;
		if (type.equals(BoolEnum.NO.getCode().toString())) {
			deptMetadata = Metadata.builder()
					.metadataChinese(FILING_DEPT_CHINESE)
					.metadataEnglish(FieldConstants.FILING_DEPT)
					.build();
		} else {
			deptMetadata = Metadata.builder()
					.metadataChinese(CREATE_DEPT_CHINESE)
					.metadataEnglish(FieldConstants.CREATED_DEPT)
					.build();
		}
		return deptMetadata;
	}

	/**
	 * 获取数据来源。
	 * （0：归档人、归档部门， 1：创建人、创建部门）
	 *
	 * @return
	 */
	private String getAuthorityPolicyType() {
		// TODO: 2020/7/1 去掉了这个参数配置
		/*final R<SysSetting> result = remoteSysSettingService
				.getSysSettingByCode(SysSettingCodeEnum.AUTHORITYPOLICY.getCode());
		if (result.getCode() == CommonConstants.SUCCESS) {
			final SysSetting sysSetting = result.getData();
			if (ObjectUtil.isNotNull(sysSetting)) {
				return sysSetting.getValue();
			}
		}*/
		return "0";
	}

	private String getDeptFieldByPolicyType() {
		final String type = getAuthorityPolicyType();
		if (StrUtil.isBlank(type)) {
			return FieldConstants.FILING_DEPT;
		}
		return type.equals(BoolEnum.NO.getCode().toString()) ? FieldConstants.FILING_DEPT : FieldConstants.CREATED_DEPT;
	}

	@Override
	public List<ArchiveTree> getTreeDataList(String fondsCode) {
		// 档案树增加全宗号后，只需要一条SQL就能查出
		return this.list(Wrappers.<ArchiveTree>lambdaQuery().eq(ArchiveTree::getFondsCode, fondsCode));
	}

	/**
	 * 判断是否包括动态数据节点
	 *
	 * @param archiveTreeList
	 * @return
	 */
	@Override
	public Boolean hasDynamicTreeNode(List<ArchiveTree> archiveTreeList) {
		return archiveTreeList.stream()
				.filter(archiveTree -> ArchiveTreeNodeEnum.DYNAMIC.getCode().equals(archiveTree.getNodeType()))
				.findAny().map(archiveTree -> ArchiveTreeNodeEnum.DYNAMIC.getCode().equals(archiveTree.getNodeType()))
				.orElse(Boolean.FALSE);
	}

	@Override
	public List<ArchiveTree> convertDynamicTreeNode(List<ArchiveTree> archiveTreeList, String filter, String fondsCode,String path) {
		final List<ArchiveTree> allArchiveTreeList = new ArrayList<>();
		archiveTreeList.forEach(archiveTree -> {
			if (ArchiveTreeNodeEnum.DYNAMIC.getCode().equals(archiveTree.getNodeType())) {
				allArchiveTreeList.addAll(getDynamicDataNode(archiveTree, filter, fondsCode,path));
			} else {
				allArchiveTreeList.add(archiveTree);
			}
		});
		return allArchiveTreeList;
	}

	private List<ArchiveTree> getDynamicDataNode(ArchiveTree archiveTree, String filter, String fondsCode,String path) {
		try {
			final String storageLocate = archiveTableService.getStorageLocateByArchiveTypeCodeAndTemplateTableId(
					archiveTree.getArchiveTypeCode(), archiveTree.getTemplateTableId());
			final StringBuilder where = new StringBuilder();
			where.append(null == filter ? "" : filter.replaceAll(";", " and "));
			//根据path来过滤当前归档范围以及子范围的所有数据
			if (StrUtil.isNotEmpty(path)) {
				where.append(" and ").append( FieldConstants.PATH)
						.append(" like '").append(path).append("%'");
			}
			//过滤已删除的数据
			where.append(" and ").append(FieldConstants.IS_DELETE).append(" = 0");

			final DynamicArchiveDTO dto = new DynamicArchiveDTO();
			dto.setTableName(storageLocate);
			final List<String> columnList = new ArrayList<>();
			columnList.add("distinct " + archiveTree.getMetadataEnglish());
			dto.setFilterColumn(columnList);
			dto.setWhere(where.toString());
			dto.setFondsCode(fondsCode);
			final R<List<Map<String, Object>>> result = remoteArchiveService.getListByCondition(dto, Boolean.TRUE);
			if (null == result || result.getCode() != CommonConstants.SUCCESS) {
				log.error("查询动态数据节点失败");
				throw new ArchiveRuntimeException("查询动态数据节点失败");
			}

			final List<Object> dataList = result.getData().stream().filter(stringObjectMap -> null != stringObjectMap)
					.map(stringObjectMap -> stringObjectMap.get(archiveTree.getMetadataEnglish()))
					.collect(Collectors.toList());
			//添加动态节点字段为空的分类
			for (Map<String, Object> datum : result.getData()) {
				if (datum == null || datum.get(archiveTree.getMetadataEnglish()) == null || datum.get(archiveTree.getMetadataEnglish()) == "") {
					String trendsNodeName = archiveTree.getTreeName().substring
							(archiveTree.getTreeName().indexOf("[") + 1,archiveTree.getTreeName().indexOf("]"));
					dataList.add(trendsNodeName + "待填");
					break;
				}
			}
			//对动态节点分类显示加“{}”
//			List<Object> dataListNew = new ArrayList<>();
//			if(dataList != null) {
//				for (Object data : dataList) {
//					if (data != null || data != "") {
//						dataListNew.add("{" + data + "}");
//					}
//				}
//			}
			return createDynamicArchiveTree(archiveTree, dataList);
		} catch (final Exception e) {
			log.error("创建动态档案树数据节点失败");
			return Collections.emptyList();
		}

	}

	private List<ArchiveTree> createDynamicArchiveTree(ArchiveTree archiveTree, List<Object> dataList) {
		if (CollUtil.isEmpty(dataList)) {
			return Collections.emptyList();
		}
		return dataList.stream().filter(o -> null != o).map(obj -> {
			final ArchiveTree tree = new ArchiveTree();
			BeanUtils.copyProperties(archiveTree, tree);
			tree.setTreeName(String.valueOf(obj));
			tree.setTreeValue(String.valueOf(obj));
			return tree;
		}).collect(Collectors.toList());
	}

	/**
	 * 根据全宗获取档案树根节点
	 *
	 * @param fondsCode
	 * @return
	 */
	private ArchiveTree getTreeRootByFondsCode(String fondsCode) throws ArchiveBusinessException {
		//获取全宗绑定档案树跟节点
		final List<FondsArchiveTree> fondsArchiveTree = fondsArchiveTreeService
				.getFondsArchiveTreeByFondsCode(fondsCode);
		if (CollUtil.isNotEmpty(fondsArchiveTree)) {
			final Long archiveTreeRootId = fondsArchiveTree.get(0).getArchiveTreeId();
			return this.getById(archiveTreeRootId);
		}
		final List<ArchiveTree> list = this.list(Wrappers.<ArchiveTree>query().lambda()
				.eq(ArchiveTree::getNodeType, ArchiveTreeNodeEnum.TREE_ROOT.getCode()));
		//获取默认档案树根节点
		if (CollUtil.isNotEmpty(list)) {
			ArchiveTree root = null;
			final Optional<ArchiveTree> optionalArchiveTree = list.stream()
					.filter(archiveTree -> archiveTree.getIsDefault()).findAny();
			if (optionalArchiveTree.isPresent()) {
				root = optionalArchiveTree.get();
			} else {
				root = list.get(0);
			}
			return root;
		} else {
			throw new ArchiveBusinessException("该租户未设置档案树！");
		}
	}

	@Override
	public List<ArrayList<String>> getArchivesTreeInfo(Long tenantId) {
		//获取档案树信息
		final List<ArchiveTree> archiveTrees = this.list(Wrappers.<ArchiveTree>lambdaQuery()
				.eq(ArchiveTree::getParentId, -1L).eq(ArchiveTree::getTenantId, tenantId));
		//所需列表
		// 树名称	全宗号   全宗名称
		final List<ArrayList<String>> collect = archiveTrees.stream().map(archiveTree -> CollUtil
				.newArrayList(archiveTree.getTreeName(), archiveTree.getFondsCode(), archiveTree.getFondsName()))
				.collect(Collectors.toList());
		return collect;
	}

	@Override
	public List<ArrayList<String>> getArchivesTreeNodeInfo(Long tenantId) {
		//获取档案信息
		final List<ArchiveTree> archiveTrees = this
				.list(Wrappers.<ArchiveTree>lambdaQuery().eq(ArchiveTree::getTenantId, tenantId));
		//取 树id ，树名称
		final Map<Long, String> terssMap = archiveTrees.stream()
				.collect(Collectors.toMap(ArchiveTree::getId, ArchiveTree::getTreeName));
		//获取表模板信息
		final List<TemplateTable> templateTables = templateTableService
				.list(Wrappers.<TemplateTable>lambdaQuery().eq(TemplateTable::getTenantId, tenantId));
		final Map<Long, String> templateTableMap = templateTables.stream()
				.collect(Collectors.toMap(TemplateTable::getId, TemplateTable::getName));
		//所需列表
		//树层级	节点名称	是否显示层级	上级节点	节点类型	档案门类表模板	层级编码  全宗号  全宗名称  是否为子节点
		final List<ArrayList<String>> collect = archiveTrees.stream()
				.map(archiveTree -> CollUtil.newArrayList(processingTreeHierarchy(null, archiveTree.getId(), archiveTrees),
						archiveTree.getTreeName(), Boolean.FALSE.equals(archiveTree.getShowLayer()) ? "否" : "是",
						StrUtil.isNotBlank(terssMap.get(archiveTree.getParentId())) ? terssMap.get(archiveTree.getParentId())
								: TemplateFieldConstants.NOT,
						ArchiveTreeNodeEnum.getEnum(archiveTree.getNodeType()).getName(),
						ObjectUtil.isNotNull(archiveTree.getTemplateTableId())
								? templateTableMap.get(archiveTree.getTemplateTableId())
								: StrUtil.EMPTY,
						archiveTree.getLayerCode(), archiveTree.getFondsCode(), archiveTree.getFondsName(), archiveTree.getIsLeaf() ? "是" : "否"))
				.collect(Collectors.toList());
		return collect;
	}

	/**
	 * 拼接树层级
	 *
	 * @param prantName    父名称
	 * @param id           当前id
	 * @param archiveTrees 数据集
	 * @return
	 */
	private String processingTreeHierarchy(String prantName, Long id, List<ArchiveTree> archiveTrees) {
		final ArchiveTree currentTree = archiveTrees.stream().filter(archiveTree -> id.equals(archiveTree.getId()))
				.findAny().orElse(null);
		if (ObjectUtil.isNull(currentTree)) {
			return TemplateFieldConstants.NOT;
		}
		if (-1 == currentTree.getParentId()) {
			return StrUtil.isNotBlank(prantName) ? currentTree.getTreeName() + "-" + prantName
					: currentTree.getTreeName();
		} else {
			final String treeName = currentTree.getTreeName();
			prantName = StrUtil.isNotBlank(prantName) ? treeName + "-" + prantName : treeName;
			return processingTreeHierarchy(prantName, currentTree.getParentId(), archiveTrees);
		}
	}

	/**
	 * @see com.cescloud.saas.archive.service.modular.archivetree.service.ArchiveTreeService#switchShowLayer(Long, String)
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(allEntries = true)
	public boolean switchShowLayer(Long id, String fondsCode) {
		//获取所有已经授权这个 点击的节点 的角色 ，然后后面根据角色向sys_archive_tree_auth表插入数据
		List<Long> role = remoteSysAuthService.getRole(id);
		final ArchiveTree entity = getById(id);
		if (BooleanUtil.isTrue(entity.getShowLayer())) {
			if (!checkFieldNode(entity)) {
				throw new ArchiveRuntimeException("该树下有动态节点，不能关闭层级节点！");
			}
			generateParentNodes(id, fondsCode, Boolean.TRUE, role);
			deleteLayerNodes(entity);
			baseMapper.updateArchiveNode(entity.getTreeCode(), Boolean.FALSE, true);
			entity.setShowLayer(Boolean.FALSE);
		} else {
			if (!checkFieldNode(entity)) {
				throw new ArchiveRuntimeException("该树下有动态节点，不能开启层级节点！");
			}
			//生成所有档案门类节点下所有层级节点
			autoGenerateAllLayerNodes(entity);
			baseMapper.updateArchiveNode(entity.getTreeCode(), Boolean.FALSE, false);
			entity.setShowLayer(Boolean.TRUE);
			generateParentNodes(id, fondsCode, Boolean.FALSE, role);
		}
		return super.updateById(entity);
	}

	@Override
	public boolean archiveTreeOrder(List<Long> ids) {
		List<ArchiveTree> archiveTrees = this.list(Wrappers.<ArchiveTree>lambdaQuery().in(ArchiveTree::getId, ids));
		final List<ArchiveTree> list = IntStream.rangeClosed(1, ids.size()).mapToObj(i -> {
			final ArchiveTree archiveTree = archiveTrees.parallelStream().filter(e -> e.getId().equals(ids.get(i - 1))).findAny().get();
			archiveTree.setSortNo(i);
			return archiveTree;
		}).collect(Collectors.toList());
		return this.updateBatchById(list);
	}

	@Override
	public List<FondsArchiveTreeSyncTreeNode> getTreeGrid() {
		//被授权的全宗
		List<Fonds> fondsList = fondsService.getFondsList();
		fondsList.add(0, FondsConstant.getGlobalFonds());
		//被授权的全宗号
		List<String> authFondsCode = fondsList.stream().map(Fonds::getFondsCode).collect(Collectors.toList());
		List<ArchiveTree> archiveTreeList = this.list(Wrappers.<ArchiveTree>lambdaQuery().in(ArchiveTree::getFondsCode, authFondsCode).eq(ArchiveTree::getParentId, -1));
		//转换为树节点
		List<FondsArchiveTreeSyncTreeNode> fondsTreeNodes = convertFondsTreeNode(fondsList);
		List<FondsArchiveTreeSyncTreeNode> archiveTreeNodes = convertTreeNode(archiveTreeList);
		//组装成树结构
		Map<String, List<FondsArchiveTreeSyncTreeNode>> fondsTreeMap = archiveTreeNodes.stream().collect(Collectors.groupingBy(FondsArchiveTreeSyncTreeNode::getFondsCode));
		fondsTreeNodes.stream().forEach(treeNode -> {
			if (fondsTreeMap.containsKey(treeNode.getFondsCode())) {
				treeNode.setChildren(fondsTreeMap.get(treeNode.getFondsCode()));
				treeNode.setIsLeaf(false);
			} else {
				treeNode.setIsLeaf(true);
			}
		});
		return fondsTreeNodes;
	}

	@Override
	public List<FondsArchiveTreeSyncTreeNode> getFondsNode() {
		List<String> fondsCodeList = CollUtil.newArrayList();
		if (archiveUtil.isAuthorityFilter()) {
			fondsCodeList.add(0, FondsConstant.GLOBAL_FONDS_CODE);
			fondsCodeList.addAll(archiveUtil.getAuthFondsCode());
		}
		List<ArchiveTree> distinctFondsCode = this.getBaseMapper().getDistinctFondsCode(fondsCodeList);
		return convertFondsTreeNodeForArchiveTree(distinctFondsCode);
	}

	private List<FondsArchiveTreeSyncTreeNode> convertFondsTreeNodeForArchiveTree(List<ArchiveTree> distinctFondsCode) {
		return distinctFondsCode.stream().map(fonds -> {
			FondsArchiveTreeSyncTreeNode treeNode = new FondsArchiveTreeSyncTreeNode();
			treeNode.setNodeClass(FondsConstant.FONDS_TREE_CLASS);
			treeNode.setPk(FondsConstant.FONDS_TREE_CLASS + StrUtil.DASHED + fonds.getFondsCode());
			treeNode.setFondsCode(fonds.getFondsCode());
			treeNode.setArchiveTreeId(ArchiveConstants.TREE_FONDS_TYPE_NODE);
			treeNode.setName(fonds.getFondsName());
			treeNode.setNodeType(FondsConstant.FONDS_TREE_CLASS);
			return treeNode;
		}).collect(Collectors.toList());
	}

	@Override
	public List<FondsArchiveTreeSyncTreeNode> getArchiveTreeNode(String fondsCode, Long archiveTreeId) {
		List<ArchiveTree> archiveTreeList = this.list(Wrappers.<ArchiveTree>lambdaQuery()
				.eq(ArchiveTree::getFondsCode, fondsCode).eq(ArchiveTree::getParentId, archiveTreeId)
				.orderByAsc(ArchiveTree::getSortNo));
		return convertTreeNode(archiveTreeList);
	}

	@Override
	public List<ArchiveTree> getArchiveTreeByFilingScopeOrArchiveType(List<String> archiveTypeCodeList, List<Long> filingScopeIdList) {
		final LambdaQueryWrapper<ArchiveTree> lambdaQuery = Wrappers.<ArchiveTree>lambdaQuery();
		if (CollUtil.isNotEmpty(archiveTypeCodeList)) {
			lambdaQuery.in(ArchiveTree::getArchiveTypeCode, archiveTypeCodeList);
		}
		if (CollUtil.isNotEmpty(filingScopeIdList)) {
			lambdaQuery.in(ArchiveTree::getFilingScopeId, filingScopeIdList);
		}
		return this.list(lambdaQuery);
	}

	private List<FondsArchiveTreeSyncTreeNode> convertFondsTreeNode(List<Fonds> fondsList) {
		return fondsList.stream().map(fonds -> {
			FondsArchiveTreeSyncTreeNode treeNode = new FondsArchiveTreeSyncTreeNode();
			treeNode.setNodeClass(FondsConstant.FONDS_TREE_CLASS);
			treeNode.setPk(FondsConstant.FONDS_TREE_CLASS + StrUtil.DASHED + fonds.getFondsCode());
			treeNode.setFondsCode(fonds.getFondsCode());
			treeNode.setArchiveTreeId(ArchiveConstants.TREE_FONDS_TYPE_NODE);
			treeNode.setName(fonds.getFondsName());
			treeNode.setNodeType(FondsConstant.FONDS_TREE_CLASS);
			return treeNode;
		}).collect(Collectors.toList());
	}

	private List<FondsArchiveTreeSyncTreeNode> convertTreeNode(List<ArchiveTree> archiveTreeList) {
		return archiveTreeList.stream().map(archiveTree -> {
			FondsArchiveTreeSyncTreeNode treeNode = new FondsArchiveTreeSyncTreeNode();
			treeNode.setNodeClass(FondsConstant.ARCHIVE_TREE_CLASS);
			treeNode.setPk(FondsConstant.ARCHIVE_TREE_CLASS + StrUtil.DASHED + archiveTree.getId());
			treeNode.setFondsCode(archiveTree.getFondsCode());
			treeNode.setName(archiveTree.getTreeName());
			treeNode.setArchiveTreeId(archiveTree.getId());
			treeNode.setShowLayer(archiveTree.getShowLayer());
			treeNode.setNodeType(archiveTree.getNodeType());
			return treeNode;
		}).collect(Collectors.toList());
	}

	private void autoGenerateAllLayerNodes(ArchiveTree rootEntity) {
		final LambdaQueryWrapper<ArchiveTree> lambdaQuery = Wrappers.<ArchiveTree>lambdaQuery();
		lambdaQuery.likeRight(ArchiveTree::getTreeCode, rootEntity.getTreeCode());
		lambdaQuery.eq(ArchiveTree::getNodeType, ArchiveTreeNodeEnum.ARCHIVE_TYPE.getCode());
		final List<ArchiveTree> entityList = list(lambdaQuery);
		if (null == entityList || entityList.isEmpty()) {
			return;
		}
		final Map<Long, Map<Long, TemplateTable>> templateTypeTableMap = Maps.newHashMap();
		entityList.forEach(entity -> {
			try {
				final ArchiveType archiveType = archiveTypeService.getByTypeCode(entity.getArchiveTypeCode());
				saveLayerTableNodes(entity, archiveType, templateTypeTableMap);
			} catch (final ArchiveBusinessException e) {
				throw new ArchiveRuntimeException(e);
			}
		});
	}

	/**
	 * 删除单棵树下所有层级节点
	 *
	 * @param entity
	 */
	private void deleteLayerNodes(ArchiveTree entity) {
		final LambdaQueryWrapper<ArchiveTree> lambdaQuery = Wrappers.<ArchiveTree>lambdaQuery();

		lambdaQuery.likeRight(ArchiveTree::getTreeCode, entity.getTreeCode());
		lambdaQuery.eq(ArchiveTree::getNodeType, ArchiveTreeNodeEnum.LAYER.getCode());
		remove(lambdaQuery);
	}

	/**
	 * 检索是否有字段节点（部门节点、动态节点）
	 *
	 * @param entity
	 * @return
	 */
	private boolean checkFieldNode(ArchiveTree entity) {
		final LambdaQueryWrapper<ArchiveTree> lambdaQuery = Wrappers.<ArchiveTree>lambdaQuery();

		lambdaQuery.likeRight(ArchiveTree::getTreeCode, entity.getTreeCode());
		lambdaQuery.and(consumer -> {
			consumer.or().eq(ArchiveTree::getNodeType, ArchiveTreeNodeEnum.DEPT.getCode()).or()
					.eq(ArchiveTree::getNodeType, ArchiveTreeNodeEnum.DYNAMIC.getCode());
		});

		return 0 == count(lambdaQuery);
	}

	/**
	 * 打开层级显示时 向sys_archive_tree_auth 表插入生成的子节点数据
	 * 关闭层级显示时 传入true，删除掉曾经生成的子节点数据
	 *
	 * @param id             id
	 * @param fondsCode      全局还是自己全宗
	 * @param insertOrDelete 插入还是删除
	 */
	public void generateParentNodes(Long id, String fondsCode, Boolean insertOrDelete, List<Long> role) {
		//遍历所有角色选中或删除父节点 (类似文书档案这样的节点)
		role.forEach(e -> {
			LambdaQueryWrapper<ArchiveTree> wrapper = new LambdaQueryWrapper<>();
			wrapper.eq(ArchiveTree::getParentId, id).eq(ArchiveTree::getFondsCode, fondsCode);
			List<ArchiveTree> list2 = this.list(wrapper);
			//list3 得到类似文书档案的id ，作为parent_id再去查询类似一文一件 的子节点
			List<Long> list3 = list2.stream().map(ArchiveTree::getId).collect(Collectors.toList());
			wrapper.clear();
			wrapper.eq(ArchiveTree::getFondsCode, fondsCode).in(ArchiveTree::getParentId, list3);
			//list4 点击层级显示后所有生成的子节点
			List<ArchiveTree> list4 = this.list(wrapper);
			list4.forEach(e2 -> {
				ArchiveTreeAuth archiveTreeAuth = ArchiveTreeAuth.builder().relationId(e).relationClass(0).menuId(0L).fondsCode(FondsConstant.GLOBAL_FONDS_CODE).archiveTreeId(e2.getId()).build();
				remoteSysAuthService.insertOrDelete(archiveTreeAuth, insertOrDelete);
			});
		});
	}


	@Override
	public Map<String, String> getArchiveTreeDataValues(Long id) {
	    Map<String, String> values = new HashMap<>();
        ArchiveTree entity = getById(id);
        ArchiveTreeNodeEnum nodeType = ArchiveTreeNodeEnum.getEnum(entity.getNodeType());
        values.put(FieldConstants.ARCHIVE_TYPE_CODE, entity.getArchiveTypeCode());
        switch (nodeType) {
        case FILING_SCOPE:
            if (entity.getFilingScopeId() != null) {
                FilingScope filingScope = filingScopeService.getById(entity.getFilingScopeId());
                values.put(FieldConstants.PATH, filingScope.getPath());
                values.put(FieldConstants.CATALOGUE_NAME, filingScope.getClassName());
                values.put(FieldConstants.SERIES_CODE, filingScope.getClassNo());
            }
            break;
        case DEPT:
        case DYNAMIC:
            // 需要递归上级节点
            values.putAll(getArchiveTreeDataValues(entity.getParentId()));
            if (StrUtil.isEmpty(entity.getMetadataEnglish())) {
                if (StrUtil.equals(entity.getMetadataEnglish(), FieldConstants.DEPT_PATH)) {
                    values.put(FieldConstants.DEPT_PATH, entity.getTreeValue());
                } else {
                    if (StrUtil.isEmpty(entity.getTreeValue())) {
                        values.put(entity.getMetadataEnglish(), null);
                    } else {
                        values.put(entity.getMetadataEnglish(), entity.getTreeValue());
                    }
                }
            }
            break;
        case TREE_ROOT:
        case ARCHIVE_TYPE:
        case LAYER:
        case CLAZZ:
        default:
            break;
        }
        return values;
    }
}
