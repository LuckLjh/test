package com.cescloud.saas.archive.service.modular.archiveconfig.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.MetadataBoxConfigDTO;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.MetadataBoxRuleDTO;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.MetadataBoxRuleUndefinedDTO;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveConfigManage;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveEditForm;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.MetadataBoxConfig;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.MetadataBoxRule;
import com.cescloud.saas.archive.api.modular.archiveconfig.feign.RemoteArchiveConfigRuleService;
import com.cescloud.saas.archive.api.modular.archivedict.entity.DictItem;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantMenu;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantTemplate;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantMenuService;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantTemplateService;
import com.cescloud.saas.archive.common.constants.*;
import com.cescloud.saas.archive.common.util.IdGenerator;
import com.cescloud.saas.archive.service.modular.archiveconfig.mapper.MetadataBoxConfigMapper;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveConfigManageService;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.MetadataBoxConfigService;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.MetadataBoxRuleService;
import com.cescloud.saas.archive.service.modular.archivedict.service.DictItemService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import com.cescloud.saas.archive.service.modular.common.tableoperation.constants.MetadataTypeEnum;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @ClassName MetadataBoxConfigServiceImpl
 * @Author zhangxuehu
 * @Date 2020/7/27 11:12
 **/
@Service
@Slf4j
public class MetadataBoxConfigServiceImpl extends ServiceImpl<MetadataBoxConfigMapper, MetadataBoxConfig> implements MetadataBoxConfigService {

    @Autowired
    private MetadataBoxRuleService metadataBoxRuleService;
    @Autowired
    private ArchiveConfigManageService archiveConfigManageService;
    @Autowired
    private ArchiveTableService archiveTableService;
    @Autowired
    private MetadataService metadataService;
    @Autowired
    private DictItemService dictItemService;
	@Autowired
	private RemoteArchiveConfigRuleService remoteArchiveConfigRuleService;
	@Autowired
	private RemoteTenantTemplateService remoteTenantTemplateService;
	@Autowired
	private RemoteTenantMenuService remoteTenantMenuService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveBoxConfig(MetadataBoxConfigDTO metadataBoxConfigDTO) {
        final String storageLocate = metadataBoxConfigDTO.getStorageLocate();
        final Long metadataId = metadataBoxConfigDTO.getMetadataId();
        final Long moduleId = metadataBoxConfigDTO.getModuleId();
        final Integer digitFlag = metadataBoxConfigDTO.getDigitFlag();
        final List<MetadataBoxRule> boxRules = metadataBoxConfigDTO.getBoxRules();
        final List<MetadataBoxRule> groupingFields = metadataBoxConfigDTO.getGroupingFields();
        MetadataBoxConfig metadataBoxConfig = this.getOne(Wrappers.<MetadataBoxConfig>lambdaQuery().eq(MetadataBoxConfig::getStorageLocate, storageLocate).eq(MetadataBoxConfig::getModuleId, moduleId));
        if (ObjectUtil.isNull(metadataBoxConfig)) {
            metadataBoxConfig = MetadataBoxConfig.builder().storageLocate(storageLocate)
                    .metadataId(metadataId).digitFlag(digitFlag).moduleId(moduleId).build();
            this.save(metadataBoxConfig);
        } else {
            metadataBoxConfig.setMetadataId(metadataId);
            metadataBoxConfig.setDigitFlag(digitFlag);
            this.updateById(metadataBoxConfig);
            metadataBoxRuleService.remove(Wrappers.<MetadataBoxRule>lambdaQuery().eq(MetadataBoxRule::getConfigId, metadataBoxConfig.getId()));
        }
		Long configId = metadataBoxConfig.getId();
        if (ObjectUtil.isNotNull(configId)) {
            IntStream.range(0,boxRules.size()).forEach(i->{
                MetadataBoxRule metadataBoxRule = boxRules.get(i);
                metadataBoxRule.setType(0);
                metadataBoxRule.setConfigId(configId);
                metadataBoxRule.setSortNo(i);
            });
            IntStream.range(0,groupingFields.size()).forEach(i->{
                MetadataBoxRule metadataBoxRule = groupingFields.get(i);
                metadataBoxRule.setType(1);
                metadataBoxRule.setConfigId(configId);
                metadataBoxRule.setSortNo(i);
            });
		//让装盒规则字段最后一个以连接符结尾，且不能为空，最后一位不能为数字，否则影响盒号累加
		if(boxRules.size() == 0){
			throw new ArchiveRuntimeException("必须选择组成规则!");
		} else if (boxRules.size() != 0) {
			String lastStr = boxRules.get(boxRules.size() - 1).getMetadataChinese();
			if (!boxRules.get(boxRules.size() - 1).getConnectSign().equals(ConnectSignEnum.CONNECT.getCode())) {
				throw new ArchiveRuntimeException("结尾字段必须为分隔符!");
			} else if (boxRules.get(boxRules.size() - 1).getMetadataChinese().equals("")) {
				throw new ArchiveRuntimeException("结尾字段不能为空");
			} else if (lastStr.substring(lastStr.length() - 1).matches("^[1-9]\\d*$")) {
				throw new ArchiveRuntimeException("结尾字段不能为数字");
			}
		}
			metadataBoxRuleService.saveBatch(boxRules);
            metadataBoxRuleService.saveBatch(groupingFields);
        }
        archiveConfigManageService.save(storageLocate, -1L, TypedefEnum.BOXPACKING.getValue());
        return Boolean.TRUE;
    }

