
package com.cescloud.saas.archive.service.modular.archiveconfig.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveLinkColumnRuleMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.LinkLayer;
import com.cescloud.saas.archive.api.modular.archivedict.dto.CopyPostDTO;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.LinkLayerService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.annotation.Inner;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;


/**
 * 挂接目录规则配置
 *
 * @author liudong1
 * @date 2019-05-14 10:33:56
 */
@Api(value = "linkDir", tags = "应用管理-档案门类管理:挂接目录规则配置")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/link-layer")
public class LinkLayerController {

	private final LinkLayerService linkLayerService;

	/**
	 * 挂接层级树，绑定到文件中心文件夹
	 *
	 * @param storageLocate
	 * @return
	 */
	@ApiOperation(value = "挂接层级树", httpMethod = SwaggerConstants.GET)
	@GetMapping("/tree")
	public R tree(@ApiParam(name = "storageLocate", value = "档案表名", required = true) @NotBlank(message = "档案表名不能为空") String storageLocate,
				  @ApiParam(name = "moduleId",value = "模块id",required = true) @NotNull(message = "模块id不能为空") Long moduleId) {
		return new R<>(linkLayerService.tree(storageLocate,moduleId));
	}

	@ApiOperation(value = "得到挂接根文件夹", httpMethod = SwaggerConstants.GET)
	@GetMapping("/root")
	public R<LinkLayer> getRoot(@ApiParam(name = "typeCode", value = "档案类型编码", required = true) @NotBlank(message = "档案类型编码不能为空") String typeCode,
					 @ApiParam(name = "templateTableId", value = "模板ID", required = true) @NotNull(message = "模板ID不能为空") Long templateTableId,
					 @ApiParam(name = "moduleId",value = "模块id",required = true) @NotNull(message = "模块id不能为空") Long moduleId) throws ArchiveBusinessException {
		return new R<>(linkLayerService.getRoot(typeCode, templateTableId,moduleId));
	}


	@ApiOperation(value = "得到挂接根文件夹(业务使用)", httpMethod = SwaggerConstants.GET)
	@GetMapping("/business/root")
	public R getBusinessRoot(@ApiParam(name = "typeCode", value = "档案类型编码", required = true) @NotBlank(message = "档案类型编码不能为空") String typeCode,
					 @ApiParam(name = "templateTableId", value = "模板ID", required = true) @NotNull(message = "模板ID不能为空") Long templateTableId,
					 @ApiParam(name = "moduleId",value = "模块id",required = true) @NotNull(message = "模块id不能为空") Long moduleId) throws ArchiveBusinessException {
		return new R<>(linkLayerService.getBusinessRoot(typeCode, templateTableId,moduleId));
	}


