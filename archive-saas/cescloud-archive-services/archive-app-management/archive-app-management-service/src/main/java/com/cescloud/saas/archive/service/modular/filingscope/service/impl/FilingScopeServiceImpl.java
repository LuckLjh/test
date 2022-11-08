
package com.cescloud.saas.archive.service.modular.filingscope.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.ArchiveTreeQueryDTO;
import com.cescloud.saas.archive.api.modular.archivedict.entity.DictItem;
import com.cescloud.saas.archive.api.modular.archivetree.constant.ArchiveTreeNodeEnum;
import com.cescloud.saas.archive.api.modular.archivetree.entity.ArchiveTree;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveType;
import com.cescloud.saas.archive.api.modular.authority.dto.ArchiveTreeAuthDTO;
import com.cescloud.saas.archive.api.modular.authority.entity.ArchiveTreeAuth;
import com.cescloud.saas.archive.api.modular.authority.feign.RemoteSysAuthService;
import com.cescloud.saas.archive.api.modular.filingscope.dto.*;
import com.cescloud.saas.archive.api.modular.filingscope.entity.FilingScope;
import com.cescloud.saas.archive.api.modular.filingscope.entity.FilingScopeType;
import com.cescloud.saas.archive.api.modular.fonds.dto.FondsConstant;
import com.cescloud.saas.archive.api.modular.fonds.entity.Fonds;
import com.cescloud.saas.archive.api.modular.role.feign.RemoteRoleService;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantTemplate;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantTemplateService;
import com.cescloud.saas.archive.common.constants.*;
import com.cescloud.saas.archive.common.util.ArchiveUtil;
import com.cescloud.saas.archive.common.util.InitializeUtil;
import com.cescloud.saas.archive.service.modular.archivedict.service.DictItemService;
import com.cescloud.saas.archive.service.modular.archivetree.service.ArchiveTreeService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTypeService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import com.cescloud.saas.archive.service.modular.common.security.service.CesCloudUser;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.filepush.util.service.PushFileOpenService;
import com.cescloud.saas.archive.service.modular.filingscope.handler.FilingScopeImportExcelListener;
import com.cescloud.saas.archive.service.modular.filingscope.mapper.FilingScopeMapper;
import com.cescloud.saas.archive.service.modular.filingscope.service.FilingScopeService;
import com.cescloud.saas.archive.service.modular.filingscope.service.FilingScopeTypeService;
import com.cescloud.saas.archive.service.modular.fonds.service.FondsService;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 归档范围定义
 *
 * @author xieanzhu
 * @date 2019-04-22 15:45:22
 */
@Service
@Slf4j
@RequiredArgsConstructor
@CacheConfig(cacheNames = "filing-scope")
public class FilingScopeServiceImpl extends ServiceImpl<FilingScopeMapper, FilingScope> implements FilingScopeService {
	@Autowired
	private DictItemService dictItemService;
	//调用档案信息
	@Autowired
	private ArchiveTypeService archiveTypeService;
	@Autowired
	private ArchiveTreeService archiveTreeService;
	private final RemoteTenantTemplateService remoteTenantTemplateService;
	@Autowired
	private FilingScopeTypeService filingScopeTypeService;
	//考虑是否是租户管理员
	private final RemoteRoleService remoteRoleService;
	//全宗查询
	private final FondsService fondsService;
	private final RemoteSysAuthService remoteSysAuthService;
	@Autowired
	private ArchiveUtil archiveUtil;
	@javax.annotation.Resource
	private ResourceLoader resourceLoader;
	@Autowired
	private PushFileOpenService pushFileOpenService;
	public static final String[] HEADERS = {"上级节点", "分类名称", "分类号", "关联档案门类"};

	/*
	 * @Author xieanzhu
	 * @Description //更新归档范围定义
	 * @Date 11:23 2019/4/26
	 * @Param [filingScopeDTO]
	 * @return com.cescloud.saas.archive.api.modular.filingscope.dto.FilingScopeDTO
	 **/
	@Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public FilingScopeDTO updateFilingScope(FilingScopePutDTO filingScopePutDTO) throws ArchiveBusinessException {
		if (log.isDebugEnabled()) {
			log.debug("修改分类名称为{}，分类号为{}的分类节点信息", filingScopePutDTO.getClassName(), filingScopePutDTO.getClassNo());
		}
		//判断是否存在
		FilingScope filingScope = this.getById(filingScopePutDTO.getId());
		if (ObjectUtil.isNull(filingScope)) {
			log.error("修改失败，归档范围分类不存在，id为{}", filingScopePutDTO.getId());
			throw new ArchiveBusinessException("修改失败，归档范围分类不存在，id为" + filingScopePutDTO.getId());
		}
		boolean isNotChangeFondsCode = true;
		//校验树名称重复
		if (filingScopePutDTO.getParentClassId() == 0L) {
			isNotChangeFondsCode = StrUtil.equalsAnyIgnoreCase(filingScope.getFondsCode(), filingScopePutDTO.getFondsCode());
			if (StrUtil.equalsAnyIgnoreCase(ArchiveConstants.FONDS_GLOBAL, filingScope.getFondsCode()) || StrUtil.equalsAnyIgnoreCase(ArchiveConstants.FONDS_GLOBAL, filingScopePutDTO.getFondsCode())) {
				R<Boolean> tenantSuperAdmin = remoteRoleService.isTenantSuperAdmin();
				this.isTrue(tenantSuperAdmin != null && BooleanUtil.isTrue(tenantSuperAdmin.getData()), "租户管理员才有权限修改全局门类");
				filingScope.setFondsName(ArchiveConstants.FONDS_GLOBAL_NAME);
			}
			//验证目前全宗是否在权限范围内
			if (!StrUtil.equalsAnyIgnoreCase(ArchiveConstants.FONDS_GLOBAL, filingScopePutDTO.getFondsCode())) {
				List<Fonds> currentFondsList = fondsService.getFondsList();
				Fonds currentFonds = currentFondsList.stream().filter(fonds -> StrUtil.equalsAnyIgnoreCase(fonds.getFondsCode(), filingScopePutDTO.getFondsCode())).findFirst().orElse(null);
				this.isTrue(currentFonds != null, "目前的全宗不在您的权限范围内");
				filingScope.setFondsCode(currentFonds.getFondsCode());
				filingScope.setFondsName(currentFonds.getFondsName());
			}
			//查询到目前归档范围树是否已经重名
			FilingScope selectFonds = this.getOne(Wrappers.<FilingScope>query().lambda()
					.eq(FilingScope::getFondsCode, filingScopePutDTO.getFondsCode())
					.eq(FilingScope::getClassName, filingScopePutDTO.getClassName())
					.eq(FilingScope::getParentClassId, filingScopePutDTO.getParentClassId())
					.ne(FilingScope::getId, filingScopePutDTO.getId()), false);
			if (ObjectUtil.isNotNull(selectFonds)) {
				throw new ArchiveRuntimeException(String.format("存在相同的归档范围名称：[%s]", filingScopePutDTO.getClassName()));
			}
		}
		String oldClassName = filingScope.getClassName();
		String oldClassNo = filingScope.getClassNo();
		BeanUtil.copyProperties(filingScopePutDTO, filingScope);
		String newClassNo = filingScope.getClassNo();
		//查询节点的父节点
		FilingScope parentFilingScope = this.getById(filingScope.getParentClassId());
		if (ObjectUtil.isNotNull(parentFilingScope) && parentFilingScope.getParentClassId() == 0L) {
			List<FilingScope> otherRootFilingScopeList = this.list(Wrappers.<FilingScope>query().lambda().eq(FilingScope::getParentClassId, parentFilingScope.getId()).and(i -> i.eq(FilingScope::getClassNo, filingScope.getClassNo())));
			otherRootFilingScopeList = otherRootFilingScopeList.stream().filter(t -> t.getId().longValue() != filingScope.getId().longValue()).collect(Collectors.toList());
			this.isTrue(otherRootFilingScopeList.size() == 0, "同一个归档树上，分类号不允许重复");
		}
		// 如果分类号不一样，则更新子孙节点的path字段
		if (!StrUtil.equals(oldClassNo, newClassNo)) {
			String path = StrUtil.isBlank(parentFilingScope.getClassNo()) ? (newClassNo) : (parentFilingScope.getClassNo() + "," + newClassNo);
			filingScope.setPath(path);
			updateChildrenPath(filingScope.getId(), path);
		}
		List<Long> filingScopeIdList = Lists.newArrayList(filingScope.getId());
		List<ArchiveTree> archiveTreeList = this.archiveTreeService.getArchiveTreeByFilingScopeOrArchiveType(null, filingScopeIdList);
		if (!isNotChangeFondsCode) {
			//检查 范围是否被归档树 引用
			this.getAllMainInfoForChildren(filingScope.getId(), filingScopeIdList);
			log.info("目前递归子范围id列表:{}", JSONUtil.toJsonStr(filingScopeIdList));
			this.isTrue(CollectionUtil.isEmpty(archiveTreeList), "归档范围被档案树引用，不允许修改全宗！");
			updateChildrenFondsCode(filingScope.getId(), filingScope.getFondsCode(), filingScope.getFondsName());
		}
		if (!StrUtil.equals(oldClassNo, newClassNo) || !StrUtil.equals(filingScope.getClassName(), oldClassName)) {
			//更新引用归档范围树节点信息
			List<ArchiveTree> trees = archiveTreeList.stream().map(e -> ArchiveTree.builder().id(e.getId()).treeValue(newClassNo).treeName(filingScope.getClassName()).build()).collect(Collectors.toList());
			archiveTreeService.updateBatchById(trees);
		}
		boolean result = this.updateById(filingScope);
		if (!result) {
			log.error("修改归档范围分类失败!");
			throw new ArchiveBusinessException("修改归档范围分类失败!");
		}
		FilingScopeDTO filingScopeDTO = new FilingScopeDTO();
		BeanUtil.copyProperties(filingScopePutDTO, filingScopeDTO);
		return convertFilingScopeDTO(filingScopeDTO);
	}

