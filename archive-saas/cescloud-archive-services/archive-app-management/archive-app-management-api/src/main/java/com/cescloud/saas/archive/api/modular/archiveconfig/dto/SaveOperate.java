package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 保存操作对象
 */
@ApiModel("保存操作对象")
@Data
@EqualsAndHashCode
public class SaveOperate implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 关联业务主键
	 */
	@ApiModelProperty("关联业务主键")
	private Long businessId;
	/**
	 * 业务类型（column_rule元数据；batch_link挂接）
	 */
	@ApiModelProperty("业务类型（column_rule元数据；batch_link挂接）")
	private String businessType;
	/**
	 * 档案层级存储表
	 */
	@ApiModelProperty("档案层级存储表")
	private String storageLocate;
	/**
	 * 操作对象集合
	 */
	@ApiModelProperty("操作对象集合")
	private List<OperationDTO> data;

}
