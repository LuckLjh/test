package com.cescloud.saas.archive.api.modular.metadata.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Data
public class MetadataTagEnglishDTO implements Serializable {

    private static final long serialVersionUID = 673864503299624102L;

    @NotBlank(message = "存储表名不能为空")
    private String storageLocate;

    @NotNull(message = "元数据标签英文名不能为空")
    @Size(min = 1, message = "至少要有一个元数据标签")
	private List<String> tagEnglishList;
}
