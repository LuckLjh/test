package com.cescloud.saas.archive.service.modular.keyword.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.keyword.dto.KeyWordDTO;
import com.cescloud.saas.archive.api.modular.keyword.entity.KeyWord;
import com.cescloud.saas.archive.service.modular.keyword.mapper.KeyWordMapper;
import com.cescloud.saas.archive.service.modular.keyword.service.KeyWordService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import lombok.Cleanup;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 主题词管理
 *
 * @author qianjiang
 * @date 2019-03-22 18:21:28
 */
@Service
public class KeyWordServiceImpl extends ServiceImpl<KeyWordMapper, KeyWord> implements KeyWordService {

	@Resource
	private ResourceLoader resourceLoader;

	private final static String KEY_WORD = "主题词";
	private final static String KEY_WORD_REMARK = "备注";
	/**
	 * 	名称最大长度
	 */
	private final static Integer MAX_NAME_LENGTH = 50;

	private final static Map<Integer,String> EXCEL_HEAD = new LinkedHashMap<>();

	static {
		EXCEL_HEAD.put(0,KEY_WORD);
		EXCEL_HEAD.put(1,KEY_WORD_REMARK);
	}

	@Override
	public IPage<KeyWord> getPage(Page page, KeyWordDTO keyWordDTO) {
		LambdaQueryWrapper<KeyWord> queryWrapper = Wrappers.<KeyWord>query().lambda();
		if (StrUtil.isNotBlank(keyWordDTO.getKeyword())) {
			queryWrapper
					.and(wrapper -> wrapper.like(KeyWord::getKeyword, StrUtil.trim(keyWordDTO.getKeyword()))
							.or()
							.like(KeyWord::getKeywordRemark, StrUtil.trim(keyWordDTO.getKeyword())));
		}
		return this.page(page,queryWrapper);
	}


	private List<List<String>> getExcelHead() {
		List<List<String>> head = new ArrayList<List<String>>();
		List<String> headCoulumn = new ArrayList<String>();
		headCoulumn.add("主题词");
		head.add(headCoulumn);

		headCoulumn = new ArrayList<String>();
		headCoulumn.add("备注");
		head.add(headCoulumn);

		return head;
	}

