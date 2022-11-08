package com.cescloud.saas.archive.api.modular.archivetype.dto;

import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ApiModel("档案门类复制到另一个档案门类实体")
public class ArchiveTypeCopyPostDTO extends ArchiveType {
	/**
	 * 全宗号编码
	 */
	@ApiModelProperty("目标全宗号编码")
	private String targetFondsCode;
	/**
	 * 全宗名称
	 */
	@ApiModelProperty("目标全宗名称")
	private String targetFondsName;
}
