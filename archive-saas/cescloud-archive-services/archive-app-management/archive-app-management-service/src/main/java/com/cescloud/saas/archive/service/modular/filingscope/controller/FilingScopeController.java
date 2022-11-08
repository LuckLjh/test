
package com.cescloud.saas.archive.service.modular.filingscope.controller;

import com.cescloud.saas.archive.api.modular.downloads.entity.CommonDownloads;
import com.cescloud.saas.archive.api.modular.filingscope.dto.*;
import com.cescloud.saas.archive.api.modular.filingscope.entity.FilingScope;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.common.util.ArchiveUtil;
import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.annotation.Inner;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.filingscope.service.FilingScopeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * 归档范围定义
 *
 * @author xieanzhu
 * @date 2019-04-22 15:45:22
 */
@Api(value = "filingScope", tags = "应用定义-归档范围定义")
@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/filing-scope")
public class FilingScopeController {

	private final FilingScopeService filingScopeService;
	private final ArchiveUtil archiveUtil;

	/**
	 * 通过id查询归档范围节点信息
	 *
	 * @param id id
	 * @return R
	 */
	@ApiOperation(value = "根据id查询归档范围节点信息", notes = "根节点：parentId为0", httpMethod = "GET")
	@GetMapping("/{id}")
	public R<FilingScopeDTO> getById(@PathVariable("id") @ApiParam(name = "id", value = "归档范围节点ID", required = true) @NotNull(message = "归档范围节点ID不能为空") Long id) throws ArchiveBusinessException {
		return new R<>(filingScopeService.getFilingScopeDTOById(id));
	}

	/**
	 * 通过id查询归档范围子节点列表
	 *
	 * @param id id
	 * @return R
	 */
	@ApiOperation(value = "根据id查询归档范围子节点列表", notes = "根节点：parentId为0", httpMethod = "GET")
	@GetMapping("/tree/{id}")
	public R<List<FilingScopeDTO>> getTreeById(@PathVariable("id") @ApiParam(name = "id", value = "归档范围节点ID", required = true) @NotNull(message = "归档范围节点ID") Long id,
											   @ApiParam(name = "fondsCodes", value = "全宗编码列表", required = false) @RequestParam(value = "fondsCodes", required = false) List<String> fondsCodes) throws ArchiveBusinessException {
		return new R<>(filingScopeService.findFilingScopeByParentClassId(id, fondsCodes));
	}

	/**
	 * 新增归档范围定义
	 *
	 * @param filingScopePostDTO 归档范围定义
	 * @return R
	 */
	@ApiOperation(value = "新增归档范围定义", httpMethod = "POST")
	@SysLog("新增归档范围定义")
	@PostMapping
	public R<FilingScopeDTO> save(@RequestBody @ApiParam(name = "归档范围对象", value = "传入json格式", required = true) @Valid FilingScopePostDTO filingScopePostDTO) throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("新增归档范围定义-归档范围所属的分类名称【%s】",filingScopePostDTO.getClassName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(filingScopeService.saveFilingScope(filingScopePostDTO));
	}

	/**
	 * 修改归档范围定义
	 *
	 * @param filingScopePutDTO 归档范围定义
	 * @return R
	 */
	@ApiOperation(value = "修改归档范围定义", httpMethod = "PUT")
	@SysLog("修改归档范围定义")
	@PutMapping
	public R<FilingScopeDTO> update(@RequestBody @ApiParam(name = "归档范围对象", value = "传入json格式", required = true) @Valid FilingScopePutDTO filingScopePutDTO) throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("修改归档范围定义-归档范围所属的分类名称【%s】",filingScopePutDTO.getClassName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(filingScopeService.updateFilingScope(filingScopePutDTO));
	}

