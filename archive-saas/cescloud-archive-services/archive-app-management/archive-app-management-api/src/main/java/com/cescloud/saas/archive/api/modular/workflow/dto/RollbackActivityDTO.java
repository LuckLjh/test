/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.workflow.dto</p>
 * <p>文件名:RollbackActivityDTO.java</p>
 * <p>创建时间:2019年11月14日 下午5:15:12</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.workflow.dto;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年11月14日
 */
@ApiModel("退回指定节点流程参数")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RollbackActivityDTO extends RollbackPreviousDTO {

    /**
     *
     */
    private static final long serialVersionUID = -3590281478235357654L;

    @NotBlank(message = "节点ID不能为空")
    @ApiModelProperty(value = "节点ID", required = true, example = "1")
    private String activityId;

}
