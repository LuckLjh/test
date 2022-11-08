package com.cescloud.saas.archive.api.modular.account.dto;

import com.cescloud.saas.archive.api.modular.account.entity.AccountTemplate;
import com.cescloud.saas.archive.api.modular.account.entity.AccountTemplateRole;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName AccountTemplateDTO
 * @Author zhangxuehu
 * @Date 2021/2/24 下午3:24
 **/
@ApiModel("台账定义")
@Data
public class AccountTemplateDTO extends AccountTemplate implements Serializable {

    private static final long serialVersionUID = 4549355030716233216L;

    @ApiModelProperty("关联关系")
    private List<AccountTemplateRole> accountTemplateRoles;

	@ApiModelProperty("模板名称")
    private String templateName;
}
