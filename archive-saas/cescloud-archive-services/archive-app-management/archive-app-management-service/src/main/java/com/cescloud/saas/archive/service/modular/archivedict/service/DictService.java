
package com.cescloud.saas.archive.service.modular.archivedict.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archivedict.entity.Dict;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.List;
import java.util.Map;


/**
 * 数据字典项
 *
 * @author liudong1
 * @date 2019-03-18 17:44:09
 */
public interface DictService extends IService<Dict> {

	/**
	 * 树形展示，其实只有一层
	 *
	 * @return
	 */
	List<Dict> getDictTree();

	/**
	 * 保存字典项
	 *
	 * @param dict
	 * @return
	 * @throws ArchiveBusinessException
	 */
	Dict saveDict(Dict dict) throws ArchiveBusinessException;

	/**
	 * 通过id删除数据字典项
	 *
	 * @param id 数据字典项ID
	 * @return
	 * @throws ArchiveBusinessException
	 */
	boolean removeDict(Long id) throws ArchiveBusinessException;

	/**
	 * 修改数据字典项
	 *
	 * @param dict
	 * @return
	 */
	R updateDictById(Dict dict) throws ArchiveBusinessException;

	/**
	 * 初始化数据字典
	 *
	 * @param templateId 模板id
	 * @param tenantId   租户id
	 * @return
	 * @throws ArchiveBusinessException
	 */
	R initializeHandle(Long templateId, Long tenantId) throws ArchiveBusinessException;

	/**
	 * 设置code值
	 * 如果不存在，则直接使用label的拼音首字母赋值给code
	 * 如果存在，则code_no+1 拼接到label的拼音首字母，作为code
	 *
	 * @param archiveDict
	 * @param dictCodeAndCodeNoMap  记录每次准备插入的code和对应的codeno(为批量插入提供的setCode方法)
	 * @return
	 */
	Dict setDictCodeWithBatch(final Dict archiveDict,final Map<String,Integer> dictCodeAndCodeNoMap);
}
