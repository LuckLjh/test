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
public class SynonymyWordPostDTO implements Serializable {

	private static final long serialVersionUID = -3427172140151060397L;

	@NotBlank(message = "同义词名称不能为空")
	@ApiModelProperty(value = "同义词名称",required=true)
	private String synonymyWord;

}
