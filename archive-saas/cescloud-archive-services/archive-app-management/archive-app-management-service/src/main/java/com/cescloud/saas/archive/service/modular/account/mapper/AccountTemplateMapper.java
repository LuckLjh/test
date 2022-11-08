package com.cescloud.saas.archive.service.modular.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.api.modular.account.dto.AccountTemplateDTO;
import com.cescloud.saas.archive.api.modular.account.entity.AccountTemplate;
import org.apache.ibatis.annotations.Param;

public interface AccountTemplateMapper extends BaseMapper<AccountTemplate> {

    IPage<AccountTemplateDTO> getPageByName(Page page, @Param("query") String keyWord);

}