	/**
	 * 递归批量修改子节点的全宗名称及编码
	 *
	 * @param parentClassId
	 * @param fondsCode
	 * @param fondsName
	 */
	private void updateChildrenFondsCode(Long parentClassId, String fondsCode, String fondsName) {
		List<FilingScope> children = this.list(Wrappers.<FilingScope>lambdaQuery().eq(FilingScope::getParentClassId, parentClassId));
		if (CollectionUtil.isNotEmpty(children)) {
			for (FilingScope filingScope : children) {
				filingScope.setFondsCode(fondsCode);
				filingScope.setFondsName(fondsName);
			}
			this.updateBatchById(children);
			for (FilingScope filingScope : children) {
				updateChildrenFondsCode(filingScope.getId(), fondsCode, fondsName);
			}
		}
	}

	private void updateChildrenPath(Long parentClassId, String parentPath) {
		List<FilingScope> children = this.list(Wrappers.<FilingScope>lambdaQuery().eq(FilingScope::getParentClassId, parentClassId));
		if (CollectionUtil.isNotEmpty(children)) {
			for (FilingScope filingScope : children) {
				String path = StrUtil.isBlank(parentPath) ? (filingScope.getClassNo()) : (parentPath + "," + filingScope.getClassNo());
				filingScope.setPath(path);
			}
			this.updateBatchById(children);
			for (FilingScope filingScope : children) {
				updateChildrenPath(filingScope.getId(), filingScope.getPath());
			}
		}
	}

	/**
	 * @return com.cescloud.saas.archive.api.modular.filingscope.dto.FilingScopeDTO
	 * @Author xieanzhu
	 * @Description //TODO
	 * @Date 13:43 2019/4/25
	 * @Param [id]
	 **/
	@Override
	@Cacheable(
			key = "'archive-app-management:filingScopeDTO:id:'+#id",
			unless = "#result == null"
	)
	public FilingScopeDTO getFilingScopeDTOById(Long id) throws ArchiveBusinessException {
		if (log.isDebugEnabled()) {
			log.debug("查找id为{}的归档范围节点信息", id);
		}

		FilingScope filingScope = this.getById(id);
		if (ObjectUtil.isNull(filingScope)) {
			log.error("找不到id为{}的归档范围节点信息", id);
			throw new ArchiveBusinessException("找不到id为" + id + "的归档范围节点信息");
		}
		FilingScopeDTO filingScopeDTO = new FilingScopeDTO();
		BeanUtil.copyProperties(filingScope, filingScopeDTO);
		return convertFilingScopeDTO(filingScopeDTO);
	}

	/**
	 * @return java.util.List<com.cescloud.saas.archive.api.modular.filingscope.dto.FilingScopeDTO>
	 * @Author xieanzhu
	 * @Description //保存分类节点
	 * @Date 17:35 2019/4/22
	 * @Param [parentClassId, filingScopeDTO]
	 **/
	@Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public FilingScopeDTO saveFilingScope(FilingScopePostDTO filingScopePostDTO) throws ArchiveBusinessException {
		if (log.isDebugEnabled()) {
			log.debug("新增分类名称为{}，分类号为{}的分类节点信息", filingScopePostDTO.getClassName(), filingScopePostDTO.getClassNo());
		}
		//校验归档范围树名称是否重复
		if (filingScopePostDTO.getParentClassId() == 0) {
			//目前是修改 归档范围树
			this.isTrue(StrUtil.isNotBlank(filingScopePostDTO.getFondsCode()), "必须传递有效全宗编号");

			//如果是树的顶级节点名称，则要校验
			FilingScope selectFonds = null;
			if (ObjectUtil.isNotNull(filingScopePostDTO.getTenantId())) {
				selectFonds = this.getOne(Wrappers.<FilingScope>query().lambda().eq(FilingScope::getFondsCode, filingScopePostDTO.getFondsCode())
						.eq(FilingScope::getClassName, filingScopePostDTO.getClassName())
						.eq(FilingScope::getParentClassId, filingScopePostDTO.getParentClassId())
						.eq(FilingScope::getTenantId, filingScopePostDTO.getTenantId()), false);
			} else {
				selectFonds = this.getOne(Wrappers.<FilingScope>query().lambda().eq(FilingScope::getFondsCode, filingScopePostDTO.getFondsCode())
						.eq(FilingScope::getClassName, filingScopePostDTO.getClassName())
						.eq(FilingScope::getParentClassId, filingScopePostDTO.getParentClassId()), false);
			}
			if (ObjectUtil.isNotNull(selectFonds)) {
				throw new ArchiveRuntimeException(String.format("存在相同的归档范围名称：[%s]", filingScopePostDTO.getClassName()));
			}
		} else {
			//查询新建节点的树根节点
			FilingScope filingScope = getRootFilingScope(filingScopePostDTO.getParentClassId());
			//获取该树根节点下的所有子节点
			List<FilingScope> list = this.list(Wrappers.<FilingScope>lambdaQuery().eq(FilingScope::getParentClassId, filingScope.getId())
					.select(FilingScope::getId, FilingScope::getClassNo, FilingScope::getClassNo, FilingScope::getClassName));
			List<FilingScope> allChildrenFilingScopeList = CollUtil.newArrayList();
			getAllChildrenFilingScope(list, allChildrenFilingScopeList);
			boolean anyMatch = allChildrenFilingScopeList.stream().anyMatch(e -> e.getClassNo().equals(filingScopePostDTO.getClassNo()));
			this.isTrue(!anyMatch, "同一个归档树上，分类号不允许重复");
		}

		//新建分类节点
		FilingScope filingScopeChild = new FilingScope();
		BeanUtil.copyProperties(filingScopePostDTO, filingScopeChild);
		// 填充归档范围层级path字段
		FilingScope filingScope = this.getById(filingScopePostDTO.getParentClassId());
		if (ObjectUtil.isNotNull(filingScope)) {
			String path = StrUtil.isBlank(filingScope.getPath()) ? (filingScopeChild.getClassNo()) : (filingScope.getPath() + "," + filingScopeChild.getClassNo());
			filingScopeChild.setPath(path);
		}

		if (filingScopePostDTO.getParentClassId() == 0) {
			//范围树的新增 考虑添加全宗
			if (StrUtil.equalsAnyIgnoreCase(ArchiveConstants.FONDS_GLOBAL, filingScopeChild.getFondsCode())) {
				R<Boolean> tenantSuperAdmin = remoteRoleService.isTenantSuperAdmin();
				this.isTrue(tenantSuperAdmin != null && BooleanUtil.isTrue(tenantSuperAdmin.getData()), "租户管理员才能够创建全局范围树");
				filingScopeChild.setFondsName(ArchiveConstants.FONDS_GLOBAL_NAME);
			} else {
				List<Fonds> currentFondsList = fondsService.getFondsList();
				log.info("目前的全宗信息列表：{}", currentFondsList);
				Fonds currentFonds = currentFondsList.stream().filter(fonds -> StrUtil.equalsAnyIgnoreCase(fonds.getFondsCode(), filingScopeChild.getFondsCode())).findFirst().orElse(null);
				this.isTrue(currentFonds != null, "目前的全宗不在您的权限范围内");
				filingScopeChild.setFondsCode(currentFonds.getFondsCode());
				filingScopeChild.setFondsName(currentFonds.getFondsName());
			}
			// 1层级 及 2层级 的父节点都是 0
			if (ObjectUtil.isNotNull(filingScopeChild) && (filingScopeChild.getParentClassId() == null || filingScopeChild.getParentClassId().longValue() == ArchiveConstants.TREE_FONDS_TYPE_NODE || filingScopeChild.getParentClassId().longValue() == ArchiveConstants.TREE_FONDS_NODE)) {
				filingScopeChild.setParentClassId(ArchiveConstants.TREE_ROOT_NODE_VALUE);
			}
		}
		CesCloudUser cesCloudUser = SecurityUtils.getUser();
		if (cesCloudUser != null) {
			filingScopeChild.setCreatedUserName(cesCloudUser.getChineseName());
		}
		Integer maxSortNo = this.baseMapper.selectMaxSortNo(filingScopeChild.getParentClassId());
		filingScopeChild.setSortNo(ObjectUtil.isNull(maxSortNo) ? 1 : maxSortNo + 1);
		boolean result = saveOrUpdate(filingScopeChild);
		if (!result) {
			log.error("新建归档范围分类节点失败，父节点ID为{}", filingScopePostDTO.getParentClassId());
			throw new ArchiveBusinessException("新建归档范围分类节点失败，父节点ID为" + filingScopePostDTO.getParentClassId() + "！");
		}
		FilingScopeDTO filingScopeDTO = new FilingScopeDTO();
		BeanUtil.copyProperties(filingScopePostDTO, filingScopeDTO);
		filingScopeDTO.setId(filingScopeChild.getId());
		// 归档范围管理新增的树节点 同步更新到 档案树管理的节点上
		SynchronizeTreeNodeToArchiveTree(filingScopeChild);
		return convertFilingScopeDTO(filingScopeDTO);
	}