	@ApiIgnore
	@ApiOperation(value = "得到租户下所有的挂接根文件夹", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list/root/{tenantId}")
	@Inner
	public R<List<LinkLayer>> getRootsByTenantId(@PathVariable("tenantId") @ApiParam(name = "tenantId", value = "租户id", required = true) @NotNull(message = "租户id不能为空") Long tenantId) {
		return new R<List<LinkLayer>>(linkLayerService.getRootsByTenantId(tenantId));
	}

	@ApiOperation(value = "保存挂接规则配置根文件夹", notes = "parentId为空；绑定到文件中心文件夹", httpMethod = SwaggerConstants.POST)
	@SysLog("保存挂接规则配置根文件夹")
	@PostMapping("/root")
	public R<LinkLayer> saveRoot(@RequestBody @ApiParam(name = "linkLayer", value = "挂接目录对象", required = true) @Valid LinkLayer linkLayer)
			throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("保存挂接规则配置根文件夹-节点名称【%s】",linkLayer.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(linkLayerService.saveRoot(linkLayer));
	}

	@ApiOperation(value = "修改挂接规则配置根文件夹", notes = "parentId为空；绑定到文件中心文件夹", httpMethod = SwaggerConstants.PUT)
	@SysLog("修改挂接规则配置根文件夹")
	@PutMapping("/root")
	public R<LinkLayer> updateRoot(@RequestBody @ApiParam(name = "linkLayer", value = "挂接目录对象", required = true) LinkLayer linkLayer)
			throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("修改挂接规则配置根文件夹-节点名称【%s】",linkLayer.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(linkLayerService.saveRoot(linkLayer));
	}

	@ApiOperation(value = "保存挂接层次字段组成规则", httpMethod = SwaggerConstants.POST)
	@SysLog("保存挂接层次字段组成规则")
	@PostMapping("/dir")
	public R saveDir(@RequestBody @ApiParam(name = "saveLinkColumnRuleMetadata", value = "字段组成规则保存对象", required = true) @Valid SaveLinkColumnRuleMetadata saveLinkColumnRuleMetadata)
			throws ArchiveBusinessException {
		return linkLayerService.saveDirLinkColumnRule(saveLinkColumnRuleMetadata);
	}

	@ApiOperation(value = "修改挂接层次字段组成规则", httpMethod = SwaggerConstants.PUT)
	@SysLog("修改挂接层次字段组成规则")
	@PutMapping("/dir")
	public R updateDir(@RequestBody @ApiParam(name = "saveLinkColumnRuleMetadata", value = "字段组成规则保存对象", required = true) @Valid SaveLinkColumnRuleMetadata saveLinkColumnRuleMetadata)
			throws ArchiveBusinessException {
		return linkLayerService.saveDirLinkColumnRule(saveLinkColumnRuleMetadata);
	}

	@ApiOperation(value = "删除挂接层次", httpMethod = SwaggerConstants.DELETE)
	@SysLog("删除挂接层次")
	@DeleteMapping("/dir/{id}")
	public R deleteDir(@PathVariable("id") @ApiParam(name = "id", value = "文件夹ID", required = true) @NotNull(message = "文件夹id不能为空") Long id) {
		return linkLayerService.deleteDirLink(id);
	}

	@ApiOperation(value = "获取文件命名", httpMethod = SwaggerConstants.GET)
	@GetMapping("/file")
	public R getFile(@ApiParam(name = "storageLocate", value = "档案表名", required = true) @NotBlank(message="档案表名不能为空") String storageLocate,
					 @ApiParam(name = "moduleId",value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId) {
		return new R<>(linkLayerService.getFile(storageLocate,moduleId));
	}

	@ApiOperation(value = "获取文件下载命名设置", httpMethod = SwaggerConstants.GET)
	@GetMapping("/download/name-setting")
	public R getDocNameSetting(@ApiParam(name = "storageLocate", value = "档案表名", required = true) @NotBlank(message="档案表名不能为空") String storageLocate,
							   @ApiParam(name = "moduleId",value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId) {
		return new R<>(linkLayerService.getDoc(storageLocate,moduleId));
	}

	@ApiOperation(value = "获取批量挂接命名设置", httpMethod = SwaggerConstants.GET)
	@GetMapping("/batch-attach/tablename")
	public R getBatchAttachByStorageLocate(@ApiParam(name = "storageLocate", value = "档案表名", required = true) @NotBlank(message="档案表名不能为空") String storageLocate,
							   @ApiParam(name = "moduleId",value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId) {
		return new R<>(linkLayerService.getBatchAttachByTable(storageLocate,moduleId));
	}

	@ApiOperation(value = "获取批量挂接命名设置", httpMethod = SwaggerConstants.GET)
	@GetMapping("/batch-attach/typeCode")
	public R getBatchAttachByTypeCode(@ApiParam(name = "typeCode", value = "档案类型编码", required = true) @NotBlank(message="档案类型编码不能为空") String typeCode,
								   @ApiParam(name = "moduleId",value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId) {
		return new R<>(linkLayerService.getBatchAttachByTypeCode(typeCode,moduleId));
	}

	@ApiOperation(value = "获取批量挂接默认文件夹", httpMethod = SwaggerConstants.GET)
	@GetMapping("/batch-attach/dir")
	public R getBatchAttachDir() {
		return new R<>(linkLayerService.getBatchAttachDir());
	}

	@ApiOperation(value = "保存文件命名字段组成规则", httpMethod = SwaggerConstants.POST)
	@SysLog("保存文件命名字段组成规则")
	@PostMapping("/file")
	public R saveFile(@RequestBody @ApiParam(name = "saveLinkColumnRuleMetadata", value = "字段组成规则保存对象", required = true) @Valid SaveLinkColumnRuleMetadata saveLinkColumnRuleMetadata)
			throws ArchiveBusinessException {
		return linkLayerService.saveFileLinkColumnRule(saveLinkColumnRuleMetadata);
	}

	@ApiOperation(value = "保存文件下载命名设置规则", httpMethod = SwaggerConstants.POST)
	@SysLog("保存文件下载命名设置规则")
	@PostMapping("/download/name-setting")
	public R saveDocNameSetting(@RequestBody @ApiParam(name = "saveLinkColumnRuleMetadata", value = "字段组成规则保存对象", required = true) @Valid SaveLinkColumnRuleMetadata saveLinkColumnRuleMetadata)
			throws ArchiveBusinessException {
		return new R(linkLayerService.saveDocLinkColumnRule(saveLinkColumnRuleMetadata));
	}

	@ApiOperation(value = "保存批量挂接设置规则", httpMethod = SwaggerConstants.POST)
	@SysLog("保存批量挂接设置规则")
	@PostMapping("/batch-attach")
	public R saveBatchAttachSetting(@RequestBody @ApiParam(name = "saveLinkColumnRuleMetadata", value = "字段组成规则保存对象", required = true) @Valid SaveLinkColumnRuleMetadata saveLinkColumnRuleMetadata)
			throws ArchiveBusinessException {
		return new R(linkLayerService.saveBatchAttachLinkColumnRule(saveLinkColumnRuleMetadata));
	}

	@ApiOperation(value = "导航条", httpMethod = SwaggerConstants.GET)
	@GetMapping("/navigation/{id}")
	public R<Collection<LinkLayer>> navigation(@PathVariable @ApiParam(name = "id", value = "目录ID", required = true) @NotNull(message = "目录id不能为空") Long id) {
		return new R<>(linkLayerService.getParentList(id));
	}

	@ApiOperation(value = "下级目录", httpMethod = SwaggerConstants.GET)
	@GetMapping("/child/{id}")
	public R<LinkLayer> child(@PathVariable @ApiParam(name = "id", value = "目录ID", required = true) @NotNull(message = "目录id不能为空") Long id) {
		return new R<>(linkLayerService.getOne(Wrappers.<LinkLayer>query().lambda()
				.eq(LinkLayer::getParentId, id).last("limit 1")));
	}

	@SysLog("清除全文定义配置")
	@ApiOperation(value = "清除全文定义配置", httpMethod = SwaggerConstants.DELETE)
	@DeleteMapping("/remove-config")
	public R remove(@RequestParam("storageLocate")@ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
					@RequestParam("moduleId") @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId ){
		return new R(linkLayerService.removeByModuleId(storageLocate,moduleId));
	}

	@SysLog("复制到其他模块")
	@ApiOperation(value = "复制到其他模块接口", httpMethod = SwaggerConstants.POST)
	@PostMapping("/copy")
	public R copyToModule(@RequestBody @ApiParam(name = "copyPostDTO", value = "复制到其他模块参数DTO") @Valid CopyPostDTO copyPostDTO) {
		return linkLayerService.copy(copyPostDTO);
	}
}
