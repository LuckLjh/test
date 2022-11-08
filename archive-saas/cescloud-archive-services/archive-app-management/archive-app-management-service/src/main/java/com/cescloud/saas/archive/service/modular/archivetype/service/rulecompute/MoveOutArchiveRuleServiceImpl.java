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
 * 移出时
 * @author liwei
 */
@Slf4j
@Service
@RuleHandler(FormStatusEnum.MOVEOUT)
public class MoveOutArchiveRuleServiceImpl implements ArchiveRuleService {

	@Autowired
	private RuleComputeService ruleComputeService;

	/**
	 * （将卷内文件移出到未组卷中）
	 * （规则向上）
	 * 则根据设置的求和、计数、求起止值、最大值、最小值、平均值规则更新案卷对应的字段值。
	 * @param storageLocate 表名
	 * @return
	 * @throws ArchiveBusinessException
	 */
	@Override
	public Map<String, List<ColumnComputeRuleDTO>> handleComputeRule(String storageLocate,Long moduleId) throws ArchiveBusinessException {
		Map<String,List<ColumnComputeRuleDTO>> result = new HashMap<>();
		//1、判断案卷有无上级表名
		String upStorageLocate = ruleComputeService.getUpStorageLocate(storageLocate);
		if (StrUtil.isNotBlank(upStorageLocate)) {
			//2、获取规则
			List<InnerRelationTypeEnum> innerRelationTypeEnumList = Arrays.asList(
					InnerRelationTypeEnum.SUMMATION,
					InnerRelationTypeEnum.COUNT,
					InnerRelationTypeEnum.START_END,
					InnerRelationTypeEnum.MAX_VALUE,
					InnerRelationTypeEnum.MIN_VALUE,
					InnerRelationTypeEnum.AVG_VALUE
			);
			List<InnerRelation> list = ruleComputeService.getRuleCompute(innerRelationTypeEnumList,storageLocate,upStorageLocate,moduleId, InnerRelationForwardEnum.UP);
			if (CollectionUtil.isNotEmpty(list)) {
				List<ColumnComputeRuleDTO> columnComputeRuleDTOS = ruleComputeService.convertInnerRelation(FormStatusEnum.MOVEOUT, list);
				result.put(upStorageLocate,columnComputeRuleDTOS);
			}
		}
		return result;
	}
}
