
package com.cescloud.saas.archive.api.modular.archivedict.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 数据字典项
 *
 * @author liudong1
 * @date 2019-03-18 17:44:09
 */
@ApiModel(value = "数据字典分类")
@Data
@TableName("apma_dict")
//@KeySequence("SEQ_APMA_DICT")
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Dict extends Model<Dict> {

    private static final long serialVersionUID = 1L;
    /**
     * 字典项ID
     */
    @ApiModelProperty(value = "字典项ID(按需使用)", example = "1")
    @TableId
    private Long id;


    /**
     * 字典项编码
     */
    @ApiModelProperty(value = "字典项编码", required = true, example = "编码")
    private String dictCode;

    /**
     * 字典项标签
     */
    @ApiModelProperty(value = "字典项标签", required = true, example = "字典项标签")
    @NotBlank(message = "数据字典项标签不能为空")
    @Size(max = 50, message = "字典分类名称过长")
    private String dictLabel;

    /**
     * 字典项描述
     */
    @ApiModelProperty(value = "字典项描述", example = "描述")
    @Size(max = 50, message = "字典项描述过长")
    private String dictDescribe;

    /**
     * 字典项隐藏编码（防止重复）
     */
    @ApiModelProperty(value = "字典项隐藏编码", hidden = true)
    private String dictCodeHidden;

    /**
     * 字典项编码组成值（防止重复）
     */
    @ApiModelProperty(value = "字典项编码组成值", hidden = true)
    private Integer dictCodeNo;
    /**
     * 子典类别（0：系统字段，1：业务字段） false
     */
    @ApiModelProperty(value = "子典类别（0：系统字段，1：业务字段）")
    private Integer dictClass;
    /**
     * 所属租户id
     */
    @ApiModelProperty(value = "所属租户id", hidden = true)
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /**
     * 乐观锁,数据版本号
     */
    @ApiModelProperty(value = "乐观锁", hidden = true)
    @TableField(fill = FieldFill.INSERT)
    @Version
    private Long revision;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人", hidden = true)
    @TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
    private Long createdBy;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", hidden = true)
    @TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    @ApiModelProperty(value = "更新人", hidden = true)
    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间", hidden = true)
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedTime;


    public Dict(@NotBlank(message = "数据字典项标签不能为空") final String dictLabel, String dictDescribe) {
        this.dictLabel = dictLabel;
        this.dictDescribe = dictDescribe;
    }
}
