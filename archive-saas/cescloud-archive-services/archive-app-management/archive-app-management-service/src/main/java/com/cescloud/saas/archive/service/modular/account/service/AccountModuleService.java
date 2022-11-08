package com.cescloud.saas.archive.service.modular.account.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.account.entity.AccountModule;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

public interface AccountModuleService extends IService<AccountModule> {

    /**
     * 租户配置初始化
     *
     * @param tenantId   租户id
     * @return
     * @throws ArchiveBusinessException
     */
    Boolean initializeAccountModule(Long tenantId) throws ArchiveBusinessException;

}
