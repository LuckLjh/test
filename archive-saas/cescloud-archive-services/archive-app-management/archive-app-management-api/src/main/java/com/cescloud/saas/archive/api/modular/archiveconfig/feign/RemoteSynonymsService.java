package com.cescloud.saas.archive.api.modular.archiveconfig.feign;

import com.cescloud.saas.archive.api.modular.synonymy.dto.SynonymyWordSearchDTO;
import com.cescloud.saas.archive.api.modular.synonymy.entity.Synonymy;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(contextId = "RemoteSynonymsService", value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteSynonymsService {

	/**
	 * 获取分组下的分词
	 * @return
	 */
	@GetMapping("/synonymy/getSynonymyList")
	public R<List<String>> getSynonymyList();

	@PostMapping("/synonymy/search")
	R<List<String>> search(@RequestBody SynonymyWordSearchDTO synonymyWordSearchDTO);
}
