package com.cescloud.saas.archive.api.modular.syssetting.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 全文加密设置DTO
 * @author ylh
 *
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel("全文加密设置")
public class FileEncryptionSettingDTO implements Serializable {

	private static final long serialVersionUID = -6183810108145473068L;
	private static final String ON="1";
	private static final String OFF="0";
	/**
	 * 原件存储加密
	 */
	@ApiModelProperty(value="原件存储加密",required=true,example="0   或  1   0：关闭  1：开启")
	private String oriStore;

	/**
	 * 利用件储存加密方式
	 */
	@ApiModelProperty(value="原件存储加密方式",required=false,example="1   或  2   1：国密算法 2：通用算法")
	private String oriStoreEncryptionType;

	/**
	 * 利用件下载加密
	 */
	@ApiModelProperty(value="利用件下载加密",required=true,example="0   或  1   0：关闭  1：开启")
	private String usingDown;

	public static FileEncryptionSettingDTO defaultEncryptSetting(){
		FileEncryptionSettingDTO defaultEncryptSetting = new FileEncryptionSettingDTO();
		defaultEncryptSetting.setOriStore(OFF);
		defaultEncryptSetting.setOriStoreEncryptionType(ON);
		defaultEncryptSetting.setUsingDown(OFF);
		return defaultEncryptSetting;
	}

}
