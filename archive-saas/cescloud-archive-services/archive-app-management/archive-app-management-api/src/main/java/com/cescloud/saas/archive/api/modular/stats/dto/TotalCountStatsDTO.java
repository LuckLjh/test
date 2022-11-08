/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.stats.dto</p>
 * <p>文件名:TotalCountStatsDTO.java</p>
 * <p>创建时间:2020年12月18日 下午3:55:47</p>
 * <p>作者: qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.stats.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年12月18日
 */
@Api("统计窗口数量")
@Data
public class TotalCountStatsDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8037551160029537202L;

    @ApiModelProperty("项目数量")
    private int projectAmount;

    @ApiModelProperty("案卷数量")
    private int folderAmount;

    @ApiModelProperty("卷内数量")
    private int fileAmount;

    @ApiModelProperty("以件整理数量")
    private int oneSingleAmount;

    @ApiModelProperty("电子文件数量")
    private int documentAmount;

    @ApiModelProperty("电子文件大小")
    private BigInteger fileSize;

    @ApiModelProperty("PDF页数")
    private int pageAmount;

    @ApiModelProperty("已数字化PDF页数")
    private int digitedPageAmount;

    @ApiModelProperty("档案总数")
    private int totalArchive;

    @ApiModelProperty("档案数字化率")
    private String digitedPercent;

    @ApiModelProperty("电子文件大小")
    private String fileSizeFormat;

    private static String[] units = { "B", "KB", "MB", "GB", "TB", "EB" };

    public int getTotalArchive() {
        return folderAmount + fileAmount + oneSingleAmount;
    }

    public String getDigitedPercent() {
        if (0 == digitedPageAmount) {
            return "0%";
        }
        if (0 == pageAmount) {
            return "0%";
        }
        return String.format("%d%%", digitedPageAmount * 100 / pageAmount);
    }

    public String getFileSizeFormat() {
        return formatFileSize(0);
    }

    // B, KB, MB, GB, TB, EB
    private String formatFileSize(int unitIndex) {
	    BigInteger unitSize = BigDecimal.valueOf(Math.pow(1024, unitIndex)).toBigInteger();
	    final double s = fileSize.doubleValue()/unitSize.doubleValue();
	    if (s > 1024) {
            return formatFileSize(++unitIndex);
        }
        return String.format("%.2f", s) + units[unitIndex];
    }

    public static void main(String[] args) {
        final TotalCountStatsDTO totalCountStatsDTO = new TotalCountStatsDTO();

        totalCountStatsDTO.setFileSize(new BigInteger("8053063680"));

        System.out.println(totalCountStatsDTO.getFileSizeFormat());
    }

}
