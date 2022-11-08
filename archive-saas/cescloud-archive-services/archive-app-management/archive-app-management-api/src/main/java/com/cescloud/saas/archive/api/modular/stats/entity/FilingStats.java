/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.stats.entity</p>
 * <p>文件名:FilingStats.java</p>
 * <p>创建时间:2020年10月14日 下午2:26:09</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.stats.entity;

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
 * @version 1.0.0 2020年10月14日
 */
@Data
@TableName("apma_filing_stats")
@EqualsAndHashCode(callSuper = true)
@ApiModel("归档率统计")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilingStats extends Model<FilingStats> {

    /**
     *
     */
    private static final long serialVersionUID = -8113913557303189042L;

    @TableId
    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty(name = "档案类型编码")
    private String archiveTypeCode;

	@ApiModelProperty(name = "档案类型名称")
	private String archiveTypeName;

    @ApiModelProperty(name = "全宗号")
    private String fondsCode;

    @ApiModelProperty(name = "年度")
    private String yearCode;

    @ApiModelProperty(name = "归档部门")
    private Long filingDeptId;

    @ApiModelProperty(name = "统计值")
    private Integer statsAmount;

    @ApiModelProperty(name = "所属租户id", hidden = true)
    private Long tenantId;

    @ApiModelProperty(name = "更新时间", hidden = true)
    private LocalDateTime updatedTime;

	@ApiModelProperty(name = "保管期限")
	private String retentionPeriod;
}