	@Override
	public Integer selectMaxSortNo(Long parentClassId){
		return this.baseMapper.selectMaxSortNo(parentClassId);
	}

	@Override
	public void getAllChildrenFilingScope(List<FilingScope> list, List<FilingScope> allChildrenFilingScopeList) {
		allChildrenFilingScopeList.addAll(list);
		if (CollUtil.isEmpty(list)) {
			return;
		}
		List<Long> ids = list.stream().map(FilingScope::getId).collect(Collectors.toList());
		List<FilingScope> downList = this.list(Wrappers.<FilingScope>lambdaQuery().in(FilingScope::getParentClassId, ids)
				.select(FilingScope::getId, FilingScope::getClassNo, FilingScope::getPath, FilingScope::getTypeCode, FilingScope::getClassName));
		getAllChildrenFilingScope(downList, allChildrenFilingScopeList);
	}

	private FilingScope getRootFilingScope(Long parentClassId) throws ArchiveBusinessException {
		FilingScope filingScope = this.getById(parentClassId);
		if (ObjectUtil.isNull(filingScope)) {
			log.error("新建归档范围分类节点失败，找不到父节点ID为{}", parentClassId);
			throw new ArchiveBusinessException("新建归档范围分类节点失败，找不到ID为" + parentClassId + "的父节点！");
		} else {
			if (filingScope.getParentClassId() == 0) {
				return filingScope;
			} else {
				return getRootFilingScope(filingScope.getParentClassId());
			}
		}
	}

	/**
	 * add by hyq
	 *2021.9.27
	 * 归档范围管理新增的树节点 同步更新到 档案树管理的节点上
	 * @param filingScope
	 */
	private void SynchronizeTreeNodeToArchiveTree(FilingScope filingScope) {
		// 去到那棵树
/*		ArchiveTree entity = new ArchiveTree();
		List<Map<String, Object>> extendValList = new ArrayList<>();
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> map1 = new HashMap<String, Object>(); // 要创建的 节点
		map1.put("id",filingScope.getId());
		map1.put("status",true);
		extendValList.add(map);
		extendValList.add(map1);*/
		//archiveTreeService.save(filingScopeDTO);
		//找一下那些树有这个创建出来的父节点，由于只添加叶节点下新增的节点 所以判断要加上.getParentId() != 0L
		List<Long> filingScopeIdList = new ArrayList<>();
		filingScopeIdList.add(filingScope.getParentClassId());
		List<ArchiveTree> archiveTreeNodes = archiveTreeService.getArchiveTreeByFilingScopeOrArchiveType(null,filingScopeIdList);
		List<ArchiveTree> archiveTrees = archiveTreeNodes.stream().filter(
				archiveTreeNode -> archiveTreeNode.getParentId() != -1L &&  archiveTreeNode.getIsLeaf() == true
		).collect(Collectors.toList());
		if (archiveTrees.size()>0){ // 有这个档案树可以添加节点
			archiveTrees.forEach( archiveTree -> {
				final ArchiveTree entity = new ArchiveTree();
				entity.setParentId(archiveTree.getId());
				entity.setNodeType(ArchiveTreeNodeEnum.FILING_SCOPE.getCode());
				entity.setTreeName(filingScope.getClassName());
				entity.setTreeValue(filingScope.getClassNo());
				entity.setArchiveTypeCode(filingScope.getTypeCode());
				entity.setFilingScopeId(filingScope.getId());
				entity.setFilingType(archiveTree.getFilingType());
				entity.setTemplateTableId(archiveTree.getTemplateTableId());
				entity.setLayerCode(archiveTree.getLayerCode());
				entity.setFondsCode(archiveTree.getFondsCode());
				entity.setFondsName(archiveTree.getFondsName());
				archiveTreeService.save(entity);
				//查询下 权限这边有没有选中新增这棵树的父节点，还必须，有的话给他选择上
				R<List<ArchiveTreeAuth>> result = remoteSysAuthService.findAuthTreeNodeByTreeId(entity.getParentId());
				if(result.getData().size()>0){
					List<ArchiveTreeAuth> ArchiveTreeAuthLists = result.getData();
					ArchiveTreeAuthLists.stream().forEach(
							ArchiveTreeAuth -> {
								ArchiveTreeAuthDTO dto = new ArchiveTreeAuthDTO();
								BeanUtil.copyProperties(ArchiveTreeAuth,dto);
								Collection<Long> collection = new ArrayList<>();
								collection.add(entity.getId());
								dto.setArchiveTreeIdList(collection);
								remoteSysAuthService.saveArchiveTreeAuth(dto);
							});
				}
			});
		}
	}

