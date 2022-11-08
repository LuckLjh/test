package com.cescloud.saas.archive.api.modular.archivetype.dto;

import com.cescloud.saas.archive.api.modular.archivetype.entity.InnerRelation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LayerRelationDTO extends InnerRelation {

	/**
	 * 原字段英文
	 */
	private String sourceMetadataEnglish;
	/**
	 * 原字段中文
	 */
	private String sourceMetadataChinese;

	/**
	 * 目标字段英文
	 */
	private String targetMetadataEnglish;
	/**
	 * 目标字段中文
	 */
	private String targetMetadataChinese;
}
