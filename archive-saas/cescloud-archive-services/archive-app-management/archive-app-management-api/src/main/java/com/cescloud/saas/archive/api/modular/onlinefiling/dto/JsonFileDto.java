package com.cescloud.saas.archive.api.modular.onlinefiling.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class JsonFileDto {

	private Map<String,Object> other = new HashMap<>();

	/**
	 * 必输项，业务系统或OA的唯一标识
	 */
	@ApiModelProperty(value = "必输项，业务系统或OA的唯一标识")
	private Long businessId;

	/**
	 *  案卷题名
	 */
	@ApiModelProperty(value = "案卷题名")
	private String titleProper;

	/**
	 * 全宗
	 */
	@ApiModelProperty(value = "全宗")
	private String fondsCode;

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

	private List<JsonDocDto> documents;

	private List<JsonInfoDto> infos;

	private List<JsonSignDto> signs;
}
