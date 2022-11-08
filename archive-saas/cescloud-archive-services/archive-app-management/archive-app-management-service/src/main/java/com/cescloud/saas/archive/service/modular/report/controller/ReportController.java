package com.cescloud.saas.archive.service.modular.report.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.api.modular.archivedict.dto.CopyPostDTO;
import com.cescloud.saas.archive.api.modular.notice.entity.NoticeFile;
import com.cescloud.saas.archive.api.modular.report.dto.*;
import com.cescloud.saas.archive.api.modular.report.entity.Report;
import com.cescloud.saas.archive.api.modular.report.entity.ReportMetadata;
import com.cescloud.saas.archive.api.modular.report.entity.ReportTable;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.annotation.Inner;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.report.service.ReportMetadataService;
import com.cescloud.saas.archive.service.modular.report.service.ReportService;
import com.cescloud.saas.archive.service.modular.report.service.ReportTableService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;


/**
 * 报表定义
 *
 * @author plez
 * @date 2019-09-04 13:04:36
 */
@Api(value = "report", tags = "应用管理-档案门类管理：报表定义")
@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {

    private final ReportService reportService;

    private final ReportTableService reportTableService;

    private final ReportMetadataService reportMetadataService;

    /**
     * 根据层级获取是否有复合类型
     *
     * @param storageLocate 当前档案类型
     * @return R
     */
    @ApiOperation(value = "获取是否有复合类型")
    @GetMapping("/getReportType/{storageLocate}")
    public R getReportType(@PathVariable("storageLocate") @ApiParam(value = "当前档案类型层级表名", name = "storageLocate", required = true) String storageLocate) throws ArchiveBusinessException {
        return reportService.getReportType(storageLocate);
    }

    /**
     * 新增报表定义
     *
     * @param reportPostDTO 报表定义
     * @return R
     */
    @ApiOperation(value = "新增报表")
    @SysLog("新增报表定义")
    @PostMapping
    public R save(@RequestBody @ApiParam(name = "报表对象", value = "传入json格式", required = true) @Valid ReportPostDTO reportPostDTO) throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("新增报表定义-报表名称【%s】",reportPostDTO.getReportTopic()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        return new R<>(reportService.save(reportPostDTO));
    }

    @ApiOperation(value = "新增报表业务模块")
    @SysLog("新增报表业务模块")
    @PostMapping("/save/business")
    public R saveBusiness(@RequestBody @ApiParam(name = "报表对象", value = "传入json格式", required = true) @Valid ReportPostDTO reportPostDTO) throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("新增报表业务模块-报表名称【%s】",reportPostDTO.getReportTopic()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(reportService.saveBusiness(reportPostDTO));
    }

    /**
     * 通过id查询报表定义
     *
     * @param id id
     * @return R
     */
    @ApiOperation(value = "通过id查询报表配置详情")
    @GetMapping("/{id}")
    public R<Report> getById(@PathVariable("id") @ApiParam(name = "id", value = "报表id", required = true) Long id) {
        return new R<>(reportService.getById(id));
    }

    /**
     * 通过ids查询报表定义
     *
     * @param ids id
     * @return R
     */
    @ApiOperation(value = "通过ids查询报表配置详情")
    @GetMapping("/listByIds")
    public R<List<Report>> getByIds(@RequestParam @ApiParam(name = "ids", value = "报表ids", required = true) List<Long> ids) {
        return new R<>((List<Report>) reportService.listByIds(ids));
    }

    /**
     * 修改报表定义
     *
     * @param reportPutDTO 报表定义
     * @return R
     */
    @ApiOperation(value = "修改报表")
    @SysLog("修改报表定义")
    @PutMapping
    public R updateById(@RequestBody @ApiParam(name = "报表对象", value = "传入json格式", required = true) @Valid ReportPutDTO reportPutDTO) throws ArchiveBusinessException {
        if (reportPutDTO.getId() != null && reportPutDTO.getReportTopic() != null && reportPutDTO.getReportType() != null && reportPutDTO.getReportFormat() != null) {
			try {
				SysLogContextHolder.setLogTitle(String.format("修改报表定义-报表名称【%s】",reportPutDTO.getReportTopic()));
			} catch (Exception e) {
				log.error("记录日志详情失败：", e);
			}
            return new R<>(reportService.update(reportPutDTO));
        }
        return new R<>().fail(null, "表单有必输项为空");
    }

    /**
     * 报表配置定义
     */
    @ApiOperation(value = "修改报表配置")
    @SysLog("修改报表定义配置")
    @PutMapping("/updateDeploy")
    public R updateDeploy(@RequestBody @ApiParam(name = "报表对象", value = "传入json格式", required = true) @Valid ReportPutDTO reportPutDTO) throws ArchiveBusinessException {
        if (reportPutDTO.getId() != null && reportPutDTO.getReportPageLines() != null && reportPutDTO.getPageField() != null) {
			try {
				SysLogContextHolder.setLogTitle(String.format("修改报表定义配置-报表名称【%s】",reportPutDTO.getReportTopic()));
			} catch (Exception e) {
				log.error("记录日志详情失败：", e);
			}
            return new R<>(reportService.updateDeploy(reportPutDTO));
        }
        return new R<>().fail(null, "表单有必输项为空");
    }

    /**
     * 通过id删除报表定义
     *
     * @param id id
     * @return R
     */
    @ApiOperation(value = "删除报表")
    @SysLog("删除报表定义")
    @DeleteMapping("/{moduleId}/{id}")
    public R removeById(@PathVariable("moduleId") @ApiParam(name = "moduleId", value = "模块id", required = true) Long moduleId,
                        @PathVariable("id") @ApiParam(name = "id", value = "报表id", required = true) Long id) throws ArchiveBusinessException {
		try {
			Report byId = reportService.getById(id);
			SysLogContextHolder.setLogTitle(String.format("删除报表定义-报表名称【%s】",byId.getReportTopic()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        return reportService.removeById(moduleId, id);
    }

    /**
     * 获取新增报表的关联表
     *
     * @param storageLocate 表名 比如T_1_KJ_KJDA_V
     * @return R
     */
    @ApiOperation(value = "获取新增报表的关联表")
    @GetMapping("/{storageLocate}/relation-tables")
    public R<List<ReportTableDTO>> getRelationStorageLocate(@PathVariable("storageLocate") @ApiParam(name = "storageLocate", value = "存储表名", required = true) String storageLocate) throws ArchiveBusinessException {
        return new R<>(reportService.getRelationStorageLocate(storageLocate));
    }

    /**
     * 查询档案门类下所有报表
     *
     * @param typeCode        档案门类Code
     * @param templateTableId 档案门类模板id
     * @return 报表名称
     * @throws ArchiveBusinessException 业务异常
     */
    @ApiOperation(value = "查询档案门类下所有报表")
    @GetMapping("/list/{moduleId}/{typeCode}/{templateTableId}")
    public R<List<ReportDTO>> getReportList(@PathVariable("moduleId") @ApiParam(name = "moduleId", value = "模块id", required = true) Long moduleId,
                                            @PathVariable("typeCode") @ApiParam(name = "typeCode", value = "档案门类Code", required = true) String typeCode,
                                            @PathVariable("templateTableId") @ApiParam(name = "templateTableId", value = "档案门类模板id", required = true) Long templateTableId) throws ArchiveBusinessException {
        String storageLocate = reportService.getStorageLocate(typeCode, templateTableId);
        return new R<>(reportService.listByStorageLocate(storageLocate, moduleId));
    }

    @ApiOperation(value = "查询档案业务下所有报表")
    @GetMapping("/list/{storageLocate}/{moduleId}")
    public R<List<ReportDTO>> getReportBusinessList(@PathVariable("storageLocate") @ApiParam(name = "storageLocate", value = "业务模块", required = true) String storageLocate,
                                                    @PathVariable("moduleId") @ApiParam(name = "moduleId", value = "模块id", required = true) Long moduleId)  {
        return new R<>(reportService.listByStorageLocate(storageLocate, moduleId));
    }

    /**
     * 根据导出空报表模板
     *
     * @param id 报表id
     * @return R
     */
    @SysLog("导出报表")
    @ApiOperation(value = "导出报表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "报表id", required = true, dataType = "Integer", paramType = "query"),
    })
    @GetMapping("/export/{id}")
    public R export(@PathVariable("id") Long id, HttpServletRequest request, HttpServletResponse response) throws ArchiveBusinessException {
		try {
			Report byId = reportService.getById(id);
			SysLogContextHolder.setLogTitle(String.format("导出报表-报表名称【%s】",byId.getReportTopic()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        return reportService.exportReport(id, request, response);
    }

    /***
     *  导入报表模板文件
     * @param id file
     */
    @SysLog("导入报表")
    @ApiOperation(value = "导入报表")
    @PostMapping("/import")
    public R importReport(@RequestParam("id") @ApiParam(name = "id", value = "报表id", required = true) Long id, @RequestParam("file") @ApiParam(name = "file", value = "报表文件", required = true) MultipartFile file) throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("导入报表-报表文件名称【%s】",file.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        return reportService.importReport(id, file);
    }


    /**
     * 分页查询报表
     *
     * @param current        当前页数
     * @param size           每页记录数
     * @param reportQueryDTO 报表定义查询对象
     * @return R
     */
    @ApiOperation(value = "分页查询报表")
    @GetMapping("/page")
    public R getReportPage(@ApiParam(value = "当前页数", name = "current", required = true) long current,
                           @ApiParam(value = "每页记录数", name = "size", required = true) long size,
                           @ApiParam(name = "报表定义查询对象", value = "传入json格式", required = true) ReportQueryDTO reportQueryDTO) {
        return new R<>(reportService.page(new Page(current, size), reportQueryDTO));
    }

    /**
     * 获取报表关联表
     *
     * @param reportId 报表ID
     * @return List
     */
    @GetMapping("/listByReport/{reportId}")
    public List<ReportTable> listByReportId(@PathVariable("reportId") Long reportId) {
        return reportTableService.listByReportId(reportId);
    }

    /**
     * 获取报表关联表字段
     *
     * @param reportId      报表id
     * @param storageLocate 档案门类表
     * @return List
     */
    @GetMapping("/listByReportId/{reportId}/{storageLocate}")
    public List<ReportMetadata> listByReportIds(@PathVariable("reportId") Long reportId, @PathVariable("storageLocate") String storageLocate) {
        return reportMetadataService.listByReportId(reportId, storageLocate);
    }

    @SysLog("清除报表定义配置")
    @ApiOperation(value = "清除报表定义配置", httpMethod = SwaggerConstants.DELETE)
    @DeleteMapping("/remove-config")
    public R remove(@RequestParam("storageLocate") @ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
                    @RequestParam("moduleId") @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId) {
        return new R(reportService.removeByModuleId(storageLocate, moduleId));
    }

    @SysLog("复制到其他模块")
    @ApiOperation(value = "复制到其他模块接口", httpMethod = SwaggerConstants.POST)
    @PostMapping("/copy")
    public R copyToModule(@Valid @RequestBody @ApiParam(name = "copyPostDTO", value = "复制到其他模块参数DTO") CopyPostDTO copyPostDTO) {
        return reportService.copy(copyPostDTO);
    }

	/**
	 * 初始化租户 默认存在
	 *
	 * @param templateId      模板id
	 * @param tenantId 租户id
	 * @return List
	 */
	@GetMapping("/initIreportData/{templateId}/{tenantId}")
	public void initIreportData(@PathVariable("templateId") Long templateId, @PathVariable("tenantId") Long tenantId) {
		 reportService.initIreportData(templateId, tenantId);
	}

	/**
	 * 获取报表导出记录
	 */
	@GetMapping("/getIrportConfigInfo/{tenantId}")
	public R<List<ArrayList<String>>> getIrportConfigInfo(@PathVariable("tenantId") Long tenantId) {
		return	new R(reportService.getIrportConfigInfo(tenantId));
	}
}
