package com.cescloud.saas.archive.api.modular.syssetting.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/*
 * Description:
 * @auther: qianbaocheng
 * @date: 8/12/2021 下午4:27
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel("系统标题配置")
public class SystemNameSetDTO implements Serializable {

    private static final long serialVersionUID = 6014792029484962479L;

    @ApiModelProperty(value = "图片")
    private String picId;

    @ApiModelProperty(value = "系统名称")
    private String sysName;

    @ApiModelProperty(value = "系统简称")
    private String sysShortName;

	/**
	 * 作废，为保证旧数据不报错，暂时保留
	 * 王谷华 2022-01-19
	 */
    @Deprecated
	@ApiModelProperty(value = "系统预览图")
	private String picUrl;


	@ApiModelProperty(value = "预览图加密")
	private String encryptWord;
}
