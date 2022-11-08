/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.controller</p>
 * <p>文件名:WorkflowCategoryController.java</p>
 * <p>创建时间:2019年10月15日 下午3:28:49</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.controller;

import javax.validation.Valid;
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

import com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowCategoryDTO;
import com.cescloud.saas.archive.service.modular.workflow.service.WorkflowCategoryService;
import com.cescloud.saas.archive.service.modular.workflow.utils.WorkflowUtil;
import com.cesgroup.bpm.persistence.domain.BpmModelCategory;
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
 * 工作流模型
 *
 * @author qiucs
 * @version 1.0.0 2019年10月15日
 */
@Api(value = "工作流模型目录", tags = "工作流模型目录")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/workflow-category")
@Validated
public class WorkflowCategoryController {

    private final WorkflowCategoryService workflowCategoryService;

    @ApiOperation(value = "获取模型目录集合")
    @GetMapping("/list")
    public R<?> categorites() {
        final Long tenantId = SecurityUtils.getUser().getTenantId();
        return new R<>(workflowCategoryService.getBmpModelCategoryListByTenantId(tenantId));
    }

    @ApiOperation(value = "根据ID获取模型目录")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "模型目录id", required = true, dataType = "long")
    })
    @GetMapping("/{id}")
    public R<?> getCategoryById(@NotNull(message = "ID不能为空") @PathVariable("id") Long id) {
        return new R<>(workflowCategoryService.getBmpModelCategoryById(id));
    }

    @ApiOperation(value = "新增模型目录")
    @SysLog("新增模型目录")
    @PostMapping
    public R<?> create(
        @Valid @RequestBody @ApiParam(name = "categoryDTO", value = "模型目录对象", required = true) WorkflowCategoryDTO categoryDTO) {
		try {
			SysLogContextHolder.setLogTitle(String.format("新增模型目录-模型目录名称【%s】",categoryDTO.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        final Long tenantId = SecurityUtils.getUser().getTenantId();
        return new R<>(
            workflowCategoryService.saveBmpModelCategory(WorkflowUtil.convert(new BpmModelCategory(), categoryDTO), tenantId));
    }

    @ApiOperation(value = "修改模型目录")
    @SysLog("修改模型目录")
    @PutMapping
    public R<?> update(
        @Valid @RequestBody @ApiParam(name = "categoryDTO", value = "模型目录对象", required = true) WorkflowCategoryDTO categoryDTO) {
		try {
			SysLogContextHolder.setLogTitle(String.format("修改模型目录-模型目录名称【%s】",categoryDTO.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        final Long tenantId = SecurityUtils.getUser().getTenantId();
        return new R<>(
            workflowCategoryService.saveBmpModelCategory(WorkflowUtil.convert(new BpmModelCategory(), categoryDTO), tenantId));
    }

    @ApiOperation(value = "根据ID删除模型目录")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "模型目录id", required = true, dataType = "long")
    })
    @SysLog("根据ID删除模型目录")
    @DeleteMapping("/{id}")
    public R<Boolean> remove(@NotNull(message = "ID不能为空") @PathVariable Long id) {
		try {
			BpmModelCategory bmpModelCategoryById = workflowCategoryService.getBmpModelCategoryById(id);
			SysLogContextHolder.setLogTitle(String.format("根据ID删除模型目录-模型目录名称【%s】",bmpModelCategoryById.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        return new R<>(workflowCategoryService.removeBmpModelCategoryById(id));
    }
}
