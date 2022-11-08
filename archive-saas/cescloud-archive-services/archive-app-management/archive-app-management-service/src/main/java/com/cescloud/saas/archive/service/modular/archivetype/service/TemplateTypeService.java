/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.archivetype.service</p>
 * <p>文件名:TemplateTypeService.java</p>
 * <p>创建时间:2020年2月17日 上午9:14:03</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.archivetype.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateType;
import com.cescloud.saas.archive.common.tree.TreeNode;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年2月17日
 */
public interface TemplateTypeService extends IService<TemplateType> {

    /**
     * 复制档案门类模板
     *
     * @param copyId
     *            要复制的档案门类模板ID
     * @param entity
     *            复制后要生成的档案门类模板
     */
    void copy(Long copyId, TemplateType entity);

	/**
	 * 获取门类-档案类型模板信息
	 * @param tenantId 租户id
	 * @return
	 */
	List<ArrayList<String>> getArchivesTypeTemplateInfor(Long tenantId);

	/**
	 * 租户初始化 档案类型模板
	 *
	 * @param templateId 模板id
	 * @param tenantId   租户id
	 * @return
	 * @throws ArchiveBusinessException
	 */
	R initialArchiveType(Long templateId, Long tenantId) ;

    /**
     * 根据整理方式获取档案门类模板
     *
     * @param filingType
     * @return
     */
    List<TemplateType> getByFilingType(String filingType);

    /**
     * 档案门类模板树
     *
     * @param parentId
     * @param type
     * @return
     */
    List<TreeNode<Long>> getTreeDataList(Long parentId, String type);

    /**
     * 获取档案门类模板列表（分页）
     *
     * @param page
     * @param keyword
     * @return
     */
    IPage<TemplateType> page(IPage<TemplateType> page, String keyword);

    Integer getMaxSortNo();
}
