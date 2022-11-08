
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
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.ColumnComputeRuleDTO;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedColumnRuleMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveColumnRuleMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveColumnRule;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveConfigManage;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveEdit;
import com.cescloud.saas.archive.api.modular.archivedict.dto.CopyPostDTO;
import com.cescloud.saas.archive.api.modular.archivetype.dto.AutovalueDTO;
import com.cescloud.saas.archive.api.modular.archivetype.dto.SourceDTO;
import com.cescloud.saas.archive.api.modular.archivetype.dto.SourcePostDTO;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.archivetype.entity.MetadataAutovalue;
import com.cescloud.saas.archive.api.modular.archivetype.entity.MetadataSource;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantMenu;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantTemplate;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantMenuService;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantTemplateService;
import com.cescloud.saas.archive.common.constants.*;
import com.cescloud.saas.archive.common.util.InitializeUtil;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveColumnRuleService;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveConfigManageService;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveEditService;
import com.cescloud.saas.archive.service.modular.archivetype.mapper.MetadataAutovalueMapper;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.archivetype.service.MetadataAutovalueService;
import com.cescloud.saas.archive.service.modular.archivetype.service.MetadataSourceService;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.enums.ColumnComputeRuleEnum;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 元数据字段自动赋值
 *
 * @author liwei
 * @date 2019-04-15 15:16:12
 */
@Service
@Slf4j
@CacheConfig(cacheNames = "metadata-autovalue")
public class MetadataAutovalueServiceImpl extends ServiceImpl<MetadataAutovalueMapper, MetadataAutovalue> implements MetadataAutovalueService {

	@Autowired
	private MetadataService metadataService;

	@Autowired
	private MetadataSourceService metadataSourceService;

	@Autowired
	private ArchiveTableService archiveTableService;

	@Autowired
	private ArchiveColumnRuleService archiveColumnRuleService;

	@Autowired
	private RemoteTenantTemplateService remoteTenantTemplateService;

	@Autowired
	private ArchiveConfigManageService archiveConfigManageService;
	@Autowired
	private RemoteTenantMenuService remoteTenantMenuService;
	@Autowired
	private ArchiveEditService archiveEditService;

	@CacheEvict(allEntries = true)
	@Override
	@Transactional(rollbackFor = {Exception.class})
	public R saveMetadataAutovalue(AutovalueDTO autovalueDTO) throws ArchiveBusinessException {
		checkAutovalue(autovalueDTO);
		MetadataAutovalue metadataAutovalue = new MetadataAutovalue();
		if (BoolEnum.NO.getCode().equals(autovalueDTO.getType())) {
			if (CollUtil.isNotEmpty(autovalueDTO.getSortFieldIds())) {
				metadataAutovalue.setSortMetadataIds(autovalueDTO.getSortFieldIds().stream().map(Object::toString).collect(Collectors.joining(",")));
			}
		}
		BeanUtil.copyProperties(autovalueDTO, metadataAutovalue);
		boolean result = this.save(metadataAutovalue);
		if (result) {
			if (AutovalueTypeEnum.FLOWNO.getValue().equals(autovalueDTO.getType())) {
				//新增累加规则 分组字段
				SourcePostDTO sourcePostDTO = new SourcePostDTO();
				sourcePostDTO.setId(metadataAutovalue.getId());
				sourcePostDTO.setGroupFieldIds(autovalueDTO.getGroupFieldIds());
				sourcePostDTO.setStorageLocate(autovalueDTO.getStorageLocate());
				metadataSourceService.saveMetadataSource(sourcePostDTO);
			} else if (AutovalueTypeEnum.SPLICING.getValue().equals(autovalueDTO.getType())) {
				//新增拼接规则 拼接字段
				SaveColumnRuleMetadata saveColumnRuleMetadata = new SaveColumnRuleMetadata();
				saveColumnRuleMetadata.setMetadataSourceId(metadataAutovalue.getId());
				saveColumnRuleMetadata.setData(autovalueDTO.getSplicingFields());
				archiveColumnRuleService.saveColumnRuleDefined(saveColumnRuleMetadata);
			} else if (AutovalueTypeEnum.NOW_DATE.getValue().equals(autovalueDTO.getType())) {
				//当前日期

			}
			archiveConfigManageService.save(autovalueDTO.getStorageLocate(), autovalueDTO.getModuleId(), TypedefEnum.SOURCE.getValue());

			return new R().success("", "新增成功！");
		}
		return new R().fail("", "新增失败！");
	}


	@Override
	@Cacheable(
			key = "'archive-app-management:metadata-autovalue:id'+#id",
			unless = "#result == null"
	)
	public MetadataAutovalue getAutovalueById(Long id) {
		return this.getById(id);
	}

	@Cacheable(
			key = "'archive-app-management:metadata-autovalue:'+#storageLocate+':'+#moduleId",
			unless = "#result == null || #result.size() == 0"
	)
	@Override
	public List<MetadataAutovalue> getDefinedAutovalues(String storageLocate, Long moduleId) {
		List<MetadataAutovalue> metadataAutovalues = this.list(Wrappers.<MetadataAutovalue>query()
				.lambda()
				.eq(MetadataAutovalue::getStorageLocate, storageLocate).eq(MetadataAutovalue::getModuleId, moduleId));
		if (CollectionUtil.isEmpty(metadataAutovalues)) {
			metadataAutovalues = this.list(Wrappers.<MetadataAutovalue>query()
					.lambda()
					.eq(MetadataAutovalue::getStorageLocate, storageLocate).eq(MetadataAutovalue::getModuleId, ArchiveConstants.PUBLIC_MODULE_FLAG));
		}
		return metadataAutovalues;
	}

