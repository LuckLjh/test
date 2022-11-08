package com.cescloud.saas.archive.service.modular.archiveconfig.controller;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.MetadataBoxConfigDTO;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.MetadataBoxRuleDTO;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.MetadataBoxRuleUndefinedDTO;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.MetadataBoxConfigService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName MetadataBoxConfigConeroller
 * @Author zhangxuehu
 * @Date 2020/7/27 11:05
 **/
@Api(value = "metadataBoxConfig", tags = "档案门类管理-装盒规则定义：主表")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/box-config")
public class MetadataBoxConfigConeroller {

    private final MetadataBoxConfigService metadataBoxConfigService;

    @ApiOperation(value = "保存装盒规则定义", httpMethod = SwaggerConstants.POST)
    @SysLog("保存档案表单定义内容")
    @PostMapping
    public R saveBoxConfig(@RequestBody @ApiParam(name = "metadataBoxConfigDTO", value = "装盒定义实体", required = true) MetadataBoxConfigDTO metadataBoxConfigDTO) {
        return new R(metadataBoxConfigService.saveBoxConfig(metadataBoxConfigDTO));
    }

    @ApiOperation(value = "获取装盒规则定义", httpMethod = SwaggerConstants.GET)
    @GetMapping
    public R<MetadataBoxConfigDTO> getBoxConfig(@RequestParam("storageLocate") @NotBlank(message = "存储表名不能为空") String storageLocate,
                                                @RequestParam("moduleId") @NotNull(message = "模块id不能为空") Long moduleId) {
        return new R<MetadataBoxConfigDTO>(metadataBoxConfigService.getBoxConfig(storageLocate, moduleId));
    }

    @ApiOperation(value = "未定义的字段列表", httpMethod = SwaggerConstants.GET)
    @GetMapping("/list/undef")
    public R<List<MetadataBoxRuleUndefinedDTO>> listOfUnDefined(@ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
                                                                @RequestParam("moduleId") @NotNull(message = "模块id不能为空") Long moduleId) {
        return new R(metadataBoxConfigService.listOfUnDefined(storageLocate, moduleId));
    }

    @ApiOperation(value = "初始化表单页面", httpMethod = SwaggerConstants.GET)
    @GetMapping("/init")
    public R initForm(@RequestParam("typeCode") @ApiParam(name = "typeCode", value = "档案门类编码", required = true) @NotBlank(message = "档案门类编码不能为空") String typeCode,
                      @RequestParam("templateTableId") @ApiParam(name = "templateTableId", value = "模板ID", required = true) @NotNull(message = "模板ID不能为空") Long templateTableId,
                      @RequestParam("moduleId") @NotNull(message = "模块id不能为空") Long moduleId) throws ArchiveBusinessException {
        return new R(metadataBoxConfigService.initForm(typeCode, templateTableId, moduleId));
    }

    @ApiOperation(value = "获取装盒规则字段信息", httpMethod = SwaggerConstants.GET)
    @GetMapping("/field-info")
    public R<MetadataBoxRuleDTO> getBoxFieldInfo(@RequestParam("storageLocate") @ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "档案门类编码不能为空") String storageLocate,
                                                 @RequestParam("moduleId") @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId) throws ArchiveBusinessException {
        return new R(metadataBoxConfigService.getBoxFieldInfo(storageLocate, moduleId));
    }

	@ApiOperation(value = "获取装盒规则配置导出信息", httpMethod = SwaggerConstants.GET)
	@GetMapping("/data/{tenantId}")
	public R<List<ArrayList<String>>> getMetadataBoxConfigInfo(@PathVariable("tenantId") Long tenantId) throws ArchiveBusinessException {
		return new R(metadataBoxConfigService.getMetadataBoxConfigInfo(tenantId));
	}

    @SysLog("清除配置")
    @ApiOperation(value = "清除配置信息", httpMethod = SwaggerConstants.DELETE)
    @DeleteMapping("/remove-config")
    public R remove(@RequestParam("storageLocate") @ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
                    @RequestParam("moduleId") @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId) {
        return new R(metadataBoxConfigService.removeByModuleId(storageLocate, moduleId));
    }

}
