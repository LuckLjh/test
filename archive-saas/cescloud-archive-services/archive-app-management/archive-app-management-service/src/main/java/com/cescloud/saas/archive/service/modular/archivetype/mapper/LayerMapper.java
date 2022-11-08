/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.archivetype.mapper</p>
 * <p>文件名:LayerMapper.java</p>
 * <p>创建时间:2020年2月14日 上午11:54:28</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.archivetype.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.archivetype.entity.Layer;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年2月14日
 */
public interface LayerMapper extends BaseMapper<Layer> {

    /**
     * 获取最大排序号
     * 
     * @return
     */
    public Integer getMaxSortNo();
}
