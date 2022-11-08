package com.cescloud.saas.archive.api.modular.onlinefiling.dto;

import lombok.Data;

import java.util.Map;

@Data
public class RetDataDto {
	/**
	 * true 为执行成功
	 */
	boolean checkStatus;

	/**
	 * 业务id
	 */
	Long  businessId;

	/**
	 * 错误码
	 */
	int retCode;
	/**
	 * 错误信息
	 */
	String info;
	/**
	 * 列的键值对
	 */
	Map<String, Object> columnMap;
}
