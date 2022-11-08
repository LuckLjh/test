/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.stats.dto</p>
 * <p>文件名:ArchiveTypeStatsDTO.java</p>
 * <p>创建时间:2020年9月25日 上午9:45:37</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.stats.dto;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年9月25日
 */
@ApiModel("档案门类统计")
@Data
public class ArchiveTypeStatsDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -2287481915492906747L;

    @ApiModelProperty("租户ID")
    private Long tenantId;

    @ApiModelProperty("项目整理档案门类集合")
    private final List<FilingTypeStatsDTO> projectArchiveTypeList = Lists.newArrayList();

    @ApiModelProperty("以卷整理档案门类集合")
    private final List<FilingTypeStatsDTO> folderArchiveTypeList = Lists.newArrayList();

    @ApiModelProperty("以件整理档案门类集合")
    private final List<FilingTypeStatsDTO> fileArchiveTypeList = Lists.newArrayList();

    @ApiModelProperty("单套制档案门类集合")
    private final List<FilingTypeStatsDTO> singleArchiveTypeList = Lists.newArrayList();

    @ApiModelProperty("电子全文档案门类集合")
    private final List<FilingTypeStatsDTO> documentArchiveTypeList = Lists.newArrayList();

}
