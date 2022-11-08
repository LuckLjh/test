package com.cescloud.saas.archive.api.modular.stats.dto;

import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class DeckStatDTO extends TotalCountStatsDTO {
	//status in (50,60,61,62,80,81,90,100,110,120,130,140,150,160)
	@ApiModelProperty("归档数量")
	private int auditedAmount;
	@ApiModelProperty("档案归档率")
	private String auditedPercent;

	@ApiModelProperty("档案总数标题")
	private String totalArchiveShow;
	@ApiModelProperty("档案总数标题")
	private String totalArchiveTitle;
	@ApiModelProperty("电子文件数量标题")
	private String documentAmountShow;
	@ApiModelProperty("电子文件数量标题")
	private String documentAmountTitle;
	@ApiModelProperty("数据总容量 电子文件大小")
	private String fileSizeFormatShow;
	@ApiModelProperty("数据总容量 电子文件大小")
	private String fileSizeFormatTitle;
	@ApiModelProperty("档案数字化率标题")
	private String digitedPercentShow;
	@ApiModelProperty("档案数字化率标题")
	private String digitedPercentTitle;
	@ApiModelProperty("档案归档率标题")
	private String auditedPercentShow;
	@ApiModelProperty("档案归档率标题")
	private String auditedPercentTitle;

	public DeckStatDTO showTitle() {
		List<String> totalArchiveList = numberSize(this.getTotalArchive());
		List<String> documentAmountList = numberSize(this.getDocumentAmount());
		this.setTotalArchiveShow(totalArchiveList.get(0));
		this.setTotalArchiveTitle(totalArchiveList.get(1));
		this.setDocumentAmountShow(documentAmountList.get(0));
		this.setDocumentAmountTitle(documentAmountList.get(1));
//		this.setFileSizeFormatShow(this.getFileSizeFormat());
//		this.setFileSizeFormatTitle("");
		this.formatFileSize(0);
		this.setDigitedPercentShow(getDigitedPercentShow(this.getDigitedPageAmount(), this.getPageAmount()) + "");
		this.setDigitedPercentTitle("%");
		int i = Integer.parseInt(totalArchiveList.get(0));
		this.setAuditedPercentShow(i == 0 ? "0" : (auditedAmount / i) + "");
		this.setAuditedPercentTitle("%");
		return this;
	}

	public String getDigitedPercentShow(int digitedPageAmount, int pageAmount) {
		if (digitedPageAmount == 0) {
			return "0";
		}
		if (pageAmount == 0) {
			return "0";
		}
		return digitedPageAmount / pageAmount + "";
	}

	public String getAuditedPercent() {
		if (0 == auditedAmount) {
			return "0%";
		}
		if (0 == super.getTotalArchive()) {
			return "0%";
		}
		return String.format("%d%%", auditedAmount * 100 / super.getTotalArchive());
	}

	private static String[] unitsS = {"B", "KB", "MB", "GB", "TB", "EB"};

	// B, KB, MB, GB, TB, EB
	private String formatFileSize(int unitIndex) {
		final double unitSize = Math.pow(1024, unitIndex);
		final double s = this.getFileSize().doubleValue() / unitSize;
		if (s > 1024) {
			return formatFileSize(++unitIndex);
		}
		this.setFileSizeFormatShow(String.format("%.2f", s));
		this.setFileSizeFormatTitle(unitsS[unitIndex]);
		return String.format("%.2f", s) + unitsS[unitIndex];
	}

	// 万
	private List<String> numberSize(int number) {
		List<String> result = Lists.newArrayList();
		if (number / 10000 > 0) {
			result.add((number / 10000) + "," + (number % 10000));
			result.add("万");
		} else {
			result.add(number + "");
			result.add("");
		}
		return result;
	}
}
