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
 * @date 2019-08-08 10:00
 */
@ApiModel("同义词组")
@Data
@EqualsAndHashCode
public class SynonymyGroupPostDTO implements Serializable {

	private static final long serialVersionUID = -3687729886623867093L;

	@NotBlank(message = "同义词组名称不能为空")
	@ApiModelProperty(value="同义词组名称",required=true)
	private String synonymyGroup;

}