	@Override
	public List<MetadataAutovalue> getAllDefinedAutovalues(String storageLocate) {
		return this.list(Wrappers.<MetadataAutovalue>query().lambda().eq(MetadataAutovalue::getStorageLocate, storageLocate));
	}
	@Override
	@Cacheable(
			key = "'archive-app-management:metadata-autovalue:with-metadata-name:'+#storageLocate+':'+#moduleId",
			unless = "#result == null || #result.size() == 0"
	)
	public List<AutovalueDTO> listByStorageLocate(String storageLocate, Long moduleId) {
		List<MetadataAutovalue> metadataAutovalueList = this.list(Wrappers.<MetadataAutovalue>query().lambda().
				eq(MetadataAutovalue::getStorageLocate, storageLocate).eq(MetadataAutovalue::getModuleId, moduleId));
		if(CollectionUtil.isEmpty(metadataAutovalueList)){
			return CollectionUtil.newArrayList();
		}
		List<AutovalueDTO> autovalueVoList = new ArrayList<>();
		final List<Long> ids = metadataAutovalueList.stream().map(metadata -> metadata.getMetadataId()).collect(Collectors.toList());
		Collection<Metadata> metadataCollection = metadataService.listByIds(ids);

		final Map<Long, Metadata> idMetadataMap = metadataCollection.stream().collect(Collectors.toMap(Metadata::getId, Function.identity()));

		metadataAutovalueList.stream().forEach(metadataAutovalue -> {
			AutovalueDTO autovalueDTO = new AutovalueDTO();
			BeanUtil.copyProperties(metadataAutovalue, autovalueDTO);
			autovalueDTO.setMetadataChinese(idMetadataMap.get(metadataAutovalue.getMetadataId())==null?"":idMetadataMap.get(metadataAutovalue.getMetadataId()).getMetadataChinese());
			autovalueDTO.setMetadataEnglish(idMetadataMap.get(metadataAutovalue.getMetadataId())==null?"":idMetadataMap.get(metadataAutovalue.getMetadataId()).getMetadataEnglish());
			if (BoolEnum.NO.getCode().equals(metadataAutovalue.getType())) {
				autovalueDTO.setGroupFieldIds(getGroupFieldIds(autovalueDTO.getId()));
				if (StrUtil.isNotBlank(metadataAutovalue.getSortMetadataIds())) {
					autovalueDTO.setSortFieldIds(Arrays.stream(metadataAutovalue.getSortMetadataIds().split(","))
							.map(Long::parseLong).collect(Collectors.toList()));
				}
			}
			autovalueVoList.add(autovalueDTO);
		});
		return autovalueVoList;
	}

	/**
	 * 获取累加规则的分组信息
	 *
	 * @param id
	 * @return
	 */
	private List<Long> getGroupFieldIds(Long id) {
		List<MetadataSource> metadataSourceList = metadataSourceService.getMetaDataSourceByTargetId(id);
		if (ObjectUtil.isNull(metadataSourceList)) {
			return null;
		}
		return metadataSourceList.stream().map(r -> r.getMetadataSourceId()).collect(Collectors.toList());
	}

	@Override
	@Cacheable(
			key = "'archive-app-management:metadata-autovalue:with-metadata-name:'+#archiveTypeCode+':'+#templateTableId+':'+#moduleId",
			unless = "#result == null || #result.size() == 0"
	)
	public List<AutovalueDTO> getAutovaluesByCodeAndLayer(String archiveTypeCode, Long templateTableId, Long moduleId) {
		ArchiveTable archiveTable = archiveTableService.getTableByArchiveTypeCodeAndTemplateTableId(archiveTypeCode, templateTableId);
		List<AutovalueDTO> autovalueDTOS = listByStorageLocate(archiveTable.getStorageLocate(), moduleId);
		if (CollectionUtil.isEmpty(autovalueDTOS)) {
			return listByStorageLocate(archiveTable.getStorageLocate(), ArchiveConstants.PUBLIC_MODULE_FLAG);
		}
		return autovalueDTOS;
	}

	@CacheEvict(allEntries = true)
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R update(AutovalueDTO autovalueDTO) throws ArchiveBusinessException {
		if (log.isDebugEnabled()) {
			log.debug("修改数据规则！");
		}
		MetadataAutovalue metadataAutovalue = this.getById(autovalueDTO.getId());
		if (!autovalueDTO.getMetadataId().equals(metadataAutovalue.getMetadataId())) {
			checkAutovalue(autovalueDTO);
		}
		BeanUtil.copyProperties(autovalueDTO, metadataAutovalue);
		if (BoolEnum.NO.getCode().equals(autovalueDTO.getType())) {
			if (CollUtil.isNotEmpty(autovalueDTO.getSortFieldIds())) {
				metadataAutovalue.setSortMetadataIds(autovalueDTO.getSortFieldIds().stream().map(Object::toString).collect(Collectors.joining(",")));
			}
		}
		boolean result = this.updateById(metadataAutovalue);
		if (result) {
			if (BoolEnum.NO.getCode().equals(autovalueDTO.getType())) {
				//修改累加规则 分组字段
				SourcePostDTO sourcePostDTO = new SourcePostDTO();
				BeanUtil.copyProperties(autovalueDTO, sourcePostDTO);
				boolean metadataSourceResult = metadataSourceService.saveMetadataSource(sourcePostDTO);
			} else {
				//修改拼接规则 拼接字段
				SaveColumnRuleMetadata saveColumnRuleMetadata = new SaveColumnRuleMetadata();
				saveColumnRuleMetadata.setMetadataSourceId(autovalueDTO.getId());
				saveColumnRuleMetadata.setData(autovalueDTO.getSplicingFields());
				archiveColumnRuleService.saveColumnRuleDefined(saveColumnRuleMetadata);
			}

			return new R().success("", "修改成功！");
		}
		return new R().fail("", "修改失败！");
	}

