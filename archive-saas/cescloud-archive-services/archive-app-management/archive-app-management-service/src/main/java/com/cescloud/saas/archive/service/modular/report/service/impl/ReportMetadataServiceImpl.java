
package com.cescloud.saas.archive.service.modular.report.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.report.entity.ReportMetadata;

import com.cescloud.saas.archive.service.modular.report.mapper.ReportMetadataMapper;
import com.cescloud.saas.archive.service.modular.report.service.ReportMetadataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报表关联元数据表
 *
 * @author plez
 */
@Service
public class ReportMetadataServiceImpl extends ServiceImpl<ReportMetadataMapper, ReportMetadata> implements ReportMetadataService {
	/**
	 * @return java.lang.Boolean
	 * @Author plez
	 * @Description //删除报表相关配置元数据信息
	 * @Param [reportId]
	 **/
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void removeByReportId(Long reportId) {
		final List<ReportMetadata> reportMetadataList = this.list(Wrappers.<ReportMetadata>query().lambda().eq(ReportMetadata::getReportId, reportId));
		final List<Long> ids = reportMetadataList.stream().map(reportMetadata -> reportMetadata.getId()).collect(Collectors.toList());
		this.removeByIds(ids);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void removeByReportIds(List<Long> reportIds) {
		final List<ReportMetadata> reportMetadataList = this.list(Wrappers.<ReportMetadata>query().lambda().in(ReportMetadata::getReportId, reportIds));
		final List<Long> ids = reportMetadataList.stream().map(reportMetadata -> reportMetadata.getId()).collect(Collectors.toList());
		this.removeByIds(ids);
	}

	/**
	 * @return java.util.List<com.cescloud.saas.archive.api.modular.report.entity.ReportMetadata>
	 * @Author plez
	 * @Description //根据报表id和表名查询所有元数据字段
	 * @Param [reportId]
	 **/
	@Override
	public List<ReportMetadata> listByReportId(Long reportId, String storageLocate) {
		return this.list(Wrappers.<ReportMetadata>query().lambda()
				.eq(ReportMetadata::getReportId, reportId)
				.eq(ReportMetadata::getStorageLocate, storageLocate));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void copyByStorageLocate(String srcStorageLocate, String destStorageLocate, Map<Long,Long> srcDestReportIdMap) {
		List<ReportMetadata> list = this.list(Wrappers.<ReportMetadata>lambdaQuery().eq(ReportMetadata::getStorageLocate, srcStorageLocate));
		if (CollectionUtil.isNotEmpty(list)) {
			list.stream().forEach(reportMetadata -> {
				reportMetadata.setId(null);
				reportMetadata.setStorageLocate(destStorageLocate);
				reportMetadata.setReportId(srcDestReportIdMap.get(reportMetadata.getId()));
			});
			this.saveBatch(list);
		}
	}
}
