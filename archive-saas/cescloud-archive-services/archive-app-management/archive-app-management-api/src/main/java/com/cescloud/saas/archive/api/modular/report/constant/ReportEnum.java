package com.cescloud.saas.archive.api.modular.report.constant;

public enum ReportEnum {
	/**
	 * ireport格式后缀
	 */
	ireport_suffix("ireport格式后缀",".jrxml"),
	/**
	 * 报表类型：独立
	 */
	TYPE_INDEPENDENT("独立", "0"),
	/**
	 * 报表类型：复合
	 */
	TYPE_COMPLEX("复合", "1");

	private ReportEnum(String name, String code) {
		this.name = name;
		this.code = code;
	}
	private final String name;

	private final String code;

	public String getName() {
		return this.name;
	}

	public String getCode() {
		return this.code;
	}
}
