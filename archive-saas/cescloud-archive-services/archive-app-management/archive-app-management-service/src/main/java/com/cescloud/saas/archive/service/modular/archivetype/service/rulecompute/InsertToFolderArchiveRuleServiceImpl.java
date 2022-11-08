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
 * 插卷，将未组卷插入到案卷
 * @author liwei
 */
@Slf4j
@Service
@RuleHandler(FormStatusEnum.INSERTTOFOLDER)
public class InsertToFolderArchiveRuleServiceImpl implements ArchiveRuleService {

	@Autowired
	private RuleComputeService ruleComputeService;


	/**
	 * （将未组卷的档案插入到选中的案卷中）。
	 * （规则向上）
	 * 根据设置的求和、计数、求起止值、最大值、最小值、平均值规则，计算案卷的字段值
	 * @param storageLocate 表名
	 * @return
	 * @throws ArchiveBusinessException
	 */
	@Override
	public Map<String, List<ColumnComputeRuleDTO>> handleComputeRule(String storageLocate,Long moduleId) throws ArchiveBusinessException {
		Map<String,List<ColumnComputeRuleDTO>> result = new HashMap<>();
		//获取案卷表名
		String upStorageLocate = ruleComputeService.getUpStorageLocate(storageLocate);
		if (StrUtil.isNotBlank(upStorageLocate)) {
			//获取规则
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
				List<ColumnComputeRuleDTO> columnComputeRuleDTOS = ruleComputeService.convertInnerRelation(FormStatusEnum.INSERTTOFOLDER, list);
				result.put(upStorageLocate,columnComputeRuleDTOS);
			}
		}
		return result;
	}
}
