package com.cescloud.saas.archive.service.modular.keyword.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.keyword.dto.KeyWordDTO;
import com.cescloud.saas.archive.api.modular.keyword.entity.KeyWord;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


/**
 * 主题词管理
 *
 * @author qianjiang
 * @date 2019-03-22 18:21:28
 */
public interface KeyWordService extends IService<KeyWord> {

	/**
	 * 主题词分页查询
	 * @param page 分页对象
	 * @param keyWordDTO 查询实体
	 * @return
	 */
	IPage<KeyWord> getPage(Page page, KeyWordDTO keyWordDTO);

	/**
	 * excel导入主题词
	 * @param file excel文件
	 * @return
	 */
	R insertExcel(MultipartFile file) throws IOException;

	/**
	 * 修改主题词
	 * @param keyWord 主题词对象
	 * @return
	 */
	R updateKeyWordById(KeyWord keyWord);

	/**
	 * 新增主题词
	 * @param keyWord 主题词对象
	 * @return
	 */
	R saveKeyWord(KeyWord keyWord);

	/**
	 * 导出主题词
	 * @param response
	 * @param fileName 文件名
	 */
	void exportExcel(HttpServletResponse response, String fileName);

	/**
	 * 导出主题词模板
	 * @param response
	 * @param fileName
	 */
	void downloadExcelTemplate(HttpServletResponse response,String fileName);

}
