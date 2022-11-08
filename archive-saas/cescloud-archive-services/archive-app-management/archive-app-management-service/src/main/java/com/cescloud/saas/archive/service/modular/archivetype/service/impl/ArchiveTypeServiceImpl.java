
package com.cescloud.saas.archive.service.modular.archivetype.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archivedict.entity.Dict;
import com.cescloud.saas.archive.api.modular.archivetree.dto.FondsArchiveTypeSyncTreeNode;
import com.cescloud.saas.archive.api.modular.archivetree.entity.ArchiveTree;
import com.cescloud.saas.archive.api.modular.archivetype.dto.*;
import com.cescloud.saas.archive.api.modular.archivetype.entity.*;
import com.cescloud.saas.archive.api.modular.common.constants.SysConstant;
import com.cescloud.saas.archive.api.modular.filingscope.entity.FilingScope;
import com.cescloud.saas.archive.api.modular.fonds.dto.FondsConstant;
import com.cescloud.saas.archive.api.modular.fonds.entity.Fonds;
import com.cescloud.saas.archive.api.modular.fourCheck.feign.RemoteArchiveFourCheckService;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataTag;
import com.cescloud.saas.archive.api.modular.role.feign.RemoteRoleService;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantTemplate;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantArchiveButtonService;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantTemplateService;
import com.cescloud.saas.archive.common.constants.*;
import com.cescloud.saas.archive.common.util.ArchiveTableUtil;
import com.cescloud.saas.archive.common.util.ArchiveUtil;
import com.cescloud.saas.archive.common.util.InitializeUtil;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.*;
import com.cescloud.saas.archive.service.modular.archivedict.service.DictService;
import com.cescloud.saas.archive.service.modular.archivetree.service.ArchiveTreeService;
import com.cescloud.saas.archive.service.modular.archivetype.mapper.ArchiveTypeMapper;
import com.cescloud.saas.archive.service.modular.archivetype.service.*;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import com.cescloud.saas.archive.service.modular.common.security.service.CesCloudUser;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.filingscope.service.FilingScopeService;
import com.cescloud.saas.archive.service.modular.fonds.service.FondsService;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataBaseService;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataService;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataTagService;
import com.cescloud.saas.archive.service.modular.relationrule.service.ArchiveRetentionRelationService;
import com.cescloud.saas.archive.service.modular.report.service.ReportService;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 档案门类
 *
 * @author liudong1
 * @date 2019-03-18 09:14:11
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ArchiveTypeServiceImpl extends ServiceImpl<ArchiveTypeMapper, ArchiveType> implements ArchiveTypeService {

	@Autowired
	private ArchiveTableService tableService;
	@Autowired
	private MetadataBaseService metadataBaseService;
	@Autowired
	private MetadataService metadataService;
	@Autowired
	@Lazy
	private MetadataTagService metadataTagService;
	@Autowired
	private DictService dictService;
	@Autowired
	private ArchiveTableService archiveTableService;
	@Autowired
	private FilingScopeService filingScopeService;
	@Autowired
	private ArchiveTreeService archiveTreeService;
	private final RemoteTenantTemplateService remoteTenantTemplateService;

	private final RemoteArchiveFourCheckService remoteArchiveFourCheckService;

	private final RemoteTenantArchiveButtonService remoteTenantArchiveButtonService;
	@Autowired
	private TemplateTableService templateTableService;
	@Autowired
	private LayerService layerService;
	@Autowired
	private TemplateTypeService templateTypeService;
	@Autowired
	private ArchiveEditService archiveEditService;
	@Autowired
	private ArchiveEditFormService archiveEditFormService;
	@Autowired
	private MetadataAutovalueService metadataAutovalueService;
	@Autowired
	private InnerRelationService innerRelationService;
	@Autowired
	private ArchiveListService archiveListService;
	@Autowired
	private ArchiveSearchService archiveSearchService;
	@Autowired
	private ArchiveSortService archiveSortService;
	@Autowired
	private DispAppraisalRuleService dispAppraisalRuleService;
	@Autowired
	private LinkLayerService linkLayerService;
	@Autowired
	private MetadataBoxConfigService metadataBoxConfigService;
	@Autowired
	private WatermarkService watermarkService;
	@Autowired
	private ReportService reportService;
	@Autowired
	private ArchiveConfigManageService archiveConfigManageService;
	@Autowired
	private ArchiveRetentionRelationService archiveRetentionRelationService;
	@Autowired
	private ArchiveUtil archiveUtil;


	private final RemoteRoleService remoteRoleService;//考虑是否是租户管理员

	private final FondsService fondsService;//全宗查询验证

	@Override
	public ArchiveType getArchiveTypeById(Long id) {
		return this.getById(id);
	}

	/**
	 * 根据档案门类节点获取其下面的子节点
	 * 如果是分类节点，则下面是档案门类节点
	 * 如果是档案门类节点，则下面是档案表数据
	 *
	 * @param parentId
	 * @param nodeType
	 * @return
	 */
	@Override
	public List<ArchiveTypeTreeNode> getTypeTreeNodes(Long parentId, NodeTypeEnum nodeType) throws ArchiveBusinessException {
		if (ObjectUtil.isNull(parentId)) {
			log.warn("根节点参数为空，初始化为0");
			parentId = ArchiveConstants.TREE_ROOT_NODE_VALUE;
		}
		if (!parentId.equals(ArchiveConstants.TREE_ROOT_NODE_VALUE) && ObjectUtil.isNull(nodeType)) {
			log.error("节点类型参数错误！");
			throw new ArchiveBusinessException("节点类型参数错误！");
		}
		List<ArchiveTypeTreeNode> treeList = null;
		//如果是分类节点，则获取 archiveType 表数据
		if (ArchiveConstants.TREE_ROOT_NODE_VALUE.equals(parentId)
				|| nodeType == NodeTypeEnum.CLAZZ) {
			List<ArchiveType> typeList = getTypeListByParentId(parentId);
			treeList = toTreeList(typeList);
		} else if (nodeType == NodeTypeEnum.DATA) { // 如果是数据节点，则获取 archiveTable 表数据
			treeList = archiveTableService.getTypeTreeListByType(this.getById(parentId));
		}
		return treeList;
	}

	/**
	 * 判断 false 抛出业务异常
	 *
	 * @param expression
	 * @param message
	 */
	public void isTrue(boolean expression, String message) throws ArchiveBusinessException {
		if (!expression) {
			throw new ArchiveBusinessException(message);
		}
	}

	/**
	 * 根据档案门类节点获取其下面的子节点
	 * 如果是分类节点，则下面是档案门类节点
	 * 如果是档案门类节点，则下面是档案表数据
	 *
	 * @param parentId
	 * @param nodeType
	 * @return
	 */
	@Override
	public List<ArchiveTypeTreeNode> getTypeTreeNodes(Long parentId, NodeTypeEnum nodeType, List<String> fondsCodes) throws ArchiveBusinessException {
		if (ObjectUtil.isNull(parentId)) {
			log.warn("根节点参数为空，初始化为0");
			parentId = ArchiveConstants.TREE_ROOT_NODE_VALUE;
		}
		if (!parentId.equals(ArchiveConstants.TREE_ROOT_NODE_VALUE) && ObjectUtil.isNull(nodeType)) {
			log.error("节点类型参数错误！");
			throw new ArchiveBusinessException("节点类型参数错误！");
		}
		List<ArchiveTypeTreeNode> treeList = null;
		Long fondsParentId = parentId;
		if (parentId.longValue() == ArchiveConstants.TREE_FONDS_TYPE_NODE || parentId.longValue() == ArchiveConstants.TREE_FONDS_NODE) {
			parentId = ArchiveConstants.TREE_ROOT_NODE_VALUE;
		}
		//如果是分类节点，则获取 archiveType 表数据
		if (nodeType == NodeTypeEnum.CLAZZ) {
			if (fondsParentId.longValue() == ArchiveConstants.TREE_FONDS_TYPE_NODE) {
				if (archiveUtil.isAuthorityFilter()){
					fondsCodes.addAll(archiveUtil.getAuthFondsCode());
					fondsCodes.add(FondsConstant.GLOBAL_FONDS_CODE);
				}
				List<ArchiveType> typeList = getFondsGroupByParentId(parentId, fondsCodes);//过滤全宗分组范围
				treeList = toFondsTreeList(typeList, fondsParentId);
			} else if (fondsParentId.longValue() == ArchiveConstants.TREE_FONDS_NODE) {
				LambdaQueryWrapper<ArchiveType> queryAllItemWrapper = Wrappers.<ArchiveType>query().lambda();
				if (archiveUtil.isAuthorityFilter()){
					Set<String> authFondsCode = archiveUtil.getAuthFondsCode();
					authFondsCode.add(FondsConstant.GLOBAL_FONDS_CODE);
					queryAllItemWrapper.in(ArchiveType::getFondsCode, authFondsCode);
				}
				queryAllItemWrapper.eq(ArchiveType::getParentId, ArchiveConstants.TREE_ROOT_NODE_VALUE);
				queryAllItemWrapper.orderByAsc(ArchiveType::getSortNo);
				//一次Db查询出所有的列表信息
				List<ArchiveType> allFilingScopeForCurrentUserList = this.list(queryAllItemWrapper);
				treeList = toFondsTreeList(allFilingScopeForCurrentUserList, fondsParentId);
			} else {
				List<ArchiveType> typeList = getTypeListByParentId(parentId, fondsCodes);//过滤全宗范围
				treeList = toFondsTreeList(typeList, fondsParentId);
			}
		} else if (nodeType == NodeTypeEnum.DATA) { // 如果是数据节点，则获取 archiveTable 表数据
			treeList = archiveTableService.getTypeTreeListByType(this.getById(parentId));
		}
		return treeList;
	}

	/**
	 * 根据档案门类节点获取其下面的子节点
	 * 如果是分类节点，则下面是档案门类节点
	 * 如果是档案门类节点，则下面是档案表数据
	 *
	 * @param parentId
	 * @param nodeType
	 * @return
	 */
	@Override
	public List<ArchiveTypeTreeNode> getTypeTreeNodesForFourCheck(Long parentId, NodeTypeEnum nodeType, List<String> fondsCodes) throws ArchiveBusinessException {
		List<ArchiveTypeTreeNode> treeList = getTypeTreeNodes(parentId,nodeType,fondsCodes);
		//如果是分类节点，则获取 archiveType 表数据
		if ((ArchiveConstants.TREE_ROOT_NODE_VALUE.equals(parentId)|| nodeType == NodeTypeEnum.CLAZZ)&&parentId.longValue() != ArchiveConstants.TREE_FONDS_TYPE_NODE) {
			treeList.stream().forEach(archiveType ->{
				archiveType.setIsLeaf(true);//四性检测不需要张开界面
			});
		}
		return treeList;
	}


	/**
	 * 根据范围全宗内的父节点获取子节点列表
	 *
	 * @param parentId
	 * @param fondsCodes 范围全宗 ， 要考虑的全宗  比如：['G','tsds','qz001']
	 * @return
	 */
	private List<ArchiveType> getTypeListByParentId(Long parentId, List<String> fondsCodes) {
		LambdaQueryWrapper<ArchiveType> lambdaQueryWrapper = Wrappers.<ArchiveType>query().lambda();
		if (CollectionUtil.isNotEmpty(fondsCodes)) {
			lambdaQueryWrapper.in(ArchiveType::getFondsCode, fondsCodes);
		}
		lambdaQueryWrapper.eq(ArchiveType::getParentId, parentId).orderByAsc(ArchiveType::getSortNo);
		List<ArchiveType> typeList = this.list(lambdaQueryWrapper);
		return typeList;
	}

	/**
	 * 根据范围全宗内的父节点 分组获取对应的全宗信息分组列表
	 *
	 * @param parentId
	 * @param fondsCodes 范围全宗 ， 要考虑的全宗  比如：['G','tsds','qz001']
	 * @return
	 */
	private List<ArchiveType> getFondsGroupByParentId(Long parentId, List<String> fondsCodes) {
		return this.baseMapper.getFondsGroupByParentId(parentId, fondsCodes);
	}

	/**
	 * 转换成带全宗的树组件所需结构
	 *
	 * @param typeList
	 * @return
	 */
	private List<ArchiveTypeTreeNode> toFondsTreeList(List<ArchiveType> typeList, Long fondsParentId) {
		if (fondsParentId != null && fondsParentId.longValue() == ArchiveConstants.TREE_FONDS_TYPE_NODE) {
			List<ArchiveTypeTreeNode> treeList = new ArrayList<>();
			long h5KeyId = -100;
			for (ArchiveType t:typeList) {
				ArchiveTypeTreeNode treeNode = new ArchiveTypeTreeNode();
				treeNode.setFondsCode(t.getFondsCode());
				treeNode.setFondsName(t.getFondsName());
				treeNode.setCreatedUserName(t.getCreatedUserName());
				treeNode.setCreatedTime(t.getCreatedTime());
				treeNode.setParentId(ArchiveConstants.TREE_FONDS_TYPE_NODE);
				treeNode.setTypeCode(t.getTypeCode());
				treeNode.setTemplateTypeId(t.getTemplateTypeId());
				treeNode.setClassType(t.getClassType());
				treeNode.setTypeName(treeNode.getFondsName());
				treeNode.setId(ArchiveConstants.TREE_ROOT_NODE_VALUE);
				treeNode.setPk(StrUtil.toString(h5KeyId--));
				treeNode.setIsLeaf(false);
				treeNode.setNodeType(NodeTypeEnum.CLAZZ.getValue());
				treeList.add(treeNode);
			}
			return treeList;
		} else if (fondsParentId != null && fondsParentId.longValue() == ArchiveConstants.TREE_FONDS_NODE) {
			return toTreeList(typeList);
		} else {
			return toTreeList(typeList);
		}
	}

	/**
	 * 根据父节点获取子节点列表
	 *
	 * @param parentId
	 * @return
	 */
	private List<ArchiveType> getTypeListByParentId(Long parentId) {
		List<ArchiveType> typeList = this.list(Wrappers.<ArchiveType>query()
				.lambda().eq(ArchiveType::getParentId, parentId).orderByAsc(ArchiveType::getSortNo));
		return typeList;
	}

	/**
	 * 转换成树组件所需结构
	 *
	 * @param typeList
	 * @return
	 */
	private List<ArchiveTypeTreeNode> toTreeList(List<ArchiveType> typeList) {
		List<ArchiveTypeTreeNode> treeList = typeList.stream()
				.map(archiveType -> {
					ArchiveTypeTreeNode treeNode = new ArchiveTypeTreeNode();
					//全宗信息绑定
					treeNode.setFondsCode(archiveType.getFondsCode());
					treeNode.setFondsName(archiveType.getFondsName());
					treeNode.setCreatedUserName(archiveType.getCreatedUserName());
					treeNode.setCreatedTime(archiveType.getCreatedTime());
					treeNode.setId(archiveType.getId());
					treeNode.setPk(treeNode.getId().toString());
					treeNode.setTypeName(archiveType.getTypeName());
					treeNode.setParentId(archiveType.getParentId());
					treeNode.setNodeType(archiveType.getNodeType());
					treeNode.setTypeCode(archiveType.getTypeCode());
					treeNode.setTemplateTypeId(archiveType.getTemplateTypeId());
					treeNode.setClassType(archiveType.getClassType());
//					if (archiveType.getNodeType().equals(NodeTypeEnum.DATA.getValue())) {
//						treeNode.setTemplateTypeName(templateTypeService.getById(archiveType.getTemplateTypeId()).getName());
//					}
					// 前台需要
					treeNode.setIsLeaf(false);
					return treeNode;
				}).collect(Collectors.toList());
		return treeList;
	}

	private List<ArchiveTypeTreeNode> toRelationTreeList(List<ArchiveType> typeList) {
		List<ArchiveTypeTreeNode> treeList = typeList.stream()
				.map(archiveType -> {
					ArchiveTypeTreeNode treeNode = new ArchiveTypeTreeNode();
					treeNode.setId(archiveType.getId());
					treeNode.setTypeName(archiveType.getTypeName());
					treeNode.setParentId(archiveType.getParentId());
					treeNode.setNodeType(archiveType.getNodeType());
					treeNode.setIsLeaf(isLeaf(archiveType, false));
					return treeNode;
				}).collect(Collectors.toList());
		return treeList;
	}

	/**
	 * 判断是否是叶子节点
	 *
	 * @param archiveType
	 * @return
	 */
	private Boolean isLeaf(ArchiveType archiveType, Boolean isShowTable) {
		Boolean isLeaf = true;
		//如果是档案门类节点，肯定不是叶子节点
		if (archiveType.getNodeType().equals(NodeTypeEnum.DATA.getValue())) {
			if (isShowTable) {
				isLeaf = false;
			} else {
				isLeaf = true;
			}
		} else if (archiveType.getNodeType().equals(NodeTypeEnum.CLAZZ.getValue())) {
			isLeaf = this.getCountTypes(archiveType.getId()) == 0;
		}
		return isLeaf;
	}

	/**
	 * 修改档案门类
	 *
	 * @param type
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public ArchiveType updateArchiveType(ArchiveType type) throws ArchiveBusinessException {
		this.isTrue(type.getId() != null && type.getId() > 0, "无效门类，无法更新！");
		final String nodeType = type.getNodeType();
		if (NodeTypeEnum.CLAZZ.getValue().equals(nodeType) || NodeTypeEnum.DATA.getValue().equals(nodeType)) {
			this.isTrue(StrUtil.isNotBlank(type.getFondsCode()), "必须传递有效全宗号编号");
			ArchiveType archiveType = this.getById(type.getId());
			//是否更改了全宗信息
			if (!StrUtil.equalsAnyIgnoreCase(archiveType.getFondsCode(), type.getFondsCode())) {
				if (StrUtil.equalsAnyIgnoreCase(ArchiveConstants.FONDS_GLOBAL, type.getFondsCode()) || StrUtil.equalsAnyIgnoreCase(ArchiveConstants.FONDS_GLOBAL, archiveType.getFondsCode())) {
					R<Boolean> tenantSuperAdmin = remoteRoleService.isTenantSuperAdmin();
					this.isTrue(tenantSuperAdmin != null && BooleanUtil.isTrue(tenantSuperAdmin.getData()), "租户管理员才有权限修改全局门类");
					type.setFondsName(ArchiveConstants.FONDS_GLOBAL_NAME);
				}
				//验证目前全宗是否在权限范围内
				if (!StrUtil.equalsAnyIgnoreCase(ArchiveConstants.FONDS_GLOBAL, type.getFondsCode())) {
					List<Fonds> currentFondsList = fondsService.getFondsList();
					log.info("目前的全宗信息列表：{}", currentFondsList);
					Fonds currentFonds = currentFondsList.stream().filter(fonds -> StrUtil.equalsAnyIgnoreCase(fonds.getFondsCode(), type.getFondsCode())).findFirst().orElse(null);
					this.isTrue(currentFonds != null, "目前的全宗不在您的权限范围内");
					type.setFondsCode(currentFonds.getFondsCode());
					type.setFondsName(currentFonds.getFondsName());
				}

				List<String> archiveTypeCodeList = Lists.newArrayList();
				archiveTypeCodeList.add(archiveType.getTypeCode());
				//获取可能的节点门类信息
				this.getAllMainInfoForChildren(archiveType.getId(), archiveTypeCodeList);
				//范围树引用情况
				List<FilingScope> filingScopeList = this.filingScopeService.getFilingScopeByArchiveType(archiveTypeCodeList);
				this.isTrue(CollectionUtil.isEmpty(filingScopeList), "该节点门类编码被范围树引用，不允许修改全宗！");
				//归档树引用情况
				List<ArchiveTree> archiveTreeList = this.archiveTreeService.getArchiveTreeByFilingScopeOrArchiveType(archiveTypeCodeList, null);
				this.isTrue(CollectionUtil.isEmpty(archiveTreeList), "该节点门类编码被档案树引用，不允许修改全宗！");
			}
			checkUnique(type);
			this.updateById(type);
		} else {
			archiveTableService.updateById(ArchiveTable.builder().id(type.getId()).storageName(type.getTypeName()).build());
		}
		return type;

	}


	/**
	 * 创建档案门类
	 * 如果是分类节点，只在archiveType添加数据
	 * 如果是档案门类节点，在archiveType添加数据，在将metadataBase表中数据复制到metadata表
	 * 然后根据metadata表中元数据创建物理档案门类表
	 *
	 * @param type
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public ArchiveType saveArchiveType(ArchiveType type) throws ArchiveBusinessException {
		long start = System.currentTimeMillis();
		checkUnique(type);
		ArchiveType archiveType = new ArchiveType();
		BeanUtils.copyProperties(type, archiveType);
		if (archiveType.getNodeType().equals(NodeTypeEnum.DATA.getValue())) {
			TemplateType templateType = templateTypeService.getById(archiveType.getTemplateTypeId());
			archiveType.setFilingType(templateType.getFilingType());
		}
		//考虑添加全宗
		CesCloudUser cesCloudUser = SecurityUtils.getUser();
		if (cesCloudUser != null) {
			//设定当前创建的用户名字
			archiveType.setCreatedUserName(cesCloudUser.getChineseName());
		}

		this.isTrue(StrUtil.isNotBlank(archiveType.getFondsCode()), "必须传递有效地全宗编号");
		if (StrUtil.equalsAnyIgnoreCase(ArchiveConstants.FONDS_GLOBAL, archiveType.getFondsCode())) {
			R<Boolean> tenantSuperAdmin = remoteRoleService.isTenantSuperAdmin();
			this.isTrue(tenantSuperAdmin != null && BooleanUtil.isTrue(tenantSuperAdmin.getData()), "租户管理员才能够创建全局门类");
			archiveType.setFondsName(ArchiveConstants.FONDS_GLOBAL_NAME);
		} else {
			List<Fonds> currentFondsList = fondsService.getFondsList();
			log.info("目前的全宗信息列表：{}", currentFondsList);
			Fonds currentFonds = currentFondsList.stream().filter(fonds -> StrUtil.equalsAnyIgnoreCase(fonds.getFondsCode(), archiveType.getFondsCode())).findFirst().orElse(null);
			this.isTrue(currentFonds != null, "目前的全宗不在您的权限范围内");
			archiveType.setFondsCode(currentFonds.getFondsCode());
			archiveType.setFondsName(currentFonds.getFondsName());
		}
		if (ObjectUtil.isNotNull(archiveType) && (archiveType.getParentId() == null || archiveType.getParentId().longValue() == ArchiveConstants.TREE_FONDS_TYPE_NODE || archiveType.getParentId().longValue() == ArchiveConstants.TREE_FONDS_NODE)) {
			archiveType.setParentId(ArchiveConstants.TREE_ROOT_NODE_VALUE);
		}

		//获取档案门类最大排序号
		Integer maxSortNo = baseMapper.getMaxSortNo();
		if (ObjectUtil.isNull(maxSortNo)) {
			maxSortNo = 0;
		}
		archiveType.setSortNo(maxSortNo + 1);
		this.save(archiveType);
		//为了前端显示
		archiveType.setIsLeaf(false);
		//如果是档案门类节点，还要创建物理表等其他操作
		if (archiveType.getNodeType().equals(NodeTypeEnum.DATA.getValue())) {
			List<TemplateTable> templateTableList = templateTableService.getByTemplateTypeId(archiveType.getTemplateTypeId());
			archiveTableService.createArchiveTable(archiveType, templateTableList);
			//动态数据源重新初始化(目前从前台创建的档案类型不分表，不走sharding数据源)
			//messageSendService.reloadDataSource();
		}
		long end = System.currentTimeMillis();
		if (log.isDebugEnabled()) {
			log.debug("创建[{}]档案门类耗时：{}", archiveType.getTypeName(), (end - start));
		}
		return archiveType;
	}

	@Override
	public void bingdingTagAndDict(ArchiveTable archiveTable) {
		List<Metadata> metadataList = null;
		if (archiveTable.getTenantId() != null) {
			metadataList = metadataService.list(Wrappers.<Metadata>query().lambda()
					.eq(Metadata::getTableId, archiveTable.getId()).eq(Metadata::getTenantId, archiveTable.getTenantId()));
		} else {
			metadataList = metadataService.list(Wrappers.<Metadata>query().lambda()
					.eq(Metadata::getTableId, archiveTable.getId()));
		}
		//得到 english与ID关联关系
		List<MetadataTag> metadataTagList = metadataTagService.listByTenantId(archiveTable.getTenantId());

		List<Metadata> updateMetadataList = metadataList.stream()
				.filter(metadata -> metadataTagList.stream().anyMatch(metadataTag -> metadataTag.getTagEnglish().equals(metadata.getMetadataEnglish())))
				.map(metadata -> {
					metadata.setTagEnglish(metadata.getMetadataEnglish());
					return metadata;
				}).collect(Collectors.toList());

		if (CollectionUtil.isNotEmpty(updateMetadataList)) {
			metadataService.updateBatchById(updateMetadataList);
		}
		//绑定数据字典
		List<Dict> dictList = dictService.list();

		List<Metadata> updateMetadataDictList = metadataList.stream()
				.map(metadata -> {
					dictList.stream().forEach(dict -> {
						if (dict.getDictCode().equals(metadata.getMetadataEnglish())) {
							metadata.setDictCode(dict.getDictCode());
						}
					});
					return metadata;
				})
				.filter(metadata -> metadata.getDictCode() != null)
				.collect(Collectors.toList());

		if (CollectionUtil.isNotEmpty(updateMetadataDictList)) {
			metadataService.updateBatchById(updateMetadataDictList);
		}
	}


	/**
	 * 删除档案类型
	 *
	 * @param id
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R deleteArchiveType(Long id) throws ArchiveBusinessException {
		this.isTrue(id != null && id.longValue() > 0, "无效门类，无法删除！");
		ArchiveType archiveType = this.getById(id);
		//是否更改了全宗信息
		if (StrUtil.equalsAnyIgnoreCase(ArchiveConstants.FONDS_GLOBAL, archiveType.getFondsCode())) {
			R<Boolean> tenantSuperAdmin = remoteRoleService.isTenantSuperAdmin();
			this.isTrue(tenantSuperAdmin != null && BooleanUtil.isTrue(tenantSuperAdmin.getData()), "租户管理员才有权限删除全局门类");
		} else {
			List<Fonds> currentFondsList = fondsService.getFondsList();
			Fonds currentFonds = currentFondsList.stream().filter(fonds -> StrUtil.equalsAnyIgnoreCase(fonds.getFondsCode(), archiveType.getFondsCode())).findFirst().orElse(null);
			this.isTrue(currentFonds != null, "目前的全宗不在您的权限范围内");
		}
		List<String> archiveTypeCodeList = Lists.newArrayList();
		archiveTypeCodeList.add(archiveType.getTypeCode());
		//获取可能的节点门类信息
		this.getAllMainInfoForChildren(archiveType.getId(), archiveTypeCodeList);
		//范围树引用情况
		List<FilingScope> filingScopeList = this.filingScopeService.getFilingScopeByArchiveType(archiveTypeCodeList);
		this.isTrue(CollectionUtil.isEmpty(filingScopeList), "该节点门类编码被范围树引用，不允许删除！");
		//归档树引用情况
		List<ArchiveTree> archiveTreeList = this.archiveTreeService.getArchiveTreeByFilingScopeOrArchiveType(archiveTypeCodeList, null);
		this.isTrue(CollectionUtil.isEmpty(archiveTreeList), "该节点门类编码被档案树引用，不允许删除！");

		//判断节点类型，分类节点下面包括档案类型节点不能删除
		checkNodeType(archiveType);
		//判断该节点及其子节点是否存在数据或绑定档案树
		log.debug("判断该节点及其子节点是否存在数据，是否绑定档案树，是否绑定归档范围树");
		conDeleteArchiveType(archiveType);
		//删除自己（单独提出来是为了以后如果速度慢，就异步线程删除子节点）
		this.removeById(id);
		//递归删除子节点
		log.debug("开始删除门类编码为[{}]的档案门类", archiveType.getTypeCode());
		deleteArchiveTypeByParent(archiveType);
		// 删除档案没类下所有的按钮
		remoteTenantArchiveButtonService.deleteButtonByTypeCode(archiveType.getTypeCode());
		//删除检测方案
		remoteArchiveFourCheckService.deleteByTypeCode(archiveType.getTypeCode());
		return new R<>("删除成功");
	}

	private void checkNodeType(ArchiveType archiveType) throws ArchiveBusinessException {
		if (NodeTypeEnum.CLAZZ.getValue().equals(archiveType.getNodeType())) {
			List<ArchiveType> typeList = getTypeListByParentId(archiveType.getId());
			if (CollectionUtil.isNotEmpty(typeList)) {
				throw new ArchiveBusinessException("该分类节点下面包括档案门类节点，请先删除档案门类节点！");
			}
		}
	}

	/**
	 * 判断能否删除档案类型节点
	 * 1、档案表已存在数据
	 * 2、档案树关联档案类型
	 *
	 * @param archiveType
	 * @return
	 */
	private void conDeleteArchiveType(ArchiveType archiveType) throws ArchiveBusinessException {
		//如果是分类节点，则递归他的子节点
		if (archiveType.getNodeType().equals(NodeTypeEnum.CLAZZ.getValue())) {
			List<ArchiveType> childrenList = this.list(Wrappers.<ArchiveType>query().lambda()
					.eq(ArchiveType::getParentId, archiveType.getId()));
			for (ArchiveType type : childrenList) {
				conDeleteArchiveType(type);
			}
		} //若果是档案类型节点，则判断 是否有数据 或 关联档案类型
		else if (archiveType.getNodeType().equals(NodeTypeEnum.DATA.getValue())) {
			//判断该节点是否绑定归档范围树
			FilingScope filingScope = filingScopeService.getOne(Wrappers.<FilingScope>query().lambda().eq(FilingScope::getTypeCode, archiveType.getTypeCode()));
			if (filingScope != null) {
				log.warn("档案类型[{}]已绑定归档范围树，不能删除", archiveType.getTypeCode());
				throw new ArchiveRuntimeException("档案类型[" + archiveType.getTypeName() + "]已经绑定归档范围树，不能删除");
			}

			R r = tableService.canDeleteArchiveTable(archiveType);
			if (r != null) {
				log.warn("档案类型[{}]中存在数据或已经绑定档案树，不能删除", archiveType.getTypeCode());
				throw new ArchiveBusinessException(r.getMsg(), r.getInfo());
			}
		}
	}

	/**
	 * 递归删除子节点
	 *
	 * @param archiveType
	 */
	private void deleteArchiveTypeByParent(ArchiveType archiveType) throws ArchiveBusinessException {
		if (archiveType.getNodeType().equals(NodeTypeEnum.CLAZZ.getValue())) {
			//所有孩子节点
			List<ArchiveType> childrenList = this.list(Wrappers.<ArchiveType>query().lambda()
					.eq(ArchiveType::getParentId, archiveType.getId()));
			if (CollectionUtil.isNotEmpty(childrenList)) {
				for (ArchiveType type : childrenList) {
					//递归
					deleteArchiveTypeByParent(type);
					//删除子节点
					this.removeById(type.getId());
				}
			}
		} else if (archiveType.getNodeType().equals(NodeTypeEnum.DATA.getValue())) {
			tableService.removeStorageLocate(archiveType.getTypeCode());
		}
	}

	/**
	 * 跟父节点，获取子节点数量
	 *
	 * @param parentId
	 * @return
	 */
	private Integer getCountTypes(Long parentId) {
		return this.count(Wrappers.<ArchiveType>query()
				.lambda().eq(ArchiveType::getParentId, parentId));
	}

	/**
	 * 校验唯一
	 *
	 * @param archiveType
	 * @throws ArchiveBusinessException
	 */
	private void checkUnique(ArchiveType archiveType) throws ArchiveBusinessException {
		checkUniqueName(archiveType);
		checkUniqueCode(archiveType);
	}

	private void checkUniqueName(ArchiveType archiveType) throws ArchiveBusinessException {
		Map<String, Object> params = CollectionUtil.newHashMap(3);
		if (!FondsConstant.GLOBAL_FONDS_CODE.equals(archiveType.getFondsCode())) {
			params.put(FieldConstants.FONDS_CODE, archiveType.getFondsCode());
		}
		params.put("type_name", archiveType.getTypeName());
		R r = metadataBaseService.checkUnique("apma_archive_type", params, "节点名称");
		if (r.getCode() == CommonConstants.FAIL) {
			if (ObjectUtil.isNull(archiveType.getId())) {
				throw new ArchiveBusinessException(r.getMsg());
			} else if (!archiveType.getId().toString().equals(r.getData().toString())) {
				throw new ArchiveBusinessException(r.getMsg());
			}
		}
	}

	private void checkUniqueCode(ArchiveType archiveType) throws ArchiveBusinessException {
		Map<String, Object> params = CollectionUtil.newHashMap(3);
		params.put("type_code", archiveType.getTypeCode());
		R r = metadataBaseService.checkUnique("apma_archive_type", params, "节点编码");
		if (r.getCode() == CommonConstants.FAIL) {
			if (ObjectUtil.isNull(archiveType.getId())) {
				throw new ArchiveBusinessException(r.getMsg());
			} else if (!archiveType.getId().toString().equals(r.getData().toString())) {
				throw new ArchiveBusinessException(r.getMsg());
			}
		}
	}

	/**
	 * 根据档案门类编码查询档案门类
	 *
	 * @param typeCode
	 * @throws ArchiveBusinessException
	 */
	@Override
	public ArchiveType getByTypeCode(String typeCode) throws ArchiveBusinessException {
		List<ArchiveType> archiveTypeList = this.list(Wrappers.<ArchiveType>query().lambda().eq(ArchiveType::getTypeCode, typeCode));
		if (CollectionUtils.isEmpty(archiveTypeList)) {
			log.debug("找不到档案门类编码为{}的档案门类" + typeCode);
			throw new ArchiveBusinessException("找不到档案门类编码为" + typeCode + "的档案门类");
		}
		return archiveTypeList.get(0);
	}

	/**
	 * 根据档案门类编码查询档案门类
	 *
	 * @param typeCode 类型
	 * @param tenantId 租户ID
	 * @throws ArchiveBusinessException
	 */
	@Override
	public ArchiveType getByTypeCode(String typeCode, Long tenantId) throws ArchiveBusinessException {
		List<ArchiveType> archiveTypeList = this.list(Wrappers.<ArchiveType>query().lambda().eq(ArchiveType::getTypeCode, typeCode).eq(ArchiveType::getTenantId, tenantId));
		if (CollectionUtils.isEmpty(archiveTypeList)) {
			log.debug("找不到档案门类编码为{}的档案门类" + typeCode);
			throw new ArchiveBusinessException("找不到档案门类编码为" + typeCode + "的档案门类");
		}
		return archiveTypeList.get(0);
	}

	@Override
	public List<ArchiveTypeDTO> getArchiveTypeRelationTree(List<String> fondsCodes) {
		LambdaQueryWrapper<ArchiveType> lambdaQueryWrapper = Wrappers.<ArchiveType>query().lambda();
		if (CollectionUtil.isNotEmpty(fondsCodes)) {
			lambdaQueryWrapper.in(ArchiveType::getFondsCode, fondsCodes);
		}
		lambdaQueryWrapper.eq(ArchiveType::getNodeType, NodeTypeEnum.DATA.getValue()).orderByAsc(ArchiveType::getId);
		List<ArchiveType> archiveTypes = this.list(lambdaQueryWrapper);
		if (CollectionUtil.isEmpty(archiveTypes)) {
			return CollectionUtil.newArrayList();
		}
		List<TemplateTable> templateTables = templateTableService.list(Wrappers.<TemplateTable>lambdaQuery().isNull(TemplateTable::getParentId));
		Map<Long, Long> collect = templateTables.stream().collect(
				Collectors.toMap(
						TemplateTable::getTemplateTypeId, TemplateTable::getId,(value1, value2 )->{
			return value2;
		})
		);
		List<ArchiveTypeDTO> archiveTypeDTOS = CollectionUtil.newArrayList();
		archiveTypes.stream().forEach(archiveType -> {
			ArchiveTypeDTO archiveTypeDTO = new ArchiveTypeDTO();
			BeanUtils.copyProperties(archiveType, archiveTypeDTO);
			archiveTypeDTO.setTemplateTableId(collect.get(archiveType.getTemplateTypeId()));
			archiveTypeDTOS.add(archiveTypeDTO);
		});
		return archiveTypeDTOS;
	}

	@Override
	public List<ArchiveTypeChildTreeNode> getArchiveTypeRelationFondsTree(List<String> fondsCodes) {
		List<ArchiveTypeChildTreeNode> result = Lists.newArrayList();
		LambdaQueryWrapper<ArchiveType> lambdaQueryWrapper = Wrappers.<ArchiveType>query().lambda();
		if (CollectionUtil.isNotEmpty(fondsCodes)) {
			lambdaQueryWrapper.in(ArchiveType::getFondsCode, fondsCodes);
		}
		lambdaQueryWrapper.eq(ArchiveType::getNodeType, NodeTypeEnum.DATA.getValue()).orderByAsc(ArchiveType::getId);
		List<ArchiveType> archiveTypes = this.list(lambdaQueryWrapper);
		if (CollectionUtil.isEmpty(archiveTypes)) {
			return result;
		}
		List<String> groupFonds = archiveTypes.stream().map(ArchiveType::getFondsCode).distinct().collect(Collectors.toList());
		List<String> sortFondsList = Lists.newArrayList();
		if (groupFonds.contains(ArchiveConstants.FONDS_GLOBAL)) {
			sortFondsList.add(ArchiveConstants.FONDS_GLOBAL);
		}
		sortFondsList.addAll(groupFonds.stream().filter(t -> !StrUtil.equalsAnyIgnoreCase(t, ArchiveConstants.FONDS_GLOBAL)).collect(Collectors.toList()));
		sortFondsList.forEach(itemGroup -> {
			List<ArchiveType> archiveTypesList = archiveTypes.stream().filter(t -> StrUtil.equalsAnyIgnoreCase(itemGroup, t.getFondsCode())).collect(Collectors.toList());
			if (!CollectionUtil.isEmpty(archiveTypesList)) {
				ArchiveType archiveTypeFirst = archiveTypesList.get(0);
				ArchiveTypeChildTreeNode archiveTypeChildTreeNode = new ArchiveTypeChildTreeNode();
				archiveTypeChildTreeNode.setLabel(archiveTypeFirst.getFondsName());
				archiveTypeChildTreeNode.setValue(archiveTypeFirst.getFondsCode());
				List<ArchiveTypeChildTreeNode> child = Lists.newArrayList();
				archiveTypesList.forEach(item -> {
					ArchiveTypeChildTreeNode archiveTypeItemTreeNode = new ArchiveTypeChildTreeNode();
					archiveTypeItemTreeNode.setLabel(item.getTypeName());
					archiveTypeItemTreeNode.setValue(item.getTypeCode());
					child.add(archiveTypeItemTreeNode);
				});
				archiveTypeChildTreeNode.setChildren(child);
				result.add(archiveTypeChildTreeNode);
			}
		});
		return result;
	}

	/**
	 * 初始化门类管理
	 *
	 * @param templateId 模板id
	 * @param tenantId   租户ID
	 * @return R
	 * @throws ArchiveBusinessException
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R initializeHandle(Long templateId, Long tenantId) throws ArchiveBusinessException {
		ExcelReader excel = null;
		try {
			InputStream inputStream = getDefaultTemplateStream(templateId);
			if (ObjectUtil.isNull(inputStream)) {
				return new R<>().fail("", "获取初始化文件异常");
			}
			excel = new ExcelReader(inputStream, TemplateFieldConstants.SHEET_NAMES.ARCHIVE_TYPE_NAME, true);
			List<List<Object>> read = excel.read();
			//对表单表头校验
			Map<Integer, String> map = InitializeUtil.checkHeader(TemplateFieldConstants.ARCHIVE_TYPE_LIST, read.get(0));
			if (CollectionUtils.isEmpty(map)) {
				return new R<>().fail("", "模板表列数据不匹配！！！");
			}
			final List<ArchiveType> archiveTypes = convertExcelData(map, read, tenantId);
			//保存数据
			boolean result = insertExcelData(archiveTypes, tenantId);
			if (result) {
				return new R("", "初始化门类成功");
			} else {
				log.info("初始门类失败！！！");
				return new R().fail(null, "初始化门类失败！！");
			}
		} finally {
			IoUtil.close(excel);
		}
	}

	/**
	 * 将excel表中每行的数据转化为archiveType对象
	 *
	 * @param map      处理好的数据集
	 * @param read     excel 数据集
	 * @param tenantId 租户ID
	 * @return 成功状态
	 */
	private List<ArchiveType> convertExcelData(Map<Integer, String> map, List<List<Object>> read, Long tenantId) {
		//获取模板信息
		final List<TemplateType> templateTypes = templateTypeService.list(Wrappers.<TemplateType>lambdaQuery().eq(TemplateType::getTenantId, tenantId));
		//处理模板信息
		final Map<String, Long> templateTypeMap = templateTypes.stream().collect(Collectors.toMap(TemplateType::getName, TemplateType::getId));
		final List<ArchiveType> archiveTypeList = CollectionUtil.<ArchiveType>newArrayList();
		//节点类型
		final Map<String, String> nodeTypeMap = Stream.of(NodeTypeEnum.values()).collect(Collectors.toMap(NodeTypeEnum::getName, NodeTypeEnum::getValue));
		//档案分类
		final Map<String, String> classTypeMap = Stream.of(ClassTypeEnum.values()).collect(Collectors.toMap(ClassTypeEnum::getName, ClassTypeEnum::getValue));
		// 行循环
		for (int i = 1, length = read.size(); i < length; i++) {
			//数据处理
			Map<String, String> data = InitializeUtil.dataTreating(map, TemplateFieldConstants.ARCHIVE_TYPE_LIST, read.get(i));
			if (CollectionUtils.isEmpty(data)) {
				continue;
			}
			String templateType = data.get(TemplateFieldConstants.ARCHIVE_TYPE.THEIR_TEMPLATE.getName());
			ArchiveType archiveType = ArchiveType.builder().tenantId(tenantId).parentId(0L).templateTypeId(templateTypeMap.get(templateType)).build();
			String nodeType = nodeTypeMap.get(data.get(TemplateFieldConstants.ARCHIVE_TYPE.NODE_TYPE.getName()));
			Optional.ofNullable(nodeType).ifPresent(archiveType::setNodeType);
			//绑定节点名称
			if (StrUtil.isNotEmpty(data.get(TemplateFieldConstants.ARCHIVE_TYPE.TYPE_NAME.getName()))) {
				archiveType.setTypeName(data.get(TemplateFieldConstants.ARCHIVE_TYPE.TYPE_NAME.getName()));
			}
			//节点编码
			if (StrUtil.isNotEmpty(data.get(TemplateFieldConstants.ARCHIVE_TYPE.TYPE_CODE.getName()))) {
				archiveType.setTypeCode(data.get(TemplateFieldConstants.ARCHIVE_TYPE.TYPE_CODE.getName()));
			}
			//档案分类
			String classType = classTypeMap.get(data.get(TemplateFieldConstants.ARCHIVE_TYPE.CLASS_TYPE.getName()));
			Optional.ofNullable(classType).ifPresent(archiveType::setClassType);
			//整理规则
			String filingType = FilingTypeEnum.getEnumByName(data.get(TemplateFieldConstants.ARCHIVE_TYPE.FILING_TYPE.getName())).getCode();
			Optional.ofNullable(filingType).ifPresent(archiveType::setFilingType);
			//排序值
			String sortNo = data.get(TemplateFieldConstants.ARCHIVE_TYPE.SORT_NO.getName());
			Optional.ofNullable(sortNo).ifPresent(type -> archiveType.setSortNo(Integer.valueOf(sortNo)));
			//全宗号
			String fondsCode = data.get(TemplateFieldConstants.ARCHIVE_TYPE.FONDS_CODE.getName());
			Optional.ofNullable(fondsCode).ifPresent(type -> archiveType.setFondsCode(fondsCode));
			//全宗名称
			String fondsName = data.get(TemplateFieldConstants.ARCHIVE_TYPE.FONDS_NAME.getName());
			Optional.ofNullable(fondsName).ifPresent(type -> archiveType.setFondsName(fondsName));
			//type_code,type_code_hidden,type_code_no赋值
			//String typeCode = HanLPUtil.toPinyinFirstCharString(archiveType.getTypeName());
			//archiveType.setTypeCode(typeCode);
			archiveTypeList.add(archiveType);
		}
		return archiveTypeList;
	}

	private boolean insertExcelData(final List<ArchiveType> archiveTypes, Long tenantId) {
		//1、保存archiveType
		boolean saveBatch = this.saveBatch(archiveTypes);
		//2、保存archiveTable
		final List<ArchiveTable> archiveTables = generateArchiveTables(archiveTypes, tenantId);
		archiveTableService.saveBatch(archiveTables);
		return Boolean.TRUE;
	}

	/**
	 * 生成记录集合
	 *
	 * @param archiveTypes
	 * @return
	 */
	private List<ArchiveTable> generateArchiveTables(final List<ArchiveType> archiveTypes, Long tenantId) {
		if (log.isDebugEnabled()) {
			archiveTypes.forEach(e -> log.debug("门类数据{}", e));
		}
		List<TemplateTable> templateTables = templateTableService.list(Wrappers.<TemplateTable>lambdaQuery().eq(TemplateTable::getTenantId, tenantId));
		List<Layer> layers = layerService.list(Wrappers.<Layer>lambdaQuery());
		return archiveTypes.stream()
				.filter(archiveType -> archiveType.getNodeType().equals(NodeTypeEnum.DATA.getValue()))
				.flatMap(archiveType -> {
					List<TemplateTable> templateTableList = templateTables.stream().filter(templateTable -> templateTable.getTemplateTypeId().equals(archiveType.getTemplateTypeId())).collect(Collectors.toList());
					return templateTableList.stream().flatMap(templateTable -> {
						Layer archiveLayer = layers.stream().filter(layer -> layer.getCode().equals(templateTable.getLayerCode())).findAny().orElseGet(()->new Layer());
						return Stream.of(getArchiveTableData(archiveType, archiveLayer, templateTable));
					});
				}).collect(Collectors.toList());
	}

	/**
	 * 生成archiveTable记录
	 *
	 * @param archiveType  档案门类
	 * @param archiveLayer 档案层级
	 * @return
	 */
	private ArchiveTable getArchiveTableData(ArchiveType archiveType, Layer archiveLayer, TemplateTable templateTable) {
		if (log.isDebugEnabled()) {
			log.debug("门类数据{}", archiveType);
		}
		String storageName = getStorageName(archiveType, archiveLayer);
		String storageLocate = ArchiveTableUtil.getArchiveTableName(archiveType.getTenantId(), archiveType.getClassType(), archiveType.getTypeCode(), templateTable.getId(), archiveLayer.getCode());
		ArchiveTable archiveTable = ArchiveTable.builder().archiveTypeCode(archiveType.getTypeCode()).storageName(storageName)
				.storageLocate(storageLocate).classType(archiveType.getClassType()).templateTableId(templateTable.getId())
				.archiveLayer(archiveLayer.getCode()).tenantId(archiveType.getTenantId()).sortNo(archiveLayer.getSortNo())
				.build();
		return archiveTable;
	}

	private String getStorageName(ArchiveType archiveType, Layer archiveLayer) {
		return new StringBuffer().append(archiveType.getTypeName())
				.append(ArchiveConstants.SYMBOL.LEFT_BRACKET).append(archiveLayer.getName()).append(ArchiveConstants.SYMBOL.RIGHT_BRACKET).toString();
	}

	/**
	 * 获取 初始化模板文件流
	 *
	 * @param templateId 模板id
	 * @return
	 */
	private InputStream getDefaultTemplateStream(Long templateId) {
		TenantTemplate tenantTemplate = remoteTenantTemplateService.selectTemplateById(templateId).getData();
		byte[] bytes = (byte[]) tenantTemplate.getTemplateContent();
		InputStream inputStream = new ByteArrayInputStream(bytes);
		return inputStream;
	}

	@Override
	public List<ArchiveTypeTableSyncTreeNode> getArchiveTypeTableSyncTree(Boolean isCatalog, List<String> fondsCodes) {
		LambdaQueryWrapper<ArchiveType> typeQueryWrapper = Wrappers.<ArchiveType>lambdaQuery();
		typeQueryWrapper.in(ArchiveType::getFondsCode, fondsCodes);
		List<ArchiveType> typelist = this.list(typeQueryWrapper);
		List<ArchiveTypeTableSyncTreeNode> typeTreeList = convertType(typelist);
		// 获取档案表节点
		List<String> typeCodeList = typeTreeList.stream().map(ArchiveTypeTableSyncTreeNode::getDataCode).collect(Collectors.toList());
		LambdaQueryWrapper<ArchiveTable> queryWrapper = Wrappers.<ArchiveTable>lambdaQuery()
				.in(ArchiveTable::getArchiveTypeCode, typeCodeList);
		if (isCatalog) {
			List<String> notCataLogLayer = Stream.of(ArchiveLayerEnum.DOCUMENT.getValue(), ArchiveLayerEnum.INFO.getValue(),
					ArchiveLayerEnum.SIGNATRUE.getValue()).collect(Collectors.toList());
			queryWrapper.notIn(ArchiveTable::getArchiveLayer, notCataLogLayer);
		} else {
			queryWrapper.eq(ArchiveTable::getArchiveLayer, ArchiveLayerEnum.DOCUMENT.getValue());
		}
		List<ArchiveTable> tableList = archiveTableService.list(queryWrapper);
		List<ArchiveTypeTableSyncTreeNode> tableTreeList = convert(tableList);

		return Stream.of(typeTreeList, tableTreeList).flatMap(Collection::stream).collect(Collectors.toList());
	}

	private List<ArchiveTypeTableSyncTreeNode> convertType(List<ArchiveType> typeList) {
		return typeList.stream().map(archiveType -> {
			ArchiveTypeTableSyncTreeNode treeNode = ArchiveTypeTableSyncTreeNode.builder()
					.id(archiveType.getId())
					.name(archiveType.getTypeName())
					.dataCode(archiveType.getTypeCode())
					.fondsName(archiveType.getFondsName())
					.nodeType(NodeTypeEnum.DATA.getValue()).build();
			treeNode.setPk(NodeTypeEnum.DATA.getValue() + "-" + archiveType.getTypeCode());
			treeNode.setParentPk(NodeTypeEnum.DATA.getValue() + "-" + archiveType.getParentId());
			return treeNode;
		}).collect(Collectors.toList());
	}

	private List<ArchiveTypeTableSyncTreeNode> convert(List<ArchiveTable> tableList) {
		return tableList.stream().map(archiveTable -> {
			ArchiveTypeTableSyncTreeNode treeNode = ArchiveTypeTableSyncTreeNode.builder()
					.id(archiveTable.getId())
					.name(archiveTable.getStorageName())
					.dataCode(archiveTable.getStorageLocate())
					.nodeType(NodeTypeEnum.TABLE.getValue()).build();
			treeNode.setPk(NodeTypeEnum.TABLE.getValue() + "-" + archiveTable.getStorageLocate());
			treeNode.setParentPk(NodeTypeEnum.DATA.getValue() + "-" + archiveTable.getArchiveTypeCode());
			return treeNode;
		}).collect(Collectors.toList());
	}

	private List<ArchiveTypeTableSyncTreeNode> peek(List<ArchiveTypeTableSyncTreeNode> archiveTypeList) {
		return archiveTypeList.stream().peek(archiveType -> {
			archiveType.setPk(NodeTypeEnum.DATA.getValue() + "-" + archiveType.getDataCode());
			archiveType.setParentPk(NodeTypeEnum.DATA.getValue() + "-" + archiveType.getParentDataCode());
		}).collect(Collectors.toList());
	}

	@Override
	public List<ArchiveTypeTableTree> getArchiveTypeTableSyncTree() {
		List<ArchiveType> typeList = this.getTypeListByParentId(0L);
		if (CollectionUtil.isEmpty(typeList)) {
			return Collections.emptyList();
		}
		List<ArchiveTypeTableTree> archiveTypeTableTreeList = processChildren(typeList);
		return archiveTypeTableTreeList;
	}

	private List<ArchiveTypeTableTree> processChildren(List<ArchiveType> typeList) {
		List<ArchiveTypeTableTree> TypeTableTreeList = typeList.stream()
				.map(archiveType -> {
					ArchiveTypeTableTree typeTableTree = toTypeTableTree(archiveType);
					if (NodeTypeEnum.CLAZZ.getValue().equals(archiveType.getNodeType())) {
						List<ArchiveType> childrenTypeList = this.getTypeListByParentId(archiveType.getId());
						typeTableTree.setChildren(processChildren(childrenTypeList));
					} else if (NodeTypeEnum.DATA.getValue().equals(archiveType.getNodeType())) {
						List<ArchiveTable> tableList = tableService.getTableListByTypeCode(archiveType.getTypeCode());
						typeTableTree.setChildren(toTypeTableTree(tableList));
					}
					return typeTableTree;
				}).collect(Collectors.toList());
		return TypeTableTreeList;
	}

	private ArchiveTypeTableTree toTypeTableTree(ArchiveType archiveType) {
		return ArchiveTypeTableTree.builder()
				.id(archiveType.getId())
				.name(archiveType.getTypeName())
				.dataCode(archiveType.getTypeCode())
				.nodeType(archiveType.getNodeType())
				.children(Collections.emptyList())
				.build();
	}

	private List<ArchiveTypeTableTree> toTypeTableTree(List<ArchiveTable> tableList) {
		return tableList.stream().map(archiveTable -> ArchiveTypeTableTree.builder()
				.id(archiveTable.getId())
				.name(archiveTable.getStorageName())
				.dataCode(archiveTable.getStorageLocate())
				.nodeType(NodeTypeEnum.TABLE.getValue())
				.children(Collections.emptyList())
				.build()).collect(Collectors.toList());
	}

	/**
	 * @see com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTypeService#checkTemplateType(java.lang.Long)
	 */
	@Override
	public boolean checkTemplateType(Long templateTypeId) {
		int count = this.count(Wrappers.<ArchiveType>lambdaQuery().eq(ArchiveType::getTemplateTypeId, templateTypeId));
		return 0 == count;
	}

	@Override
	public List<ArrayList<String>> getArchivesClassInfor(Long tenantId) {
		//获取模板信息
		final List<TemplateType> templateTypes = templateTypeService.list(Wrappers.<TemplateType>lambdaQuery().eq(TemplateType::getTenantId, tenantId));
		//处理模板信息
		final Map<Long, String> templateTypeMap = templateTypes.stream().collect(Collectors.toMap(TemplateType::getId, TemplateType::getName));
		//查询门类信息
		final List<ArchiveType> archiveTypes = this.list(Wrappers.<ArchiveType>lambdaQuery().eq(ArchiveType::getTenantId, tenantId));
		//所属模板	节点类型	节点名称 节点编码	档案分类	整理规则
		List<ArrayList<String>> collect = archiveTypes.stream().map(archiveType ->
				CollectionUtil.newArrayList(templateTypeMap.get(archiveType.getTemplateTypeId()),
						NodeTypeEnum.getEnum(archiveType.getNodeType()).getName(),
						archiveType.getTypeName(),
						archiveType.getTypeCode(),
						ClassTypeEnum.getEnum(archiveType.getClassType()).getName(),
						FilingTypeEnum.getEnum(archiveType.getFilingType()).getName(),
						archiveType.getSortNo().toString(),
						archiveType.getFondsCode(),
						archiveType.getFondsName())
		).collect(Collectors.toList());
		return collect;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean copyArchiveType(ArchiveTypeCopyPostDTO type) throws ArchiveBusinessException {
		//1、获取待复制的档案类型
		Long srcTypeId = type.getId();
		ArchiveType srcArchiveType = this.getById(srcTypeId);
		//2、生成archiveType、archiveTable、metadata记录
		type.setId(null);
		checkUnique(type);
		ArchiveType archiveType = new ArchiveType();
		BeanUtils.copyProperties(type, archiveType);
		if (StrUtil.isBlank(type.getTargetFondsName())) {
			List<Fonds> currentFondsList = fondsService.getFondsList();
			Fonds currentFonds = currentFondsList.stream().filter(fonds -> StrUtil.equalsAnyIgnoreCase(fonds.getFondsCode(), type.getTargetFondsCode())).findFirst().orElse(null);
			if (type.getTargetFondsCode().equals(ArchiveConstants.FONDS_GLOBAL) && remoteRoleService.isTenantSuperAdmin().getData()) {
				type.setTargetFondsName(type.getFondsName());
			} else if (currentFonds != null) {
				type.setTargetFondsName(currentFonds.getFondsName());
			} else {
				this.isTrue(false, "只有租户管理员才能复制到全局分类");
			}
		}
		archiveType.setFondsCode(type.getTargetFondsCode());
		archiveType.setFondsName(type.getTargetFondsName());
		//获取档案门类最大排序号
		Integer maxSortNo = baseMapper.getMaxSortNo();
		if (ObjectUtil.isNull(maxSortNo)) {
			maxSortNo = 0;
		}
		archiveType.setSortNo(maxSortNo + 1);
		archiveType.setFilingType(srcArchiveType.getFilingType());
		this.save(archiveType);
		archiveType.setIsLeaf(false);
		//如果是档案门类节点，还要创建物理表等其他操作
		// 新创建的表
		final List<ArchiveTable> archiveTables = CollectionUtil.newArrayList();
		// 源元字段和目标元字段map对照表
		final Map<Long, Long> srcDestMetadataMap = MapUtil.newHashMap();
		final Map<String, String> destSrcStorageLocateMap = MapUtil.newHashMap();
		if (archiveType.getNodeType().equals(NodeTypeEnum.DATA.getValue())) {
			List<TemplateTable> templateTableList = templateTableService.getByTemplateTypeId(archiveType.getTemplateTypeId());
			archiveTables.addAll(archiveTableService.createArchiveTableForCopy(srcArchiveType, archiveType, templateTableList, srcDestMetadataMap, destSrcStorageLocateMap));
		}
		//3、拷贝配置信息
		// 配置信息复制
		CompletableFuture cf1 = CompletableFuture.runAsync(() -> archiveTables.stream().forEach(archiveTable -> archiveConfigManageService.copyByStorageLocate(destSrcStorageLocateMap.get(archiveTable.getStorageLocate()), archiveTable.getStorageLocate())));
		// 表单定义配置复制
		CompletableFuture cf2 = CompletableFuture.runAsync(() -> {
			archiveTables.stream().forEach(archiveTable -> {
				archiveEditService.copyByStorageLocate(destSrcStorageLocateMap.get(archiveTable.getStorageLocate()), archiveTable.getStorageLocate(), srcDestMetadataMap);
				archiveEditFormService.copyByStorageLocate(destSrcStorageLocateMap.get(archiveTable.getStorageLocate()), archiveTable.getStorageLocate());
			});
		});
		// 数据规则定义配置复制
		CompletableFuture cf3 = CompletableFuture.runAsync(() -> archiveTables.stream().forEach(archiveTable -> metadataAutovalueService.copyByStorageLocate(destSrcStorageLocateMap.get(archiveTable.getStorageLocate()), archiveTable.getStorageLocate(), srcDestMetadataMap)));
		// 关联关系定义配置复制
		CompletableFuture cf4 = CompletableFuture.runAsync(() -> archiveTables.stream().forEach(archiveTable -> innerRelationService.copyByStorageLocate(destSrcStorageLocateMap.get(archiveTable.getStorageLocate()), archiveTable.getStorageLocate(), srcDestMetadataMap, destSrcStorageLocateMap)));
		// 列表定义配置复制
		CompletableFuture cf5 = CompletableFuture.runAsync(() -> archiveTables.stream().forEach(archiveTable -> archiveListService.copyByStorageLocate(destSrcStorageLocateMap.get(archiveTable.getStorageLocate()), archiveTable.getStorageLocate(), srcDestMetadataMap)));
		// 检索定义配置复制
		CompletableFuture cf6 = CompletableFuture.runAsync(() -> archiveTables.stream().forEach(archiveTable -> archiveSearchService.copyByStorageLocate(destSrcStorageLocateMap.get(archiveTable.getStorageLocate()), archiveTable.getStorageLocate(), srcDestMetadataMap)));
		// 排序自定义配置复制
		CompletableFuture cf7 = CompletableFuture.runAsync(() -> archiveTables.stream().forEach(archiveTable -> archiveSortService.copyByStorageLocate(destSrcStorageLocateMap.get(archiveTable.getStorageLocate()), archiveTable.getStorageLocate(), srcDestMetadataMap)));
		// 鉴定规则配置复制
		CompletableFuture cf8 = CompletableFuture.runAsync(() -> archiveTables.stream().forEach(archiveTable -> dispAppraisalRuleService.copyByStorageLocate(destSrcStorageLocateMap.get(archiveTable.getStorageLocate()), archiveTable.getStorageLocate(), srcDestMetadataMap)));
		// 全文规则配置复制
		CompletableFuture cf9 = CompletableFuture.runAsync(() -> archiveTables.stream().forEach(archiveTable -> linkLayerService.copyByStorageLocate(destSrcStorageLocateMap.get(archiveTable.getStorageLocate()), archiveTable.getStorageLocate(), srcDestMetadataMap)));
		// 装盒配置复制
		CompletableFuture cf10 = CompletableFuture.runAsync(() -> archiveTables.stream().forEach(archiveTable -> metadataBoxConfigService.copyByStorageLocate(destSrcStorageLocateMap.get(archiveTable.getStorageLocate()), archiveTable.getStorageLocate(), srcDestMetadataMap)));
		// 水印规则配置复制
		CompletableFuture cf11 = CompletableFuture.runAsync(() -> archiveTables.stream().forEach(archiveTable -> watermarkService.copyByStorageLocate(destSrcStorageLocateMap.get(archiveTable.getStorageLocate()), archiveTable.getStorageLocate(), srcDestMetadataMap)));
		// 报表定义配置复制
		CompletableFuture cf12 = CompletableFuture.runAsync(() -> archiveTables.stream().forEach(archiveTable -> reportService.copyByStorageLocate(destSrcStorageLocateMap.get(archiveTable.getStorageLocate()), archiveTable.getStorageLocate(), srcDestMetadataMap)));
		// 按钮定义配置复制
		CompletableFuture cf13 = CompletableFuture.runAsync(() -> archiveTables.stream().forEach(archiveTable -> remoteTenantArchiveButtonService.copyByStorageLocate(destSrcStorageLocateMap.get(archiveTable.getStorageLocate()), archiveTable.getStorageLocate(), SecurityConstants.FROM_IN)));
		// 拷贝档案类型绑定保管期限
		CompletableFuture<Void> cf14 = CompletableFuture.runAsync(() -> archiveRetentionRelationService.copyByTypeCode(srcArchiveType.getTypeCode(), archiveType.getTypeCode()));
		CompletableFuture<Void> all = CompletableFuture.allOf(cf1, cf2, cf3, cf4, cf5, cf6, cf7, cf8, cf9, cf10, cf11, cf12, cf13, cf14);
		all.join();
		//复制四性检测方案
		remoteArchiveFourCheckService.copyByTypeCode(srcArchiveType.getTypeCode(), archiveType.getTypeCode());
		return true;
	}

	@Override
	public List<ArchiveType> getArchiveTypes() {
		return this.list();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean archiveTypeOrder(ArchiveTypeOrderDTO archiveTypeOrderDTO) {
		final Long parentId = archiveTypeOrderDTO.getParentId();
		if (!ArchiveConstants.TREE_ROOT_NODE_VALUE.equals(parentId)) {
			return false;
		}
		final List<Long> ids = archiveTypeOrderDTO.getIds();
		List<ArchiveType> archiveTypes = this.list(Wrappers.<ArchiveType>lambdaQuery().in(ArchiveType::getId, ids));
		final List<ArchiveType> list = IntStream.rangeClosed(1, ids.size()).mapToObj(i -> {
			final ArchiveType archiveType = archiveTypes.parallelStream().filter(e -> e.getId().equals(ids.get(i - 1))).findAny().get();
			archiveType.setSortNo(i);
			return archiveType;
		}).collect(Collectors.toList());
		return this.updateBatchById(list);
	}

	/**
	 * 获取列表主要信息 列表
	 *
	 * @param parentId
	 * @param result
	 */
	@Override
	public List<String> getAllMainInfoForChildren(Long parentId, List<String> result) {
		List<ArchiveType> children = this.list(Wrappers.<ArchiveType>lambdaQuery().eq(ArchiveType::getParentId, parentId));
		List<String> idList = children.stream().map(ArchiveType::getTypeCode).distinct().collect(Collectors.toList());
		idList.forEach(id -> {
			if (!result.contains(id)) {
				result.add(id);
			}
		});
		if (CollectionUtil.isNotEmpty(children)) {
			for (ArchiveType archiveType : children) {
				getAllMainInfoForChildren(archiveType.getId(), result);
			}
		}
		return result;
	}

	@Override
	public List<FondsArchiveTypeSyncTreeNode> getFondsTypeTree(Long id, String fondsCode) {
		// 全宗节点
		if (SysConstant.Virtual.ROOT_PARENT_CODE.equals(id)) {
			return getFondsTreeNode(fondsCode);
		}
		List<ArchiveType> archiveTypeList = this.list(Wrappers.<ArchiveType>lambdaQuery().eq(ArchiveType::getParentId, id).eq(ArchiveType::getFondsCode, fondsCode));
		return convertTypeNode(archiveTypeList);
	}

	@Override
	public List<FondsArchiveTypeSyncTreeNode> getFondsTypeTableTree(Long id, String fondsCode, String nodeClass, String typeCode, Integer showDocument) {
		// 全宗节点
		if (SysConstant.Virtual.ROOT_PARENT_CODE.equals(id)) {
			return getFondsTreeNode(fondsCode);
		}
		if (FondsConstant.FONDS_TREE_CLASS.equals(nodeClass) || NodeTypeEnum.CLAZZ.getValue().equals(nodeClass)) {
			List<ArchiveType> archiveTypeList = this.list(Wrappers.<ArchiveType>lambdaQuery().eq(ArchiveType::getParentId, id).eq(ArchiveType::getFondsCode, fondsCode));
			return convertTypeNode(archiveTypeList);
		}
		if (NodeTypeEnum.DATA.getValue().equals(nodeClass)) {
			List<ArchiveTable> tableList = archiveTableService.getTableListByTypeCode(typeCode);
			Stream<ArchiveTable> tableStream = tableList.stream().filter(archiveTable -> !ArchiveLayerEnum.INFO.getCode().equals(archiveTable.getArchiveLayer()));
			if (BoolEnum.NO.getCode().equals(showDocument)) {
				tableStream = tableStream.filter(archiveTable -> !ArchiveLayerEnum.DOCUMENT.getCode().equals(archiveTable.getArchiveLayer()));
			}
			return convertTableNode(tableStream.collect(Collectors.toList()));
		}
		return Collections.emptyList();
	}

	@Override
	public List<FondsArchiveTypeSyncTreeNode> getFondsAllTypeTableTree(String fondsCode) {
			List<FondsArchiveTypeSyncTreeNode>  fondsArchiveTypeSyncTreeNodeList = getFondsTreeNode(fondsCode);;
			fondsArchiveTypeSyncTreeNodeList.forEach(fondsArchiveTypeSyncTreeNode ->{
				List<ArchiveType> archiveTypeList = this.list(Wrappers.<ArchiveType>lambdaQuery().eq(ArchiveType::getParentId, fondsArchiveTypeSyncTreeNode.getArchiveTypeId()).eq(ArchiveType::getFondsCode, fondsArchiveTypeSyncTreeNode.getFondsCode()));
				List<FondsArchiveTypeSyncTreeNode> result =convertTypeNode(archiveTypeList);
				result.stream().forEach(fondsArchiveTypeTreeNode -> {
					List<ArchiveTable> tableList = archiveTableService.getTableListByTypeCode(fondsArchiveTypeTreeNode.getTypeCode());
					Stream<ArchiveTable> tableStream = tableList.stream().filter(archiveTable -> !ArchiveLayerEnum.INFO.getCode().equals(archiveTable.getArchiveLayer())&&!ArchiveLayerEnum.SIGNATRUE.getCode().equals(archiveTable.getArchiveLayer()));
					fondsArchiveTypeTreeNode.setChildren(convertTableNode(tableStream.collect(Collectors.toList())));
				});
				fondsArchiveTypeSyncTreeNode.setChildren(result);
			});
			return fondsArchiveTypeSyncTreeNodeList;
	}

	private List<FondsArchiveTypeSyncTreeNode> convertTableNode(List<ArchiveTable> tableList) {
		return tableList.stream().map(archiveTable -> {
			FondsArchiveTypeSyncTreeNode treeNode = FondsArchiveTypeSyncTreeNode.builder().name(archiveTable.getStorageName())
					.nodeClass(NodeTypeEnum.TABLE.getValue()).typeCode(archiveTable.getArchiveTypeCode()).archiveLayer(archiveTable.getArchiveLayer())
					.storageLocate(archiveTable.getStorageLocate()).templateTableId(archiveTable.getTemplateTableId()).isLeaf(Boolean.TRUE).build();
			treeNode.setPk(treeNode.getNodeClass() + StrUtil.DASHED + treeNode.getStorageLocate());
			return treeNode;
		}).collect(Collectors.toList());
	}

	private List<FondsArchiveTypeSyncTreeNode> getFondsTreeNode(String fondsCode){
		Fonds globalFonds = Fonds.builder().fondsCode(FondsConstant.GLOBAL_FONDS_CODE).fondsName(SysConstant.Virtual.ROOT_NAME).build();
		List<Fonds> fondsList;
		if (StrUtil.isBlank(fondsCode)) {
			// 如果fondsCode为空，则获取权限的全宗集合
			fondsList = fondsService.list();
			fondsList.add(0, globalFonds);
		} else {
			fondsList = CollectionUtil.newArrayList(globalFonds, fondsService.getFondsByCode(fondsCode));
		}
		// 去掉没有自己创建档案门类的全宗
		List<String> fondsCodeList = this.getBaseMapper().getDistinctFondscode(fondsList.stream().map(Fonds::getFondsCode).collect(Collectors.toList()));
		fondsList = fondsList.stream().filter(fonds -> fondsCodeList.contains(fonds.getFondsCode())).collect(Collectors.toList());
		return convertFondsNode(fondsList);
	}

	@Override
	public List<FondsArchiveTypeSyncTreeNode> getFondsTreeNode1(String fondsCode){
		Fonds globalFonds = Fonds.builder().fondsCode(FondsConstant.GLOBAL_FONDS_CODE).fondsName(SysConstant.Virtual.ROOT_NAME).build();
		List<Fonds> fondsList;
		if (StrUtil.isBlank(fondsCode)) {
			// 如果fondsCode为空，则获取权限的全宗集合
			fondsList = fondsService.list();
			fondsList.add(0, globalFonds);
		} else {
			fondsList = CollectionUtil.newArrayList(globalFonds, fondsService.getFondsByCode(fondsCode));
		}
		// 去掉没有自己创建档案门类的全宗
		List<String> fondsCodeList = this.getBaseMapper().getDistinctFondscode(fondsList.stream().map(Fonds::getFondsCode).collect(Collectors.toList()));
		fondsList = fondsList.stream().filter(fonds -> fondsCodeList.contains(fonds.getFondsCode())).collect(Collectors.toList());
		return convertFondsNode(fondsList);
	}
	private List<FondsArchiveTypeSyncTreeNode> convertTypeNode(List<ArchiveType> archiveTypeList) {
		return archiveTypeList.stream().map(archiveType -> {
			FondsArchiveTypeSyncTreeNode treeNode = FondsArchiveTypeSyncTreeNode.builder().archiveTypeId(archiveType.getId())
					.fondsCode(archiveType.getFondsCode()).name(archiveType.getTypeName()).nodeClass(archiveType.getNodeType())
					.templateTypeId(archiveType.getTemplateTypeId()).typeCode(archiveType.getTypeCode()).build();
			treeNode.setPk(treeNode.getNodeClass() + StrUtil.DASHED + treeNode.getArchiveTypeId());
			if (NodeTypeEnum.DATA.getValue().equals(archiveType.getNodeType())) {
				List<ArchiveTable> tableList = archiveTableService.getTableListByTypeCode(archiveType.getTypeCode());
				if (CollectionUtil.isNotEmpty(tableList)) {
					treeNode.setTemplateTableId(tableList.get(0).getTemplateTableId());
					treeNode.setStorageLocate(tableList.get(0).getStorageLocate());
					treeNode.setArchiveLayer(tableList.get(0).getArchiveLayer());
				}
			}
			return treeNode;
		}).collect(Collectors.toList());
	}

	private List<FondsArchiveTypeSyncTreeNode> convertFondsNode(List<Fonds> fondsList) {
		return fondsList.stream().map(fonds -> {
			FondsArchiveTypeSyncTreeNode treeNode = FondsArchiveTypeSyncTreeNode.builder().archiveTypeId(SysConstant.Virtual.ROOT_CODE)
					.fondsCode(fonds.getFondsCode()).name(fonds.getFondsName()).nodeClass(FondsConstant.FONDS_TREE_CLASS).build();
			treeNode.setPk(treeNode.getNodeClass() + StrUtil.DASHED + treeNode.getFondsCode());
			return treeNode;
		}).collect(Collectors.toList());
	}

	@Override
	public  List<ArchiveType>  getTypeNameByTableIds( Long tenantId,String ids){
		return this.getBaseMapper().getTypeNameByTableIds(tenantId,ids);
	}

	@Override
	public void updateArchiveTypeTree(String fondsName, String fondsCode) {
		this.getBaseMapper().updateArchiveTypeTree(fondsName, fondsCode);
		this.getBaseMapper().updateArchiveTree(fondsName, fondsCode);
	}

	@Override
	public ArchiveType getByTypeName(String typeName, Long tenantId) throws ArchiveBusinessException {
		List<ArchiveType> archiveTypeList = this.list(Wrappers.<ArchiveType>query().lambda().eq(ArchiveType::getTypeName, typeName).eq(ArchiveType::getTenantId, tenantId));
		if (CollectionUtils.isEmpty(archiveTypeList)) {
			log.debug("找不到档案门类名称为{}的档案门类" + typeName);
			throw new ArchiveBusinessException("找不到档案门类名称为" + typeName + "的档案门类");
		}
		return archiveTypeList.get(0);
	}
}
