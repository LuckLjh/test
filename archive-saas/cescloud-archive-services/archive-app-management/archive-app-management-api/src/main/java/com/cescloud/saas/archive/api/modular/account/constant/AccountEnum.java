package com.cescloud.saas.archive.api.modular.account.constant;

public enum AccountEnum {
	/**
	 * 档案专题
	 */
	ARCHIVES_PROJECT("档案专题", "ArchivesProject");

	private AccountEnum(String name, String code) {
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
