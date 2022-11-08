
package com.cescloud.saas.archive.service.modular.report.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.report.entity.ReportTable;

import java.util.List;
import java.util.Map;


/**
 * 报表关联元数据表
 *
 * @author xieanzhu
 * @date 2019-04-29 18:38:01
 */
public interface ReportTableService extends IService<ReportTable> {

    void removeByReportId(Long reportId);

    void removeByReportIds(List<Long> reportIds);

    List<ReportTable> listByReportId(Long reportId);

	void copyByStorageLocate(String srcStorageLocate, String destStorageLocate, Map<Long,Long> srcDestReportIdMap);

}
