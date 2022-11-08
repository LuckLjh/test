/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.controller</p>
 * <p>文件名:WorkflowMonitorController.java</p>
 * <p>创建时间:2019年11月13日 下午3:20:45</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowSearchDTO;
import com.cescloud.saas.archive.common.search.Page;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.workflow.service.WorkflowMonitorService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;

/**
 * 流程监控
 *
 * @author qiucs
 * @version 1.0.0 2019年11月13日
 */
@Api(value = "流程监控", tags = "流程监控")
@RestController
@AllArgsConstructor
@RequestMapping("/workflow-monitor")
@Validated
public class WorkflowMonitorController {

    private final WorkflowMonitorService workflowMonitorService;

    @ApiOperation(value = "获取租户的业务模型")
    @GetMapping("/list/business-model/tenant")
    public R<?> getTenantBusinessModelList() {
        final Long tenantId = SecurityUtils.getUser().getTenantId();
        return new R<>(workflowMonitorService.getTenantBusinessModelList(tenantId));
    }

    @ApiOperation(value = "获取租户的流程列表（分页）")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "current", value = "当前页数", required = true, paramType = "int"),
        @ApiImplicitParam(name = "size", value = "每页数量，默认为10", paramType = "int"),
        @ApiImplicitParam(name = "status", value = "状态：active-审核中，complete-审核完成", required = true, paramType = "string")
    })
    @GetMapping("/page/process/tenant")
    public R<?> getTenantProcessInstancePage(Page<?> page, WorkflowSearchDTO searchDTO) {
        final Long tenantId = SecurityUtils.getUser().getTenantId();
        return new R<>(workflowMonitorService.getTenantProcessInstancePageByTenantId(page, tenantId, searchDTO));
    }
}
