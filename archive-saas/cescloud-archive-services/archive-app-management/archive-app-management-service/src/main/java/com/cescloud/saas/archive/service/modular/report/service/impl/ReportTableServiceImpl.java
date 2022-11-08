
package com.cescloud.saas.archive.service.modular.report.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.report.entity.ReportTable;
import com.cescloud.saas.archive.service.modular.report.mapper.ReportTableMapper;
import com.cescloud.saas.archive.service.modular.report.service.ReportTableService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报表关联元数据表
 *
 * @author xieanzhu
 * @date 2019-04-29 18:38:01
 */
@Service
public class ReportTableServiceImpl extends ServiceImpl<ReportTableMapper, ReportTable> implements ReportTableService {

	/**
	 * @return java.lang.Boolean
	 * @Author xieanzhu
	 * @Description //删除报表相关配置表信息
	 * @Date 15:21 2019/5/5
	 * @Param [reportId]
	 **/
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void removeByReportId(Long reportId) {
		final List<ReportTable> reportTableList = this.list(Wrappers.<ReportTable>query().lambda().eq(ReportTable::getReportId, reportId));
		final List<Long> ids = reportTableList.stream().map(reportTable -> reportTable.getId()).collect(Collectors.toList());
		this.removeByIds(ids);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void removeByReportIds(List<Long> reportIds) {
		final List<ReportTable> reportTableList = this.list(Wrappers.<ReportTable>query().lambda().in(ReportTable::getReportId, reportIds));
		final List<Long> ids = reportTableList.stream().map(reportTable -> reportTable.getId()).collect(Collectors.toList());
		this.removeByIds(ids);
	}

	/**
	 * @return java.util.List<com.cescloud.saas.archive.api.modular.report.entity.ReportTable>
	 * @Author xieanzhu
	 * @Description //根据报表id查询所有关联表
	 * @Date 15:27 2019/5/6
	 * @Param [reportId]
	 **/
	@Override
	public List<ReportTable> listByReportId(Long reportId) {
		return this.list(Wrappers.<ReportTable>query().lambda().eq(ReportTable::getReportId, reportId));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void copyByStorageLocate(String srcStorageLocate, String destStorageLocate, Map<Long, Long> srcDestReportIdMap) {
		List<ReportTable> list = this.list(Wrappers.<ReportTable>lambdaQuery().eq(ReportTable::getStorageLocate, srcStorageLocate));
		if (CollectionUtil.isNotEmpty(list)) {
			list.stream().forEach(reportTable -> {
				reportTable.setId(null);
				reportTable.setStorageLocate(destStorageLocate);
				reportTable.setReportId(srcDestReportIdMap.get(reportTable.getReportId()));
			});
			this.saveBatch(list);
		}
	}
}
