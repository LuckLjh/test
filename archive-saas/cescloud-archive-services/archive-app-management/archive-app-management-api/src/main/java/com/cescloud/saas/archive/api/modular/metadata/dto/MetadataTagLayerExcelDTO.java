package com.cescloud.saas.archive.api.modular.metadata.dto;

import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataTag;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * excel导入实体
 * @author liwei
 */
@Data
public class MetadataTagLayerExcelDTO extends MetadataTagLayerDTO {

	List<MetadataTag> metadataTags = new ArrayList<MetadataTag>();

}
