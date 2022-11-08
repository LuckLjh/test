package com.cescloud.saas.archive.api.modular.documentnoauthor.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author LS
 * @date 2021/6/23
 */
@Data
@TableName("apma_document_number_author")
public class DocumentNumberAuthor implements Serializable {

	private static final long serialVersionUID = 8832667188235087932L;
	@TableId
	@ApiModelProperty(value = "主键ID")
	private Long id;

	@ApiModelProperty(value = "全宗号")
	private String fondsCode;

	@ApiModelProperty(value = "全宗名称")
	private String fondsName;

	@ApiModelProperty(value = "文号")
	private String documentNumber;

	@ApiModelProperty(value = "责任者")
	private String author;

	@TableField(fill = FieldFill.INSERT)
	@ApiModelProperty(value = "租户ID", hidden = true)
	private Long tenantId;

	@TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
	@ApiModelProperty(value = "创建人", hidden = true)
	private Long createdBy;

	@TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
	@ApiModelProperty(value = "创建时间", hidden = true)
	private LocalDateTime createdTime;

	@TableField(fill = FieldFill.UPDATE)
	@ApiModelProperty(value = "更新人", hidden = true)
	private Long updatedBy;

	@TableField(fill = FieldFill.UPDATE)
	@ApiModelProperty(value = "更新时间", hidden = true)
	private LocalDateTime updatedTime;

	@ApiModelProperty("乐观锁")
	@TableField(fill = FieldFill.INSERT)
	@Version
	private Long revision;

}
