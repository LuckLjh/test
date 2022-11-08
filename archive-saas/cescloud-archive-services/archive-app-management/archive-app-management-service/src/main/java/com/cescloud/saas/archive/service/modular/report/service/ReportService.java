
package com.cescloud.saas.archive.service.modular.report.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archivedict.dto.CopyPostDTO;
import com.cescloud.saas.archive.api.modular.report.dto.*;
import com.cescloud.saas.archive.api.modular.report.entity.Report;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 报表定义
 *
 * @author plez
 */
public interface ReportService extends IService<Report> {
	/***
	 * 根据层级获取是否有复合类型
	 * @author plez
	 * @param storageLocate 当前档案名
	 * @return R
	 * @throws ArchiveBusinessException 档案业务异常
	 */
	R getReportType(String storageLocate) throws ArchiveBusinessException;

	/***
	 * 新增报表定义
	 * @author plez
	 * @param reportPostDTO 报表定义对象
	 * @return 报表对象
	 * @throws ArchiveBusinessException 档案业务异常
	 */
	ReportDTO save(ReportPostDTO reportPostDTO) throws ArchiveBusinessException;

	/***
	 * 查询分页
	 * @author plez
	 * @param page 页面
	 * @param reportQueryDTO 报表查询对象
	 * @return 报表对象集合
	 */
    IPage<Report> page(IPage<Report> page, ReportQueryDTO reportQueryDTO);



	/***
	 * 修改报表定义
	 * @author plez
	 * @param reportPutDTO 报表修改对象
	 * @return 报表对象
	 * @throws ArchiveBusinessException 档案业务异常
	 */
    ReportDTO update(ReportPutDTO reportPutDTO) throws ArchiveBusinessException;

	/***
	 * 修改报表定义配置
	 * @author plez
	 * @param reportPutDTO 报表修改配置对象
	 * @return 报表对象
	 * @throws ArchiveBusinessException 档案业务异常
	 */
	ReportDTO updateDeploy(ReportPutDTO reportPutDTO) throws ArchiveBusinessException;

	/***
	 * 通过id删除报表定义
	 * @author plez
	 * @param moduleId 模块id
	 * @param id 报表id
	 * @return R对象
	 * @throws ArchiveBusinessException 档案业务异常
	 */
    R removeById(Long moduleId, Long id) throws ArchiveBusinessException;

	/***
	 * 获取新增报表的关联表
	 * @param storageLocate 关联表
	 * @return 关联表对象集合
	 * @throws ArchiveBusinessException 档案业务异常
	 */
    List<ReportTableDTO> getRelationStorageLocate(String storageLocate)  throws ArchiveBusinessException;

	/***
	 * 导入报表模板文件
	 * @param id 报表id
	 * @param request 请求
	 * @param response 响应
	 * @return R对象
	 * @throws ArchiveBusinessException 档案业务异常
	 */
    R exportReport(Long id ,HttpServletRequest request, HttpServletResponse response) throws ArchiveBusinessException;

	/***
	 * 打印报表
	 * @param id 报表id
	 * @param file 文件对象
	 * @return R对象
	 * @throws ArchiveBusinessException 档案业务异常
	 */
    R importReport(Long id,MultipartFile file) throws ArchiveBusinessException;

	/**
	 * 查询档案门类下所有报表
	 * @param typeCode 档案门类Code
	 * @param templateTableId 档案门类层级
	 * @return 报表名称
	 * @throws ArchiveBusinessException 业务异常
	 */
	String getStorageLocate(String typeCode, Long templateTableId) throws ArchiveBusinessException;

	List<ReportDTO> listByStorageLocate(String storageLocate,Long moduleId);

	/**
	 * 根据模块id和storageLocate 删除 配置信息
	 *
	 * @param storageLocate 物理表名
	 * @param moduleId      模块id
	 * @return
	 */
	Boolean removeByModuleId(String storageLocate, Long moduleId);

	/**
	 * 复制到另一模块
	 * @param copyPostDTO
	 * @return
	 */
	R copy(CopyPostDTO copyPostDTO);

	/**
	 * 复制报表定义的配置
	 * @param srcStorageLocate
	 * @param destStorageLocate
	 * @param srcDestMetadataMap
	 */
	void copyByStorageLocate(String srcStorageLocate, String destStorageLocate, Map<Long, Long> srcDestMetadataMap);

	ReportDTO saveBusiness(ReportPostDTO reportPostDTO) throws ArchiveBusinessException;

	void initIreportData(Long templateId, Long tenantId);

    List<ArrayList<String>> getIrportConfigInfo(Long tenantId);
}
