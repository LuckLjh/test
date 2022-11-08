package com.cescloud.saas.archive.api.modular.filingscope.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @ClassName FilingScopeType
 * @Author zhangxuehu
 * @Date 2020/6/29 11:14
 **/
@ApiModel("归档范围信息")
@Data
@TableName("apma_filing_scope_type")
//@KeySequence("SEQ_APMA_FILING_SCOPE_TYPE")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilingScopeType extends Model<FilingScopeType> {

    @ApiModelProperty("主键id")
    @TableId
    private Long id;

    @ApiModelProperty(value = "父节点的id")
    private Long parentId;

    @ApiModelProperty(value = "序号")
    private String catalogueNo;

    @ApiModelProperty("归档范围,可以是多个关键词，也可以是一段描述")
    private String filingScope;

    @ApiModelProperty("保管期限,取值范围：永久、定期30年、定期10年")
    private String retentionPeriod;

    @ApiModelProperty("处置方式：续存，销毁")
    private String disposalMethod;

    @ApiModelProperty("部门id")
    private Long deptId;

    @ApiModelProperty("部门名称")
    private String deptName;

	/**
	 * 年度 year_code
	 */
	@ApiModelProperty("年度")
	private Integer yearCode;

    /**
     * 所属租户id true
     */
    @ApiModelProperty("所属租户id")
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /**
     * 乐观锁 true
     */
    @ApiModelProperty("乐观锁")
    @TableField(fill = FieldFill.INSERT)
    private Long revision;
    /**
     * 创建人 true
     */
    @ApiModelProperty("创建人")
    @TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
    private Long createdBy;
    /**
     * 创建时间 true
     */
    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
    private LocalDateTime createdTime;
    /**
     * 更新人 true
     */
    @ApiModelProperty("更新人")
    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;
    /**
     * 更新时间 true
     */
    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedTime;
}
