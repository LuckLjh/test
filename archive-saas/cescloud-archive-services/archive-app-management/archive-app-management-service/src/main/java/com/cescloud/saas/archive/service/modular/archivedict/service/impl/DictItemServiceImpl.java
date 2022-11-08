
package com.cescloud.saas.archive.service.modular.archivedict.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archivedict.dto.DictItemDTO;
import com.cescloud.saas.archive.api.modular.archivedict.entity.Dict;
import com.cescloud.saas.archive.api.modular.archivedict.entity.DictItem;
import com.cescloud.saas.archive.service.modular.archivedict.mapper.DictItemMapper;
import com.cescloud.saas.archive.service.modular.archivedict.service.DictItemService;
import com.cescloud.saas.archive.service.modular.archivedict.service.DictService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
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
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 数据字典值
 *
 * @author liudong1
 * @date 2019-03-18 17:47:15
 */
@Slf4j
@Service
@CacheConfig(cacheNames = "dict-item")
public class DictItemServiceImpl extends ServiceImpl<DictItemMapper, DictItem> implements DictItemService {

	private final static String DICT_LABEL = "字典项名称";
	private final static String ITEM_CODE = "字典值编码";
	private final static String ITEM_LABEL = "字典值名称";
	private final static String ITEM_DESCRIBE = "字典值描述";
	private final static String CONVERSION_VALUE = "转换值";
	//名称最大长度
	private final static Integer MAX_NAME_LENGTH = 50;

	private final static Map<Integer,String> EXCEL_HEAD = new LinkedHashMap<>();

	static {
		EXCEL_HEAD.put(0,DICT_LABEL);
		EXCEL_HEAD.put(1,ITEM_LABEL);
		EXCEL_HEAD.put(2,ITEM_CODE);
		EXCEL_HEAD.put(3,ITEM_DESCRIBE);
		EXCEL_HEAD.put(4,CONVERSION_VALUE);
	}

	@Autowired
	private DictService dictService;
	@Resource
	private ResourceLoader resourceLoader;

	@Override
	public IPage<DictItem> getPage(Page page, DictItemDTO dictItemDTO) {
		LambdaQueryWrapper<DictItem> queryWrapper = Wrappers.<DictItem>query().lambda();
		queryWrapper.eq(DictItem::getDictCode, dictItemDTO.getDictCode());
		queryWrapper.orderByAsc(DictItem::getSortNo);
		if (StrUtil.isNotBlank(dictItemDTO.getKeyword())) {
			queryWrapper
					.and(wrapper -> wrapper.like(DictItem::getItemCode, StrUtil.trim(dictItemDTO.getKeyword()))
							.or()
							.like(DictItem::getItemLabel, StrUtil.trim(dictItemDTO.getKeyword()))
							.or()
							.like(DictItem::getItemDescribe, StrUtil.trim(dictItemDTO.getKeyword())));
		}
		return this.page(page, queryWrapper);
	}

	@Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public DictItem saveDictItem(DictItem entity) {
		checkSaveRepeat(entity);
		super.save(entity);
		return entity;
	}

	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	@Override
	public DictItem updateDictItem(DictItem entity) {
		checkUpdateRepeat(entity);
		super.updateById(entity);
		return entity;
	}

	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	@Override
	public boolean removeDictItem(Long id) {
		return super.removeById(id);
	}

	/**
	 * 根据字典编码获取字典项列表
	 *
	 * @param dictCode
	 * @return
	 */
	@Cacheable(
			key = "'archive-app-management:dict-item-list:' + #dictCode",
			unless = "#result == null || #result.size() == 0"
	)
	@Override
	public List<DictItem> getItemListByDictCode(String dictCode) {
		return this.list(Wrappers.<DictItem>query().lambda()
				.eq(DictItem::getDictCode, dictCode));
	}
	@Override
	public List<DictItem> getItemListByDictCodeRel(String dictCode,String typeCode){
		List<DictItem> itemListByDictCodeRel = baseMapper.getItemListByDictCodeRel(dictCode, typeCode);
		if(CollectionUtil.isEmpty(itemListByDictCodeRel)){
			itemListByDictCodeRel =this.list(Wrappers.<DictItem>query().lambda()
					.eq(DictItem::getDictCode, dictCode));
		}
		return itemListByDictCodeRel;
	}
	@Cacheable(
			key = "'archive-app-management:codes:' + #dictCodes",
			unless = "#result == null || #result.size() == 0"
	)
	@Override
	public List<DictItem> getDictItemListByDictCodes(String dictCodes) {
		return this.list(Wrappers.<DictItem>query().lambda()
				.in(DictItem::getDictCode, Arrays.asList(dictCodes.split(","))));
	}

