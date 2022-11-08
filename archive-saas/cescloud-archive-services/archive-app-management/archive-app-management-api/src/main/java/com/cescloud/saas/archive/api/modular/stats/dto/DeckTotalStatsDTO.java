package com.cescloud.saas.archive.api.modular.stats.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 响应信息主体
 *
 */
@Builder
@ToString
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(description = "统计信息列表及合计主体")
public class DeckTotalStatsDTO<T,RL> implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 后端返回结果的详细业务数据
	 */
	@Getter
	@Setter
	@ApiModelProperty("返回的数据详细")
	private T total;

	@Getter
	@Setter
	@ApiModelProperty("返回的数据详细列表")
	private RL list;


}
