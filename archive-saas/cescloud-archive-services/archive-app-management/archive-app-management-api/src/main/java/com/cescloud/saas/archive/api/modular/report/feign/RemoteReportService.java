package com.cescloud.saas.archive.api.modular.report.feign;
/**
@author xaz
@date 2019/5/7 - 13:52
**/

import com.cescloud.saas.archive.api.modular.report.entity.Report;
import com.cescloud.saas.archive.api.modular.report.entity.ReportMetadata;
import com.cescloud.saas.archive.api.modular.report.entity.ReportTable;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@FeignClient(contextId = "remoteReportService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteReportService {

	/**
	 * 通过id查询报表定义
	 * @param id id
	 * @return R
	 */
	@GetMapping("/report/{id}")
	R<Report> getById(@PathVariable("id")  Long id);

	/**
	 * 通过ids查询报表定义
	 * @param ids ids
	 * @return R
	 */
	@GetMapping("/report/listByIds")
	R<List<Report>> getByIds(@RequestParam("ids") List<Long> ids);


	/**
	 * 获取报表关联表
	 * @param reportId 报表ID
	 * @return List
	 */
	 @GetMapping("/report/listByReport/{reportId}")
	 List<ReportTable> listByReportId(@PathVariable("reportId") Long reportId);

	/**
	 * 获取报表关联表字段
	 * @param reportId 报表id
	 * @param storageLocate 档案门类表
	 * @return List
	 */
	@GetMapping("/report/listByReportId/{reportId}/{storageLocate}")
	 List<ReportMetadata> listByReportIds(@PathVariable("reportId") Long reportId,@PathVariable("storageLocate") String storageLocate);

	/**
	 *  初始化 租户档案类型默认irpoert模板
	 */
	@GetMapping(value = "/report/initIreportData/{templateId}/{tenantId}")
	public void createIreportData(@PathVariable(value = "templateId")Long templateId,@PathVariable(value = "tenantId") Long tenantId);

	@GetMapping("/report/getIrportConfigInfo/{tenantId}")
    R<List<ArrayList<String>>> getIrportConfigInfo(@PathVariable(value = "tenantId") Long tenantId);
}
