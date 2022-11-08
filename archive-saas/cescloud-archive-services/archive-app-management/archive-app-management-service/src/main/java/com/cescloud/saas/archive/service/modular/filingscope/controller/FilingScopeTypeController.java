package com.cescloud.saas.archive.service.modular.filingscope.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.api.modular.filingscope.dto.FilingScopeOrderDTO;
import com.cescloud.saas.archive.api.modular.filingscope.dto.FilingScopeTypePostDTO;
import com.cescloud.saas.archive.api.modular.filingscope.dto.FilingScopeTypePutDTO;
import com.cescloud.saas.archive.api.modular.filingscope.entity.FilingScope;
import com.cescloud.saas.archive.api.modular.filingscope.entity.FilingScopeType;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.filingscope.service.FilingScopeTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @ClassName FilingScopeTypeController
 * @Author zhangxuehu
 * @Date 2020/6/29 14:15
 **/
@Api(value = "filingScopeType", tags = "应用定义-归档范围信息")
@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/filing-scope-type")
public class FilingScopeTypeController {

    private final FilingScopeTypeService filingScopeTypeService;

    @ApiOperation(value = "新增归档范围信息", httpMethod = "POST")
    @SysLog("新增归档范围信息")
    @PostMapping
    public R save(@RequestBody @ApiParam(name = "归档范围对象", value = "传入json格式", required = true) @Valid FilingScopeTypePostDTO filingScopeTypePostDTO) throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("新增归档范围信息-归档范围【%s】",filingScopeTypePostDTO.getFilingScope()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(filingScopeTypeService.saveFilingScopeType(filingScopeTypePostDTO));
    }

    @ApiOperation(value = "修改归档范围定义", httpMethod = "PUT")
    @SysLog("修改归档范围定义")
    @PutMapping
    public R update(@RequestBody @ApiParam(name = "归档范围对象", value = "传入json格式", required = true) @Valid FilingScopeTypePutDTO filingScopeTypePutDTO) throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("修改归档范围定义-归档范围【%s】",filingScopeTypePutDTO.getFilingScope()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(filingScopeTypeService.updateFilingScopeType(filingScopeTypePutDTO));
    }

    @ApiOperation(value = "根据id删除归档范围定义", httpMethod = "DELETE")
    @SysLog("删除归档范围定义")
    @DeleteMapping("/{id}")
    public R removeById(@PathVariable @ApiParam(name = "id", value = "归档范围信息ID", required = true) @NotNull(message = "归档范围信息ID不能为空") Long id) throws ArchiveBusinessException {
		try {
			FilingScopeType byId = filingScopeTypeService.getById(id);
			SysLogContextHolder.setLogTitle(String.format("删除归档范围定义-归档范围【%s】",byId.getFilingScope()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(filingScopeTypeService.deleteById(id));
    }

    @ApiOperation(value = "根据父级id查询归档范围定义列表", httpMethod = "GET")
    @GetMapping("/page")
    public R getTreeById(@ApiParam(name = "page", value = "分页条件") Page page, @ApiParam(name = "id", value = "父级id") String id, @ApiParam(name = "keyword", value = "模糊查询条件") String keyword) {
        return new R(filingScopeTypeService.findFilingScopeTypeByParentId(page, id, keyword));
    }

    @ApiOperation(value = "获取归档范围信息", httpMethod = "GET")
    @GetMapping("/{id}")
    public R<FilingScopeType> getById(@ApiParam(name = "id", value = "id") @PathVariable Long id) {
        return new R<FilingScopeType>(filingScopeTypeService.getById(id));
    }

    @ApiOperation(value = "获取租户归档范围树的信息", httpMethod = SwaggerConstants.GET,hidden = true)
    @GetMapping(value = "/data/{tenantId}")
    @SysLog("获取租户归档范围树的信息")
    public R getFilingScopeTypeInfo(@PathVariable("tenantId") Long tenantId){
        return new R(filingScopeTypeService.getFilingScopeTypeInfo(tenantId));
    }

	@ApiOperation("归档范围信息拖动排序")
	@PutMapping("/order")
	@SysLog("归档范围信息拖动排序")
	public R<Boolean> filingScopeTypeOrder(@RequestBody @ApiParam(name = "filingScopeOrderDTO", value = "归档范围排序", required = true) FilingScopeOrderDTO filingScopeOrderDTO) {
		return new R<>(filingScopeTypeService.filingScopeTypeOrder(filingScopeOrderDTO));
	}
}
