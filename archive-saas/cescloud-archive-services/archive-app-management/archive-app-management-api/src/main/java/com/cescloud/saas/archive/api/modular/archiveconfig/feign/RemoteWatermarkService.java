package com.cescloud.saas.archive.api.modular.archiveconfig.feign;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.WatermarkDTO;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.Watermark;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.WatermarkDetail;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@FeignClient(contextId = "remoteWatermarkService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteWatermarkService {

	@RequestMapping(value = "/watermark/list" , method = RequestMethod.GET)
	R<List<Watermark>> getListWatermark(@RequestParam("typeCode") String typeCode);

	@RequestMapping(value = "/watermark/{id}" , method = RequestMethod.GET)
	R<WatermarkDTO> getWatermark(Long watermarkId) ;

	@GetMapping("/watermark/defaultWatermark")
	R<Watermark> getDefaultWatermark(@RequestParam("storageLocate") String storageLocate, @RequestParam("waterClassification") int waterClassification,@RequestParam("watermarkFormat") String watermarkFormat);

	@GetMapping("/watermark/watermarkDetail/{watermarkId}")
	R<List<WatermarkDetail>> getWatermarkDetail(@PathVariable("watermarkId") Long watermarkId);
}
