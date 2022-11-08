package com.cescloud.saas.archive.service.modular.filingscope.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.cescloud.saas.archive.api.modular.filecenter.dto.FilePushDTO;
import com.cescloud.saas.archive.api.modular.filingscope.dto.FilingScopeExcelDTO;
import com.cescloud.saas.archive.api.modular.filingscope.dto.FilingScopeExcelErrorDTO;
import com.cescloud.saas.archive.api.modular.filingscope.entity.FilingScope;
import com.cescloud.saas.archive.common.constants.PushFileTypeEnum;
import com.cescloud.saas.archive.common.util.CesFileUtil;
import com.cescloud.saas.archive.common.util.DateUtil;
import com.cescloud.saas.archive.common.util.InitializeUtil;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import com.cescloud.saas.archive.service.modular.common.security.service.CesCloudUser;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.filepush.util.service.PushFileOpenService;
import com.cescloud.saas.archive.service.modular.filingscope.service.FilingScopeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author LS
 * @date 2022/4/19
 */
@Slf4j
public class FilingScopeImportExcelListener extends AnalysisEventListener<FilingScopeExcelDTO> {

	public List<FilingScopeExcelErrorDTO> filingScopeExcelErrors = new ArrayList<>();
	private List<FilingScope> filingScopeList;
	private Map<String, String> archiveTypeMap;
	private MultipartFile multipartFile;
	private final FilingScopeService filingScopeService;
	private final PushFileOpenService pushFileOpenService;
	/**
	 * excel中读取的数据
	 */
	private List<FilingScopeExcelDTO> excelData = new ArrayList<>();

	public FilingScopeImportExcelListener(MultipartFile file, List<FilingScope> allChildrenFilingScopeList, Map<String, String> archiveTypeMap,
	                                      PushFileOpenService pushFileOpenService, FilingScopeService filingScopeService) {
		this.pushFileOpenService = pushFileOpenService;
		this.filingScopeList = allChildrenFilingScopeList;
		this.archiveTypeMap = archiveTypeMap;
		this.multipartFile = file;
		this.filingScopeService = filingScopeService;
	}

