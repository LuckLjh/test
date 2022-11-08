package com.cescloud.saas.archive.service.modular.syssetting.controller;

import com.cescloud.saas.archive.api.modular.quartz.feign.RemoteQuartzService;
import com.cescloud.saas.archive.api.modular.synonymy.entity.Synonymy;
import com.cescloud.saas.archive.api.modular.syssetting.dto.*;
import com.cescloud.saas.archive.api.modular.syssetting.entity.SysSetting;
import com.cescloud.saas.archive.api.modular.tenant.entity.Tenant;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantService;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.annotation.Inner;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.syssetting.service.SysSettingService;
import com.cesgroup.core.util.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author liwei
 */
@Api(value = "sysSetting", tags = "系统管理-参数管理：参数设置")
@Slf4j
@RestController
@Validated
@RequestMapping("/sys-setting")
public class SysSettingController {
	@Autowired
	private SysSettingService sysSettingService;

	@Autowired
	private RemoteTenantService remoteTenantService;

    @Autowired
    private  RemoteQuartzService remoteQuartzService;


	@ApiOperation(value = "参数设置的查询", httpMethod = "GET")
	@GetMapping
	public R<Map<String, Object>> getSysSettingList(@RequestParam(value = "keyword", required = false) @ApiParam(value = "检索关键字", name = "keyword", required = false) String keyword) {
		return new R<>(sysSettingService.getSysSettingList(keyword));
	}

	@ApiOperation(value = "根据code查询参数设置", httpMethod = "GET")
	@GetMapping("/code/{code}")
	public R<SysSetting> getSysSettingByCode(@PathVariable("code") @ApiParam(value = "配置编码", name = "code", required = true) @NotBlank(message = "code不能为空") String code) {
		return new R<>(sysSettingService.getSysSettingByCode(code));
	}

	@GetMapping("/code/{tenantId}/{code}")
	@Inner
	public R<SysSetting> getSysSettingByTenantIdAndCode(@PathVariable("tenantId") Long tenantId, @PathVariable("code") String code) {
		return new R<>(sysSettingService.getSysSettingByTenantIdAndCode(tenantId, code));
	}

	@ApiOperation(value = "根据文件类型获取文本类型", httpMethod = "GET")
	@GetMapping("/doc-type/{tenantId}/{fileType}")
	@Inner
	public R<Integer> getDocType(@PathVariable("tenantId") @ApiParam(value = "租户ID", name = "tenantId", required = true) @NotNull(message = "tenantId不能为空") Long tenantId,
								 @PathVariable("fileType") @ApiParam(value = "文件后缀", name = "fileType", required = true) @NotBlank(message = "fileType不能为空") String fileType) {
		return new R<>(sysSettingService.getDocType(tenantId, fileType));
	}

	@ApiOperation(value = "获取密码连续输入错误账户锁定", httpMethod = "GET")
	@GetMapping("/password-error-times/{tenantId}")
	@Inner
	public R<Integer> getPasswordErrorTimes(@PathVariable("tenantId") Long tenantId) {
		return new R<Integer>(sysSettingService.getPasswordErrorTimes(tenantId));
	}

	@ApiOperation(value = "获取用户锁定时间", httpMethod = "GET")
	@GetMapping("/user-lock-time/{tenantId}")
	@Inner
	public R<Integer> getUserLockTime(@PathVariable("tenantId") Long tenantId) {
		return new R<Integer>(sysSettingService.getUserLockTime(tenantId));
	}

	@ApiOperation(value = "获取加密设置", httpMethod = "GET")
	@GetMapping("/encrypt-setting/{tenantId}")
	@Inner
	public R<FileEncryptionSettingDTO> getEncryptSetting(@PathVariable("tenantId") Long tenantId) {
		return new R<>(sysSettingService.getEncryptSetting(tenantId));
	}

	@ApiOperation(value = "获取密码错误间隔", httpMethod = "GET")
	@GetMapping("/password-error-separation/{tenantId}")
	@Inner
	public R<Integer> getPasswordErrorSeparation(@PathVariable("tenantId") Long tenantId) {
		return new R<>(sysSettingService.getPasswordErrorSeparation(tenantId));
	}

	@ApiOperation(value = "参数设置的修改", httpMethod = "PUT")
	@PutMapping
	@SysLog("参数设置的修改")
	public R updateSysSettingList(@RequestBody @ApiParam(value = "传入json格式", name = "更新", required = true) Map<String, Object> map) {
		return sysSettingService.updateSysSetting(map);
	}

	@ApiOperation(value = "初始化系统参数设置", httpMethod = "POST")
	@PostMapping(value = "/initialize")
	@SysLog("初始化系统参数设置")
	public R initializeHandle(@RequestParam(value = "templateId", required = false) Long templateId, @RequestParam(value = "tenantId") Long tenantId) throws ArchiveBusinessException, IOException {
		return sysSettingService.initializeHandle(templateId, tenantId);
	}

	@ApiOperation(value = "获取系统参数设置", httpMethod = SwaggerConstants.GET)
	@GetMapping(value = "/data/{tenantId}")
	@SysLog("获取系统参数设置")
	public R<List<ArrayList<String>>> getSystemParameterInfo(@PathVariable("tenantId") Long tenantId) {
		return new R(sysSettingService.getSystemParameterInfo(tenantId));
	}

