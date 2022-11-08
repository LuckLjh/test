package com.cescloud.saas.archive.service.modular.archivetype.service.rulecompute;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.ColumnComputeRuleDTO;
import com.cescloud.saas.archive.api.modular.archivetype.entity.InnerRelation;
import com.cescloud.saas.archive.common.annotation.RuleHandler;
import com.cescloud.saas.archive.common.constants.FormStatusEnum;
import com.cescloud.saas.archive.common.constants.InnerRelationForwardEnum;
import com.cescloud.saas.archive.service.modular.common.core.constant.enums.InnerRelationTypeEnum;
import com.cescloud.saas.archive.service.modular.archivetype.service.RuleComputeService;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 组卷时
 * @author liwei
 */
@Slf4j
@Service
@RuleHandler(FormStatusEnum.COMPOSE)
public class ComposeArchiveRuleServiceImpl implements ArchiveRuleService {

	@Autowired
	private RuleComputeService ruleComputeService;

	/**
	 * （规则向下）。
	 * 根据设置的相等(如果卷内对应字段值不相等，取最大值)、求和、计数、求起止值、最大值、最小值、平均值规则，计算案卷的字段值
	 * @param storageLocate 表名
	 * @return
	 * @throws ArchiveBusinessException
	 */
	@Override
	public Map<String, List<ColumnComputeRuleDTO>> handleComputeRule(String storageLocate,Long moduleId) throws ArchiveBusinessException {
		Map<String,List<ColumnComputeRuleDTO>> result = new HashMap<>();
		//判断有无下级表名
		String downStorageLocate = ruleComputeService.getDownStorageLocate(storageLocate).get(0);
		if (StrUtil.isNotBlank(downStorageLocate)) {
			//获取规则
			List<InnerRelationTypeEnum> innerRelationTypeEnumList = Arrays.asList(
					InnerRelationTypeEnum.EQUAL,
					InnerRelationTypeEnum.SUMMATION,
					InnerRelationTypeEnum.COUNT,
					InnerRelationTypeEnum.START_END,
					InnerRelationTypeEnum.MAX_VALUE,
					InnerRelationTypeEnum.MIN_VALUE,
					InnerRelationTypeEnum.AVG_VALUE
			);
			List<InnerRelation> list = ruleComputeService.getRuleCompute(innerRelationTypeEnumList,storageLocate, storageLocate, moduleId, InnerRelationForwardEnum.DOWN);
			if (CollectionUtil.isNotEmpty(list)) {
				List<ColumnComputeRuleDTO> columnComputeRuleDTOS = ruleComputeService.convertInnerRelation(FormStatusEnum.COMPOSE, list);
				result.put(downStorageLocate,columnComputeRuleDTOS);
			}
		}
		return result;
	}
}