	/**
	 * 通过id删除归档范围定义
	 *
	 * @param id id
	 * @return R
	 */
	@ApiOperation(value = "根据id删除归档范围定义", notes = "根节点：parentId为0", httpMethod = "DELETE")
	@SysLog("删除归档范围定义")
	@DeleteMapping("/{id}")
	public R removeById(@PathVariable @ApiParam(name = "id", value = "归档范围节点ID", required = true) @NotNull(message = "归档范围节点ID不能为空") Long id) throws ArchiveBusinessException {
		try {
			FilingScope byId = filingScopeService.getById(id);
			SysLogContextHolder.setLogTitle(String.format("删除归档范围定义-归档范围所属的分类名称【%s】",byId.getClassName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(filingScopeService.deleteById(id));
	}


	@ApiOperation("归档范围拖动排序")
	@PutMapping("/order")
	@SysLog("归档范围拖动排序")
	public R<Boolean> archiveTypeOrder(@RequestBody @ApiParam(name = "filingScopeOrderDTO", value = "归档范围排序", required = true) FilingScopeOrderDTO filingScopeOrderDTO) {
		return new R<>(filingScopeService.filingScopeOrder(filingScopeOrderDTO));
	}

	@SysLog("导出归档范围信息")
	@ApiOperation(value = "导出归档范围信息")
	@GetMapping("/export/{id}")
	public void exportExcel(@PathVariable("id") @ApiParam(name = "id", value = "归档范围节点ID", required = true) Long id, HttpServletResponse response) throws ArchiveBusinessException {
		filingScopeService.exportExcel(id, response);
	}

	@SysLog("导入归档范围信息")
	@ApiOperation(value = "导入归档范围信息")
	@PostMapping("/import/{id}")
	public R<Boolean> importExcel(@RequestParam("file") MultipartFile file, @PathVariable("id") Long id) throws ArchiveBusinessException {
		return new R<>(filingScopeService.importExcel(file, id));
	}

	@ApiOperation(value = "导出归档范围信息模板", httpMethod = "GET")
	@SysLog("导出归档范围信息模板")
	@GetMapping("/exportEmpty")
	public void downloadExcelTemplate(HttpServletResponse response) throws ArchiveBusinessException {
		filingScopeService.downloadExcelTemplate(response);
	}

	@ApiIgnore
	@ApiOperation(value = "归档范围初始化", httpMethod = "POST")
	@PostMapping("/initialize")
	@SysLog("归档范围初始化")
	public R initializeHandle(@RequestParam(value = "templateId", required = false) Long templateId, @RequestParam(value = "tenantId") Long tenantId) throws ArchiveBusinessException {
		return filingScopeService.initializeHandle(templateId, tenantId);
	}

	@ApiOperation(value = "获取租户归档范围树的信息", httpMethod = SwaggerConstants.GET, hidden = true)
	@GetMapping(value = "/data/{tenantId}")
	public R getFilingRangeInfo(@PathVariable("tenantId") Long tenantId) {
		return new R(filingScopeService.getFilingRangeTreeNodeInfo(tenantId));
	}

	@ApiOperation(value = "根据档案类型-分类号，查询归档范围")
	@GetMapping("/type-code-class-no/{typeCode}/{classNo}")
	public R<FilingScope> getFilingScopeByTypeCodeClassNo(@PathVariable("typeCode") String typeCode, @PathVariable("classNo") String classNo) {
		return new R<>(filingScopeService.getFilingScopeByTypeCodeClassNo(typeCode, classNo));
	}

	@ApiOperation(value = "根据分类号，查询归档范围")
	@RequestMapping("/inner/class-no/classNo")
	@Inner
	public R<FilingScope> getFilingScopeByClassNoInner(String classNo) {
		return new R<>(filingScopeService.getFilingScopeByClassNo(classNo));
	}

	@ApiOperation(value = "查询归档范围")
	@GetMapping("/query")
	public R<List<FilingScope>> getFilingScope(FilingScopeDTO filingScopeDTO) {
		return new R<>(filingScopeService.getFilingScope(filingScopeDTO));
	}

	/**
	 * 复制归档范围定义及对应的范围信息（注意仅仅针对全局）
	 *
	 * @param filingScopeCopyPostDTO 归档范围定义
	 * @return R
	 */
	@ApiOperation(value = "复制归档范围定义及对应的范围信息", httpMethod = "POST")
	@SysLog("复制归档范围定义及对应的范围信息")
	@PostMapping("/copy")
	public R<FilingScopeDTO> copy(@RequestBody @ApiParam(name = "归档范围对象", value = "传入json格式", required = true) @Valid FilingScopeCopyPostDTO filingScopeCopyPostDTO) throws ArchiveBusinessException {
		return new R<>(filingScopeService.copyFilingScope(filingScopeCopyPostDTO));
	}

	@ApiOperation(value = "获取所有的归档范围信息", httpMethod = "GET")
	@Inner
	@GetMapping("/list")
	public R<FilingScope> list(@RequestParam(value = "tenantId") Long tenantId) throws ArchiveBusinessException {
		Boolean isAddTenantId = archiveUtil.addTenantId(tenantId);
		List<FilingScope> list = filingScopeService.list();
		archiveUtil.clearTenantId(isAddTenantId);
		return new R(list);
	}


	@ApiOperation(value = "更新全宗名称的时候更新归档范围里根据全宗绑定的节点", httpMethod = SwaggerConstants.GET)
	@SysLog("更新归档范围里根据全宗绑定的节点")
	@GetMapping("/updateArchiveFilingScopeTree")
	public void updateArchiveFilingScopeTree(@RequestParam("fondsName") String fondsName, @RequestParam("fondsCode") String fondsCode) {
		filingScopeService.updateArchiveFilingScopeTree(fondsName, fondsCode);
	}
}
