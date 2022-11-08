/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.workflow.dto</p>
 * <p>文件名:WorkflowModelPurviewDTO.java</p>
 * <p>创建时间:2019年12月9日 下午1:49:58</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.workflow.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年12月9日
 */
@ApiModel("模型可见范围")
@Data
public class WorkflowModelPurviewDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6886758944178809476L;

    @ApiModelProperty(value = "ID", example = "1")
    private Long id;

    // 业务编码
    @ApiModelProperty(value = "业务编码", example = "borrow")
    private String businessCode;

    // 模型编码
    @ApiModelProperty(value = "模型编码", example = "borrow")
    private String bpmModelCode;

    // 对象ID：用户、部门或角色ID， 与object_type对应
    @ApiModelProperty(value = "对象ID", example = "1")
    private Long objectId;

    // 对象名称：用户、部门或角色名称， 与object_type对应
    @ApiModelProperty(value = "对象名称：用户、部门或角色名称， 与object_type对应", example = "张二")
    private String objectName;

    // 对象类型：u-用户，d-部门，r-角色
    @ApiModelProperty(value = "对象类型：u-用户，d-部门，r-角色", example = "d")
    private String objectType;

}
