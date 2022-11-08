
package com.cescloud.saas.archive.service.modular.archiveconfig.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedColumnRuleMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveLinkColumnRuleMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.LinkColumnRule;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.LinkLayer;
import com.cescloud.saas.archive.api.modular.archivedict.dto.CopyPostDTO;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.filecenter.entity.DirStorage;
import com.cescloud.saas.archive.api.modular.filecenter.feign.RemoteFileCenterService;
import com.cescloud.saas.archive.api.modular.storage.constants.StorageConstants;
import com.cescloud.saas.archive.common.constants.ArchiveConstants;
import com.cescloud.saas.archive.common.constants.ArchiveLayerEnum;
import com.cescloud.saas.archive.common.constants.FileLinkEnum;
import com.cescloud.saas.archive.common.constants.TypedefEnum;
import com.cescloud.saas.archive.common.util.TreeUtil;
import com.cescloud.saas.archive.service.modular.archiveconfig.dto.LinkLayerTreeNode;
import com.cescloud.saas.archive.service.modular.archiveconfig.mapper.LinkLayerMapper;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveConfigManageService;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.LinkColumnRuleService;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.LinkLayerService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import com.cescloud.saas.archive.service.modular.common.security.tenantfilter.TenantContextHolder;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.filecenter.service.DirStorageOpenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 挂接目录规则配置
 *
 * @author liudong1
 * @date 2019-05-14 10:33:56
 */
@Service
@Slf4j
@RequiredArgsConstructor
@CacheConfig(cacheNames = "link-layer")
public class LinkLayerServiceImpl extends ServiceImpl<LinkLayerMapper, LinkLayer> implements LinkLayerService {

	@Autowired
	private LinkColumnRuleService linkColumnRuleService;

	@Autowired
	private ArchiveTableService archiveTableService;

	@Autowired
	private ArchiveConfigManageService archiveConfigManageService;

	//构造注入
	private final RemoteFileCenterService remoteFileCenterService;

	@Autowired
	private DirStorageOpenService dirStorageOpenService;


	@Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public LinkLayer saveRoot(LinkLayer linkLayer) throws ArchiveBusinessException {
		Long tenantId = SecurityUtils.getUser().getTenantId();
		if (ObjectUtil.isNull(linkLayer.getRelationId())) {
			log.error("被关联的文件中心文件夹ID为空，无法保存！");
			throw new ArchiveBusinessException("被关联的文件中心文件夹ID为空，无法保存！");
		}

		R<String> rDirName = remoteFileCenterService.getDirName(tenantId, linkLayer.getRelationId());
		if (rDirName.getCode() == CommonConstants.FAIL) {
			log.error("远程调用获取文件夹名失败");
			linkLayer.setName("根文件夹");
		} else {
			linkLayer.setName(rDirName.getData());
		}

		linkLayer.setParentId(-1L);
		linkLayer.setParentIds("-1");
		linkLayer.setIsDir(FileLinkEnum.DIR.getCode());
		this.saveOrUpdate(linkLayer);
		archiveConfigManageService.save(linkLayer.getStorageLocate(), linkLayer.getModuleId(), TypedefEnum.DOCUMENT.getValue());
		return linkLayer;
	}

	@Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public R saveDirLinkColumnRule(SaveLinkColumnRuleMetadata saveLinkColumnRuleMetadata) throws ArchiveBusinessException {
		LinkLayer linkLayer = saveLinkColumnRule(saveLinkColumnRuleMetadata, FileLinkEnum.DIR);
		return new R<>(linkLayer);
	}

	@Override
	public LinkLayer getRoot(String typeCode, Long templateTableId, Long moduleId) throws ArchiveBusinessException {
		ArchiveTable archiveTable = archiveTableService.getTableByArchiveTypeCodeAndTemplateTableId(typeCode, templateTableId);
		if (ObjectUtil.isNull(archiveTable)) {
			log.error("未找到档案类型编码为[{}],模板ID为[{}]的档案类型", typeCode, templateTableId);
			throw new ArchiveBusinessException("未找到档案类型编码为" + typeCode + ", 模板ID为" + templateTableId + "的档案类型");
		}
		return getRoot(archiveTable.getStorageLocate(), moduleId);
	}