	/**
	 * @return java.util.List<com.cescloud.saas.archive.api.modular.filingscope.DTO.FilingScopeDTO>
	 * @Author xieanzhu
	 * @Description //根据归档范围所属父节点的id查找子节点下所有归档分类信息
	 * @Date 17:17 2019/4/22
	 * @Param [parentClassId]
	 **/
	@Override
	public List<FilingScopeDTO> findFilingScopeByParentClassId(Long parentClassId, List<String> fondsCodes) {
		if(fondsCodes == null){
			fondsCodes = new ArrayList<>();
		}
		if (log.isDebugEnabled()) {
			log.debug("查询节点id为{}下所有节点信息", parentClassId);
		}

		Long fondsParentId = parentClassId;
		if (parentClassId != null && (parentClassId.longValue() == ArchiveConstants.TREE_FONDS_TYPE_NODE || parentClassId.longValue() == ArchiveConstants.TREE_FONDS_NODE)) {
			parentClassId = ArchiveConstants.TREE_ROOT_NODE_VALUE;
		}

		if (ObjectUtil.equals(ArchiveConstants.TREE_FONDS_TYPE_NODE, fondsParentId)) {
			if (archiveUtil.isAuthorityFilter()) {
				fondsCodes.addAll(archiveUtil.getAuthFondsCode());
				fondsCodes.add(FondsConstant.GLOBAL_FONDS_CODE);
			}
			List<FilingScope> filingScopeList = getFondsGroupByParentId(parentClassId, fondsCodes);
			//转成前台需要的数据格式
			if (CollectionUtil.isEmpty(filingScopeList)) {
				return Collections.emptyList();
			}
			return filingScopeList.stream().map(filingScope -> {
				FilingScopeDTO filingScopeDTO = new FilingScopeDTO();
				BeanUtil.copyProperties(filingScope, filingScopeDTO);
				filingScopeDTO.setLeaf(false);
				filingScopeDTO.setClassName(filingScope.getFondsName());
				filingScopeDTO.setParentClassId(ArchiveConstants.TREE_FONDS_TYPE_NODE);
				filingScopeDTO.setId(ArchiveConstants.TREE_ROOT_NODE_VALUE);
				filingScopeDTO.setPk(FondsConstant.FONDS_TREE_CLASS + StrUtil.DASHED + filingScope.getFondsCode());
				return filingScopeDTO;
			}).collect(Collectors.toList());
		} else if (ObjectUtil.equals(ArchiveConstants.TREE_FONDS_NODE, fondsParentId)) {
			LambdaQueryWrapper<FilingScope> queryAllItemWrapper = Wrappers.<FilingScope>query().lambda();
			if (archiveUtil.isAuthorityFilter()) {
				Set<String> authFondsCode = archiveUtil.getAuthFondsCode();
				authFondsCode.add(FondsConstant.GLOBAL_FONDS_CODE);
				queryAllItemWrapper.in(FilingScope::getFondsCode, authFondsCode);
			}
			queryAllItemWrapper.eq(FilingScope::getParentClassId, ArchiveConstants.TREE_ROOT_NODE_VALUE);
			queryAllItemWrapper.orderByAsc(FilingScope::getSortNo);
			//一次Db查询出所有的列表信息
			List<FilingScope> allFilingScopeForCurrentUserList = this.list(queryAllItemWrapper);
			return allFilingScopeForCurrentUserList.stream().map(filingScope -> {
				FilingScopeDTO filingScopeDTO = new FilingScopeDTO();
				BeanUtil.copyProperties(filingScope, filingScopeDTO);
				filingScopeDTO.setPk(filingScope.getId().toString());
				return filingScopeDTO;
			}).collect(Collectors.toList());

		} else {
			LambdaQueryWrapper<FilingScope> lambdaQueryWrapper = Wrappers.<FilingScope>query().lambda();
			if (CollectionUtil.isNotEmpty(fondsCodes)) {
				lambdaQueryWrapper.in(FilingScope::getFondsCode, fondsCodes);
			}
			lambdaQueryWrapper.eq(FilingScope::getParentClassId, parentClassId);
			lambdaQueryWrapper.orderByAsc(FilingScope::getSortNo);
			List<FilingScope> filingScopeList = this.list(lambdaQueryWrapper);
			//转成前台需要的数据格式
			if (CollectionUtil.isEmpty(filingScopeList)) {
				return Collections.emptyList();
			}
			List<FilingScopeDTO> filingScopeDTOList = filingScopeList.stream().map(filingScope -> {
				FilingScopeDTO filingScopeDTO = new FilingScopeDTO();
				BeanUtil.copyProperties(filingScope, filingScopeDTO);
				try {
					convertFilingScopeDTO(filingScopeDTO);
				} catch (ArchiveBusinessException e) {
					log.error("归档范围转换失败");
				}
				filingScopeDTO.setPk(filingScope.getId().toString());
				return filingScopeDTO;
			}).collect(Collectors.toList());
			return filingScopeDTOList;
		}
	}

	private List<FilingScope> getFondsGroupByParentId(Long parentClassId, List<String> fondsCodes) {
		return this.baseMapper.getFondsGroupByParentId(parentClassId, fondsCodes);
	}

	/**
	 * 判断 false 抛出业务异常
	 *
	 * @param expression
	 * @param message
	 */
	@SneakyThrows
	public void isTrue(boolean expression, String message) {
		if (!expression) {
			throw new ArchiveBusinessException(message);
		}
	}

	/**
	 * @return com.cescloud.saas.archive.api.modular.filingscope.dto.FilingScopeDTO
	 * @Author xieanzhu
	 * @Description //封装DTO对象 设置保管期限和档案门类中文名称
	 * @Date 11:27 2019/4/25
	 * @Param [filingScopeDTO]
	 **/
	private FilingScopeDTO convertFilingScopeDTO(FilingScopeDTO filingScopeDTO) throws ArchiveBusinessException {
		//设置是否叶子节点
		filingScopeDTO.setLeaf(isLeaf(filingScopeDTO));
		if (filingScopeDTO.getParentClassId() == 0L) {//归档范围树  直接返回即可
			return filingScopeDTO;
		}
		if (StrUtil.isNotBlank(filingScopeDTO.getTypeCode())) {
			ArchiveType archiveType = null;
			if (filingScopeDTO.getTenantId() != null) {
				archiveType = archiveTypeService.getByTypeCode(filingScopeDTO.getTypeCode(), filingScopeDTO.getTenantId());
			} else {
				archiveType = archiveTypeService.getByTypeCode(filingScopeDTO.getTypeCode());
			}
			filingScopeDTO.setTypeName(archiveType.getTypeName());
		}
		return filingScopeDTO;
	}

    /**
     * @return java.util.List<com.cescloud.saas.archive.api.modular.filingscope.dto.FilingScopeDTO>
     * @Author xieanzhu
     * @Description //删除归档范围节点包括所有子节点
     * @Date 14:38 2019/4/23
     * @Param [parentClassId]
     **/
    @Override
	@CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
	public R deleteById(Long id) throws ArchiveBusinessException {
        FilingScope filingScope = this.getById(id);

		if (filingScope.getParentClassId().longValue() == ArchiveConstants.TREE_ROOT_NODE_VALUE) {
			//是否更改了全宗信息
			if (StrUtil.equalsAnyIgnoreCase(ArchiveConstants.FONDS_GLOBAL, filingScope.getFondsCode())) {
				R<Boolean> tenantSuperAdmin = remoteRoleService.isTenantSuperAdmin();
				this.isTrue(tenantSuperAdmin != null && BooleanUtil.isTrue(tenantSuperAdmin.getData()), "租户管理员才有权限删除全局门类");
			} else {
				List<Fonds> currentFondsList = fondsService.getFondsList();
				Fonds currentFonds = currentFondsList.stream().filter(fonds -> StrUtil.equalsAnyIgnoreCase(fonds.getFondsCode(), filingScope.getFondsCode())).findFirst().orElse(null);
				this.isTrue(currentFonds != null, "目前的全宗不在您的权限范围内");
			}
		}

		//检查 范围是否被归档树 引用
		List<Long> filingScopeIdList = Lists.newArrayList();
		filingScopeIdList.add(id);
		this.getAllMainInfoForChildren(filingScope.getId(), filingScopeIdList);
		log.info("目前递归子范围id列表:{}", JSONUtil.toJsonStr(filingScopeIdList));
		List<ArchiveTree>  archiveTreeList = this.archiveTreeService.getArchiveTreeByFilingScopeOrArchiveType(null,filingScopeIdList);
		if (CollectionUtil.isNotEmpty(archiveTreeList)) {
			throw new ArchiveRuntimeException("归档范围被档案树引用，不允许删除！");
		}
        //检查有无子节点
        List<FilingScope> filingScopeList = this.list(Wrappers.<FilingScope>query().lambda()
                .select(FilingScope::getId)
                .eq(FilingScope::getParentClassId, id));
        if (CollectionUtil.isNotEmpty(filingScopeList)) {
			final List<Long> idList = filingScopeList.parallelStream().map(FilingScope::getId).collect(Collectors.toList());
            //先递归删除子孙节点
            for (Long cId: idList) {
                deleteById(cId);
            }
            //再删除子节点
            this.removeByIds(idList);
        }
        this.removeById(filingScope.getId());
        return new R().success(null, "删除归档范围成功！");
	}

