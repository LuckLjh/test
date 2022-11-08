/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.stats.constant</p>
 * <p>文件名:StatsConstants.java</p>
 * <p>创建时间:2020年10月20日 下午2:18:06</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.stats.constant;

import lombok.Getter;

/**
 * @author qiucs
 * @version 1.0.0 2020年10月20日
 */
@Getter
public enum StatsTypeEnum {

	/**
	 * 归档数量统计(按照门类统计)
	 */
	FILING_STATS_BY_TYPE_CODE(1, "归档数量统计"),

	/**
	 * 归档数量统计(按照归档部门统计)
	 */
	FILING_STATS_BY_DEPT(2, "归档数量统计"),

	/**
	 * 档案汇总统计(按照年度统计)
	 */
	COLLECTION_STATS_BY_YEAR(3, "档案汇总统计"),
	/**
	 * 档案汇总统计(按照保管期限统计)
	 */
	COLLECTION_STATS_BY_RETENTION(4, "档案汇总统计"),
	/**
	 * 档案数字化率统计(按照目录统计)
	 */
	DIGITIZATION_RATE_BY_CATALOG(5, "馆藏档案数字化率统计表"),
	/**
	 * 档案数字化率统计(按照页数统计)
	 */
	DIGITIZATION_RATE_BY_PAGE_NUMBER(6, "馆藏档案数字化率统计表"),
	/**
	 * 档案移交接收统计
	 */
	TRANSFER_STATS(7, "档案移交接收统计"),
	/**
	 * 档案鉴定统计
	 */
	APPRAISAL_STATS(8, "档案鉴定统计"),
	/**
	 * 档案销毁统计
	 */
	DESTROY_STATS(9, "档案销毁统计"),
	/**
	 * 库房容量统计
	 */
	STORAGE_CAPACITY_STATS(10, "库房容量统计"),
	;


	private final Integer code;

	private final String name;


	StatsTypeEnum(Integer code, String name) {
		this.name = name;
		this.code = code;
	}

	public static StatsTypeEnum getEnum(Integer code){
		StatsTypeEnum[] values = StatsTypeEnum.values();
		for (StatsTypeEnum value : values) {
			if (code.equals(value.code)){
				return value;
			}
		}
		return null;
	}

}
