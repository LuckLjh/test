/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.workflow.dto</p>
 * <p>文件名:RollbackPreviousDTO.java</p>
 * <p>创建时间:2019年11月14日 下午5:11:34</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.workflow.dto;

import java.io.Serializable;
import java.util.Map;

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
 * @version 1.0.0 2019年11月14日
 */
@ApiModel("退回上一节点参数")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RollbackPreviousDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8857921822140649305L;

    @NotBlank(message = "任务ID不能为空")
    @ApiModelProperty(value = "任务ID", required = true, example = "1")
    private String taskId;

    @ApiModelProperty(value = "退回意见", required = true, example = "不同意")
    private String comment;

    @ApiModelProperty(value = "流程参数集合", required = true, example = "表单数据")
    private Map<String, Object> paramMap;
}