	/**
	 * 根据字典编码和项编码 获取对应的值
	 *
	 * @param dictCode 字典项编码
	 * @param itemCode 字典值编码
	 * @return
	 */
	@Cacheable(
			key = "'archive-app-management:dict-item-list:' + #dictCodes + ':' + #itemCode",
			unless = "#result == null"
	)
	@Override
	public DictItem getDictItemByDictCodeAndItemCode(String dictCode, String itemCode,Long tenantId) {
		List<DictItem> dictItemList = this.list(Wrappers.<DictItem>query().lambda()
				.eq(DictItem::getDictCode, dictCode)
				.eq(DictItem::getItemCode, itemCode)
				.eq(DictItem::getTenantId,tenantId));

		if (CollectionUtils.isEmpty(dictItemList)) {
			log.warn("找不到字典编码为[]，字典值编码为[]的字典项", dictCode, itemCode);
			return null;
		}
		if (CollectionUtil.isEmpty(dictItemList)) {
			return null;
		}
		return dictItemList.get(0);
	}
	@Override
	public DictItem getDictItemByDictCodeAndItemLabel(String dictCode, String itemLabel,Long tenantId) {
		List<DictItem> dictItemList = this.list(Wrappers.<DictItem>query().lambda()
				.eq(DictItem::getDictCode, dictCode)
				.eq(DictItem::getItemLabel, itemLabel)
				.eq(DictItem::getTenantId,tenantId));

		if (CollectionUtils.isEmpty(dictItemList)) {
			log.warn("找不到字典编码为[]，字典值编码为[]的字典项", dictCode, itemLabel);
			return null;
		}
		return dictItemList.get(0);
	}

	@Override
	public void exportExcel(HttpServletResponse response, String fileName) {
		OutputStream out = null;
		InputStream fileInputStream = null;
		POIFSFileSystem poifsFileSystem = null;
		HSSFWorkbook sheets = null;
		try {
			String encodeName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
			response.setContentType("application/vnd.ms-excel");
			response.setCharacterEncoding("utf-8");
			response.setHeader("Content-Disposition", "attachment;filename=" + encodeName + ".xls");

			String path = "templatefile/dictTemplate.xls";
			org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:" + path);
			fileInputStream = resource.getInputStream();
			out = response.getOutputStream();
			poifsFileSystem = new POIFSFileSystem(fileInputStream);
			sheets = new HSSFWorkbook(poifsFileSystem);
			HSSFSheet sheetAt = sheets.getSheetAt(0);
			List<List<String>> excelHead = this.getExcelHead();
			List<List<String>> excelData = this.getExcelData();
			//起始位置
			int count = 2;
			for (int j = 0, length = excelData.size(); j < length; j++) {
				HSSFRow row = sheetAt.createRow(count + 1);
				for (int i = 0; i < excelHead.size(); i++) {
					row.createCell(i).setCellValue(excelData.get(j).get(i));
				}
				count++;
			}
			out.flush();
			sheets.write(out);
		} catch (IOException e) {
			log.error("读取excel文件失败", e);
		} finally {
			IoUtil.close(sheets);
			IoUtil.close(poifsFileSystem);
			IoUtil.close(fileInputStream);
			IoUtil.close(out);
		}
	}

