
package com.cescloud.saas.archive.service.modular.help.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.help.entity.HelpConf;

import java.util.List;


/**
 * 基本数据权限
 *
 * @author zhaiyachao
 * @date 2021-05-11 18:32:44
 */
public interface HelpConfService extends IService<HelpConf> {

	Boolean updateOpen(Integer fondId, Integer openType);

	Boolean isOpen(Integer fondId);

	Boolean copyOtherFond(Integer fondId, List<Integer> fondIds);
}
