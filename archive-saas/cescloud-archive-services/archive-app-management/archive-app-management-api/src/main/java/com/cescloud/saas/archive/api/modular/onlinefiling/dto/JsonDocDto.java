package com.cescloud.saas.archive.api.modular.onlinefiling.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 黄宇权
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class JsonDocDto {

	private Map<String,Object> other = new HashMap<>();

	/**
	 * 必输项，业务系统或OA的唯一标识
	 */
	@ApiModelProperty(value = "必输项，业务系统或OA的唯一标识")
	private Long businessId;

	/**
	 * 文件名
	 */
	@ApiModelProperty(value = "文件名")
	private String fileName;

	/**
	 *
	 * 文件大小
	 */
	@ApiModelProperty(value = "文件大小")
	private String fileSize;

	/**
	 *
	 * 校验码
	 */
	@ApiModelProperty(value = "校验码")
	private String checkCode;

	/**
	 *
	 * 文件url
	 */
	@ApiModelProperty(value = "文件url")
	private String fileUrl;

	@JsonAnyGetter
	public Map<String, Object> getOther() {
		return other;
	}

	/**
	 * 没有匹配上的反序列化属性，放到这里
	 * @param key
	 * @param value
	 */
	@JsonAnySetter
	public void setOther(String key, Object value) {
		this.other.put(key,value);
	}


}
