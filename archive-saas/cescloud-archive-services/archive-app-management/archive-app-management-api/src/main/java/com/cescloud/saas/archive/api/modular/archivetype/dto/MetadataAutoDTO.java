package com.cescloud.saas.archive.api.modular.archivetype.dto;
/**
@author xaz
@date 2019/6/20 - 14:11
**/

import com.baomidou.mybatisplus.annotation.*;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveColumnRule;
import com.cescloud.saas.archive.api.modular.archivetype.entity.MetadataSource;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MetadataAutoDTO {
    /**
     * 自动生成元数据id,主键 false
     */
    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("类型：0、累加，1、档案字段组成拼接")
    private Integer type;
    /**
     * 档案层级存储表 false
     */
    @ApiModelProperty("档案层级存储表")
    private String storageLocate;

    /**
     * 元数据id
     */
    @ApiModelProperty("元数据id")
    private Long metadataId;
    /**
     * 补零标识，1:代表补零 0：代表不补零 false
     */
    @ApiModelProperty("补零标识，1:代表补零 0：代表不补零")
    private Boolean flagZero;
    /**
     * 所属租户ID true
     */
    @ApiModelProperty("所属租户id")
    private Long tenantId;
    /**
     * 乐观锁 true
     */
    @ApiModelProperty("乐观锁")
    @Version
    private Long revision;
    /**
     * 创建人 true
     */
    @ApiModelProperty("创建人")
    private Long createdBy;
    /**
     * 创建时间 true
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createdTime;
    /**
     * 更新人 true
     */
    @ApiModelProperty("更新人")
    private Long updatedBy;
    /**
     * 更新时间 true
     */
    @ApiModelProperty("更新时间")
    private LocalDateTime updatedTime;

    /**
     * 累加规则
     */
    @ApiModelProperty("累加规则")
    private List<MetadataSource> metadataSources;

    /**
     * 档案字段组成规则
     */
    @ApiModelProperty("档案字段组成规则")
    private List<ArchiveColumnRule> archiveColumnRules;


}