	@ApiOperation(value = "保存全文加密设置", httpMethod = "PUT")
	@PutMapping("/save/fileEncryption")
	@SysLog("保存全文加密设置")
	public R saveFileEncryptionSetting(@RequestBody @ApiParam(value = "全文加密设置DTO", name = "dto", required = false) FileEncryptionSettingDTO dto) {
		try {
			SysLogContextHolder.setLogTitle(String.format("保存全文加密设置-原件存储加密【%s】-原件存储加密方式【%s】-利用件下载加密【%s】",dto.getOriStore().equals("0")?"关闭":"开启",dto.getOriStoreEncryptionType().equals("1")?"国密算法":"通用算法",dto.getUsingDown().equals("0")?"关闭":"开启"));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return sysSettingService.saveFileEncryptionSetting(dto);
	}

	@ApiOperation(value = "保存格式转换设置", httpMethod = "PUT")
	@PutMapping("/file-convert")
	@SysLog("保存格式转换设置")
	public R saveFileConvertSetting(@RequestBody @ApiParam(value = "格式转换设置参数", name = "dto", required = false) FileConvertSettingDTO dto) {
		sysSettingService.saveFileComvertSetting(dto);
		return new R<>("", "保存成功！");
	}

	@ApiOperation(value = "保存邮件设置", httpMethod = "PUT")
	@PutMapping("/email")
	@SysLog("保存邮件设置")
	public R saveEmailSetting(@RequestBody @ApiParam(value = "邮箱设置参数", name = "emailSettingDTO") EmailSettingDTO emailSettingDTO) {
		sysSettingService.saveEmailSetting(emailSettingDTO);
		return new R<>("", "保存成功！");
	}

	@ApiOperation(value = "更新系统标题图片上传", httpMethod = "POST")
	@PostMapping("/systemNameSet-upload")
	@SysLog("更新系统标题图片上传")
	public R saveSystemNameSetforUpload(@RequestParam("file") @ApiParam(name = "file", value = "文件对象") MultipartFile file) {
		try {
			SysLogContextHolder.setLogTitle(String.format("更新系统标题图片上传-图片名称【%s】",file.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(sysSettingService.uploadSystemNameSet(file));
	}

	@ApiOperation(value = "保存系统标题设置", httpMethod = "POST")
	@PostMapping("/systemNameSet")
	@SysLog("保存系统标题设置")
	public R saveSystemNameSet(@RequestBody @ApiParam(value = "系统标题参数", name = "systemNameSetDTO")SystemNameSetDTO systemNameSetDTO) {
		try {
			SysLogContextHolder.setLogTitle(String.format("保存系统标题设置-系统名称【%s】",systemNameSetDTO.getSysName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(sysSettingService.saveSystemNameSet(systemNameSetDTO));
	}

	@ApiOperation(value = "保存短信设置", httpMethod = "PUT")
	@PutMapping("/note")
	@SysLog("保存短信设置")
	public R saveNoteSetting(@RequestBody @ApiParam(value = "短信设置参数", name = "noteSettingDTO") NoteSettingDTO noteSettingDTO) {
		sysSettingService.saveNoteSetting(noteSettingDTO);
		return new R<>("", "保存成功！");
	}

	@ApiOperation(value = "保存系统水印设置", httpMethod = "PUT")
	@PutMapping("/system-watermark")
	@SysLog("保存系统水印设置")
	public R<Boolean> saveNoteSetting(@RequestBody @ApiParam(value = "系统水印设置参数", name = "systemWatermarkDTO") SystemWatermarkDTO systemWatermarkDTO) {
		return new R<Boolean>(sysSettingService.saveSystemWatermarkSetting(systemWatermarkDTO), "保存成功！");
	}

	@ApiOperation(value = "得到格式转换设置", httpMethod = "GET")
	@GetMapping("/file-convert")
	public R<FileConvertSettingDTO> getFileConvertSetting() {
		return new R<>(sysSettingService.getFileComvertSetting());
	}

	@ApiOperation(value = "获取短信邮箱设置", httpMethod = "GET")
	@GetMapping("/source-setting/{tenantId}")
	@Inner
	public R<SourceSettingDTO> getSourceSetting(@PathVariable("tenantId") Long tenantId){
		return new R<SourceSettingDTO>(sysSettingService.getSourceSetting(tenantId));
	}
	@ApiOperation(value = "开启或者关闭格式转换设置", httpMethod = "PUT")
	@PutMapping("/file-convert/{enabled}")
	public R enabledFileConvertSetting(@PathVariable("enabled") @ApiParam(value = "是否开启：0：关闭，1：开启", name = "enabled", required = true) Integer enabled) {
		sysSettingService.enabledFileComvertSetting(enabled);
        R r = remoteQuartzService.startJobByParameter(String.valueOf(enabled));
        if(StringUtils.isNotBlank(r.getMsg())){
            return new R().fail(null, r.getMsg());
        }else {
            return new R<>("", "设置成功！");
        }
	}


	@ApiOperation(value = "获取允许上传的全文类型", httpMethod = "GET")
	@GetMapping("/getAllowType/{tenantId}")
	public R<List<String>> getAllowType(@PathVariable("tenantId") Long tenantId) {
		return new R<List<String>>(sysSettingService.getAllowType(tenantId));
	}

	@ApiOperation(value = "获取登录信息整合")
	@GetMapping("/login-info")
	public R<LoginInfoDTO> loginInfo() {
		LoginInfoDTO loginInfoDTO = new LoginInfoDTO();
		//登录方式
		R<Boolean> saasLogin = remoteTenantService.saasLogin(SecurityConstants.FROM_IN);
		if(saasLogin.getCode() == CommonConstants.SUCCESS){
			loginInfoDTO.setIsOnline(saasLogin.getData());
		}
		//所有租户
		R<List<Tenant>> activeTenants = remoteTenantService.listActiveTenants(SecurityConstants.FROM_IN);
		if (activeTenants.getCode() == CommonConstants.SUCCESS) {
			loginInfoDTO.setTenants(activeTenants.getData());
		}
		//系统给名称
		loginInfoDTO.setSystemNameSet(sysSettingService.getSystemNameSet());
		return new R<>(loginInfoDTO);
	}
}