	//一个元数据只能有一个数据规则
	private void checkAutovalue(AutovalueDTO autovalueDTO) throws ArchiveBusinessException {
		if (log.isDebugEnabled()){
			log.debug("校验数据规则！");
		}
		if (ObjectUtil.isNull(autovalueDTO.getFlagZero())){
			autovalueDTO.setFlagZero(0);
		}
		QueryWrapper queryWrapper = new QueryWrapper();
		queryWrapper.eq("metadata_id", autovalueDTO.getMetadataId());
		queryWrapper.eq("storage_locate", autovalueDTO.getStorageLocate());
		queryWrapper.eq("module_id", autovalueDTO.getModuleId());
		Integer count = baseMapper.selectCount(queryWrapper);
		if (count > 0) {
			log.error("该字段数据规则已存在！");
			throw new ArchiveBusinessException("该字段数据规则已存在！");
		}
		 if (AutovalueTypeEnum.NOW_DATE.getValue().equals(autovalueDTO.getType())) { // 等于2,当前日期
/*				易用性及校验需要考虑下：
				1、支持 字符型、整数型、日期型、日期时间型字段
				2、需判断上诉字段的长度，必须大于4位，小于4位的，给出提示不能选择或该下拉选项置灰不可选择
				3、字段程度定义大于4位，且能满足填充日期时间长度的，“当前日期”填写完整当前日期时间
				4、字段程度定义大于4位，但小于填充日期时间长度的，“当前日期”填写当前年度*/
			 Metadata metadata = metadataService.getMetadataById(autovalueDTO.getMetadataId());
			 if(metadata != null){
				 String type =  metadata.getMetadataType();
				 if(type.equals(MetadataTypeEnum.VARCHAR.getValue())
						 || type.equals(MetadataTypeEnum.INT.getValue())
						 || type.equals(MetadataTypeEnum.DATETIME.getValue())
						 || type.equals(MetadataTypeEnum.DATE.getValue())){
				 	if(type.equals(MetadataTypeEnum.DATETIME.getValue())
						 || type.equals(MetadataTypeEnum.DATE.getValue()) ){
				 		// 日期类型没长度不要校验
					}else{
						int length = metadata.getMetadataLength();
						if(length<4){
							log.error("当前日期选项只支持 字符型、整数型、日期型、日期时间型字段！且字段长度最小为4");
							throw new ArchiveBusinessException("当前日期选项只支持 字符型、整数型、日期型、日期时间型字段！且字段长度最小为4");
						}
					}
				 }else{
					 log.error("当前日期选项只支持 字符型、整数型、日期型、日期时间型字段！且字段长度最小为4");
					 throw new ArchiveBusinessException("当前日期选项只支持 字符型、整数型、日期型、日期时间型字段！且字段长度最小为4");
				 }
			 }

		}
		// 等于3,页数页号规则,页数或者页号规则只能存在一个，配了页数就不能有页号，反之也是一样的
		if (AutovalueTypeEnum.PAGESORPAGENO.getValue().equals(autovalueDTO.getType())) {
			//修改要判断是不是改的就是页数页号的这个规则,新增没有id
			if(autovalueDTO.getId() == null){
				Metadata metadata = metadataService.getMetadataById(autovalueDTO.getMetadataId());
				if(metadata != null){
					String english =  metadata.getMetadataEnglish();
					if(english.toLowerCase().equals(FieldConstants.File.PAGE_NO)){
						//是页号就要查有没有配置过页数
						Metadata data = metadataService.getByStorageLocateAndMetadataEnglish(autovalueDTO.getStorageLocate(),FieldConstants.File.AMOUNT_OF_PAGES);
						if(data!=null){
							selectCount(data,autovalueDTO.getStorageLocate(),autovalueDTO.getModuleId());
						}
					}else if(english.toLowerCase().equals(FieldConstants.File.AMOUNT_OF_PAGES)){
						//是页数就要查有没有配置过页号
						Metadata data = metadataService.getByStorageLocateAndMetadataEnglish(autovalueDTO.getStorageLocate(),FieldConstants.File.PAGE_NO);
						if(data!=null){
							selectCount(data,autovalueDTO.getStorageLocate(),autovalueDTO.getModuleId());
						}
					}
				}else{
					log.error("页数页号规则未查询到对应元数据");
					throw new ArchiveBusinessException("页数页号规则未查询到对应元数据！请检查");
				}
			}
		}
	}

	private void selectCount(Metadata data, String storageLocate, Long moduleId) throws ArchiveBusinessException {
		QueryWrapper queryWrapper = new QueryWrapper();
		queryWrapper.eq("metadata_id", data.getId());
		queryWrapper.eq("storage_locate", storageLocate);
		queryWrapper.eq("module_id", moduleId);
		Integer count = baseMapper.selectCount(queryWrapper);
		if (count > 0) {
			log.error("当前层级已设置页数页号规则,页数、页号字段只能设置其中一个");
			throw new ArchiveBusinessException("当前层级已设置页数页号规则,页数、页号字段只能设置其中一个");
		}
	}


	/**
	 * 根据表名获取字段计算规则
	 * @param moduleId 模块id
	 * @param storageLocate 表名
	 * @return
	 * @throws ArchiveBusinessException
	 */
	@Override
	@Cacheable(
			key = "'archive-app-management:metadata-autovalue:compute-rule' + #moduleId + ':' + #storageLocate",
			unless = "#result == null || #result.size() == 0"
	)
	public List<ColumnComputeRuleDTO> getComputeRuleByStorageLocate(Long moduleId, String storageLocate) throws ArchiveBusinessException {
		// 判断模块有无配置，没有配置就查询全局配置
		final Boolean isDefined = archiveConfigManageService.checkModuleIsDefined(moduleId, storageLocate, TypedefEnum.SOURCE.getValue());
		final LambdaQueryWrapper<MetadataAutovalue> queryWrapper = Wrappers.<MetadataAutovalue>query().lambda().eq(MetadataAutovalue::getStorageLocate, storageLocate)
				.eq(MetadataAutovalue::getType, BoolEnum.NO.getCode());
		if (isDefined) {
			queryWrapper.eq(MetadataAutovalue::getModuleId,moduleId);
		} else {
			queryWrapper.eq(MetadataAutovalue::getModuleId,ArchiveConstants.PUBLIC_MODULE_FLAG);
		}
		//得到设置为自动累加的字段,0代表累加
		List<MetadataAutovalue> metadataAutovalueList = this.list(queryWrapper);

		List<ColumnComputeRuleDTO> columnComputeRuleDTOList = metadataAutovalueList.stream().map(metadataAutovalue -> {
			//到该字段 由哪些字段分组
			List<SourceDTO> sourceDTOList = metadataSourceService.getMetadataSourcesByStorageAndTargetId(storageLocate, metadataAutovalue.getId());

			ColumnComputeRuleDTO columnComputeRuleDTO = new ColumnComputeRuleDTO();
			Metadata metadata = metadataService.getMetadataById(metadataAutovalue.getMetadataId());
			columnComputeRuleDTO.setMetadataEnglish(metadata.getMetadataEnglish());
			columnComputeRuleDTO.setRelationType(ColumnComputeRuleEnum.AUTO_VALUE.getValue());
			columnComputeRuleDTO.setMethod(ColumnComputeRuleEnum.AUTO_VALUE.getMethod());
			columnComputeRuleDTO.setColumn(metadata.getMetadataEnglish());
			columnComputeRuleDTO.setFrom(storageLocate + " t");
			columnComputeRuleDTO.setFlagZero(metadataAutovalue.getFlagZero());
			columnComputeRuleDTO.setLength(metadata.getMetadataLength());
			columnComputeRuleDTO.setMetadataType(metadata.getMetadataType());
			//如果不为空 则有分组字段，则加入where条件
			if (CollectionUtil.isNotEmpty(sourceDTOList)) {
				//columnComputeRuleDTO.setWhere(getAutoValueWhere(metadataSourceList));
				//设置分组字段（如：year_code,fonds_code）
				String metadataEnglishs = sourceDTOList.stream().map(s -> s.getMetadataEnglish()).collect(Collectors.joining(","));
				columnComputeRuleDTO.setGroup(metadataEnglishs);
			}

			return columnComputeRuleDTO;

		}).collect(Collectors.toList());
		return columnComputeRuleDTOList;
	}

