/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.controller</p>
 * <p>文件名:WorkflowModelController.java</p>
 * <p>创建时间:2019年10月15日 下午5:31:59</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.controller;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.cescloud.saas.archive.api.modular.synonymy.entity.Synonymy;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowModelDTO;
import com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowModelPurviewPostDTO;
import com.cescloud.saas.archive.service.modular.workflow.service.WorkflowModelService;
import com.cescloud.saas.archive.service.modular.workflow.utils.WorkflowUtil;
import com.cesgroup.bpm.persistence.domain.BpmModelEntity;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年10月15日
 */
@Api(value = "工作流模型", tags = "工作流模型")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/workflow-model")
@Validated
public class WorkflowModelController {

    private final WorkflowModelService workflowModelService;



    @ApiOperation(value = "获取模型集合")
    @GetMapping("/list/{categoryId}/{type}")
    public R<?> list(@NotNull(message = "categoryId不能为空") @PathVariable Long categoryId,@NotNull(message = "type不能为空") @PathVariable String type) {
        final Long tenantId = SecurityUtils.getUser().getTenantId();
        return new R<>(workflowModelService.getBpmModelListByCatelogId(tenantId, categoryId,type));
    }

    @ApiOperation(value = "根据ID获取模型目录")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "模型目录id", required = true, dataType = "long")
    })
    @GetMapping("/{id}")
    public R<?> getModelById(@NotNull(message = "ID不能为空") @PathVariable("id") Long id) {
        return new R<>(workflowModelService.getBpmModelEntityById(id));
    }

    @ApiOperation(value = "新增模型")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "categoryId", value = "模型目录id", required = true, dataType = "long")
    })
    @SysLog("新增模型")
    @PostMapping("/{categoryId}")
    public R<?> create(
        @Valid @RequestBody @ApiParam(name = "modelDTO", value = "模型对象", required = true) WorkflowModelDTO modelDTO,
        @NotNull(message = "categoryId不能为空") @PathVariable("categoryId") Long categoryId) {
		try {
			SysLogContextHolder.setLogTitle(String.format("新增模型-工作流模型名称【%s】",modelDTO.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        final Long tenantId = SecurityUtils.getUser().getTenantId();
        return new R<>(
            workflowModelService.saveBpmModelEntity(WorkflowUtil.convert(new BpmModelEntity(), modelDTO),
                categoryId, tenantId));
    }

    @ApiOperation(value = "修改模型")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "categoryId", value = "模型目录id", required = true, dataType = "long")
    })
    @SysLog("修改模型")
    @PutMapping("/{categoryId}")
    public R<?> update(
        @Valid @RequestBody @ApiParam(name = "modelDTO", value = "模型对象", required = true) WorkflowModelDTO modelDTO,
        @NotNull(message = "categoryId不能为空") @PathVariable("categoryId") Long categoryId) {
		try {
			SysLogContextHolder.setLogTitle(String.format("修改模型-工作流模型名称【%s】",modelDTO.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        final Long tenantId = SecurityUtils.getUser().getTenantId();
        return new R<>(
            workflowModelService.saveBpmModelEntity(WorkflowUtil.convert(new BpmModelEntity(), modelDTO),
                categoryId, tenantId));
    }

    @ApiOperation(value = "根据ID删除模型")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "模型id", required = true, dataType = "long")
    })
    @SysLog("根据ID删除模型")
    @DeleteMapping("/{id}")
    public R<Boolean> remove(@NotNull(message = "ID不能为空") @PathVariable Long id) {
		try {
			BpmModelEntity byId = workflowModelService.getBpmModelEntityById(id);
			SysLogContextHolder.setLogTitle(String.format("根据ID删除模型-工作流模型名称【%s】",byId.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        return new R<>(workflowModelService.removeBpmModelEntityById(id));
    }

    @ApiOperation(value = "启用模型")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "模型id", required = true, dataType = "long")
    })
    @SysLog("启用模型")
    @PostMapping("/{id}/enable")
    public R<?> enable(
        @NotNull(message = "id不能为空") @PathVariable("id") Long id) {
		try {
			BpmModelEntity byId = workflowModelService.getBpmModelEntityById(id);
			SysLogContextHolder.setLogTitle(String.format("启用模型-工作流模型名称【%s】",byId.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        return new R<>(workflowModelService.enableBpmModelEntityById(id));
    }

    @ApiOperation(value = "停用模型")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "模型id", required = true, dataType = "long")
    })
    @SysLog("停用模型")
    @PostMapping("/{id}/disable")
    public R<?> disable(
        @NotNull(message = "id不能为空") @PathVariable("id") Long id) {
		try {
			BpmModelEntity byId = workflowModelService.getBpmModelEntityById(id);
			SysLogContextHolder.setLogTitle(String.format("停用模型-工作流模型名称【%s】",byId.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        return new R<>(workflowModelService.disableBpmModelEntityById(id));
    }

    @ApiOperation(value = "模型可见范围设置")
    @SysLog("模型可见范围设置")
    @PostMapping("/purview")
    public R<?> purview(@Valid @RequestBody WorkflowModelPurviewPostDTO workflowModelPurviewDTO) {
        final Long tenantId = SecurityUtils.getUser().getTenantId();
        return new R<>(workflowModelService.purview(workflowModelPurviewDTO, tenantId.toString()));
    }

    @ApiOperation(value = "获取模型可见范围设置")
    @GetMapping("/purview/{bpmModelCode}")
    public R<?> getpurview(@NotBlank(message = "bpmModelCode不能为空") @PathVariable("bpmModelCode") String bpmModelCode) {
        final Long tenantId = SecurityUtils.getUser().getTenantId();
        return new R<>(workflowModelService.getBpmModelPurviewListByBpmModelCode(bpmModelCode, tenantId.toString()));
    }

}
