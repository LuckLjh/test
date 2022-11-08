/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.service</p>
 * <p>文件名:WorkflowVersionService.java</p>
 * <p>创建时间:2019年10月15日 下午6:15:53</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.service;

import com.cescloud.saas.archive.common.search.Page;
import com.cesgroup.bpm.persistence.domain.BpmConfBase;
import org.activiti.engine.repository.Model;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年10月15日
 */
public interface WorkflowVersionService {

    /**
     * 根据模型ID获取版本集合
     *
     * @param bpmModelId
     *            模型ID
     * @return
     */
    List<BpmConfBase> getBpmConfBaseListByBpmModelId(Long bpmModelId);

    /**
     * 根据模型ID获取版本分页集合
     *
     * @param page
     *            分页信息
     * @param bpmModelId
     *            模型ID
     * @return
     */
    Page<?> getBpmConfBasePageByBpmModelId(Page<?> page, Long bpmModelId);

    /**
     * 根据模型ID获取模型信息
     *
     * @param id
     *            模型ID
     * @return BpmConfBase
     */
    BpmConfBase getBpmConfBaseById(Long id);

    /**
     * 保存模型版本
     *
     * @param bpmConfBase
     *            模型版本的对象
     * @param bpmModelId
     *            模型ID
     * @return BpmModelEntity
     */
    BpmConfBase saveBpmConfBase(BpmConfBase bpmConfBase, Long bpmModelId);

    /**
     * 根据ID删除模型版本信息
     *
     * @param id
     *            ID
     * @return boolean
     */
    boolean removeBpmConfBaseById(Long id);

	/**
	 * 检查是否有未完成的流程
	 *
	 * @param id
	 *            ID
	 * @return boolean
	 */
	boolean hasUnfinishedProcessByID(Long id);

    /**
     * 根据流程模型ID删除模型下所有版本信息
     *
     * @param bpmModelId
     *            模型ID
     * @return
     */
    boolean removeBpmConfBaseByBpmModelId(Long bpmModelId);

    /**
     * 复制模型版本信息
     *
     * @param id
     *            ID
     * @return BpmConfBase
     */
    BpmConfBase copyBpmConfBaseById(Long id);

    /**
     * 根据模型ID打开流程图
     *
     * @param modelId
     *            ID
     * @return boolean
     */
    Map<String, Object> openModelByModelId(String modelId);

    /**
     * 保存流程图
     *
     * @param modelId
     *            ID
     * @param xmlJson
     *            流程图json
     * @return
     */
    Model saveModel(String modelId, String xmlJson);

    /**
     * 导出流程图
     *
     * @param xmlJson
     *            流程图json
     * @return
     */
    String exportModelXmlFile(String xmlJson);

    /**
     * 根据模型ID获取流程图
     *
     * @param modelId
     *            ID
     * @return boolean
     */
    Model getModelByModelId(String modelId);

    /**
     * 导入流程图
     *
     * @param modelId
     *            模型ID
     * @param xmlFile
     *            流程图xml文件
     * @return
     */
    Model importModelXmlFile(String modelId, File xmlFile);

    /**
     * 激活流程
     *
     * @param bpmConfBaseId
     *            流程配置ID
     * @return
     */
    boolean activeProcessByBpmConfBaseId(Long bpmConfBaseId);

    /**
     * 激活流程
     *
     * @param modelId
     *            流程配置ID
     * @return
     */
    boolean activeProcessByModelId(String modelId);

    /**
     * 挂起流程
     *
     * @param bpmConfBaseId
     *            流程配置ID
     * @return
     */
    boolean suspendProcessByBpmConfBaseId(Long bpmConfBaseId);

    /**
     * 挂起流程
     *
     * @param bpmModelId
     *            流程模型ID
     * @return
     */
    boolean suspendProcessByBpmModelId(Long bpmModelId);

    /**
     * 获取组织部门根节点
     *
     * @return
     */
    List<Map<String, Object>> getDeptRootList();

    /**
     * 根据父节点ID获取组织部门节点
     *
     * @param parentId
     *            父节点ID
     * @return
     */
    List<Map<String, Object>> getDeptNodeByParentId(Long parentId);

    /**
     * 根据部门ID获取用户集合
     *
     * @param deptId
     *            部门ID
     * @return
     */
    List<Map<String, Object>> getUserListByDeptId(Long deptId);

    /**
     * 根据所有角色集合
     *
     * @return
     */
    List<Map<String, Object>> getRoleRootList();

    /**
     * 根据角色ID获取用户集合
     *
     * @param roleId
     *            角色ID
     * @return
     */
    List<Map<String, Object>> getUserListByRoleId(Long roleId);

    /**
     * 获取流程的自定义表达式
     *
     * @param tenantId
     *            租户ID
     * @param bpmModelCode
     *            流程编码
     * @return
     */
    List<Map<String, Object>> getCustomFormulaList(Long tenantId, String bpmModelCode);

    /**
     * 获取流程的执行监听器
     *
     * @param tenantId
     *            租户ID
     * @param bpmModelCode
     *            流程编码
     * @return
     */
    List<Map<String, Object>> getExecutionListenerList(Long tenantId, String bpmModelCode);

    /**
     * 获取流程的任务监听器
     *
     * @param tenantId
     *            租户ID
     * @param bpmModelCode
     *            流程编码
     * @return
     */
    List<Map<String, Object>> getTaskListenerList(Long tenantId, String bpmModelCode);

    /**
     * 获取流程的事件监听器
     *
     * @param tenantId
     *            租户ID
     * @param bpmModelCode
     *            流程编码
     * @return
     */
    List<Map<String, Object>> getEventListenerList(Long tenantId, String bpmModelCode);

    /**
     * 获取条件字段
     *
     * @param tenantId
     *            租户ID
     * @param bpmModelCode
     *            流程编码
     * @return
     */
    List<Map<String, Object>> getConditionFieldList(Long tenantId, String bpmModelCode);

    /**
     * 获取部门字段
     *
     * @param tenantId
     *            租户ID
     * @param bpmModelCode
     *            流程编码
     * @return
     */
    List<Map<String, Object>> getDeptFieldList(Long tenantId, String bpmModelCode);

    /**
     * 获取流程图
     *
     * @param processDefinitionId
     *            流程定义ID
     * @return
     */
    String getImageByProcessDefinitionId(String processDefinitionId);

    /**
     * 获取流程图
     *
     * @param bpmModelId
     *            模型ID
     * @param version
     *            版本号
     * @return
     */
    String getImageByBpmModelIdAndVersion(Long bpmModelId, String version);

    /**
     * 获取流程图
     *
     * @param modelId
     *            模型ID
     * @return
     */
    String getImageByModelId(String modelId);

}