	@CacheEvict(allEntries = true)
	@Override
	@Transactional(rollbackFor = {Exception.class})
	public boolean removeAutovalue(Long id, String storageLocate, Long moduleId) {
		//获取删除的记录
		MetadataAutovalue metadataAutovalue = this.getById(id);
		boolean result = this.removeById(id);
		if (BoolEnum.NO.getCode().equals(metadataAutovalue.getType())) {
			//累加规则
			metadataSourceService.removeByWrappers(Wrappers.<MetadataSource>query()
					.lambda()
					.eq(MetadataSource::getMetadataTargetId, metadataAutovalue.getId()));
		} else {
			//拼接规则
			archiveColumnRuleService.removeByWrappers(Wrappers.<ArchiveColumnRule>query()
					.lambda()
					.eq(ArchiveColumnRule::getMetadataSourceId, metadataAutovalue.getId()));
		}
		List<MetadataAutovalue> metadataAutovalues = this.list(Wrappers.<MetadataAutovalue>lambdaQuery().eq(MetadataAutovalue::getModuleId, moduleId).eq(MetadataAutovalue::getStorageLocate, storageLocate));
		if (CollectionUtil.isEmpty(metadataAutovalues)) {
			archiveConfigManageService.update(storageLocate, moduleId, TypedefEnum.SOURCE.getValue(), 0);
		}
		return result;
	}


	private List<ColumnComputeRuleDTO> getSplicingRuleByStorageLocate(String storageLocate) {
		List<MetadataAutovalue> metadataAutovalueList = this.list(Wrappers.<MetadataAutovalue>query().lambda()
				.eq(MetadataAutovalue::getStorageLocate, storageLocate)
				.eq(MetadataAutovalue::getType, BoolEnum.YES.getCode()));//!代表拼接

		List<ColumnComputeRuleDTO> columnComputeRuleDTOList = metadataAutovalueList.stream().map(metadataAutovalue -> {
			//得到每个字段定义的组成规则
			List<DefinedColumnRuleMetadata> definedColumnRuleMetadataList = archiveColumnRuleService.listOfDefined(metadataAutovalue.getId());

			ColumnComputeRuleDTO columnComputeRuleDTO = new ColumnComputeRuleDTO();
			columnComputeRuleDTO.setMetadataEnglish(metadataService.getById(metadataAutovalue.getMetadataId()).getMetadataEnglish());
			columnComputeRuleDTO.setRelationType(ColumnComputeRuleEnum.AUTO_SPLICING.getValue());
			columnComputeRuleDTO.setMethod(ColumnComputeRuleEnum.AUTO_SPLICING.getMethod());
			columnComputeRuleDTO.setColumn(getSplicingColumn(definedColumnRuleMetadataList));
			columnComputeRuleDTO.setFrom(storageLocate + " as t");
			columnComputeRuleDTO.setWhere("");
			columnComputeRuleDTO.setGroup("");
			return columnComputeRuleDTO;
		}).collect(Collectors.toList());
		return columnComputeRuleDTOList;
	}

	/**
	 * 得到拼接字段
	 *
	 * @param definedColumnRuleMetadataList
	 * @return
	 */
	private String getSplicingColumn(List<DefinedColumnRuleMetadata> definedColumnRuleMetadataList) {
		StringBuffer column = new StringBuffer().append("CONCAT(");
		definedColumnRuleMetadataList.stream().forEach(definedColumnRuleMetadata -> {
			if (definedColumnRuleMetadata.getConnectSign().equals(ConnectSignEnum.METADATA.getCode())) {
				column.append("t.").append(definedColumnRuleMetadata.getMetadataEnglish());
			} else if (definedColumnRuleMetadata.getConnectSign().equals(ConnectSignEnum.CONNECT.getCode())) {
				column.append("'").append(definedColumnRuleMetadata.getMetadataEnglish()).append("'");
			}
			column.append(",");
		});
		return column.deleteCharAt(column.length() - 1).append(")").toString();
	}

	private String getAutoValueWhere(List<MetadataSource> metadataSourceList) {
		StringBuffer where = new StringBuffer();
		metadataSourceList.stream().forEach(metadataSource -> {
			Metadata metadata = metadataService.getMetadataById(metadataSource.getMetadataSourceId());
			where.append(metadata.getMetadataEnglish()).append(" = ${").append(metadata.getMetadataEnglish()).append("} and ");
		});

		return where.substring(0, where.length() - 5);
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
		return this.remove(Wrappers.<MetadataAutovalue>query().lambda()
				.eq(MetadataAutovalue::getStorageLocate, storageLocate));
	}

	@Override
	public List<ArrayList<String>> getDataRuleDefinitionInfo(Long tenantId) throws ArchiveBusinessException {
		//获取档案类型
		final List<ArchiveTable> archiveTables = archiveTableService.list(Wrappers.<ArchiveTable>lambdaQuery().eq(ArchiveTable::getTenantId, tenantId));
		//处理档案数据
		final Map<String, String> archiveTableMap = archiveTables.stream().collect(Collectors.toMap(ArchiveTable::getStorageLocate, ArchiveTable::getStorageName));
		final Map<String, String> archiveLayerMap = archiveTables.stream().collect(Collectors.toMap(ArchiveTable::getStorageLocate, ArchiveTable::getArchiveLayer));
		//获取字段信息
		final List<Metadata> metadata = metadataService.list(Wrappers.<Metadata>lambdaQuery().eq(Metadata::getTenantId, tenantId));
		//处理字段信息
		final Map<Long, String> metadatamap = metadata.stream().collect(Collectors.toMap(Metadata::getId, Metadata::getMetadataChinese));
		//获取规则信息
		final List<MetadataAutovalue> metadataAutovalues = this.list(Wrappers.<MetadataAutovalue>lambdaQuery().eq(MetadataAutovalue::getTenantId, tenantId));
		//获取分组信息
		final List<MetadataSource> metadataSources = metadataSourceService.list(Wrappers.<MetadataSource>lambdaQuery().eq(MetadataSource::getTenantId, tenantId));
		//获取 拼接设置信息
		final List<ArchiveColumnRule> archiveColumnRules = archiveColumnRuleService.list(Wrappers.<ArchiveColumnRule>lambdaQuery().eq(ArchiveColumnRule::getTenantId, tenantId));
		Map<Long, String> menuMaps = getArchiveTypeMenuByTenantId(tenantId).stream().collect(Collectors.toMap(TenantMenu::getMenuId, TenantMenu::getMenuName));
		menuMaps.put(-1L, "全部");
		//门类名称	字段名称	字段规则	是否补零	分组字段	拼接字段 模块
		List<ArrayList<String>> collect = metadataAutovalues.stream()
				.map(metadataAutovalue -> CollectionUtil.newArrayList(
						archiveTableMap.get(metadataAutovalue.getStorageLocate()),
						metadatamap.get(metadataAutovalue.getMetadataId()), disposeFieldRules(metadataAutovalue.getType()),
						disposeIsZeroize(metadataAutovalue.getFlagZero()), disposePacketField(metadataAutovalue, metadataSources, metadatamap),
						disposeConcatenateField(metadataAutovalue, archiveColumnRules, metadatamap, archiveLayerMap),
						menuMaps.get(metadataAutovalue.getModuleId())
				)).collect(Collectors.toList());
		return collect;
	}

