package com.cescloud.saas.archive.api.modular.archivedict.feign;

import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author zhangxuehu
 */
@FeignClient(contextId = "remoteDictService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteDictService {

}
