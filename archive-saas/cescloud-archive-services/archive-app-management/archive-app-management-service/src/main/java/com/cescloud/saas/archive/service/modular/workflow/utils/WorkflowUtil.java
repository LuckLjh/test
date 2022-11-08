/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.utils</p>
 * <p>文件名:WorkflowUtil.java</p>
 * <p>创建时间:2019年10月15日 下午4:25:33</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.utils;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年10月15日
 */
@Slf4j
public class WorkflowUtil {

	private WorkflowUtil() {

	}

    /**
     * 复制属性
     *
     * @param target
     *            目标对象
     * @param source
     *            源对象
     * @return
     */
    public static <T> T convert(T target, Object source) {
        try {
            BeanUtil.copyProperties(source, target);
            return target;
        } catch (final Exception e) {
            log.error("复制属性出错", e);
        }
        return null;
    }
}