	/**
	 * 处理拼接字段
	 *
	 * @return
	 */
	private String disposeConcatenateField(MetadataAutovalue metadataAutovalue, List<ArchiveColumnRule> archiveColumnRules, Map<Long, String> metadatamap, Map<String, String> archiveLayerMap) {
		//自动拼接处理
		// BoolEnum.YES.getCode() 标识自动累加
		if (BoolEnum.YES.getCode().equals(metadataAutovalue.getType())) {
			List<ArchiveColumnRule> archiveColumnRuleList = archiveColumnRules.stream().filter(archiveColumnRule -> metadataAutovalue.getId().equals(archiveColumnRule.getMetadataSourceId())).collect(Collectors.toList());
			List<String> collect = archiveColumnRuleList.stream().map(archiveColumnRule -> ObjectUtil.isNotNull(archiveColumnRule.getMetadataId()) ? archiveLayerMap.get(archiveColumnRule.getStorageLocate()) + StrUtil.COLON + metadatamap.get(archiveColumnRule.getMetadataId()) : archiveColumnRule.getConnectStr()).collect(Collectors.toList());
			return CollectionUtil.isNotEmpty(collect) ? String.join(StrUtil.COMMA, collect) : TemplateFieldConstants.NOT;
		} else {
			return TemplateFieldConstants.NOT;
		}
	}

	/**
	 * 处理分组字段
	 *
	 * @return
	 */
	private String disposePacketField(MetadataAutovalue metadataAutovalue, List<MetadataSource> metadataSources, Map<Long, String> metadatamap) {
		//自动累加的为分组处理
		// BoolEnum.NO.getCode() 标识自动累加
		if (BoolEnum.NO.getCode().equals(metadataAutovalue.getType())) {
			//筛选统一档案表字段
			List<MetadataSource> collect = metadataSources.stream().filter(metadataSource -> metadataAutovalue.getStorageLocate().equals(metadataSource.getStorageLocate())).collect(Collectors.toList());
			List<String> collect1 = collect.stream().map(metadataSource -> metadatamap.get(metadataSource.getMetadataSourceId())).collect(Collectors.toList());
			return CollectionUtil.isNotEmpty(collect1) ? String.join(StrUtil.DASHED, collect1) : TemplateFieldConstants.NOT;
		} else {
			return TemplateFieldConstants.NOT;
		}
	}

	/**
	 * 处理字段规则
	 *
	 * @param type
	 * @return
	 */
	private String disposeFieldRules(Integer type) {
		//类型：0、自动累加，1、自动拼接
		// BoolEnum.NO.getCode() 标识自动累加
		AutovalueTypeEnum rule = AutovalueTypeEnum.getEnum(type);
		switch (rule) {
		case FLOWNO:
			return "自动累加";
		case SPLICING:
			return "自动拼接";
		case NOW_DATE:
			return "当前日期";
		default:
			return "自动累加";
		}
	}

	private Integer disposeFieldRules(String rule) {
		//类型：0、自动累加，1、自动拼接
		// BoolEnum.NO.getCode() 标识自动累加
		switch (rule) {
		case "自动累加":
			return AutovalueTypeEnum.FLOWNO.getValue();
		case "自动拼接":
			return AutovalueTypeEnum.SPLICING.getValue();
		case "当前日期":
			return AutovalueTypeEnum.NOW_DATE.getValue();
		default:
			return AutovalueTypeEnum.FLOWNO.getValue();
		}
	}

	/**
	 * 处理是否补零
	 *
	 * @param flagZero 补零标识
	 * @return
	 */
	private String disposeIsZeroize(Integer flagZero) {
		//补零标识，1:代表补零 0：代表不补零
		// BoolEnum.NO.getCode() 标识不补零
		return BoolEnum.NO.getCode().equals(flagZero) ? "否" : "是";
	}

