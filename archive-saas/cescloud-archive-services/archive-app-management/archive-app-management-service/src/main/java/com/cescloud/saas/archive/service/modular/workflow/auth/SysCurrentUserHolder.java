/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.auth</p>
 * <p>文件名:SysCurrentUserHolder.java</p>
 * <p>创建时间:2019年11月11日 下午4:41:36</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.auth;

import com.cesgroup.core.auth.CurrentUserHolder;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年11月11日
 */
public class SysCurrentUserHolder implements CurrentUserHolder {

    /**
     *
     * @see com.cesgroup.core.auth.CurrentUserHolder#getUserId()
     */
    @Override
    public String getUserId() {
        return SecurityUtils.getUser().getId().toString();
    }

    /**
     *
     * @see com.cesgroup.core.auth.CurrentUserHolder#getUsername()
     */
    @Override
    public String getUsername() {
        return SecurityUtils.getUser().getChineseName();
    }

    /**
     *
     * @see com.cesgroup.core.auth.CurrentUserHolder#getLoginName()
     */
    @Override
    public String getLoginName() {
        return SecurityUtils.getUser().getUsername();
    }

    /**
     *
     * @see com.cesgroup.core.auth.CurrentUserHolder#getTenantId()
     */
    @Override
    public String getTenantId() {
        return SecurityUtils.getUser().getTenantId().toString();
    }

}
