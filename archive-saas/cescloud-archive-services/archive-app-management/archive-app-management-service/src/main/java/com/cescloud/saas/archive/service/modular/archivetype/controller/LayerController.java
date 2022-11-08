/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.archivetype.controller</p>
 * <p>文件名:LayerController.java</p>
 * <p>创建时间:2020年2月14日 下午12:02:36</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.archivetype.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.api.modular.archivetype.entity.Layer;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.archivetype.service.LayerService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年2月14日
 */
@Api(value = "layer", tags = "层级管理")
@Validated
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/layer")
public class LayerController {

    private final LayerService layerService;

    @ApiOperation(value = "新增层级", httpMethod = SwaggerConstants.POST)
    @SysLog("新增层级")
    @PostMapping
    public R<Layer> create(
        @RequestBody @ApiParam(value = "传入json格式", name = "层级对象", required = true) @Valid Layer layer) {
		try {
			SysLogContextHolder.setLogTitle(String.format("新增层级-层级编码【%s】-层级名称【%s】",layer.getCode(),layer.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        layerService.save(layer);
        return new R<Layer>(layer);
    }

    @ApiOperation(value = "修改层级", httpMethod = SwaggerConstants.PUT)
    @SysLog("修改层级")
    @PutMapping
    public R<Layer> update(
        @RequestBody @ApiParam(value = "传入json格式", name = "层级对象", required = true) @Valid Layer layer) {
		try {
			SysLogContextHolder.setLogTitle(String.format("修改层级-层级编码【%s】-层级名称【%s】",layer.getCode(),layer.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        layerService.updateById(layer);
        return new R<Layer>(layer);
    }

    @ApiOperation(value = "删除层级", httpMethod = SwaggerConstants.DELETE)
    @SysLog("删除层级")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable("id") Long id) {
		try {
			Layer byId = layerService.getById(id);
			SysLogContextHolder.setLogTitle(String.format("删除层级-层级编码【%s】-层级名称【%s】",byId.getCode(),byId.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        layerService.removeById(id);
        return new R<>(true);
    }

    @ApiOperation(value = "根据ID获取对象", httpMethod = SwaggerConstants.GET)
    @GetMapping("/{id}")
    public R<Layer> getById(@PathVariable("id") Long id) {
        return new R<>(layerService.getById(id));
    }

    @ApiOperation(value = "层级分页列表", httpMethod = SwaggerConstants.GET)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "current", value = "当前页数", required = true, paramType = "int"),
        @ApiImplicitParam(name = "size", value = "每页数量，默认为10", paramType = "int"),
        @ApiImplicitParam(name = "keyword", value = "层级名称或编码", paramType = "int")
    })
    @GetMapping("/page")
    public R<?> page(Page<Layer> page, String keyword) {
        return new R<>(layerService.page(page, keyword));
    }

    @ApiOperation(value = "层级列表", httpMethod = SwaggerConstants.GET)
    @GetMapping("/list")
    public R<?> list(
        @ApiParam(value = "keyword", name = "层级名称或编码") String keyword) {
        return new R<>(layerService.getLayerList(keyword));
    }

	@ApiOperation(value = "根据ids获取层级列表", httpMethod = SwaggerConstants.GET)
	@GetMapping
    public R<List<Layer>> getListByIds(@RequestParam("ids") @ApiParam(value = "ids", name = "id用逗号隔开") @NotBlank(message = "id不能为空") String ids){
    	return new R(layerService.listByIds(Arrays.asList(ids.split(","))));
	}

	@ApiOperation(value = "根据层级编码获取层级", httpMethod = SwaggerConstants.GET)
	@GetMapping("/code/{code}")
	public R<Layer> getByCode(@PathVariable("code") @ApiParam(value = "code", name = "层级编码") @NotBlank(message = "层级编码不能为空") String code){
		return new R(layerService.getOne(Wrappers.<Layer>lambdaQuery()
				.eq(Layer::getCode, code)));
	}

}
