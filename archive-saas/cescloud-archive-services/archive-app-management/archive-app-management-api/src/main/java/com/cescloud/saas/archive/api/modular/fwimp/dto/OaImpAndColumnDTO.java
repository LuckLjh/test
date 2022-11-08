package com.cescloud.saas.archive.api.modular.fwimp.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@ApiModel("数据导入对象")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OaImpAndColumnDTO {
	public OaImportDTO oaImport;

	public List<OaColumnDTO> oaColumns;
}
