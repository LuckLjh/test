package com.cescloud.saas.archive.api.modular.syssetting.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName SourceSettingDTO
 * @Author zhangxuehu
 * @Date 2021/3/12 上午10:18
 **/
@Data
@ApiModel("短信源和邮件源设置")
public class SourceSettingDTO implements Serializable {

    private static final long serialVersionUID = 631604019105579643L;

    @ApiModelProperty(value = "是否开启短信通知")
    private Boolean isNoteMessage;

    @ApiModelProperty(value = "是否开启邮件通知")
    private Boolean isEmailMessage;

    @ApiModelProperty(value = "短信源设置")
    private NoteSettingDTO noteSettingDTO;

    @ApiModelProperty(value = "邮箱源设置")
    private EmailSettingDTO emailSettingDTO;
}