	private Integer disposeIsZeroize(String status) {
		//补零标识，1:代表补零 0：代表不补零
		// BoolEnum.NO.getCode() 标识不补零
		return "否".equals(status) ? BoolEnum.NO.getCode() : BoolEnum.YES.getCode();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R initializeDataRule(Long templateId, Long tenantId) throws ArchiveBusinessException {
		ExcelReader excel = null;
		try {
			InputStream inputStream = getDefaultTemplateStream(templateId);
			if (ObjectUtil.isNull(inputStream)) {
				return new R<>().fail("", "获取初始化文件异常");
			}
			excel = new ExcelReader(inputStream, TemplateFieldConstants.SHEET_NAMES.DATA_RULE_DEFINITION, true);
			final List<List<Object>> read = excel.read();
			final List<ArchiveTable> archiveTables = archiveTableService.list(Wrappers.<ArchiveTable>lambdaQuery().eq(ArchiveTable::getTenantId, tenantId));
			final Map<String, ArchiveTable> archiveTableMap = archiveTables.stream().collect(Collectors.toMap(ArchiveTable::getStorageName, archive -> archive));
			final List<Metadata> metadatas = metadataService.list(Wrappers.<Metadata>lambdaQuery().eq(Metadata::getTenantId, tenantId));
			List<MetadataSource> metadataSources = CollectionUtil.newArrayList();
			List<ArchiveColumnRule> archiveColumnRules = CollectionUtil.newArrayList();
			final Map<String, Long> menuMaps = getArchiveTypeMenuByTenantId(tenantId).stream().collect(Collectors.toMap(TenantMenu::getMenuName, TenantMenu::getMenuId));
			menuMaps.put("全部", -1L);
			final List<ArchiveConfigManage> archiveConfigManages = CollectionUtil.newArrayList();
			final List<MetadataAutovalue> metadataAutovalues = CollectionUtil.newArrayList();
			for (int i = 1, length = read.size(); i < length; i++) {
				ArchiveTable archiveTable = archiveTableMap.get(StrUtil.toString(read.get(i).get(0)));
				String storageLocate = archiveTable.getStorageLocate();
				Map<String, String> layerMap = archiveTables.parallelStream().filter(archiveTable1 -> archiveTable.getArchiveTypeCode().equals(archiveTable1.getArchiveTypeCode())).collect(Collectors.toMap(ArchiveTable::getArchiveLayer, ArchiveTable::getStorageLocate));
				String fieldName = StrUtil.toString(read.get(i).get(1));
				//字段规则
				Integer rule = disposeFieldRules(StrUtil.toString(read.get(i).get(2)));
				//是否补零
				Integer flagZero = disposeIsZeroize(StrUtil.toString(read.get(i).get(3)));
				//模块
				String module = StrUtil.toString(read.get(i).get(6));
				Metadata metadata1 = metadatas.stream().filter(metadata -> metadata.getMetadataChinese().equals(fieldName) && metadata.getStorageLocate().equals(storageLocate)).findAny().orElseGet(()->new Metadata());
				MetadataAutovalue metadataAutovalue = MetadataAutovalue.builder().storageLocate(storageLocate).metadataId(metadata1.getId()).type(rule).flagZero(flagZero)
						.tenantId(tenantId).moduleId(menuMaps.get(module)).build();
				this.save(metadataAutovalue);
				metadataAutovalues.add(metadataAutovalue);
				//处理分组字段
				handleGroupingField(metadataSources, metadatas, storageLocate, metadataAutovalue.getId(), StrUtil.toString(read.get(i).get(4)), tenantId);
				//处理拼接字段
				handleSplicingField(archiveColumnRules, metadatas, metadataAutovalue.getId(), StrUtil.toString(read.get(i).get(5)), tenantId, archiveTable.getArchiveLayer(), layerMap);
			}
			boolean batch = Boolean.FALSE;
			if (CollectionUtil.isNotEmpty(metadataSources)) {
				batch = metadataSourceService.saveBatch(metadataSources);
			}
			if (CollectionUtil.isNotEmpty(archiveColumnRules)) {
				batch = archiveColumnRuleService.saveBatch(archiveColumnRules);
			}
			if (CollectionUtil.isNotEmpty(metadataAutovalues)) {
				metadataAutovalues.parallelStream().collect(Collectors.groupingBy(metadataAutovalue -> metadataAutovalue.getStorageLocate() + metadataAutovalue.getModuleId())).
						forEach((storageLocate, list) -> {
							MetadataAutovalue metadataAutovalue = list.get(0);
							ArchiveConfigManage archiveConfigManage = ArchiveConfigManage.builder().tenantId(tenantId).storageLocate(metadataAutovalue.getStorageLocate()).moduleId(metadataAutovalue.getModuleId()).typedef(TypedefEnum.SOURCE.getValue()).isDefine(BoolEnum.YES.getCode()).build();
							archiveConfigManages.add(archiveConfigManage);
						});
			}
			if (CollectionUtil.isNotEmpty(archiveConfigManages)) {
				archiveConfigManageService.saveBatch(archiveConfigManages);
			}
			return batch ? new R("", "初始化门类信息数据规则成功") : new R().fail(null, "初始化门类信息数据规则失败！！");
		} finally {
			IoUtil.close(excel);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(allEntries = true)
	public Boolean removeByModuleId(String storageLocate, Long moduleId) {
		List<MetadataAutovalue> list = this.list(Wrappers.<MetadataAutovalue>lambdaQuery().eq(MetadataAutovalue::getStorageLocate, storageLocate).eq(MetadataAutovalue::getModuleId, moduleId));
		List<Long> ids = list.stream().map(metadataAutovalue -> metadataAutovalue.getId()).collect(Collectors.toList());
		if(CollectionUtil.isEmpty(ids)){
			return Boolean.TRUE;
		}
		metadataSourceService.remove(Wrappers.<MetadataSource>lambdaQuery().in(MetadataSource::getMetadataSourceId, ids));
		archiveColumnRuleService.remove(Wrappers.<ArchiveColumnRule>query().lambda().in(ArchiveColumnRule::getMetadataSourceId, ids));
		boolean result = this.removeByIds(ids);
		archiveConfigManageService.update(storageLocate, moduleId, TypedefEnum.SOURCE.getValue(), BoolEnum.NO.getCode());
		return result;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R copy(CopyPostDTO copyPostDTO) throws ArchiveBusinessException {
		final Long sourceModuleId = copyPostDTO.getSourceModuleId();
		final String storageLocate = copyPostDTO.getStorageLocate();
		final List<Long> targetModuleIds = copyPostDTO.getTargetModuleIds();
		checkArchiveEdit(sourceModuleId, storageLocate, targetModuleIds);
		if (CollectionUtil.isNotEmpty(targetModuleIds)) {
			final List<MetadataAutovalue> list = this.list(Wrappers.<MetadataAutovalue>lambdaQuery().in(MetadataAutovalue::getModuleId, targetModuleIds).eq(MetadataAutovalue::getStorageLocate, storageLocate));
			final List<Long> ids = list.stream().map(metadataAutovalue -> metadataAutovalue.getId()).collect(Collectors.toList());
			if (CollectionUtil.isNotEmpty(ids)) {
				metadataSourceService.remove(Wrappers.<MetadataSource>lambdaQuery().in(MetadataSource::getMetadataTargetId, ids));
				archiveColumnRuleService.remove(Wrappers.<ArchiveColumnRule>lambdaQuery().in(ArchiveColumnRule::getMetadataSourceId, ids));
				this.removeByIds(ids);
			}
		}
		List<MetadataAutovalue> sourceMetadataAutovalues = this.list(Wrappers.<MetadataAutovalue>lambdaQuery().eq(MetadataAutovalue::getModuleId, sourceModuleId).eq(MetadataAutovalue::getStorageLocate, storageLocate));
		if (CollectionUtil.isEmpty(sourceMetadataAutovalues)) {
			return new R().fail(null, "当前模块无信息可复制，请先配置当前模块信息。");
		}
		List<Long> sourceIds = sourceMetadataAutovalues.stream().map(metadataAutovalue -> metadataAutovalue.getId()).collect(Collectors.toList());
		List<MetadataAutovalue> targetMetadataAutovalues = CollectionUtil.newArrayList();
		targetModuleIds.parallelStream().forEach(moduleId -> {
			sourceMetadataAutovalues.forEach(metadataAutovalue -> {
				MetadataAutovalue targetMetadataAutovalue = new MetadataAutovalue();
				BeanUtil.copyProperties(metadataAutovalue, targetMetadataAutovalue);
				targetMetadataAutovalue.setId(null);
				targetMetadataAutovalue.setModuleId(moduleId);
				targetMetadataAutovalues.add(targetMetadataAutovalue);
			});
		});
		if (CollectionUtil.isNotEmpty(targetMetadataAutovalues)) {
			this.saveBatch(targetMetadataAutovalues);
			copyChildTable(sourceIds, sourceMetadataAutovalues, targetMetadataAutovalues);
		}
		archiveConfigManageService.saveBatchByModuleIds(storageLocate, targetModuleIds, TypedefEnum.SOURCE.getValue());
		return new R(null, "复制成功！");
	}

	@Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public void copyByStorageLocate(String srcStorageLocate, String destStorageLocate, Map<Long, Long> srcDestMetadataMap) {
		List<MetadataAutovalue> srclist = this.list(Wrappers.<MetadataAutovalue>lambdaQuery().eq(MetadataAutovalue::getStorageLocate, srcStorageLocate));
		if (CollectionUtil.isNotEmpty(srclist)) {
			List<MetadataAutovalue> destList = srclist.stream().map(metadataAutovalue -> {
				MetadataAutovalue autovalue = new MetadataAutovalue();
				BeanUtil.copyProperties(metadataAutovalue, autovalue);
				autovalue.setId(null);
				autovalue.setStorageLocate(destStorageLocate);
				autovalue.setMetadataId(srcDestMetadataMap.get(metadataAutovalue.getMetadataId()));
				return autovalue;
			}).collect(Collectors.toList());
			this.saveBatch(destList);
			final Map<Long,Long> srcDestAutovalueIdMap = MapUtil.newHashMap();
			for (int i = 0, len = srclist.size(); i < len; i++) {
				srcDestAutovalueIdMap.put(srclist.get(i).getId(), destList.get(i).getId());
			}
			// 复制 metadataAutovalue
			metadataSourceService.copyByStorageLocate(srcStorageLocate, destStorageLocate, srcDestAutovalueIdMap, srcDestMetadataMap);
			// 复制 archiveColumnRule
			archiveColumnRuleService.copyByStorageLocate(srcStorageLocate, destStorageLocate, srcDestAutovalueIdMap, srcDestMetadataMap);
		}
	}

	private void copyChildTable(List<Long> sourceIds, List<MetadataAutovalue> sourceMetadataAutovalues, List<MetadataAutovalue> targetMetadataAutovalues) {
		final List<MetadataSource> metadataSources = metadataSourceService.list(Wrappers.<MetadataSource>lambdaQuery().in(MetadataSource::getMetadataTargetId, sourceIds));
		Map<Long, List<MetadataSource>> metadataIdAndMetadataSourceMap = sourceMetadataAutovalues.stream().collect(Collectors.toMap(MetadataAutovalue::getMetadataId, metadataAutovalue -> {
			return metadataSources.stream().filter(metadataSource -> metadataAutovalue.getId().equals(metadataSource.getMetadataTargetId())).collect(Collectors.toList());
		}));
		final List<ArchiveColumnRule> archiveColumnRules = archiveColumnRuleService.list(Wrappers.<ArchiveColumnRule>lambdaQuery().in(ArchiveColumnRule::getMetadataSourceId, sourceIds));
		Map<Long, List<ArchiveColumnRule>> metadataIdAndArchiveColumnRuleMap = sourceMetadataAutovalues.stream().collect(Collectors.toMap(MetadataAutovalue::getMetadataId, metadataAutovalue -> {
			return archiveColumnRules.stream().filter(archiveColumnRule -> metadataAutovalue.getId().equals(archiveColumnRule.getMetadataSourceId())).collect(Collectors.toList());
		}));
		List<MetadataSource> metadataSourceList = CollectionUtil.newArrayList();
		List<ArchiveColumnRule> archiveColumnRuleList = CollectionUtil.newArrayList();
		targetMetadataAutovalues.parallelStream().forEach(metadataAutovalue -> {
			metadataIdAndMetadataSourceMap.get(metadataAutovalue.getMetadataId()).stream().forEach(metadataSource -> {
				MetadataSource metadataSource1 = new MetadataSource();
				BeanUtil.copyProperties(metadataSource, metadataSource1);
				metadataSource1.setMetadataTargetId(metadataAutovalue.getId());
				metadataSource1.setId(null);
				metadataSourceList.add(metadataSource1);
			});
			metadataIdAndArchiveColumnRuleMap.get(metadataAutovalue.getMetadataId()).stream().forEach(archiveColumnRule -> {
				ArchiveColumnRule archiveColumnRule1 = new ArchiveColumnRule();
				BeanUtil.copyProperties(archiveColumnRule, archiveColumnRule1);
				archiveColumnRule1.setMetadataSourceId(metadataAutovalue.getId());
				archiveColumnRule1.setId(null);
				archiveColumnRuleList.add(archiveColumnRule1);
			});
		});
		if (CollectionUtil.isNotEmpty(metadataSourceList)) {
			metadataSourceService.saveBatch(metadataSourceList);
		}
		if (CollectionUtil.isNotEmpty(archiveColumnRuleList)) {
			archiveColumnRuleService.saveBatch(archiveColumnRuleList);
		}
	}

	/**
	 * 校验表单定义 已定义的列表 与现在的模块表单定义相同
	 *
	 * @param sourceModuleId
	 * @param storageLocate
	 * @param targetModuleIds
	 */
	private void checkArchiveEdit(Long sourceModuleId, String storageLocate, List<Long> targetModuleIds) throws ArchiveBusinessException {
		final List<MetadataAutovalue> list = this.list(Wrappers.<MetadataAutovalue>lambdaQuery().eq(MetadataAutovalue::getModuleId, sourceModuleId).eq(MetadataAutovalue::getStorageLocate, storageLocate));
		final List<Long> ids = list.stream().map(metadataAutovalue -> metadataAutovalue.getId()).collect(Collectors.toList());
		final List<MetadataSource> metadataSources = metadataSourceService.list(Wrappers.<MetadataSource>lambdaQuery().in(MetadataSource::getMetadataTargetId, ids));
		final List<Long> metadataSourceMetadataIds = metadataSources.stream().filter(metadataSource -> ObjectUtil.isNotNull(metadataSource.getMetadataSourceId())).map(metadataSource -> metadataSource.getMetadataSourceId()).collect(Collectors.toList());
		final List<ArchiveColumnRule> list1 = archiveColumnRuleService.list(Wrappers.<ArchiveColumnRule>lambdaQuery().in(ArchiveColumnRule::getMetadataSourceId, ids));
		final List<Long> archiveColumnRuleMetadataIds = list1.stream().filter(archiveColumnRule -> ObjectUtil.isNotNull(archiveColumnRule.getMetadataId())).map(archiveColumnRule -> archiveColumnRule.getMetadataId()).collect(Collectors.toList());
		// 源数据规则 配置的所有字段
		final List<Long> sourceMetadataIds = CollectionUtil.union(metadataSourceMetadataIds, archiveColumnRuleMetadataIds).stream().collect(Collectors.toList());
		final List<ArchiveEdit> archiveEdits = archiveEditService.list(Wrappers.<ArchiveEdit>lambdaQuery().eq(ArchiveEdit::getStorageLocate, storageLocate).in(ArchiveEdit::getModuleId,targetModuleIds));
		//目标 模块 表单定义字段
		Map<Long, List<ArchiveEdit>> map = archiveEdits.stream().collect(Collectors.groupingBy(ArchiveEdit::getModuleId, Collectors.toList()));
		for (Long targetModuleId : targetModuleIds) {
			List<ArchiveEdit> archiveEdits1 = map.get(targetModuleId);
			if(CollectionUtil.isEmpty(archiveEdits1)){
				throw new ArchiveBusinessException("复制到的目标模块表单定义字段不一致，请先配置表单定义字段信息");
			}
			List<Long> collect1 = archiveEdits1.stream().filter(archiveEdit->ObjectUtil.isNotNull(archiveEdit.getMetadataId())).map(archiveEdit -> archiveEdit.getMetadataId()).collect(Collectors.toList());
			if (!CollectionUtil.containsAll(collect1, sourceMetadataIds)) {
				throw new ArchiveBusinessException("复制到的目标模块表单定义字段不一致，请先配置表单定义字段信息");
			}
		}
	}

	private List<TenantMenu> getArchiveTypeMenuByTenantId(Long tenantId) throws ArchiveBusinessException {
		final R<List<TenantMenu>> archiveTypeMenu = remoteTenantMenuService.getArchiveTypeMenu(tenantId);
		if (archiveTypeMenu.getCode() != CommonConstants.SUCCESS) {
			throw new ArchiveBusinessException("获取租户绑定门类菜单失败");
		}
		return archiveTypeMenu.getData();
	}

	/**
	 * 处理拼接字段信息
	 *
	 * @param archiveColumnRules 拼接字段存储
	 * @param metadatas          字段信息
	 * @param metadataTargetId   自动生成元数据的参照元数据id
	 * @param field              Excel 数据
	 * @param tenantId           租户id
	 */
	private void handleSplicingField(List<ArchiveColumnRule> archiveColumnRules, List<Metadata> metadatas, Long metadataTargetId, String field, Long tenantId, String code, Map<String, String> layerMap) {
		if (TemplateFieldConstants.NOT.equals(field)) {
			return;
		} else {
			List<String> split = StrUtil.split(field, StrUtil.COMMA, -1, Boolean.TRUE, Boolean.TRUE);
			for (int i = 0, length = split.size(); i < length; i++) {
				String str = split.get(i);
				List<String> strings = StrUtil.split(str, StrUtil.COLON, -1, Boolean.TRUE, Boolean.TRUE);
				Integer upperLevel = code.equals(strings.get(0)) ? UpperLevelEnum.FORMSTYLE.getValue() : UpperLevelEnum.LISTSTYLE.getValue();
				String storageLocate = layerMap.get(strings.get(0));
				ArchiveColumnRule archiveColumnRule = ArchiveColumnRule.builder().storageLocate(storageLocate).metadataSourceId(metadataTargetId).zeroFlag(0).dictKeyValue(0).sortNo(i + 1).tenantId(tenantId).upperLevel(upperLevel).build();
				if (strings.size() > 1) {
					metadatas.stream().filter(metadata -> metadata.getMetadataChinese().equals(InitializeUtil.checkListVal(strings, 1)) && metadata.getStorageLocate().equals(storageLocate))
							.findAny().ifPresent(metadata -> {
						archiveColumnRule.setMetadataId(metadata.getId());
						archiveColumnRule.setMetadataEnglish(metadata.getMetadataEnglish());
						archiveColumnRule.setMetadataChinese(metadata.getMetadataChinese());
						archiveColumnRule.setConnectSign("M");
					});
				} else {
					archiveColumnRule.setConnectSign("C");
					archiveColumnRule.setConnectStr(str);
				}
				archiveColumnRules.add(archiveColumnRule);
			}
		}
	}

	/**
	 * 处理分组字段
	 *
	 * @param metadataSources
	 * @param metadatas        列表信息
	 * @param storageLocate    门类存储表名
	 * @param metadataTargetId 自动生成元数据的参照元数据id
	 * @param field            Excel数据
	 * @param tenantId         租户id
	 */
	private void handleGroupingField(List<MetadataSource> metadataSources, List<Metadata> metadatas, String storageLocate, Long metadataTargetId, String field, Long tenantId) {
		if (TemplateFieldConstants.NOT.equals(field)) {
			return;
		} else {
			List<String> split = StrUtil.split(field, StrUtil.DASHED, -1, Boolean.TRUE, Boolean.TRUE);
			for (int i = 0, length = split.size(); i < length; i++) {
				String str = split.get(i);
				Metadata metadata1 = metadatas.stream().filter(metadata -> metadata.getMetadataChinese().equals(str) && metadata.getStorageLocate().equals(storageLocate)).findAny().orElse(null);
				if (ObjectUtil.isNull(metadata1)) {
					continue;
				}
				MetadataSource metadataSource = MetadataSource.builder().storageLocate(storageLocate).metadataTargetId(metadataTargetId).metadataSourceId(metadata1.getId()).sortNo(i + 1).tenantId(tenantId).build();
				metadataSources.add(metadataSource);
			}
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


}