	private List<List<String>> getExcelData() {
		Long tenantId = SecurityUtils.getUser().getTenantId();
		List<KeyWord> keyWords = baseMapper.selectList(Wrappers.<KeyWord>query().lambda().eq(KeyWord::getTenantId, tenantId));
		List<List<String>> data = new ArrayList<>();
		for (KeyWord keyWord : keyWords) {
			List<String> item = new ArrayList<>();
			item.add(keyWord.getKeyword());
			item.add(keyWord.getKeywordRemark());
			data.add(item);
		}
		return data;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R insertExcel(MultipartFile file) throws IOException {
		@Cleanup InputStream inputStream = file.getInputStream();
		final List<LinkedHashMap<Integer,String>> dataList = EasyExcel.read(inputStream).sheet(0).doReadSync();
		if (CollectionUtil.isEmpty(dataList)) {
			return new R<>().fail(null, "导入数据为空，请确认后重新导入！");
		}
		//存储头部字段对应顺序： {0：主题词名称，1：主题词描述}
		Map<Integer, String> headIndex = new HashMap<>(16);
		//检查头部信息
		getHead(dataList, headIndex);

		if(dataList.size() == 2){
			throw new ArchiveRuntimeException("导入数据为空，请确认后重新导入！");
		}
		//插入excel数据
		List<Map<String, String>> maps = insertExcelData(dataList);
		if(CollectionUtil.isEmpty(maps)){
			return new R<>(null, "导入完成！");
		}else{
			return new R<>().fail(maps,"导入失败！");
		}
	}

	/***
	 * 获取sheet 表中的字段名称 并且判断缺少的字段
	 */
	private void getHead(List<LinkedHashMap<Integer,String>> dataList, Map<Integer, String> headIndex){
		if(dataList.size() < 2) {
			throw new ArchiveRuntimeException("导入表格不符合模板规范，请确认后重新导入！");
		}

		LinkedHashMap<Integer,String> headMap = dataList.get(1);
		final List<String> missHead = CollectionUtil.newArrayList();
		if(headMap.size() != EXCEL_HEAD.size()){
			throw new ArchiveRuntimeException( "导入表格不符合模板规范，请确认后重新导入！");
		}
		//检查列（表头）是否有缺少
		EXCEL_HEAD.forEach((k,v) -> {
			if(StrUtil.isBlank(headMap.get(k)) || !v.equals(headMap.get(k))) {
				missHead.add(headMap.get(k));
			}
		});
		if (missHead.size() > 0) {
			throw new ArchiveRuntimeException( "导入表格不符合模板规范，请确认后重新导入！");
		}
		//获取字段对应位置
		headMap.forEach((k,v) -> {
			headIndex.put(k,EXCEL_HEAD.get(k));
		});
	}
	/**
	 * 修改数据
	 * @param keyWord
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R updateKeyWordById(KeyWord keyWord) {
		KeyWord keywordName = this.getOne(Wrappers.<KeyWord>query().lambda().eq(KeyWord::getKeyword,keyWord.getKeyword()).ne(KeyWord::getKeywordId,keyWord.getKeywordId()),false);
		if (ObjectUtil.isNotNull(keywordName)) {
			throw new ArchiveRuntimeException(String.format("同一个租户下存在相同的主题词名称：[%s]",keyWord.getKeyword()));
		}
		return new R<>(updateById(keyWord));
	}

	/**
	 * 新增数据
	 * @param keyWord
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R saveKeyWord(KeyWord keyWord) {
		KeyWord keywordName = this.getOne(Wrappers.<KeyWord>query().lambda().eq(KeyWord::getKeyword,keyWord.getKeyword()),false);
		if (ObjectUtil.isNotNull(keywordName)) {
			throw new ArchiveRuntimeException(String.format("同一个租户下存在相同的主题词名称：[%s]",keyWord.getKeyword()));
		}
		return new R<>(this.save(keyWord));
	}

	@Override
	public void exportExcel(HttpServletResponse response, String fileName) {
		ServletOutputStream out = null;
		InputStream fileInputStream = null;
		POIFSFileSystem poifsFileSystem = null;
		HSSFWorkbook sheets = null;
		try {
			String encodeName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
			response.setContentType("application/vnd.ms-excel");
			response.setCharacterEncoding("utf-8");
			response.setHeader("Content-Disposition", "attachment;filename=" + encodeName + ".xls");
			//加载模板
			String path = "templatefile/keyword.xls";
			org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:"+path);
			fileInputStream = resource.getInputStream();
			poifsFileSystem = new POIFSFileSystem(fileInputStream);
			sheets = new HSSFWorkbook(poifsFileSystem);
			out = response.getOutputStream();
			HSSFSheet sheetAt = sheets.getSheetAt(0);
			List<List<String>> excelData = this.getExcelData();
			List<List<String>> excelHead = this.getExcelHead();
			//起始位置
			int count = 2;
			for(int j = 0, length = excelData.size(); j < length; j++){
				HSSFRow row = sheetAt.createRow(count + 1 );
				for (int i = 0; i < excelHead.size(); i++) {
					row.createCell(i).setCellValue(excelData.get(j).get(i));
				}
				count++;
			}
			out.flush();
			sheets.write(out);
		} catch (IOException e) {
			log.error("读取模板文件失败");
		} finally {
			IoUtil.close(sheets);
			IoUtil.close(poifsFileSystem);
			IoUtil.close(fileInputStream);
			IoUtil.close(out);
		}
	}

	@Override
	public void downloadExcelTemplate(HttpServletResponse response, String fileName) {
		InputStream inputStream = null;
		ServletOutputStream servletOutputStream = null;
		try {
			String path = "templatefile/keyword.xls";
			org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:"+path);

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
			log.error("读取模板文件失败");
		} finally {
			IoUtil.close(servletOutputStream);
			IoUtil.close(inputStream);
		}
	}


	/***
	 *  插入数据
	 * */
	private List<Map<String,String>> insertExcelData(List<LinkedHashMap<Integer,String>> dataList) {
		final List<KeyWord> keyWords = CollectionUtil.<KeyWord>newArrayList();
		final List<Map<String,String>> duplicateName = CollectionUtil.<Map<String,String>>newArrayList();
		//查询库中所有数据
		List<KeyWord> list = this.list();
		//循环数据
		for (int i = 2, length = dataList.size(); i < length; i++) {
			LinkedHashMap<Integer,String> data = dataList.get(i);
			String keyword = data.get(0);
			String keywordRemark = data.get(1);
			//校验主题词名称长度
			validateLength(duplicateName,keyword,keywordRemark);
			//校验是否重复
			for (KeyWord word : keyWords) {
				if(word.getKeyword().equals(data.get(0))){
					Map<String,String > map = new HashMap(16);
					map.put("index", String.valueOf(i));
					map.put("keyword",data.get(0));
					map.put("status","Excel中主题词名称重复");
					duplicateName.add(map);
					break;
				}
			}
			KeyWord existKeyWord = list.parallelStream().filter(word -> StrUtil.equals(word.getKeyword(),keyword))
					.findAny()
					.orElse(null);
			if (ObjectUtil.isNull(existKeyWord)) {
				if(StrUtil.isNotBlank(keywordRemark) && keywordRemark.length() > MAX_NAME_LENGTH){
					Map<String,String > map = new HashMap(16);
					map.put("index", String.valueOf(i));
					map.put("keyword",keyword);
					map.put("status","主题词描述过长（不得超过50个字符）");
					duplicateName.add(map);
				}else{
					KeyWord keyWord = new KeyWord();
					keyWord.setKeyword(keyword);
					keyWord.setKeywordRemark(keywordRemark);
					keyWords.add(keyWord);
				}
			} else {
				Map<String,String > map = new HashMap(16);
				map.put("index", String.valueOf(i));
				map.put("keyword",keyword);
				map.put("status","excel中数据与系统中主题词名称重复");
				duplicateName.add(map);
			}
		}
		if (CollectionUtil.isNotEmpty(keyWords) && CollectionUtil.isEmpty(duplicateName)) {
			this.saveBatch(keyWords);
		}
		return duplicateName;
	}

	private void validateLength(List<Map<String,String>> duplicateName,String keyword,String keywordRemark) {
		Map<String,String> excelDetail = new HashMap(16);
		if (StrUtil.isBlank(keyword)) {
			excelDetail.put("keyword",keyword);
			excelDetail.put("keywordRemark",keywordRemark);
			excelDetail.put("status","主题词名称为空");
			duplicateName.add(excelDetail);
		} else {
			if (keyword.length() > MAX_NAME_LENGTH) {
				excelDetail.put("keyword",keyword);
				excelDetail.put("keywordRemark",keywordRemark);
				excelDetail.put("status","主题词名称过长");
				duplicateName.add(excelDetail);
			}
		}

	}
}