	/**
	 * 得到导出数据字典 的表头
	 *
	 * @return
	 */
	private List<List<String>> getExcelHead() {
		final List<List<String>> head = CollectionUtil.newArrayList();
		EXCEL_HEAD.forEach((k,v) -> {
			List<String> headCoulumn = new ArrayList<String>();
			headCoulumn.add(v);
			head.add(headCoulumn);
		});
		return head;
	}

	/**
	 * 得到导出数据字典 的数据
	 *
	 * @return
	 */
	private List<List<String>> getExcelData() {
		List<DictItemDTO> dictItemDtoList = baseMapper.exportExcel();
		List<List<String>> data = dictItemDtoList.stream().map(diDto -> CollectionUtil.newArrayList(diDto.getDictLabel(),diDto.getItemLabel(),diDto.getItemCode(),diDto.getItemDescribe(),diDto.getItemValue())).collect(Collectors.toList());
		return data;
	}

	/**
	 * 导入数据字典（excel）
	 *
	 * @param file excel文件
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(allEntries = true)
	public R insertExcel(MultipartFile file) throws IOException {
		InputStream inputStream = null;
		try {
			inputStream = file.getInputStream();
			List<LinkedHashMap<Integer,String>> dataList = EasyExcel.read(inputStream).sheet(0).doReadSync();
			if (CollectionUtil.isEmpty(dataList)) {
				log.warn("数据字典导入的excel中数据为空!");
				throw new ArchiveRuntimeException("导入数据为空，请确认后重新导入！");
			}
			//存储头部字段对应顺序： {0：字典项名称，1：字典值编码，2：字典值}
			final Map<Integer, String> headIndex = CollectionUtil.<Integer, String>newHashMap();
			//检查头部信息
			getHead(dataList, headIndex);

			if (dataList.size() == 3) {
				throw new ArchiveRuntimeException("导入数据为空，请确认后重新导入！");
			}
			//插入excel数据
			List<Map<String, String>> maps = insertExcelData(headIndex, dataList);
			if (maps.size() == 0) {
				return new R<>(null, "导入完成！");
			} else {
				return new R<>().fail(maps, "导入失败！");
			}
		} finally {
			IoUtil.close(inputStream);
		}
	}

	/**
	 * 插入excel数据
	 *
	 * @param dataList
	 */
	private List<Map<String, String>> insertExcelData(Map<Integer, String> headIndex, List<LinkedHashMap<Integer,String>> dataList) {
		final List<DictItem> dictItemsList = CollectionUtil.<DictItem>newArrayList();
		final List<Map<String, String>> duplicateName = CollectionUtil.<Map<String, String>>newArrayList();
		//待新增字典项集合
		final List<Dict> addDictsList = CollectionUtil.<Dict>newArrayList();
		//待新增字典项code和code最大值，防止code值重复情况下，无法获取数据库最大code值编号（此时查询数据库无法得到数据）
		final Map<String, Integer> dictCodeAndCodeNoMap = CollectionUtil.<String, Integer>newHashMap();
		//获取所有的字典项和字典值
		List<Dict> dictList = dictService.list();
		List<DictItem> itemlist = this.list();
		//循环数据
		for (int i = 2, length = dataList.size(); i < length; i++) {
			LinkedHashMap<Integer,String> data = dataList.get(i);
			String dictLabel = "";
			String itemCode = "";
			String itemLabel = "";
			String itemDescribe = "";
			String itemValue = "";
			//循环每一行
			for (int j = 0; j < data.size(); j++) {
				if (DICT_LABEL.equals(headIndex.get(j))) {
					dictLabel = data.get(j);
					continue;
				}
				if (ITEM_CODE.equals(headIndex.get(j))) {
					itemCode = data.get(j);
					continue;
				}
				if (ITEM_LABEL.equals(headIndex.get(j))) {
					itemLabel = data.get(j);
					continue;
				}
				if (ITEM_DESCRIBE.equals(headIndex.get(j))) {
					itemDescribe = data.get(j);
					continue;
				}
				if (CONVERSION_VALUE.equals(headIndex.get(j))) {
					itemValue = data.get(j);
					continue;
				}
			}
			String finalDictLabel = dictLabel;
			Dict dict = dictList.parallelStream().filter(d -> StrUtil.equals(d.getDictLabel(), finalDictLabel)).findAny().orElse(null);
			//数据库不存在情况下，从待新增字典项集合中获取
			if (ObjectUtil.isNull(dict)) {
				dict = addDictsList.parallelStream().filter(d -> StrUtil.equals(d.getDictLabel(), finalDictLabel)).findAny().orElse(null);
			}
			//不存在，则新增后导入
			if (ObjectUtil.isNull(dict)) {
				dict = new Dict(dictLabel, dictLabel);
				dict.setTenantId(SecurityUtils.getUser().getTenantId());
				dict = dictService.setDictCodeWithBatch(dict, dictCodeAndCodeNoMap);
				if (StrUtil.isBlank(dict.getDictCode())) {
					Map excelDetail = CollectionUtil.newHashMap(4);
					excelDetail.put("index", String.valueOf(i + 1));
					excelDetail.put("dictLabel", dictLabel);
					excelDetail.put("itemLabel", itemLabel);
					excelDetail.put("status", "字典编码生成失败，请检查字典名称！");
					duplicateName.add(excelDetail);
					continue;
				}
				addDictsList.add(dict);
			}
			final String finalDictCode = dict.getDictCode();
			if (itemCode.length() > MAX_NAME_LENGTH) {
				Map excelDetail = CollectionUtil.newHashMap(4);
				excelDetail.put("index", String.valueOf(i + 1));
				excelDetail.put("dictLabel", dictLabel);
				excelDetail.put("itemLabel", itemLabel);
				excelDetail.put("status", "字典分类名称过长（不得超过100个字符）");
				duplicateName.add(excelDetail);
			}
			if (itemLabel.length() > MAX_NAME_LENGTH) {
				Map excelDetail = CollectionUtil.newHashMap(4);
				excelDetail.put("index", String.valueOf(i + 1));
				excelDetail.put("dictLabel", dictLabel);
				excelDetail.put("itemLabel", itemLabel);
				excelDetail.put("status", "字典分类编码过长（不得超过100个字符）");
				duplicateName.add(excelDetail);

			}
			String finalItemCode = itemCode;
			DictItem dictItem = itemlist.parallelStream()
					.filter(item -> StrUtil.equals(item.getDictCode(), finalDictCode) && StrUtil.equals(item.getItemCode(), finalItemCode))
					.findAny().orElse(null);

			//不存在，就插入
			if (ObjectUtil.isNull(dictItem)) {
				dictItemsList.add(new DictItem(finalDictCode, itemCode, itemLabel, itemDescribe,itemValue));
			} else {
				//存在
				dictItem.setItemLabel(itemLabel);
				dictItem.setItemDescribe(itemDescribe);
				dictItem.setItemValue(itemValue);
				dictItemsList.add(dictItem);
			}
		}
		if (duplicateName.size() > 0) {
			return duplicateName;
		}
		if (CollectionUtil.isNotEmpty(addDictsList)) {
			log.debug("开始批量插入字典分类！");
			dictService.saveOrUpdateBatch(addDictsList);
		}
		if (CollectionUtil.isNotEmpty(dictItemsList)) {
			log.debug("开始批量插入数据字典值！");
			this.saveOrUpdateBatch(dictItemsList);
		}
		return duplicateName;
	}