    @Override
    public MetadataBoxConfigDTO getBoxConfig(String storageLocate, Long moduleId) {
        MetadataBoxConfig metadataBoxConfig = this.getOne(Wrappers.<MetadataBoxConfig>lambdaQuery().eq(MetadataBoxConfig::getStorageLocate, storageLocate).eq(MetadataBoxConfig::getModuleId, moduleId));
        if (ObjectUtil.isNull(metadataBoxConfig)) {
            metadataBoxConfig = this.getOne(Wrappers.<MetadataBoxConfig>lambdaQuery().eq(MetadataBoxConfig::getStorageLocate, storageLocate).eq(MetadataBoxConfig::getModuleId, ArchiveConstants.PUBLIC_USER_FLAG));
        }
        if (ObjectUtil.isNull(metadataBoxConfig)) {
            return new MetadataBoxConfigDTO();
        }
		Long configId = metadataBoxConfig.getId();
        MetadataBoxConfigDTO metadataBoxConfigDTO = new MetadataBoxConfigDTO();
        BeanUtils.copyProperties(metadataBoxConfig, metadataBoxConfigDTO);
        List<MetadataBoxRule> metadataBoxRules = metadataBoxRuleService.list(Wrappers.<MetadataBoxRule>lambdaQuery().eq(MetadataBoxRule::getConfigId, configId).orderByAsc(MetadataBoxRule::getSortNo));
        //BoolEnum.NO.getCode() 标识 盒号组成字段类型
        List<MetadataBoxRule> boxRules = metadataBoxRules.stream().filter(metadataBoxRule -> BoolEnum.NO.getCode().equals(metadataBoxRule.getType())).collect(Collectors.toList());
        //BoolEnum.YES.getCode() 标识 分组设置字段 类型
        List<MetadataBoxRule> groupingFields = metadataBoxRules.stream().filter(metadataBoxRule -> BoolEnum.YES.getCode().equals(metadataBoxRule.getType())).collect(Collectors.toList());
        metadataBoxConfigDTO.setBoxRules(boxRules);
        metadataBoxConfigDTO.setGroupingFields(groupingFields);
        return metadataBoxConfigDTO;
    }

    @Override
    public List<MetadataBoxRuleUndefinedDTO> listOfUnDefined(String storageLocate, Long moduleId) {
        MetadataBoxConfig metadataBoxConfig = this.getOne(Wrappers.<MetadataBoxConfig>lambdaQuery().eq(MetadataBoxConfig::getStorageLocate, storageLocate).eq(MetadataBoxConfig::getModuleId, moduleId));
        if (ObjectUtil.isNull(metadataBoxConfig)) {
            metadataBoxConfig = this.getOne(Wrappers.<MetadataBoxConfig>lambdaQuery().eq(MetadataBoxConfig::getStorageLocate, storageLocate).eq(MetadataBoxConfig::getModuleId, ArchiveConstants.PUBLIC_USER_FLAG));
        }
        if (ObjectUtil.isNotNull(metadataBoxConfig)) {
			Long configId = metadataBoxConfig.getId();
            return metadataBoxRuleService.listOfUnDefined(storageLocate, configId);
        } else {
            return metadataBoxRuleService.listOfUnDefined(storageLocate, null);
        }
    }

