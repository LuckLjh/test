/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.workflow.dto</p>
 * <p>文件名:WorkflowBusinessMetadataDTO.java</p>
 * <p>创建时间:2019年12月3日 下午3:32:20</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.workflow.dto;

import com.cescloud.saas.archive.api.modular.workflow.constant.WorkflowConstants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年12月3日
 */
@ApiModel("业务字段")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowBusinessMetadataDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7442416663211836587L;

    // 业务编码（BusinessModel.code）
    //@ApiModelProperty(name = "业务编码（由字符组成，长度不超过20）", required = true)
    //private String businessCode;

    // 业务类型：master-业务主表/detail-业务明细/document-业务附件
    @ApiModelProperty(name = "业务类型：master-业务主表/detail-业务明细/document-业务附件", required = true)
    private String businessType = WorkflowConstants.BusinessType.MATSER;

    @ApiModelProperty(name = "字段中文名称", required = true)
    private String metadataChinese;

    @ApiModelProperty(name = "字段英文名称", required = true)
    private String metadataEnglish;

    @ApiModelProperty(name = "字段类型", required = true)
    private String metadataType;

    @ApiModelProperty(name = "字段长度", required = true)
    private Integer metadataLength;

    @ApiModelProperty(name = "小数点位数", required = false)
    private Integer metadataDotLength;

    @ApiModelProperty(name = "字段描述信息", required = false)
    private String metadataDescription;

    //@ApiModelProperty(name = "排序号", required = false)
    //private Integer sortNo;

    // 是否作为条件字段
    @ApiModelProperty(name = "是否作为条件字段")
    private boolean condition;

    // 是否为部门字段
    @ApiModelProperty(name = "是否为部门字段")
    private boolean dept;

    // 租户ID，-1表示公共的
    @ApiModelProperty(name = "租户ID，-1表示公共的")
    private String tenantId;

}
