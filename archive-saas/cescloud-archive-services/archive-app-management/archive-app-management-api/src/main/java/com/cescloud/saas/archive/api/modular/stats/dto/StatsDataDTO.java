/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.stats.dto</p>
 * <p>文件名:StatsDataDTO.java</p>
 * <p>创建时间:2020年10月10日 上午10:44:33</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.stats.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年10月10日
 */
@Data
public class StatsDataDTO<FilingTypeDataDTO> implements Serializable {

    /**
    *
    */
    private static final long serialVersionUID = -4709919734064762577L;

    @ApiModelProperty(name = "档案类型编码/年度")
    private String showTitle;

    @ApiModelProperty(name = "按卷整理")
    private FilingTypeDataDTO folderData;

    @ApiModelProperty(name = "按件整理")
    private FilingTypeDataDTO fileData;

    public FilingTypeDataDTO folder(Class<FilingTypeDataDTO> clz) {
        if (null == folderData) {
            try {
                folderData = clz.newInstance();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return folderData;
    }

    public FilingTypeDataDTO file(Class<FilingTypeDataDTO> clz) {
        if (null == fileData) {
            try {
                fileData = clz.newInstance();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return fileData;
    }

}
