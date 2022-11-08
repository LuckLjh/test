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
public class JsonInfoDto {
	private Map<String,Object> other = new HashMap<>();


	/**
	 * 必输项，业务系统或OA的唯一标识
	 */
	@ApiModelProperty(value = "必输项，业务系统或OA的唯一标识")
	private Long businessId;

	/**
	 *  节点部门
	 */
	@ApiModelProperty(value = "节点名")
	private String activityName;

	/**
	 * 节点部门
	 */
	@ApiModelProperty(value = "节点部门")
	private String activityDept;

	/**
	 * 节点人
	 */
	@ApiModelProperty(value = "节点人")
	private String activityUser;

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
