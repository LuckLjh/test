package com.cescloud.saas.archive.api.modular.onlinefiling.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class JsonSignDto {

	private Map<String,Object> other = new HashMap<>();

	/**
	 * 必输项，业务系统或OA的唯一标识
	 */
	@ApiModelProperty(value = "必输项，业务系统或OA的唯一标识")
	private Long businessId;

	/**
	 * 单套制算法
	 */
	@ApiModelProperty(value = "单套制算法")
	private String signatureAlgorithm;

	/**
	 * 单套制信息
	 */
	@ApiModelProperty(value = "单套制信息")
	private String signatureInfo;

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
