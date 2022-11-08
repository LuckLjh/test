/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.stats.dto</p>
 * <p>文件名:StatsTaskDTO.java</p>
 * <p>创建时间:2020年9月27日 上午10:24:54</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.stats.dto;

import java.io.Serializable;

import com.cescloud.saas.archive.api.modular.stats.constant.StatsFilingTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年9月27日
 */
@Data
@AllArgsConstructor
public class StatsTaskDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -374870043214162703L;

    private Long tenantId;

    private StatsFilingTypeEnum statsFilingTypeEnum;

    private String statsField;

    private FilingTypeStatsDTO filingTypeStatsDTO;

}
