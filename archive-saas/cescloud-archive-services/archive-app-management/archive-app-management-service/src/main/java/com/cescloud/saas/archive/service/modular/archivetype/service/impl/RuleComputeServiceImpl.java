package com.cescloud.saas.archive.service.modular.archivetype.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.ColumnComputeRuleDTO;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.archivetype.entity.InnerRelation;
import com.cescloud.saas.archive.common.constants.ArchiveConstants;
import com.cescloud.saas.archive.common.constants.FormStatusEnum;
import com.cescloud.saas.archive.common.constants.InnerRelationForwardEnum;
import com.cescloud.saas.archive.common.constants.TypedefEnum;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveConfigManageService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.archivetype.service.InnerRelationService;
import com.cescloud.saas.archive.service.modular.archivetype.service.RuleComputeService;
import com.cescloud.saas.archive.service.modular.common.core.constant.enums.InnerRelationTypeEnum;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author liwei
 */
@Slf4j
@Service
public class RuleComputeServiceImpl implements RuleComputeService {

	@Autowired
	private InnerRelationService innerRelationService;

	@Autowired
	private ArchiveTableService archiveTableService;

	@Autowired
	private MetadataService metadataService;

	@Autowired
	private ArchiveConfigManageService archiveConfigManageService;


	@Override
	public List<InnerRelation> getRuleCompute(List<InnerRelationTypeEnum> innerRelationTypeEnumList, String storageLocate, String upStorageLocate,
											  Long moduleId, InnerRelationForwardEnum forward) {
		List<Integer> relationTypeList = innerRelationTypeEnumList.parallelStream().map(innerRelationTypeEnum -> innerRelationTypeEnum.getValue()).collect(Collectors.toList());
		final boolean flag = checkHasInnerRelation(moduleId,upStorageLocate);
		if (flag) {
			//取本模块的关联关系配置
			return getRelationList(forward,storageLocate,moduleId,relationTypeList);
		}
		// 取全局配置
		return getRelationList(forward,storageLocate, ArchiveConstants.PUBLIC_MODULE_FLAG, relationTypeList);
	}

	@Override
	public String getUpStorageLocate(String storageLocate) {
		ArchiveTable upTable = archiveTableService.getUpTableByStorageLocate(storageLocate);
		return Optional.ofNullable(upTable).map(ArchiveTable::getStorageLocate).orElse("");
	}

	@Override
	public List<String> getDownStorageLocate(String storageLocate) {
		List<ArchiveTable> downTable = archiveTableService.getDownTableByStorageLocate(storageLocate);
		return downTable.stream().map(ArchiveTable::getStorageLocate).collect(Collectors.toList());
	}


	@Override
	public List<ColumnComputeRuleDTO> convertInnerRelation(FormStatusEnum formStatusEnum, List<InnerRelation> innerRelationList) {
		List<ColumnComputeRuleDTO> list = new ArrayList<>();
		if (formStatusEnum.equals(FormStatusEnum.ADD)) {
			CollectionUtil.addAll(list,convertInnerRelationAdd(innerRelationList));
		} else if (formStatusEnum.equals(FormStatusEnum.SAVE)
				|| formStatusEnum.equals(FormStatusEnum.DECOMPOSE)
				|| formStatusEnum.equals(FormStatusEnum.MOVEOUT)
				|| formStatusEnum.equals(FormStatusEnum.DELETE)
				|| formStatusEnum.equals(FormStatusEnum.INSERTTOFOLDER)) {
			CollectionUtil.addAll(list,convertInnerRelationSave(innerRelationList));
		} else if (formStatusEnum.equals(FormStatusEnum.COMPOSE)) {
			CollectionUtil.addAll(list,convertInnerRelationCompose(innerRelationList));
		}
		return list;
	}

	private boolean checkHasInnerRelation(Long moduleId, String storageLocate) {
		final Boolean result = archiveConfigManageService.checkModuleIsDefined(moduleId, storageLocate, TypedefEnum.RELATION.getValue());
		return result;
	}

	private List<InnerRelation> getRelationList(InnerRelationForwardEnum forward,String storageLocate, Long moduleId, List<Integer> relationTypeList) {
		final LambdaQueryWrapper<InnerRelation> queryWrapper = Wrappers.<InnerRelation>lambdaQuery()
				.eq(InnerRelation::getModuleId, moduleId)
				.in(InnerRelation::getRelationType, relationTypeList);
		if (forward.equals(InnerRelationForwardEnum.DOWN)) {
			queryWrapper.eq(InnerRelation::getSourceStorageLocate, storageLocate);
		} else if (forward.equals(InnerRelationForwardEnum.UP)) {
			queryWrapper.eq(InnerRelation::getTargetStorageLocate, storageLocate);
		}
		return innerRelationService.list(queryWrapper);
	}


