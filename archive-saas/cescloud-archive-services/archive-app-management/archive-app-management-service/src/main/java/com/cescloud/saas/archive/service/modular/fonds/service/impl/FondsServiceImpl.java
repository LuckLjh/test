
package com.cescloud.saas.archive.service.modular.fonds.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archivetree.dto.FondsArchiveTreeSyncTreeNode;
import com.cescloud.saas.archive.api.modular.archivetree.entity.FondsArchiveTree;
import com.cescloud.saas.archive.api.modular.archivetree.feign.RemoteArchiveTreeService;
import com.cescloud.saas.archive.api.modular.archivetype.dto.ArchiveTypeDTO;
import com.cescloud.saas.archive.api.modular.archivetype.dto.ArchiveTypeTreeNode;
import com.cescloud.saas.archive.api.modular.archivetype.feign.RemoteArchiveTypeService;
import com.cescloud.saas.archive.api.modular.authority.feign.RemoteFondsAuthService;
import com.cescloud.saas.archive.api.modular.common.constants.SysConstant;
import com.cescloud.saas.archive.api.modular.datasource.dto.DynamicArchiveDTO;
import com.cescloud.saas.archive.api.modular.datasource.feign.RemoteArchiveInnerService;
import com.cescloud.saas.archive.api.modular.dept.dto.DeptSyncTreeNode;
import com.cescloud.saas.archive.api.modular.dept.feign.RemoteDeptFondsService;
import com.cescloud.saas.archive.api.modular.dept.feign.RemoteDeptService;
import com.cescloud.saas.archive.api.modular.filingscope.dto.FilingScopeDTO;
import com.cescloud.saas.archive.api.modular.filingscope.feign.RemoteFilingScopeService;
import com.cescloud.saas.archive.api.modular.fonds.dto.FondsConstant;
import com.cescloud.saas.archive.api.modular.fonds.dto.FondsDTO;
import com.cescloud.saas.archive.api.modular.fonds.entity.Fonds;
import com.cescloud.saas.archive.api.modular.register.feign.RemoteSysRegisterService;
import com.cescloud.saas.archive.api.modular.role.entity.SysRole;
import com.cescloud.saas.archive.api.modular.role.feign.RemoteSysRoleAuthService;
import com.cescloud.saas.archive.api.modular.tenant.entity.Tenant;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantService;
import com.cescloud.saas.archive.common.constants.BoolEnum;
import com.cescloud.saas.archive.common.constants.NodeTypeEnum;
import com.cescloud.saas.archive.common.constants.RoleConstant;
import com.cescloud.saas.archive.common.util.ArchiveUtil;
import com.cescloud.saas.archive.common.util.FilterUtil;
import com.cescloud.saas.archive.service.modular.archivetree.service.FondsArchiveTreeService;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.localcache.LocalCacheable;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import com.cescloud.saas.archive.service.modular.common.security.tenantfilter.TenantContextHolder;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.fonds.mapper.FondsMapper;
import com.cescloud.saas.archive.service.modular.fonds.service.FondsService;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 全宗
 *
 * @author zhangpeng
 * @date 2019-03-21 12:04:54
 */
@Service
@Slf4j
@CacheConfig(cacheNames = "fonds")
@RequiredArgsConstructor
public class FondsServiceImpl extends ServiceImpl<FondsMapper, Fonds> implements FondsService {

	private final static String FONDS_CODE = "全宗编码";
	private final static String FONDS_NAME = "全宗名称";
	private final static String FONDS_DECR = "全宗描述";
	//名称最大长度
	private final static Integer MAX_NAME_LENGTH = 50;

	@Autowired
	private FondsArchiveTreeService fondsArchiveTreeService;

	@Resource
	private ResourceLoader resourceLoader;

	private final RemoteSysRegisterService remoteSysRegisterService;

	private final RemoteTenantService remoteTenantService;

	private final RemoteDeptFondsService remoteDeptFondsService;

	private final RemoteSysRoleAuthService remoteSysRoleAuthService;

	private final RemoteFondsAuthService remoteFondsAuthService;

	private final RemoteArchiveTypeService remoteArchiveTypeService;

	private final ArchiveUtil archiveUtil;

	private final RemoteArchiveInnerService remoteArchiveInnerService;

	private final FondsMapper fondsMapper;

	private final static Map<Integer, String> EXCEL_HEAD = new LinkedHashMap<>();

	private final RemoteArchiveTreeService remoteArchiveTreeService;