	/**
	 * @return boolean
	 * @Author xieanzhu
	 * @Description //判断是否叶子节点
	 * @Date 10:57 2019/4/26
	 * @Param [filingScopeDTO]
	 **/
	private boolean isLeaf(FilingScopeDTO filingScopeDTO) {
		if (log.isDebugEnabled()) {
			log.debug("判断分类名称为{}，分类号为{}的分类节点信息是否为叶子节点", filingScopeDTO.getClassName(), filingScopeDTO.getClassNo());
		}
		int count = this.count(Wrappers.<FilingScope>query().lambda()
				.eq(FilingScope::getParentClassId, filingScopeDTO.getId()));
		return count == 0;
	}


	@Override
	public void exportExcel(Long id, HttpServletResponse response) throws ArchiveBusinessException {
		FilingScope filingScope = this.getById(id);
		if (ObjectUtil.isNull(filingScope)) {
			log.error("归档范围分类节点不存在，节点ID为{}", id);
			throw new ArchiveBusinessException("归档范围分类节点不存在，节点ID为" + id + "！");
		}
		String path = "templatefile/filingScopeTemplate.xls";
		Resource resource = resourceLoader.getResource("classpath:" + path);
		try (OutputStream out = response.getOutputStream();
		     InputStream fileInputStream = resource.getInputStream();
		     POIFSFileSystem poifsFileSystem = new POIFSFileSystem(fileInputStream);
		     HSSFWorkbook sheets = new HSSFWorkbook(poifsFileSystem)) {
			String encodeName = URLEncoder.encode("归档范围信息", StandardCharsets.UTF_8.toString());
			response.setContentType("application/vnd.ms-excel");
			response.setCharacterEncoding("utf-8");
			response.setHeader("Content-Disposition", "attachment;filename=" + encodeName + ".xls");
			//加载模板
			HSSFSheet sheetAt = sheets.getSheetAt(0);
			List<List<String>> excelData = getExcelData(filingScope);
			List<List<String>> excelHead = getExcelHead();
			//起始位置
			int count = 3;
			for (int j = 0, excelDataSize = excelData.size(); j < excelDataSize; j++) {
				List<String> excelDatum = excelData.get(j);
				HSSFRow row = sheetAt.createRow(count);
				for (int i = 0; i < excelHead.size(); i++) {
					row.createCell(i).setCellValue(excelDatum.get(i));

				}
				count++;
			}
			out.flush();
			sheets.write(out);
		} catch (IOException e) {
			log.error("导出归档范围信息失败！", e);
			throw new ArchiveBusinessException("导出归档范围信息失败");
		}
	}


	/**
	 * 获取导出excel 头
	 */
	private List<List<String>> getExcelHead() {
		final List<List<String>> head = CollectionUtil.<List<String>>newArrayList();
		Arrays.stream(HEADERS).forEach(header -> {
			final List<String> headColumn = CollectionUtil.<String>newArrayList();
			headColumn.add(header);
			head.add(headColumn);
		});
		return head;
	}


	/**
	 * 获取excel导出数据
	 *
	 * @param filingScope 归档树节点
	 * @return excel导出数据
	 */
	private List<List<String>> getExcelData(FilingScope filingScope) {
		List<FilingScopeDTO> filingScopeList = baseMapper.getAllFilingScopeList(filingScope.getFondsCode(), filingScope.getTenantId());

		final List<List<String>> data = CollectionUtil.<List<String>>newArrayList();
		List<FilingScopeDTO> list = CollUtil.newArrayList();
		filterList(filingScope.getId(), filingScopeList, list);
		list.forEach(e -> {
			final List<String> item = CollectionUtil.<String>newArrayList();
			item.add(InitializeUtil.toString(e.getParentClassName()));
			item.add(InitializeUtil.toString(e.getClassName()));
			item.add(InitializeUtil.toString(e.getClassNo()));
			item.add(InitializeUtil.toString(e.getTypeCode()));
			data.add(item);
		});
		return data;
	}

	private void filterList(Long id, List<FilingScopeDTO> filingScopeList, List<FilingScopeDTO> list) {
		List<FilingScopeDTO> childrenList = filingScopeList.stream().filter(e -> id.equals(e.getParentClassId())).collect(Collectors.toList());
		if (CollUtil.isNotEmpty(childrenList)) {
			list.addAll(childrenList);
			childrenList.forEach(e -> {
				filterList(e.getId(), filingScopeList, list);
			});
		}
	}


	@Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public Boolean importExcel(MultipartFile file, Long id) throws ArchiveBusinessException {
		FilingScope filingScope = this.getById(id);
		if (ObjectUtil.isNull(filingScope)) {
			log.error("归档范围分类节点不存在，节点ID为{}", id);
			throw new ArchiveBusinessException("归档范围分类节点不存在，节点ID为" + id + "！");
		}
		if (StrUtil.equalsAnyIgnoreCase(ArchiveConstants.FONDS_GLOBAL, filingScope.getFondsCode())) {
			R<Boolean> tenantSuperAdmin = remoteRoleService.isTenantSuperAdmin();
			this.isTrue(tenantSuperAdmin != null && BooleanUtil.isTrue(tenantSuperAdmin.getData()), "租户管理员才能够导入全局范围树分类节点");
		} else {
			List<Fonds> currentFondsList = fondsService.getFondsList(SecurityUtils.getUser().getId());
			Fonds currentFonds = currentFondsList.stream().filter(fonds -> StrUtil.equalsAnyIgnoreCase(fonds.getFondsCode(), filingScope.getFondsCode())).findFirst().orElse(null);
			this.isTrue(currentFonds != null, "目前的全宗不在您的权限范围内");
		}
		//获取该树根节点下的所有子节点
		//获取档案门类
		Map<String, String> archiveTypeMap = archiveTypeService.list(Wrappers.<ArchiveType>lambdaQuery()
						.select(ArchiveType::getTypeCode, ArchiveType::getTypeName))
				.stream().collect(Collectors.toMap(ArchiveType::getTypeName, ArchiveType::getTypeCode));
		List<FilingScope> allChildrenFilingScopeList = CollUtil.newArrayList();
		List<FilingScope> list = this.list(Wrappers.<FilingScope>lambdaQuery().eq(FilingScope::getParentClassId, filingScope.getId())
				.select(FilingScope::getId, FilingScope::getClassNo, FilingScope::getPath, FilingScope::getTypeCode, FilingScope::getClassName));
		getAllChildrenFilingScope(list, allChildrenFilingScopeList);
		allChildrenFilingScopeList.add(filingScope);
		try (InputStream inputStream = file.getInputStream()) {
			FilingScopeImportExcelListener excelListener = new FilingScopeImportExcelListener(file, allChildrenFilingScopeList, archiveTypeMap, pushFileOpenService, this);
			EasyExcel.read(inputStream, FilingScopeExcelDTO.class, excelListener).headRowNumber(3).sheet().doRead();
			return ObjectUtil.isEmpty(excelListener.filingScopeExcelErrors);
		} catch (IOException e) {
			throw new ArchiveBusinessException("归档范围信息导入异常", e);
		}
	}

