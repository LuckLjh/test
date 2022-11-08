/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.controller</p>
 * <p>文件名:WorkflowVersionController.java</p>
 * <p>创建时间:2019年10月17日 下午2:10:50</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.controller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.UUID;
import com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowVersinDTO;
import com.cescloud.saas.archive.common.search.Page;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.core.util.SpringContextHolder;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.workflow.connector.AuthUserConnector;
import com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService;
import com.cescloud.saas.archive.service.modular.workflow.utils.WorkflowUtil;
import com.cesgroup.bpm.persistence.domain.BpmConfBase;
import com.cesgroup.bpm.persistence.domain.BpmModelEntity;
import com.cesgroup.bpm.persistence.manager.BpmModelManager;
import com.google.common.collect.Lists;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.repository.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年10月17日
 */
@Api(value = "工作流版本", tags = "工作流版本")
@RestController
@AllArgsConstructor
@RequestMapping("/workflow-version")
@Validated
@Slf4j
public class WorkflowVersionController {

    private final WorkflowVersionService workflowVersionService;

	@Autowired
	private BpmModelManager bpmModelManager;

    @ApiOperation(value = "获取工作流版本集合")
    @GetMapping("/list/{bpmModelId}")
    public R<?> list(@NotNull(message = "bpmModelId不能为空") @PathVariable("bpmModelId") Long bpmModelId) {
        return new R<>(workflowVersionService.getBpmConfBaseListByBpmModelId(bpmModelId));
    }