    @Override
    public List<Map<String, Object>> initForm(String typeCode, Long templateTableId, Long moduleId) throws ArchiveBusinessException {
        List<Map<String, Object>> formMap = CollectionUtil.newArrayList();
        List<Metadata> metadatas = CollectionUtil.newArrayList();
        final String storageLocate = archiveTableService.getStorageLocateByArchiveTypeCodeAndTemplateTableId(typeCode, templateTableId);
        MetadataBoxConfig metadataBoxConfig = this.getOne(Wrappers.<MetadataBoxConfig>lambdaQuery().eq(MetadataBoxConfig::getStorageLocate, storageLocate).eq(MetadataBoxConfig::getModuleId, moduleId));
        if (ObjectUtil.isNull(metadataBoxConfig)) {
            metadataBoxConfig = this.getOne(Wrappers.<MetadataBoxConfig>lambdaQuery().eq(MetadataBoxConfig::getStorageLocate, storageLocate).eq(MetadataBoxConfig::getModuleId, ArchiveConstants.PUBLIC_USER_FLAG));
        }
        List<Long> boxMetadataIds = CollectionUtil.newArrayList();
        if (ObjectUtil.isNotNull(metadataBoxConfig)) {
			Long configId = metadataBoxConfig.getId();
            //BoolEnum.YES.getCode() 标识 分组设置字段 类型
            List<MetadataBoxRule> metadataBoxRules = metadataBoxRuleService.list(Wrappers.<MetadataBoxRule>lambdaQuery().eq(MetadataBoxRule::getConfigId, configId));
            boxMetadataIds = metadataBoxRules.stream().filter(metadataBoxRule -> BoolEnum.NO.getCode().equals(metadataBoxRule.getType())).map(metadataBoxRule -> metadataBoxRule.getMetadataId()).collect(Collectors.toList());
            List<Long> metadataIds = metadataBoxRules.stream().filter(metadataBoxRule -> BoolEnum.YES.getCode().equals(metadataBoxRule.getType())).map(metadataBoxRule -> metadataBoxRule.getMetadataId()).collect(Collectors.toList());
           if(!metadataIds.isEmpty()){
	           metadatas = metadataService.list(Wrappers.<Metadata>lambdaQuery().in(Metadata::getId, metadataIds));
           }
        }
        //获取表单配置
		ArchiveEditForm archiveEditForm = getArchiveEditFormByStorageLocate(storageLocate, moduleId);
		if (ObjectUtil.isNull(archiveEditForm) || ObjectUtil.isNull(archiveEditForm.getFormContent())) {
			log.info("获取表：" + storageLocate + "公共表单配置....");
			archiveEditForm = getArchiveEditFormByStorageLocate(storageLocate, ArchiveConstants.PUBLIC_MODULE_FLAG);
		}
		if (ObjectUtil.isNull(archiveEditForm) || ObjectUtil.isNull(archiveEditForm.getFormContent())) {
			log.error("获取表：" + storageLocate + "表单配置失败.采用元数据的字段类型!");
		}else{//更新元数据的数据类型为表单设置的数据类型
			final LinkedHashMap contentMap = (LinkedHashMap) archiveEditForm.getFormContent();
			final List<Map<String, Object>> list = (List<Map<String, Object>>) contentMap.get(FormConstant.LIST);
			final Map<String, Map<String ,Object>> typeMap = list.stream().collect(Collectors.toMap(map -> ObjectUtil.toString(map.get(FormConstant.MODEL)),
					map -> map));
			metadatas.stream().forEach(metadata -> {
				if(typeMap.containsKey(metadata.getMetadataEnglish()) && ObjectUtil.isNotNull(typeMap.get(metadata.getMetadataEnglish()))){
					metadata.setMetadataType(ObjectUtil.toString((typeMap.get(metadata.getMetadataEnglish())).get(FormConstant.TYPE)));
					metadata.setOption((typeMap.get(metadata.getMetadataEnglish())).get(FormConstant.OPTIONS));
				}
			});
		}
		assemblyForm(formMap, metadatas, boxMetadataIds,typeCode);
        return formMap;
    }

