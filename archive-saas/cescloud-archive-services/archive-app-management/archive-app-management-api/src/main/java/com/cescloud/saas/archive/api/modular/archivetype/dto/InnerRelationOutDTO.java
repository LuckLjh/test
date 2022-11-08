package com.cescloud.saas.archive.api.modular.archivetype.dto;
/**
@author xaz
@date 2019/6/25 - 17:16
**/

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(value = "关联关系定义列表参数")
public class InnerRelationOutDTO extends InnerRelationPutDTO{

	private static final long serialVersionUID = -2468175724517602898L;

}
