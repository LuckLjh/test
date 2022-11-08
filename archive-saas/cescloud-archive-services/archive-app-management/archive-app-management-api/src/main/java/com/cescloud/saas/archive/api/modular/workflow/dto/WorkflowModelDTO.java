/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.dto</p>
 * <p>文件名:WorkflowModelDTO.java</p>
 * <p>创建时间:2019年10月15日 下午5:39:04</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.workflow.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年10月15日
 */
@ApiModel("工作流模型")
@Data
public class WorkflowModelDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7156417152583682588L;

    @ApiModelProperty(value = "主键ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "编码", example = "borrow")
    private String code;

    @ApiModelProperty(value = "名称", example = "借阅流程")
    @NotBlank(message = "名称不能为空")
    private String name;

    @ApiModelProperty(value = "流程状态：0-禁用，1-启用", example = "1")
    private Integer status;

    @ApiModelProperty(value = "业务编码：一个业务标识可以有多个流程", example = "borrow")
    private String businessCode;

    @ApiModelProperty(value = "创建时间", example = "borrow")
    private Date createTime;

    @ApiModelProperty(value = "租户ID", example = "1", hidden = true)
    private String tenantId;

    @ApiModelProperty(value = "描述", example = "")
    private String description;

    @ApiModelProperty(value = "版本号（激活的版本号或是最新版本号）")
    private String confBaseVersion;

    @ApiModelProperty(value = "版本号对应的流程图模型ID")
    private String confBaseModelId;

    @ApiModelProperty(value = "版本对的状态")
    private Integer confBaseStatus;

    @ApiModelProperty(value = "可见范围集合")
    private List<WorkflowModelPurviewDTO> modelPerviewList;

	@ApiModelProperty(value = "icon")
	private String icon;
}