	private final RemoteFilingScopeService remoteFilingScopeService;

	private final RemoteDeptService remoteDeptService;


	static {
		EXCEL_HEAD.put(0, FONDS_CODE);
		EXCEL_HEAD.put(1, FONDS_NAME);
		EXCEL_HEAD.put(2, FONDS_DECR);
	}

	@Override
	public Page<Fonds> getPage(FondsDTO fondsDTO) {
		LambdaQueryWrapper<Fonds> queryWrapper = Wrappers.<Fonds>query().lambda();
		queryWrapper.orderByAsc(Fonds::getCreatedTime);
		if (StrUtil.isNotBlank(fondsDTO.getKeyword())) {
			queryWrapper
					.and(wrapper -> wrapper.like(Fonds::getFondsName, StrUtil.trim(fondsDTO.getKeyword()))
							.or()
							.like(Fonds::getFondsCode, StrUtil.trim(fondsDTO.getKeyword()))
							.or()
							.like(Fonds::getDescription, StrUtil.trim(fondsDTO.getKeyword())));
		}
		queryWrapper.in(CollUtil.isNotEmpty(fondsDTO.getFondsCodes()), Fonds::getFondsCode, fondsDTO.getFondsCodes());
		Page<Fonds> page = new Page<>(fondsDTO.getCurrent(), fondsDTO.getSize());
		return this.page(page, queryWrapper);
	}

	@Override
	@Cacheable(
			key = "'archive-app-management:fonds:'+#fondsId",
			unless = "#result == null"
	)
	public Fonds getFondsById(Long fondsId) {
		return this.getById(fondsId);
	}

	/**
	 * 增加了SQL权限拦截，暂不使用缓存
	 *
	 * @return
	 */
	@Override
	public List<Fonds> getFondsList() {
		return this.list(Wrappers.<Fonds>lambdaQuery().orderByAsc(Fonds::getCreatedTime));
	}

