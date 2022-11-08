package com.cescloud.saas.archive.service.modular.archiveconfig.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.appraise.feign.RemoteDisposalAppraisaService;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.WatermarkDTO;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.Watermark;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.WatermarkDetail;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.businessconfig.entity.BusinessStyleSetting;
import com.cescloud.saas.archive.api.modular.businessconfig.feign.RemoteBusinessStyleSettingService;
import com.cescloud.saas.archive.api.modular.dept.entity.SysDept;
import com.cescloud.saas.archive.api.modular.dept.feign.RemoteDeptService;
import com.cescloud.saas.archive.api.modular.destory.feign.RemoteDestoryService;
import com.cescloud.saas.archive.api.modular.filecenter.entity.OtherFileStorage;
import com.cescloud.saas.archive.api.modular.filecenter.feign.RemoteOtherFileStorageService;
import com.cescloud.saas.archive.api.modular.fileview.feign.RemoteFileViewService;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.api.modular.storage.constants.StorageConstants;
import com.cescloud.saas.archive.api.modular.syssetting.entity.SysSetting;
import com.cescloud.saas.archive.api.modular.syssetting.feign.RemoteSysSettingService;
import com.cescloud.saas.archive.api.modular.syssetting.support.SysSettingCacheHolder;
import com.cescloud.saas.archive.api.modular.transfer.feign.RemoteTransferService;
import com.cescloud.saas.archive.api.modular.using.feign.RemoteArchiveUsingService;
import com.cescloud.saas.archive.common.constants.ArchiveModuleEnum;
import com.cescloud.saas.archive.common.constants.BoolEnum;
import com.cescloud.saas.archive.common.constants.BrowseFileTypeEnum;
import com.cescloud.saas.archive.common.constants.FieldConstants;
import com.cescloud.saas.archive.common.constants.SysSettingCodeEnum;
import com.cescloud.saas.archive.common.constants.TypedefEnum;
import com.cescloud.saas.archive.common.constants.business.UsingTypeEnum;
import com.cescloud.saas.archive.common.util.ArchiveUtil;
import com.cescloud.saas.archive.common.util.DateUtil;
import com.cescloud.saas.archive.service.modular.archiveconfig.mapper.WatermarkMapper;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveConfigManageService;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.WatermarkDetailService;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.WatermarkService;
import com.cescloud.saas.archive.service.modular.archiveconfig.util.HtmlImageGenerator;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.enums.WatermarkCustomizeEnum;
import com.cescloud.saas.archive.service.modular.common.core.util.AddrUtil;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.filecenter.service.FileStorageOpenService;
import com.cescloud.saas.archive.service.modular.filecenter.service.OtherFileStorageOpenService;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataService;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.qrcode.EncodeHintType;
import com.itextpdf.text.pdf.qrcode.ErrorCorrectionLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WatermarkServiceImpl extends ServiceImpl<WatermarkMapper, Watermark> implements WatermarkService {

	@Autowired
	private RemoteSysSettingService remoteSysSettingService;

	@Autowired
	private SysSettingCacheHolder cacheHolder;

	@Autowired
	private RemoteFileViewService remoteFileViewService;

	@Autowired
	private MetadataService metadataService;

	@Autowired
	private ArchiveTableService archiveTableService;

	@Autowired
	private ArchiveConfigManageService archiveConfigManageService;

	@Autowired
	private FileStorageOpenService fileStorageOpenService;

	@Autowired
	private WatermarkDetailService watermarkDetailService;

	@Autowired
	private RemoteArchiveUsingService remoteArchiveUsingService;

	@Autowired
	private RemoteDisposalAppraisaService remoteDisposalAppraisaService;

	@Autowired
	private RemoteDestoryService remoteDestoryService;

	@Autowired
	private RemoteTransferService remoteTransferService;

	@Autowired
	private RemoteDeptService remoteDeptService;

	@Autowired
	private ArchiveUtil archiveUtil;


	@Autowired
	private RemoteOtherFileStorageService remoteotherFileStorageOpenService;

	@Autowired
	private OtherFileStorageOpenService otherFileStorageOpenService;

	@Autowired
	private RemoteBusinessStyleSettingService remoteBusinessStyleSettingService;



	//水印类型
	private final static String WATERTYPE_TEXT = "text";
	private final static String WATERTYPE_PICTURE = "picture";
	private final static String WATERTYPE_QRCODE = "code";
	private final static String WATERTYPE_SEAL = "seal";
	//文字水印类型
	private final static String WATERTYPE_TEXT_TEXT = "input";
	private final static String WATERTYPE_TEXT_FIELD = "select";

	private final static String WATERMARKFORMAT_DOC = "doc";
	private final static String WATERMARKFORMAT_PIC = "pic";

	/**
	 * @param storageLocate 表名称
	 * @return java.util.List<Watermark>
	 * @Description 列出水印配置列表
	 * @author qianbaocheng
	 * @date 2020-9-28 18:23
	 */
	@Override
	public List<Watermark> listWatermark(String storageLocate, String keyword) {
		LambdaQueryWrapper<Watermark> queryWrapper = Wrappers.<Watermark>query().lambda().eq(Watermark::getStorageLocate, storageLocate);
		if (StrUtil.isNotBlank(keyword)) {
			queryWrapper.like(Watermark::getWatermarkName, StrUtil.trim(keyword));
		}
		return this.list(queryWrapper);
	}


	@Override
	public Watermark getDefaultWatermark(String storageLocate,int waterClassification,String watermarkFormat) {
		LambdaQueryWrapper<Watermark> queryWrapper = Wrappers.<Watermark>query().lambda()
				.eq(Watermark::getStorageLocate, storageLocate)
				.eq(Watermark::getWaterClassification,waterClassification)
				.eq(Watermark::getWatermarkFormat,watermarkFormat)
				.eq(Watermark::getIsDefault, true);
		return this.getOne(queryWrapper, false);
	}

	@Override
	public List<WatermarkDetail> getDefaultWatermarkDetail(WatermarkDTO watermarkDTO) {
		int browsing = getBrowseType(watermarkDTO.getModuleId());//根据moduleId判断那个加水印方式
		Long tenantId = SecurityUtils.getUser().getTenantId();
		if (BrowseFileTypeEnum.OTHER.getCode().equals(browsing)) {
			log.info("水印配置:其他附件分类不加水印");
			return new ArrayList<>();
		}
		//档案和流程模块电子文件浏览
		Boolean isOpenWatermark = isOpenWatermark(tenantId);//是否开启水印配置
		watermarkDTO.setAttachmentType(browsing);
		if (isOpenWatermark && ObjectUtil.isNotEmpty(watermarkDTO.getStorageLocate()) && !"all".equals(watermarkDTO.getStorageLocate())) {//开启配置和水印表名参数
			String storageLocate = watermarkDTO.getStorageLocate();
			Watermark defaultWatermark = getDefaultWatermark(storageLocate, browsing,"doc");
			if (ObjectUtil.isNotNull(defaultWatermark)){
				Long defaultWatermarkId = defaultWatermark.getId();
				return getWatermarkDetailResult(defaultWatermarkId,watermarkDTO);
			}
		}
		return new ArrayList<>();
	}

	private Boolean isOpenWatermark(Long tenantId) {
		boolean isOpenWatermark = false;
		//先从缓存判断水印参数是否开启
		Optional<String> optional = cacheHolder.getCacheStrByKey(SysSettingCodeEnum.IS_OPEN_WATERMARK.getCode());
		if (optional.isPresent()) {
			isOpenWatermark = BoolEnum.YES.getCode().toString().equals(optional.get());
		} else {//缓存中没有就用feign接口去取值
			final R<SysSetting> remoteData = remoteSysSettingService.getSysSettingByTenantIdAndCode(tenantId, SysSettingCodeEnum.IS_OPEN_WATERMARK.getCode(), SecurityConstants.FROM_IN);
			if (CommonConstants.FAIL.equals(remoteData.getCode())) {
				log.info("获取[水印]参数值失败");
			} else {
				SysSetting SysSetting = remoteData.getData();
				isOpenWatermark = BoolEnum.YES.getCode().toString().equals(Optional.ofNullable(SysSetting.getValue()).orElseGet(()->BoolEnum.YES.getCode().toString()));
			}
		}
		return isOpenWatermark;
	}

	private int getBrowseType(Long moduleId) {
		if (ArchiveModuleEnum.FILE_MANAGE.getModuleId().equals(moduleId) ||
				ArchiveModuleEnum.ARCHIVE_FILING.getModuleId().equals(moduleId) ||
				ArchiveModuleEnum.ARCHIVE_RECEIVE.getModuleId().equals(moduleId) ||
				ArchiveModuleEnum.ARCHIVE_MAINTAIN.getModuleId().equals(moduleId) ||
				ArchiveModuleEnum.ARCHIVE_EDITOR_FILE.getModuleId().equals(moduleId) ||
				ArchiveModuleEnum.ARCHIVE_SEARCH.getModuleId().equals(moduleId)||
				ArchiveModuleEnum.BASIC_SEARCH.getModuleId().equals(moduleId)||
				ArchiveModuleEnum.ARCHIVE_CONDUCT.getModuleId().equals(moduleId)||
				ArchiveModuleEnum.ARCHIVE_INSTRUCTIONS.getModuleId().equals(moduleId)||
				ArchiveModuleEnum.ARCHIVE_FEEDBACK.getModuleId().equals(moduleId)||
				ArchiveModuleEnum.ARCHIVE_BOX.getModuleId().equals(moduleId)) {
			return BrowseFileTypeEnum.DOCUMENT.getCode();//档案附件浏览
		} else if (ArchiveModuleEnum.ARCHIVE_MY_USING.getModuleId().equals(moduleId) ||
				ArchiveModuleEnum.ARCHIVE_USING_MANAGE.getModuleId().equals(moduleId)) {
			return BrowseFileTypeEnum.USING.getCode();
		} else if (ArchiveModuleEnum.ARCHIVE_IDENTIFY.getModuleId().equals(moduleId)) {
			return BrowseFileTypeEnum.APPRAISAL.getCode();
		} else if (ArchiveModuleEnum.ARCHIVE_DESTORY.getModuleId().equals(moduleId)) {
			return BrowseFileTypeEnum.DESTROY.getCode();
		} else if (ArchiveModuleEnum.ARCHIVE_TRANSFER.getModuleId().equals(moduleId)||
				ArchiveModuleEnum.TRANSFER_RECEIVE.getModuleId().equals(moduleId)) {
			return BrowseFileTypeEnum.TRANSFER.getCode();
		} else if (ArchiveModuleEnum.RECORDS_CENTER.getModuleId().equals(moduleId)) {
			return BrowseFileTypeEnum.RECORDSCENTER.getCode();
		} else {
			return BrowseFileTypeEnum.OTHER.getCode();//其他附件
		}
	}


	public List<WatermarkDetail> getWatermarkDetailResult(Long watermarkId,WatermarkDTO watermarkDTO) {
		LambdaQueryWrapper<WatermarkDetail> queryWrapper = Wrappers.<WatermarkDetail>query().lambda().eq(WatermarkDetail::getWatermarkId, watermarkId)
				.orderByAsc(WatermarkDetail::getSortNo);
		List<WatermarkDetail> watermarkDetails = watermarkDetailService.list(queryWrapper);
		if(UsingTypeEnum.BROWSE.getValue().equals(watermarkDTO.getUsingType())){
			watermarkDetails = watermarkDetails.stream().filter(watermarkDetail -> Optional.ofNullable(watermarkDetail).map(WatermarkDetail::getWatermarkBrowse).orElse(Boolean.FALSE)).collect(Collectors.toList());
		}
		if(UsingTypeEnum.PRINT.getValue().equals(watermarkDTO.getUsingType())){
			watermarkDetails = watermarkDetails.stream().filter(watermarkDetail -> Optional.ofNullable(watermarkDetail).map(WatermarkDetail::getWatermarkPrint).orElse(Boolean.FALSE)).collect(Collectors.toList());
		}
		if(UsingTypeEnum.DOWNLOAD.getValue().equals(watermarkDTO.getUsingType())){
			watermarkDetails = watermarkDetails.stream().filter(watermarkDetail -> Optional.ofNullable(watermarkDetail).map(WatermarkDetail::getWatermarkDownload).orElse(Boolean.FALSE)).collect(Collectors.toList());
		}
		watermarkDetails.forEach(watermarkDetail -> {
			String watermarkType = watermarkDetail.getWatermarkType();//水印类型
			switch (watermarkType) {
				case WATERTYPE_PICTURE:
					getPicture(watermarkDetail);
					break;
				case WATERTYPE_TEXT:
					getTxt(watermarkDetail,watermarkDTO);
					break;
				case WATERTYPE_QRCODE:
					getQR(watermarkDetail,watermarkDTO);//二维码也走图片逻辑
					break;
				case WATERTYPE_SEAL:
					getSeal(watermarkDetail,watermarkDTO);//归档章水印
					break;
				default:
					break;
			}

		});
		return watermarkDetails;
	}
	/*
	 *
	 * @Description  水印详细配置
	 * @param watermarkId
	 * @return java.util.List<com.cescloud.saas.archive.api.modular.archiveconfig.entity.WatermarkDetail>
	 * @author qianbaocheng
	 * @date 2020-10-21 16:47
	 */
	@Override
	public List<WatermarkDetail> getWatermarkDetail(Long watermarkId) {
		LambdaQueryWrapper<WatermarkDetail> queryWrapper = Wrappers.<WatermarkDetail>query().lambda().eq(WatermarkDetail::getWatermarkId, watermarkId)
				.orderByAsc(WatermarkDetail::getSortNo);
		List<WatermarkDetail> watermarkDetails = watermarkDetailService.list(queryWrapper);
		watermarkDetails.stream().forEach(watermarkDetail -> {
			if (watermarkDetail.getWatermarkType().equals(WATERTYPE_PICTURE)) {//图片需要获取预览图
				Long fileId = watermarkDetail.getWatermarkPathId();
				String url = this.getImageViewUrl(String.valueOf(fileId));
				watermarkDetail.setWatermarkUrl(url);
			}
		});
		return watermarkDetails;
	}

	@SneakyThrows
	private void getQR(WatermarkDetail watermarkDetail, WatermarkDTO watermarkDTO) {
		Map<EncodeHintType, Object> hints = new HashMap<>();
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);//设置容错率默认为最高
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");// 字符编码为UTF-8
		String watermarkTxt = getTxt(watermarkDetail, watermarkDTO);
		BarcodeQRCode barcodeQRCode = new BarcodeQRCode(ObjectUtil.isEmpty(watermarkTxt)?" ":watermarkTxt, 100, 100, hints);//防止空值导致二维码报
		java.awt.Image waterImg = barcodeQRCode.createAwtImage(Color.BLACK,Color.WHITE);
		BufferedImage img = new BufferedImage( waterImg.getWidth(null),waterImg.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics g = img.createGraphics();
		g.drawImage(waterImg, 0, 0, Color.WHITE, null);
		g.dispose();
		File tempFile = new File(System.getProperty("java.io.tmpdir"), DateUtil.now() + ".png");
		ImageIO.write(img, "PNG",tempFile);
		InputStream inputStream =null;
		try {
			inputStream = new FileInputStream(tempFile);
			OtherFileStorage otherFileStorage = new OtherFileStorage();
			otherFileStorage.setName(tempFile.getName());
			otherFileStorage.setFileSourceName(tempFile.getName());
			otherFileStorage.setFileType("png");
			otherFileStorage.setFileSize(tempFile.length());
			otherFileStorage.setParentPath(StorageConstants.WATERMARK_FILE);
			otherFileStorage.setContentType("image/png");
			otherFileStorage = otherFileStorageOpenService.upload(inputStream, otherFileStorage);
			String url = this.getImageViewUrl(String.valueOf(otherFileStorage.getId()));
			watermarkDetail.setWatermarkUrl(url);
			log.info("图片的预览url:" + url);
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			IoUtil.close(inputStream);
		}

	}

	@SneakyThrows
	private void getSeal(WatermarkDetail watermarkDetail, WatermarkDTO watermarkDTO) {
		Map<EncodeHintType, Object> hints = new HashMap<>();
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);//设置容错率默认为最高
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");// 字符编码为UTF-8
		String watermarkSeal = getSealTxt(watermarkDTO);
		File tempFile = new File(System.getProperty("java.io.tmpdir"), DateUtil.now() + ".png");
		if(!tempFile.exists()) {
			tempFile.mkdirs();
		}
		HtmlImageGenerator imageGenerator = new HtmlImageGenerator();
		imageGenerator.loadHtml(watermarkSeal);
		imageGenerator.getBufferedImage();
		imageGenerator.saveAsImage(tempFile);
		InputStream inputStream =null;
		try {
			inputStream = new FileInputStream(tempFile);
			OtherFileStorage otherFileStorage = new OtherFileStorage();
			otherFileStorage.setName(tempFile.getName());
			otherFileStorage.setFileSourceName(tempFile.getName());
			otherFileStorage.setFileType("png");
			otherFileStorage.setFileSize(tempFile.length());
			otherFileStorage.setParentPath(StorageConstants.WATERMARK_FILE);
			otherFileStorage.setContentType("image/png");
			otherFileStorage = otherFileStorageOpenService.upload(inputStream, otherFileStorage);
			String url = this.getImageViewUrl(String.valueOf(otherFileStorage.getId()));
			watermarkDetail.setWatermarkUrl(url);
			log.info("图片的预览url:" + url);
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			IoUtil.close(inputStream);
		}
	}

	private String getSealTxt(WatermarkDTO watermarkDTO) {
		String htmlstr ="";
		long storageFileId = watermarkDTO.getFileId();
		ArchiveTable upTableR= archiveTableService.getUpTableByStorageLocate(watermarkDTO.getStorageLocate());
		if (ObjectUtil.isNull(upTableR)) {
			log.error("获取上级表失败!!");
		}
		String tablaName = upTableR.getStorageLocate();
		String where = FieldConstants.ID + "= (select " + FieldConstants.OWNER_ID + " from " + watermarkDTO.getStorageLocate() +
				" where " + FieldConstants.Document.FILE_STORAGE_ID + " = " + storageFileId + ")";
		List<Map<String, Object>> list = archiveUtil.getListByCondition(tablaName, null, null, null, null, where, false);
		if(ObjectUtil.isNotEmpty(list)&&ObjectUtil.isNotNull(list.get(0))){
			String fondsCode = String.valueOf(list.get(0).get("fonds_code") == null ? "" : list.get(0).get("fonds_code"));
			String yearCode = String.valueOf(list.get(0).get("year_code") == null ? "" : list.get(0).get("year_code"));
			String fileNo = String.valueOf(list.get(0).get("file_no") == null ? "" : list.get(0).get("file_no"));
			String filingDept = String.valueOf(list.get(0).get("filing_dept") == null ? "" : list.get(0).get("filing_dept"));
			String retentionPeriod = String.valueOf(list.get(0).get("retention_period") == null ? "" : list.get(0).get("retention_period"));
			String pageNumber = String.valueOf(list.get(0).get("amount_of_pages") == null ? "" : list.get(0).get("amount_of_pages"));
			htmlstr =
					"<table cellpadding='0' cellspacing='0' width='45mm' height='16mm' style='border-style:solid; border-color : red ;text-align: center; background-color: red; font-size: 9px; color : black;font-weight: bold;'>" +
							"<tr width='45mm' height='8mm' bgcolor='white'>"+
							"<td border='1px' style='text-align: center;border-style:solid;border-color: red;' width = '15mm' height='8mm'>"+fondsCode+"</td>"+
							"<td border='1px' style='text-align: center;border-style:solid;border-color: red;' width = '15mm' height='8mm'>"+yearCode+"</td>"+
							"<td border='1px' style='text-align: center;border-style:solid;border-color: red;' width = '15mm' height='8mm'>"+fileNo+"</td>"+
							"</tr>"+
							"<tr width='45mm' height='8mm' bgcolor='white'>"+
							"<td border='1px' style='text-align: center;border-style:solid;border-color: red;' width = '15mm' height='8mm'>"+filingDept+"</td>"+
							"<td border='1px' style='text-align: center;border-style:solid;border-color: red;' width = '15mm' height='8mm'>"+retentionPeriod+"</td>"+
							"<td border='1px' style='text-align: center;border-style:solid;border-color: red;' width = '15mm' height='8mm'>"+pageNumber+"</td>"+
							"</tr>"+
							"</table>";
		}
		return htmlstr;
	}

	private String getTxt(WatermarkDetail watermarkDetail,WatermarkDTO watermarkDTO) {
		String watermarkTxt = "";
		if(BrowseFileTypeEnum.DOCUMENT.getCode().compareTo(watermarkDTO.getAttachmentType())<0){//流程单电子文件水印
			watermarkTxt = getWatermarkTxtForProcessList(watermarkDetail,watermarkDTO);
		}
		if (BrowseFileTypeEnum.DOCUMENT.getCode().equals(watermarkDTO.getAttachmentType())) {//档案电子文件水印
			watermarkTxt = getWatermarkTxtForDocument(watermarkDetail,watermarkDTO);
		}
		log.info("[文字水印] : " + watermarkTxt);
		watermarkDetail.setWatermarkTxt(watermarkTxt);
		return watermarkTxt;
	}

	private String getWatermarkTxtForDocument(WatermarkDetail watermarkDetail, WatermarkDTO watermarkDTO){
		String watermarkTxtConfiguration = watermarkDetail.getWatermarkTxtConfiguration();//获取水印的配置信息
		String watermarkType = watermarkDetail.getWatermarkType();
		AtomicReference<String> watermarkTxtValue = new AtomicReference<>("");
		if (WATERTYPE_TEXT.equals(watermarkType)||WATERTYPE_QRCODE.equals(watermarkType)) {//如果是文字水印
			List<Map<String, Object>> watermarkTxtConfigurationList = (List<Map<String, Object>>) JSON.parse(watermarkTxtConfiguration);
			if (ObjectUtil.isNotNull(watermarkTxtConfigurationList)) {
				for (int i = 0; i < watermarkTxtConfigurationList.size(); i++) {
					Map<String, Object> configMap = watermarkTxtConfigurationList.get(i);
					if (WATERTYPE_TEXT_TEXT.equals(configMap.get("type"))) {//文字类型水印
						watermarkTxtValue.updateAndGet(v -> v + configMap.get("value"));
					} else {//字段类型水印.需要取文件层的相应字段信息
						String watermarkField = String.valueOf(configMap.get("value"));
						boolean customize = false;//是否自定义
						for (WatermarkCustomizeEnum watermarkCustomizeEnum : WatermarkCustomizeEnum.values()) {
							if (watermarkCustomizeEnum.getKey().equals(String.valueOf(watermarkField))) {
								customize = true;
								break;
							}
						}
						if (customize) {
							watermarkTxtValue.updateAndGet(v -> v + getCustomizeMsg(watermarkField));
						} else {
							long storageFileId = watermarkDTO.getFileId();
							ArchiveTable upTableR= archiveTableService.getUpTableByStorageLocate(watermarkDTO.getStorageLocate());
							if (ObjectUtil.isNull(upTableR)) {
								log.error("获取上级表失败!!");
							}
							String tablaName = upTableR.getStorageLocate();
							String where = FieldConstants.ID + "= (select " + FieldConstants.OWNER_ID + " from " + watermarkDTO.getStorageLocate() +
									" where " + FieldConstants.Document.FILE_STORAGE_ID + " = " + storageFileId + ")";
							List filterCollumn = new ArrayList<>();
							filterCollumn.add(watermarkField);
							List<Map<String, Object>> list = archiveUtil.getListByCondition(tablaName, null, null, filterCollumn, null, where, false);
							if(ObjectUtil.isNotEmpty(list)&&ObjectUtil.isNotNull(list.get(0))){
								watermarkTxtValue.updateAndGet(v -> v + list.get(0).get(watermarkField));
							}
						}
					}
				}
			}
		}
		return watermarkTxtValue.get();
	}

	private String getWatermarkTxtForProcessList(WatermarkDetail watermarkDetail, WatermarkDTO watermarkDTO){
		Long listId = watermarkDTO.getListId();//单子id
		String watermarkTxtConfiguration = watermarkDetail.getWatermarkTxtConfiguration();//获取水印的配置信息
		String watermarkType = watermarkDetail.getWatermarkType();
		AtomicReference<String> watermarkTxtValue = new AtomicReference<>("");
		if (WATERTYPE_TEXT.equals(watermarkType)||WATERTYPE_QRCODE.equals(watermarkType)) {//如果是文字水印
			List<Map<String, Object>> watermarkTxtConfigurationList = (List<Map<String, Object>>) JSON.parse(watermarkTxtConfiguration);
			if (ObjectUtil.isNotNull(watermarkTxtConfigurationList)) {
				for (int i = 0; i < watermarkTxtConfigurationList.size(); i++) {
					Map<String, Object> configMap = watermarkTxtConfigurationList.get(i);
					if (WATERTYPE_TEXT_TEXT.equals(configMap.get("type"))) {//文字类型水印
						watermarkTxtValue.updateAndGet(v -> v + configMap.get("value"));
					} else {//字段类型水印.需要取文件层的相应字段信息
						String watermarkField = String.valueOf(configMap.get("value"));//字段名称
						boolean customize = false;//是否自定义字段==
						for (WatermarkCustomizeEnum watermarkCustomizeEnum : WatermarkCustomizeEnum.values()) {
							if (watermarkCustomizeEnum.getKey().equals(String.valueOf(watermarkField))) {
								customize = true;
								break;
							}
						}
						if (customize) {
							watermarkTxtValue.updateAndGet(v -> v + getCustomizeMsg(watermarkField));
						} else {
							Map<String, Object> data = new HashMap<>() ;
							String value = String.valueOf(configMap.get("value"));
							if(ObjectUtil.isEmpty(listId)||ObjectUtil.isNull(listId)){
								log.error("流程单id为空!!");
								break;
							}
							R<Map<String, Object>> result = new R<>();
							if(ObjectUtil.equal(BrowseFileTypeEnum.USING.getCode(),watermarkDTO.getAttachmentType())){
								result = remoteArchiveUsingService.getUsingById(listId);
							}
							if(ObjectUtil.equal(BrowseFileTypeEnum.APPRAISAL.getCode(),watermarkDTO.getAttachmentType())){
								result = remoteDisposalAppraisaService.getAppraisalById(listId);
							}
							if(ObjectUtil.equal(BrowseFileTypeEnum.DESTROY.getCode(),watermarkDTO.getAttachmentType())){
								result = remoteDestoryService.getDestoryById(listId);
							}
							if(ObjectUtil.equal(BrowseFileTypeEnum.TRANSFER.getCode(),watermarkDTO.getAttachmentType())){
								result = remoteTransferService.getTransferById(listId);
							}
							if (result.getCode() == CommonConstants.SUCCESS && ObjectUtil.isNotEmpty(result.getData())){
								data = result.getData();
							}
							Optional.ofNullable(((Map<String,Object>)data.get("formData")).get(value)).ifPresent(dataV -> {
								watermarkTxtValue.updateAndGet(v -> v + dataV);
							});
						}
					}
				}
			}
		}
		return watermarkTxtValue.get();
	}

	private String getCustomizeMsg(String watermarkField) {
		String watermarkTextValue = "";
		if (WatermarkCustomizeEnum.userLoginName.getKey().equals(watermarkField)) {
			watermarkTextValue = SecurityUtils.getUser().getUsername();
		}
		if (WatermarkCustomizeEnum.userName.getKey().equals(watermarkField)) {
			watermarkTextValue = SecurityUtils.getUser().getChineseName();
		}
		if (WatermarkCustomizeEnum.userIP.getKey().equals(watermarkField)) {
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
					.getRequestAttributes()).getRequest();
			watermarkTextValue = AddrUtil.getRemoteAddr(request);

		}
		if (WatermarkCustomizeEnum.userWatermarkDate.getKey().equals(watermarkField)) {
			watermarkTextValue = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		}
		return watermarkTextValue;
	}


	private void getPicture(WatermarkDetail watermarkDetail) {
		Long fileId = 0L;
		try {
			//是否取当前人所在的部门签章
			if (Optional.ofNullable(watermarkDetail.getCurrentUserDeptSeal()).orElse(false)) {
				long deptId = SecurityUtils.getUser().getDeptId();
				R<SysDept> deptR = remoteDeptService.getDeptById(deptId);
				SysDept sysDept;
				if (deptR.getCode() == CommonConstants.SUCCESS && ObjectUtil.isNotEmpty(deptR.getData())) {
					sysDept = deptR.getData();
				} else {
					sysDept = new SysDept();
				}
				if (ObjectUtil.isNull(sysDept) || ObjectUtil.isEmpty(sysDept.getFileStorageId())) {
					throw new ArchiveRuntimeException("部门[" + deptId + "]签章不存在！");
				}
				fileId = sysDept.getFileStorageId();
				log.info("部门签章id是:" + fileId);
			} else {
				fileId = watermarkDetail.getWatermarkPathId();
			}
		} catch (Exception e) {
			log.error("获取水印图片失败", e);
		}
		String url = this.getImageViewUrl(String.valueOf(fileId));
		watermarkDetail.setWatermarkUrl(url);
		log.info("图片的预览url:" + url);
	}


	/*
	 *
	 * @Description  获取水印详细配置
	 * @param watermarkId
	 * @return com.cescloud.saas.archive.api.modular.archiveconfig.dto.WatermarkDTO
	 * @author qianbaocheng
	 * @date 2020-10-21 16:47
	 */
	@Override
	public WatermarkDTO getWatermark(Long watermarkId) {
		Watermark watermark = this.getById(watermarkId);
		WatermarkDTO watermarkDTO = new WatermarkDTO();
		BeanUtil.copyProperties(watermark, watermarkDTO);
		watermarkDTO.setWatermarkDetailList(getWatermarkDetail(watermarkId));
		return watermarkDTO;
	}


	/*
	 *
	 * @Description  保存水印配置
	 * @param watermarkDTO
	 * @return com.cescloud.saas.archive.service.modular.common.core.util.R
	 * @author qianbaocheng
	 * @date 2020-10-21 16:49
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R saveWatermark(WatermarkDTO watermarkDTO) {
		Boolean isDefault = Optional.ofNullable(watermarkDTO.getIsDefault()).orElse(false);//默认配置
		watermarkDTO.setIsDefault(isDefault);
		Watermark watermark = new Watermark();
		BeanUtil.copyProperties(watermarkDTO, watermark);
		//更新updatetime
		watermark.setUpdatedTime(LocalDateTime.now());
		if (isDefault) {//将所有的配置设为false
			LambdaUpdateWrapper<Watermark> updateWrapper  =
					Wrappers.<Watermark>lambdaUpdate().set(Watermark::getIsDefault, Boolean.FALSE)
							.eq(Watermark::getStorageLocate, watermarkDTO.getStorageLocate())
							.eq(Watermark::getWatermarkFormat, watermarkDTO.getWatermarkFormat())
							.eq(Watermark::getWaterClassification,watermarkDTO.getWaterClassification());
			this.update(updateWrapper);
		}
		this.saveOrUpdate(watermark);
		//delete原详细方案
		List<WatermarkDetail> watermarkDetails = getWatermarkDetail(watermark.getId());
		List<Long> watermarkDetailIdList = watermarkDetails.stream().map(watermarkDetail -> watermarkDetail.getId()).collect(Collectors.toList());
		watermarkDetailService.removeByIds(watermarkDetailIdList);
		//insert新定义的方案
		List<WatermarkDetail> watermarkDetailList = watermarkDTO.getWatermarkDetailList();
		watermarkDetailList.forEach(watermarkDetail -> {
			watermarkDetail.setWatermarkId(watermark.getId());
			watermarkDetail.setWatermarkUrl(null);//用于浏览不插入数据库
			//将watermarkTxtConfigurationList装成json存在watermarkTxtConfiguration中
			watermarkDetail.setWatermarkTxtConfiguration(JSON.toJSONString(watermarkDetail.getWatermarkTxtConfigurationList()));
		});
		watermarkDetailService.saveBatch(watermarkDetailList);
		//界面上配置打勾
		archiveConfigManageService.save(watermarkDTO.getStorageLocate(), watermarkDTO.getModuleId(), TypedefEnum.WATERMARK.getValue());
		return new R().success(null, "保存成功！");
	}

	/**
	 * 根据存储表名删除水印配置
	 *
	 * @param watermarkId 水印ID
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R deleteWatermark(Long watermarkId, String storageLocate, Long moduleId) {
		this.removeById(watermarkId);
		List<WatermarkDetail> watermarkDetails = getWatermarkDetail(watermarkId);
		List<Long> ids = watermarkDetails.stream().map(WatermarkDetail::getId).collect(Collectors.toList());
		watermarkDetailService.removeByIds(ids);
		if (listWatermark(storageLocate, "").isEmpty()) {
			//去掉定义界面的打勾
			archiveConfigManageService.update(storageLocate, moduleId, TypedefEnum.WATERMARK.getValue(), BoolEnum.NO.getCode());
		}
		return new R().success(null, "删除成功！");
	}


	@Override
	public R getMetadata(String storageLocate,int type)  {
		List<Combo> comboList = new ArrayList<Combo>();
		if(1 == type){//档案模块字段
			List<Metadata> metadataList = new ArrayList<>();
			//文件表的字段
			final ArchiveTable upTable = archiveTableService.getUpTableByStorageLocate(storageLocate);
			if (null == upTable) {
				throw new ArchiveRuntimeException("档案门类表[" + storageLocate + "]父表不存在！");
			}
			metadataList.addAll(metadataService.listAllByTableId(upTable.getId()));
			metadataList.forEach(metadata -> {
				Combo combo = new Combo();
				combo.setKey(metadata.getMetadataEnglish());
				combo.setValue(metadata.getMetadataChinese());
				comboList.add(combo);
			});
		}else{//流程单模块与表单配置对应
			type = type - 1;//和表单配置对应（1：利用表单,2、鉴定表单，3、销毁表单，4、移交表单)
			R<BusinessStyleSetting> result = remoteBusinessStyleSettingService.initForm(type);
			if (result.getCode() == CommonConstants.FAIL) {
				throw new ArchiveRuntimeException("获取表单设置失败");
			}
			//获取表单设置
			BusinessStyleSetting businessStyleSetting = result.getData();
			if (ObjectUtil.isNotNull(businessStyleSetting) && ObjectUtil.isNotNull(businessStyleSetting.getFormContent())) {
				Map<String , Object> formContentMap =  (Map<String ,Object>)businessStyleSetting.getFormContent();
				List<Map<String , Object>>  formList = (List<Map<String , Object>>)formContentMap.get("list");
				formList.forEach(map ->{
					Combo combo = new Combo();
					combo.setKey(String.valueOf(map.get("model")));
					combo.setValue(String.valueOf(map.get("name")));
					comboList.add(combo);
				});
			}
		}
		//遍历枚举来获取自定义选项
		for (WatermarkCustomizeEnum waterCustomize : WatermarkCustomizeEnum.values()) {
			Combo combo = new Combo();
			combo.setKey(waterCustomize.getKey());
			combo.setValue(waterCustomize.getValue());
			comboList.add(combo);
		}
		return new R<>(comboList);
	}


	/**
	 * 上传文件
	 *
	 * @param file 文件
	 */
	@Override
	public R<Map<String, Object>> uploadFile(MultipartFile file) throws Exception {
		Map<String, Object> resultMap = new HashMap<>(4);
		OtherFileStorage otherFileStorage;
		try {
			otherFileStorage = otherFileStorageOpenService.upload(file, StorageConstants.WATERMARK_FILE);
		} catch (Exception e) {
			log.error("文件上传失败!", e);
			throw new ArchiveBusinessException("文件上传失败!", e);
		}
		resultMap.put("fileId", otherFileStorage.getId());
		//图片url地址用于前端界面预览
		resultMap.put("imgUrl", this.getImageViewUrl(String.valueOf(otherFileStorage.getId())));
		return new R().success(resultMap, "图片上传成功！");
	}

	/*
	 *
	 * Description:
	 * @param: String
	 * @return: String
	 * @auther: qianbaocheng
	 * @date: 2020-10-21 17:14
	 */
	private String getImageViewUrl(String fileIdStr) {
		if (ObjectUtil.isEmpty(fileIdStr)) {
			return "";
		}
		final R<String> result = remoteFileViewService.getFileUrl(Long.parseLong(fileIdStr), BrowseFileTypeEnum.OTHER.getCode());
		if (result.getCode() == CommonConstants.FAIL) {
			log.error("获取文件浏览地址失败！" + result.getMsg());
			return "";
		}
		return result.getData();
	}

	@Override
	public void showImage(Long fileId, HttpServletResponse response) {
		try (InputStream in = fileStorageOpenService.getInputStream(fileId, SecurityUtils.getUser().getTenantId());
			 ServletOutputStream out = response.getOutputStream()
		) {
			IoUtil.copy(in, out);
		} catch (Exception e) {
			log.error("预览图片失败", e);
		}
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public R copy(Long watermarkId, List<Long> targetModuleIds) {
		Watermark watermark = this.getById(watermarkId);
		targetModuleIds.forEach(targetModuleId -> {
			Watermark watermarkCopy = ObjectUtil.clone(watermark);
			watermarkCopy.setModuleId(targetModuleId);
			watermarkCopy.setId(null);
			getBaseMapper().insert(watermarkCopy);
			List<WatermarkDetail> watermarkDetails = getWatermarkDetail(watermarkId);
			watermarkDetails.forEach(watermarkDetail -> {
				WatermarkDetail watermarkDetailCopy = ObjectUtil.clone(watermarkDetail);
				watermarkDetailCopy.setId(null);
				watermarkDetailCopy.setWatermarkId(watermarkCopy.getId());
			});
			watermarkDetailService.saveBatch(watermarkDetails);
		});
		return new R().success(null, "复制成功！");
	}

	@Data
	@NoArgsConstructor
	private class Combo {
		private String key;
		private String value;
	}


	/*
	 * Description: 复制水印配置
	 * @param srcStorageLocate
	 * @param destStorageLocate
	 * @param srcDestMetadataMap
	 * @return: void
	 * @auther: qianbaocheng
	 * @date: 2020-10-21 17:33
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void copyByStorageLocate(String srcStorageLocate, String destStorageLocate, Map<Long, Long> srcDestMetadataMap) {
		// 保存主表配置
		List<Watermark> srcList = this.list(Wrappers.<Watermark>lambdaQuery().eq(Watermark::getStorageLocate, srcStorageLocate));
		if (CollectionUtil.isNotEmpty(srcList)) {
			List<Watermark> destList = srcList.stream().map(e -> {
				Watermark watermark = new Watermark();
				BeanUtil.copyProperties(e, watermark);
				watermark.setId(null);
				watermark.setStorageLocate(destStorageLocate);
				return watermark;
			}).collect(Collectors.toList());
			this.saveBatch(destList);
			final Map<Long, Long> srcDestConfigIdMap = MapUtil.newHashMap();
			for (int i = 0, len = srcList.size(); i < len; i++) {
				srcDestConfigIdMap.put(srcList.get(i).getId(), destList.get(i).getId());
			}
			// 保存从表配置
			watermarkDetailService.copyConfig(srcDestConfigIdMap);
		}
	}


}
