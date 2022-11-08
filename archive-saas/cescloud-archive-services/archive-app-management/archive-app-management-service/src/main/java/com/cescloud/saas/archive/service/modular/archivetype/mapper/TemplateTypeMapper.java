/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.archivetype.mapper</p>
 * <p>文件名:TemplateTypeMapper.java</p>
 * <p>创建时间:2020年2月14日 下午4:50:09</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.archivetype.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateType;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年2月14日
 */
public interface TemplateTypeMapper extends BaseMapper<TemplateType> {

    /**
     * 获取最大排序号
     *
     * @return
     */
    public Integer getMaxSortNo();
}
