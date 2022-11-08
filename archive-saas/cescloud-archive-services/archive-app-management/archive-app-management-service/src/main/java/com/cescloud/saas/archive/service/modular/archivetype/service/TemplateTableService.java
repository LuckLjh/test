/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.archivetype.service</p>
 * <p>文件名:TemplateTableService.java</p>
 * <p>创建时间:2020年2月17日 上午9:16:03</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.archivetype.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateTable;
import com.cescloud.saas.archive.common.constants.ArchiveLayerEnum;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年2月17日
 */
public interface TemplateTableService extends IService<TemplateTable> {

    /**
     * 根据templateTypeId删除表模板
     *
     * @param templateTypeId
     */
    void removeByTemplateTypeId(Long templateTypeId);

    /**
     *
     * 复制表模板
     *
     * @param copyId
     *            表模板ID
     * @param toTemplateTypeId
     */
    void copy(Long copyId, Long toTemplateTypeId,Map<String, Long> parentMap);



    /**
     *
     * 根据档案类型模板复制表模板
     *
     * @param copyTemplateTypeId
     *
     * @param toTemplateTypeId
     */
    void copyByTemplateTypeId(Long copyTemplateTypeId, Long toTemplateTypeId);

    /**
     * 根据档案类型模板获取表模板
     *
     * @param templateTypeId
     * @return
     */
    List<TemplateTable> getByTemplateTypeId(Long templateTypeId);

    /**
     * 根据层级自动生成表模板
     *
     * @param templateTypeId
     * @param layerEnums
     */
    void autoGenerateByLayers(Long templateTypeId, ArchiveLayerEnum[] layerEnums);

    /**
     * 获取档案-档案类型表模板
     *
     * @param tenantId
     *            租户id
     * @return
     */
    List<ArrayList<String>> getArchivesTypeTableTemplateInfor(Long tenantId);

    /**
     * 租户初始化 档案类型表模板
     *
     * @param templateId
     *            模板id
     * @param tenantId
     *            租户id
     * @return
     * @throws ArchiveBusinessException
     */
    R initialArchiveTypeTable(Long templateId, Long tenantId);

    /**
     * 取表模板父关系
     * key：表模板ID，value: 父模板对象
     *
     * @param id
     * @return
     */
    Map<Long, TemplateTable> getParentMapById(Long id);

    /**
     * 取表模板父关系集合
     *
     * @param id
     * @return
     */
    List<TemplateTable> getParentListById(Long id);

    /**
     * 获取表模板列表（分页）
     *
     * @param page
     * @param templateTypeId
     * @param keyword
     * @return
     */
    IPage<TemplateTable> page(IPage<TemplateTable> page, Long templateTypeId, String keyword);

    /**
     * 获取表模板列表
     *
     * @param templateTypeId
     * @param keyword
     * @return
     */
    List<TemplateTable> list(Long templateTypeId, String keyword);

    /**
     * 获取条目子模板集合（不包含过程信息、签名签章表）
     *
     * @param id
     * @return
     */
    List<TemplateTable> getEntryChildListById(Long id);

	/**
	 * 获取条目所有子模板集合（不包含全文、过程信息、签名签章表）
	 *
	 * @param id
	 * @return
	 */
	List<TemplateTable> getAllChildListById(Long id);

	/**
	 * 取表模板的子模板
	 * key：表模板ID，value: 模板ID
	 *
	 * @param id
	 * @return
	 */
	List<TemplateTable> getChildsById(Long id, List<TemplateTable> templateTables, List<TemplateTable> childList);

    /**
     * 表模板对应的父模板
     *
     * @param id
     * @return
     */
    TemplateTable getParentById(Long id);

	/**
	 * 根据档案类型 获取表模板信息
	 * @param archiveCode
	 * @return
	 */
	List<TemplateTable> getListByArchiveCode(String archiveCode);
}
