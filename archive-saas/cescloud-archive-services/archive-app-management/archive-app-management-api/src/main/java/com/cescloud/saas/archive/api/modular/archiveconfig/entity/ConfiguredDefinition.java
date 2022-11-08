package com.cescloud.saas.archive.api.modular.archiveconfig.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author LS
 * @date 2021/6/30
 */
@ApiModel("配置定义")
@Data
@TableName("apma_configured_definition")
public class ConfiguredDefinition implements Serializable {

	private static final long serialVersionUID = 5395777952697709189L;

	@ApiModelProperty("主键")
	@TableId
	private Long id;

	@ApiModelProperty("菜单id")
	private Long menuId;

	@ApiModelProperty("菜单名称")
	private String menuName;

	@ApiModelProperty("配置项（可配置的配置定义 TypedefEnum，DataPermissionDefEnum  的value值）")
	private String configured;

	@ApiModelProperty("档案层级")
	private String archiveLayer;

}
