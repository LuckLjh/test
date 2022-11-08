package com.cescloud.saas.archive.service.modular.archiveconfig.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.WatermarkDetail;
import com.cescloud.saas.archive.service.modular.archiveconfig.mapper.WatermarkDetailMapper;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.WatermarkDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class WatermarkDetailServiceImpl extends ServiceImpl<WatermarkDetailMapper, WatermarkDetail> implements WatermarkDetailService {


	@Override
	@Transactional(rollbackFor = Exception.class)
	public void copyConfig(Map<Long, Long> srcDestConfigIdMap) {
		Set<Long> idSet = srcDestConfigIdMap.keySet();
		if (CollectionUtil.isEmpty(idSet)) {
			return;
		}
		List<WatermarkDetail> watermarkDetails = this.list(Wrappers.<WatermarkDetail>lambdaQuery().in(WatermarkDetail::getWatermarkId, idSet));
		if (CollectionUtil.isNotEmpty(watermarkDetails)) {
			watermarkDetails.stream().forEach(watermarkDetail -> {
				watermarkDetail.setId(null);
				watermarkDetail.setWatermarkId(srcDestConfigIdMap.get(watermarkDetail.getWatermarkId()));
			});
			this.saveBatch(watermarkDetails);
		}
	}
}