	@Override
	public LinkLayer getBusinessRoot(String typeCode, Long templateTableId, Long moduleId) throws ArchiveBusinessException {
		ArchiveTable archiveTable = archiveTableService.getTableByArchiveTypeCodeAndTemplateTableId(typeCode, templateTableId);
		if (ObjectUtil.isNull(archiveTable)) {
			log.error("未找到档案类型编码为[{}],模板ID为[{}]的档案类型", typeCode, templateTableId);
			throw new ArchiveBusinessException("未找到档案类型编码为" + typeCode + ", 模板ID为" + templateTableId + "的档案类型");
		}
		LinkLayer root = getRoot(archiveTable.getStorageLocate(), moduleId);
		if(ObjectUtil.isNull(root)){
			root = getRoot(archiveTable.getStorageLocate(), ArchiveConstants.PUBLIC_MODULE_FLAG);
		}
		return root;
	}


	private LinkLayer getRoot(String storageLocate, Long moduleId) {
		LinkLayer linkLayer = this.getOne(Wrappers.<LinkLayer>query().lambda()
				.eq(LinkLayer::getStorageLocate, storageLocate)
				.eq(LinkLayer::getParentId, -1L).eq(LinkLayer::getModuleId, moduleId));
		return linkLayer;
	}

	@Override
	public List<LinkLayer> getRootsByTenantId(Long tenantId) {
		List<LinkLayer> list = this.list(Wrappers.<LinkLayer>query().lambda()
				.eq(LinkLayer::getTenantId, tenantId)
				.eq(LinkLayer::getParentId, -1L));
		return list;
	}

	@Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public R saveFileLinkColumnRule(SaveLinkColumnRuleMetadata saveLinkColumnRuleMetadata) {
		//删除现有文件名规则
		this.remove(Wrappers.<LinkLayer>query().lambda()
				.eq(LinkLayer::getStorageLocate, saveLinkColumnRuleMetadata.getStorageLocate())
				.eq(LinkLayer::getIsDir, FileLinkEnum.FILE.getCode()).eq(LinkLayer::getModuleId, saveLinkColumnRuleMetadata.getModuleId()));

		LinkLayer linkLayer = new LinkLayer();

		if (CollectionUtil.isNotEmpty(saveLinkColumnRuleMetadata.getData())) {
			linkLayer.setStorageLocate(saveLinkColumnRuleMetadata.getStorageLocate());
			linkLayer.setIsDir(FileLinkEnum.FILE.getCode());
			linkLayer.setModuleId(saveLinkColumnRuleMetadata.getModuleId());
			this.save(linkLayer);

			saveLinkColumnRuleMetadata.setId(linkLayer.getId());
			//保存挂接字段组成规则
			linkColumnRuleService.saveFileLinkColumnRule(saveLinkColumnRuleMetadata);

			String linkLayerName = linkColumnRuleService.getLinkLayerName(saveLinkColumnRuleMetadata.getStorageLocate(), saveLinkColumnRuleMetadata.getId());
			linkLayer.setName(linkLayerName);
			this.updateById(linkLayer);
		}
		archiveConfigManageService.save(linkLayer.getStorageLocate(), linkLayer.getModuleId(), TypedefEnum.DOCUMENT.getValue());
		return new R<>(linkLayer);
	}

