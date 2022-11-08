package com.cescloud.saas.archive.api.modular.onlinefiling.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 黄宇权
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class JsonEntiyDto implements Serializable {
	/**
	 * 批次号
	 */
	@ApiModelProperty(value = "批次号", required = true)
	private String batchNo;

	@ApiModelProperty(value = "档案类型", required = true)
	private String archiveType;

	@ApiModelProperty(value = "租户id", required = true)
	private Long tenantId;

	@ApiModelProperty(value = "全文校验码类型，MD5、SM3、SHA1等，没有的话可以默认为MD5", required = true)
	private String checkCodeType;

	@ApiModelProperty(value = "回调地址", required = true)
	private String callback_url;

	/*@ApiModelProperty(value = "工号", required = true)
	private String code;*/

	public JsonEntiyDto(String batchNo, String archiveType, Long tenantId,String checkCodeType) {
		this.batchNo = batchNo;
		this.archiveType = archiveType;
		this.tenantId = tenantId;
		this.checkCodeType = checkCodeType;
	}
	private List<JsonProjectDto> projects;

	private List<JsonFolderDto> folders;

	private List<JsonFileDto> files;

	private Map<String,Object> other = new HashMap<>();

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