	@Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public R initializeHandle(Long templateId, Long tenantId) throws ArchiveBusinessException {
		cn.hutool.poi.excel.ExcelReader excel = null;
		try {
			InputStream inputStream = getDefaultTemplateStream(templateId);
			if (ObjectUtil.isNull(inputStream)) {
				return new R<>().fail("", "获取初始化文件异常");
			}
			excel = new cn.hutool.poi.excel.ExcelReader(inputStream, TemplateFieldConstants.SHEET_NAMES.FILING_SCOPE_NAME);
			List<List<Object>> read = excel.read();
			//对表单表头校验
			Map<Integer, String> map = InitializeUtil.checkHeader(TemplateFieldConstants.FILING_SCOPE_LIST, read.get(0));
			if (CollectionUtils.isEmpty(map)) {
				return new R<>().fail("", "模板表列数据不匹配！！！");
			}
			//新增数据
			insertData(map, read, tenantId);
			filingScopeTypeService.initializeFilingScopeTypeHandle(templateId, tenantId);
			return new R("", "成功");
		} finally {
			IoUtil.close(excel);
		}
	}


	@Override
	public List<ArrayList<String>> getFilingRangeTreeNodeInfo(Long tenantId) {
		//获取归档范围树节点
		final List<FilingScope> filingScopes = this.list(Wrappers.<FilingScope>lambdaQuery().eq(FilingScope::getTenantId, tenantId).orderByAsc(FilingScope::getSortNo));
		//获取档案类型
		final List<ArchiveType> archiveTypes = archiveTypeService.list(Wrappers.<ArchiveType>query().lambda().eq(ArchiveType::getNodeType, NodeTypeEnum.DATA.getValue()).eq(ArchiveType::getTenantId, tenantId));
		final Map<String, String> archives = archiveTypes.stream().collect(Collectors.toMap(ArchiveType::getTypeCode, ArchiveType::getTypeName));
		//过滤根节点信息
		final Map<Long, String> rootNode = filingScopes.stream().collect(Collectors.toMap(FilingScope::getId, FilingScope::getClassName));
		List<ArrayList<String>> lists = new ArrayList<>();
		//  上级节点	类型	名称	分类号	保管期限	关联档案门类	处置方式
		filingScopes.forEach(filingScope -> {
			//BoolEnum.NO.getCode() 代表根节点
			if (Long.valueOf(BoolEnum.NO.getCode()).equals(filingScope.getParentClassId())) {
				//树 节点
				//上级节点 类型	名称	  分类号	  关联档案门类	全宗号   全宗名称  顺序号
				// not     树   名称  分类号        not        G        全局     1
				lists.add(CollectionUtil.newArrayList(TemplateFieldConstants.NOT, TemplateFieldConstants.FILING_SCOPE.TREE, filingScope.getClassName(),
						TemplateFieldConstants.NOT, TemplateFieldConstants.NOT, filingScope.getFondsCode(), filingScope.getFondsName(), InitializeUtil.toString(filingScope.getSortNo())));
			} else {
				//节点信息
				//上级节点	类型	名称	分类号			 关联档案门类
				lists.add(CollectionUtil.newArrayList(rootNode.get(filingScope.getParentClassId()), TemplateFieldConstants.FILING_SCOPE.CLASSIFY,
						filingScope.getClassName(), filingScope.getClassNo(), archives.get(filingScope.getTypeCode()), filingScope.getFondsCode(),
						filingScope.getFondsName(), InitializeUtil.toString(filingScope.getSortNo())));
			}
		});
		return lists;
	}

	/***
	 * 根据档案类型分类号查询归档范围
	 * @param typeCode 档案类型
	 * @param classNo 分类号
	 * @return
	 */
	@Override
	@Cacheable(key = "'archive-app-management:filing-scope:' + #typeCode + ':' + #classNo",
			unless = "#result == null"
	)
	public FilingScope getFilingScopeByTypeCodeClassNo(String typeCode, String classNo) {
		return this.getOne(Wrappers.<FilingScope>lambdaQuery().eq(FilingScope::getTypeCode, typeCode).eq(FilingScope::getClassNo, classNo), false);
	}

