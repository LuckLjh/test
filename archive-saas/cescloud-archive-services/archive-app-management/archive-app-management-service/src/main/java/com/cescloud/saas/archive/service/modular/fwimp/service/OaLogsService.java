package com.cescloud.saas.archive.service.modular.fwimp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.fwimp.dto.OaLogsDTO;
import com.cescloud.saas.archive.api.modular.fwimp.entity.OaLogs;

import javax.servlet.http.HttpServletResponse;

public interface OaLogsService extends IService<OaLogs> {
	IPage<OaLogs> getPage(Page page,Long ownerId, String status,String keyword);

	void getXmlFile(HttpServletResponse response, String ids);

	OaLogs getOaLogDetail(Long id);


}

