package com.cescloud.saas.archive.api.modular.metadata.dto;

import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataBase;
import lombok.Data;

/**
 * @author liwei
 */
@Data
public class MetadataBaseDTO extends MetadataBase {

	private String classType;

	private String filingType;
}
