/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.stats.feign</p>
 * <p>文件名:RemoteAppStatsTimingService.java</p>
 * <p>创建时间:2020年9月28日 下午3:24:44</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.stats.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年9月28日
 */
@FeignClient(contextId = "remoteAppStatsTimingService", value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT, path = "/stats-timing")
public interface RemoteAppStatsTimingService {
	//档案新增
	@PostMapping("/archive-deck-new")
	public R<Boolean> archiveDeckNew(@RequestHeader(SecurityConstants.FROM) String from);
    @PostMapping("/archive-data")
    public R<Boolean> archiveData(@RequestHeader(SecurityConstants.FROM) String from);

    @PostMapping("/filing")
    public R<Boolean> filing(@RequestHeader(SecurityConstants.FROM) String from);

}
