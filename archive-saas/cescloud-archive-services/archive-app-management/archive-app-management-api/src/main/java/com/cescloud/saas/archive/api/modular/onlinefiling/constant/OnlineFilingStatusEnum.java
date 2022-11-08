package com.cescloud.saas.archive.api.modular.onlinefiling.constant;

public enum OnlineFilingStatusEnum {
	/**
	 * 解析json异常
	 */
	PARSE_JSON_EXCEPTION("解析json异常", 1000),
	/**
	 * 档案节点与json格式不符
	 */
	PARSE_ARCHIVE_TYPE_EXCEPTION("档案节点与json格式不符", 1001),
	/**
	 * 档案节点不存在
	 */
	PARSE_ARCHIVE_TYPE_NULL("档案节点不存在", 1002),
	/**
	 * 保存电子文件失败
	 */
	SAVE_DOC_FAIL("保存电子文件失败", 3002),
	/**
	 *下载电子文件失败
	 */
	DOWN_DOC_FAIL("下载电子文件失败", 3003),
	/**
	 *保存过程信息失败
	 */
	SAVE_INFO_FAIL("保存过程信息失败", 3004),
	SAVE_FILE_FAIL("保存卷内失败", 3005),
	SAVE_FOLDER_FAIL("保存案卷失败", 3006),
	SAVE_ONE_FAIL("保存一文一件失败", 3007),
	DOC_CHECK_FAIL("电子文件校验码失败", 3008),
	DOC_DOWMN_FAIL("电子文件下载失败", 3009),
	SAVE_SIGN_FAIL("保存签名签章失败", 3010),
	/**
	 * 成功
	 */
	JSON_SUCCESS("成功", 0),
	JSON_FAIL("失败", 4000),
	PART_JSON_SUCCESS("部分成功", 1);

	private OnlineFilingStatusEnum(String name, int code) {
		this.name = name;
		this.code = code;
	}

	private final String name;

	private final int code;

	public String getName() {
		return this.name;
	}

	public int getCode() {
		return this.code;
	}
}
