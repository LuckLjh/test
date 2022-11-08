/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.stats.dto</p>
 * <p>文件名:StatsDataDTO.java</p>
 * <p>创建时间:2020年10月10日 上午10:44:33</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.stats.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年10月10日
 */
@Builder
@ToString
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(description = "档案新增信息明细")
public class DeckNewDataDTO implements Serializable {
	@ApiModelProperty(name = "统计组标题展示")
	private String groupTitle;
	@ApiModelProperty(name = "统计组对应的值")
	private int groupValue;
	@ApiModelProperty(name = "档案新增数量")
	private int newStatsAmount;
	@ApiModelProperty(name = "文件新增数量")
	private int newPageAmount;
	@ApiModelProperty(name = "文件新增大小")
	private int newDigitedAmount;
}
