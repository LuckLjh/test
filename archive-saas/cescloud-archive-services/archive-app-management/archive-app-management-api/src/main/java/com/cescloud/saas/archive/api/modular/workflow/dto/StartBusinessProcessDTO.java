/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.workflow.dto</p>
 * <p>文件名:StartBusinessProcessDTO.java</p>
 * <p>创建时间:2019年12月5日 下午2:38:57</p>
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
import java.util.List;
import java.util.Map;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年12月5日
 */
@ApiModel("流程启动参数")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StartBusinessProcessDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1441487247634098747L;

    @NotBlank(message = "业务编码不能为空")
    @ApiModelProperty(value = "业务编码", required = true, example = "BORROW")
    private String businessCode;

    @ApiModelProperty(value = "业务数据主键ID", required = true, example = "1")
    private String businessKey;

    @ApiModelProperty(value = "是否启动后自动提交", example = "false")
    private boolean autoCommit;

    @ApiModelProperty(value = "用户ID", example = "false")
    private String userId;

    @ApiModelProperty(value = "部门ID集合（用于获取对应流程）", required = true)
    private List<String> deptIdList;

	@ApiModelProperty(value = "候选人", example = "1234;1233")
	private String assignees;

    @ApiModelProperty(value = "流程参数集合", required = true, example = "表单数据")
    private Map<String, Object> paramMap;

    @ApiModelProperty(value = "租户ID", example = "false")
    private String tenantId;

}
