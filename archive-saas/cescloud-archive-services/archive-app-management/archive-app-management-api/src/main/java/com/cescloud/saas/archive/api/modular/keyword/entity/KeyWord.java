package com.cescloud.saas.archive.api.modular.keyword.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 主题词管理
 *
 * @author qianjiang
 * @date 2019-03-22 18:21:28
 */
@Data
@TableName("apma_key_word")
//@KeySequence("SEQ_APMA_KEY_WORD")
@EqualsAndHashCode(callSuper = true)
public class KeyWord extends Model<KeyWord> {

    private static final long serialVersionUID = 1L;

    /**
     * 主题词id,主键 false
     */
    @TableId
	@ApiModelProperty(value = "主题词主键(按需使用)", example = "1")
    private Long keywordId;

    /**
     * 主题词 false
     */
	@NotBlank(message = "主题词名称不能为空")
	@Size(max = 50, message = "主题词名称长度过长(不得超过100个英文字符)")
	@ApiModelProperty(value = "主题词名称", required = true, example = "主题词")
    private String keyword;

    /**
     * 备注 false
     */
	@ApiModelProperty(value = "主题词备注", example = "备注")
    private String keywordRemark;

	@TableField(fill = FieldFill.INSERT)
	@ApiModelProperty(hidden = true)
	private Long tenantId;
	/**
	 * 乐观锁,数据版本号 true
	 */
	@TableField(fill = FieldFill.INSERT)
	@Version
	@ApiModelProperty(value = "乐观锁标识",hidden = true)
	private Long revision;
	/**
	 * 创建人 true
	 */
	@TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
	@ApiModelProperty(value = "创建人id",hidden = true)
	private Long createdBy;
	/**
	 * 创建时间 true
	 */
	@TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
	@ApiModelProperty(value = "创建时间",hidden = true)
	private LocalDateTime createdTime;
	/**
	 * 更新人 true
	 */
	@TableField(fill = FieldFill.UPDATE)
	@ApiModelProperty(value = "更新人id",hidden = true)
	private Long updatedBy;
	/**
	 * 更新时间 true
	 */
	@TableField(fill = FieldFill.UPDATE)
	@ApiModelProperty(value = "更新时间",hidden = true)
	private LocalDateTime updatedTime;

}
