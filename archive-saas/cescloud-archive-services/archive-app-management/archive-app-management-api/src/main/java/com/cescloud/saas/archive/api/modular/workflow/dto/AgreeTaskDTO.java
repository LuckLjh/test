/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.workflow.dto</p>
 * <p>文件名:AgreeTaskDTO.java</p>
 * <p>创建时间:2019年12月13日 下午2:37:11</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.workflow.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年12月13日
 */
@ApiModel("同意参数")
@Data
public class AgreeTaskDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3819943683595108190L;

    @NotBlank(message = "任务ID不能为空")
    @ApiModelProperty(value = "任务ID", required = true, example = "1")
    private String taskId;

    @ApiModelProperty(value = "审批意见", required = true, example = "同意")
    private String comment;

    @ApiModelProperty(value = "表单数据", required = true, example = "表单数据")
    private Map<String, Object> formData;

	@ApiModelProperty(value = "候选人", example = "1234;1233")
	private String assignees;

}
