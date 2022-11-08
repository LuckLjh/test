package com.cescloud.saas.archive.api.modular.onlinefiling.dto;

import lombok.Data;

import java.util.List;

@Data
public class SaveEntityAndSendMsgDto {
	String archiveTypeCode;
	int successNum;
	int failedNum;
	String msg;
	int code;
	List<DetailErrorDto> data;
}
