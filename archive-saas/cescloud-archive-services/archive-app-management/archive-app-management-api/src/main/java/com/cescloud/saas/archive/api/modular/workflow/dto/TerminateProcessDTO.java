/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.workflow.dto</p>
 * <p>文件名:TerminateProcessDTO.java</p>
 * <p>创建时间:2019年12月16日 上午10:14:45</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.workflow.dto;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年12月16日
 */
@ApiModel("终止流程参数")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TerminateProcessDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3819943683595108190L;

    @NotBlank(message = "流程实例ID不能为空")
    @ApiModelProperty(value = "流程实例ID", required = true, example = "1")
    private String processInstanceId;

    @ApiModelProperty(value = "终止意见", required = true, example = "不同意")
    private String comment;
}