	@Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public boolean saveDocLinkColumnRule(SaveLinkColumnRuleMetadata saveLinkColumnRuleMetadata) {
		// 删除现有的保存文件下载命名设置规则
		this.remove(Wrappers.<LinkLayer>query().lambda()
				.eq(LinkLayer::getStorageLocate, saveLinkColumnRuleMetadata.getStorageLocate())
				.eq(LinkLayer::getIsDir, FileLinkEnum.DOWNLOAD.getCode()).eq(LinkLayer::getModuleId, saveLinkColumnRuleMetadata.getModuleId()));
		LinkLayer linkLayer = new LinkLayer();
		if (CollectionUtil.isNotEmpty(saveLinkColumnRuleMetadata.getData())) {
			linkLayer.setStorageLocate(saveLinkColumnRuleMetadata.getStorageLocate());
			linkLayer.setIsDir(FileLinkEnum.DOWNLOAD.getCode());
			linkLayer.setModuleId(saveLinkColumnRuleMetadata.getModuleId());
			this.save(linkLayer);

			saveLinkColumnRuleMetadata.setId(linkLayer.getId());
			//保存文件下载命名设置规则
			linkColumnRuleService.saveFileLinkColumnRule(saveLinkColumnRuleMetadata);

			String linkLayerName = linkColumnRuleService.getLinkLayerName(saveLinkColumnRuleMetadata.getStorageLocate(), saveLinkColumnRuleMetadata.getId());
			linkLayer.setName(linkLayerName);
			this.updateById(linkLayer);
		}
		archiveConfigManageService.save(linkLayer.getStorageLocate(), linkLayer.getModuleId(), TypedefEnum.DOCUMENT.getValue());
		return Boolean.TRUE;
	}

	@Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public R saveBatchAttachLinkColumnRule(SaveLinkColumnRuleMetadata saveLinkColumnRuleMetadata) throws ArchiveBusinessException {
		//删除现有文件名规则
		this.remove(Wrappers.<LinkLayer>query().lambda()
				.eq(LinkLayer::getStorageLocate, saveLinkColumnRuleMetadata.getStorageLocate())
				.eq(LinkLayer::getIsDir, FileLinkEnum.BATCH_ATTACH.getCode()).eq(LinkLayer::getModuleId, saveLinkColumnRuleMetadata.getModuleId()));

		LinkLayer linkLayer = new LinkLayer();

		if (CollectionUtil.isNotEmpty(saveLinkColumnRuleMetadata.getData())) {
			linkLayer.setStorageLocate(saveLinkColumnRuleMetadata.getStorageLocate());
			linkLayer.setIsDir(FileLinkEnum.BATCH_ATTACH.getCode());
			linkLayer.setModuleId(saveLinkColumnRuleMetadata.getModuleId());
			this.save(linkLayer);

			saveLinkColumnRuleMetadata.setId(linkLayer.getId());
			//保存挂接字段组成规则
			linkColumnRuleService.saveFileLinkColumnRule(saveLinkColumnRuleMetadata);

			String linkLayerName = linkColumnRuleService.getLinkLayerName(saveLinkColumnRuleMetadata.getStorageLocate(), saveLinkColumnRuleMetadata.getId());
			linkLayer.setName(linkLayerName);
			this.updateById(linkLayer);
		}
		archiveConfigManageService.save(linkLayer.getStorageLocate(), linkLayer.getModuleId(), TypedefEnum.DOCUMENT.getValue());
		return new R<>(linkLayer);
	}

	@Override
	public List<LinkLayerTreeNode> tree(String storageLocate, Long moduleId) {
		LinkLayer root = this.getRoot(storageLocate, moduleId);
		if (ObjectUtil.isNull(root)) {
			return new ArrayList<>();
		}

		List<LinkLayer> linkLayers = this.list(Wrappers.<LinkLayer>query().lambda()
				.eq(LinkLayer::getStorageLocate, storageLocate)
				.eq(LinkLayer::getIsDir, FileLinkEnum.DIR.getCode())
				.ne(LinkLayer::getParentId, -1L).eq(LinkLayer::getModuleId, moduleId));
		List<LinkLayerTreeNode> treeNodeList = linkLayers.stream().map(linkLayer -> {
			LinkLayerTreeNode treeNode = new LinkLayerTreeNode();
			treeNode.setId(linkLayer.getId());
			treeNode.setName(linkLayer.getName());
			treeNode.setParentId(linkLayer.getParentId());

			return treeNode;
		}).collect(Collectors.toList());

		return TreeUtil.build(treeNodeList, root.getId());
	}

