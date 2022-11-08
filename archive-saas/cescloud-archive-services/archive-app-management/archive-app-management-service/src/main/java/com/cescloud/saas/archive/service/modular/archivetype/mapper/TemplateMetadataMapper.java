/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.archivetype.mapper</p>
 * <p>文件名:TemplateMetadataMapper.java</p>
 * <p>创建时间:2020年2月14日 下午4:48:30</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.archivetype.mapper;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateMetadata;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年2月14日
 */
public interface TemplateMetadataMapper extends BaseMapper<TemplateMetadata> {

    /**
     * 获取最大排序号
     *
     * @return
     */
    public Integer getMaxSortNo(@Param("templateTableId") Long templateTableId);

}
