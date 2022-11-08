/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.archivetype.entity</p>
 * <p>文件名:TemplateType.java</p>
 * <p>创建时间:2020年2月14日 下午4:20:57</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.archivetype.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年2月14日
 */
@ApiModel("档案类型模板")
@Data
@TableName("apma_template_type")
//@KeySequence("SEQ_APMA_TEMPLATE_TYPE")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TemplateType extends Model<TemplateType> {

    /**
     *
     */
    private static final long serialVersionUID = 8007042414467201692L;

    @ApiModelProperty("档案类型模板ID")
    @TableId
    private Long id;

    @NotBlank(message = "档案类型模板名称不能为空")
    @ApiModelProperty("档案类型模板名称")
    private String name;

    @NotBlank(message = "整理方式不能为空")
    @ApiModelProperty("整理方式")
    private String filingType;

    @ApiModelProperty("档案类型模板备注")
    private String remark;

    @ApiModelProperty("排序字段")
    private Integer sortNo;

    @ApiModelProperty("乐观锁")
    @TableField(fill = FieldFill.INSERT)
    @Version
    private Long revision;

    @ApiModelProperty("所属租户id")
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    @ApiModelProperty("创建人")
    @TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
    private Long createdBy;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
    private LocalDateTime createdTime;

    @ApiModelProperty("更新人")
    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedTime;
}
