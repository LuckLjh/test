/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.stats.dto</p>
 * <p>文件名:YearStatsQueryDTO.java</p>
 * <p>创建时间:2020年11月20日 上午11:15:35</p>
 * <p>作者: qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.stats.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * 年度统计查询数据
 *
 * @author qiucs
 * @version 1.0.0 2020年11月20日
 */
@Data
public class YearStatsQueryDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -2632877739231092216L;

    private Integer filingType;

    private String retentionPeriod;

    private Integer statsAmount;

}
