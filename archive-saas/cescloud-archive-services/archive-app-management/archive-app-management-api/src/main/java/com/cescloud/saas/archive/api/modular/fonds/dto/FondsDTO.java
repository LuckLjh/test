package com.cescloud.saas.archive.api.modular.fonds.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * @ClassName FondsDTO
 * @Author zhangxuehu
 * @Date 2019/10/17 4:25 下午
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FondsDTO implements Serializable {

	/**
	 * 全宗号 false
	 */

	@ApiModelProperty(value = "全宗代码", required = true,example = "QZGL")
	private String fondsCode;
	/**
	 * 全宗名称 false
	 */
	@ApiModelProperty(value = "全宗名称", required = true,example = "全宗名称")
	private String fondsName;
	/**
	 * 描述信息 false
	 */
	@ApiModelProperty(value = "描述",example = "描述")
	private String description;

	/**
	 * 检索关键字 false
	 */
	@ApiModelProperty(value = "检索关键字",example = "检索关键字")
	private String keyword;

	@ApiModelProperty(value = "全宗代码", required = true,example = "QZGL")
	private Set<String> fondsCodes;

	/**
	 * 每页显示条数，默认 10
	 */
	private long size = 10;

	/**
	 * 当前页
	 */
	private long current = 1;

}
