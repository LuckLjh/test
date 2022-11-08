/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.stats.constant</p>
 * <p>文件名:StatsFilingTypeEnum.java</p>
 * <p>创建时间:2020年9月25日 下午1:41:30</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.stats.constant;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年9月25日
 */
public enum StatsFilingTypeEnum {

    PROJECT(0, "项目整理"),
	FOLDER(1, "以卷整理"),
	ONE(2, "以件整理"),
	SINGAL(3, "单套制"),
	DOCUMENT(4, "电子全文"),
	NONE(null,"none"),
	NO_YEAR_CODE(null,"无年度"),
	NO_DEPT(null,"无部门"),
	NO_RETENTION_PERIOD(null,"无保管期限"),
	;

    private final Integer code;

    private final String name;

    private StatsFilingTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
