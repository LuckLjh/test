package com.cescloud.saas.archive.api.modular.syssetting.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName SmsSettingDTO
 * @Author zhangxuehu
 * @Date 2021/3/10 下午3:11
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel("短信源设置")
public class NoteSettingDTO implements Serializable {

    private static final long serialVersionUID = 6014792029484962479L;

    @ApiModelProperty(value = "供应商提供服务器域名", required = true)
    private String host;

    @ApiModelProperty(value = "供应商提供服务器code", required = true)
    private String appcode;

    @ApiModelProperty(value = "消息模板", required = true, example = "**code**:12345,**minute**:5")
    private String param;

    @ApiModelProperty(value = "签名ID：供应商提供", required = true)
    private String smsSignId;

    @ApiModelProperty(value = "模板Id;供应商提供", required = true)
    private String templateId;

    @ApiModelProperty(value = "需要发送的手机号", required = true)
    private String mobile;
}