	@CacheEvict(allEntries = true)
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R deleteDirLink(Long id) {
		String parentIds = this.getById(id).getParentIds();
		String childrenParentIds = new StringBuilder().append(parentIds).append(ArchiveConstants.SYMBOL.COMMA).append(id).toString();

		log.debug("删除所有层次的字段组成规则");
		List<Long> childrenDirIds = getChildrenDirIds(parentIds);
		if (CollectionUtil.isNotEmpty(childrenDirIds)) {
			linkColumnRuleService.remove(Wrappers.<LinkColumnRule>query().lambda()
					.in(LinkColumnRule::getLinkLayerId, childrenDirIds));
		}

		log.debug("删除所有子层次");
		this.remove(Wrappers.<LinkLayer>query().lambda()
				.eq(LinkLayer::getIsDir, FileLinkEnum.DIR.getCode())
				.likeRight(LinkLayer::getParentIds, childrenParentIds));

		log.debug("删除当前层次");
		this.removeById(id);
		return new R<>("", "删除成功");
	}

	@Override
	public LinkLayer getFile(String storageLocate, Long moduleId) {
		LinkLayer linkLayer = this.getOne(Wrappers.<LinkLayer>query().lambda()
				.eq(LinkLayer::getStorageLocate, storageLocate)
				.eq(LinkLayer::getIsDir, FileLinkEnum.FILE.getCode()).eq(LinkLayer::getModuleId, moduleId));
		return linkLayer;
	}

	@Override
	public LinkLayer getDoc(String storageLocate, Long moduleId) {
		LinkLayer linkLayer = this.getOne(Wrappers.<LinkLayer>query().lambda()
				.eq(LinkLayer::getStorageLocate, storageLocate)
				.eq(LinkLayer::getIsDir, FileLinkEnum.DOWNLOAD.getCode()).eq(LinkLayer::getModuleId, moduleId));
		return linkLayer;
	}

	private List<Long> getChildrenDirIds(String parentIds) {
		List<LinkLayer> linkLayerList = this.list(Wrappers.<LinkLayer>query().lambda()
				.eq(LinkLayer::getIsDir, FileLinkEnum.DIR.getCode())
				.likeRight(LinkLayer::getParentIds, parentIds));
		List<Long> list = linkLayerList.stream().map(linkLayer -> linkLayer.getId()).collect(Collectors.toList());
		return list;
	}

	private LinkLayer saveLinkColumnRule(SaveLinkColumnRuleMetadata saveLinkColumnRuleMetadata, FileLinkEnum isDir) throws ArchiveBusinessException {
		LinkLayer linkLayer = new LinkLayer();
		linkLayer.setId(saveLinkColumnRuleMetadata.getId());
		linkLayer.setStorageLocate(saveLinkColumnRuleMetadata.getStorageLocate());
		linkLayer.setModuleId(saveLinkColumnRuleMetadata.getModuleId());
		if (ObjectUtil.isNull(saveLinkColumnRuleMetadata.getParentId()) && isDir == FileLinkEnum.DIR) {
			log.debug("保存挂接层次配置时：没有传入parentId，获取根节点的ID，作为parentId");
			LinkLayer root = getRoot(saveLinkColumnRuleMetadata.getStorageLocate(), saveLinkColumnRuleMetadata.getModuleId());
			if (ObjectUtil.isNull(root)) {
				log.error("获取表[{}]挂接规则根节点为空！", saveLinkColumnRuleMetadata.getStorageLocate());
				throw new ArchiveBusinessException("获取表[" + saveLinkColumnRuleMetadata.getStorageLocate() + "]挂接规则根节点为空！");
			}
			saveLinkColumnRuleMetadata.setParentId(root.getId());
			linkLayer.setParentIds(root.getParentIds());
		}
		linkLayer.setParentId(saveLinkColumnRuleMetadata.getParentId());
		linkLayer.setIsDir(isDir.getCode());
		checkChild(linkLayer);
		String parentIds = new StringBuilder()
				.append(this.getById(saveLinkColumnRuleMetadata.getParentId()).getParentIds())
				.append(ArchiveConstants.SYMBOL.COMMA)
				.append(saveLinkColumnRuleMetadata.getParentId()).toString();
		linkLayer.setParentIds(parentIds);
		//如果没有ID先保存生成ID
		if (ObjectUtil.isNull(saveLinkColumnRuleMetadata.getId())) {
			//保存挂接层次，返回ID插入字段规则表的linkLayerId
			this.save(linkLayer);
			saveLinkColumnRuleMetadata.setId(linkLayer.getId());
		}

		//保存挂接字段组成规则
		linkColumnRuleService.saveFileLinkColumnRule(saveLinkColumnRuleMetadata);

		String linkLayerName = linkColumnRuleService.getLinkLayerName(saveLinkColumnRuleMetadata.getStorageLocate(), saveLinkColumnRuleMetadata.getId());
		linkLayer.setName(linkLayerName);
		checkUniqueName(linkLayer);
		this.updateById(linkLayer);

		return linkLayer;
	}

