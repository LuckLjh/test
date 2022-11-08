package com.cescloud.saas.archive.api.modular.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoteResultDTO {

	//会签是否完成
	private Boolean completed = false;

	//会签结果
	private Boolean result = true;
}