	private void getHead(List<LinkedHashMap<Integer,String>> dataList, Map<Integer, String> headIndex) {
		LinkedHashMap<Integer,String> headMap = dataList.get(1);
		final List<String> missHead = CollectionUtil.newArrayList();
		if (dataList.size() < 2) {
			throw new ArchiveRuntimeException("导入表格不符合模板规范，请确认后重新导入！");
		}

		//检查列（表头）是否有缺少
		EXCEL_HEAD.forEach((k,v) -> {
			if (StrUtil.isBlank(headMap.get(k)) || !v.equals(headMap.get(k))) {
				missHead.add(headMap.get(k));
			}
		});
		if (missHead.size() > 0) {
			throw new ArchiveRuntimeException("导入表格不符合模板规范，请确认后重新导入！");
		}
		if (headMap.size() != EXCEL_HEAD.size()) {
			throw new ArchiveRuntimeException("导入表格不符合模板规范，请确认后重新导入！");
		}
		//获取字段对应位置
		headMap.forEach((k,v) -> {
			headIndex.put(k,EXCEL_HEAD.get(k));
		});
	}

	@Override
	public List<ArrayList<String>> getDataDictionary(Long tenantId) {
		//获取字典项
		List<Dict> dicts = dictService.list(Wrappers.<Dict>lambdaQuery().eq(Dict::getTenantId, tenantId));
		//处理数据
		Map<String, String> dictMap = dicts.stream().collect(Collectors.toMap(Dict::getDictCode, Dict::getDictLabel));
		List<DictItem> dictItems = this.list(Wrappers.<DictItem>lambdaQuery().eq(DictItem::getTenantId, tenantId));
		// 	字典项	字典项编码	编码名称	编码值
		return dictItems.stream().map(dictItem -> CollUtil.newArrayList(dictMap.get(dictItem.getDictCode()), dictItem.getDictCode(), dictItem.getItemLabel(),
				dictItem.getItemCode(),StrUtil.trimToEmpty(dictItem.getItemValue()))).collect(Collectors.toList());
	}

