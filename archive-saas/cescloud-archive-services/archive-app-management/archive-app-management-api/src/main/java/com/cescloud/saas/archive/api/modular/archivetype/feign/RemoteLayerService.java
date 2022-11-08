package com.cescloud.saas.archive.api.modular.archivetype.feign;

import com.cescloud.saas.archive.api.modular.archivetype.entity.Layer;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(contextId = "remoteLayerService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteLayerService {

	@GetMapping("/layer")
	R<List<Layer>> getListByIds(@RequestParam("ids") String ids);

	@GetMapping("/layer/code/{code}")
	R<Layer> getByCode(@PathVariable("code") String code);

}
