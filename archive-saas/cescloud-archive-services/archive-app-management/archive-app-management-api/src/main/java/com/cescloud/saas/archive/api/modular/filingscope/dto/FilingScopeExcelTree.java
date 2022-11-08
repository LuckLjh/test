package com.cescloud.saas.archive.api.modular.filingscope.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 归档范围节点树（excel中的数据层次）
 * @author liwei
 */
@Data
public class FilingScopeExcelTree extends FilingScopeDTO {

	/**
	 * 节点下的子节点
	 */
	List<FilingScopeExcelTree> children = new ArrayList<FilingScopeExcelTree>();
}
