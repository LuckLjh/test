package com.cescloud.saas.archive.service.modular.fwimp.controller;

import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.annotation.Inner;
import com.cescloud.saas.archive.service.modular.fwimp.service.OaImportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用于测试REST风格调用的demo
 *
 * @author hyq
 * @date 2021-05-08
 */
@Slf4j
@RestController
@RequestMapping("/innerOaImpjob")
public class RestOaImpInner{
	@Autowired
	private OaImportService oaImportService;

	/**
	 * oa 导入定时接口
	 */
	@Inner(value = false)
	@GetMapping("/startImp/{param}")
	public R demoMethod(@PathVariable("param") String param) {
		oaImportService.startImp(param);
		return new R<>().success(null, "发送oa 导入定时任务完成！");
	}
}
