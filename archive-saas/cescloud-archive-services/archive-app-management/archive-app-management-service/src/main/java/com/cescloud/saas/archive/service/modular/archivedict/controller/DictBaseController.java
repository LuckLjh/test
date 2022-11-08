
package com.cescloud.saas.archive.service.modular.archivedict.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cescloud.saas.archive.api.modular.archivedict.entity.DictBase;
import com.cescloud.saas.archive.common.constants.BoolEnum;
import com.cescloud.saas.archive.service.modular.archivedict.service.DictBaseService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 基础数据字典
 *
 * @author liudong1
 * @date 2019-04-25 16:10:20
 */
@Api(value = "dictBase", tags = "基础数据字典项")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/dict-base")
public class DictBaseController {

	private final DictBaseService dictBaseService;

	/**
	 * 系统数据字典分类
	 *
	 * @return
	 */
	@ApiOperation(value = "获取系统数据字典分类", httpMethod = "GET")
	@GetMapping("/sys")
	public R<List<DictBase>> getDictBasePage() {
		return new R<>(dictBaseService.list(Wrappers.<DictBase>query().lambda()
				.eq(DictBase::getDictClass, BoolEnum.NO.getCode())));
	}

}
