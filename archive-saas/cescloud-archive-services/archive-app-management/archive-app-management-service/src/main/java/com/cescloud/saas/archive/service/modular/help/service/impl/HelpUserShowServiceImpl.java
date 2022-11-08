
package com.cescloud.saas.archive.service.modular.help.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.fonds.entity.Fonds;
import com.cescloud.saas.archive.api.modular.help.entity.HelpUserShow;

import com.cescloud.saas.archive.service.modular.common.security.service.CesCloudUser;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.fonds.service.FondsService;
import com.cescloud.saas.archive.service.modular.help.mapper.HelpUserShowMapper;
import com.cescloud.saas.archive.service.modular.help.service.HelpUserShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 档案类型数据权限
 *
 * @author zhaiyachao
 * @date 2021-05-11 18:32:44
 */
@Service
public class HelpUserShowServiceImpl extends ServiceImpl<HelpUserShowMapper, HelpUserShow> implements HelpUserShowService {


	@Autowired
	private FondsService fondsService;

	/**
	 *
	 * @param fondId
	 * @param menuId
	 * @param tenantId
	 * @param fondsCode
	 * @return
	 */
	@Override
	public Boolean isData(Long fondId, Integer menuId, Long tenantId, String fondsCode) {
		HelpUserShow userShow = this.baseMapper.selectOne(Wrappers.<HelpUserShow>lambdaQuery().eq(HelpUserShow::getMenuId, menuId)
				.eq(HelpUserShow::getTenantId, tenantId).eq(HelpUserShow::getFondsCode, fondsCode));
		// 如果有数据 用户不要求查看
		if (ObjectUtil.isEmpty(userShow)) return true;
		return false;
	}

	/**
	 * @param fondId
	 * @param menuId
	 * @param isShowType
	 * @return true:显示 / false:不显示
	 */
	@Override
	public Boolean isShow(Long fondId, Integer menuId, Integer isShowType) {
		if (null == fondId || null == menuId || null == isShowType) return true;
		// 系统默认显示
		Boolean type = true;
		CesCloudUser user = SecurityUtils.getUser();
		// 获取全宗
		Fonds fonds = fondsService.getFondsById(fondId);
		if (ObjectUtil.isNotEmpty(fonds)) {
			switch (isShowType) {
				case 0:
					break;
				case 1:
					HelpUserShow show = new HelpUserShow();
					show.setUserId(user.getId());
					show.setTenantId(user.getTenantId());
					show.setMenuId(menuId.longValue());
					show.setFondsCode(fonds.getFondsCode());
					this.baseMapper.insert(show);
					type = false;
					break;
			}
		}
		return type;
	}

	/**
	 * 删除显示数据
	 *
	 * @param fondsCode
	 * @param menuId
	 */
	@Override
	public void clearUserData(String fondsCode, Integer menuId) {
		CesCloudUser user = SecurityUtils.getUser();
		this.baseMapper.delete(Wrappers.<HelpUserShow>lambdaQuery()
				.eq(HelpUserShow::getMenuId, menuId)
				.eq(HelpUserShow::getFondsCode, fondsCode)
				.eq(HelpUserShow::getTenantId, user.getTenantId()));
	}
}
