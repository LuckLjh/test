package com.cescloud.saas.archive.service.modular.filingscope.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.filingscope.dto.FilingScopeOrderDTO;
import com.cescloud.saas.archive.api.modular.filingscope.dto.FilingScopeTypePostDTO;
import com.cescloud.saas.archive.api.modular.filingscope.dto.FilingScopeTypePutDTO;
import com.cescloud.saas.archive.api.modular.filingscope.entity.FilingScopeType;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.ArrayList;
import java.util.List;

public interface FilingScopeTypeService extends IService<FilingScopeType> {

    Boolean saveFilingScopeType(FilingScopeTypePostDTO filingScopeTypePostDTO) throws ArchiveBusinessException;

    Boolean updateFilingScopeType(FilingScopeTypePutDTO filingScopeTypePutDTO) throws ArchiveBusinessException;

    Boolean deleteById(Long id) throws ArchiveBusinessException;

    IPage<List<FilingScopeType>> findFilingScopeTypeByParentId(Page page, String id, String keyWord);

    /**
     * 归档范围信息初始化
     * @return
     * @throws ArchiveBusinessException
     */
    void initializeFilingScopeTypeHandle(Long templateId, Long tenantId) throws ArchiveBusinessException ;

    List<ArrayList<String>> getFilingScopeTypeInfo(Long tenantId);

	Boolean filingScopeTypeOrder(FilingScopeOrderDTO filingScopeOrderDTO);
}
