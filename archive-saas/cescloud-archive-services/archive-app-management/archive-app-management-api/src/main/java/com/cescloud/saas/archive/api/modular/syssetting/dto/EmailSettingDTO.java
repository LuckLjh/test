package com.cescloud.saas.archive.api.modular.syssetting.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName EmailSettingDTO
 * @Author zhangxuehu
 * @Date 2021/3/10 下午3:11
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel("邮箱配置")
public class EmailSettingDTO implements Serializable {

    private static final long serialVersionUID = 6014792029484962479L;

    @ApiModelProperty(value = "SMTP服务器域名", required = true, example = "如：smtp.163.com")
    private String host;

    @ApiModelProperty(value = "邮箱地址", required = true, example = "如：xxx@163.com")
    private String emailAddress;

    @ApiModelProperty(value = "授权码", required = true, example = "如：邮箱登录授权码")
    private String authorizationCode;
}
