package com.cescloud.saas.archive.service.modular.archivetype.service.rulecompute;

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
 * 新增初始化
 * @author liwei
 */
@Slf4j
@Service
@RuleHandler(FormStatusEnum.ADD)
public class AddArchiveRuleServiceImpl implements ArchiveRuleService {

	@Autowired
	private RuleComputeService ruleComputeService;

	/**
	 * 即点击新增按钮，表单字段值需要初始化。（规则向上）。
	 * 点击录入按钮时，表单里面的字段值要根据关联关系规则需要初始化，
	 * 如果上级档案配置了 相等（equal） 的关联关系，则根据配置的规则，需要把父档案的值带到（继承）到表单字段中
	 * @param storageLocate 表名
	 * @return
	 * @throws ArchiveBusinessException
	 */
	@Override
	public Map<String,List<ColumnComputeRuleDTO>> handleComputeRule(String storageLocate,Long moduleId) throws ArchiveBusinessException {
		Map<String,List<ColumnComputeRuleDTO>> result = new HashMap<>(1);
		//1、有没有上级表名
		String upStorageLocate = ruleComputeService.getUpStorageLocate(storageLocate);
		if (StrUtil.isNotBlank(upStorageLocate)) {
			//2、返回规则
			List<InnerRelation> ruleList = ruleComputeService.getRuleCompute(Arrays.asList(InnerRelationTypeEnum.EQUAL), storageLocate, upStorageLocate, moduleId, InnerRelationForwardEnum.UP);
			List<ColumnComputeRuleDTO> columnComputeRuleDTOS = ruleComputeService.convertInnerRelation(FormStatusEnum.ADD, ruleList);
			result.put(upStorageLocate,columnComputeRuleDTOS);
			return result;
		}
		return result;
	}

}
