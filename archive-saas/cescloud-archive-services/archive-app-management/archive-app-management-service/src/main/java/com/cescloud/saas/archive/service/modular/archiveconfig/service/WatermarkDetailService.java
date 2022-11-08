package com.cescloud.saas.archive.service.modular.archiveconfig.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.WatermarkDetail;

import java.util.Map;

public interface WatermarkDetailService extends IService<WatermarkDetail> {

	void copyConfig(Map<Long,Long> srcDestConfigIdMap);
}