	/**
	 * 根据表名获取表单配置
	 *
	 * @param storageLocate 表名
	 * @return ArchiveEditForm
	 */
	public ArchiveEditForm getArchiveEditFormByStorageLocate(String storageLocate, Long moduleId) {
		final R<ArchiveEditForm> result = remoteArchiveConfigRuleService.getEditFormByStorageLocate(storageLocate, moduleId);
		if (result.getCode() == CommonConstants.SUCCESS) {
			return result.getData();
		}
		return null;
	}

    @Override
    public MetadataBoxRuleDTO getBoxFieldInfo(String storageLocate, Long moduleId) throws ArchiveBusinessException {
        MetadataBoxConfig metadataBoxConfig = this.getOne(Wrappers.<MetadataBoxConfig>lambdaQuery().eq(MetadataBoxConfig::getStorageLocate, storageLocate).eq(MetadataBoxConfig::getModuleId, moduleId));
        if (ObjectUtil.isNull(metadataBoxConfig)) {
            metadataBoxConfig = this.getOne(Wrappers.<MetadataBoxConfig>lambdaQuery().eq(MetadataBoxConfig::getStorageLocate, storageLocate).eq(MetadataBoxConfig::getModuleId, ArchiveConstants.PUBLIC_USER_FLAG));
        }
        if (ObjectUtil.isNull(metadataBoxConfig)) {
            log.error("该档案门类未配置装盒定义");
            throw new ArchiveBusinessException("该档案门类未配置装盒定义");
        }
        final Long metadataId = metadataBoxConfig.getMetadataId();
        final Long configId = metadataBoxConfig.getId();
        //获取起止顺序号字段信息
        Metadata metadata = metadataService.getById(metadataId);
        //获取分组设置字段信息
        List<MetadataBoxRule> list = metadataBoxRuleService.list(Wrappers.<MetadataBoxRule>lambdaQuery().eq(MetadataBoxRule::getConfigId, configId));
        List<Long> collect = list.stream().map(metadataBoxRule -> metadataBoxRule.getMetadataId()).collect(Collectors.toList());
        List<Metadata> metadatas = metadataService.listByIds(collect);
        MetadataBoxRuleDTO metadataBoxRuleDTO = MetadataBoxRuleDTO.builder().metadataInfo(metadata).metadatas(metadatas).build();
        return metadataBoxRuleDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeByModuleId(String storageLocate, Long moduleId) {
        MetadataBoxConfig metadataBoxConfig = this.getOne(Wrappers.<MetadataBoxConfig>lambdaQuery().eq(MetadataBoxConfig::getStorageLocate, storageLocate).eq(MetadataBoxConfig::getModuleId, moduleId));
        if (ObjectUtil.isNull(metadataBoxConfig)) {
            return Boolean.TRUE;
        }
		Long id = metadataBoxConfig.getId();
		Long configId = metadataBoxConfig.getId();
        metadataBoxRuleService.remove(Wrappers.<MetadataBoxRule>lambdaQuery().eq(MetadataBoxRule::getConfigId, configId));
        this.removeById(id);
        archiveConfigManageService.update(storageLocate, moduleId, TypedefEnum.BOXPACKING.getValue(), BoolEnum.NO.getCode());
        return Boolean.TRUE;
    }

    private void assemblyForm(List<Map<String, Object>> formMap, List<Metadata> metadatas, List<Long> boxMetadataIds,String typeCode) {
        // 拼装固定字段
        formMap.add(assemblyElement(MetadataTypeEnum.VARCHAR.getValue(), "盒号", "boxNo", null, 0,typeCode));
		if (CollectionUtil.isNotEmpty(metadatas)) {
			metadatas.stream().forEach(metadata -> {
						//无盒号规则 为 0  有盒号规则并且该字段是盒号拼接规则  为 1 反之该字段不是 盒号规则内字段 为2
						Integer onBlur = 0;
						if (CollectionUtil.isNotEmpty(boxMetadataIds)) {
							if (boxMetadataIds.contains(metadata.getId())) {
								onBlur = 1;
							} else {
								onBlur = 2;
							}
						}
						formMap.add(assemblyElement(metadata.getMetadataType(), metadata.getMetadataChinese(), metadata.getMetadataEnglish(), metadata, onBlur,typeCode));
					}
			);
		}
        formMap.add(assemblyElement(MetadataTypeEnum.VARCHAR.getValue(), "起始件号", "beginFileNo", null, 2,typeCode));
        formMap.add(assemblyElement(MetadataTypeEnum.VARCHAR.getValue(), "结束件号", "endFileNo", null, 2,typeCode));
        formMap.add(assemblyElement(MetadataTypeEnum.VARCHAR.getValue(), "盒内页数", "amountOfPages", null, 0,typeCode));
        formMap.add(assemblyElement(MetadataTypeEnum.TEXT.getValue(), "备注", "remarks", null, 0,typeCode));
    }

	@Override
	@Transactional(rollbackFor = Exception.class)
    public void copyByStorageLocate(String srcStorageLocate, String destStorageLocate, Map<Long,Long> srcDestMetadataMap) {
    	// 复制主表配置
		List<MetadataBoxConfig> srcList = this.list(Wrappers.<MetadataBoxConfig>lambdaQuery().eq(MetadataBoxConfig::getStorageLocate, srcStorageLocate));
		if (CollectionUtil.isNotEmpty(srcList)) {
			List<MetadataBoxConfig> destList = srcList.stream().map(e -> {
				MetadataBoxConfig metadataBoxConfig = new MetadataBoxConfig();
				BeanUtil.copyProperties(e, metadataBoxConfig);
				metadataBoxConfig.setId(null);
				metadataBoxConfig.setStorageLocate(destStorageLocate);
				metadataBoxConfig.setMetadataId(srcDestMetadataMap.get(metadataBoxConfig.getMetadataId()));
				return metadataBoxConfig;
			}).collect(Collectors.toList());
			this.saveBatch(destList);
			final Map<Long,Long> srcDestConfigIdMap = MapUtil.newHashMap();
			for (int i = 0, len = srcList.size(); i < len; i++) {
				srcDestConfigIdMap.put(srcList.get(i).getId(), destList.get(i).getId());
			}
			// 复制从表配置
			metadataBoxRuleService.copyConfig(srcDestMetadataMap, srcDestConfigIdMap);
		}
	}

	@Override
	public R initializeHandle(Long templateId, Long tenantId) throws ArchiveBusinessException {
		ExcelReader excel = null;
		try {
			TenantTemplate tenantTemplate = remoteTenantTemplateService.selectTemplateById(templateId).getData();
			byte[] bytes = tenantTemplate.getTemplateContent();
			InputStream inputStream = new ByteArrayInputStream(bytes);
			if (ObjectUtil.isNull(inputStream)) {
				return new R<>().fail("", "获取初始化文件异常");
			}
			excel = new ExcelReader(inputStream, TemplateFieldConstants.SHEET_NAMES.METADATA_BOX_NAME);
			final List<List<Object>> read = excel.read();
			final List<ArchiveTable> archiveTables = archiveTableService.list(Wrappers.<ArchiveTable>lambdaQuery().eq(ArchiveTable::getTenantId, tenantId));
			final Map<String, ArchiveTable> archiveTableMap = archiveTables.stream().collect(Collectors.toMap(ArchiveTable::getStorageName, archive -> archive));
			final List<Metadata> metadatas = metadataService.list(Wrappers.<Metadata>lambdaQuery().eq(Metadata::getTenantId, tenantId));
			List<MetadataBoxRule> metadataBoxRules = CollUtil.newArrayList();
			final Map<String, Long> menuMaps = getArchiveTypeMenuByTenantId(tenantId).stream().collect(Collectors.toMap(TenantMenu::getMenuName, TenantMenu::getMenuId));
			menuMaps.put("全部", -1L);
			final List<ArchiveConfigManage> archiveConfigManages = CollUtil.newArrayList();
			final List<MetadataBoxConfig> metadataBoxConfigs = CollUtil.newArrayList();
			for (int i = 1, length = read.size(); i < length; i++) {
				//门类名称
				ArchiveTable archiveTable = archiveTableMap.get(StrUtil.toString(read.get(i).get(0)));
				String storageLocate = archiveTable.getStorageLocate();
				//起始序号字段
				String fieldName = StrUtil.toString(read.get(i).get(1));
				//分组设置
				String groupField = StrUtil.toString(read.get(i).get(2));
				//关联模块
				String module = StrUtil.toString(read.get(i).get(3));
				Metadata metadata = metadatas.stream().filter(e -> e.getMetadataChinese().equals(fieldName) && e.getStorageLocate().equals(storageLocate)).findAny().orElseGet(Metadata::new);
				MetadataBoxConfig metadataBoxConfig = MetadataBoxConfig.builder().metadataId(metadata.getId()).moduleId(menuMaps.get(module)).tenantId(tenantId).storageLocate(storageLocate).build();
				this.save(metadataBoxConfig);
				metadataBoxConfigs.add(metadataBoxConfig);
				//处理分组字段
				handleGroupingField(metadataBoxRules, metadatas, storageLocate, metadataBoxConfig.getId(), groupField);
				//处理拼接字段
				//handleSplicingField(archiveColumnRules, metadatas, metadataAutovalue.getId(), StrUtil.toString(read.get(i).get(5)), tenantId, archiveTable.getArchiveLayer(), layerMap);
			}
			if (CollUtil.isNotEmpty(metadataBoxRules)) {
				metadataBoxRuleService.saveBatch(metadataBoxRules);
			}
			if (CollUtil.isNotEmpty(metadataBoxConfigs)) {
				metadataBoxConfigs.parallelStream().collect(Collectors.groupingBy(metadataBoxConfig -> metadataBoxConfig.getStorageLocate() + metadataBoxConfig.getModuleId())).
						forEach((storageLocate, list) -> {
							MetadataBoxConfig metadataBoxConfig = list.get(0);
							ArchiveConfigManage archiveConfigManage = ArchiveConfigManage.builder().tenantId(tenantId).storageLocate(metadataBoxConfig.getStorageLocate()).moduleId(metadataBoxConfig.getModuleId())
									.typedef(TypedefEnum.BOXPACKING.getValue()).isDefine(BoolEnum.YES.getCode()).build();
							archiveConfigManages.add(archiveConfigManage);
						});
			}
			if (CollUtil.isNotEmpty(archiveConfigManages)) {
				archiveConfigManageService.saveBatch(archiveConfigManages);
			}
		}catch (Exception e){
			log.error("初始化门类信息装盒规则失败", e);
		}finally {
			IoUtil.close(excel);
		}
		return new R<>("","初始化门类信息装盒规则成功");
	}

	@Override
	public List<ArrayList<String>> getMetadataBoxConfigInfo(Long tenantId) throws ArchiveBusinessException {
		//获取档案类型
		final List<ArchiveTable> archiveTables = archiveTableService.list(Wrappers.<ArchiveTable>lambdaQuery().eq(ArchiveTable::getTenantId, tenantId));
		//处理档案数据
		final Map<String, String> archiveTableMap = archiveTables.stream().collect(Collectors.toMap(ArchiveTable::getStorageLocate, ArchiveTable::getStorageName));
		//获取字段信息
		final List<Metadata> metadata = metadataService.list(Wrappers.<Metadata>lambdaQuery().eq(Metadata::getTenantId, tenantId));
		//处理字段信息
		final Map<Long, String> metadataMap = metadata.stream().collect(Collectors.toMap(Metadata::getId, Metadata::getMetadataChinese));
		//获取规则信息
		final List<MetadataBoxConfig> metadataBoxConfigs = this.list(Wrappers.<MetadataBoxConfig>lambdaQuery().eq(MetadataBoxConfig::getTenantId, tenantId));
		List<Long> configIds = metadataBoxConfigs.stream().map(MetadataBoxConfig::getId).collect(Collectors.toList());
		//获取分组信息
		final List<MetadataBoxRule> metadataBoxRules = metadataBoxRuleService.list(Wrappers.<MetadataBoxRule>lambdaQuery().in(MetadataBoxRule::getConfigId, configIds));
		Map<Long, String> menuMaps = getArchiveTypeMenuByTenantId(tenantId).stream().collect(Collectors.toMap(TenantMenu::getMenuId, TenantMenu::getMenuName));
		menuMaps.put(-1L, "全部");
		//门类名称	起始序号字段	分组设置	关联模块
		return metadataBoxConfigs.stream()
				.map(metadataBoxConfig -> CollUtil.newArrayList(
						archiveTableMap.get(metadataBoxConfig.getStorageLocate()),
						metadataMap.get(metadataBoxConfig.getMetadataId()),
						disposeConcatenateField(metadataBoxConfig, metadataBoxRules, metadataMap),
						menuMaps.get(metadataBoxConfig.getModuleId())
				)).collect(Collectors.toList());
	}

	private String disposeConcatenateField(MetadataBoxConfig metadataBoxConfig, List<MetadataBoxRule> metadataBoxRules, Map<Long, String> metadataMap) {
		List<MetadataBoxRule> metadataBoxRuleList = metadataBoxRules.stream().filter(metadataBoxRule -> metadataBoxConfig.getId().equals(metadataBoxRule.getConfigId())
				&& BoolEnum.YES.getCode().equals(metadataBoxRule.getType())).collect(Collectors.toList());
		List<String> collect = metadataBoxRuleList.stream().map(metadataBoxRule -> metadataMap.get(metadataBoxRule.getMetadataId())).collect(Collectors.toList());
		return CollUtil.isNotEmpty(collect) ? String.join(StrUtil.COMMA, collect) : "";
	}

	private void handleGroupingField(List<MetadataBoxRule> metadataBoxRules, List<Metadata> metadatas, String storageLocate, Long id, String groupField) {
		List<String> split = Arrays.asList(StrUtil.split(groupField, StrUtil.COMMA));
		for (int i = 0, index = 1, length = split.size(); i < length; i++) {
			String str = split.get(i);
			Metadata metadata = metadatas.stream().filter(e -> e.getMetadataChinese().equals(str) && e.getStorageLocate().equals(storageLocate)).findAny().orElse(null);
			if (ObjectUtil.isNull(metadata)) {
				continue;
			}
			MetadataBoxRule metadataBoxRule = MetadataBoxRule.builder().configId(id).metadataId(metadata.getId()).type(1).metadataChinese(metadata.getMetadataChinese()).sortNo(i + 1).build();
			metadataBoxRules.add(metadataBoxRule);
			//处理装盒规则的组成字段。默认使用： 分组字段 - 分组字段 - ···(年度-保管期限-分类号-)，必须分隔符结尾
			handleSplicingField(id, metadata, index, metadataBoxRules);
			//元数据 和 - 分隔符，所以加2
			index += 2;
		}
	}

	private void handleSplicingField(Long id, Metadata metadata, Integer index, List<MetadataBoxRule> metadataBoxRules) {
		MetadataBoxRule metadataSign = MetadataBoxRule.builder()
				.configId(id)
				.type(0)
				.metadataId(metadata.getId())
				.metadataChinese(metadata.getMetadataChinese())
				.metadataEnglish(metadata.getMetadataEnglish())
				.dictKeyValue(1)
				.connectSign(ConnectSignEnum.METADATA.getCode())
				.sortNo(index)
				.isShowCode(0)
				.build();
		if (FieldConstants.RETENTION_PERIOD.equals(metadata.getMetadataEnglish())) {
			metadataSign.setIsShowCode(1);
			metadataSign.setDictCode(FieldConstants.RETENTION_PERIOD);
		}
		metadataBoxRules.add(metadataSign);

		MetadataBoxRule connectSign = MetadataBoxRule.builder()
				.configId(id)
				.type(0)
				.metadataId(metadata.getId())
				.metadataChinese("-")
				.metadataEnglish("-")
				.connectSign(ConnectSignEnum.CONNECT.getCode())
				.sortNo(index + 1)
				.build();
		metadataBoxRules.add(connectSign);
	}

	private List<TenantMenu> getArchiveTypeMenuByTenantId(Long tenantId) throws ArchiveBusinessException {
		final R<List<TenantMenu>> archiveTypeMenu = remoteTenantMenuService.getArchiveTypeMenu(tenantId);
		if (archiveTypeMenu.getCode() != CommonConstants.SUCCESS) {
			throw new ArchiveBusinessException("获取租户绑定门类菜单失败");
		}
		return archiveTypeMenu.getData();
	}

	private Map<String, Object> assemblyElement(String type, String name, String model, Metadata metadata, Integer onBlur,String typeCode) {
        Map<String, Object> map = CollectionUtil.newHashMap();
        map.put(FormConstant.NAME, name);
        map.put(FormConstant.MODEL, model);
        map.put(FormConstant.KEY, IdGenerator.getId());
        map.put("onBlur", onBlur);
        Map<String, Object> rules = CollectionUtil.newHashMap();
        rules.put(FormConstant.MESSAGE, name + "必须填写");
        Map options = null;
        if (MetadataTypeEnum.INT.getValue().equals(type)) {
            map.put(FormConstant.TYPE, "number");
            rules.put(FormConstant.REQUIRED, true);
            map.put(FormConstant.RULES, CollectionUtil.newArrayList(rules));
            options = CollectionUtil.newHashMap();
            options.put(FormConstant.MAX, 999999);
            options.put(FormConstant.MIN, 1);
            options.put(FormConstant.WIDTH, 12);
            map.put(FormConstant.OPTIONS, options);
        } else if (MetadataTypeEnum.DATE.getValue().equals(type) || MetadataTypeEnum.DATETIME.getValue().equals(type)) {
            map.put(FormConstant.TYPE, "date");
            map.put(FormConstant.RULES, CollectionUtil.newArrayList());
			options = CollectionUtil.newHashMap();
            if(ObjectUtil.isNotEmpty(metadata.getOption())){//先取表单参数,没有就取默认参数
				options = (Map) metadata.getOption();
			}else{
				options.put(FormConstant.FORMAT, "yyyy-MM-dd");
				options.put(FormConstant.TYPE, "date");
			}
	        options.put(FormConstant.WIDTH, 12);
	        map.put(FormConstant.OPTIONS, options);
            rules.put(FormConstant.REQUIRED, true);
            map.put(FormConstant.RULES, CollectionUtil.newArrayList(rules));
        } else if (MetadataTypeEnum.TEXT.getValue().equals(type)) {
            map.put(FormConstant.TYPE, "textarea");
            options = CollectionUtil.newHashMap();
            options.put(FormConstant.WIDTH, 24);
            map.put(FormConstant.OPTIONS, options);
            map.put(FormConstant.RULES, CollectionUtil.newArrayList());
        } else {
            if ((ObjectUtil.isNotNull(metadata)&&FieldConstants.FONDS_CODE.equals(metadata.getMetadataEnglish()))||(ObjectUtil.isNotNull(metadata) && StrUtil.isNotBlank(metadata.getDictCode()))) {
                map.put(FormConstant.TYPE, "select");
                map.put(FormConstant.OPTIONS, assemblySelectType(metadata.getDictCode(),typeCode));
                rules.put(FormConstant.REQUIRED, true);
                map.put(FormConstant.RULES, CollectionUtil.newArrayList(rules));
            } else {
                map.put(FormConstant.TYPE, "input");
                options = CollectionUtil.newHashMap();
                if ("beginFileNo".equals(model) || "endFileNo".equals(model)) {
                    options.put(FormConstant.DATATYPE, "number");
                } else {
                    options.put(FormConstant.DATATYPE, "string");
                }
                options.put(FormConstant.WIDTH, 12);
                map.put(FormConstant.OPTIONS, options);
                rules.put(FormConstant.REQUIRED, true);
                map.put(FormConstant.RULES, CollectionUtil.newArrayList(rules));
            }
        }
        return map;
    }

    private Map<String, Object> assemblySelectType(String dictCode,String typeCode) {
        Map<String, Object> map = CollectionUtil.newHashMap();
        map.put(FormConstant.MULTIPLE, false);
        map.put(FormConstant.PLACEHOLDER, "");
        map.put(FormConstant.WIDTH, 12);
        map.put("showLabel", true);
        if(StrUtil.isBlank(dictCode)){
            map.put(FormConstant.DISABLED, true);
            return map;
        }
		List<DictItem> dictItemList;
		if(StrUtil.isNotEmpty(dictCode) && DictEnum.BGQX.getValue().equals(dictCode)){ //保管期限特殊处理
			dictItemList = dictItemService.getItemListByDictCodeRel(dictCode,typeCode);
		} else {
			dictItemList = dictItemService.getItemListByDictCode(dictCode);
		}
         List<Map<String, String>> options = dictItemList.stream().map(dictItem -> {
            Map<String, String> dicMap = CollectionUtil.newHashMap(2);
            dicMap.put("label", dictItem.getItemLabel());
            dicMap.put("value", dictItem.getItemCode());
            return dicMap;
        }).collect(Collectors.toList());
        map.put(FormConstant.OPTIONS, options);
        Map props = CollectionUtil.newHashMap();
        props.put("value", "value");
        props.put("lable", "lable");
        map.put(FormConstant.PROPS, props);
        return map;
    }
}
