/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.workflow.dto</p>
 * <p>文件名:DeliverTaskDTO.java</p>
 * <p>创建时间:2019年12月17日 下午1:17:23</p>
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
 * @version 1.0.0 2019年12月17日
 */
@ApiModel("转发任务参数")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliverTaskDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -4451776003405894283L;

    @NotBlank(message = "任务ID不能为空")
    @ApiModelProperty(value = "任务ID", required = true, example = "1")
    private String taskId;

    @ApiModelProperty(value = "转发用户IDS", required = true, example = "1")
    private String[] toUserIds;

    @ApiModelProperty(value = "转发意见", example = "转发")
    private String comment;
}