	/**
	 * 检测编码名称、编码值是否重复
	 */
	private void checkSaveRepeat(DictItem entity) {
		DictItem label = super.getOne(new LambdaQueryWrapper<DictItem>()
				.eq(DictItem::getDictCode, entity.getDictCode())
				.eq(DictItem::getItemLabel, entity.getItemLabel()));
		if (ObjectUtil.isNotNull(label)) {
			throw new ArchiveRuntimeException(String.format("同一分类下存在相同的编码名称【%s】，无法新增！", entity.getItemLabel()));
		}

		DictItem code = super.getOne(new LambdaQueryWrapper<DictItem>()
				.eq(DictItem::getDictCode, entity.getDictCode())
				.eq(DictItem::getItemCode, entity.getItemCode()));
		if (ObjectUtil.isNotNull(code)) {
			throw new ArchiveRuntimeException(String.format("同一分类下存在相同的编码值【%s】，无法新增！", entity.getItemCode()));
		}
	}

	private void checkUpdateRepeat(DictItem entity) {
		List<DictItem> label = super.list(new LambdaQueryWrapper<DictItem>()
				.eq(DictItem::getDictCode, entity.getDictCode())
				.eq(DictItem::getItemLabel, entity.getItemLabel())
				.ne(DictItem::getId, entity.getId()));
		if (CollUtil.isNotEmpty(label) && entity.getItemLabel().equals(label.get(0).getItemLabel())) {
			throw new ArchiveRuntimeException(String.format("同一分类下存在相同的编码名称【%s】，无法修改！", entity.getItemLabel()));
		}

		List<DictItem> code = super.list(new LambdaQueryWrapper<DictItem>()
				.eq(DictItem::getDictCode, entity.getDictCode())
				.eq(DictItem::getItemCode, entity.getItemCode())
				.ne(DictItem::getId, entity.getId()));
		if (CollUtil.isNotEmpty(code) && entity.getItemCode().equals(code.get(0).getItemCode())) {
			throw new ArchiveRuntimeException(String.format("同一分类下存在相同的编码值【%s】，无法修改！", entity.getItemCode()));
		}
	}

	@Override
	public void setOrder(List<Long> ids) {
		List<DictItem> DictItems = this.listByIds(ids);
		final List<DictItem> list = IntStream.rangeClosed(1, ids.size()).mapToObj(i -> {
			final DictItem dictItem = DictItems.parallelStream().filter(e -> e.getId().equals(ids.get(i - 1))).findAny().get();
			dictItem.setSortNo(i);
			return dictItem;
		}).collect(Collectors.toList());
		this.updateBatchById(list);
	}
}
