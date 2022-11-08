
package com.cescloud.saas.archive.service.modular.fwimp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.fwimp.dto.OaColumnDTO;
import com.cescloud.saas.archive.api.modular.fwimp.dto.OaImpAndColumnDTO;
import com.cescloud.saas.archive.api.modular.fwimp.dto.OaImportDTO;
import com.cescloud.saas.archive.api.modular.fwimp.entity.OaColumn;
import com.cescloud.saas.archive.api.modular.fwimp.entity.OaImport;
import com.cescloud.saas.archive.service.modular.common.core.util.R;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * oa 导入
 *
 * @author hyq
 * @date 2019-03-21 12:04:54
 */
public interface OaImportService extends IService<OaImport> {

	/**
	 * 全宗分页查询
	 * @param page 分页参数
	 * @param OaImportDTO 查询实体参数
	 * @return 分页对象
	 */
	IPage<OaImport> getPage(Page page, OaImportDTO oaImportDTO);

	/**
	 * 激活oa 任务
	 * @param id
	 * @return
	 */
	R activate(String id);

	/**
	 * 停止oa 任务
	 * @param id
	 * @return
	 */
	R disActivate(String id);
	/**
	 * 删除oa 任务，级联删除
	 * @param id
	 * @return
	 */
	R disOa(String id);

	/**
	 * 新增oa 任务
	 * @param oaImp
	 * @param oaColumns
	 * @return
	 */
	R saveOa(OaImpAndColumnDTO oaImpAndColumn);

	/**
	 * 修改oa 任务
	 * @param oaImp
	 * @param oaColumns
	 * @param fondsCode
	 * @return
	 */
	R updateOaById( OaImpAndColumnDTO oaImpAndColumn);

	/**
	 * 复制oa 任务
	 * @param oaImp
	 * @param fondsCode
	 * @return
	 */
	R copyOa(OaImportDTO oaImp, String id);

	void downloadOaColumnTemplate(HttpServletResponse response);

	/**
	 * 定时任务执行
	 * @param param
	 */
	void startImp(String param);
}
