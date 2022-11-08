
package com.cescloud.saas.archive.service.modular.fonds.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.api.modular.filingscope.entity.FilingScopeType;
import com.cescloud.saas.archive.api.modular.fonds.dto.FondsDTO;
import com.cescloud.saas.archive.api.modular.fonds.entity.Fonds;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.annotation.Inner;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.common.security.tenantfilter.TenantContextHolder;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.fonds.service.FondsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;


/**
 * 全宗
 *
 * @author zhangpeng
 * @date 2019-03-21 12:04:54
 */
@Api(value = "fonds", tags = "全宗管理")
@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/fonds")
public class FondsController {

	private final FondsService fondsService;

	/**
	 * 分页查询
	 *
	 * @param fondsDTO 全宗分页对象
	 * @return
	 */

	@ApiOperation(value = "全宗的分页查询", httpMethod = "GET")
	@GetMapping("/page")
	public R<Page<Fonds>> getFondsPage(@ApiParam(value = "查询的实体", name = "fonds", required = false) FondsDTO fondsDTO) {
		return new R<>(fondsService.getPage(fondsDTO));
	}

	/**
	 * 全宗 分页查询
	 *
	 * @param fondsDTO 全宗查询实体
	 * @return
	 */
	@ApiOperation(value = "全宗的分页查询", httpMethod = "POST")
	@PostMapping("/page")
	public R<Page<Fonds>> getAuthFondsPage(@ApiParam(value = "查询的实体", name = "fonds", required = false)@RequestBody FondsDTO fondsDTO) {
		return new R<>(fondsService.getPage(fondsDTO));
	}

	/**
	 * 通过id查询全宗
	 *
	 * @param fondsId id
	 * @return R
	 */
	@ApiOperation(value = "查询全宗", httpMethod = "GET")
	@GetMapping("/{fondsId}")
	public R<Fonds> getById(@PathVariable("fondsId") @ApiParam(value = "传入全宗主键", name = "fondsId", required = true) @NotNull(message = "全宗id不能为空") Long fondsId) {
		return new R<>(fondsService.getFondsById(fondsId));
	}

	/**
	 * 通过name查询全宗
	 *
	 * @param fondsName id
	 * @return R
	 */
	@ApiOperation(value = "查询全宗", httpMethod = "GET")
	@GetMapping("/details/{fondsName}")
	public R<Fonds> getByName(@PathVariable("fondsName") @ApiParam(value = "传入全宗名主键", name = "fondsName", required = true) @NotNull(message = "全宗名不能为空") String fondsName) {
		Fonds fonds = fondsService.getFondsByName(fondsName);
		if (ObjectUtil.isNotNull(fonds)) {
			return new R<>(fonds);
		}else{
			return new R<>().fail(null,"全宗未找到！");
		}
	}

	/**
	 * 通过code查询全宗
	 * @param fondsCode
	 * @return R
	 */
	@ApiOperation(value = "查询全宗", httpMethod = "GET")
	@GetMapping("/code/{fondsCode}")
	public R<Fonds> getByCode(@PathVariable("fondsCode") String fondsCode) {
		Fonds fonds = fondsService.getFondsByCode(fondsCode);
		if (ObjectUtil.isNotNull(fonds)) {
			return new R<>(fonds);
		}else{
			return new R<>().fail(null,"全宗未找到！");
		}
	}

	/**
	 * 功能描述:
	 *
	 * @param
	 * @return
	 * @functionName
	 * @auther zhangpeng
	 * @DATE 2019-05-21 13:25
	 */
	@ApiOperation(value = "查询所有的全宗", httpMethod = "GET")
	@GetMapping("/list")
	public R<List<Fonds>> getFondsData() {
		return new R<>(fondsService.getFondsList());
	}


	/***
	 * 获取当前用户的全宗
	 * 该方法会走localcache
	 * @return
	 */
	@ApiOperation(value = "获取当前用户的全宗", httpMethod = "GET")
	@GetMapping("/current-user/list")
	public R<List<Fonds>> getCurrentUserFondsData() {
		return new R<>(fondsService.getFondsList(SecurityUtils.getUser().getId()));
	}

	/**
	 * 全宗数量是否达到上限
	 *
	 * @param
	 * @return R
	 */
	@ApiOperation(value = "判断是否可新增全宗", httpMethod = "GET")
	@GetMapping("/permit-add")
	public R permitAddFonds() {
		return fondsService.permitAddFonds();
	}

