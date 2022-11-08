
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
 * 数据字典值
 *
 * @author liudong1
 * @date 2019-03-18 17:47:15
 */
@Data
@TableName("apma_dict_item")
//@KeySequence("SEQ_APMA_DICT_ITEM")
@EqualsAndHashCode(callSuper = true)
@ApiModel("数据字典值")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DictItem extends Model<DictItem> {

    private static final long serialVersionUID = 1L;

    /**
     * 字典值ID
     */
    @ApiModelProperty("字典值ID")
    @TableId
    private Long id;

    /**
     * 字典项编码
     */
	@ApiModelProperty("字典项编码")
    private String dictCode;

    /**
     * 字典值编码
     */
	@ApiModelProperty("字典值编码")
    @NotBlank(message = "数据字典值编码不能为空")
	@Size(max = 100, message = "数据字典值过长（不得超过100个字符）")
    private String itemCode;

    /**
     * 字典值标签
     */
	@ApiModelProperty("字典值标签")
	@NotBlank(message = "字典值标签不能为空")
	@Size(max = 100, message = "字典值标签过长（不得超过100个字符）")
	private String itemLabel;

    /**
     * 字典值的具体值
     */
	@ApiModelProperty("字典值")
    private String itemValue;

    /**
     * 字典项描述
     */
	@ApiModelProperty("字典项描述")
    private String itemDescribe;

    /**
     * 排序
     */
	@ApiModelProperty("排序")
    private Integer sortNo;

    /**
     * 所属租户id
     */
	@ApiModelProperty("所属租户id")
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /**
     * 乐观锁,数据版本号
     */
	@ApiModelProperty("乐观锁")
    @TableField(fill = FieldFill.INSERT)
    @Version
    private Long revision;

    /**
     * 创建人
     */
	@ApiModelProperty("创建人")
    @TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
    private Long createdBy;

    /**
     * 创建时间
     */
	@ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
	@ApiModelProperty("更新人")
    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    /**
     * 更新时间
     */
	@ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedTime;


    public DictItem(final String dictCode, @NotBlank(message = "数据字典值编码不能为空") final String itemCode,
        @NotBlank(message = "数据字典值名称不能为空") final String itemLabel, String itemDescribe,String itemValue) {
        this.dictCode = dictCode;
        this.itemCode = itemCode;
        this.itemLabel = itemLabel;
		this.itemDescribe = itemDescribe;
		this.itemValue = itemValue;
    }
}
