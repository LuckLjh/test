package com.cescloud.saas.archive.service.modular.fwimp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.fwimp.dto.OaColumnExpListDTO;
import com.cescloud.saas.archive.api.modular.fwimp.dto.OaColumnExpandDTO;
import com.cescloud.saas.archive.api.modular.fwimp.entity.OaColumn;
import com.cescloud.saas.archive.api.modular.fwimp.entity.OaColumnExpand;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.List;

/**
 * oa 导入列扩展
 */
public interface OaColumnExpandService extends IService<OaColumnExpand> {
	R saveOaColumnExp(OaColumnExpListDTO oaColumnExpands);

	R findByColumnAndFlowId(String columnName, Long flowId) throws ArchiveBusinessException;

	R<List<OaColumnExpand>> findByFlowId(Long oaFlowid);
}
