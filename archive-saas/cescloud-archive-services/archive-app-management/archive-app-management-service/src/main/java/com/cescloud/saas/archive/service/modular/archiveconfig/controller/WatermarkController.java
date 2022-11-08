
package com.cescloud.saas.archive.service.modular.archiveconfig.controller;

import cn.hutool.http.Header;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.WatermarkCopyDTO;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.WatermarkDTO;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.Watermark;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.WatermarkDetail;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.WatermarkService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;


/**
 * 水印设置
 *
 * @author qianjiang
 * @date 2019-05-14 10:33:56
 */
@Api(value = "Watermark", tags = "应用管理-档案门类管理:水印管理")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/watermark")
public class WatermarkController {

	private final WatermarkService watermarkService;


	@ApiOperation(value = "水印方案列表", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list")
	public R<List<Watermark>> getWatermarkList(@ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
											   @ApiParam(name = "keyword", value = "查询关键字", required = false)  String keyword) {
		return new R<>(watermarkService.listWatermark(storageLocate,keyword));
	}


	@ApiOperation(value = "获取默认配置", httpMethod = SwaggerConstants.GET)
	@GetMapping("/defaultWatermark")
	public R<Watermark> getDefaultWatermark(@ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
												  @ApiParam(name = "waterClassification", value = "水印分类", required = true) @NotNull(message = "水印分类不能为空") int waterClassification,
											   @ApiParam(name = "watermarkFormat", value = "水印格式", required = true) @NotBlank(message = "水印格式不能为空")  String watermarkFormat) {
		return new R<>(watermarkService.getDefaultWatermark(storageLocate,waterClassification,watermarkFormat));
	}

	@ApiOperation(value = "获取默认配置", httpMethod = SwaggerConstants.GET)
	@GetMapping("/defaultWatermarkDetail")
	public R<List<WatermarkDetail>> getDefaultWatermarkDetail(@ApiParam(name = "watermarkDTO", value = "水印配置信息", required = true)  WatermarkDTO watermarkDTO) {
		return new R<>(watermarkService.getDefaultWatermarkDetail(watermarkDTO));
	}

	@ApiOperation(value = "获取详细配置", httpMethod = SwaggerConstants.GET)
	@GetMapping("/watermarkDetail/{watermarkId}")
	public R<List<WatermarkDetail>> getWatermarkDetail(@PathVariable("watermarkId") @ApiParam(value = "传入水印方案主键id" ,name = "watermarkId") @NotNull(message = "Id不能为空") Long watermarkId) {
		return new R<>(watermarkService.getWatermarkDetail(watermarkId));
	}


	@ApiOperation(value = "根据ID获取水印管理方案", httpMethod = SwaggerConstants.GET)
	@GetMapping("/{id}")
	public R getById(@PathVariable("id") @ApiParam(value = "传入水印方案主键id" ,name = "id") @NotNull(message = "Id不能为空") Long id) {
		return new R<>(watermarkService.getWatermark(id));
	}


	@ApiOperation(value = "保存水印管理方案", httpMethod = SwaggerConstants.POST)
	@SysLog("保存水印管理方案")
	@PostMapping
	public R save(@RequestBody @ApiParam(name = "watermarkDTO", value = "水印方案保存对象", required = true) @Valid WatermarkDTO watermarkDTO)  {
		try {
			SysLogContextHolder.setLogTitle(String.format("保存水印管理方案-水印名称【%s】",watermarkDTO.getWatermarkName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return watermarkService.saveWatermark(watermarkDTO);
	}


	@ApiOperation(value = "通过id删除水印管理方案", httpMethod = SwaggerConstants.DELETE)
	@SysLog("通过id删除水印管理方案")
	@DeleteMapping("/{id}")
	public R removeById(@PathVariable @ApiParam(name = "id", value = "水印ID", required = true) @NotNull(message = "Id不能为空") Long id,
						@ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
						@ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId)  {
		try {
			WatermarkDTO watermark = watermarkService.getWatermark(id);
			SysLogContextHolder.setLogTitle(String.format("通过id删除水印管理方案-水印名称【%s】",watermark.getWatermarkName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return watermarkService.deleteWatermark(id,storageLocate,moduleId);
	}


	@ApiOperation(value = "取得水印字段列表下拉", httpMethod = SwaggerConstants.GET)
	@GetMapping("/metadata")
	public R<List> getMetadata(@ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
							   @ApiParam(name = "type", value = "类型", required = true) @NotNull(message = "类型不能为空") int type) {
		return new R(watermarkService.getMetadata(storageLocate,type));
	}


	@ApiOperation(value = "上传水印文件",httpMethod = SwaggerConstants.POST)
	@SysLog("上传水印文件")
	@PostMapping("/uploadfile")
	public R<Map<String,Object>> uploadFile(@RequestBody @ApiParam(name = "file", value = "上传水印文件", required = true) MultipartFile file) throws Exception {
		try {
			SysLogContextHolder.setLogTitle(String.format("上传水印文件-上传水印文件名称【%s】",file.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return watermarkService.uploadFile(file);
	}

	@ApiOperation(value = "复制水印管理方案" ,httpMethod = SwaggerConstants.POST)
	@SysLog("复制水印文件")
	@PostMapping("/copy")
	public R<String> copyToModule(@RequestBody @ApiParam(name = "watermarkCopyDTO", value = "水印赋值对象", required = true) @Valid WatermarkCopyDTO watermarkCopyDTO) {
		try {
			WatermarkDTO watermark = watermarkService.getWatermark(watermarkCopyDTO.getWatermarkId());
			SysLogContextHolder.setLogTitle(String.format("复制水印文件-复制水印文件名称【%s】",watermark.getWatermarkName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return watermarkService.copy(watermarkCopyDTO.getWatermarkId(),watermarkCopyDTO.getTargetModuleIds());
	}

	@ApiOperation(value = "读取背景图片")
	@GetMapping("/image/{fileId}")
	public void showImage(@PathVariable("fileId") Long fileId, HttpServletResponse response) {
		watermarkService.showImage(fileId,response);
	}


}
