/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.stats.dto</p>
 * <p>文件名:YearStatsDTO.java</p>
 * <p>创建时间:2020年10月23日 下午1:53:09</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.stats.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年10月23日
 */
@Data
public class YearStatsDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6202113943548386275L;

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty(name = "全宗号")
    private String fondsCode;

    @ApiModelProperty(name = "年度")
    private String yearCode;

    @ApiModelProperty(name = "单位名称")
    private String unitName;

    @ApiModelProperty(name = "单位类别代码")
    private String unitCatalog;

    @ApiModelProperty(name = "统一社会信用代码")
    private String unifiedSocialCreditCode;

    @ApiModelProperty(name = "联系电话")
    private Integer telephone;

    @ApiModelProperty(name = "单位地址")
    private String unitAddress;

    @ApiModelProperty(name = "单位负责人")
    private String unitManager;

    @ApiModelProperty(name = "填表人")
    private String fillingUser;

    @ApiModelProperty(name = "报出年份")
    private String reportYear;

    @ApiModelProperty(name = "报出月份")
    private String reportMonth;

    @ApiModelProperty(name = "报出日")
    private String reportDay;

    @ApiModelProperty(name = "第1-114行json值，key为行号，value是行对应的数量")
    private Map<String, Integer> lineValueMap;

    @ApiModelProperty(name = "所属租户id", hidden = true)
    private Long tenantId;

    @ApiModelProperty(name = "乐观锁", hidden = true)
    private Long revision;

    @ApiModelProperty(name = "创建人", hidden = true)
    private Long createdBy;

    @ApiModelProperty(name = "创建时间", hidden = true)
    private LocalDateTime createdTime;

    @ApiModelProperty(name = "更新人", hidden = true)
    private Long updatedBy;

    @ApiModelProperty(name = "更新时间", hidden = true)
    private LocalDateTime updatedTime;

}