	@Override
	@LocalCacheable
	public List<Fonds> getFondsList(Long userID){
		return this.list(Wrappers.<Fonds>lambdaQuery().orderByAsc(Fonds::getCreatedTime));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R insertExcel(MultipartFile file) throws IOException {
		@Cleanup InputStream inputStream = file.getInputStream();
		List<LinkedHashMap<Integer, String>> dataList = EasyExcel.read(inputStream).sheet(0).doReadSync();
		if (dataList == null || CollectionUtil.isEmpty(dataList)) {
			throw new ArchiveRuntimeException("导入数据为空，请确认后重新导入！");
		}
		//存储头部字段对应顺序： {0：全宗编码，1：全宗名称，2：全宗描述}
		Map<Integer, String> headIndex = new HashMap<>(16);
		//检查头部信息
		getHead(dataList, headIndex);
		if (dataList.size() == 2) {
			throw new ArchiveRuntimeException("导入数据为空，请确认后重新导入！");
		}

		//整个系统的总全宗数限制
		if (!registerEnableAddFonds(dataList.size() - 2)) {
			throw new ArchiveRuntimeException("导入全宗数量超过注册限制，请确认后重新导入！");
		}
		//校验导入全宗数量是否超过限制
		if (!userEnableAddFonds(dataList.size() - 2)) {
			throw new ArchiveRuntimeException("导入全宗数量超过租户限制，请确认后重新导入！");
		}
		int fondsCodeMaxLength = 50;
		int fondsNameMaxLength = 50;
		int fondsDesMaxLength = 200;
		try{
			fondsCodeMaxLength = Fonds.class.getDeclaredField("fondsCode").getAnnotation(Size.class).max();
			fondsNameMaxLength = Fonds.class.getDeclaredField("fondsName").getAnnotation(Size.class).max();
			fondsDesMaxLength = Fonds.class.getDeclaredField("description").getAnnotation(Size.class).max();
		}catch (Exception e){
			e.printStackTrace();
		}
		//校验数据长度限制
		for (int i = 2, length = dataList.size(); i < length; i++) {
			LinkedHashMap<Integer, String> data = dataList.get(i);
			String fondsCode = "";
			String fondsName = "";
			String fondsDesc = "";
			for (int j = 0, len = data.size(); j < len; j++) {
				if (headIndex.get(j).equals(FONDS_CODE)) {
					fondsCode = data.get(j);
				}
				if (headIndex.get(j).equals(FONDS_NAME)) {
					fondsName = data.get(j);
				}
				if (headIndex.get(j).equals(FONDS_DECR)) {
					fondsDesc = data.get(j);
				}
			}
			if(fondsCode.length()>fondsCodeMaxLength){
				throw new ArchiveRuntimeException("导入全宗编码["+fondsCode+"]长度太长，请修改后重新导入！");
			}
			if(fondsName.length()>fondsNameMaxLength){
				throw new ArchiveRuntimeException("导入全宗名称["+fondsName+"]长度太长，请修改后重新导入！");
			}
			if(fondsDesc.length()>fondsDesMaxLength){
				throw new ArchiveRuntimeException("导入全宗描述长度太长，请修改后重新导入！");
			}
		}
		//插入excel数据
		List<Map<String, String>> maps = insertExcelData(headIndex, dataList);
		if (CollectionUtil.isEmpty(maps)) {
			return new R<>(null, "导入完成！");
		} else {
			return new R<>().fail(maps, "导入失败！");
		}
	}

	@Override
	public void exportExcel(HttpServletResponse response, String fileName, String keyword) {
		String path = "templatefile/fondsTemplate.xls";
		org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:" + path);
		try (OutputStream out = response.getOutputStream();
		     InputStream fileInputStream = resource.getInputStream();
		     POIFSFileSystem poifsFileSystem = new POIFSFileSystem(fileInputStream);
		     HSSFWorkbook sheets = new HSSFWorkbook(poifsFileSystem);) {
			String encodeName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
			response.setContentType("application/vnd.ms-excel");
			response.setCharacterEncoding("utf-8");
			response.setHeader("Content-Disposition", "attachment;filename=" + encodeName + ".xls");
			//加载模板
			HSSFSheet sheetAt = sheets.getSheetAt(0);
			List<List<String>> excelData = getExcelData(keyword);
			List<List<String>> excelHead = getExcelHead();
			//起始位置
			int count = 2;
			for (int j = 0; j < excelData.size(); j++) {
				HSSFRow row = sheetAt.createRow(count + 1);
				for (int i = 0; i < excelHead.size(); i++) {
					row.createCell(i).setCellValue(excelData.get(j).get(i));
				}
				count++;
			}
			out.flush();
			sheets.write(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void downloadExcelTemplate(HttpServletResponse response, String fileName) {
		InputStream inputStream = null;
		ServletOutputStream servletOutputStream = null;
		try {
			String path = "templatefile/fondsTemplate.xls";
			org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:" + path);

			response.setContentType("application/vnd.ms-excel");
			response.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
			response.addHeader("charset", "utf-8");
			response.addHeader("Pragma", "no-cache");
			String encodeName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
			response.setHeader("Content-Disposition", "attachment;filename=" + encodeName + ".xls");

			inputStream = resource.getInputStream();
			servletOutputStream = response.getOutputStream();
			IOUtils.copy(inputStream, servletOutputStream);
			response.flushBuffer();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IoUtil.close(servletOutputStream);
			IoUtil.close(inputStream);
		}
	}

	private List<List<String>> getExcelHead() {
		final List<List<String>> head = CollectionUtil.<List<String>>newArrayList();

		List<String> headCoulumn = new ArrayList<String>();
		headCoulumn.add(FONDS_CODE);
		head.add(headCoulumn);

		headCoulumn = new ArrayList<String>();
		headCoulumn.add(FONDS_NAME);
		head.add(headCoulumn);

		headCoulumn = new ArrayList<String>();
		headCoulumn.add(FONDS_DECR);
		head.add(headCoulumn);

		return head;
	}

	private List<List<String>> getExcelData(String keyword) {
		final List<List<String>> dataLists = CollectionUtil.<List<String>>newArrayList();
		LambdaQueryWrapper<Fonds> queryWrapper = Wrappers.<Fonds>query().lambda();
		queryWrapper.orderByAsc(Fonds::getCreatedTime);
		if (StrUtil.isNotBlank(keyword)) {
			queryWrapper
					.and(wrapper -> wrapper.like(Fonds::getFondsName, StrUtil.trim(keyword))
							.or()
							.like(Fonds::getFondsCode, StrUtil.trim(keyword))
							.or()
							.like(Fonds::getDescription, StrUtil.trim(keyword)));
		}
		//获取所有数据
		List<Fonds> fondsList = this.list(queryWrapper);
		List<String> data = null;
		for (Fonds f : fondsList) {
			data = new ArrayList<>();
			data.add(f.getFondsCode());
			data.add(f.getFondsName());
			data.add(f.getDescription());
			dataLists.add(data);
		}
		return dataLists;
	}

	/***
	 * 获取sheet 表中的字段名称 并且判断缺少的字段
	 */
	private void getHead(List<LinkedHashMap<Integer, String>> dataList, Map<Integer, String> headIndex) {
		if (dataList.size() < 2) {
			throw new ArchiveRuntimeException("导入表格不符合模板规范，请确认后重新导入！");
		}
		LinkedHashMap<Integer, String> headMap = dataList.get(1);
		final List<String> missHead = CollectionUtil.newArrayList();
		if (headMap.size() != EXCEL_HEAD.size()) {
			throw new ArchiveRuntimeException("导入表格不符合模板规范，请确认后重新导入！");
		}
		//检查列（表头）是否有缺少
		EXCEL_HEAD.forEach((k, v) -> {
			if (StrUtil.isBlank(headMap.get(k)) || !v.equals(headMap.get(k))) {
				missHead.add(headMap.get(k));
			}
		});
		if (missHead.size() > 0) {
			throw new ArchiveRuntimeException("导入表格不符合模板规范，请确认后重新导入！");
		}
		//获取字段对应位置
		headMap.forEach((k, v) -> {
			headIndex.put(k, EXCEL_HEAD.get(k));
		});
	}

	/***
	 *  插入数据
	 * */
	private List<Map<String, String>> insertExcelData(Map<Integer, String> headIndex, List<LinkedHashMap<Integer, String>> dataList) {
		final List<Fonds> fondsList = CollectionUtil.<Fonds>newArrayList();
		final List<Map<String, String>> duplicateName = CollectionUtil.<Map<String, String>>newArrayList();
		//获取全部的全宗数据
		List<Fonds> list = this.list();
		//循环数据
		for (int i = 2, length = dataList.size(); i < length; i++) {
			LinkedHashMap<Integer, String> data = dataList.get(i);
			String fondsCode = "";
			String fondsName = "";
			String fondsDesc = "";
			for (int j = 0, len = data.size(); j < len; j++) {
				if (headIndex.get(j).equals(FONDS_CODE)) {
					fondsCode = data.get(j);
				}
				if (headIndex.get(j).equals(FONDS_NAME)) {
					fondsName = data.get(j);
				}
				if (headIndex.get(j).equals(FONDS_DECR)) {
					fondsDesc = data.get(j);
				}
			}
			//校验是否重复
			for (Fonds f : fondsList) {
				if (f.getFondsCode().equals(fondsCode) || f.getFondsName().equals(fondsName)) {
					Map<String, String> map = new HashMap(16);
					map.put("index", String.valueOf(i + 1));
					map.put("fondsCode", fondsCode);
					map.put("fondsName", fondsName);
					map.put("status", "Excel中全宗编码或全宗名称重复");
					duplicateName.add(map);
					break;
				}
			}
			//查询有没有重复的
			String finalFondsCode = fondsCode;
			String finalFondsName = fondsName;
			Fonds fondsExsit = list.parallelStream()
					.filter(fonds -> StrUtil.equals(fonds.getFondsCode(), finalFondsCode) || StrUtil.equals(fonds.getFondsName(), finalFondsName))
					.findAny()
					.orElse(null);
			//不存在，就插入
			if (ObjectUtil.isNull(fondsExsit)) {
				if (fondsCode.length() <= MAX_NAME_LENGTH && fondsName.length() <= MAX_NAME_LENGTH) {
					Fonds newFonds = new Fonds();
					newFonds.setFondsName(fondsName);
					newFonds.setFondsCode(fondsCode);
					newFonds.setDescription(fondsDesc);
					fondsList.add(newFonds);
				} else {
					validateLength(duplicateName, fondsCode, fondsName, i + 1);
				}
			} else {
				Map<String, String> map = new HashMap(16);
				map.put("index", String.valueOf(i + 1));
				map.put("fondsCode", fondsCode);
				map.put("fondsName", fondsName);
				map.put("status", "excel中数据与系统中全宗编码或全宗名称重复");
				duplicateName.add(map);
			}
		}

		if (CollectionUtil.isNotEmpty(fondsList) && CollectionUtil.isEmpty(duplicateName)) {
			//保存全宗组
//			fondsList.stream().forEach(fonds -> {
//				createFondsRoleGroup(fonds);
//			});
			this.saveBatch(fondsList);
		}
		return duplicateName;
	}

	private void validateLength(List<Map<String, String>> duplicateName, String fondsCode, String fondsName, int i) {
		Map<String, String> excelDetail = new HashMap(16);
		excelDetail.put("index", String.valueOf(i));
		excelDetail.put("fondsCode", fondsCode);
		excelDetail.put("fondsName", fondsName);
		if (fondsName.length() > MAX_NAME_LENGTH && fondsCode.length() > MAX_NAME_LENGTH) {
			excelDetail.put("status", "全宗名称和全宗编码过长");
		} else if (fondsName.length() > MAX_NAME_LENGTH) {
			excelDetail.put("status", "全宗名称过长");
		} else {
			excelDetail.put("status", "全宗编码过长");
		}
		duplicateName.add(excelDetail);
	}


	@Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public R updateFondsById(Fonds fonds) {
		//判断同一个租户下是有存在相同的全宗
		Fonds selectFonds = this.getOne(
				Wrappers.<Fonds>query().lambda()
						.and(wrapper -> wrapper.eq(Fonds::getFondsCode, fonds.getFondsCode()).or().eq(Fonds::getFondsName, fonds.getFondsName()))
						.ne(Fonds::getFondsId, fonds.getFondsId())
		);
		if (ObjectUtil.isNotNull(selectFonds)) {
			throw new ArchiveRuntimeException(String.format("同一个租户下存在相同的全宗编码：[%s],或相同的全宗名称：[%s]", selectFonds.getFondsCode(), selectFonds.getFondsName()));
		}
		Fonds old = this.getFondsById(fonds.getFondsId());
		if(!fonds.getFondsCode().equals(old.getFondsCode())){
			List<FondsArchiveTree> selectFodsTreeList = fondsArchiveTreeService.getFondsArchiveTreeByFondsCode(old.getFondsCode());
			if (CollUtil.isNotEmpty(selectFodsTreeList)) {
				throw new ArchiveRuntimeException(String.format("此[%s]全宗代码已经绑定到档案树无法修改全宗代码", fonds.getFondsCode()));
			}
			//判断全宗是否绑定权限，有则不允许修改
			R<Boolean> result = remoteFondsAuthService.checkIsUseFonds(old.getFondsCode(),fonds.getTenantId());
			if (ObjectUtil.isNotNull(result) && CommonConstants.SUCCESS == result.getCode()) {
				if (result.getData()) {
					throw new ArchiveRuntimeException("此全宗已绑定权限，无法修改全宗代码");
				}
			}
			//遍历档案门类，只要有一张主表包含全宗数据，即返回，不允许删除
			final List<String> fondsCodes = Arrays.asList(old.getFondsCode(), FondsConstant.GLOBAL_FONDS_CODE);
			final List<ArchiveTypeDTO> archiveTypes = archiveUtil.getArchiveType(fondsCodes);
			archiveTypes.forEach(e -> {
				DynamicArchiveDTO dynamicCountDTO = DynamicArchiveDTO.builder()
						.tableName(archiveUtil.getStorageLocate(e.getTypeCode(), e.getTemplateTableId()))
						.fondsCode(old.getFondsCode()).build();
				R<Boolean> exists = remoteArchiveInnerService.isExistsByCondition(dynamicCountDTO, SecurityConstants.FROM_IN);
				if (ObjectUtil.isNotNull(exists) && exists.getCode() == CommonConstants.SUCCESS && exists.getData()) {
					throw new ArchiveRuntimeException("此全宗已绑定档案数据，无法修改全宗代码");
				}
			});
		}
		boolean flag = this.updateById(fonds);
//		ArchiveType archiveType =new ArchiveType();
//		archiveType.setFondsName(fonds.getFondsName());
//		archiveType.setFondsCode(fonds.getFondsCode());
//
//		if (flag) {
//			createFondsRoleGroup(fonds);
//		}
		return new R<>(flag);
	}

	@Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public R deleteFondsById(Long fondsId) throws ArchiveBusinessException{
		Fonds fonds = this.getById(fondsId);
		if (ObjectUtil.isNull(fonds)) {
			throw new ArchiveRuntimeException("没有此全宗无法删除");
		}
		//删除全宗之前判断是否绑定其他东西
		String message = beforeDelete(fonds.getFondsCode());
		if (StrUtil.isNotBlank(message)) {
			throw new ArchiveRuntimeException(message.substring(0, message.length()-1) + "已绑定此全宗，无法删除！");
		}
		List<FondsArchiveTree> selectFodsTreeList = fondsArchiveTreeService.getFondsArchiveTreeByFondsCode(fonds.getFondsCode());
		if (CollUtil.isNotEmpty(selectFodsTreeList)) {
			throw new ArchiveRuntimeException(String.format("此[%s]全宗代码已经绑定到档案树无法进行删除", fonds.getFondsCode()));
		}
		//判断全宗是否绑定权限，有则不允许删除
		R<Boolean> result = remoteFondsAuthService.checkIsUseFonds(fonds.getFondsCode(),fonds.getTenantId());
		if (ObjectUtil.isNotNull(result) && CommonConstants.SUCCESS == result.getCode()) {
			if (result.getData()) {
				throw new ArchiveRuntimeException("此全宗已绑定权限，无法删除");
			}
		}
		//遍历档案门类，只要有一张主表包含全宗数据，即返回，不允许删除
		final List<String> fondsCodes = Arrays.asList(fonds.getFondsCode(), FondsConstant.GLOBAL_FONDS_CODE);
		final List<ArchiveTypeDTO> archiveTypes = archiveUtil.getArchiveType(fondsCodes);
		archiveTypes.forEach(e -> {
			DynamicArchiveDTO dynamicCountDTO = DynamicArchiveDTO.builder()
					.tableName(archiveUtil.getStorageLocate(e.getTypeCode(), e.getTemplateTableId()))
					.fondsCode(fonds.getFondsCode()).build();
			R<Boolean> exists = remoteArchiveInnerService.isExistsByCondition(dynamicCountDTO, SecurityConstants.FROM_IN);
			if (ObjectUtil.isNotNull(exists) && exists.getCode() == CommonConstants.SUCCESS && exists.getData()) {
				throw new ArchiveRuntimeException("此全宗已绑定档案数据，无法删除");
			}
		});
		//删除全宗部门关系
		remoteDeptFondsService.removeFondsCodeByFondsCode(fonds.getFondsCode());
		return new R<>(this.removeById(fondsId));
	}

	public String beforeDelete(String fondsCode) throws ArchiveBusinessException {
		StringBuilder message = new StringBuilder();
		//部门管理关联全宗
		List<DeptSyncTreeNode> dept = remoteDeptService.getDeptTrees().getData();
		List<DeptSyncTreeNode> collect = dept.stream().filter(e -> CollUtil.isNotEmpty(e.getFondsCodeList())).collect(Collectors.toList());
		collect.forEach(e -> {
			if (e.getFondsCodeList().contains(fondsCode)) {
				message.append(e.getName()).append("、");
			}
		});
		if (StrUtil.isNotBlank(message)) {
			throw new ArchiveRuntimeException(message.substring(0, message.length()-1) + "已关联此全宗，无法删除！");
		}
		//角色管理
		List<SysRole> role = remoteSysRoleAuthService.getRoleClassByFondsCode(fondsCode).getData();
		if (CollUtil.isNotEmpty(role)) {
			message.append("角色管理、");
		}
		//归档范围管理
		List<FilingScopeDTO> filingScopes = remoteFilingScopeService.getTreeById(-1L, CollUtil.newArrayList()).getData();
		List<FilingScopeDTO> collect1 = filingScopes.stream().filter(e -> fondsCode.equals(e.getFondsCode())).collect(Collectors.toList());
		if (CollUtil.isNotEmpty(collect1)) {
			message.append("归档范围管理、");
		}
		//档案树管理
		List<FondsArchiveTreeSyncTreeNode> trees = remoteArchiveTreeService.getFondsNode().getData();
		List<FondsArchiveTreeSyncTreeNode> collect2 = trees.stream().filter(e -> fondsCode.equals(e.getFondsCode())).collect(Collectors.toList());
		if (CollUtil.isNotEmpty(collect2)) {
			message.append("档案树管理、");
		}
		//档案门类管理
		List<ArchiveTypeTreeNode> typeTreeNodes = remoteArchiveTypeService.getArchiveTypeTree(-1L, NodeTypeEnum.CLAZZ.getValue(), CollUtil.newArrayList()).getData();
		List<ArchiveTypeTreeNode> collect3 = typeTreeNodes.stream().filter(e -> fondsCode.equals(e.getFondsCode())).collect(Collectors.toList());
		if (CollUtil.isNotEmpty(collect3)) {
			message.append("档案门类管理、");
		}
		//TO-DO 数据字典
		return message.toString();
	}

	@Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public R saveFonds(Fonds fonds) {
		String fondsName = fonds.getFondsName().replace(" ","");
		fonds.setFondsName(fondsName);
		String fondsCode =fonds.getFondsCode().replace(" ","");
		fonds.setFondsCode(fondsCode);
		String value = FilterUtil.checkContain(fondsName);
		if(!value.equals(FilterUtil.OK)){
			return new R<>().fail("false", value);
		}
		String value1 = FilterUtil.checkContain(fondsCode);
		if(!value1.equals(FilterUtil.OK)){
			return new R<>().fail("false", value1);
		}
		//校验当前用户是否可以继续添加全宗
		if (!registerEnableAddFonds(null)) {
			return new R<>().fail("false", "全宗数量超过注册限制");
		}
		if (!userEnableAddFonds(null)) {
			return new R<>().fail("false", "全宗数量超过租户限制");
		}
		Fonds selectFonds = null;
		List<Fonds> otherFonds = null;
		if (ObjectUtil.isNotNull(fonds.getTenantId())) {
			//数据初始化条件
			//判断同一个租户下是否存在相同的全宗编码或相同名称
			selectFonds = this.getOne(
					Wrappers.<Fonds>lambdaQuery()
							.eq(Fonds::getTenantId, fonds.getTenantId())
							.and(wrapper -> wrapper.eq(Fonds::getFondsCode, fonds.getFondsCode()).or().eq(Fonds::getFondsName, fonds.getFondsName()))
			);
		} else {
			//正常保存
			selectFonds = this.getOne(
					Wrappers.<Fonds>lambdaQuery()
							.and(wrapper -> wrapper.eq(Fonds::getFondsCode, fonds.getFondsCode()).or().eq(Fonds::getFondsName, fonds.getFondsName()))
			);
		}
		if (ObjectUtil.isNotNull(selectFonds)) {
			throw new ArchiveRuntimeException(String.format("同一个租户下存在相同的全宗编码：[%s],或相同的名称：[%s],无法新增", selectFonds.getFondsCode(), selectFonds.getFondsName()));
		}

		if(!checkFonds(fonds.getFondsCode(), fonds.getFondsName())){
			throw new ArchiveRuntimeException(String.format("其他租户下存在相同的全宗编码：[%s],或相同的名称：[%s],无法新增", fonds.getFondsCode(), fonds.getFondsName()));
		}
		boolean flag = this.save(fonds);
//		if (flag) {
//			createFondsRoleGroup(fonds);
//		}
		return new R<>(flag);
	}

	@Override
	public boolean checkFonds(String fondsCode, String fondsName) {
		Long tenantId = TenantContextHolder.getTenantId();
		TenantContextHolder.setTenantId(null);
		List<Fonds> hasOthers = fondsMapper.selectAllFondsByNameOrCode(fondsCode, fondsName);
		TenantContextHolder.setTenantId(tenantId);
		return CollUtil.isEmpty(hasOthers);
	}

	/**
	 * 增加全宗角色组，用户角色的分级授权
	 *  二次更新 角色组、档案门类管理、归档范围管理
	 * @param fonds
	 */
	private void createFondsRoleGroup(Fonds fonds) {
		String fondsName = fonds.getFondsName();
		String fondsCode = fonds.getFondsCode();
		SysRole sysRole = SysRole.builder().roleCode(fonds.getFondsCode() + "_role_group")
				.roleName(fonds.getFondsName() + "角色组")
				.roleType(RoleConstant.RoleType.CLASS).parentId(SysConstant.Virtual.ROOT_CODE).fondsCode(fonds.getFondsCode())
				.tenantId(fonds.getTenantId()).delFlag(String.valueOf(BoolEnum.NO.getCode())).build();

			remoteSysRoleAuthService.saveRole(sysRole);
			remoteArchiveTypeService.updateArchiveTypeTree(fondsName, fondsCode);
			remoteFilingScopeService.updateArchiveFilingScopeTree(fondsName, fondsCode);

	}


	@Override
	public List<Fonds> listByRootTreeCode(List<String> rootTreeCodeList) {
		return getBaseMapper().listByRootTreeCode(rootTreeCodeList);
	}

	@Override
	public R permitAddFonds() {
		if (!registerEnableAddFonds(null)) {
			return new R<>().fail("false", "全宗数量超过注册限制");
		}
		if (!userEnableAddFonds(null)) {
			return new R<>().fail("false", "全宗数量超过租户限制");
		}
		return new R<>().success("true", "全宗数量未超过限制");
	}

	@Override
	@Cacheable(
			key = "'archive-app-management:fondsName:' + #fondsName",
			unless = "#result == null"
	)
	public Fonds getFondsByName(String fondsName) {
		return super.getOne(Wrappers.<Fonds>lambdaQuery().eq(Fonds::getFondsName, fondsName));
	}

	@Override
	@Cacheable(
			key = "'archive-app-management:fondsCode:' + #fondsCode",
			unless = "#result == null"
	)
	public Fonds getFondsByCode(String fondsCode) {
		return super.getOne(Wrappers.<Fonds>lambdaQuery().eq(Fonds::getFondsCode, fondsCode));
	}

	/**
	 * 注册文件中的全宗限制
	 *
	 * @param addCount
	 * @return
	 */
	private boolean registerEnableAddFonds(Integer addCount) {
		if (SecurityUtils.getUser().getTenantId() == 1L) {
			return Boolean.TRUE;
		} else {
			int fondsCount = this.allFondsCodeNum();
			//注册文件中的全宗限制
			R<Integer> fondsCodeNum = remoteSysRegisterService.getFondsCodeNum();
			if (fondsCodeNum.getCode() == CommonConstants.SUCCESS) {
				Integer limitCounts = fondsCodeNum.getData();
				if (limitCounts == 0) {
					return Boolean.TRUE;
				}
				if (ObjectUtil.isNull(addCount)) {
					return ObjectUtil.isNotNull(limitCounts) && fondsCount < limitCounts;
				}
				return ObjectUtil.isNotNull(limitCounts) && (fondsCount + addCount) <= limitCounts;
			}
			return Boolean.FALSE;
		}
	}

	/**
	 * 查询系统中的所有全宗数量
	 * @return
	 */
	@Override
	public Integer allFondsCodeNum() {
		Long OrignTenantId = TenantContextHolder.getTenantId();
		TenantContextHolder.setTenantId(1L);
		int fondsCount = this.count();
		TenantContextHolder.setTenantId(OrignTenantId);
		return fondsCount;
	}

	@Override
	public Page<Fonds> getCopyFondsAuth(Page<Fonds> page, String keyword, String fondsCode) {
		List<Fonds> list = CollUtil.newArrayList();
		LambdaQueryWrapper<Fonds> queryWrapper = Wrappers.<Fonds>query().lambda();
		if (!FondsConstant.GLOBAL_FONDS_CODE.equals(fondsCode)) {
			list.add(FondsConstant.getGlobalFonds());
			page.setSize(page.getSize() - list.size());
			queryWrapper.ne(Fonds::getFondsCode, fondsCode);
		}
		queryWrapper.select(Fonds::getFondsCode,Fonds::getFondsName);
		queryWrapper.orderByAsc(Fonds::getCreatedTime);
		if (StrUtil.isNotBlank(keyword)) {
			queryWrapper
					.and(wrapper -> wrapper.like(Fonds::getFondsName, StrUtil.trim(keyword))
							.or()
							.like(Fonds::getFondsCode, StrUtil.trim(keyword))
							.or()
							.like(Fonds::getDescription, StrUtil.trim(keyword)));
		}
		Page<Fonds> pageFonds = this.page(page, queryWrapper);
		list.addAll(page.getRecords());
		pageFonds.setRecords(list);
		return pageFonds;
	}

	/**
	 * 当前登录用户是否可以继续新增全宗记录
	 *
	 * @param addCount
	 * @return
	 */
	private boolean userEnableAddFonds(Integer addCount) {
	    // 一级缓存问题，需要自带tenant_id
		int fondsCount = this.count(Wrappers.<Fonds>lambdaQuery().eq(Fonds::getTenantId, SecurityUtils.getUser().getTenantId()));
		//租户管理中的全宗限制
		R<Tenant> tenantResult = remoteTenantService.getById(SecurityUtils.getUser().getTenantId());
		if (tenantResult.getCode() == CommonConstants.SUCCESS) {
			Integer limitCounts = tenantResult.getData().getFondsCount();
			if (ObjectUtil.isNull(addCount)) {
				return ObjectUtil.isNotNull(limitCounts) && fondsCount < limitCounts;
			}
			return ObjectUtil.isNotNull(limitCounts) && (fondsCount + addCount) <= limitCounts;
		}

		return Boolean.FALSE;
	}

	@Override
	public List<Fonds> getFondsByIds(List<Integer> fondIds) {
		return this.baseMapper.selectList(Wrappers.<Fonds>lambdaQuery().in(Fonds::getFondsId, Arrays.asList(fondIds)));
	}
}