	/**
	 * 检查每层只能有一个文件夹
	 *
	 * @param linkLayer
	 */
	private void checkChild(LinkLayer linkLayer) throws ArchiveBusinessException {
		LambdaQueryWrapper<LinkLayer> queryWrapper = Wrappers.<LinkLayer>query().lambda()
				.eq(LinkLayer::getParentId, linkLayer.getParentId());
		if (linkLayer.getId() != null) {
			queryWrapper.ne(LinkLayer::getId, linkLayer.getId());
		}
		List<LinkLayer> linkLayerList = this.list(queryWrapper);
		if (CollectionUtil.isNotEmpty(linkLayerList)) {
			throw new ArchiveBusinessException("该文件夹下已存在文件夹，不能再创建！");
		}
	}

	/**
	 * 检查挂接名字不能重复
	 *
	 * @param linkLayer
	 */
	private void checkUniqueName(LinkLayer linkLayer) throws ArchiveBusinessException {
		LambdaQueryWrapper<LinkLayer> queryWrapper = Wrappers.<LinkLayer>query().lambda()
				.eq(LinkLayer::getStorageLocate, linkLayer.getStorageLocate())
				.eq(LinkLayer::getIsDir, FileLinkEnum.DIR.getCode())
				.eq(LinkLayer::getName, linkLayer.getName())
				.ne(LinkLayer::getParentId, -1L);
		if (linkLayer.getId() != null) {
			queryWrapper.ne(LinkLayer::getId, linkLayer.getId());
		}
		List<LinkLayer> linkLayerList = this.list(queryWrapper);
		if (CollectionUtil.isNotEmpty(linkLayerList)) {
			throw new ArchiveBusinessException("已存在为[" + linkLayer.getName() + "]的文件夹，不能重复创建！");
		}
	}

	/**
	 * 根据表名获取挂接规则
	 *
	 * @param storageLocate
	 * @return
	 */
	@Cacheable(
			key = "'archive-app-management:link-layer:'+#storageLocate+':'+#moduleId",
			unless = "#result == null || #result.size() == 0"
	)
	@Override
	public List<LinkLayer> getLinkRule(String storageLocate, Long moduleId) {
		LambdaQueryWrapper<LinkLayer> linkLayerLambdaQueryWrapper = Wrappers.<LinkLayer>query().lambda()
				.eq(LinkLayer::getStorageLocate, storageLocate)
				.eq(LinkLayer::getIsDir, 1);
		if (ObjectUtil.isNotEmpty(moduleId)) {
			linkLayerLambdaQueryWrapper.eq(LinkLayer::getModuleId, moduleId);
		}
		linkLayerLambdaQueryWrapper.orderByAsc(LinkLayer::getParentIds);
		List<LinkLayer> linkLayerList = this.list(linkLayerLambdaQueryWrapper);
		if (ObjectUtil.isNotEmpty(moduleId) && CollectionUtil.isEmpty(linkLayerList)) {
			linkLayerList = this.list(Wrappers.<LinkLayer>query().lambda()
					.eq(LinkLayer::getStorageLocate, storageLocate)
					.eq(LinkLayer::getIsDir, 1).eq(LinkLayer::getModuleId, ArchiveConstants.PUBLIC_MODULE_FLAG)
					.orderByAsc(LinkLayer::getParentIds));
		}
		linkLayerList.stream().forEach(linkLayer -> {
			List<DefinedColumnRuleMetadata> definedColumnRuleMetadata = linkColumnRuleService.listOfDefined(storageLocate, linkLayer.getId());
			linkLayer.setLinkColumnRule(definedColumnRuleMetadata);

		});

		return linkLayerList;
	}

