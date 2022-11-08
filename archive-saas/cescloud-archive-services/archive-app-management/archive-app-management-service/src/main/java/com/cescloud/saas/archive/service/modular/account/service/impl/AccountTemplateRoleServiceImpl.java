package com.cescloud.saas.archive.service.modular.account.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.account.entity.AccountTemplateRole;
import com.cescloud.saas.archive.service.modular.account.mapper.AccountTemplateRoleMapper;
import com.cescloud.saas.archive.service.modular.account.service.AccountTemplateRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName AccountTemplateRoleServiceImpl
 * @Author zhangxuehu
 * @Date 2021/2/24 上午10:00
 **/
@Service
@Slf4j
public class AccountTemplateRoleServiceImpl extends ServiceImpl<AccountTemplateRoleMapper, AccountTemplateRole> implements AccountTemplateRoleService {

    @Override
    public Map<Long, List<AccountTemplateRole>> getTemplateRoleByTemplatIds(List<Long> templatIds) {
        if (CollectionUtil.isEmpty(templatIds)) {
            return Collections.emptyMap();
        }
        Map<Long, List<AccountTemplateRole>> accountTemplateRoleMap = this.list(Wrappers.<AccountTemplateRole>lambdaQuery().in(AccountTemplateRole::getTemplateId,templatIds))
                .stream().collect(Collectors.groupingBy(AccountTemplateRole::getTemplateId));
        return accountTemplateRoleMap;
    }
}
