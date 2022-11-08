package com.cescloud.saas.archive.api.modular.archivetype.constant;

public enum ListAlignmentEnum {
	/**
	 * 左对齐
	 */
	TREE_ROOT("左对齐", "L"),

	/**
	 * 居中
	 */
	EMPTY("居中", "C"),

	/**
	 * 右对齐
	 */
	DYNAMIC("右对齐", "R"),
	;

	private final String name;
	private final String code;

	private ListAlignmentEnum(String name, String code) {
		this.name = name;
		this.code = code;
	}

	public static ListAlignmentEnum getEnum(String code) {
		for (ListAlignmentEnum listAlignmentEnum : ListAlignmentEnum.values()) {
			if (listAlignmentEnum.getCode().equals(code)) {
				return listAlignmentEnum;
			}
		}
		return null;
	}
	public static ListAlignmentEnum getEnumByName(String name) {
		for (ListAlignmentEnum listAlignmentEnum : ListAlignmentEnum.values()) {
			if (listAlignmentEnum.getName().equals(name)) {
				return listAlignmentEnum;
			}
		}
		return null;
	}


	public String getName() {
		return this.name;
	}

	public String getCode() {
		return this.code;
	}

}