	/**
	 * 根据表名获取文件名规则
	 *
	 * @param storageLocate 存储表名
	 * @return
	 */
	@Cacheable(
			key = "'archive-app-management:link-layer:fileName:'+#storageLocate+':'+#moduleId",
			unless = "#result == null"
	)
	@Override
	public LinkLayer getFileNameLinkRule(String storageLocate, Long moduleId) {
		LinkLayer linkLayer = this.getOne(Wrappers.<LinkLayer>query().lambda()
				.eq(LinkLayer::getStorageLocate, storageLocate)
				.eq(LinkLayer::getIsDir, FileLinkEnum.FILE.getCode()).eq(LinkLayer::getModuleId, moduleId));
		if (ObjectUtil.isEmpty(linkLayer)) {
			linkLayer = this.getOne(Wrappers.<LinkLayer>query().lambda()
					.eq(LinkLayer::getStorageLocate, storageLocate)
					.eq(LinkLayer::getIsDir, FileLinkEnum.FILE.getCode()).eq(LinkLayer::getModuleId, ArchiveConstants.PUBLIC_MODULE_FLAG));
		}
		if (ObjectUtil.isNotNull(linkLayer)) {
			List<DefinedColumnRuleMetadata> definedColumnRuleMetadata = linkColumnRuleService.listOfDefined(storageLocate, linkLayer.getId());
			linkLayer.setLinkColumnRule(definedColumnRuleMetadata);
		}
		return linkLayer;
	}

	@Cacheable(
			key = "'archive-app-management:link-layer:docName:'+#storageLocate+':'+#moduleId",
			unless = "#result == null"
	)
	@Override
	public LinkLayer getDocNameLinkRule(String storageLocate,Long moduleId) {
		LambdaQueryWrapper<LinkLayer> wrapper = Wrappers.<LinkLayer>query().lambda()
				.eq(LinkLayer::getStorageLocate, storageLocate)
				.eq(LinkLayer::getIsDir, FileLinkEnum.DOWNLOAD.getCode());
		if(ObjectUtil.isNotEmpty(moduleId)){
			wrapper.eq(LinkLayer::getModuleId,moduleId);
		}
		LinkLayer linkLayer = this.getOne(wrapper);
		if(ObjectUtil.isEmpty(linkLayer)){
			linkLayer = this.getOne(Wrappers.<LinkLayer>query().lambda()
					.eq(LinkLayer::getStorageLocate, storageLocate)
					.eq(LinkLayer::getIsDir, FileLinkEnum.DOWNLOAD.getCode()).eq(LinkLayer::getModuleId,ArchiveConstants.PUBLIC_MODULE_FLAG));
		}
		if (ObjectUtil.isNotNull(linkLayer)) {
			List<DefinedColumnRuleMetadata> definedColumnRuleMetadata = linkColumnRuleService.listOfDefined(storageLocate, linkLayer.getId());
			linkLayer.setLinkColumnRule(definedColumnRuleMetadata);
		}
		return linkLayer;
	}

	@Cacheable(
			key = "'archive-app-management:link-layer:batchAttach:'+#storageLocate+':'+#moduleId",
			unless = "#result == null"
	)
	@Override
	public LinkLayer getBatchAttachByTable(String storageLocate, Long moduleId) {
		LambdaQueryWrapper<LinkLayer> wrapper = Wrappers.<LinkLayer>query().lambda()
				.eq(LinkLayer::getStorageLocate, storageLocate)
				.eq(LinkLayer::getIsDir, FileLinkEnum.BATCH_ATTACH.getCode());
		if(ObjectUtil.isNotEmpty(moduleId)){
			wrapper.eq(LinkLayer::getModuleId,moduleId);
		}
		LinkLayer linkLayer = this.getOne(wrapper);
		if(ObjectUtil.isEmpty(linkLayer)){
			linkLayer = this.getOne(Wrappers.<LinkLayer>query().lambda()
					.eq(LinkLayer::getStorageLocate, storageLocate)
					.eq(LinkLayer::getIsDir, FileLinkEnum.BATCH_ATTACH.getCode()).eq(LinkLayer::getModuleId,ArchiveConstants.PUBLIC_MODULE_FLAG));
		}
		if (ObjectUtil.isNotNull(linkLayer)) {
			List<DefinedColumnRuleMetadata> definedColumnRuleMetadata = linkColumnRuleService.listOfDefined(storageLocate, linkLayer.getId());
			linkLayer.setLinkColumnRule(definedColumnRuleMetadata);
		}
		return linkLayer;
	}

