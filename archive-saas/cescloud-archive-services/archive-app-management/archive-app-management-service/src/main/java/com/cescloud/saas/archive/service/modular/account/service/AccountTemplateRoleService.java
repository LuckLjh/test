package com.cescloud.saas.archive.service.modular.account.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.account.entity.AccountTemplateRole;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveEdit;

import java.util.List;
import java.util.Map;

public interface AccountTemplateRoleService extends IService<AccountTemplateRole> {

    Map<Long, List<AccountTemplateRole>> getTemplateRoleByTemplatIds(List<Long> templatIds);
}
