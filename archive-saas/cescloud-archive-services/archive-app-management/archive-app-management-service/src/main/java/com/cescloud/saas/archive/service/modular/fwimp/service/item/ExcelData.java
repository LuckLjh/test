package com.cescloud.saas.archive.service.modular.fwimp.service.item;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;

public class ExcelData extends BaseRowModel {
	@ExcelProperty(value = "业务字段", index = 0)
	private String businessName;

	@ExcelProperty(value = "档案字段", index = 1)
	private String archiveName;
	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public String getArchiveName() {
		return archiveName;
	}

	public void setArchiveName(String archiveName) {
		this.archiveName = archiveName;
	}

}
