
package com.cescloud.saas.archive.service.modular.help.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.help.entity.HelpUserShow;


/**
 * 档案类型数据权限
 *
 * @author zhaiyachao
 * @date 2021-05-11 18:32:44
 */
public interface HelpUserShowService extends IService<HelpUserShow> {

	Boolean isData(Long fondId, Integer menuId, Long tenantId, String fondsCode);

	void clearUserData(String fondsCode, Integer menuId);

	Boolean isShow(Long fondId, Integer menuId, Integer isShowType);
}
