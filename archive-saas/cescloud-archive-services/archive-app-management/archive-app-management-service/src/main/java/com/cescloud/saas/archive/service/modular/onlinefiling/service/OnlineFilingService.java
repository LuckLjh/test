package com.cescloud.saas.archive.service.modular.onlinefiling.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.onlinefiling.entity.OnlineFiling;

/**
 * 在线导入
 * @author 黄宇权
 */
public interface OnlineFilingService extends IService<OnlineFiling> {

	void startImp(String param);
}
