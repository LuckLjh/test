
package com.cescloud.saas.archive.service.modular.archiveconfig.controller;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.*;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveEditForm;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveEditFormService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
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

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;


/**
 * 档案表单定义内容
 *
 * @author liudong1
 * @date 2019-04-22 19:56:41
 */
@Api(value = "archiveEditForm", tags = "应用管理-档案门类管理:档案表单定义内容")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/edit-form")
public class ArchiveEditFormController {

    private final ArchiveEditFormService archiveEditFormService;

    private final ArchiveTableService archiveTableService;

    /**
     * 通过storageLocate查询档案表单定义内容
     *
     * @param storageLocate
     * @return R
     */
    @ApiOperation(value = "查询档案表单定义内容", httpMethod = SwaggerConstants.GET)
    @GetMapping
    public R getByStorageLocate(@RequestParam("storageLocate") @ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
                                @RequestParam("moduleId") @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId) throws ArchiveBusinessException {
        ArchiveEditForm editForm = archiveEditFormService.getEditFormByStorageLocate(storageLocate, moduleId);
        return new R<>(editForm);
    }

    @ApiOperation(value = "查询档案表单定义的文本框字段", httpMethod = SwaggerConstants.GET)
    @GetMapping("/column")
    public R<List<String>> getEditFormColumn(@ApiParam(name = "typeCode", value = "档案门类编码", required = true) @NotBlank(message = "档案门类编码不能为空") String typeCode,
                                             @ApiParam(name = "templateTableId", value = "档案层级", required = true) @NotNull(message = "模板ID不能为空") Long templateTableId,
                                             @RequestParam("moduleId") @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId) {
        ArchiveTable archiveTable = archiveTableService.getTableByArchiveTypeCodeAndTemplateTableId(typeCode, templateTableId);
        return new R<>(archiveEditFormService.getEditFormColumnByStorageLocate(archiveTable.getStorageLocate(), moduleId));
    }

    /**
     * 保存档案表单定义内容
     *
     * @param archiveEditForm 档案表单定义内容
     * @return R
     */
    @ApiOperation(value = "保存档案表单定义内容", httpMethod = SwaggerConstants.POST)
    @SysLog("保存档案表单定义内容")
    @PostMapping
    public R<ArchiveEditForm> save(@RequestBody @ApiParam(name = "archiveEditForm", value = "表单定义内容实体", required = true) @Valid ArchiveEditForm archiveEditForm) throws ArchiveBusinessException {
        return new R<>(archiveEditFormService.saveEditForm(archiveEditForm));
    }

    /**
     * 初始化表单页面
     *
     * @param typeCode
     * @param templateTableId
     * @return
     * @throws ArchiveBusinessException
     */
    @ApiOperation(value = "初始化表单页面", httpMethod = SwaggerConstants.GET)
    @GetMapping("/init")
    public R<ArchiveEditForm> initForm(@RequestParam("typeCode") @ApiParam(name = "typeCode", value = "档案门类编码", required = true) @NotBlank(message = "档案门类编码不能为空") String typeCode,
                                       @RequestParam("templateTableId") @ApiParam(name = "templateTableId", value = "模板ID", required = true) @NotNull(message = "模板ID不能为空") Long templateTableId,
                                       @RequestParam("moduleId") @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId) throws ArchiveBusinessException {
        return new R<>(archiveEditFormService.initForm(typeCode,templateTableId, moduleId));
    }


    /**
     * @Description: 查询拼接规则给前端处理
     * @return: com.cescloud.saas.archive.service.modular.common.core.util.R<java.util.List < java.lang.String>>
     **/
    @ApiOperation(value = "查询档案表单定义的数据规则", httpMethod = SwaggerConstants.GET)
    @GetMapping("/column/autovalue-rule")
    public R<AutovalueRuleDTO> getRuleColumn(@ApiParam(name = "typeCode", value = "档案门类编码", required = true) @NotBlank(message = "档案门类编码不能为空") String typeCode,
                                             @ApiParam(name = "templateTableId", value = "模板ID", required = true) @NotNull(message = "模板ID不能为空") Long templateTableId,
                                             @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId,
                                             @RequestParam(value = "compose", required = false) @ApiParam(name = "compose", value = "是否组卷，0：未组卷", required = false) Integer compose,
                                             @RequestParam(value = "folderTemplateId", required = false) @ApiParam(name = "folderTemplateId", value = "案卷表模板id", required = false) Long folderTemplateId) throws ArchiveBusinessException {
        ArchiveTable archiveTable = archiveTableService.getTableByArchiveTypeCodeAndTemplateTableId(typeCode, templateTableId);
        return new R<>(archiveEditFormService.getRuleColumn(archiveTable.getStorageLocate(), moduleId, compose));
    }


    /**
     * @param archiveInitFormDataDTO 初始化表单实体
     * @return
     * @throws ArchiveBusinessException
     */
    @ApiOperation(value = "获取表单初始化数据", httpMethod = SwaggerConstants.GET)
    @GetMapping("/init/data")
    public R initFormData(@ApiParam(name = "archiveInitFromDataDTO", value = "初始化表单实体", required = true) @Valid ArchiveInitFormDataDTO archiveInitFormDataDTO) throws ArchiveBusinessException {
        return new R(archiveEditFormService.initFormData(archiveInitFormDataDTO));
    }

    @ApiOperation(value = "修改分组字段触发重新计算流水号值", httpMethod = SwaggerConstants.POST)
    @PostMapping("/computation/flowno")
    public R<Map<String, Object>> getFlownoValue(@RequestBody @ApiParam(name = "computeFlowNoDTO", value = "计算流水号实体", required = true) @Valid ComputeFlowNoDTO computeFlowNoDTO) throws ArchiveBusinessException {
        return new R<>(archiveEditFormService.getFlownoValue(computeFlowNoDTO));
    }

	@ApiOperation(value = "获取最大页号", httpMethod = SwaggerConstants.POST)
	@GetMapping("/computation/pageno/{typeCode}/{templateTableId}/{ownerId}")
	public R<Map<String, Object>> getPageNo(@PathVariable("typeCode") String typeCode,
											@PathVariable("templateTableId") Long templateTableId,
											@PathVariable("ownerId") Long ownerId) throws ArchiveBusinessException {
		return new R<>(archiveEditFormService.getMaxPageNoByOwnerId(typeCode,templateTableId,ownerId));
	}

    @ApiOperation(value = "获取档案门类表单定义信息", httpMethod = SwaggerConstants.GET)
    @GetMapping(value = "/data/{tenantId}")
    @SysLog("获取档案门类表单定义信息")
    public R getFormDefinitionInfo(@PathVariable("tenantId") Long tenantId) throws ArchiveBusinessException {
        return new R(archiveEditFormService.getFormDefinitionInfo(tenantId));
    }

	@ApiOperation(value = "根据表单字段获取下拉树", httpMethod = "GET")
	@GetMapping("/tree/{treeType}")
	public R<List<ArchiveTreeResultDTO>> getTreeByTreeType(@PathVariable("treeType") @ApiParam(name = "id", value = "下拉树类型", required = true) @NotNull(message = "下拉树类型不能为空") Integer treeType,
	                                                       @Valid ArchiveTreeQueryDTO archiveTreeQueryDTO) throws ArchiveBusinessException {
		return new R<>(archiveEditFormService.getTreeByTreeType(treeType,archiveTreeQueryDTO));
	}
}
