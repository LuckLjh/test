package com.cescloud.saas.archive.api.modular.archivetree.dto;

import com.cescloud.saas.archive.api.modular.archivetree.entity.ArchiveTree;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 *
 *
 * @author qiucs
 * @version 1.0.0 2019年4月17日
 */
@ApiModel("档案树新增")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArchiveTreePutDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -2109718926124117920L;

    /**
     * 树节点基本信息
     */
    @ApiModelProperty(value = "树节点基本信息", required = true)
    private ArchiveTree entity;

    /**
     * 批量新增信息
     */
    @ApiModelProperty(value = "批量新增信息")
    private List<Map<String, Object>> extendValList;

}
