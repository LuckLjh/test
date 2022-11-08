package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 数据规则实体
 */
@Data
public class AutovalueRuleDTO implements Serializable {

	private static final long serialVersionUID = -6776061361989807588L;

	/**
	 * 拼接规则
	 */
	private List<Rules> splicing;

	/**
	 * 累加规则
	 */
	private List<Rules> flowno;

	/**
	 * 当前日期
	 */
	private List<Rules> nowDate;

	/**
	 * 页号页数联动规则
	 */
	private List<Rules> pagesOrPageNo;
	@Data
	public static class Rules {
		/**
		 * 规则字段的 metadataEnglish
		 */
		private String name;

		/**
		 * 规则字段 name 的具体规则
		 */
		private List<Map<String, Object>> columnRules;
	}
}