	@Cacheable(
			key = "'archive-app-management:link-layer:batchAttach:'+#typeCode+':'+#moduleId",
			unless = "#result == null"
	)
	@Override
	public LinkLayer getBatchAttachByTypeCode(String typeCode, Long moduleId) {
		List<ArchiveTable> tableListByTypeCode = archiveTableService.getTableListByTypeCode(typeCode);
		List<ArchiveTable> archiveTables = tableListByTypeCode.stream().filter(table -> ArchiveLayerEnum.FILE.getValue().equals(table.getArchiveLayer())
				|| ArchiveLayerEnum.ONE.getValue().equals(table.getArchiveLayer()) || ArchiveLayerEnum.PRE.getValue().equals(table.getArchiveLayer())
				|| ArchiveLayerEnum.SINGLE.getCode().equals(table.getArchiveLayer())
		).collect(Collectors.toList());
		//查到卷内、一文一件层次的表，为挂接的目标
		if (ObjectUtil.isNotEmpty(archiveTables) && archiveTables.size()==1) {
			LinkLayer batchAttachByTable = getBatchAttachByTable(archiveTables.get(0).getStorageLocate(), moduleId);
			if (ObjectUtil.isNotNull(batchAttachByTable) && ObjectUtil.isNotNull(batchAttachByTable.getLinkColumnRule()) && batchAttachByTable.getLinkColumnRule().size() > 0){
				return batchAttachByTable;
			}else{
				throw new ArchiveRuntimeException("没有设置挂接规则！");
			}
		}else{
			throw new ArchiveRuntimeException("没有找到可以挂接电子全文的文件层级！");
		}
	}

