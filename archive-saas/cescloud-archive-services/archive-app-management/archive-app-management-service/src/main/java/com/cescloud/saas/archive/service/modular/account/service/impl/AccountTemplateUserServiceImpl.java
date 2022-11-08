
package com.cescloud.saas.archive.service.modular.account.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.account.entity.AccountTemplate;
import com.cescloud.saas.archive.api.modular.account.entity.AccountTemplateUser;

import com.cescloud.saas.archive.api.modular.stats.dto.DeckTotalStatsDTO;
import com.cescloud.saas.archive.common.constants.ArchiveConstants;
import com.cescloud.saas.archive.common.constants.BoolEnum;
import com.cescloud.saas.archive.service.modular.account.mapper.AccountTemplateUserMapper;
import com.cescloud.saas.archive.service.modular.account.service.AccountTemplateUserService;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 驾驶舱个人台账模板表
 *
 * @author bob
 * @date 2021-05-28 23:02:59
 */
@Service
public class AccountTemplateUserServiceImpl extends ServiceImpl<AccountTemplateUserMapper, AccountTemplateUser> implements AccountTemplateUserService {

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean settingDefaultTemplate(AccountTemplateUser accountTemplate) {
		final Integer isDefault = accountTemplate.getIsDefault();
		this.updateById(accountTemplate);
		if (BoolEnum.YES.getCode().equals(isDefault)) {
			AccountTemplateUser updateAccountTemplate = AccountTemplateUser.builder().isDefault(BoolEnum.NO.getCode()).build();
			this.update(updateAccountTemplate, Wrappers.<AccountTemplateUser>lambdaQuery().ne(AccountTemplateUser::getId, accountTemplate.getId()));
		}
		return Boolean.TRUE;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public DeckTotalStatsDTO<Boolean, List<AccountTemplateUser>> settingAccountTemplateUser(List<AccountTemplate> userTemplateLists,Long userId) {
		DeckTotalStatsDTO<Boolean, List<AccountTemplateUser>> resultAll = new DeckTotalStatsDTO<>();
		Boolean showDeck = false;
		List<AccountTemplateUser> result = null;

		List<AccountTemplateUser> accountTemplateUserList = this.list(Wrappers.<AccountTemplateUser>lambdaQuery().eq(AccountTemplateUser::getUserId,userId));
		AccountTemplateUser defaultAccountTemplateUser = accountTemplateUserList.stream().filter(t->t.getIsDefault().intValue() == BoolEnum.YES.getCode()).findFirst().orElse(null);
		final List<Long> userTemplateIds = accountTemplateUserList.stream().map(accountTemplateDTO -> accountTemplateDTO.getTemplateId()).collect(Collectors.toList());
		final List<Long> templateIds = userTemplateLists.stream().map(accountTemplateDTO -> accountTemplateDTO.getId()).collect(Collectors.toList());
		List<Long> colDisjunction = CollUtil.disjunction(userTemplateIds,templateIds).stream().collect(Collectors.toList());

		if(colDisjunction.size()>0){
			this.remove(Wrappers.<AccountTemplateUser>lambdaQuery().eq(AccountTemplateUser::getUserId,userId));
			List<AccountTemplateUser> insertAccountTemplateUserList = Lists.newArrayList();
			userTemplateLists.stream().forEach(t->{
				AccountTemplateUser insertAccountTemplate = AccountTemplateUser.builder()
						.templateId(t.getId())
						.templateName(t.getTemplateName())
						.tenantId(t.getTenantId())
						.userId(userId)
						.updatedBy(userId)
						.updatedTime(LocalDateTime.now())
						.isDefault(defaultAccountTemplateUser!=null && defaultAccountTemplateUser.getTemplateId().longValue()==t.getId().longValue()?BoolEnum.YES.getCode() :  BoolEnum.NO.getCode()).build();
				insertAccountTemplateUserList.add(insertAccountTemplate);
			});
			if(insertAccountTemplateUserList.size()>0){
				this.saveBatch(insertAccountTemplateUserList);
				result = insertAccountTemplateUserList;
			} else {
				result = insertAccountTemplateUserList;
			}
		}else{
			result = accountTemplateUserList;
		}
		AccountTemplateUser showDeckAccountTemplateUser = result.stream().filter(t-> StrUtil.equalsAnyIgnoreCase(ArchiveConstants.V8_DECK_SHOW_TILE,t.getTemplateName())).findFirst().orElse(null);
		boolean setV8DeckAccountTemplateUserDefault = false;
		long v8Count = result.stream().filter(t-> StrUtil.equalsAnyIgnoreCase(ArchiveConstants.V8_DECK_SHOW_TILE,t.getTemplateName())).count();
		if(v8Count>0&&result.size()==1){
			showDeck = true;
			setV8DeckAccountTemplateUserDefault = true;
		}
		if(showDeckAccountTemplateUser!=null){
			if(showDeckAccountTemplateUser.getIsDefault().intValue() == 1){
				showDeck = true;
			}
		}
		long defaultCount = result.stream().filter(t-> t.getIsDefault().intValue() == 1 ).count();
		if(defaultCount==0 && v8Count>0){
			if(showDeckAccountTemplateUser!=null){
				showDeckAccountTemplateUser.setIsDefault(1);
				showDeck = true;
				setV8DeckAccountTemplateUserDefault = true;
			}
		}
		if(setV8DeckAccountTemplateUserDefault){
			showDeckAccountTemplateUser.setIsDefault(1);
			this.updateById(showDeckAccountTemplateUser);
		}
		resultAll.setTotal(showDeck);
		resultAll.setList(result);
		return resultAll;
	}
}
