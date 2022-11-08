/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.archivetype.service</p>
 * <p>文件名:TemplateMetadataService.java</p>
 * <p>创建时间:2020年2月17日 上午9:16:39</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.archivetype.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateMetadata;
import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataBase;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qiucs
 * @version 1.0.0 2020年2月17日
 */
public interface TemplateMetadataService extends IService<TemplateMetadata> {

    /**
     * 插入基础层级字段
     *
     * @param templateTableId
     *            表模板ID
     * @param metadataBases
     *            层级基础字段集合
     */
    void insertLayerMetadatas(Long templateTableId, List<MetadataBase> metadataBases);

    /**
     * 模板字段复制
     *
     * @param copyIds
     * @param toTemplateTableId
     * @return
     */
    void copy(Long[] copyIds, Long toTemplateTableId);

    /**
     * 从表模板中复制
     *
     * @param copyTemplateTableId
     * @param toTemplateTableId
     * @return
     */
    void copyByTemplateTableId(Long copyTemplateTableId, Long toTemplateTableId);

    /**
     * 根据表模板ID获取字段集合
     *
     * @param templateTableId
     * @return
     */
    List<TemplateMetadata> getByTemplateTableId(Long templateTableId);

    /**
     * 根据表模板ID删除字段集合
     *
     * @param templateTableId
     * @return
     */
    boolean removeByTemplateTableId(Long templateTableId);

    /**
     * 获取档案类型模板表字段信息
     *
     * @param tenantId
     *            租户id
     * @return
     */
    List<ArrayList<String>> getArchivesTypeTableTemplateMetadataInfor(Long tenantId);

    /**
     * 获取模板字段列表（分页）
     *
     * @param page
     * @param templateTableId
     * @param keyword
     * @return
     */
    IPage<TemplateMetadata> page(IPage<TemplateMetadata> page, Long templateTableId, String keyword);

    /**
     * 获取模板字段列表
     *
     * @param templateTableId
     * @param keyword
     * @return
     */
    List<TemplateMetadata> list(Long templateTableId, String keyword);
/**
	 * 租户初始化 档案类型表字段
	 *
	 * @param templateId 模板id
	 * @param tenantId   租户id
	 * @return
	 * @throws ArchiveBusinessException
	 */
	R initialArchiveTypeTableField(Long templateId, Long tenantId) ;
}