	@Override
	public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
		super.invokeHeadMap(headMap, context);
	}

	@Override
	public void invoke(FilingScopeExcelDTO filingScopeExcelDTO, AnalysisContext context) {
		Integer rowIndex = context.readSheetHolder().getRowIndex();
		//校验数据
		if (StrUtil.isBlank(filingScopeExcelDTO.getParentClassName())) {
			filingScopeExcelErrors.add(new FilingScopeExcelErrorDTO(0, rowIndex, "上级节点不能为空"));
		}
		if (StrUtil.isBlank(filingScopeExcelDTO.getClassName())) {
			filingScopeExcelErrors.add(new FilingScopeExcelErrorDTO(1, rowIndex, "分类名称不能为空"));
		}
		if (StrUtil.isBlank(filingScopeExcelDTO.getClassNo())) {
			filingScopeExcelErrors.add(new FilingScopeExcelErrorDTO(2, rowIndex, "分类号不能为空"));
		}
		if (StrUtil.isBlank(filingScopeExcelDTO.getTypeCode())) {
			filingScopeExcelErrors.add(new FilingScopeExcelErrorDTO(3, rowIndex, "关联档案门类不能为空"));
		}
		//与数据库中的分类号、分类名称不允许重复
		this.filingScopeList.forEach(e -> {
			if (Objects.equals(e.getClassName(), filingScopeExcelDTO.getClassName())) {
				filingScopeExcelErrors.add(new FilingScopeExcelErrorDTO(1, rowIndex, "分类名称不允许重复"));
			}
			if (Objects.equals(e.getClassNo(), filingScopeExcelDTO.getClassNo())) {
				filingScopeExcelErrors.add(new FilingScopeExcelErrorDTO(2, rowIndex, "分类号不允许重复"));
			}
		});
		//与excel中的分类号、分类名称不允许重复
		this.excelData.forEach(e -> {
			if (Objects.equals(e.getClassName(), filingScopeExcelDTO.getClassName())) {
				filingScopeExcelErrors.add(new FilingScopeExcelErrorDTO(1, rowIndex, "分类名称不允许重复"));
			}
			if (Objects.equals(e.getClassNo(), filingScopeExcelDTO.getClassNo())) {
				filingScopeExcelErrors.add(new FilingScopeExcelErrorDTO(2, rowIndex, "分类号不允许重复"));
			}
		});
		//关联档案门类系统中必须存在
		if (StrUtil.isNotBlank(filingScopeExcelDTO.getTypeCode())) {
			if (!this.archiveTypeMap.containsKey(filingScopeExcelDTO.getTypeCode())) {
				filingScopeExcelErrors.add(new FilingScopeExcelErrorDTO(3, rowIndex, "档案门类不存在"));
			}
		}
		filingScopeExcelDTO.setRowIndex(rowIndex);
		excelData.add(filingScopeExcelDTO);
	}

	@Override
	public void doAfterAllAnalysed(AnalysisContext context) {
		for (int i = 0; i < excelData.size(); i++) {
			checkParentFilingScope(excelData.get(i));
		}
		if (filingScopeExcelErrors.size() > 0) {
			generateErrorExcel();
		} else {
			if (excelData.size() == 0) {
				throw new ArchiveRuntimeException("导入数据为空");
			}
			List<FilingScope> list = CollUtil.newArrayList();
			toSave(list);
		}
	}

	private void toSave(List<FilingScope> list) {
		excelData.forEach(filingScopeExcelDTO -> getParentFilingScope(filingScopeExcelDTO, list));
		if (CollUtil.isNotEmpty(list)) {
			Map<Long, List<FilingScope>> listMap = list.stream().collect(Collectors.groupingBy(FilingScope::getParentClassId));
			listMap.forEach((k, v) -> {
				Integer maxSortNo = InitializeUtil.toInteger(filingScopeService.selectMaxSortNo(k));
				for (FilingScope e : v) {
					e.setSortNo(++maxSortNo);
				}
			});
			filingScopeService.saveBatch(list);
			filingScopeList.addAll(list);
			List<String> strings = list.stream().map(FilingScope::getClassName).collect(Collectors.toList());
			excelData = excelData.stream().filter(e -> !strings.contains(e.getClassName())).collect(Collectors.toList());
			list.clear();
			if (CollUtil.isNotEmpty(excelData)) {
				toSave(list);
			}
		}
	}

	private void getParentFilingScope(FilingScopeExcelDTO filingScopeExcel, List<FilingScope> list) {
		Optional<FilingScope> scope = filingScopeList.stream().filter(e -> Objects.equals(e.getClassName(),
				filingScopeExcel.getParentClassName())).findFirst();
		if (scope.isPresent()) {
			//上级节点已存在于数据库
			FilingScope parentFilingScope = scope.get();
			if (list.stream().noneMatch(e -> e.getClassName().equals(filingScopeExcel.getClassName()))) {
				FilingScope build = FilingScope.builder().className(filingScopeExcel.getClassName()).classNo(filingScopeExcel.getClassNo())
						.fondsName(parentFilingScope.getFondsName()).fondsCode(parentFilingScope.getFondsCode()).parentClassId(parentFilingScope.getId())
						.path(StrUtil.isBlank(parentFilingScope.getPath()) ? filingScopeExcel.getClassNo() : (parentFilingScope.getPath() + "," + filingScopeExcel.getClassNo()))
						.typeCode(archiveTypeMap.get(filingScopeExcel.getTypeCode())).build();
				list.add(build);
			}
		} else {
			//不存在于数据库，
			Optional<FilingScopeExcelDTO> first = excelData.stream().filter(e -> Objects.equals(e.getClassName(),
					filingScopeExcel.getParentClassName())).findFirst();
			if (first.isPresent()) {
				//存在于excel中
				FilingScopeExcelDTO filingScopeExcelDTO = first.get();
				getParentFilingScope(filingScopeExcelDTO, list);
			}
		}
	}

	private void checkParentFilingScope(FilingScopeExcelDTO filingScopeExcel) {
		Optional<FilingScope> scope = filingScopeList.stream().filter(e -> Objects.equals(e.getClassName(), filingScopeExcel.getParentClassName())).findFirst();
		if (scope.isPresent()) {
			//上级节点已存在于数据库
			FilingScope parentFilingScope = scope.get();
			if (StrUtil.isNotBlank(parentFilingScope.getTypeCode()) && StrUtil.isNotBlank(filingScopeExcel.getTypeCode())) {
				if (!parentFilingScope.getTypeCode().equals(archiveTypeMap.get(filingScopeExcel.getTypeCode()))) {
					filingScopeExcelErrors.add(new FilingScopeExcelErrorDTO(3, filingScopeExcel.getRowIndex(), "关联门类不一致"));
				}
			}
		} else {
			//不存在于数据库，
			Optional<FilingScopeExcelDTO> first = excelData.stream().filter(e -> Objects.equals(e.getClassName(), filingScopeExcel.getParentClassName())).findFirst();
			if (first.isPresent()) {
				//存在于excel中
				FilingScopeExcelDTO filingScopeExcelDTO = first.get();
				if (StrUtil.isNotBlank(filingScopeExcelDTO.getTypeCode()) && StrUtil.isNotBlank(filingScopeExcel.getTypeCode())) {
					if (!filingScopeExcelDTO.getTypeCode().equals(archiveTypeMap.get(filingScopeExcel.getTypeCode()))) {
						filingScopeExcelErrors.add(new FilingScopeExcelErrorDTO(3, filingScopeExcel.getRowIndex(), "关联门类不一致"));
					}
				}
				//在遍历下一级时清除本级数据
				excelData.remove(0);
				if (ObjectUtil.isNotEmpty(excelData)){
					checkParentFilingScope(filingScopeExcelDTO);
				}
			} else {
				filingScopeExcelErrors.add(new FilingScopeExcelErrorDTO(0, filingScopeExcel.getRowIndex(), "上级节点不存在"));
			}
		}
	}


	private void generateErrorExcel() {
		CesCloudUser user = SecurityUtils.getUser();
		File temFile = new File(System.getProperty("java.io.tmpdir") + File.separator + DateUtil.now() + ".xls");
		try (InputStream in = multipartFile.getInputStream(); FileOutputStream osOut = new FileOutputStream(temFile);
		     POIFSFileSystem poifsFileSystem = new POIFSFileSystem(in); HSSFWorkbook hssfWorkbook = new HSSFWorkbook(poifsFileSystem)) {
			HSSFSheet sheet = hssfWorkbook.getSheetAt(0);
			for (FilingScopeExcelErrorDTO e : filingScopeExcelErrors) {
				Integer rowIndex = e.getRowIndex();
				Integer columnIndex = e.getColumnIndex();
				HSSFCell cell = sheet.getRow(rowIndex).getCell(columnIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
				HSSFComment cellComment = cell.getCellComment();
				String oldString = "";
				if (ObjectUtil.isNotNull(cellComment)) {
					HSSFRichTextString hssfRichTextString = cellComment.getString();
					oldString = StrUtil.trim(hssfRichTextString.getString());
					cell.removeCellComment();
				}
				//添加批注
				HSSFPatriarch hssfShapes = sheet.createDrawingPatriarch();
				HSSFClientAnchor hssfClientAnchor = new HSSFClientAnchor(0, 0, 0, 0,
						(short) (columnIndex + 0), rowIndex, (short) (columnIndex + 2), rowIndex + 2);
				HSSFComment comment = hssfShapes.createCellComment(hssfClientAnchor);
				comment.setString(new HSSFRichTextString(StrUtil.equals(oldString, e.getMsg()) ? e.getMsg() : (oldString + " " + e.getMsg())));
				cell.setCellComment(comment);
				//修改文字红色
				HSSFCellStyle cellStyle = hssfWorkbook.createCellStyle();
				HSSFFont font = hssfWorkbook.createFont();
				font.setColor(IndexedColors.RED.index);
				cellStyle.setFont(font);
				cell.setCellStyle(cellStyle);
			}
			hssfWorkbook.write(osOut);
			//上传并发消息
			FilePushDTO filePushDTO = FilePushDTO.builder().fileSourceName(temFile.getName()).fileType(CesFileUtil.XLS)
					.tenantId(user.getTenantId()).userId(user.getId()).fileSize(temFile.length()).build();
			pushFileOpenService.finishedImportLogPush(temFile, filePushDTO, true, false, PushFileTypeEnum.FILING_SCOPE_IMPORT_ERROR_MESSAGE.getCode());
		} catch (Exception e) {
			throw new ArchiveRuntimeException("归档范围信息导入异常", e);
		}
	}


}