    @ApiOperation(value = "获取工作流版本集合（分页）")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "current", value = "当前页数", required = true, paramType = "int"),
        @ApiImplicitParam(name = "size", value = "每页数量，默认为10", paramType = "int"),
        @ApiImplicitParam(name = "bpmModelId", value = "模型ID", paramType = "long")
    })
    @GetMapping("/page")
    public R<?> page(Page<?> page, @NotNull(message = "bpmModelId不能为空") Long bpmModelId) {
        return new R<>(workflowVersionService.getBpmConfBasePageByBpmModelId(page, bpmModelId));
    }

    @ApiOperation(value = "根据ID获取工作流版本")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "工作流版本id", required = true, dataType = "long")
    })
    @GetMapping("/{id}")
    public R<?> getVersionById(@NotNull(message = "ID不能为空") @PathVariable("id") Long id) {
        return new R<>(workflowVersionService.getBpmConfBaseById(id));
    }

    @ApiOperation(value = "新增工作流版本")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "bpmModelId", value = "模型id", required = true, dataType = "long")
    })
    @SysLog("新增工作流版本")
    @PostMapping("/{bpmModelId}")
    public R<?> create(
        @Valid @RequestBody @ApiParam(name = "versionDTO", value = "工作流版本对象", required = true) WorkflowVersinDTO versionDTO,
        @NotNull(message = "bpmModelId不能为空") @PathVariable("bpmModelId") Long bpmModelId) {
		try {
			BpmModelEntity bpmModelEntity = bpmModelManager.get(bpmModelId);
			SysLogContextHolder.setLogTitle(String.format("新增工作流版本-工作流模型名称【%s】-用户自定义的流程版本号【%s】",bpmModelEntity.getName(),versionDTO.getVersion()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(
            workflowVersionService.saveBpmConfBase(WorkflowUtil.convert(new BpmConfBase(), versionDTO),
                bpmModelId));
    }

    @ApiOperation(value = "修改工作流版本")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "bpmModelId", value = "模型id", required = true, dataType = "long")
    })
    @SysLog("修改工作流版本")
    @PutMapping("/{bpmModelId}")
    public R<?> update(
        @Valid @RequestBody @ApiParam(name = "modelDTO", value = "工作流版本对象", required = true) WorkflowVersinDTO versionDTO,
        @NotNull(message = "bpmModelId不能为空") @PathVariable("bpmModelId") Long bpmModelId) {
		try {
			BpmModelEntity bpmModelEntity = bpmModelManager.get(bpmModelId);
			SysLogContextHolder.setLogTitle(String.format("修改工作流版本-工作流模型名称【%s】-用户自定义的流程版本号【%s】",bpmModelEntity.getName(),versionDTO.getVersion()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        return new R<>(
            workflowVersionService.saveBpmConfBase(WorkflowUtil.convert(new BpmConfBase(), versionDTO),
                bpmModelId));
    }

    @ApiOperation(value = "根据ID删除工作流版本")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "工作流版本id", required = true, dataType = "long")
    })
    @SysLog("根据ID删除工作流版本")
    @DeleteMapping("/{id}")
    public R<Boolean> remove(@NotNull(message = "ID不能为空") @PathVariable Long id) {
		try {
			BpmConfBase bpmConfBaseById = workflowVersionService.getBpmConfBaseById(id);
			SysLogContextHolder.setLogTitle(String.format("根据ID删除工作流版本-工作流模型名称【%s】-用户自定义的流程版本号【%s】",bpmConfBaseById.getName(),bpmConfBaseById.getVersion()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        return new R<>(workflowVersionService.removeBpmConfBaseById(id));
    }

	@ApiOperation(value = "检查是否有未完成的流程实例")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "工作流版本id", required = true, dataType = "long")
	})
	@GetMapping("has-unfinished/{id}")
	public R<Boolean> hasUnfinished(@NotNull(message = "ID不能为空") @PathVariable Long id) {
		return new R<>(workflowVersionService.hasUnfinishedProcessByID(id));
	}

    @ApiOperation(value = "激活流程版本")
    @SysLog("激活流程版本")
    @PostMapping("/active/{modelId}")
    public R<Boolean> activeProcessByBpmConfBaseId(
        @NotNull(message = "modelId不能为空") @PathVariable("modelId") String modelId) {
        return new R<>(workflowVersionService.activeProcessByModelId(modelId));
    }

    @ApiOperation(value = "挂起流程版本")
    @SysLog("挂起流程版本")
    @PostMapping("/suspend/{id}")
    public R<Boolean> suspendProcessByBpmConfBaseId(@NotNull(message = "ID不能为空") @PathVariable Long id) {
        return new R<>(workflowVersionService.suspendProcessByBpmConfBaseId(id));
    }

    @ApiOperation(value = "复制流程版本")
    @PostMapping("/copy/{id}")
    @SysLog("复制流程版本")
    public R<?> copyBpmConfBaseById(@NotNull(message = "ID不能为空") @PathVariable("id") Long id) {
        return new R<>(workflowVersionService.copyBpmConfBaseById(id));
    }

    @ApiOperation(value = "获取流程图")
    @GetMapping("/image/{modelId}")
    public R<?> getConfBaseImageByModelId(
        @NotBlank(message = "modelId不能为空") @PathVariable("modelId") String modelId) {
        return new R<>(workflowVersionService.getImageByModelId(modelId));
    }

    @ApiOperation(value = "获取流程图", hidden = true)
    @GetMapping("/image/{bpmModelId}/{version}")
    public R<?> getConfBaseImageByBpmModelIdAndVersion(
        @NotNull(message = "bpmModelId不能为空") @PathVariable("bpmModelId") Long bpmModelId,
        @NotBlank(message = "version不能为空") @PathVariable("version") String version) {
        return new R<>(workflowVersionService.getImageByBpmModelIdAndVersion(bpmModelId, version));
    }

    /**
     * 根据modelId打开流程图
     *
     * @param modelId
     *            工作流模型id
     * @return
     */
    @ApiOperation(value = "根据modelId打开流程图", hidden = true)
    @GetMapping("/model")
    public R<?> openModelByModelId(String modelId) {
        return new R<>(workflowVersionService.openModelByModelId(modelId));
    }

    /**
     * 保存流程图
     *
     * @param modelId
     *            工作流模型id
     * @param xmlJson
     *            流程图xml json
     * @return
     */
    @ApiOperation(value = "保存流程图", hidden = true)
    @PostMapping("/model")
    @SysLog("保存流程图")
    public R<?> saveModel(final String modelId, final String xmlJson) {
        return new R<>(workflowVersionService.saveModel(modelId, xmlJson));
    }

    /**
     * 导出流程图xml文件
     *
     * @param modelId
     *            工作流模型id
     * @param xmlJson
     *            流程图xml json
     * @param resp
     */
    @ApiOperation(value = "导出流程图xml文件", hidden = true)
    @PostMapping("/model/export")
    @SysLog("导出流程图xml文件")
    public void exportModelXmlFile(final String modelId, final String xmlJson, HttpServletResponse resp) {
        final Model model = workflowVersionService.getModelByModelId(modelId);
        final String fileName = model.getName() + ".bpmn20.xml";
        final String xmlContent = workflowVersionService.exportModelXmlFile(xmlJson);
        OutputStream out = null;
        try {
            out = resp.getOutputStream();
            resp.setContentType("text/xml; charset=utf-8");
            resp.setHeader("Content-Disposition", "attachment;fileName="
                + URLEncoder.encode(fileName, "utf-8").replaceAll("%28", "(").replaceAll("%29", ")"));

            out.write(xmlContent.getBytes("utf-8"));
            out.flush();

        } catch (final Exception e) {
            log.error("导出流程图xml文件", e);
        } finally {
            IoUtil.close(out);
        }
    }

    /**
     * 导入流程图xml文件
     *
     * @param modelId
     *            工作流模型id
     * @param file
     *            流程图xml文件
     * @return
     * @throws IllegalStateException
     * @throws IOException
     */
    @ApiOperation(value = "导入流程图xml文件", hidden = true)
    @PostMapping("/model/import")
    @SysLog("导入流程图xml文件")
    public R<?> importModelXmlFile(String modelId, @RequestParam(value = "file", required = true) MultipartFile file)
        throws IllegalStateException, IOException {
        final File xmlFile = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString(false) + ".xml");
        file.transferTo(xmlFile);
        return new R<>(workflowVersionService.importModelXmlFile(modelId, xmlFile));
    }

    /**
     * 获取事件监听器
     *
     * @param bpmModelCode
     *            流程编码
     * @return
     */
    @ApiOperation(value = "获取事件监听器", hidden = true)
    @GetMapping("/listener/events")
    public R<?> getEventListeners(String bpmModelCode) {
        final Long tenantId = SecurityUtils.getUser().getTenantId();
        return new R<>(workflowVersionService.getEventListenerList(tenantId, bpmModelCode));
    }

    /**
     * 获取执行监听器
     *
     * @param bpmModelCode
     *            流程编码
     * @return
     */
    @ApiOperation(value = "获取执行监听器", hidden = true)
    @GetMapping("/listener/executions")
    public R<?> getExecutionListeners(String bpmModelCode) {
        final Long tenantId = SecurityUtils.getUser().getTenantId();
        return new R<>(workflowVersionService.getExecutionListenerList(tenantId, bpmModelCode));
    }

    /**
     * 获取任务监听器
     *
     * @param bpmModelCode
     *            流程编码
     * @return
     */
    @ApiOperation(value = "获取任务监听器", hidden = true)
    @GetMapping("/listener/tasks")
    public R<?> getTaskListeners(String bpmModelCode) {
        final Long tenantId = SecurityUtils.getUser().getTenantId();
        return new R<>(workflowVersionService.getTaskListenerList(tenantId, bpmModelCode));
    }

    /**
     * 获取条件字段集合
     *
     * @param bpmModelCode
     *            流程编码
     * @return
     */
    @ApiOperation(value = "获取条件字段集合", hidden = true)
    @GetMapping("/condition/fields")
    public R<?> getConditionFields(String bpmModelCode) {
        final Long tenantId = SecurityUtils.getUser().getTenantId();
        return new R<>(workflowVersionService.getConditionFieldList(tenantId, bpmModelCode));
    }

    /**
     * 获取组织部门根节点集合
     *
     * @return
     */
    @ApiOperation(value = "获取组织部门根节点集合", hidden = true)
    @GetMapping("/assignment/dept-root")
    public R<?> getDeptRootList() {
        return new R<>(workflowVersionService.getDeptRootList());
    }

    /**
     * 获取组织部门根节点集合
     *
     * @return
     */
    @ApiOperation(value = "获取组织部门根节点集合", hidden = true)
    @GetMapping("/assignment/role-root")
    public R<?> getRoleRootList() {
        return new R<>(workflowVersionService.getRoleRootList());
    }

    /**
     * 获取子部门集合
     *
     * @return
     */
    @ApiOperation(value = "获取子部门集合", hidden = true)
    @GetMapping("/assignment/dept-node")
    public Object getDeptNodeList(Long id) {
        return workflowVersionService.getDeptNodeByParentId(id);
    }

    /**
     * 获取部门下用户集合
     *
     * @return
     */
    @ApiOperation(value = "获取部门下用户集合", hidden = true)
    @GetMapping("/assignment/users/dept/{deptId}")
    public Object getUserListByDeptId(@PathVariable Long deptId) {
        return workflowVersionService.getUserListByDeptId(deptId);
    }

    /**
     * 获取角色下用户集合
     *
     * @return
     */
    @ApiOperation(value = "获取角色下用户集合", hidden = true)
    @GetMapping("/assignment/users/role/{roleId}")
    public Object getUserListByRoleId(@PathVariable Long roleId) {
        return workflowVersionService.getUserListByRoleId(roleId);
    }

    /**
     * 获取组织级别
     *
     * @return [{id:"组织级别ID", name:"组织级别名称"}, ...]
     */
    @ApiOperation(value = "获取组织级别", hidden = true)
    @GetMapping("/assignment/tenant/org-levels")
    public Object getTenantOrgLevelList() {
        return Lists.newArrayList();
    }

    /**
     * 获取流程对应表单中部门字段集合
     *
     * @param bpmModelCode
     *            流程编码
     * @return [{code:"字段英文名称", name:"字段中文名称"}, ...]
     */
    @ApiOperation(value = "获取流程对应表单中部门字段集合", hidden = true)
    @GetMapping("/assignment/dept-columns/{bpmModelCode}")
    public Object getDeptColumnList(@PathVariable("bpmModelCode") String bpmModelCode) {
        final Long tenantId = SecurityUtils.getUser().getTenantId();
        return new R<>(workflowVersionService.getDeptFieldList(tenantId, bpmModelCode));
    }

    /**
     * 获取自定义表达式
     *
     * @param bpmModelCode
     *            流程编码
     *
     * @return [{code:"字段英文名称", name:"字段中文名称"}, ...]
     */
    @ApiOperation(value = "获取自定义表达式", hidden = true)
    @GetMapping("/assignment/custom-formulas/{bpmModelCode}")
    public Object getCustomFormulaList(@PathVariable("bpmModelCode") String bpmModelCode) {
        final Long tenantId = SecurityUtils.getUser().getTenantId();
        return workflowVersionService.getCustomFormulaList(tenantId, bpmModelCode);
    }

    @ApiOperation(value = "测试", hidden = true)
    @GetMapping("/test")
    public Object test() {
        //workflowVersionService.testTransaction();
        //return "true";
        //final CustomRuleService bean = SpringContextHolder.getBean(CustomRuleService.class);
        //return bean.candidateByUserId(null, "13",
        //    "{\"userType\":\"initiator\",\"userOrigin\":\"currentDept\",\"memberOrManager\":\"member\",\"checkMajorLine\":\"0\",\"selectUser\":\"(角色)租户管理员:3\",\"andOr\":\"||\",\"hiddenUserType\":\"initiator\"}");
        final AuthUserConnector bean = SpringContextHolder.getBean(AuthUserConnector.class);
        return bean.getUserByUserIdsForUserTaskCustomRule("12256", "verticalSuperior", null,
            Lists.newArrayList("305711"),
            "member");
    }
}
