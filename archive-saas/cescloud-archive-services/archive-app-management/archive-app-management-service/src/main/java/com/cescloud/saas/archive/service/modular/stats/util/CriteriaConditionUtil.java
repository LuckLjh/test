package com.cescloud.saas.archive.service.modular.stats.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.cescloud.saas.archive.api.modular.authority.dto.DataConditionDTO;
import com.cescloud.saas.archive.common.constants.LogicalOperator;
import com.cescloud.saas.archive.common.constants.MetadataTypeEnum;
import com.cescloud.saas.archive.common.search.Condition;
import com.cescloud.saas.archive.common.search.CriteriaCondition;
import com.cescloud.saas.archive.common.search.OperatorKey;
import com.cescloud.saas.archive.common.search.SingleCondition;
import com.cescloud.saas.archive.common.util.CesBlobUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
@UtilityClass
public class CriteriaConditionUtil {

	/**
	 * 将后台数据库中的条件转换成CriteriaCondition
	 *
	 * @param backCriteriaCondition
	 * @return
	 */
	public CriteriaCondition toCriteriaConditionFromDbBackCondition(List<Object> backCriteriaCondition) {
		if (CollectionUtil.isEmpty(backCriteriaCondition)) {
			return null;
		}
		if (backCriteriaCondition.size() == 1) {
			return toCriteriaConditionFromDbBackCondition(backCriteriaCondition.get(0));
		} else {
			CriteriaCondition allCriteriaCondition = CriteriaCondition.or();
			backCriteriaCondition.stream().forEach(o -> {
				CriteriaCondition condition = toCriteriaConditionFromDbBackCondition(o);
				if (null != condition) {
					allCriteriaCondition.addCondition(condition);
				}
			});
			return allCriteriaCondition;
		}
	}

	public CriteriaCondition toCriteriaConditionFromDbBackCondition(Object backCriteriaCondition) {
		if (null == backCriteriaCondition){
			return null;
		}
		Object criteriaCondition = unserialize(backCriteriaCondition);
		if (criteriaCondition instanceof CriteriaCondition) {
			return (CriteriaCondition) criteriaCondition;
		}
		return null;
	}

	public CriteriaCondition and(CriteriaCondition criteriaCondition, CriteriaCondition globalCriteriaCondition) {
		if (null == criteriaCondition) {
			return globalCriteriaCondition;
		}
		if (null == globalCriteriaCondition) {
			return criteriaCondition;
		}
		return criteriaCondition.addCondition(globalCriteriaCondition);
	}

	/**
	 * 用or拼接多个条件
	 *
	 * @param conditionList
	 * @return
	 */
	public CriteriaCondition joinOr(List<CriteriaCondition> conditionList) {
		if (CollectionUtil.isEmpty(conditionList)) {
			return null;
		}
		if (conditionList.size() == 1) {
			return conditionList.get(0);
		} else {
			CriteriaCondition andCondition = CriteriaCondition.and();
			CriteriaCondition allCondition = CriteriaCondition.or();
			conditionList.stream().forEach(criteriaCondition -> {
				allCondition.addCondition(criteriaCondition);
			});
			return andCondition.addCondition(allCondition);
		}
	}

	/**
	 * 将前台对象转换为CriteriaCondition
	 *
	 * @param dataConditionDTOList
	 * @return
	 */
	public CriteriaCondition toCriteriaCondition(List<DataConditionDTO> dataConditionDTOList) {
		//得到最外层的查询对象
		CriteriaCondition outCriteria = getOutCriteria(dataConditionDTOList);
		process(outCriteria, dataConditionDTOList);
		return outCriteria;
	}

	/**
	 * 将数据库保存的字节数组反序列化对象
	 *
	 * @param obj
	 * @return
	 */
	public Object unserialize(Object obj) {
		if (null == obj) {
			return null;
		}
		byte[] bytes = CesBlobUtil.objConvertToByte(obj);
		if (ArrayUtil.isEmpty(bytes)) {
			return null;
		}
		return ObjectUtil.deserialize(bytes);
	}

	/**
	 * 将标签字段转换为数据库字段
	 *
	 * @param backCondition
	 * @param metedataMap   标签字段与数据库字段映射关系
	 */
	public void convetDbFieldCriteriaCondition(CriteriaCondition backCondition, Map<String, String> metedataMap) {
		if (CollectionUtil.isEmpty(metedataMap)) {
			return;
		}
		convetDbField(backCondition.getConditions(), metedataMap);
	}

	/**
	 * 使用迭代替换循环来执行remove操作
	 *
	 * @param conditionList
	 * @param metedataMap
	 */
	private void convetDbField(List<Condition> conditionList, Map<String, String> metedataMap) {
		Iterator<Condition> iterator = conditionList.iterator();
		while (iterator.hasNext()) {
			Condition condition = iterator.next();
			if (condition instanceof SingleCondition) {
				SingleCondition singleCondition = (SingleCondition) condition;
				if (ArrayUtil.isNotEmpty(singleCondition.getFields())) {
					String metadataEnglish = metedataMap.get(singleCondition.getFields()[0]);
					if (StrUtil.isBlank(metadataEnglish)) {
						iterator.remove();
					} else {
						singleCondition.setFields(new String[]{metadataEnglish});
					}
				}
			} else if (condition instanceof CriteriaCondition) {
				CriteriaCondition criteriaCondition = (CriteriaCondition) condition;
				if (CollectionUtil.isNotEmpty(criteriaCondition.getConditions())) {
					convetDbField(criteriaCondition.getConditions(), metedataMap);
				}
			}
		}
	}

