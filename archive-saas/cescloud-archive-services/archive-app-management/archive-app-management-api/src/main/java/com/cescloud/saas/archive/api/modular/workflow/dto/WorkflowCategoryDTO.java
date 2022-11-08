/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.dto</p>
 * <p>文件名:WorkflowCategoryDTO.java</p>
 * <p>创建时间:2019年10月15日 下午3:51:54</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.workflow.dto;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年10月15日
 */
@ApiModel("模型目录")
@Data
public class WorkflowCategoryDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 8605242802034229082L;

    /** 主键. */
    @ApiModelProperty(value = "主键ID", example = "1")
    private Long id;

    /** 名称. */
    @NotBlank(message = "模型目录名称不能为空")
    @ApiModelProperty(value = "模型目录名称", example = "档案流程")
    private String name;

    /** 租户. */
    @ApiModelProperty(value = "租户ID", example = "1", hidden = true)
    private String tenantId;

    /** 父目录ID. */
    @ApiModelProperty(value = "父目录ID", example = "-1", hidden = true)
    private Long parentId = -1L;
}
