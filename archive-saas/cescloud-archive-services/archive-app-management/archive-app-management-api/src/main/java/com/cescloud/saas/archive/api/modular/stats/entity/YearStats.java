/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.stats.entity</p>
 * <p>文件名:YearStats.java</p>
 * <p>创建时间:2020年10月22日 下午1:58:46</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.stats.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年10月22日
 */
@Data
@TableName("apma_year_stats")
@EqualsAndHashCode(callSuper = true)
@ApiModel("年报统计")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class YearStats extends Model<YearStats> {

    /**
     *
     */
    private static final long serialVersionUID = 5819425065022937096L;

    @TableId
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

    @ApiModelProperty(name = "第1-114行json值")
    private String lineValueJson;

    /**
     * 所属租户id true
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(name = "所属租户id", hidden = true)
    private Long tenantId;

    /**
     * 创建人 true
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(name = "创建人", hidden = true)
    private Long createdBy;

    /**
     * 创建时间 true
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(name = "创建时间", hidden = true)
    private LocalDateTime createdTime;

	/**
	 * 更新时间
	 */
	@ApiModelProperty("更新时间")
	@TableField(fill = FieldFill.UPDATE)
	private LocalDateTime updatedTime;
}
