/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.workflow.dto</p>
 * <p>文件名:StartBpmModelProcessDTO.java</p>
 * <p>创建时间:2019年11月14日 下午4:31:00</p>
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
@ApiModel("流程启动参数")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StartBpmModelProcessDTO implements Serializable {

    /**
    *
    */
    private static final long serialVersionUID = 3100613739796101335L;

    @NotBlank(message = "流程编码不能为空")
    @ApiModelProperty(value = "流程编码", required = true, example = "BORROW")
    private String bpmModelCode;

    @ApiModelProperty(value = "租户ID", example = "false")
    private String tenantId;

    @ApiModelProperty(value = "用户ID", example = "false")
    private String userId;

    @ApiModelProperty(value = "业务数据主键ID", required = true, example = "1")
    private String businessKey;

    @ApiModelProperty(value = "是否启动后自动提交", example = "false")
    private boolean autoCommit;

	@ApiModelProperty(value = "候选人", example = "123;456")
	private String assignees;

    @ApiModelProperty(value = "流程参数集合", required = true, example = "表单数据")
    private Map<String, Object> paramMap;

}
