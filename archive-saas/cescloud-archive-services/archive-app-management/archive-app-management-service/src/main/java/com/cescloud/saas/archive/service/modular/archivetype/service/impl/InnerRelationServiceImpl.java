
package com.cescloud.saas.archive.service.modular.archivetype.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.ColumnComputeRuleDTO;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveConfigManage;
import com.cescloud.saas.archive.api.modular.archivedict.dto.CopyPostDTO;
import com.cescloud.saas.archive.api.modular.archivetype.dto.InnerRelationOutDTO;
import com.cescloud.saas.archive.api.modular.archivetype.dto.InnerRelationPostDTO;
import com.cescloud.saas.archive.api.modular.archivetype.dto.InnerRelationPutDTO;
import com.cescloud.saas.archive.api.modular.archivetype.dto.LayerRelationDTO;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.archivetype.entity.InnerRelation;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantMenu;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantTemplate;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantMenuService;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantTemplateService;
import com.cescloud.saas.archive.common.constants.*;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveConfigManageService;
import com.cescloud.saas.archive.service.modular.archivetype.mapper.InnerRelationMapper;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.archivetype.service.InnerRelationService;
import com.cescloud.saas.archive.service.modular.archivetype.service.RuleComputeService;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.enums.ColumnComputeRuleEnum;
import com.cescloud.saas.archive.service.modular.common.core.constant.enums.InnerRelationTypeEnum;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 档案类型关联
 *
 * @author xieanzhu
 * @date 2019-04-16 14:13:01
 */
