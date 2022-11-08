/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.stats.dto</p>
 * <p>文件名:DigitTableFilingTypeDataDTO.java</p>
 * <p>创建时间:2020年10月20日 下午1:56:56</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.stats.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年10月20日
 */
@Api("数字化统计（表格）")
@Data
public class DigitTableFilingTypeDataDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8975233879766546220L;

    @ApiModelProperty(name = "统计维度集合，如保管期限，则key为保管期限值（如永久），value为保管期限统计值（如永久的统计值30件或卷）")
    private final Map<String, Integer> data = new HashMap<String, Integer>();

    @ApiModelProperty(name = "总数（卷/件）或应该数字化目录数")
    private Integer totalAmount = 0;

    @ApiModelProperty(name = "应数字化数量（总页数）/总时长")
    private Integer totalPageAmonut = null;

    @ApiModelProperty(name = "卷内文件总数")
    private Integer totalFileAmount = null;

    @ApiModelProperty(name = "按目录统计：已数字化目录数量；按页数统计：已数字化页数数量")
    private Long totalDigitedAmount = null;

    public void addPageAmount(Integer pageAmount) {
        if (null == this.totalPageAmonut) {
            this.totalPageAmonut = pageAmount;
        } else {
            this.totalPageAmonut += pageAmount;
        }
    }

    public void addFileAmount(Integer fileAmount) {
        if (null == this.totalFileAmount) {
            this.totalFileAmount = fileAmount;
        } else {
            this.totalFileAmount += fileAmount;
        }
    }

    public void addDigitAmount(Long digitAmount) {
        if (null == this.totalDigitedAmount) {
            this.totalDigitedAmount = digitAmount;
        } else {
            this.totalDigitedAmount += digitAmount;
        }
    }

    public void putStatsData(String statsTitle, Integer statsAmount) {
        if (data.containsKey(statsTitle)) {
            data.put(statsTitle, data.get(statsTitle) + statsAmount);
        } else {
            data.put(statsTitle, statsAmount);
        }

        this.totalAmount += statsAmount;
    }
}
