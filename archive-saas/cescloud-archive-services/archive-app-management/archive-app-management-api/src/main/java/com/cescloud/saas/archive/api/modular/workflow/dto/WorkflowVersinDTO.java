/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.dto</p>
 * <p>文件名:WorkflowVersinDTO.java</p>
 * <p>创建时间:2019年10月18日 上午10:59:55</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.workflow.dto;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年10月18日
 */
@ApiModel("工作流版本")
@Data
public class WorkflowVersinDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -2991578010576133767L;

    /** 主键. */
    @ApiModelProperty(value = "主键ID", example = "1")
    private Long id;

    /** 流程状态：0是未定义 ，1是激活，2是挂起 */
    @ApiModelProperty(value = "流程状态：0是初始化 ，1是激活，2是挂起", example = "0")
    private Integer status = 0;

    /** 引擎中模型id */
    @ApiModelProperty(value = "引擎中模型id", example = "1")
    private String modelId;

    /** 用户自定义的流程版本号. */
    @ApiModelProperty(value = "用户自定义的流程版本号", example = "1", required = true)
    private String version;

    /** 流程的描述信息. */
    @ApiModelProperty(value = "流程版本的描述信息", example = "1")
    private String description;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

}
