/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.workflow.dto</p>
 * <p>文件名:CompleteProcessDTO.java</p>
 * <p>创建时间:2019年11月14日 下午5:04:44</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.workflow.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年11月14日
 */
@ApiModel("提交流程参数")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompleteProcessDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3819943683595108190L;

    @NotBlank(message = "流程实例ID不能为空")
    @ApiModelProperty(value = "流程实例ID", required = true, example = "1")
    private String processInstanceId;

    @ApiModelProperty(value = "是否同意", required = true, example = "true")
    private boolean agreement;

    @ApiModelProperty(value = "审批意见", required = true, example = "同意")
    private String comment;

    @ApiModelProperty(value = "流程参数集合", required = true, example = "表单数据")
    private Map<String, Object> paramMap;

	@ApiModelProperty(value = "候选人", example = "1234;1233")
	private String assignees;

}
