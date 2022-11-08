package com.cescloud.saas.archive.api.modular.filingscope.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LS
 * @date 2022/4/20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilingScopeExcelErrorDTO {

	/**
	 * 列
	 */
	private Integer columnIndex;

	/**
	 * 行
	 */
	private Integer rowIndex;

	/**
	 * 备注
	 */
	private String msg;
}
