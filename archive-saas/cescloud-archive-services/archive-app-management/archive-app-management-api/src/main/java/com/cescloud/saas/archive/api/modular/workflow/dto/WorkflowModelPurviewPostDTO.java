/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.workflow.dto</p>
 * <p>文件名:WorkflowModelPurviewPostDTO.java</p>
 * <p>创建时间:2019年12月2日 下午4:27:14</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.workflow.dto;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年12月2日
 */
@ApiModel("模型可见设置")
@Data
public class WorkflowModelPurviewPostDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 8119868531977166072L;

    @ApiModelProperty(value = "业务编码", example = "borrow")
    private String businessCode;

    @ApiModelProperty(value = "流程编码", example = "borrow")
    private String bpmModelCode;

    @ApiModelProperty(value = "部门ID集合", example = "")
    private List<KeyValueDTO<Long, String>> deptList;

    @ApiModelProperty(value = "用户ID集合", example = "borrow")
    private List<KeyValueDTO<Long, String>> userList;
}
