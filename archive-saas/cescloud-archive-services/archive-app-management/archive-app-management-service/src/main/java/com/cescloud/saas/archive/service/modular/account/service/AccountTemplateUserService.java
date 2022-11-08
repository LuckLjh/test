
package com.cescloud.saas.archive.service.modular.account.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.account.entity.AccountTemplate;
import com.cescloud.saas.archive.api.modular.account.entity.AccountTemplateUser;
import com.cescloud.saas.archive.api.modular.stats.dto.DeckTotalStatsDTO;

import java.util.List;


/**
 * 驾驶舱个人台账模板表
 *
 * @author bob
 * @date 2021-05-28 23:02:59
 */
public interface AccountTemplateUserService extends IService<AccountTemplateUser> {
	Boolean settingDefaultTemplate(AccountTemplateUser accountTemplate);
	//根据 用户角色所获取的魔板列表 获取 用户魔板列表
	DeckTotalStatsDTO<Boolean, List<AccountTemplateUser>> settingAccountTemplateUser(List<AccountTemplate> userTemplateLists, Long id);
}
