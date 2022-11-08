
package com.cescloud.saas.archive.service.modular.archiveconfig.controller;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedColumnRuleDTO;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedColumnRuleMetadata;

import com.cescloud.saas.archive.api.modular.archivetype.entity.MetadataAutovalue;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveColumnRuleService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * 档案字段组成规则
 *
 * @author liudong1
 * @date 2019-04-19 15:06:53
 */
@Api(value = "archiveColumnRule", tags = "档案门类管理-数据规则定义：档案字段组成规则")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/column-rule")
public class ArchiveColumnRuleController {

	private final ArchiveColumnRuleService archiveColumnRuleService;

	/**
	 * 已定义的字段列表
	 *
	 * @param metadataSourceId
	 * @return
	 */
	@ApiOperation(value = "已定义的字段列表", httpMethod = "GET")
	@GetMapping("/list/def")
	public R<List<DefinedColumnRuleMetadata>> getColumnRuleDefList(
			@ApiParam(name = "metadataSourceId", value = "被设置的元数据ID", required = true) Long metadataSourceId) {
		return new R<>(archiveColumnRuleService.listOfDefined(metadataSourceId));
	}

	@ApiOperation(value = "判断拼接规则字段中有无流水号字段并获取", httpMethod = "GET")
	@GetMapping("/include/flow-no")
	public R<MetadataAutovalue> getFlowNoColumn(@RequestParam("metadataSourceId") @ApiParam(name = "metadataSourceId", value = "被设置的元数据ID", required = true) Long metadataSourceId,
												@RequestParam("storageLocate") @ApiParam(name = "storageLocate", value = "被设置的元数据ID", required = true) String storageLocate,
												@RequestParam("moduleId") @ApiParam(name = "moduleId", value = "模块ID", required = true) Long moduleId) {
		final List<DefinedColumnRuleMetadata> definedColumnRuleMetadatas = archiveColumnRuleService.listOfDefined(metadataSourceId);
		return new R<>(archiveColumnRuleService.getFlowNoColumn(moduleId,storageLocate,definedColumnRuleMetadatas));
	}

	/**
	 * 未定义的字段列表
	 *
	 * @param storageLocate
	 * @param metadataSourceId
	 * @return
	 */
	@ApiOperation(value = "未定义的字段列表", httpMethod = "GET")
	@GetMapping("/list/undef")
	public R<DefinedColumnRuleDTO> getColumnRuleUnDefList(
			@ApiParam(name = "storageLocate", value = "存储表名", required = true)@NotBlank(message = "存储表名不能为空") String storageLocate,
			@ApiParam(name = "metadataSourceId", value = "被设置的元数据ID", required = true) Long metadataSourceId,
			@ApiParam(name = "metadataId", value = "元数据ID", required = true) Long metadataId,
			@ApiParam(name = "moduleId", value = "模块id", required = true)@NotNull(message = "模块id不能为空") Long moduleId) {
		return new R<>(archiveColumnRuleService.mapOfUnDefined(storageLocate, metadataSourceId,metadataId,moduleId));
	}


}
