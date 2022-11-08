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

/**
 * @author 黄宇权
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class JsonProjectDto {

	private Map<String,Object> other = new HashMap<>();
	@JsonAnyGetter
	public Map<String, Object> getOther() {
		return other;
	}

	/**
	 * 必输项，业务系统或OA的唯一标识
	 */
	@ApiModelProperty(value = "必输项，业务系统或OA的唯一标识")
	private long businessId;

	/**
	 *  案卷题名
	 */
	@ApiModelProperty(value = "项目名称")
	private String titleProper;

	/**
	 * 全宗
	 */
	@ApiModelProperty(value = "全宗")
	private String fondsCode;

	private List<JsonFolderDto> folders;
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