	/**
	 * 根据分类号查询归档范围
	 * @param classNo 分类号
	 * @return
	 */
	@Override
	public FilingScope getFilingScopeByClassNo(String classNo) {
		return this.getOne(Wrappers.<FilingScope>lambdaQuery().eq(FilingScope::getClassNo, classNo), false);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public FilingScopeDTO copyFilingScope(FilingScopeCopyPostDTO filingScopeCopyPostDTO) throws ArchiveBusinessException {
		this.isTrue(StrUtil.equalsAnyIgnoreCase(filingScopeCopyPostDTO.getSourceFondsCode(), ArchiveConstants.FONDS_GLOBAL), "该归档范围树的门类不是全局公共门类！");
		this.isTrue(StrUtil.isNotBlank(filingScopeCopyPostDTO.getTargetClassName()), "目标范围树名字不能为空！");
		this.isTrue(StrUtil.isNotBlank(filingScopeCopyPostDTO.getTargetFondsCode()), "目标全宗没有选择！");
		LambdaQueryWrapper<FilingScope> lambdaQueryWrapper = Wrappers.<FilingScope>query().lambda();
		if (StrUtil.isBlank(filingScopeCopyPostDTO.getTargetFondsName())) {
			List<Fonds> currentFondsList = fondsService.getFondsList();
			Fonds currentFonds = currentFondsList.stream().filter(fonds -> StrUtil.equalsAnyIgnoreCase(fonds.getFondsCode(), filingScopeCopyPostDTO.getTargetFondsCode())).findFirst().orElse(null);
			if (filingScopeCopyPostDTO.getTargetFondsCode().equals(ArchiveConstants.FONDS_GLOBAL) && remoteRoleService.isTenantSuperAdmin().getData()) {
				filingScopeCopyPostDTO.setTargetFondsName(filingScopeCopyPostDTO.getFondsName());
			} else if (currentFonds != null) {
				filingScopeCopyPostDTO.setTargetFondsName(currentFonds.getFondsName());
			} else {
				this.isTrue(false, "只有租户管理员才能复制到全局分类");
			}
		}
		if (CollectionUtil.isNotEmpty(filingScopeCopyPostDTO.getFondsCodes())) {
			lambdaQueryWrapper.in(FilingScope::getFondsCode, filingScopeCopyPostDTO.getFondsCodes());
		}
		lambdaQueryWrapper.orderByAsc(FilingScope::getSortNo);
		//查询到目前归档范围树
		List<FilingScope> filingScopeAllList = this.list(lambdaQueryWrapper);
		FilingScope searchFilingScope = filingScopeAllList.stream().filter(t -> StrUtil.equalsAnyIgnoreCase(t.getClassName(), filingScopeCopyPostDTO.getTargetClassName())
				&& t.getParentClassId().longValue() == ArchiveConstants.TREE_ROOT_NODE_VALUE
				&& StrUtil.equalsAnyIgnoreCase(t.getFondsCode(), filingScopeCopyPostDTO.getTargetFondsCode())).findFirst().orElse(null);
		this.isTrue(searchFilingScope == null, "该目标档案范围树已经存在，无法进行批量复制！");
		//考虑添加全宗
		CesCloudUser cesCloudUser = SecurityUtils.getUser();
		LocalDateTime createdTime = LocalDateTime.now();
		filingScopeAllList.forEach(item -> {
			item.setFondsCode(filingScopeCopyPostDTO.getTargetFondsCode());
			item.setFondsName(filingScopeCopyPostDTO.getTargetFondsName());
			item.setCreatedUserName(cesCloudUser.getChineseName());
			item.setCreatedBy(cesCloudUser.getId());
			item.setUpdatedBy(null);
			item.setUpdatedTime(null);
			item.setCreatedTime(createdTime);
			item.setRevision(1L);
		});
		List<FilingScope> rootFilingScope = filingScopeAllList.stream().filter(t -> t.getId().longValue() == filingScopeCopyPostDTO.getSourceId().longValue()).collect(Collectors.toList());
		rootFilingScope.forEach(root -> {
			//树根的名字进行了修改
			root.setClassName(filingScopeCopyPostDTO.getTargetClassName());
			root.setFondsCode(filingScopeCopyPostDTO.getTargetFondsCode());
			root.setFondsName(filingScopeCopyPostDTO.getTargetFondsName());
		});
		copyFilingScope(rootFilingScope, filingScopeAllList);
		return null;
	}

	/**
	 * 获取列表主要信息 列表
	 *
	 * @param parentClassId
	 * @param result
	 */
	@Override
	public List<Long> getAllMainInfoForChildren(Long parentClassId, List<Long> result) {
		List<FilingScope> children = this.list(Wrappers.<FilingScope>lambdaQuery().eq(FilingScope::getParentClassId, parentClassId));
		List<Long> idList = children.stream().map(FilingScope::getId).distinct().collect(Collectors.toList());
		idList.forEach(id -> {
			if (!result.contains(id)) {
				result.add(id);
			}
		});
		if (CollectionUtil.isNotEmpty(children)) {
			for (FilingScope filingScope : children) {
				getAllMainInfoForChildren(filingScope.getId(), result);
			}
		}
		return result;
	}

	@Override
	public List<FilingScope> getFilingScopeByArchiveType(List<String> archiveTypeCodeList) {
		return this.list(Wrappers.<FilingScope>lambdaQuery().in(FilingScope::getTypeCode, archiveTypeCodeList));
	}

	@Override
	@CacheEvict(allEntries = true)
	public Boolean filingScopeOrder(FilingScopeOrderDTO filingScopeOrderDTO) {
		final List<Long> ids = filingScopeOrderDTO.getIds();
		List<FilingScope> filingScopes = this.list(Wrappers.<FilingScope>lambdaQuery().in(FilingScope::getId, ids));
		final List<FilingScope> list = IntStream.rangeClosed(1, ids.size()).mapToObj(i -> {
			final FilingScope filingScope = filingScopes.parallelStream().filter(e -> e.getId().equals(ids.get(i - 1))).findAny().get();
			filingScope.setSortNo(i);
			return filingScope;
		}).collect(Collectors.toList());
		return this.updateBatchById(list);
	}

	@Override
	public List<FilingScope> getFilingScopeByParentId(ArchiveTreeQueryDTO archiveTreeQueryDTO) {
		List<FilingScope> result = CollectionUtil.newArrayList();
		Long parentId = archiveTreeQueryDTO.getParentId();
		final String typeCode = archiveTreeQueryDTO.getTypeCode();
		LambdaQueryWrapper<FilingScope> wrapper = new LambdaQueryWrapper<>();
		if (!ArchiveConstants.TREE_ROOT_NODE_VALUE.equals(parentId) && StrUtil.isNotBlank(typeCode)) {
			wrapper.eq(FilingScope::getTypeCode, typeCode);
			List<FilingScope> allBindTypeCode = this.list(wrapper);
			Set<Long> parentIds = CollectionUtil.newHashSet();
			allBindTypeCode.forEach(scope ->{
				if(scope.getParentClassId().equals(parentId)){
					result.add(scope);
				}else{
					FilingScope parent = getParentFilingScope(scope,parentId);
					if(ObjectUtil.isNotNull(parent)){
						parentIds.add(parent.getId());
					}
				}
			});
			if(parentIds.size()>0){
				LambdaQueryWrapper<FilingScope> wrapper1 = new LambdaQueryWrapper<>();
				wrapper1.in(FilingScope::getId,parentIds);
				result.addAll(this.list(wrapper1));
			}
		}else{
			wrapper.eq(FilingScope::getParentClassId, parentId);
			result.addAll(this.list(wrapper));
		}
		// 去重
		return result;
	}

	private FilingScope getParentFilingScope(FilingScope scope,Long parentId){
		LambdaQueryWrapper<FilingScope> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(FilingScope::getId,scope.getParentClassId());
		FilingScope filingScope = this.getOne(wrapper);
		if(filingScope.getParentClassId().equals(ArchiveConstants.TREE_ROOT_NODE_VALUE)){
			return null;
		}
		if(filingScope.getParentClassId().equals(parentId)){
			return filingScope;
		}
		return getParentFilingScope(filingScope,parentId);
	}

	@Override
	public List<FilingScope> getFilingScope(FilingScopeDTO filingScopeDTO) {
		String typeCode = filingScopeDTO.getTypeCode();
		String classNo = filingScopeDTO.getClassNo();
		String fondsCode = filingScopeDTO.getFondsCode();
		String path = filingScopeDTO.getPath();
		LambdaQueryWrapper<FilingScope> wrapper = Wrappers.lambdaQuery();
		if (StrUtil.isNotBlank(typeCode)) {
			wrapper.eq(FilingScope::getTypeCode, typeCode);
		}
		if (StrUtil.isNotBlank(classNo)) {
			wrapper.eq(FilingScope::getClassNo, classNo);
		}
		if (StrUtil.isNotBlank(fondsCode)) {
			wrapper.eq(FilingScope::getFondsCode, fondsCode);
		}
		if (StrUtil.isNotBlank(path)) {
			wrapper.eq(FilingScope::getPath, path);
		}
		return this.list(wrapper);
	}

	@Override
	public void downloadExcelTemplate(HttpServletResponse response) throws ArchiveBusinessException {
		InputStream inputStream = null;
		ServletOutputStream servletOutputStream = null;
		try {
			String path = "templatefile/filingScopeTemplate.xls";
			Resource resource = resourceLoader.getResource("classpath:" + path);
			response.setContentType("application/vnd.ms-excel");
			response.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
			response.addHeader("charset", "utf-8");
			response.addHeader("Pragma", "no-cache");
			String encodeName = URLEncoder.encode("归档范围信息导入模板", StandardCharsets.UTF_8.toString());
			response.setHeader("Content-Disposition", "attachment;filename=" + encodeName + ".xls");
			inputStream = resource.getInputStream();
			servletOutputStream = response.getOutputStream();
			IOUtils.copy(inputStream, servletOutputStream);
			response.flushBuffer();
		} catch (IOException e) {
			log.error("导出归档范围信息模板失败！", e);
			throw new ArchiveBusinessException("导出归档范围信息模板失败");
		} finally {
			IoUtil.close(servletOutputStream);
			IoUtil.close(inputStream);
		}
	}

	private void copyFilingScope(List<FilingScope> rootFilingScope, List<FilingScope> allFilingScopeList) {
		rootFilingScope.forEach(root -> {
			log.info("目前id 是：{} 父id:{}", root.getId(), root.getParentClassId());
			FilingScope filingScopeNewCopy = new FilingScope();
			BeanUtil.copyProperties(root, filingScopeNewCopy);
			Long id = filingScopeNewCopy.getId();
			filingScopeNewCopy.setId(null);
			this.baseMapper.insert(filingScopeNewCopy);
			LambdaQueryWrapper<FilingScopeType> queryWrapper = Wrappers.lambdaQuery();
			queryWrapper.eq(FilingScopeType::getParentId, id);
			List<FilingScopeType> list = filingScopeTypeService.list(queryWrapper);//找出对应归档范围信息
			if (list.size() > 0) {
				list.stream().forEach(filingScopeType -> {
					filingScopeType.setId(null);
					filingScopeType.setParentId(filingScopeNewCopy.getId());
					filingScopeTypeService.save(filingScopeType);
				});
			}
			log.info("目前新增的id 是：{}", filingScopeNewCopy.getId());
			List<FilingScope> childFilingScope = allFilingScopeList.stream().filter(child -> child.getParentClassId().longValue() == root.getId()).collect(Collectors.toList());
			childFilingScope.forEach(t -> {
				t.setFondsCode(root.getFondsCode());
				t.setFondsName(root.getFondsName());
				t.setParentClassId(filingScopeNewCopy.getId());
			});
			copyFilingScope(childFilingScope, allFilingScopeList);
		});
	}


	/**
	 * 新增初始化数据操作
	 *
	 * @param map      处理好的数据集
	 * @param read     excel 数据集
	 * @param tenantId 租户ID
	 * @return 成功状态
	 */
	private Boolean insertData(Map<Integer, String> map, List<List<Object>> read, Long tenantId) {
		//获取保管期限
		final List<DictItem> dictItems = dictItemService.list(Wrappers.<DictItem>query().lambda().eq(DictItem::getDictCode, DictEnum.BGQX.getValue()).eq(DictItem::getTenantId, tenantId));
		//获取档案类型
		final List<ArchiveType> archiveTypes = archiveTypeService.list(Wrappers.<ArchiveType>query().lambda().eq(ArchiveType::getNodeType, NodeTypeEnum.DATA.getValue()).eq(ArchiveType::getTenantId, tenantId));
		//将档案类型转化为  typeName ---> typeCode  的 map
		final Map<String, String> archiveTypeNameCodeMap = archiveTypes.parallelStream().collect(Collectors.toMap(ArchiveType::getTypeName, ArchiveType::getTypeCode));
		//数据转换
		final List<Map<String, String>> excelData = CollectionUtil.<Map<String, String>>newArrayList();
		for (int i = 1, length = read.size(); i < length; i++) {
			//数据处理
			Map<String, String> dataTreating = InitializeUtil.dataTreating(map, TemplateFieldConstants.FILING_SCOPE_LIST, read.get(i));
			if (CollectionUtils.isEmpty(dataTreating)) {
				continue;
			}
			excelData.add(dataTreating);
		}
		//最顶级的节点，即节点名称是公共归档范围树
		Map<String, String> topNode = excelData.stream().filter(data -> StrUtil.equals(data.get(TemplateFieldConstants.FILING_SCOPE.TYPE), TemplateFieldConstants.FILING_SCOPE.TREE)).findAny().get();
		String className = topNode.get(TemplateFieldConstants.FILING_SCOPE.CLASSNAME);
		String fondsCode = topNode.get(TemplateFieldConstants.FILING_SCOPE.FONDS_CODE);
		String fondsName = topNode.get(TemplateFieldConstants.FILING_SCOPE.FONDS_NAME);
		String sortNo = topNode.get(TemplateFieldConstants.FILING_SCOPE.SORT_NO);
		final FilingScopeExcelTree treeNode = new FilingScopeExcelTree();
		treeNode.setTenantId(tenantId);
		treeNode.setParentClassId(0L);
		treeNode.setClassName(className);
		treeNode.setFondsCode(fondsCode);
		treeNode.setFondsName(fondsName);
		treeNode.setSortNo(Integer.parseInt(sortNo));
		convertExcelTreeNode(treeNode, excelData, dictItems, archiveTypeNameCodeMap);
		//批量保存同一层次的节点数据,先保存顶层节点
		//先保存顶层节点生成id
		final FilingScope filingScope = new FilingScope();
		BeanUtil.copyProperties(treeNode, filingScope);
		this.save(filingScope);
		treeNode.setId(filingScope.getId());
		//递归批量保存
		saveExcelBatch(treeNode);
		return Boolean.TRUE;
	}

	/**
	 * 递归生成树节点
	 *
	 * @param treeNode
	 * @param excelData
	 */
	private void convertExcelTreeNode(final FilingScopeExcelTree treeNode, final List<Map<String, String>> excelData,
	                                  final List<DictItem> dictItems, final Map<String, String> archiveTypeNameCodeMap) {
		if (ObjectUtil.isNull(treeNode)) {
			return;
		}
		//归档范围的节点名称
		final String parentClassName = treeNode.getClassName();
		final String parentPath = treeNode.getPath();
		final List<FilingScopeExcelTree> children = excelData.stream().filter(data -> {
			String excelParentClassName = data.get(TemplateFieldConstants.FILING_SCOPE.PARENT_CLASS_ID);
			if (StrUtil.equals(parentClassName, excelParentClassName)) {
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		}).map(data -> {
			//获取档案门类编码
			String typeCode = Optional.ofNullable(data.get(TemplateFieldConstants.FILING_SCOPE.TYPE_CODE)).map(name -> archiveTypeNameCodeMap.get(name)).orElse(null);
			// 实例化树节点
			FilingScopeExcelTree childrenNode = new FilingScopeExcelTree();
			childrenNode.setClassName(data.get(TemplateFieldConstants.FILING_SCOPE.CLASSNAME));
			String classNo = data.get(TemplateFieldConstants.FILING_SCOPE.CLASSNO);
			String fondsCode = data.get(TemplateFieldConstants.FILING_SCOPE.FONDS_CODE);
			String fondsName = data.get(TemplateFieldConstants.FILING_SCOPE.FONDS_NAME);
			String sortNo = data.get(TemplateFieldConstants.FILING_SCOPE.SORT_NO);
			childrenNode.setClassNo(classNo);
			childrenNode.setFondsCode(fondsCode);
			childrenNode.setFondsName(fondsName);
			childrenNode.setSortNo(Integer.parseInt(sortNo));
			childrenNode.setPath(StrUtil.isBlank(parentPath) ? (classNo) : (parentPath + "," + classNo));
			if (ObjectUtil.isNotNull(typeCode)) {
				childrenNode.setTypeCode(typeCode);
			}
			return childrenNode;
		}).collect(Collectors.toList());
		//递归
		if (CollectionUtil.isNotEmpty(children)) {
			treeNode.setLeaf(false);
			treeNode.getChildren().addAll(children);
			//递归生成树节点
			children.forEach(child -> {
				convertExcelTreeNode(child, excelData, dictItems, archiveTypeNameCodeMap);
			});
		} else {
			treeNode.setLeaf(true);
		}
	}

	/**
	 * 递归保存
	 *
	 * @param treeNode
	 */
	private void saveExcelBatch(final FilingScopeExcelTree treeNode) {
		final List<FilingScopeExcelTree> children = treeNode.getChildren();
		if (CollectionUtil.isNotEmpty(children)) {
			// 类型转化
			final List<FilingScope> list = children.stream().map(node -> {
				final FilingScope filingScope = new FilingScope();
				BeanUtil.copyProperties(node, filingScope);
				filingScope.setTenantId(treeNode.getTenantId());
				filingScope.setParentClassId(treeNode.getId());
				return filingScope;
			}).collect(Collectors.toList());
			//批量保存
			this.saveBatch(list);
			//递归保存
			children.forEach(child -> {
				FilingScope scope = list.stream().filter(filingScope -> StrUtil.equals(filingScope.getClassName(), child.getClassName())).findAny().get();
				child.setId(scope.getId());
				child.setTenantId(scope.getTenantId());
				saveExcelBatch(child);
			});
		}
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

	/**
	 * @return void
	 * @Author xieanzhu
	 * @Description //读取excel数据插入到数据库
	 * @Date 10:07 2019/4/24
	 * @Param [dataList]
	 **/
	private void insertExcelData(List<Object> dataList) {
		final List<FilingScope> filingScopeList = dataList.parallelStream().map(obj -> {
			List<String> data = (List<String>) obj;
			FilingScope filingScope = new FilingScope();
			filingScope.setClassNo(data.get(0));
			filingScope.setClassName(data.get(1));
			/*filingScope.setFilingScope(data.get(2));
			filingScope.setRetentionPeriod(data.get(3));*/
			return filingScope;
		}).collect(Collectors.toList());
		this.saveBatch(filingScopeList);
	}

	@Override
	public void updateArchiveFilingScopeTree(String fondsName, String fondsCode) {
		this.getBaseMapper().updateArchiveFilingScopeTree(fondsName, fondsCode);
	}
}