@Service
@Slf4j
@CacheConfig(cacheNames = "inner-relation")
public class InnerRelationServiceImpl extends ServiceImpl<InnerRelationMapper, InnerRelation>
    implements InnerRelationService {

    @Autowired
    private ArchiveTableService archiveTableService;
    @Autowired
    private MetadataService metadataService;
    @Autowired
    private RuleComputeService ruleComputeService;
    @Autowired
    private RemoteTenantTemplateService remoteTenantTemplateService;
    @Autowired
    private ArchiveConfigManageService archiveConfigManageService;
	@Autowired
    private RemoteTenantMenuService remoteTenantMenuService;


    @Override
    @Cacheable(key = "'archive-app-management:inner-relation:' + #id", unless = "#result == null")
    public InnerRelation getInnerRelationById(Long id) {
        return this.getById(id);
    }

    @Override
    @CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
    public boolean removeInnerRelationById(Long id,String storageLocate,Long moduleId) {
		boolean remove = this.removeById(id);
		int count = this.count(Wrappers.<InnerRelation>lambdaQuery().eq(InnerRelation::getModuleId, moduleId).eq(InnerRelation::getSourceStorageLocate, storageLocate));
		if(count == 0){
			 archiveConfigManageService.update(storageLocate, moduleId, TypedefEnum.RELATION.getValue(), 0);
		}
		return remove;
    }

    /**
     * @return java.util.List<com.cescloud.saas.archive.api.modular.archivetype.entity.InnerRelation>
     * @Author xieanzhu
     * @Description //获取档案类型关联列表
     * @Date 22:18 2019/4/17
     * @Param [archiveTableId]
     **/
    @Cacheable(key = "'archive-app-management:inner-relation:'+#storageLocate+':'+#moduleId",
			unless = "#result == null || #result.size() == 0")
    @Override
    public List<InnerRelationOutDTO> listByArchiveTableName(String storageLocate, Long moduleId) {
        log.debug("根据表名{}获取档案类型关联列表" + storageLocate);
        List<InnerRelation> innerRelationList = this.list(Wrappers.<InnerRelation> query().lambda()
            .eq(InnerRelation::getSourceStorageLocate, storageLocate).eq(InnerRelation::getModuleId,moduleId));
        final List<InnerRelationOutDTO> innerRelationVOList = CollUtil.<InnerRelationOutDTO> newArrayList();
        innerRelationList.stream().forEach(innerRelation -> {
            final InnerRelationOutDTO innerRelationOutDTO = new InnerRelationOutDTO();
            BeanUtil.copyProperties(innerRelation, innerRelationOutDTO);
            final Boolean relation = innerRelation.getIsRelation() == 0 ? true : false;
            innerRelationOutDTO.setRelation(relation);
            innerRelationVOList.add(innerRelationOutDTO);
        });
        return innerRelationVOList;
    }

    /**
     * @param storageLocate
     *            存储表名
     * @param type
     *            type :1 0 --> 0表示通过关联表名查询被关联表名 1表示通过被关联表名查询关联表名
     * @return java.lang.String
     * @Author xieanzhu
     * @Description //通过表名查询相关联的表名
     * @Date 16:00 2019/4/19
     **/
    private String getStorageLocate(String storageLocate, Integer type) throws ArchiveBusinessException {
        log.debug("通过{}查询相关联表名" + storageLocate);
        final ArchiveTable archiveTable = archiveTableService.getTableByStorageLocate(storageLocate);
        ArchiveTable tableByTypeCodeAndLayer = null;
        if (1 == type) {
            tableByTypeCodeAndLayer = archiveTableService.getUpTableByStorageLocate(archiveTable.getStorageLocate());
        } else {
            tableByTypeCodeAndLayer = archiveTableService.getDownTableByStorageLocate(archiveTable.getStorageLocate())
                .get(0);
        }
        return tableByTypeCodeAndLayer.getStorageLocate();
    }

    /**
     * @return com.cescloud.saas.archive.service.modular.common.core.util.R
     * @Author xieanzhu
     * @Description //新增关联关系规则
     * @Date 16:28 2019/6/18
     * @Param [innerRelationDTO]
     **/
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public R save(InnerRelationPostDTO innerRelationPostDTO) throws ArchiveBusinessException {
        log.debug("新增关联关系规则！");
        //关联表
        final String sourceStorageLocate = innerRelationPostDTO.getStorageLocate();
        //获取被关联表
        final String targetStorageLocate = getStorageLocate(sourceStorageLocate, 0);
        final InnerRelation innerRelation = new InnerRelation();
        BeanUtil.copyProperties(innerRelationPostDTO, innerRelation);
        innerRelation.setSourceStorageLocate(sourceStorageLocate);
        innerRelation.setTargetStorageLocate(targetStorageLocate);
        final Integer isRelation = innerRelationPostDTO.getRelation() == true ? 0 : 1;
        innerRelation.setIsRelation(isRelation);
        final boolean result = this.save(innerRelation);
        if (result) {
			archiveConfigManageService.save(sourceStorageLocate,innerRelationPostDTO.getModuleId(), TypedefEnum.RELATION.getValue());
            return new R<>("", "新增成功！");
        }
        return new R<>("", "新增失败！");
    }

    /**
     * @param innerRelationPutDTO
     * @return com.cescloud.saas.archive.service.modular.common.core.util.R
     * @Author xieanzhu
     * @Description //修改关联关系规则
     * @Date 13:08 2019/4/19
     **/
    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public R update(InnerRelationPutDTO innerRelationPutDTO) {
        log.debug("修改关联关系规则");
        final Long id = innerRelationPutDTO.getId();
        //更新时先删除关联关系规则
        this.removeById(id);
        //关联表
        final String sourceStorageLocate = innerRelationPutDTO.getSourceStorageLocate();
        //获取被关联表
        final String targetStorageLocate = innerRelationPutDTO.getTargetStorageLocate();
        final InnerRelation innerRelation = new InnerRelation();
        BeanUtil.copyProperties(innerRelationPutDTO, innerRelation);
        innerRelation.setSourceStorageLocate(sourceStorageLocate);
        innerRelation.setTargetStorageLocate(targetStorageLocate);
        final Integer isRelation = innerRelationPutDTO.getRelation() == true ? 0 : 1;
        innerRelation.setIsRelation(isRelation);
        final boolean result = this.save(innerRelation);
        if (result) {
            return new R<>("", "修改成功！");
        }
        return new R<>().fail(null, "修改失败！");
    }

    @Override
    public List<InnerRelation> listByStorageLocate(String storageLocate,Long moduleId) {
		LambdaQueryWrapper<InnerRelation> wrapper = Wrappers.<InnerRelation>query().lambda().like(InnerRelation::getSourceStorageLocate, storageLocate)
				.or().like(InnerRelation::getTargetStorageLocate, storageLocate);
		if(ObjectUtil.isNotNull(moduleId)){
			wrapper.eq(InnerRelation::getModuleId,moduleId);
		}
		return this.list(wrapper);
    }

    @Override
    public Map<String, Object> getMetadataMap(String storagelocate) throws ArchiveBusinessException {
        final String targetStorageLocate = getStorageLocate(storagelocate, 0);
        final List<Metadata> sourceMetadataList = metadataService.listByStorageLocate(storagelocate);
        final List<Metadata> targetMetadataList = metadataService.listByStorageLocate(targetStorageLocate);
        final HashMap<String, Object> map = CollUtil.<String, Object> newHashMap();
        map.put("sourceMetadata", sourceMetadataList);
        map.put("targetMetadata", targetMetadataList);
        return map;
    }

    /**
     * 根据表名获取录入界面和组卷表单的字段计算规则
     *
     * @param storageLocate
     * @return
     */
    @Override
    public List<ColumnComputeRuleDTO> getComputeRuleByStorageLocate(String storageLocate, FormStatusEnum formStatusEnum)
        throws ArchiveBusinessException {
        final List<ColumnComputeRuleDTO> columnComputeRuleDTOList = CollUtil.<ColumnComputeRuleDTO> newArrayList();
        List<InnerRelation> relationlist = null;
        if (formStatusEnum.equals(FormStatusEnum.ADD)) {
            //新增 相等是是向上取值
            relationlist = this.list(Wrappers.<InnerRelation> query().lambda()
                .eq(InnerRelation::getTargetStorageLocate, storageLocate)
                .eq(InnerRelation::getRelationType, InnerRelationTypeEnum.EQUAL.getValue()));
        } else if (formStatusEnum.equals(FormStatusEnum.COMPOSE)) {
            //组卷 相等、求和、计数、求起止值、最大值、最小值、平均值是向下取值
            relationlist = this.list(Wrappers.<InnerRelation> query().lambda()
                .eq(InnerRelation::getSourceStorageLocate, storageLocate));
        }
        final String eqfrom = getRelationFrom(storageLocate, formStatusEnum);
        if (StrUtil.isNotBlank(eqfrom)) {
            final List<ColumnComputeRuleDTO> eqColumnComputeRuleDTOList = relationlist.stream().map(innerRelation -> {
                final ColumnComputeRuleDTO columnComputeRuleDTO = new ColumnComputeRuleDTO();
                if (formStatusEnum.equals(FormStatusEnum.ADD)) {
                    columnComputeRuleDTO.setMetadataEnglish(
                        metadataService.getMetadataById(innerRelation.getTargetMetadataId()).getMetadataEnglish());
                    columnComputeRuleDTO.setColumn(
                        metadataService.getMetadataById(innerRelation.getSourceMetadataId()).getMetadataEnglish());
                } else {
                    columnComputeRuleDTO.setMetadataEnglish(
                        metadataService.getMetadataById(innerRelation.getSourceMetadataId()).getMetadataEnglish());
                    columnComputeRuleDTO.setColumn(
                        metadataService.getMetadataById(innerRelation.getTargetMetadataId()).getMetadataEnglish());
                }
                columnComputeRuleDTO.setRelationType(innerRelation.getRelationType());
                columnComputeRuleDTO
                    .setMethod(InnerRelationTypeEnum.getEnum(innerRelation.getRelationType()).getMethod());
                columnComputeRuleDTO.setFrom(eqfrom);
                columnComputeRuleDTO.setWhere("");
                columnComputeRuleDTO.setGroup("");
                return columnComputeRuleDTO;
            }).collect(Collectors.toList());
            CollUtil.addAll(columnComputeRuleDTOList, eqColumnComputeRuleDTOList);
        }
        return columnComputeRuleDTOList;
    }

    private String getRelationFrom(String storageLocate, FormStatusEnum formStatusEnum) throws ArchiveBusinessException {
        final StringBuffer from = new StringBuffer();
        //临时
        if (formStatusEnum.equals(FormStatusEnum.ADD)) {
            final String upStorageLocate = ruleComputeService.getUpStorageLocate(storageLocate);
            if (StrUtil.isNotBlank(upStorageLocate)) {
                from.append(upStorageLocate + " t");
            }
        } else if (formStatusEnum.equals(FormStatusEnum.COMPOSE)) {
            final String downStorageLocate = ruleComputeService.getDownStorageLocate(storageLocate).get(0);
            if (StrUtil.isNotBlank(downStorageLocate)) {
                from.append(downStorageLocate + " t");
            }
        }
        return from.toString();
    }

    /**
     * 根据表名删除所有关联规则
     *
     * @param storageLocate
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(allEntries = true)
    public boolean deleteByStorageLocate(String storageLocate) {
        return this.remove(Wrappers.<InnerRelation> query().lambda()
            .eq(InnerRelation::getSourceStorageLocate, storageLocate)
            .or()
            .eq(InnerRelation::getTargetStorageLocate, storageLocate));
    }

    @Override
    public List<ArrayList<String>> getAssociationDefinitionInfo(Long tenantId) throws ArchiveBusinessException {
        //获取门类信息
        final List<ArchiveTable> archiveTables = archiveTableService
            .list(Wrappers.<ArchiveTable> lambdaQuery().eq(ArchiveTable::getTenantId, tenantId));
        //处理门类数据 StorageLocate，archiveTable
        final Map<String, ArchiveTable> archiveMap = archiveTables.stream()
            .collect(Collectors.toMap(ArchiveTable::getStorageLocate, archiveTable -> archiveTable));
        //获取字段信息
        final List<Metadata> metadatas = metadataService
            .list(Wrappers.<Metadata> lambdaQuery().eq(Metadata::getTenantId, tenantId));
        //处理字段信息 id ,MetadataChinese
        final Map<Long, String> metadataMap = metadatas.stream().collect(Collectors.toMap(Metadata::getId, Metadata::getMetadataChinese));
        //获取关联信息
        final List<InnerRelation> innerRelations = this
            .list(Wrappers.<InnerRelation> lambdaQuery().eq(InnerRelation::getTenantId, tenantId));
		Map<Long, String> menuMaps = getArchiveTypeMenuByTenantId(tenantId).stream().collect(Collectors.toMap(TenantMenu::getMenuId, TenantMenu::getMenuName));
		menuMaps.put(-1L,"全部");
        //门类名称	元数据所属层级	元数据字段	被关联元数据所属层级	被关联元数据字段	对应方式	是否关联 模块
        //BoolEnum.NO.getCode() 标识 ：否（不关联）
        final List<ArrayList<String>> collect = innerRelations.stream()
            .map(innerRelation -> CollUtil.newArrayList(
                archiveMap.get(innerRelation.getSourceStorageLocate()).getStorageName(),
                ArchiveLayerEnum.getEnum(archiveMap.get(innerRelation.getSourceStorageLocate()).getArchiveLayer())
                    .getName(),
                metadataMap.get(innerRelation.getSourceMetadataId()),
                ArchiveLayerEnum.getEnum(archiveMap.get(innerRelation.getTargetStorageLocate()).getArchiveLayer())
                    .getName(),
                metadataMap.get(innerRelation.getTargetMetadataId()),
                ColumnComputeRuleEnum.getEnum(innerRelation.getRelationType()).getName(),
                BoolEnum.NO.getCode().equals(innerRelation.getIsRelation()) ? "否" : "是",
					menuMaps.get(innerRelation.getModuleId()))).collect(Collectors.toList());
        return collect;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R initializeInnerRelation(Long templateId, Long tenantId) throws ArchiveBusinessException{
        ExcelReader excel = null;
        try {
            final InputStream inputStream = getDefaultTemplateStream(templateId);
            if (ObjectUtil.isNull(inputStream)) {
                return new R<>().fail("", "获取初始化文件异常");
            }
            excel = new ExcelReader(inputStream, TemplateFieldConstants.SHEET_NAMES.CORRELATION_DEFINITION, true);
            final List<List<Object>> read = excel.read();
            final List<ArchiveTable> archiveTables = archiveTableService
                .list(Wrappers.<ArchiveTable> lambdaQuery().eq(ArchiveTable::getTenantId, tenantId));
            final List<Metadata> metadatas = metadataService
                .list(Wrappers.<Metadata> lambdaQuery().eq(Metadata::getTenantId, tenantId));
            final List<InnerRelation> innerRelations = new ArrayList<>();
			final Map<String, Long> menuMaps = getArchiveTypeMenuByTenantId(tenantId).stream().collect(Collectors.toMap(TenantMenu::getMenuName, TenantMenu::getMenuId));
			menuMaps.put("全部", -1L);
			final List<ArchiveConfigManage> archiveConfigManages = CollectionUtil.newArrayList();
            for (int i = 1, length = read.size(); i < length; i++) {
                //门类名称	元数据所属层级	元数据字段	被关联元数据所属层级	被关联元数据字段	对应方式	是否关联
                final String archiveName = StrUtil.toString(read.get(i).get(0));
                //获取层级
                final String code = ArchiveLayerEnum.getEnumByName(StrUtil.toString(read.get(i).get(1))).getCode();
                //获取元数据字段值
                final String metadataName = StrUtil.toString(read.get(i).get(2));
                //获取被关联元数据所属层级
                final String targetStorageLocate = ArchiveLayerEnum.getEnumByName(StrUtil.toString(read.get(i).get(3))).getCode();
                //获取被关联元数据字段
                final String targetMetadata = StrUtil.toString(read.get(i).get(4));
                //获取对应方式
                final Integer relationType = ColumnComputeRuleEnum.getEnumByName(StrUtil.toString(read.get(i).get(5))).getValue();
                //获取是否关联 BoolEnum.YES.getCode() 标识是 ；BoolEnum.NO.getCode() 标识 否
                final Integer isRelation = "是".equals(StrUtil.toString(read.get(i).get(6))) ? BoolEnum.YES.getCode() : BoolEnum.NO.getCode();
				//模块
				String module = StrUtil.toString(read.get(i).get(7));
                final ArchiveTable archiveTable1 = archiveTables.parallelStream()
                    .filter(archiveTable -> archiveTable.getArchiveLayer().equals(code)
                        && archiveTable.getStorageName().equals(archiveName))
                    .findAny().orElseGet(()->new ArchiveTable());
                final Metadata metadata1 = metadatas.parallelStream()
                    .filter(metadata -> metadata.getStorageLocate().equals(archiveTable1.getStorageLocate())
                        && metadata.getMetadataChinese().equals(metadataName))
                    .findAny().orElseGet(()->new Metadata());
                final ArchiveTable archiveTable2 = archiveTables.parallelStream()
                    .filter(archiveTable -> archiveTable.getArchiveLayer().equals(targetStorageLocate)
                        && archiveTable.getArchiveTypeCode().equals(archiveTable1.getArchiveTypeCode()))
                    .findAny().orElseGet(()->new ArchiveTable());
                final Metadata metadata2 = metadatas.parallelStream()
                    .filter(metadata -> metadata.getStorageLocate().equals(archiveTable2.getStorageLocate())
                        && metadata.getMetadataChinese().equals(targetMetadata))
                    .findAny().orElseGet(()->new Metadata());
                final InnerRelation innerRelation = InnerRelation.builder()
                    .sourceStorageLocate(archiveTable1.getStorageLocate()).sourceMetadataId(metadata1.getId())
                    .targetStorageLocate(archiveTable2.getStorageLocate()).targetMetadataId(metadata2.getId())
                    .isRelation(isRelation).relationType(relationType).tenantId(tenantId).moduleId(menuMaps.get(module)).build();
                innerRelations.add(innerRelation);
            }
            boolean batch = Boolean.FALSE;
            if (CollUtil.isNotEmpty(innerRelations)) {
                batch = this.saveBatch(innerRelations);
				innerRelations.parallelStream().collect(Collectors.groupingBy(innerRelation -> innerRelation.getSourceStorageLocate() + innerRelation.getModuleId())).
						forEach((storageLocate, list) -> {
							InnerRelation innerRelation = list.get(0);
							ArchiveConfigManage archiveConfigManage = ArchiveConfigManage.builder().tenantId(tenantId).storageLocate(innerRelation.getSourceStorageLocate()).moduleId(innerRelation.getModuleId()).typedef(TypedefEnum.RELATION.getValue()).isDefine(BoolEnum.YES.getCode()).build();
							archiveConfigManages.add(archiveConfigManage);
						});
            }
			if (CollectionUtil.isNotEmpty(archiveConfigManages)) {
				archiveConfigManageService.saveBatch(archiveConfigManages);
			}
            return batch ? new R("", "初始化关联关系成功") : new R().fail(null, "初始化关联关系失败！！");
        } finally {
            IoUtil.close(excel);
        }

    }

	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(allEntries = true)
	public Boolean removeByModuleId(String storageLocate, Long moduleId) {
		boolean remove = this.remove(Wrappers.<InnerRelation>lambdaQuery().eq(InnerRelation::getSourceStorageLocate, storageLocate).eq(InnerRelation::getModuleId, moduleId));
		archiveConfigManageService.update(storageLocate,moduleId,TypedefEnum.RELATION.getValue(),BoolEnum.NO.getCode());
		return remove;
	}

	@Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public R copy(CopyPostDTO copyPostDTO) {
		final Long sourceModuleId = copyPostDTO.getSourceModuleId();
		final String storageLocate = copyPostDTO.getStorageLocate();
		final List<Long> targetModuleIds = copyPostDTO.getTargetModuleIds();
		if(CollectionUtil.isNotEmpty(targetModuleIds)){
			this.remove(Wrappers.<InnerRelation>lambdaQuery().in(InnerRelation::getModuleId,targetModuleIds).eq(InnerRelation::getSourceStorageLocate, storageLocate));
		}
		List<InnerRelation> innerRelations = this.list(Wrappers.<InnerRelation>lambdaQuery().eq(InnerRelation::getModuleId, sourceModuleId).eq(InnerRelation::getSourceStorageLocate, storageLocate));
		if(CollectionUtil.isEmpty(innerRelations)){
			return new R().fail(null,"当前模块无信息可复制，请先配置当前模块信息。");
		}
		List<InnerRelation> innerRelationList =CollectionUtil.newArrayList();
		targetModuleIds.forEach(moduleId -> {
			innerRelations.forEach(innerRelation -> {
				InnerRelation innerRelation1 = new InnerRelation();
				BeanUtil.copyProperties(innerRelation,innerRelation1);
				innerRelation1.setId(null);
				innerRelation1.setModuleId(moduleId);
				innerRelationList.add(innerRelation1);
			});
		});
		if(CollectionUtil.isNotEmpty(innerRelationList)){
			this.saveBatch(innerRelationList);
		}
		archiveConfigManageService.saveBatchByModuleIds(storageLocate,targetModuleIds,TypedefEnum.RELATION.getValue());
		return new R(null,"复制成功！");
	}

	private List<TenantMenu> getArchiveTypeMenuByTenantId(Long tenantId) throws ArchiveBusinessException {
		final R<List<TenantMenu>> archiveTypeMenu = remoteTenantMenuService.getArchiveTypeMenu(tenantId);
		if (archiveTypeMenu.getCode() != CommonConstants.SUCCESS) {
			throw new ArchiveBusinessException("获取租户绑定门类菜单失败");
		}
		return archiveTypeMenu.getData();
	}
    /**
     * 获取 初始化模板文件流
     *
     * @param templateId
     *            模板id
     * @return
     */
    private InputStream getDefaultTemplateStream(Long templateId) {
        final TenantTemplate tenantTemplate = remoteTenantTemplateService.selectTemplateById(templateId).getData();
        final byte[] bytes = (byte[]) tenantTemplate.getTemplateContent();
        final InputStream inputStream = new ByteArrayInputStream(bytes);
        return inputStream;
    }

	/**
	 * 获取相等的关联关系
	 *
	 * @param storageLocate
	 * @param moduleId
	 * @return
	 */
    @Override
	public List<LayerRelationDTO> getLayerRelation(String storageLocate, Long moduleId) {
		//原表的所有字段
    	List<Metadata> sourceMetadataList = metadataService.listAllByStorageLocate(storageLocate);
		//查找相等的关系
    	List<InnerRelation> innerRelationList = this.list(Wrappers.<InnerRelation>lambdaQuery()
				.eq(InnerRelation::getSourceStorageLocate, storageLocate)
				.eq(InnerRelation::getIsRelation, BoolEnum.YES.getCode())
				.eq(InnerRelation::getRelationType, InnerRelationTypeEnum.EQUAL.getValue()));

    	return innerRelationList.stream().map(innerRelation -> {
			List<Metadata> targetMetadataList = metadataService.listAllByStorageLocate(innerRelation.getTargetStorageLocate());

			LayerRelationDTO layerRelationDTO = new LayerRelationDTO();
			BeanUtils.copyProperties(innerRelation, layerRelationDTO);
			Metadata sourceMetadata = sourceMetadataList.stream().filter(metadata -> metadata.getId().equals(innerRelation.getSourceMetadataId()))
					.findFirst().orElseGet(()->new Metadata());
			layerRelationDTO.setSourceMetadataEnglish(sourceMetadata.getMetadataEnglish());
			layerRelationDTO.setSourceMetadataChinese(sourceMetadata.getMetadataChinese());
			Metadata targetMetadata = targetMetadataList.stream().filter(metadata -> metadata.getId().equals(innerRelation.getTargetMetadataId()))
					.findFirst().orElseGet(()->new Metadata());
			layerRelationDTO.setTargetMetadataEnglish(targetMetadata.getMetadataEnglish());
			layerRelationDTO.setTargetMetadataChinese(targetMetadata.getMetadataChinese());
			return layerRelationDTO;
		}).collect(Collectors.toList());
    }

    @Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
    public void copyByStorageLocate(String srcStorageLocate, String destStorageLocate,
									Map<Long,Long> srcDestMetadataMap, Map<String, String> destSrcStorageLocateMap) {
		List<InnerRelation> list = this.list(Wrappers.<InnerRelation>lambdaQuery().eq(InnerRelation::getSourceStorageLocate, srcStorageLocate));
		if (CollectionUtil.isNotEmpty(list)) {
			Map<String, String> srcDestStorageLocateMap = MapUtil.reverse(destSrcStorageLocateMap);
			list.stream().forEach(innerRelation -> {
				innerRelation.setId(null);
				innerRelation.setSourceStorageLocate(destStorageLocate);
				innerRelation.setTargetStorageLocate(srcDestStorageLocateMap.get(innerRelation.getTargetStorageLocate()));
				innerRelation.setSourceMetadataId(srcDestMetadataMap.get(innerRelation.getSourceMetadataId()));
				innerRelation.setTargetMetadataId(srcDestMetadataMap.get(innerRelation.getTargetMetadataId()));
			});
			this.saveBatch(list);
		}
	}

}
