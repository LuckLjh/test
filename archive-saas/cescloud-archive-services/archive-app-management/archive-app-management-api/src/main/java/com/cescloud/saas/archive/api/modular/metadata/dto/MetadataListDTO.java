package com.cescloud.saas.archive.api.modular.metadata.dto;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MetadataListDTO implements Serializable{

    private static final long serialVersionUID = 3148090268261666345L;

    private String storageLocate;

	private List<MetadataDTO> metadataList;
	
	public MetadataListDTO() {    
	}
}
