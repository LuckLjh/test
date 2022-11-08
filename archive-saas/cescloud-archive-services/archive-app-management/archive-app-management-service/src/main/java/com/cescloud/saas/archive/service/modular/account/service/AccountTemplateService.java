package com.cescloud.saas.archive.service.modular.account.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.account.dto.AccountTemplateDTO;
import com.cescloud.saas.archive.api.modular.account.entity.AccountTemplate;
import com.cescloud.saas.archive.api.modular.account.entity.AccountTemplateRole;
import com.cescloud.saas.archive.api.modular.account.entity.AccountTemplateUser;
import com.cescloud.saas.archive.api.modular.role.dto.RoleSyncTreeNode;
import com.cescloud.saas.archive.api.modular.stats.dto.DeckTotalStatsDTO;
import com.cescloud.saas.archive.api.modular.stats.entity.ArchiveDeckNew;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.ArrayList;
import java.util.List;

public interface AccountTemplateService extends IService<AccountTemplate> {

    IPage getPage(Page page, String moduleName);

    AccountTemplateDTO getInfoById(Long id);

    Boolean saveAccountTemplate(AccountTemplateDTO accountTemplateDTO);

    Boolean removeAccountTemplate(String ids);

    Boolean updateAccountTemplate(AccountTemplateDTO accountTemplateDTO);

    Boolean settingAssociatedRole(AccountTemplateDTO accountTemplateDTO);

    Boolean settingDefaultTemplate(AccountTemplate accountTemplate);

    AccountTemplate getAccountModule(String roles);

    Boolean initializeAccountTemplate(Long templateId, Long tenantId);

    List<RoleSyncTreeNode> roleTree(Long templateId,String templateName) throws ArchiveBusinessException;
	List<ArrayList<String>> getAccountInfo(Long tenantId);

	DeckTotalStatsDTO<Boolean, List<AccountTemplateUser>> showDeck(ArchiveDeckNew archiveDeckNew);

	List<AccountTemplateRole> getAccountTemplateRole(Integer templateId);
}
