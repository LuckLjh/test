
package com.cescloud.saas.archive.service.modular.report.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archivedict.dto.CopyPostDTO;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveType;
import com.cescloud.saas.archive.api.modular.businessconfig.entity.BusinessModelDefine;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataTag;
import com.cescloud.saas.archive.api.modular.report.constant.ReportEnum;
import com.cescloud.saas.archive.api.modular.report.dto.*;
import com.cescloud.saas.archive.api.modular.report.entity.Report;
import com.cescloud.saas.archive.api.modular.report.entity.ReportMetadata;
import com.cescloud.saas.archive.api.modular.report.entity.ReportTable;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantTemplate;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantTemplateService;
import com.cescloud.saas.archive.common.constants.*;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveConfigManageService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTypeService;
import com.cescloud.saas.archive.service.modular.businessconfig.service.BusinessModelDefineService;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataService;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataTagService;
import com.cescloud.saas.archive.service.modular.report.mapper.ReportMapper;
import com.cescloud.saas.archive.service.modular.report.service.ReportMetadataService;
import com.cescloud.saas.archive.service.modular.report.service.ReportService;
import com.cescloud.saas.archive.service.modular.report.service.ReportTableService;
import com.cescloud.saas.archive.service.modular.report.util.JasperUtil;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * 报表定义
 *
 * @author plez
 * @date 2019-09-03 13:56:36
 */
