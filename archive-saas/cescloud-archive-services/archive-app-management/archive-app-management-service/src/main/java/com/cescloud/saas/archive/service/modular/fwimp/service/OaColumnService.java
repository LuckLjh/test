package com.cescloud.saas.archive.service.modular.fwimp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.fwimp.entity.OaColumn;
import com.cescloud.saas.archive.api.modular.fwimp.entity.OaImport;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * oa 导入列
 */
public interface OaColumnService extends IService<OaColumn> {

	List<OaColumn> getOaColumnDetail(Long id);

    R uploadExcel(MultipartFile file, String tableName) throws ArchiveBusinessException;

	R<List<Metadata>> getColumnByName(String tableName) throws ArchiveBusinessException;

}
