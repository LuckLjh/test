package com.cescloud.saas.archive.api.modular.businessconfig.feign;

import com.cescloud.saas.archive.api.modular.businessconfig.entity.BusinessModelDefine;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author liwei
 */
@FeignClient(contextId = "remoteBusinessModelDefineService", value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteBusinessModelDefineService {

    /**
     * 创建业务表
     *
     * @param tenantId 租户id
     * @return R
     */
    @GetMapping("/model-define/create-table/{tenantId}")
    R createTable(@PathVariable("tenantId") Long tenantId);

    @GetMapping("/model-define/list")
    R<List<BusinessModelDefine>> getBusinessModelDefines(@RequestParam(value = "modelType", required = false) Integer modelType, @RequestParam(value = "keyword", required = false) String keyword);

    @GetMapping("/model-define/all")
    R<List<BusinessModelDefine>> getBusinessModelDefinesAll(@RequestParam(value = "modelType", required = false) Integer modelType);

}
