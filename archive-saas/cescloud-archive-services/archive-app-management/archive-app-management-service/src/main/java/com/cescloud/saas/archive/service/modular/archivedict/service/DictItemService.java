
package com.cescloud.saas.archive.service.modular.archivedict.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archivedict.dto.DictItemDTO;
import com.cescloud.saas.archive.api.modular.archivedict.entity.DictItem;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * 数据字典值
 *
 * @author liudong1
 * @date 2019-03-18 17:47:15
 */
public interface DictItemService extends IService<DictItem> {

	/**
	 * 字典值分页查询
	 * @param page 分页对象
	 * @param dictItemDTO 查询参数
	 * @return
	 */
	IPage<DictItem> getPage(Page page, DictItemDTO dictItemDTO);

	/**
	 * 新增数据字典值
	 * @param entity
	 * @return
	 */
	DictItem saveDictItem(DictItem entity);

	/**
	 * 修改数据字典值
	 * @param entity
	 * @return
	 */
	DictItem updateDictItem(DictItem entity);

	/**
	 * 根据id删除数据字典值
	 * @param id 主键
	 * @return
	 */
	boolean removeDictItem(Long id);

	/**
	 * 通过字典项编码查询数据字典值
	 * @param dictCode 字典项编码
	 * @return
	 */
	List<DictItem> getItemListByDictCode(String dictCode);

	/**
	 * 通过字段项 和档案类型获取绑定关联关系
	 * @param dictCode
	 * @param typeCode
	 * @return
	 */
	 List<DictItem> getItemListByDictCodeRel(String dictCode,String typeCode);

	/**
	 * 根据字典项编码查询数据字典值列表
	 * @param dictCodes
	 * @return
	 */
	List<DictItem> getDictItemListByDictCodes(String dictCodes);

	/**
	 * 导入数据字典
	 * @param file excel文件
	 * @return
	 */
	R insertExcel(MultipartFile file) throws IOException;

	/**
	 * 根据字典项编码、字典值编码和租户id获取字典值
	 * @param dictCode
	 * @param itemValue
	 * @return
	 */
	DictItem getDictItemByDictCodeAndItemCode(String dictCode,String itemValue,Long tenantId);

	/**
	 * 根据字典项编码、字典值和租户id获取字典值
	 * @param dictCode
	 * @param itemLabel
	 * @return
	 */
	DictItem getDictItemByDictCodeAndItemLabel(String dictCode,String itemLabel,Long tenantId);

	/**
	 * 导出excel
	 * @param response
	 * @param fileName excel文件名
	 */
	void exportExcel(HttpServletResponse response, String fileName);

	/**
	 *	获取当前租户 数据字典
	 * @param tenantId
	 * @return
	 */
	List<ArrayList<String>> getDataDictionary(Long tenantId);

	void setOrder(List<Long> ids);
}
