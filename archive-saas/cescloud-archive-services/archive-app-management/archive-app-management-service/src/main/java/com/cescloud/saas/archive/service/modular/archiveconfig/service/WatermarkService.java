
package com.cescloud.saas.archive.service.modular.archiveconfig.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.WatermarkDTO;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.Watermark;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.WatermarkDetail;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


/**
 * 水印配置
 *
 * @author qianjiang
 * @date 2020-05-13 11:12:08
 */
public interface WatermarkService extends IService<Watermark> {

	List<Watermark> listWatermark(String storageLocate, String keyword);

	Watermark getDefaultWatermark(String storageLocate,int waterClassification,String watermarkFormat);

	List<WatermarkDetail> getDefaultWatermarkDetail(WatermarkDTO watermarkDTO);

	List<WatermarkDetail> getWatermarkDetail(Long watermarkId);

	WatermarkDTO getWatermark(Long watermarkId);

	R saveWatermark(WatermarkDTO watermarkDTO);

	R deleteWatermark(Long watermarkId, String storageLocate, Long moduleId);

	R getMetadata(String storageLocate,int type) ;

	R<Map<String, Object>> uploadFile(MultipartFile file) throws Exception;

	R copy(Long watermarkId, List<Long> targetModuleIds);

	void showImage(Long fileId, HttpServletResponse response);

	void copyByStorageLocate(String srcStorageLocate, String destStorageLocate, Map<Long, Long> srcDestMetadataMap);

}
