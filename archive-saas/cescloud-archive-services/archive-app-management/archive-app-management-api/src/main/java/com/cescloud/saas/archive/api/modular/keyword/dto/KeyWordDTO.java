package com.cescloud.saas.archive.api.modular.keyword.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @ClassName KeyWordDTO
 * @Author zhangxuehu
 * @Date 2019/10/17 6:03 下午
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KeyWordDTO implements Serializable {
	/**
	 * 主题词 false
	 */
	@ApiModelProperty(value = "关键字(因同名，也做检索)",required = true,example = "主题词")
	private String keyword;

	/**
	 * 备注 false
	 */
	@ApiModelProperty(value = "关键字备注",example = "备注")
	private String keywordRemark;

}
