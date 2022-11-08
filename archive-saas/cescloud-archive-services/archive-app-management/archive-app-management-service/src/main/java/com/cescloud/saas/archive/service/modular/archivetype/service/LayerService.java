/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.archivetype.service</p>
 * <p>文件名:LayerService.java</p>
 * <p>创建时间:2020年2月14日 下午12:00:11</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.archivetype.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archivetype.entity.Layer;

import java.util.List;

/**
 * 层级service
 *
 * @author qiucs
 * @version 1.0.0 2020年2月14日
 */
public interface LayerService extends IService<Layer> {

    /**
     * 获取层级下拉框选项
     *
     * @param keyword
     * @return
     */
    List<Layer> getLayerList(String keyword);

    /**
     * 初始化预置层级
     */
    void initLayer();

    /**
     * 根据编码获取对象
     *
     * @param code
     * @return
     */
    Layer getByCode(String code);

    /**
     * 获取层级列表（分页）
     *
     * @param page
     * @param keyword
     * @return
     */
    IPage<Layer> page(IPage<Layer> page, String keyword);

}
