package com.cescloud.saas.archive.api.modular.archivedict.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @ClassName CopyPostDTO
 * @Author zhangxuehu
 * @Date 2020/5/20 14:11
 **/
@Data
@ApiModel("复制到另一模块传参实体")
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class CopyPostDTO {

	@NotNull(message = "源模块id不能为空")
	@ApiModelProperty(value = "源模块id", required = true)
	private Long sourceModuleId;

	@NotBlank(message = "存储表名不能为空")
	@ApiModelProperty(value = "存储表名", required = true)
	private String storageLocate;

	@NotNull(message = "目标模块id不能为空")
	@ApiModelProperty(value = "目标模块id", required = true)
	private List<Long> targetModuleIds;
}