	/**
	 * 删除规则
	 *
	 * @param storageLocate
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(allEntries = true)
	public boolean deleteByStorageLocate(String storageLocate) {
		return this.remove(Wrappers.<LinkLayer>query().lambda()
				.eq(LinkLayer::getStorageLocate, storageLocate));
	}

	@Cacheable(
			key = "'archive-app-management:link-layer:toolbar'+#id",
			unless = "#result == null || #result.size() == 0"
	)
	@Override
	public Collection<LinkLayer> getParentList(Long id) {
		LinkLayer linkLayer = this.getById(id);
		if (ObjectUtil.isNull(linkLayer)) {
			return new ArrayList<>();
		}
		List<Long> idList = Arrays.stream((linkLayer.getParentIds() + ArchiveConstants.SYMBOL.COMMA + id).split(ArchiveConstants.SYMBOL.COMMA))
				.map(s -> Long.parseLong(s)).collect(Collectors.toList());
		return this.list(Wrappers.<LinkLayer>query().lambda()
				.in(LinkLayer::getId, idList)
				.orderByAsc(LinkLayer::getParentIds));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(allEntries = true)
	public Boolean removeByModuleId(String storageLocate, Long moduleId) {
		boolean remove = this.remove(Wrappers.<LinkLayer>lambdaQuery().eq(LinkLayer::getModuleId, moduleId).eq(LinkLayer::getStorageLocate, storageLocate));
		archiveConfigManageService.update(storageLocate, moduleId, TypedefEnum.DOCUMENT.getValue(), FileLinkEnum.FILE.getCode());
		return remove;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R copy(CopyPostDTO copyPostDTO) {
		final Long sourceModuleId = copyPostDTO.getSourceModuleId();
		final String storageLocate = copyPostDTO.getStorageLocate();
		final List<Long> targetModuleIds = copyPostDTO.getTargetModuleIds();
		if (CollectionUtil.isNotEmpty(targetModuleIds)) {
			this.remove(Wrappers.<LinkLayer>lambdaQuery().eq(LinkLayer::getStorageLocate, storageLocate).in(LinkLayer::getModuleId, targetModuleIds));
		}
		List<LinkLayer> linkLayers = this.list(Wrappers.<LinkLayer>lambdaQuery().eq(LinkLayer::getStorageLocate, storageLocate).eq(LinkLayer::getModuleId, sourceModuleId));
		if (CollectionUtil.isEmpty(linkLayers)) {
			return new R().fail(null, "当前模块无信息可复制，请先配置当前模块信息。");
		}
		List<LinkLayer> linkLayerList = CollectionUtil.newArrayList();
		targetModuleIds.parallelStream().forEach(moduleId -> {
			linkLayers.stream().forEach(linkLayer -> {
				LinkLayer linkLayer1 = new LinkLayer();
				BeanUtil.copyProperties(linkLayer, linkLayer1);
				linkLayer1.setId(null);
				linkLayer1.setModuleId(moduleId);
				linkLayerList.add(linkLayer1);
			});
		});
		if (CollectionUtil.isNotEmpty(linkLayerList)) {
			this.saveBatch(linkLayerList);
		}
		archiveConfigManageService.saveBatchByModuleIds(storageLocate, targetModuleIds, TypedefEnum.DOCUMENT.getValue());
		return new R(null, "复制成功！");
	}

	@Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public void copyByStorageLocate(String srcStorageLocate, String destStorageLocate, Map<Long,Long> srcDestMetadataMap) {
		// 拷贝主表记录
		List<LinkLayer> srcList = this.list(Wrappers.<LinkLayer>lambdaQuery().eq(LinkLayer::getStorageLocate, srcStorageLocate));
		if (CollectionUtil.isNotEmpty(srcList)) {
			List<LinkLayer> destList = srcList.stream().map(e -> {
				LinkLayer linkLayer = new LinkLayer();
				BeanUtil.copyProperties(e, linkLayer);
				linkLayer.setId(null);
				linkLayer.setStorageLocate(destStorageLocate);
				return linkLayer;
			}).collect(Collectors.toList());
			this.saveBatch(destList);
			final Map<Long,Long> srcDestLinkLayerIdMap = MapUtil.newHashMap();
			for (int i = 0, len = srcList.size(); i < len; i++) {
				srcDestLinkLayerIdMap.put(srcList.get(i).getId(), destList.get(i).getId());
			}
			List<LinkLayer> list = destList.stream()
					.filter(linkLayer -> ObjectUtil.isNotNull(linkLayer.getParentId()) && !linkLayer.getParentId().equals(-1L))
					.map(linkLayer -> {
						linkLayer.setParentId(srcDestLinkLayerIdMap.get(linkLayer.getParentId()));
						String parentIds = linkLayer.getParentIds();
						String[] idArray = parentIds.split(StrUtil.COMMA);
						String newParentIds = Arrays.stream(idArray).map(parentId -> {
							if (!"-1".equals(parentId)) {
								return String.valueOf(srcDestLinkLayerIdMap.get(Long.valueOf(parentId)));
							}
							return parentId;
						}).collect(Collectors.joining(","));
						linkLayer.setParentIds(newParentIds);
						return linkLayer;
					}).collect(Collectors.toList());
			this.updateBatchById(list);
			// 拷贝从表配置
			linkColumnRuleService.copyByStorageLocate(srcStorageLocate, destStorageLocate, srcDestMetadataMap, srcDestLinkLayerIdMap);
		}
	}

	@Override
	public Map<String, Object> getBatchAttachDir() {
		//获取我的文件中心/挂接目录文件夹
		DirStorage dirStorage = dirStorageOpenService.getChildrenByDirName(TenantContextHolder.getTenantId(), 1L, StorageConstants.BATCH_LINK_NAME);
		Map<String, Object> map = MapUtil.<String, Object>builder().put("dirId", null).put("dirName", null).build();
		if (ObjectUtil.isNotNull(dirStorage)) {
			map.put("dirId", dirStorage.getId());
			map.put("dirName", dirStorage.getName());
		}
		return map;
	}
}
