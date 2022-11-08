package com.cescloud.saas.archive.api.modular.onlinefiling.feign;

import com.cescloud.saas.archive.api.modular.dept.entity.SysDept;
import com.cescloud.saas.archive.api.modular.user.entity.SysUser;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(contextId = "remoteSysUserOrDeptService",value = ServiceNameConstants.ARCHIVE_SYS_MANAGEMENT)
public interface RemoteSysUserOrDeptService {

	@GetMapping("/user/code")
	R<SysUser> getUserByCode(@RequestParam(value = "code") String code);

	@GetMapping("/dept/id")
	R<SysDept> getDeptById(@RequestParam(value = "deptId") Long deptId);
}
