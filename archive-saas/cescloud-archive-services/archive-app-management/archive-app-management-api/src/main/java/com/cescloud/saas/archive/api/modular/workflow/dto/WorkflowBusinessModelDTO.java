/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.workflow.dto</p>
 * <p>文件名:WorkflowBusinessModelDTO.java</p>
 * <p>创建时间:2019年12月3日 下午3:29:46</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.workflow.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author qiucs
 * @version 1.0.0 2019年12月3日
 */
@ApiModel("业务模型")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowBusinessModelDTO implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -2220544845935849241L;

	// 业务编码
	@ApiModelProperty(name = "业务编码（由字符组成，长度不超过20）", required = true)
	private String code;

	// 业务名称
	@ApiModelProperty(name = "业务名称（长度不超过100）", required = true)
	private String name;

	// 业务描述
	@ApiModelProperty(name = "业务描述（长度不超过255）")
	private String description;

	// 是否为公共业务
	@ApiModelProperty(name = "是否为公共业务")
	private boolean common;

	// 模板顺序
	@ApiModelProperty(name = "模板顺序")
	private Integer sortNo;

	// 租户ID
	@ApiModelProperty(name = "租户ID")
	private String tenantId;

}