	/**
	 *
	 * 1）、ADD：即点击新增按钮，表单字段值需要初始化。（规则向上）。如果上级档案配置了相等的关联关系，则根据配置的规则，需要把父档案的值带到（继承）到表单字段中
	 * 2）、SAVE:新增或者修改表单中的字段值填写或修改完成后，点击保存按钮触发。即录入档案或者修改档案的保存。（规则向上）如果有父档案，则根据 求和、计数、求起止值、最大值、最小值、平均值 规则，更新父档案对应字段的
	 *  3）、COMPOSE：组卷时，（规则向下）。根据设置的相等(如果卷内对应字段值不相等，取最大值)、求和、计数、求起止值、最大值、最小值、平均值规则，计算案卷的字段值
	 *  4) 、DECOMPOSE:拆卷（将卷内文件移到未组卷中，同时删除案卷）。（规则向上）如果案卷有父档案即项目，则根据设置的求和、计数、求起止值、最大值、最小值、平均值规则更新父档案对应的字段值。
	 *  5）、INSERTTOFOLDER：插卷（将未组卷的档案插入到选中的案卷中）。（规则向上）根据设置的求和、计数、求起止值、最大值、最小值、平均值规则，计算案卷的字段值
	 *  6）、MOVEOUT：移出（将卷内文件移出到未组卷中）。（规则向上）则根据设置的求和、计数、求起止值、最大值、最小值、平均值规则更新案卷对应的字段值。
	 *  7）、DELETE：因为删除会级联删除子档案，所以不考虑向下的规则。（规则向上）根据设置的求和、计数、求起止值、最大值、最小值、平均值规则更新父档案对应的字段值
	 *
	 */

	/**
	 * 转化关联关系规则对象(ADD、UPDATE)
	 * 上面的 1）
	 * @param relationlist 关联关系
	 * @return
	 */
	private List<ColumnComputeRuleDTO> convertInnerRelationAdd(List<InnerRelation> relationlist) {
		//只考虑相等的情况
		List<ColumnComputeRuleDTO> columnComputeRuleDTOList = relationlist.stream()
				.filter(innerRelation -> innerRelation.getRelationType().equals(InnerRelationTypeEnum.EQUAL.getValue()))
				.map(innerRelation -> {
					ColumnComputeRuleDTO columnComputeRuleDTO = new ColumnComputeRuleDTO();
					columnComputeRuleDTO.setFrom(innerRelation.getSourceStorageLocate() + " t");
					columnComputeRuleDTO.setColumn(metadataService.getMetadataById(innerRelation.getSourceMetadataId()).getMetadataEnglish());
					columnComputeRuleDTO.setMetadataEnglish(metadataService.getMetadataById(innerRelation.getTargetMetadataId()).getMetadataEnglish());
					columnComputeRuleDTO.setRelationType(innerRelation.getRelationType());
					columnComputeRuleDTO.setMethod(InnerRelationTypeEnum.getEnum(innerRelation.getRelationType()).getMethod());
					columnComputeRuleDTO.setWhere("");
					columnComputeRuleDTO.setGroup("");
					return columnComputeRuleDTO;
				}).collect(Collectors.toList());
		return columnComputeRuleDTOList;
	}

	/**
	 * 转化关联关系规则对象(SAVE、UPDATE、DECOMPOSE、MOVEOUT、DELETE)
	 * 上面的 2）、4）、5）、6）、7）
	 * @param relationlist 关联关系
	 * @return
	 */
	private List<ColumnComputeRuleDTO> convertInnerRelationSave(List<InnerRelation> relationlist) {
		//不考虑相等的情况
		List<ColumnComputeRuleDTO> columnComputeRuleDTOList = relationlist.stream()
				.filter(innerRelation -> !innerRelation.getRelationType().equals(InnerRelationTypeEnum.EQUAL.getValue()))
				.map(innerRelation -> {
					ColumnComputeRuleDTO columnComputeRuleDTO = new ColumnComputeRuleDTO();
					columnComputeRuleDTO.setFrom(innerRelation.getTargetStorageLocate() + " t");
					columnComputeRuleDTO.setStorageLocate(innerRelation.getTargetStorageLocate());
					columnComputeRuleDTO.setColumn(metadataService.getMetadataById(innerRelation.getTargetMetadataId()).getMetadataEnglish());
					columnComputeRuleDTO.setMetadataEnglish(metadataService.getMetadataById(innerRelation.getSourceMetadataId()).getMetadataEnglish());
					columnComputeRuleDTO.setRelationType(innerRelation.getRelationType());
					columnComputeRuleDTO.setMethod(InnerRelationTypeEnum.getEnum(innerRelation.getRelationType()).getMethod());
					columnComputeRuleDTO.setWhere("");
					columnComputeRuleDTO.setGroup("");
					return columnComputeRuleDTO;
				}).collect(Collectors.toList());
		return columnComputeRuleDTOList;
	}

	/**
	 * 转化关联关系规则对象(COMPOSE、INSERTTOFOLDER)
	 * 上面的 3）
	 * @param relationlist 关联关系
	 * @return
	 */
	private List<ColumnComputeRuleDTO> convertInnerRelationCompose(List<InnerRelation> relationlist) {
		List<ColumnComputeRuleDTO> columnComputeRuleDTOList = relationlist.stream()
				.map(innerRelation -> {
					ColumnComputeRuleDTO columnComputeRuleDTO = new ColumnComputeRuleDTO();
					columnComputeRuleDTO.setFrom(innerRelation.getTargetStorageLocate() + " t");
					columnComputeRuleDTO.setColumn(metadataService.getMetadataById(innerRelation.getTargetMetadataId()).getMetadataEnglish());
					columnComputeRuleDTO.setMetadataEnglish(metadataService.getMetadataById(innerRelation.getSourceMetadataId()).getMetadataEnglish());
					columnComputeRuleDTO.setRelationType(innerRelation.getRelationType());
					columnComputeRuleDTO.setMethod(InnerRelationTypeEnum.getEnum(innerRelation.getRelationType()).getMethod());
					columnComputeRuleDTO.setWhere("");
					columnComputeRuleDTO.setGroup("");
					return columnComputeRuleDTO;
				}).collect(Collectors.toList());
		return columnComputeRuleDTOList;
	}


}