	/**
	 * 新增全宗
	 *
	 * @param fonds 全宗
	 * @return R
	 */
	@ApiOperation(value = "新增全宗", httpMethod = "POST")
	@SysLog("新增全宗")
	@PostMapping
	public R save(@RequestBody @ApiParam(value = "传入json格式", name = "全宗对象", required = true) @Valid Fonds fonds) {
		try {
			SysLogContextHolder.setLogTitle(String.format("新增全宗-全宗名称【%s】",fonds.getFondsName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return fondsService.saveFonds(fonds);
	}

	/**
	 * 修改全宗
	 *
	 * @param fonds 全宗
	 * @return R
	 */
	@ApiOperation(value = "修改全宗", httpMethod = "PUT")
	@SysLog("修改全宗")
	@PutMapping
	public R updateById(@RequestBody @ApiParam(value = "传入json格式", name = "更新全宗实体", required = true) @Valid Fonds fonds) {
		try {
			SysLogContextHolder.setLogTitle(String.format("修改全宗-全宗名称【%s】",fonds.getFondsName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return fondsService.updateFondsById(fonds);
	}

	/**
	 * 通过id删除全宗
	 *
	 * @param fondsId id
	 * @return R
	 */
	@ApiOperation(value = "删除全宗", httpMethod = "DELETE")
	@SysLog("删除全宗")
	@DeleteMapping("/{fondsId}")
	public R removeById(@PathVariable @ApiParam(value = "全宗主键id", name = "fondsId", required = true) @NotNull(message = "全宗id不能为空") Long fondsId) throws ArchiveBusinessException {
		try {
			Fonds fondsById = fondsService.getFondsById(fondsId);
			SysLogContextHolder.setLogTitle(String.format("删除全宗-全宗名称【%s】",fondsById.getFondsName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return fondsService.deleteFondsById(fondsId);
	}

	/***
	 *  导入全宗
	 * @param file iscover
	 */
	@ApiOperation(value = "导入全宗", httpMethod = "POST")
	@SysLog("导入全宗")
	@PostMapping("/import")
	public R importExcle(@RequestParam("file") @ApiParam(value = "excel文件", name = "file", required = true) MultipartFile file) throws IOException {
		try {
			SysLogContextHolder.setLogTitle(String.format("导入全宗-导入文件名【%s】",file.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return fondsService.insertExcel(file);
	}

	@ApiOperation(value = "导出全宗", httpMethod = "GET")
	@SysLog("导出全宗")
	@GetMapping("/export")
	public void exportExcel(HttpServletResponse response, String keyword) {
		fondsService.exportExcel(response, "全宗信息表", keyword);
	}

	@ApiOperation(value = "导出全宗模板", httpMethod = "GET")
	@SysLog("导出全宗模板")
	@GetMapping("/exportEmpty")
	public void downloadFondsExcel(HttpServletResponse response) {
		fondsService.downloadExcelTemplate(response, "全宗导入模板");
	}


    @ApiOperation(value = "查询所有租户的全宗总数", httpMethod = "GET")
    @Inner(false)
    @GetMapping("/total-count")
    public R<Integer> totalCount() {
        TenantContextHolder.setTenantId(null);
	    return new R<>(fondsService.count());
    }
	/**
	 * 权限管理  全宗设置 复制到其它全宗分页查询
	 *
	 * @return
	 */
	@ApiOperation(value = "复制到其它全宗分页查询")
	@GetMapping("/copy/page/{fondsCode}")
	public R<Page<Fonds>> getCopyFondsAuth(@ApiParam(name = "page", value = "分页对象") Page<Fonds> page,
	                                                 @ApiParam(value = "查询关键字", name = "keywords") String keyword,
	                                                 @PathVariable("fondsCode") String fondsCode) {
		return new R<>(fondsService.getCopyFondsAuth(page, keyword, fondsCode));
	}

	@ApiOperation(value = "判断全宗是否重复")
	@PostMapping("/check")
	public R<Boolean> checkFonds(@RequestParam(value = "fondsCode") String fondsCode, @RequestParam(value = "fondsName")  String fondsName) {
		return new R<>(fondsService.checkFonds(fondsCode, fondsName));
	}
}
