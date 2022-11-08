package com.cescloud.saas.archive.api.modular.documentnoauthor.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author LS
 * @date 2021/6/23
 */
@Data
public class DocumentNumberAuthorDTO implements Serializable {

	private static final long serialVersionUID = -4197199054292046653L;

	@TableId
	@ApiModelProperty(value = "主键ID")
	private Long id;

	@NotBlank(message = "全宗号不能为空")
	@ApiModelProperty(value = "全宗号")
	private String fondsCode;

	@NotBlank(message = "全宗名称不能为空")
	@ApiModelProperty(value = "全宗名称")
	private String fondsName;

	@NotBlank(message = "文号不能为空")
	@Length(max = 100, message = "文号输入大小超出限制")
	@ApiModelProperty(value = "文号")
	private String documentNumber;

	@NotBlank(message = "责任者不能为空")
	@Length(max = 200, message = "责任者输入大小超出限制")
	@ApiModelProperty(value = "责任者")
	private String author;

}
