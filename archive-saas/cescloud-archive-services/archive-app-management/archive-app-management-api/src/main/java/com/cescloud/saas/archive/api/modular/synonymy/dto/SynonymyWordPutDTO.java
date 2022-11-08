package com.cescloud.saas.archive.api.modular.synonymy.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author liwei
 * @date 2019-08-08 12:00
 */
@ApiModel("同义词")
@Data
@EqualsAndHashCode
public class SynonymyWordPutDTO implements Serializable {

	private static final long serialVersionUID = 1350694159662049320L;

	@NotNull(message = "同义词id不能为空")
	@ApiModelProperty(value = "同义词主键",required = true)
	private Long id;

	@NotBlank(message = "同义词名称不能为空")
	@ApiModelProperty(value = "同义词名称",required=true)
	private String synonymyWord;
}
