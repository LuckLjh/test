/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.workflow.dto</p>
 * <p>文件名:WorkflowBusinessModelSyncDTO.java</p>
 * <p>创建时间:2019年12月3日 下午3:29:18</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.workflow.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年12月3日
 */
@ApiModel("业务模型同步DTO")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowBusinessModelSyncDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5175357889996884002L;

    @ApiModelProperty(name = "业务模型", required = true)
    private WorkflowBusinessModelDTO businessModel;

    @ApiModelProperty(name = "业务字段集合", required = true)
    private List<WorkflowBusinessMetadataDTO> businessMetadataList;

    @ApiModelProperty(name = "租户ID", required = true)
    private String tenantId;

}
