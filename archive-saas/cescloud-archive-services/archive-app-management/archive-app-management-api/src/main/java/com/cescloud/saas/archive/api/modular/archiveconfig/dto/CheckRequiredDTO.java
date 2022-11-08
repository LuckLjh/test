package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class CheckRequiredDTO implements Serializable {

	private static final long serialVersionUID = 4294436699326787787L;
	/**
	 * true 校验通过，false 校验不通过
	 */
	private Boolean status;
	/**
	 * 错误信息
	 */
	private String message;
	/**
	 * 详细信息 如 题名：xx 的条目缺失必输项xxx。
	 */
	private List<String> detail;
}
