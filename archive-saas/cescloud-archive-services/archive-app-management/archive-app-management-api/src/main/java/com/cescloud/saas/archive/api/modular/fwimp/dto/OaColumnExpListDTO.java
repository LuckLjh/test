package com.cescloud.saas.archive.api.modular.fwimp.dto;

import lombok.Data;

import java.util.List;

@Data
public class OaColumnExpListDTO {
	private static final long serialVersionUID = 1L;

	private List<OaColumnExpandDTO> oaColumnExpand;
}
