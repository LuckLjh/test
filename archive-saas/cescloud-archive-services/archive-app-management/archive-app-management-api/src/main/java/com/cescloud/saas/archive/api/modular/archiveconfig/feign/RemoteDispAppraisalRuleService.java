package com.cescloud.saas.archive.api.modular.archiveconfig.feign;

import com.cescloud.saas.archive.api.modular.archiveconfig.entity.DispAppraisalRule;
import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(contextId = "remoteDispAppraisalRuleService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteDispAppraisalRuleService {

    @GetMapping("/appraisal-rule/inner/list")
    public R<List<DispAppraisalRule>> innerList(@RequestHeader(SecurityConstants.FROM) String from);

    @GetMapping("/appraisal-rule/data")
    R<DispAppraisalRule> getByStorageLocate(@RequestParam("storageLocate") String storageLocate);
}
