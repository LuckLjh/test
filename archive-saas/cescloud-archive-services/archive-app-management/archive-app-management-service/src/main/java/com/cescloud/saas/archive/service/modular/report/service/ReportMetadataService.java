
package com.cescloud.saas.archive.service.modular.report.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.report.entity.ReportMetadata;

import java.util.List;
import java.util.Map;


/**
 * 报表关联元数据表
 *
 * @author xieanzhu
 * @date 2019-04-29 18:40:02
 */
public interface ReportMetadataService extends IService<ReportMetadata> {

       void removeByReportId(Long reportId);

       void removeByReportIds(List<Long> reportIds);

       List<ReportMetadata> listByReportId(Long reportId,String storageLocate);

       void copyByStorageLocate(String srcStorageLocate, String destStorageLocate, Map<Long,Long> srcDestReportIdMap);
}
