/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.stats.entity</p>
 * <p>文件名:ArchiveStats.java</p>
 * <p>创建时间:2020年11月5日 下午2:53:13</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.stats.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年11月5日
 */
@Data
@TableName("apma_archive_stats")
@EqualsAndHashCode(callSuper = true)
@ApiModel("档案统计")
@NoArgsConstructor
public class ArchiveStats extends Model<ArchiveStats> {

    /**
     *
     */
    private static final long serialVersionUID = 8105774842221909349L;

    @TableId
    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty(name = "档案门类分类")
    private String archiveClassType;

    @ApiModelProperty(name = "档案类型编码")
    private String archiveTypeCode;

	@ApiModelProperty(name = "档案类型名称")
	private String archiveTypeName;

    @ApiModelProperty(name = "档案类型整理方式：0、项目；1、案卷；2、以件整理；3、单套制；4、电子全文")
    private Integer filingType;

    @ApiModelProperty(name = "状态")
    private Integer status;

    @ApiModelProperty(name = "全宗号")
    private String fondsCode;

    @ApiModelProperty(name = "年度")
    private String yearCode;

    @ApiModelProperty(name = "保管期限")
    private String retentionPeriod;

    @ApiModelProperty(name = "统计数量")
    private Integer statsAmount;

    @ApiModelProperty(name = "页数（sum(amount_of_pages)）")
    private Integer pageAmount;

    @ApiModelProperty(name = "卷内文件数量（sum(amount_of_files)）")
    private Integer fileAmount;

    @ApiModelProperty(name = "已数字化页数（sum(pdf_page)）")
    private Long digitedPageAmount;

    @ApiModelProperty(name = "已数字化数量（统计pdf_page>0数量）")
    private Long digitedAmount;

    @ApiModelProperty(name = "所属租户id", hidden = true)
    private Long tenantId;

    @ApiModelProperty(name = "更新时间", hidden = true)
    private LocalDateTime updatedTime;
}
