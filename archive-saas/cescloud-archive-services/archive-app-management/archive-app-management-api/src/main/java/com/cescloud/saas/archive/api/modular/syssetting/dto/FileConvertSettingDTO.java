package com.cescloud.saas.archive.api.modular.syssetting.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("格式转换设置")
public class FileConvertSettingDTO implements Serializable {

	private static final long serialVersionUID = 779259742621107439L;

	@ApiModelProperty(value = "是否开启", required = true)
	private Boolean enabled;

	private ConvertPDF pdf;
	private ConvertJPG jpg;
	private ConvertFLV flv;
	private ConvertMP4 mp4;
	private ConvertMP3 mp3;
	private ConvertTIF tif;
	private ConvertOFD ofd;

	public static FileConvertSettingDTO defaultConvertSetting() {
		return FileConvertSettingDTO.builder().enabled(false).build();
	}

	public List<String> getTargetFormat(String format) {
		if (!this.getEnabled()) {
			return Collections.emptyList();
		}
		List<String> targetFormat = new ArrayList<>();
		if (null != this.getPdf() && this.getPdf().contains(format)) {
			targetFormat.add("pdf");
		}
		if (null != this.getJpg() && this.getJpg().contains(format)) {
			targetFormat.add("jpg");
		}
		if (null != this.getFlv() && this.getFlv().contains(format)) {
			targetFormat.add("flv");
		}
		if (null != this.getMp4() && this.getMp4().contains(format)) {
			targetFormat.add("mp4");
		}
		if (null != this.getMp3() && this.getMp3().contains(format)) {
			targetFormat.add("mp3");
		}
		if (null != this.getTif() && this.getTif().contains(format)) {
			targetFormat.add("tif");
		}
		if (null != this.getOfd() && this.getOfd().contains(format)) {
			targetFormat.add("ofd");
		}
		return targetFormat;
	}
}