@Slf4j
@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    @Autowired
    private ArchiveTableService archiveTableService;

    @Autowired
    private ArchiveTypeService archiveTypeService;

    @Autowired
    private ReportTableService reportTableService;

    @Autowired
    private ReportMetadataService reportMetadataService;

    @Autowired
    private MetadataService metadataService;

    @Autowired
    private BusinessModelDefineService businessModelDefineService;

    @Autowired
   private ArchiveConfigManageService archiveConfigManageService;

	@Autowired
	private MetadataTagService metadataTagService;

	@Autowired
	private RemoteTenantTemplateService remoteTenantTemplateService;

	@Resource
	private ResourceLoader resourceLoader;
    /**
     * 根据层级获取是否有复合类型
     *
     * @param storageLocate 当前层级档案名称
     * @return R
     * @Author plez
     */
    @Override
    public R getReportType(String storageLocate) {
        List<ArchiveTable> archiveTables = archiveTableService.getDownTableByStorageLocateExclude(storageLocate);
        if (CollectionUtil.isEmpty(archiveTables)) {
            return new R<>().success(false, "独立");
        } else {
            return new R<>().success(true, "复合");
        }

    }

    /**
     * 新增报表
     *
     * @return ReportDTO 报表对象
     * @Author plez
     * @Param ReportPostDTO
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportDTO save(ReportPostDTO reportPostDTO) throws ArchiveBusinessException {
        Report report = saveReport(reportPostDTO);
        //保存报表的数据源表
        List<ReportTable> reportTableList = saveReportTable(report, reportPostDTO);
        //保存报表的元数据信息
        List<ReportMetadata> reportMetadataList = saveReportMetadata(reportTableList);
        //创建ireport模板文件
        createIreportFile(report, reportTableList, reportMetadataList);
        archiveConfigManageService.save(reportPostDTO.getStorageLocate(), reportPostDTO.getModuleId(), TypedefEnum.REPORT.getValue());
        log.debug("将报表信息转成前端需要的DTO对象");
        return convertToReportDTO(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportDTO saveBusiness(ReportPostDTO reportPostDTO) throws ArchiveBusinessException {
        Report report = saveReport(reportPostDTO);
        List<ReportMetadata> reportMetadata = saveReportBusinessMetadata(report);
        createIreportFile(report,null,reportMetadata);
        //保存 报表的元数据信息
        return convertToReportDTO(report);
    }

	@Transactional(rollbackFor = Exception.class)
	public ReportDTO saveBusinessInit(ReportPostDTO reportPostDTO,Long tenantId) throws ArchiveBusinessException {
		Report report = saveReportInit(reportPostDTO,tenantId);
		this.saveOrUpdate(report);
		List<ReportMetadata> reportMetadata = saveReportBusinessMetadata(report);
		createIreportFile(report,null,reportMetadata);
		//保存 报表的元数据信息
		return convertToReportDTO(report);
	}
	/**
	 * 获取 初始化模板文件流
	 *
	 * @param templateId 模板id
	 * @return
	 */
	private InputStream getDefaultTemplateStream(Long templateId) {
		TenantTemplate tenantTemplate = remoteTenantTemplateService.selectTemplateById(templateId).getData();
		byte[] bytes = tenantTemplate.getTemplateContent();
		InputStream inputStream = new ByteArrayInputStream(bytes);
		return inputStream;
	}
	@Override
	public void initIreportData(Long templateId, Long tenantId) {
		InputStream inputStream = getDefaultTemplateStream(templateId);
		if (ObjectUtil.isNull(inputStream)) {
			return ;
		}
		//获取档案类型
		final List<ArchiveTable> archiveTables = archiveTableService.list(Wrappers.<ArchiveTable>lambdaQuery().eq(ArchiveTable::getTenantId, tenantId));
		//处理门类信息
		Map<String, String> archiveTableMap = archiveTables.stream().collect(Collectors.toMap(ArchiveTable::getStorageName, ArchiveTable::getStorageLocate));
		@Cleanup ExcelReader excel = new ExcelReader(inputStream, TemplateFieldConstants.SHEET_NAMES.IREPORT_DATA, true);
		List<List<Object>> read = excel.read();
		IntStream.range(1, read.size()).forEach(i -> {
			List<Object> objectList = read.get(i);
			//报表标题
			String reportTopic = StrUtil.toString(objectList.get(0));
			//报表类型
			String reportType = StrUtil.toString(objectList.get(1));
			if(reportType.equals(ReportEnum.TYPE_INDEPENDENT.getName())){
				reportType = ReportEnum.TYPE_INDEPENDENT.getCode();
			}else{
				reportType = ReportEnum.TYPE_COMPLEX.getCode();
			}
			//报表分页行数
			Integer reportPageLines = Integer.valueOf(StrUtil.toString(objectList.get(2)));
			//报表格式
			String reportFormat = StrUtil.toString(objectList.get(3));
			//报表所属档案类型表
			String storageLocates = StrUtil.toString(objectList.get(4));
			ReportPostDTO  reportPostDTO = new  ReportPostDTO();
			reportPostDTO.setModuleId(-1L);
			reportPostDTO.setReportFormat(reportFormat);
			reportPostDTO.setReportPageLines(reportPageLines);
			reportPostDTO.setReportTopic(reportTopic);
			reportPostDTO.setReportType(reportType);
			String[] storageLocatesList = storageLocates.split(",");
			if(storageLocatesList.length>0){
                try {
                    //报表文件内容
                    String initReportValue = StrUtil.toString(objectList.get(5));
                    byte[] initReportByte = initReportValue.getBytes("UTF-8");
                    @Cleanup InputStream sb = new ByteArrayInputStream(initReportByte);
                    @Cleanup ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    byte[] byteXML = new byte[sb.available()];
                    //编译报表
                    JasperCompileManager.compileReportToStream(sb, outputStream);
                    new ByteArrayInputStream(initReportByte).read(byteXML);
                    byte[] byteJasper = outputStream.toByteArray();

                    for(int j=0;j<storageLocatesList.length;j++){
                        String storageLocate = storageLocatesList[j].toString();
                        try {
                            if(archiveTableMap.get(storageLocate) == null){
                                ModelTypeEnum modelType = ModelTypeEnum.getEnumByName(storageLocate);
                                if(modelType !=null){
                                    storageLocate = modelType.getCode();
                                    reportPostDTO.setModuleId(modelType.getValue().longValue());
                                    reportPostDTO.setStorageLocate(storageLocate);
                                    // 调用保存
                                    saveBusinessInit(reportPostDTO,tenantId);
                                }
                            }else{
                                storageLocate = archiveTableMap.get(storageLocate);
                                reportPostDTO.setStorageLocate(storageLocate);
                                // 调用保存
                                saveInit(reportPostDTO,tenantId);
                            }
                            // 根据报表名称，租户表名插询id  调用上传附件接口
                            LambdaQueryWrapper<Report> queryWrapper = Wrappers.<Report>query().lambda();
                            queryWrapper.eq(Report::getStorageLocate, storageLocate);
                            queryWrapper.eq(Report::getTenantId,tenantId);
                            queryWrapper.eq(Report::getReportTopic,reportTopic);
                            Report report = this.getOne(queryWrapper);
                            if(report !=null){
                                //去temp 目录根据文件名读取,-- 废弃，修改为把内容放进excel 中
            /*					String fileName = reportTopic+ReportEnum.ireport_suffix.getCode();
                                String path = "ireportTemplate/" + fileName;
                                org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:" + path);
                                if(resource.exists()){
                                    importReport(report.getId(),getMultipartFile(resource.getFile()));
                                }*/
                                report.setReportPath(byteXML);
                                report.setJasperContext(byteJasper);
                                report.setReportModel(CommonConstants.REPORT_MODEL_EXIST);
                                this.saveOrUpdate(report);
                            }
                        } catch (ArchiveBusinessException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException | JRException e) {
                    e.printStackTrace();
                }
			}

		});
	}

	@Override
	public List<ArrayList<String>> getIrportConfigInfo(Long tenantId) {
		//获取档案类型
		final List<ArchiveTable> archiveTables = archiveTableService.list(Wrappers.<ArchiveTable>lambdaQuery().eq(ArchiveTable::getTenantId, tenantId));
		//处理门类信息
		Map<String, String> archiveTableMap = archiveTables.stream().collect(Collectors.toMap(ArchiveTable::getStorageLocate, ArchiveTable::getStorageName));
		//获取报表配置
		LambdaQueryWrapper<Report> queryWrapper = Wrappers.<Report>query().lambda();
		queryWrapper.eq(Report::getTenantId,tenantId);
		List<Report> reportList = this.list(queryWrapper);
		List<Report> reportListconvert = new ArrayList<>();
		Map<Long, String> convertMess = new HashMap<>();
		if(reportList.size()>0){
			//将表名替换为中文表名称，
			reportList.stream().forEach(report -> {
				String storageLocate ="";
				if(StrUtil.isNotBlank(archiveTableMap.get(report.getStorageLocate()))){
					storageLocate = archiveTableMap.get(report.getStorageLocate());
				}else{
					ModelTypeEnum modelType = ModelTypeEnum.getEnum(report.getStorageLocate());
					if(modelType !=null) {
						storageLocate = modelType.getName();
					}
				}
				report.setStorageLocate(storageLocate);
				//独立或者符合转换
				//报表类型
				String reportType = report.getReportType();
				if(reportType.equals(ReportEnum.TYPE_INDEPENDENT.getCode())){
					reportType = ReportEnum.TYPE_INDEPENDENT.getName();
				}else{
					reportType = ReportEnum.TYPE_COMPLEX.getName();
				}
				report.setReportType(reportType);
				byte[] byteXML = report.getReportPath();
				String byteXMLStr = new String(byteXML);
				convertMess.put(report.getId(),byteXMLStr);
				reportListconvert.add(report);
			});
			//报表标题	报表类型	报表分页行数	报表格式	报表所属档案类型	报表内容
			List<ArrayList<String>> data =  reportListconvert.stream()
					.map(report -> CollUtil.newArrayList(
							report.getReportTopic(),
							report.getReportType(),
							report.getReportPageLines().toString(),
							report.getReportFormat(),
							report.getStorageLocate(),
							convertMess.get(report.getId())
					)).collect(Collectors.toList());
			return data;

		}
		return null;
	}

	@Transactional(rollbackFor = Exception.class)
	public ReportDTO saveInit(ReportPostDTO reportPostDTO,Long tenantId) throws ArchiveBusinessException {
		Report report = saveReportInit(reportPostDTO,tenantId);
		this.saveOrUpdate(report);
		//保存报表的数据源表
		List<ReportTable> reportTableList = saveReportTable(report, reportPostDTO);
		//保存报表的元数据信息
		List<ReportMetadata> reportMetadataList = saveReportMetadata(reportTableList);
		//创建ireport模板文件
		createIreportFile(report, reportTableList, reportMetadataList);
		archiveConfigManageService.saveInit(reportPostDTO.getStorageLocate(), reportPostDTO.getModuleId(), TypedefEnum.REPORT.getValue(),tenantId);
		log.debug("将报表信息转成前端需要的DTO对象");
		return convertToReportDTO(report);
	}

	private Report saveReportInit(ReportPostDTO reportPostDTO, Long tenantId) throws ArchiveBusinessException {
		log.debug("新增报表，报表名称为{}" + reportPostDTO.getReportTopic());
		List<Report> reports = this.list(Wrappers.<Report>lambdaQuery().eq(Report::getStorageLocate, reportPostDTO.getStorageLocate())
				.eq(Report::getModuleId, reportPostDTO.getModuleId()).eq(Report::getReportTopic, reportPostDTO.getReportTopic())
				.eq(Report::getTenantId, tenantId));
		if (reports.size() == 0) {
			Report report = new Report();
			report.setTenantId(tenantId);
			//赋值
			BeanUtil.copyProperties(reportPostDTO, report);
			report.setReportModel(CommonConstants.REPORT_MODEL_NOT_EXIST);
			int insertResult = this.getBaseMapper().insert(report);
			if (insertResult == 0) {
				log.error("新增报表失败！失败信息为{}" + report.toString());
				throw new ArchiveBusinessException("新增报表失败!" + report.toString());
			}
			return report;
		}else{
			return reports.get(0);
		}
	}

	public static MultipartFile getMultipartFile(File file) {
		FileItem item = new DiskFileItemFactory().createItem("file"
				, MediaType.MULTIPART_FORM_DATA_VALUE
				, true
				, file.getName());
		try (InputStream input = new FileInputStream(file);
			 OutputStream os = item.getOutputStream()) {
			// 流转移
			IOUtils.copy(input, os);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid file: " + e, e);
		}

		return new CommonsMultipartFile(item);
	}

	private List<ReportMetadata> saveReportBusinessMetadata(Report report) throws ArchiveBusinessException {
        String storageLocate = report.getStorageLocate();
        ModelTypeEnum anEnum = ModelTypeEnum.getEnum(storageLocate);
        List<BusinessModelDefine> businessModelDefines = businessModelDefineService.getBusinessModelDefines(anEnum.getValue(),null);
        List<ReportMetadata> reportMetadata = businessModelDefines.stream().map(mode -> ReportMetadata.builder()
				.storageLocate(storageLocate).tenantId(report.getTenantId())
				.metadataChinese("业务_"+mode.getMetadataChinese())
				.metadataEnglish(mode.getMetadataEnglish()).build()).collect(Collectors.toList());
        if (CommonConstants.REPORT_TYPE_COMPLEX.equals(report.getReportType())) {
            String detailStorageLocate= "de";
			//全部获取元数据标签的值
			final List<MetadataTag> metadataTags = metadataTagService.list(Wrappers.<MetadataTag>lambdaQuery().eq(MetadataTag::getTenantId, report.getTenantId()));
			for (MetadataTag metadataTag : metadataTags) {
				reportMetadata.add(ReportMetadata.builder().storageLocate(detailStorageLocate)
						.metadataChinese(metadataTag.getTagChinese()).metadataEnglish(metadataTag.getTagEnglish())
						.tenantId(report.getTenantId()).build());
			}
        }
        return reportMetadata;
    }

    private Report saveReport(ReportPostDTO reportPostDTO) throws ArchiveBusinessException {
        log.debug("新增报表，报表名称为{}" + reportPostDTO.getReportTopic());
        List<Report> reports = this.list(Wrappers.<Report>lambdaQuery().eq(Report::getStorageLocate, reportPostDTO.getStorageLocate()).eq(Report::getModuleId, reportPostDTO.getModuleId()).eq(Report::getReportTopic, reportPostDTO.getReportTopic()));
        if (CollectionUtil.isNotEmpty(reports)) {
            log.error("新增报表失败，相同模块报表名称不允许相同！！");
            throw new ArchiveBusinessException("新增报表失败，相同模块报表名称不允许相同！！");
        }
        Report report = new Report();
        //赋值
        BeanUtil.copyProperties(reportPostDTO, report);
        report.setReportModel(CommonConstants.REPORT_MODEL_NOT_EXIST);
        int insertResult = this.getBaseMapper().insert(report);
        if (insertResult == 0) {
            log.error("新增报表失败！失败信息为{}" + report.toString());
            throw new ArchiveBusinessException("新增报表失败!" + report.toString());
        }
        return report;
    }

    /**
     * @return List 报表数据源集合
     * @Author xieanzhu
     * @Description 保存报表的数据源表
     * @Param reportDTO
     * @Param report 报表对象
     */
    private List<ReportTable> saveReportTable(Report report, ReportPostDTO reportPostDTO) throws ArchiveBusinessException {
        List<ReportTable> reportTableList = new ArrayList<>();
        List<String> storageLocateList = new ArrayList<>();
        //如果有关联数据表
        if (CommonConstants.REPORT_TYPE_COMPLEX.equals(reportPostDTO.getReportType())) {
            String archiveTableName = reportPostDTO.getStorageLocate();
            //获取下一级数据表
            List<ArchiveTable> archiveTableLists = archiveTableService.getDownTableByStorageLocateExclude(archiveTableName);
            if (ObjectUtil.isNotEmpty(archiveTableLists)) {
                archiveTableLists.forEach(archiveTable -> {
                    storageLocateList.add(archiveTable.getStorageLocate());
                });
            }
        }
        storageLocateList.add(report.getStorageLocate());
        Map<String, ArchiveTable> archiveTableMap = CollectionUtil.newHashMap();
        if (CollectionUtil.isNotEmpty(storageLocateList)) {
            List<ArchiveTable> tableByStorageLocates = archiveTableService.getTableByStorageLocates(storageLocateList);
            archiveTableMap = tableByStorageLocates.stream().collect(Collectors.toMap(ArchiveTable::getStorageLocate, table -> table));
        }

        for (String storageLocate : storageLocateList) {
            ReportTable reportTable = new ReportTable();
            reportTable.setReportId(report.getId());
            reportTable.setStorageLocate(storageLocate);
            ArchiveTable tableByStorageLocate = archiveTableMap.get(storageLocate);
            assert tableByStorageLocate != null;
            reportTable.setStorageName(tableByStorageLocate.getStorageName());
			reportTable.setTenantId(report.getTenantId());
            reportTableList.add(reportTable);
        }

        log.debug("保存报表的数据源表,表名为{}" + storageLocateList.toString());
        reportTableService.saveBatch(reportTableList);
        return reportTableList;

    }

    /**
     * @return void
     * @Author xieanzhu
     * @Description 保存报表的元数据信息
     * @Date 19:09 2019/4/29
     * @Param [reportDTO]
     **/
    private List<ReportMetadata> saveReportMetadata(List<ReportTable> reportTableList) {
        log.debug("保存报表的元数据信息");
        List<ReportMetadata> reportMetadataList = new ArrayList<>();
        reportTableList.forEach(reportTable -> {
            List<Metadata> metadataList = metadataService.listByStorageLocate(reportTable.getStorageLocate());
            metadataList.forEach(metadata -> {
                ReportMetadata reportMetadata = new ReportMetadata();
                reportMetadata.setReportId(reportTable.getReportId());
                reportMetadata.setStorageLocate(reportTable.getStorageLocate());
                reportMetadata.setMetadataChinese(metadata.getMetadataChinese());
                reportMetadata.setMetadataEnglish(metadata.getMetadataEnglish());
				reportMetadata.setTenantId(metadata.getTenantId());
                reportMetadataList.add(reportMetadata);
            });
        });
        reportMetadataService.saveBatch(reportMetadataList);
        return reportMetadataList;
    }

    /***
     * @Author plez
     * @Description 创建iReport模板文件
     * @Param [report]
     */
    private void createIreportFile(Report report, List<ReportTable> reportTableList, List<ReportMetadata> reportMetadataList) throws ArchiveBusinessException {
        log.debug("创建ireport模板文件");
        String reportModelStr = JasperUtil.getiReportModelStr(report.getReportTopic(), reportTableList, reportMetadataList, report);
        try {
            report.setReportPath(reportModelStr.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new ArchiveBusinessException("报表模板文件转byte报错！");
        }
        boolean result = this.updateById(report);
        if (!result) {
            throw new ArchiveBusinessException("往数据库插入报表模板文件报错！");
        }
    }

    /**
     * @return com.cescloud.saas.archive.api.modular.report.dto.ReportDTO
     * @Author xieanzhu
     * @Description //修改报表配置
     * @Date 14:12 2019/5/5
     * @Param [reportDTO]
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportDTO update(ReportPutDTO reportPutDTO) throws ArchiveBusinessException {
        log.debug("修改报表配置，报表名称为{}" + reportPutDTO.getReportTopic());
        return getReportDTO(reportPutDTO);
    }

    /**
     * @Description 报表定义配置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportDTO updateDeploy(ReportPutDTO reportPutDTO) throws ArchiveBusinessException {
        log.debug("修改报表配置，报表分组字段为{}" + reportPutDTO.getPageField() + "报表分页行数为{}" + reportPutDTO.getReportPageLines());
        return getReportDTO(reportPutDTO);
    }

    /**
     * 修改报表公共方法
     *
     * @param reportPutDTO 报表修改对象
     * @return reportDTO
     * @throws ArchiveBusinessException 业务异常
     */
    private ReportDTO getReportDTO(ReportPutDTO reportPutDTO) throws ArchiveBusinessException {
        Report report = this.getById(reportPutDTO.getId());
        if (report == null) {
            log.error("报表不存在！id为{}" + reportPutDTO.getId().toString());
            throw new ArchiveBusinessException("报表不存在！id为" + reportPutDTO.getId());
        }
        List<Report> reports = this.list(Wrappers.<Report>lambdaQuery().eq(Report::getModuleId, report.getModuleId()).eq(Report::getStorageLocate, report.getStorageLocate()).ne(Report::getId, report.getId()).eq(Report::getReportTopic, reportPutDTO.getReportTopic()));
        if (CollectionUtil.isNotEmpty(reports)) {
            log.error("修改报表失败，相同模块报表名称不允许相同！！");
            throw new ArchiveBusinessException("修改报表失败，相同模块报表名称不允许相同！！");
        }
        BeanUtil.copyProperties(reportPutDTO, report, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
        boolean result = this.updateById(report);
        if (!result) {
            log.error("修改报表失败！");
            throw new ArchiveBusinessException("修改报表失败！");
        }
        return convertToReportDTO(report);
    }

    /***
     * 分页查询报表配置
     * @Author plez
     * @Date 16:19 2019/5/13
     * @Param [page, reportQueryDTO]
     * @return R
     **/
    @Override
    public IPage<Report> page(IPage<Report> page, ReportQueryDTO reportQueryDTO) {
        log.debug("分页查询，条件为表名={}，报表名称={}" + reportQueryDTO.getStorageLocate(), StrUtil.trim(reportQueryDTO.getReportTopic()));
        QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
		queryWrapper.select(Report.class,info ->
				!info.getColumn().equals("jasper_context") && !info.getColumn().equals("report_path")
		);
        if (StrUtil.isNotBlank(reportQueryDTO.getReportTopic())) {
            queryWrapper.like("report_topic", StrUtil.trim(reportQueryDTO.getReportTopic()));
        }
        if (StrUtil.isNotBlank(reportQueryDTO.getStorageLocate())) {
            queryWrapper.eq("storage_locate", reportQueryDTO.getStorageLocate());
        }
        return this.getBaseMapper().selectPage(page, queryWrapper);
    }


    /**
     * @return com.cescloud.saas.archive.api.modular.report.dto.ReportDTO
     * @Author xieanzhu
     * @Description //获取新增报表的关联表
     * @Date 14:26 2019/4/30
     * @Param [storageLocate]
     **/
    @Override
    public List<ReportTableDTO> getRelationStorageLocate(String storageLocate) throws ArchiveBusinessException {
        log.debug("通过表名{}获取新增报表的关联表" + storageLocate);
        ArchiveTable archiveTable = archiveTableService.getTableByStorageLocate(storageLocate);
        ArchiveType archiveType = archiveTypeService.getByTypeCode(archiveTable.getArchiveTypeCode());
        List<ReportTableDTO> reportTableDTOList = new ArrayList<>();
        if (archiveType.getFilingType().equals(FilingTypeEnum.FOLDER.getCode())) {
            //案卷层  如果是案卷表 关联表为文件 如果是文件表 关联表是 案卷表
            if (archiveTable.getArchiveLayer().equals(ArchiveLayerEnum.FOLDER.getValue())) {
                //案卷表
                List<ArchiveTable> archiveTableList = archiveTableService.getDownTableByStorageLocateAndDownLayerCode(storageLocate, archiveTable.getArchiveTypeCode());
                archiveTableList.forEach(downArchiveTable -> {
                    ReportTableDTO reportTableDTO = new ReportTableDTO();
                    reportTableDTO.setStorageLocate(downArchiveTable.getStorageLocate());
                    reportTableDTO.setStorageLocateName(downArchiveTable.getStorageName());
                    reportTableDTOList.add(reportTableDTO);
                });
                return reportTableDTOList;
            } else if (archiveTable.getArchiveLayer().equals(ArchiveLayerEnum.FILE.getValue())) {
                //文件表
                ArchiveTable tableFile = archiveTableService.getUpTableByStorageLocate(storageLocate);
                ReportTableDTO reportTableDTO = new ReportTableDTO();
                reportTableDTO.setStorageLocate(tableFile.getStorageLocate());
                reportTableDTO.setStorageLocateName(tableFile.getStorageName());
                reportTableDTOList.add(reportTableDTO);
                return reportTableDTOList;
            }
        } else if (archiveType.getFilingType().equals(FilingTypeEnum.PROJECT.getCode())) {
            //项目层 如果是项目表或者文件表 关联表为案卷表 如果是案卷表 关联表为项目表和文件表
            //项目表
            if (archiveTable.getArchiveLayer().equals(ArchiveLayerEnum.PROJECT.getValue())) {
                List<ArchiveTable> archiveTableList = archiveTableService.
                        getDownTableByStorageLocateAndDownLayerCode(storageLocate, archiveTable.getArchiveTypeCode());
                archiveTableList.forEach(downArchiveTable -> {
                    ReportTableDTO reportTableDTO = new ReportTableDTO();
                    reportTableDTO.setStorageLocate(downArchiveTable.getStorageLocate());
                    reportTableDTO.setStorageLocateName(downArchiveTable.getStorageName());
                    reportTableDTOList.add(reportTableDTO);
                });
                return reportTableDTOList;
            } else if (archiveTable.getArchiveLayer().equals(ArchiveLayerEnum.FILE.getValue())) {
                // 文件表
                ArchiveTable tableFolder = archiveTableService.getUpTableByStorageLocate(storageLocate);
                ReportTableDTO reportTableDTO = new ReportTableDTO();
                reportTableDTO.setStorageLocate(tableFolder.getStorageLocate());
                reportTableDTO.setStorageLocateName(tableFolder.getStorageName());
                reportTableDTOList.add(reportTableDTO);
                return reportTableDTOList;
            } else if (archiveTable.getArchiveLayer().equals(ArchiveLayerEnum.FOLDER.getValue())) {
                //案卷表
                ArchiveTable tableProject = archiveTableService.getUpTableByStorageLocate(storageLocate);
                ReportTableDTO reportTableProjectDTO = new ReportTableDTO();
                reportTableProjectDTO.setStorageLocate(tableProject.getStorageLocate());
                reportTableProjectDTO.setStorageLocateName(tableProject.getStorageName());
                reportTableDTOList.add(reportTableProjectDTO);
                List<ArchiveTable> tableFileList = archiveTableService.
                        getDownTableByStorageLocateAndDownLayerCode(storageLocate, archiveTable.getArchiveTypeCode());
                tableFileList.forEach(tableFile -> {
                    ReportTableDTO reportTableFileDTO = new ReportTableDTO();
                    reportTableFileDTO.setStorageLocate(tableFile.getStorageLocate());
                    reportTableFileDTO.setStorageLocateName(tableFile.getStorageName());
                    reportTableDTOList.add(reportTableProjectDTO);
                });
                return reportTableDTOList;
            }
        }
        return reportTableDTOList;
    }

    /**
     * @return com.cescloud.saas.archive.api.modular.report.dto.ReportDTO
     * @Author xieanzhu
     * @Description //将report转成前端需要的DTO对象
     * @Date 13:47 2019/5/5
     * @Param [report]
     **/
    private ReportDTO convertToReportDTO(Report report) {
        ReportDTO reportDTO = new ReportDTO();
        BeanUtil.copyProperties(report, reportDTO);
        if (StrUtil.isEmpty(report.getReportModel())) {
            reportDTO.setReportModel(CommonConstants.REPORT_MODEL_NOT_EXIST);
        }
        return reportDTO;
    }

    /**
     * @return com.cescloud.saas.archive.service.modular.common.core.util.R
     * @Author xieanzhu
     * @Description //删除报表配置
     * @Date 15:18 2019/5/5
     * @Param [id]
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R removeById(Long moduleId, Long id) throws ArchiveBusinessException {
        log.debug("删除报表配置，报表id为{}" + id.toString());
        final Report report = this.getById(id);
        final String storageLocate = report.getStorageLocate();
        boolean flag = this.removeById(id);
        if (!flag) {
            log.error("删除报表配置失败，id不存在！");
            throw new ArchiveBusinessException("删除报表配置失败，id不存在");
        }
        final int count = this.count(Wrappers.<Report>lambdaQuery().eq(Report::getModuleId, moduleId).eq(Report::getStorageLocate, storageLocate));
        if (count == 0) {
            archiveConfigManageService.update(storageLocate, moduleId, TypedefEnum.REPORT.getValue(), 0);
        }
        log.debug("删除报表关联表配置，报表id为{}" + id.toString());
        reportTableService.removeByReportId(id);
        log.debug("删除报表关联元数据配置，报表id为{}" + id.toString());
        reportMetadataService.removeByReportId(id);
        return new R().success(null, "删除报表配置成功！");
    }


    /**
     * @return com.cescloud.saas.archive.service.modular.common.core.util.R
     * @Author xieanzhu
     * @Description 导出报表空模板
     * @Date 11:08 2019/5/5
     * @Param [id, request, response]
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R exportReport(Long id, HttpServletRequest request, HttpServletResponse response) throws ArchiveBusinessException {
        log.debug("导出报表id为{}的空模板" + id.toString());
        Report report = this.getById(id);
        if (report == null) {
            log.error("报表不存在，报表id为{}" + id.toString());
            throw new ArchiveBusinessException("报表不存在，报表id为" + id.toString());
        }
        byte[] reportPath = report.getReportPath();
        try {
            String reportTopic = java.net.URLEncoder.encode(report.getReportTopic(), "UTF-8");
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/jrxml");
            response.setHeader("Content-Disposition", "attachment;filename=" + reportTopic + ".jrxml");
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(reportPath);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            log.error("导出报表空模板失败-" + e.getMessage(), e);
            throw new ArchiveBusinessException("导出报表空模板失败！" + e.getMessage());
        }
        return new R().success(null, "导出报表空模板成功！");
    }

    /**
     * @return com.cescloud.saas.archive.service.modular.common.core.util.R
     * @Author xieanzhu
     * @Description //报表模板导入
     * @Date 16:30 2019/5/5
     * @Param [file]
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R importReport(Long id, MultipartFile file) throws ArchiveBusinessException {
        Report report = this.getById(id);
        try {
            @Cleanup ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            @Cleanup InputStream inputStream = file.getInputStream();
            byte[] byteXML = new byte[inputStream.available()];
            //编译报表
            JasperCompileManager.compileReportToStream(inputStream, outputStream);
            file.getInputStream().read(byteXML);
            byte[] byteJasper = outputStream.toByteArray();
            report.setReportPath(byteXML);
            report.setJasperContext(byteJasper);
            report.setReportModel(CommonConstants.REPORT_MODEL_EXIST);
        } catch (IOException e) {
            log.error("导入了错误的报表！", e);
            throw new ArchiveBusinessException("请导入正确报表！");
        } catch (JRException e) {
            log.error("导入的报表编译失败！", e);
            throw new ArchiveBusinessException("报表编译出错，请导入正确报表！");
        }
        boolean result = this.updateById(report);
        if (!result) {
            throw new ArchiveBusinessException("往数据库导入报表模板文件报错！");
        }
        return new R().success(null, "报表模板导入成功！");
    }

    /**
     * 查询档案门类下所有报表
     *
     * @param typeCode        档案门类Code
     * @param templateTableId 档案门类模板id
     * @return 报表名称
     * @throws ArchiveBusinessException 业务异常
     */
    @Override
    public String getStorageLocate(String typeCode, Long templateTableId) throws ArchiveBusinessException {
        return archiveTableService.getTableByArchiveTypeCodeAndTemplateTableId(typeCode, templateTableId).getStorageLocate();
    }

    /**
     * @return java.util.List<com.cescloud.saas.archive.api.modular.report.dto.ReportDTO>
     * @Author xieanzhu
     * @Description //根据表名查询所有报表
     * @Date 13:31 2019/5/5
     * @Param [storageLocate]
     **/
    @Override
    public List<ReportDTO> listByStorageLocate(String storageLocate, Long moduleId) {
        log.debug("根据表名{}查询所有报表", storageLocate);
		LambdaQueryWrapper<Report> queryWrapper = Wrappers.<Report>query().lambda();
		queryWrapper.select(Report.class,info ->
				!info.getColumn().equals("jasper_context") && !info.getColumn().equals("report_path")
		).eq(Report::getStorageLocate, storageLocate).eq(Report::getModuleId, moduleId);
        List<Report> reportList = this.list(queryWrapper);
        List<ReportDTO> reportDTOList = new ArrayList<>(reportList.size());
        reportList.stream().forEach(report -> {
            ReportDTO reportDTO = new ReportDTO();
            BeanUtil.copyProperties(report, reportDTO);
            if (StrUtil.isEmpty(report.getReportModel())) {
                reportDTO.setReportModel(CommonConstants.REPORT_MODEL_NOT_EXIST);
            }
            reportDTOList.add(reportDTO);
        });
        return reportDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeByModuleId(String storageLocate, Long moduleId) {
        final List<Report> list = this.list(Wrappers.<Report>lambdaQuery().eq(Report::getStorageLocate, storageLocate).eq(Report::getModuleId, moduleId));
        if (CollectionUtil.isEmpty(list)) {
            return Boolean.TRUE;
        }
        final List<Long> ids = list.stream().map(report -> report.getId()).collect(Collectors.toList());
        log.debug("删除报表关联表配置，报表ids为{}" + ids.toString());
        reportTableService.removeByReportIds(ids);
        log.debug("删除报表关联元数据配置，报表ids为{}" + ids.toString());
        reportMetadataService.removeByReportIds(ids);
        boolean result = this.removeByIds(ids);
        archiveConfigManageService.update(storageLocate, moduleId, TypedefEnum.REPORT.getValue(), BoolEnum.NO.getCode());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R copy(CopyPostDTO copyPostDTO) {
        final Long sourceModuleId = copyPostDTO.getSourceModuleId();
        final String storageLocate = copyPostDTO.getStorageLocate();
        final List<Long> targetModuleIds = copyPostDTO.getTargetModuleIds();
        if (CollectionUtil.isNotEmpty(targetModuleIds)) {
            final List<Report> list = this.list(Wrappers.<Report>lambdaQuery().in(Report::getModuleId, targetModuleIds).eq(Report::getStorageLocate, storageLocate));
            final List<Long> ids = list.stream().map(report -> report.getId()).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(ids)) {
                reportTableService.remove(Wrappers.<ReportTable>lambdaQuery().in(ReportTable::getReportId, ids));
                reportMetadataService.remove(Wrappers.<ReportMetadata>lambdaQuery().in(ReportMetadata::getReportId, ids));
                this.removeByIds(ids);
            }
        }
        List<Report> sourceReports = this.list(Wrappers.<Report>lambdaQuery().eq(Report::getModuleId, sourceModuleId).eq(Report::getStorageLocate, storageLocate));
        if (CollectionUtil.isEmpty(sourceReports)) {
            return new R().fail(null, "当前模块无信息可复制，请先配置当前模块信息。");
        }
        List<Long> sourceIds = sourceReports.stream().map(report -> report.getId()).collect(Collectors.toList());
        List<Report> targetReports = CollectionUtil.newArrayList();
        targetModuleIds.parallelStream().forEach(moduleId -> {
            sourceReports.stream().forEach(report -> {
                Report targetReport = new Report();
                BeanUtil.copyProperties(report, targetReport);
                targetReport.setId(null);
                targetReport.setModuleId(moduleId);
                targetReports.add(targetReport);
            });
        });
        if (CollectionUtil.isNotEmpty(targetReports)) {
            this.saveBatch(targetReports);
            List<ReportTable> reportTables = reportTableService.list(Wrappers.<ReportTable>lambdaQuery().in(ReportTable::getReportId, sourceIds));
            Map<String, List<ReportTable>> topicAndTableMap = sourceReports.stream().collect(Collectors.toMap(Report::getReportTopic, report -> {
                return reportTables.stream().filter(reportTable -> report.getId().equals(reportTable.getReportId())).collect(Collectors.toList());
            }));
            List<ReportMetadata> reportMetadatas = reportMetadataService.list(Wrappers.<ReportMetadata>lambdaQuery().in(ReportMetadata::getReportId, sourceIds));
            Map<String, List<ReportMetadata>> topicAndMetadataMap = sourceReports.stream().collect(Collectors.toMap(Report::getReportTopic, report -> {
                return reportMetadatas.stream().filter(reportTable -> report.getId().equals(reportTable.getReportId())).collect(Collectors.toList());
            }));
            List<ReportMetadata> reportMetadataList = CollectionUtil.newArrayList();
            List<ReportTable> reportTableList = CollectionUtil.newArrayList();
            targetReports.parallelStream().forEach(report -> {
                topicAndTableMap.get(report.getReportTopic()).stream().forEach(reportTable -> {
                    ReportTable reportTable1 = new ReportTable();
                    BeanUtil.copyProperties(reportTable, reportTable1);
                    reportTable1.setId(null);
                    reportTable1.setReportId(report.getId());
                    reportTableList.add(reportTable1);
                });
                topicAndMetadataMap.get(report.getReportTopic()).stream().forEach(reportMetadata -> {
                    ReportMetadata reportMetadata1 = new ReportMetadata();
                    BeanUtil.copyProperties(reportMetadata, reportMetadata1);
                    reportMetadata1.setId(null);
                    reportMetadata1.setReportId(report.getId());
                    reportMetadataList.add(reportMetadata1);
                });
            });
            if (CollectionUtil.isNotEmpty(reportTableList)) {
                reportTableService.saveBatch(reportTables);
            }
            if (CollectionUtil.isNotEmpty(reportMetadataList)) {
                reportMetadataService.saveBatch(reportMetadataList);
            }
        }
        archiveConfigManageService.saveBatchByModuleIds(storageLocate, targetModuleIds, TypedefEnum.REPORT.getValue());
        return new R(null, "复制成功！");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void copyByStorageLocate(String srcStorageLocate, String destStorageLocate, Map<Long, Long> srcDestMetadataMap) {
        // 保存主表配置
        List<Report> srcList = this.list(Wrappers.<Report>lambdaQuery().eq(Report::getStorageLocate, srcStorageLocate));
        if (CollectionUtil.isNotEmpty(srcList)) {
            List<Report> destList = srcList.stream().map(e -> {
                Report report = new Report();
                BeanUtil.copyProperties(e, report);
                report.setId(null);
                report.setStorageLocate(destStorageLocate);
                return report;
            }).collect(Collectors.toList());
            this.saveBatch(destList);
            final Map<Long, Long> srcDestReportIdMap = MapUtil.newHashMap();
            for (int i = 0, len = srcList.size(); i < len; i++) {
                srcDestReportIdMap.put(srcList.get(i).getId(), destList.get(i).getId());
            }
            // 保存从表配置
            reportMetadataService.copyByStorageLocate(srcStorageLocate, destStorageLocate, srcDestReportIdMap);
            reportTableService.copyByStorageLocate(srcStorageLocate, destStorageLocate, srcDestReportIdMap);
        }
    }

}