	private void process(CriteriaCondition outCriteria, List<DataConditionDTO> dataConditionDTOList) {
		dataConditionDTOList.stream().forEach(dataConditionDTO -> {
			if (ObjectUtil.isNotNull(dataConditionDTO.getBracket()) && !")".equals(dataConditionDTO.getBracket())) {
				CriteriaCondition condition = addCondition(outCriteria, dataConditionDTO);
				if (CollectionUtil.isNotEmpty(dataConditionDTO.getChildren())) {
					process(condition, dataConditionDTO.getChildren());
				}
			}
		});
	}

	/**
	 * 得到最外层的 and 还是 or 条件
	 * 判断依据 Fields字段 是否为空
	 *
	 * @param dataConditionDTOList
	 * @return
	 */
	private CriteriaCondition getOutCriteria(List<DataConditionDTO> dataConditionDTOList) {
		if (CollectionUtil.isEmpty(dataConditionDTOList)) {
			return null;
		}
		DataConditionDTO firstDataConditionDTO = dataConditionDTOList.stream()
				.filter(dataConditionDTO -> StrUtil.isBlank(dataConditionDTO.getFields()))
				.findFirst().orElse(null);
		if (null == firstDataConditionDTO) {
			throw new IllegalArgumentException("未找到最外层的条件");
		}
		return createCriteriaCondition(firstDataConditionDTO);
	}

	private CriteriaCondition createCriteriaCondition(DataConditionDTO dataConditionDTO) {
		if (null == dataConditionDTO) {
			return CriteriaCondition.and();
		}
		if (LogicalOperator.OR.name().equalsIgnoreCase(dataConditionDTO.getLogicalOperator())) {
			return CriteriaCondition.or();
		} else if (LogicalOperator.AND.name().equalsIgnoreCase(dataConditionDTO.getLogicalOperator())) {
			return CriteriaCondition.and();
		} else {
			throw new IllegalArgumentException("对象DataConditionDTO的属性LogicalOperator不等于 AND或OR ！");
		}
	}

	private CriteriaCondition addCondition(CriteriaCondition condition, DataConditionDTO conditionDTO) {
		if (StrUtil.isBlank(conditionDTO.getFields())) {
			CriteriaCondition incCriteriaCondition = createCriteriaCondition(conditionDTO);
			condition.addCondition(incCriteriaCondition);
			return incCriteriaCondition;
		}
		OperatorKey operatorKey = OperatorKey.get(conditionDTO.getOperatorKey());
		switch (operatorKey) {
			case EQ:
				condition.equal(conditionDTO.getFields(), MetadataTypeEnum.getEnum(conditionDTO.getMetadataType()), conditionDTO.getVals());
				break;
			case NEQ:
				condition.notEqual(conditionDTO.getFields(), MetadataTypeEnum.getEnum(conditionDTO.getMetadataType()), conditionDTO.getVals());
				break;
			case LT:
				condition.lessThan(conditionDTO.getFields(), MetadataTypeEnum.getEnum(conditionDTO.getMetadataType()), conditionDTO.getVals());
				break;
			case LTE:
				condition.lessThanEqual(conditionDTO.getFields(), MetadataTypeEnum.getEnum(conditionDTO.getMetadataType()), conditionDTO.getVals());
				break;
			case GT:
				condition.greaterThan(conditionDTO.getFields(), MetadataTypeEnum.getEnum(conditionDTO.getMetadataType()), conditionDTO.getVals());
				break;
			case GTE:
				condition.greaterThanEqual(conditionDTO.getFields(), MetadataTypeEnum.getEnum(conditionDTO.getMetadataType()), conditionDTO.getVals());
				break;
			case LIKE:
				condition.like(conditionDTO.getFields(), MetadataTypeEnum.getEnum(conditionDTO.getMetadataType()), conditionDTO.getVals());
				break;
			case NLIKE:
				condition.notLike(conditionDTO.getFields(), MetadataTypeEnum.getEnum(conditionDTO.getMetadataType()), conditionDTO.getVals());
				break;
			case IN:
				if (conditionDTO.getVals() instanceof ArrayList) {
					Object[] objs = ArrayUtil.toArray((ArrayList) conditionDTO.getVals(), Object.class);
					condition.in(conditionDTO.getFields(), MetadataTypeEnum.getEnum(conditionDTO.getMetadataType()), objs);
				} else {
					condition.in(conditionDTO.getFields(), MetadataTypeEnum.getEnum(conditionDTO.getMetadataType()), conditionDTO.getVals());
				}
				break;
			case NIN:
				if (conditionDTO.getVals() instanceof ArrayList) {
					Object[] objs = ArrayUtil.toArray((ArrayList) conditionDTO.getVals(), Object.class);
					condition.notIn(conditionDTO.getFields(), MetadataTypeEnum.getEnum(conditionDTO.getMetadataType()), objs);
				} else {
					condition.notIn(conditionDTO.getFields(), MetadataTypeEnum.getEnum(conditionDTO.getMetadataType()), conditionDTO.getVals());
				}
				break;
			case NULL:
				condition.isNull(conditionDTO.getFields());
				break;
			case NNULL:
				condition.isNotNull(conditionDTO.getFields());
				break;
			default:
				if (log.isWarnEnabled()) {
					log.warn("数据库查询不支持的操作符：{}", conditionDTO.getOperatorKey());
				}
				break;
		}
		return condition;
	}

}
