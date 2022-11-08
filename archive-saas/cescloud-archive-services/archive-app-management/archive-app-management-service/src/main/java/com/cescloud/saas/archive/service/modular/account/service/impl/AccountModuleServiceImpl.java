package com.cescloud.saas.archive.service.modular.account.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.account.entity.AccountModule;
import com.cescloud.saas.archive.common.constants.AccountModuleEnum;
import com.cescloud.saas.archive.service.modular.account.mapper.AccountModuleMapper;
import com.cescloud.saas.archive.service.modular.account.service.AccountModuleService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * @ClassName AccountModuleServiceImpl
 * @Author zhangxuehu
 * @Date 2021/2/24 上午10:00
 **/
@Service
@Slf4j
@AllArgsConstructor
public class AccountModuleServiceImpl extends ServiceImpl<AccountModuleMapper, AccountModule> implements AccountModuleService {


    @Override
    public Boolean initializeAccountModule(Long tenantId) {
        List<AccountModule> accountModules = CollectionUtil.newArrayList();
        for (AccountModuleEnum accountModuleEnum : AccountModuleEnum.values()) {
            AccountModule accountModule = AccountModule.builder().moduleName(accountModuleEnum.getModuleName()).moduleCode(accountModuleEnum.getModuleCode()).sortNo(accountModuleEnum.getSortNo()).tenantId(tenantId).build();
            accountModules.add(accountModule);
        }
        if(CollectionUtil.isNotEmpty(accountModules)){
            this.saveBatch(accountModules);
        }
        return Boolean.TRUE;
    }
}
