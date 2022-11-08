/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.service</p>
 * <p>文件名:WorkflowModelService.java</p>
 * <p>创建时间:2019年10月15日 下午5:16:19</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.service;

import com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowModelPurviewPostDTO;
import com.cesgroup.bpm.persistence.domain.BpmModelEntity;
import com.cesgroup.bpm.persistence.domain.BpmModelPurview;

import java.util.List;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年10月15日
 */
public interface WorkflowModelService {

    /**
     * 根据目录ID查询该目录下所有的模型
     *
     * @param tenantId
     *            租户ID
     * @param categoryId
     *            模型目录ID
     * @return
     */
    List<?> getBpmModelListByCatelogId(Long tenantId, Long categoryId,String type);

    /**
     * 根据模型ID获取模型目录信息
     *
     * @param id
     *            模型ID
     * @return BpmModelEntity
     */
    BpmModelEntity getBpmModelEntityById(Long id);

    /**
     * 保存模型目录
     *
     * @param modelEntity
     *            模型的对象
     * @param categoryId
     *            模型目录ID
     * @param tenantId
     *            租户ID
     * @return BpmModelEntity
     */
    BpmModelEntity saveBpmModelEntity(BpmModelEntity modelEntity, Long categoryId, Long tenantId);

    /**
     * 根据模型ID删除模型信息
     *
     * @param id
     *            模型ID
     * @return boolean
     */
    boolean removeBpmModelEntityById(Long id);

    /**
     * 启用
     *
     * @param id
     *            模型ID
     * @return boolean
     */
    boolean enableBpmModelEntityById(Long id);

    /**
     * 停用
     *
     * @param id
     *            模型ID
     * @return boolean
     */
    boolean disableBpmModelEntityById(Long id);

    /**
     * 流程可见范围设置
     *
     * @param workflowModelPurviewDTO
     *            可见范围设置信息
     * @param tenantId
     *            租户ID
     * @return
     */
    boolean purview(WorkflowModelPurviewPostDTO workflowModelPurviewDTO, String tenantId);

    /**
     * 获取流程可见范围集合
     *
     * @param bpmModelCode
     *            流程编码
     * @param tenantId
     *            租户ID
     * @return
     */
    List<BpmModelPurview> getBpmModelPurviewListByBpmModelCode(String bpmModelCode, String tenantId);

}
