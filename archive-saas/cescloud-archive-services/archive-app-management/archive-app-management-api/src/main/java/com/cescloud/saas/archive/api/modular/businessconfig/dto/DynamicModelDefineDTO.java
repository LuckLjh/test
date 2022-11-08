package com.cescloud.saas.archive.api.modular.businessconfig.dto;


import lombok.Data;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * 动态表字段对应
 *
 * @author 王谷华
 * @date 2021-04-01 16:39:49
 */
@Data
public class DynamicModelDefineDTO implements Serializable {
private static final long serialVersionUID = 1L;

    /**
   * id id
   */
    @ApiModelProperty("id")
    private Long id;
    /**
   * 模板类型 model_type
   */
    @ApiModelProperty("模板类型")
    private Integer modelType;
    /**
   * 模板code model_code
   */
    @ApiModelProperty("模板code")
    private String modelCode;
    /**
   * 业务模板定义ID defined_id
   */
    @ApiModelProperty("业务模板定义ID")
    private Long definedId;
    /**
   * 租户id tenant_id
   */
    @ApiModelProperty("租户id")
    private Long tenantId;
    /**
   * 全宗号 fonds_code
   */
    @ApiModelProperty("全宗号")
    private String fondsCode;
    /**
   * 创建人 created_by
   */
    @ApiModelProperty("创建人")
    private Long createdBy;
    /**
   * 创建时间 created_time
   */
    @ApiModelProperty("创建时间")
    private LocalDateTime createdTime;
  
}
