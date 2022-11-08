package com.cescloud.saas.archive.service.modular.syssetting.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.ExcelReader;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.quartz.feign.RemoteQuartzService;
import com.cescloud.saas.archive.api.modular.filecenter.entity.OtherFileStorage;
import com.cescloud.saas.archive.api.modular.fileview.feign.RemoteFileViewService;
import com.cescloud.saas.archive.api.modular.storage.constants.StorageConstants;
import com.cescloud.saas.archive.api.modular.syssetting.dto.*;
import com.cescloud.saas.archive.api.modular.syssetting.entity.SysSetting;
import com.cescloud.saas.archive.api.modular.syssetting.support.SysSettingCacheHolder;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantTemplate;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantTemplateService;
import com.cescloud.saas.archive.api.modular.user.feign.RemoteUserService;
import com.cescloud.saas.archive.common.constants.*;
import com.cescloud.saas.archive.common.util.InitializeUtil;
import com.cescloud.saas.archive.common.util.JsonUtil;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import com.cescloud.saas.archive.service.modular.common.security.service.CesCloudUser;
import com.cescloud.saas.archive.service.modular.common.security.tenantfilter.TenantContextHolder;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.filecenter.service.OtherFileStorageOpenService;
import com.cescloud.saas.archive.service.modular.syssetting.annotation.SysSettingReload;
import com.cescloud.saas.archive.service.modular.syssetting.mapper.SysSettingMapper;
import com.cescloud.saas.archive.service.modular.syssetting.service.SysSettingService;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liwei
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysSettingServiceImpl extends ServiceImpl<SysSettingMapper, SysSetting> implements SysSettingService {

    private static final String FILE_TYPE_SPLIT = ",";

    private static List<String> allowType=new LinkedList<>();
    static {
		allowType.add(SysSettingCodeEnum.TEXTTYPE.getCode());
		allowType.add(SysSettingCodeEnum.AUDIONTYPE.getCode());
		allowType.add(SysSettingCodeEnum.PHOTOTYPE.getCode());
		allowType.add(SysSettingCodeEnum.VIDEOTYPE.getCode());
		allowType.add(SysSettingCodeEnum.OTHERTYPE.getCode());
	}

    @Autowired(required = false)
    private RemoteTenantTemplateService remoteTenantTemplateService;

    @Autowired(required = false)
    SysSettingCacheHolder cacheHolder;

    @Autowired
    private RemoteQuartzService remoteQuartzService;

    @Autowired
	private RemoteUserService remoteUserService;

	@Autowired
	OtherFileStorageOpenService otherFileStorageOpenService;

	@Autowired(required = false)
	RemoteFileViewService remoteFileViewService;


    @Override
	public Map<String, Object> getSysSettingList(String keyword) {
        LambdaQueryWrapper<SysSetting> queryWrapper = Wrappers.lambdaQuery();
        if (StrUtil.isNotBlank(StrUtil.trim(keyword))) {
            queryWrapper.like(SysSetting::getName, StrUtil.trim(keyword));
        }
        List<SysSetting> list = this.list(queryWrapper);
        // 下面的注释中写法map的key活value为空时会报异常，防止数据错误报异常
        //Map<String, Object> map = list.parallelStream().collect(Collectors.toMap(SysSetting::getCode, setting -> filterType(setting)));
        final Map<String, Object> map = CollectionUtil.newHashMap(list.size());
        list.stream().forEach(sysSetting -> {
            map.put(sysSetting.getCode(), filterType(sysSetting));
        });
        return map;
    }

    private Object filterType(SysSetting sysSetting) {
        //开关项
        if (FormConstant.TYPE_SWITCH.equals(sysSetting.getType())) {
            // 1 : true ;0: false
            if (BoolEnum.YES.getCode().equals(Integer.valueOf(sysSetting.getValue()))) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }
        // 下拉框 文本框 单选
        if (FormConstant.TYPE_SELECT.equals(sysSetting.getType()) || FormConstant.TYPE_TEXT.equals(sysSetting.getType())
                || FormConstant.TYPE_RADIO.equals(sysSetting.getType()) || FormConstant.TEXT_AREA.equals(sysSetting.getType())) {
            return sysSetting.getValue();
        }
        //复选框
        if (FormConstant.TYPE_CHECKBOX.equals(sysSetting.getType())) {
            return Arrays.asList(sysSetting.getValue().split(","));
        }

        //自定义 全文加密
        if (FormConstant.IS_CUSTOM.equals(sysSetting.getType()) && SysSettingCodeEnum.FILEENCRYPTION.getCode().equals(sysSetting.getCode())) {
            String value = sysSetting.getValue();
            Map<String, String> map = new HashMap<String, String>();
            try {
                map = JsonUtil.fromJson(value, Map.class);
            } catch (IOException e) {
                log.error("json转化为Map失败！");
            }
            return map;
        }
        //自定义 邮箱设置
        if (FormConstant.IS_CUSTOM.equals(sysSetting.getType()) && SysSettingCodeEnum.EMAIL_CONFIGURATION.getCode().equals(sysSetting.getCode())) {
            return Optional.ofNullable(sysSetting).map(email -> JSONUtil.toBean(email.getValue(), EmailSettingDTO.class)).orElse(null);
        }
		//自定义 系统标题设置
		if (FormConstant.IS_CUSTOM.equals(sysSetting.getType()) && SysSettingCodeEnum.SYSTEM_NAME_SET.getCode().equals(sysSetting.getCode())) {
			return Optional.ofNullable(sysSetting).map(systemNameSet -> JSONUtil.toBean(systemNameSet.getValue(), SystemNameSetDTO.class)).orElse(null);
		}
        //自定义 短信设置
        if (FormConstant.IS_CUSTOM.equals(sysSetting.getType()) && SysSettingCodeEnum.SMS_CONFIGURATION.getCode().equals(sysSetting.getCode())) {
            return Optional.ofNullable(sysSetting).map(note -> JSONUtil.toBean(note.getValue(), NoteSettingDTO.class)).orElse(null);
        }
        //自定义 格式转换
        if (FormConstant.IS_CUSTOM.equals(sysSetting.getType()) && SysSettingCodeEnum.FORMAT_CONVERSION_PARAMETER.getCode().equals(sysSetting.getCode())) {
            return Optional.ofNullable(sysSetting).map(fileConvertSetting -> JSONUtil.toBean(fileConvertSetting.getValue(), FileConvertSettingDTO.class)).orElse(null);
        }
		//自定义 系统水印
	    if (FormConstant.IS_CUSTOM.equals(sysSetting.getType()) && SysSettingCodeEnum.SYSTEM_WATERMARK.getCode().equals(sysSetting.getCode())){
			return Optional.ofNullable(sysSetting).map(systemWatermark -> JSONUtil.toBean(systemWatermark.getValue(), SystemWatermarkDTO.class)).orElse(null);
	    }

        return sysSetting.getValue();
    }

    @Override
	public SysSetting getSysSettingByCode(String code) {
		Optional<SysSetting> opSysSetting = cacheHolder.getCacheEntityByKey(code);
		if (opSysSetting.isPresent()) {
			return opSysSetting.get();
		}
		SysSetting sysSetting ;
		if(ObjectUtil.equals(code,SysSettingCodeEnum.SYSTEM_NAME_SET.getCode())) {
			if(ObjectUtil.isNull(TenantContextHolder.getTenantId())){
				TenantContextHolder.setTenantId(1L);
				sysSetting = this.getOne(Wrappers.<SysSetting>query().lambda().eq(SysSetting::getCode, code), false);
			}else{
				Long oldTenantId = TenantContextHolder.getTenantId();
				TenantContextHolder.setTenantId(1L);
				sysSetting = this.getOne(Wrappers.<SysSetting>query().lambda().eq(SysSetting::getCode, code), false);
				TenantContextHolder.setTenantId(oldTenantId);
			}
			return sysSetting;
		}
		sysSetting = this.getOne(Wrappers.<SysSetting>query().lambda().eq(SysSetting::getCode, code), false);
		return sysSetting;
	}

    @Override
    public SysSetting getSysSettingByTenantIdAndCode(Long tenantId, String code) {
        Optional<SysSetting> opSysSetting = cacheHolder.getCacheEntityByTenantIdKey(tenantId, code);
        if (opSysSetting.isPresent()) {
            return opSysSetting.get();
        }
        SysSetting sysSetting = this.getOne(Wrappers.<SysSetting>query().lambda().eq(SysSetting::getTenantId, tenantId)
                .eq(SysSetting::getCode, code), false);
        return sysSetting;
    }

    @Override
    public FileEncryptionSettingDTO getEncryptSetting(Long tenantId) {
        Optional<SysSetting> opSysSetting = cacheHolder.getCacheEntityByKey(SysSettingCodeEnum.FILEENCRYPTION.getCode());
        SysSetting sysSetting;
        if (opSysSetting.isPresent()) {
            sysSetting = opSysSetting.get();
        } else {
            sysSetting = this.getOne(Wrappers.<SysSetting>query().lambda()
                    .eq(SysSetting::getTenantId, tenantId)
                    .eq(SysSetting::getCode, SysSettingCodeEnum.FILEENCRYPTION.getCode()), false);
        }
        if (null == sysSetting) {
            return FileEncryptionSettingDTO.defaultEncryptSetting();
        }
        FileEncryptionSettingDTO fileEncryptionSettingDTO = null;
        try {
            fileEncryptionSettingDTO = JsonUtil.fromJson(sysSetting.getValue(), FileEncryptionSettingDTO.class);
        } catch (IOException e) {
            log.error("解析json字符串[{}]失败, 采用默认加密设置, 原因：{}", sysSetting.getValue(), e);
            fileEncryptionSettingDTO = FileEncryptionSettingDTO.defaultEncryptSetting();
        }
        return fileEncryptionSettingDTO;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @SysSettingReload
    public R updateSysSetting(Map<String, Object> map) {
        List<SysSetting> list = this.list();
        List<SysSetting> collect = list.parallelStream().map(sysSetting -> updateFilterType(sysSetting, map)).collect(Collectors.toList());
        boolean batch = this.updateBatchById(collect);
        if (!batch) {
            throw new ArchiveRuntimeException("修改失败！！");
        }
        if (map.containsKey(SysSettingCodeEnum.LOGIN_MODIFY_PASSWORD.getCode())){
			// 修改二次登录状态
			R<Boolean> isTrue = remoteUserService.updateLoginModifyPassword((Boolean) map.get(SysSettingCodeEnum.LOGIN_MODIFY_PASSWORD.getCode()) ? 1 : 0);
			if (null != isTrue && isTrue.getData()){
				log.info("修改成功! ");
			}
		}
		return new R(null, "修改成功！");
    }

    /**
     * 根据文件类型获取文本类型
     *
     * @param fileType
     * @return
     */
    @Override
    public Integer getDocType(Long tenantId, String fileType) {
        List<String> codeList = ImmutableList.of(SysSettingCodeEnum.TEXTTYPE.getCode(), SysSettingCodeEnum.PHOTOTYPE.getCode(),
                SysSettingCodeEnum.AUDIONTYPE.getCode(), SysSettingCodeEnum.VIDEOTYPE.getCode(), SysSettingCodeEnum.OTHERTYPE.getCode());
        List<SysSetting> settingList;
        Optional<List<SysSetting>> opSettingList = cacheHolder.getCacheEntityListByKeyList(tenantId,codeList);
	    settingList = opSettingList.orElseGet(() -> this.list(Wrappers.<SysSetting>query().lambda().eq(SysSetting::getTenantId, tenantId).in(SysSetting::getCode, codeList)));
        if (CollectionUtil.isEmpty(settingList)) {
            return DocTypeEnum.TEXTTYPE.getValue();
        }
        SysSetting sysSetting = settingList.stream().filter(Objects::nonNull).filter(setting -> {
            String[] split = StrUtil.split(setting.getValue(), FILE_TYPE_SPLIT);
            return Arrays.stream(split).anyMatch(s -> s.equalsIgnoreCase(fileType));
        }).findFirst().orElse(null);
        return Optional.ofNullable(sysSetting).map(sysSetting1 -> {
            if (SysSettingCodeEnum.TEXTTYPE.getCode().equals(sysSetting1.getCode())) {
                return DocTypeEnum.TEXTTYPE.getValue();
            } else if (SysSettingCodeEnum.PHOTOTYPE.getCode().equals(sysSetting1.getCode())) {
                return DocTypeEnum.PHOTOTYPE.getValue();
            } else if (SysSettingCodeEnum.AUDIONTYPE.getCode().equals(sysSetting1.getCode())) {
                return DocTypeEnum.AUDIOTYPE.getValue();
            } else if (SysSettingCodeEnum.VIDEOTYPE.getCode().equals(sysSetting1.getCode())) {
                return DocTypeEnum.VIDEOTYPE.getValue();
            } else if (SysSettingCodeEnum.OTHERTYPE.getCode().equals(sysSetting1.getCode())) {
	            return DocTypeEnum.OTHERTYPE.getValue();
            } else {
                return DocTypeEnum.TEXTTYPE.getValue();
            }
        }).orElseGet(DocTypeEnum.TEXTTYPE::getValue);
    }

    @Override
    public List<String> getAllowType(Long tenantId){
		LambdaQueryWrapper<SysSetting> queryWrapper = Wrappers.lambdaQuery();

		queryWrapper.in(SysSetting::getCode,allowType);
		queryWrapper.eq(SysSetting::getTenantId,tenantId);
		List<SysSetting> list = this.list(queryWrapper);
		List<String> types=new ArrayList<>();
		if(!CollUtil.isEmpty(list)) {
			list.stream().forEach(sysSetting -> {
				if (!StrUtil.isBlank(sysSetting.getValue())) {
					CollUtil.addAll(types,sysSetting.getValue().split(","));
				}
			});
		}
		return types;
	}

    private SysSetting updateFilterType(SysSetting sysSetting, Map<String, Object> map) {
        Object value = map.get(sysSetting.getCode());

        if (ObjectUtil.isNotNull(value)) {
            //开关项
            if (FormConstant.TYPE_SWITCH.equals(sysSetting.getType())) {
                // 1 : true ;0: false
                if (Boolean.TRUE.equals(value)) {
                    sysSetting.setValue(StrUtil.toString(BoolEnum.YES.getCode()));
                } else {
                    sysSetting.setValue(StrUtil.toString(BoolEnum.NO.getCode()));
                }
            }
            // 下拉框 文本框 单选
            // 修复 textArea无法更新val的bug
            if (FormConstant.TYPE_SELECT.equals(sysSetting.getType()) || FormConstant.TYPE_TEXT.equals(sysSetting.getType()) || FormConstant.TYPE_RADIO.equals(sysSetting.getType()) || FormConstant.TEXT_AREA.equals(sysSetting.getType())) {
                sysSetting.setValue(StrUtil.toString(value));
            }
            //复选框
            if (FormConstant.TYPE_CHECKBOX.equals(sysSetting.getType())) {
                sysSetting.setValue(StrUtil.toString(value));
            }

            //全文加密 自定义
            if (value != null && FormConstant.IS_CUSTOM.equals(sysSetting.getType()) && SysSettingCodeEnum.FILEENCRYPTION.getCode().equals(sysSetting.getCode())) {
                sysSetting.setValue(JsonUtil.bean2json(value));
            }
            //滑块
            if(FormConstant.SLIDER.equals(sysSetting.getType())){
                sysSetting.setValue(StrUtil.toString(value));
            }
        }

        return sysSetting;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SysSettingReload(value = "#tenantId")
    public R initializeHandle(Long templateId, Long tenantId) throws ArchiveBusinessException {
        ExcelReader excel = null;
        try {
            InputStream inputStream = getDefaultTemplateStream(templateId);
            if (ObjectUtil.isNull(inputStream)) {
                return new R<>().fail("", "获取初始化文件异常");
            }
            //closeAfterReader设为true，创建工作簿后会关闭inputStream流
            excel = new ExcelReader(inputStream, TemplateFieldConstants.SHEET_NAMES.SYSTEM_SETTING_NAME, true);
            List<List<Object>> read = excel.read();
            //对表单表头校验
            Map<Integer, String> map = InitializeUtil.checkHeader(TemplateFieldConstants.SYSTEM_SETTING_LIST, read.get(0));
            if (CollectionUtil.isEmpty(map)) {
                return new R<>().fail("", "模板表列数据不匹配！！！");
            }
            final List<SysSetting> sysSettingList = CollectionUtil.newArrayList();
            //循环行
            for (int i = 1, length = read.size(); i < length; i++) {
                //数据处理
                Map<String, String> resultHandling = InitializeUtil.dataTreating(map, TemplateFieldConstants.SYSTEM_SETTING_LIST, read.get(i));
                if (CollectionUtils.isEmpty(resultHandling)) {
                    return new R<>().fail("", "数据异常！！！");
                }
                //新增数据
                SysSetting sysSetting = SysSetting.builder()
                        .value(resultHandling.get(TemplateFieldConstants.SystemSetting.VALUE))
                        .remark(resultHandling.get(TemplateFieldConstants.SystemSetting.REMARK))
                        .name(resultHandling.get(TemplateFieldConstants.SystemSetting.NAME))
                        .tenantId(tenantId).build();
                String code = resultHandling.get(TemplateFieldConstants.SystemSetting.CODE);
                SysSettingCodeEnum anEnum = SysSettingCodeEnum.getEnum(code);
                if (ObjectUtil.isNotNull(anEnum)) {
                    sysSetting.setType(anEnum.getType());
                    sysSetting.setCode(anEnum.getCode());
                }
                sysSettingList.add(sysSetting);
            }
            if (CollUtil.isNotEmpty(sysSettingList)) {
                this.saveBatch(sysSettingList);
            }
        } finally {
            IoUtil.close(excel);
        }

        return new R(null, "成功");
    }

    @Override
    public List<ArrayList<String>> getSystemParameterInfo(Long tenantId) {
        List<SysSetting> sysSettingList = this.list(Wrappers.<SysSetting>lambdaQuery().eq(SysSetting::getTenantId, tenantId));
        //配置项编码	名称	值	说明
        List<ArrayList<String>> collect = sysSettingList.stream().map(sysSetting -> CollectionUtil.newArrayList(sysSetting.getCode(), sysSetting.getName(), sysSetting.getValue(), sysSetting.getRemark())).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<SysSetting> getSystemSettingInfo() {
        //获取系统信息
        final List<SysSetting> sysSettingList = this.list();
        return sysSettingList;
    }

    @Override
    public List<SysSetting> getSystemSettingInfo(Long tenantId) {
        final List<SysSetting> sysSettingList = this.list(Wrappers.<SysSetting>lambdaQuery().eq(SysSetting::getTenantId, tenantId));
        return sysSettingList;
    }

    /**
     * 获取 初始化模板文件流
     *
     * @param templateId 模板id
     * @return
     */
    private InputStream getDefaultTemplateStream(Long templateId) {
        TenantTemplate tenantTemplate = remoteTenantTemplateService.selectTemplateById(templateId).getData();
        byte[] bytes = tenantTemplate.getTemplateContent();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        return inputStream;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SysSettingReload
    public R saveFileEncryptionSetting(FileEncryptionSettingDTO dto) {
        Boolean rtn;
        LambdaQueryWrapper<SysSetting> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysSetting::getCode, SysSettingCodeEnum.FILEENCRYPTION.getCode());
        List<SysSetting> list = this.list(queryWrapper);
        if (list.size() > 0) {
            SysSetting sysSetting = list.get(0);
            sysSetting.setValue(JsonUtil.bean2json(dto));
            rtn = this.updateById(sysSetting);
        } else {
            SysSetting sysSetting = new SysSetting();
            sysSetting.setCode(SysSettingCodeEnum.FILEENCRYPTION.getCode());
            sysSetting.setType(FormConstant.IS_CUSTOM);
            sysSetting.setName(SysSettingCodeEnum.FILEENCRYPTION.getName());
            sysSetting.setRemark("系统将根据加密设置，针对原件或利用件进行加密");
            sysSetting.setValue(JsonUtil.bean2json(dto));
            final CesCloudUser user = SecurityUtils.getUser();
            sysSetting.setTenantId(user.getTenantId());
            this.getBaseMapper().insert(sysSetting);
            rtn = this.updateById(sysSetting);
        }
        if (rtn) {
            return new R().success(null, "保存成功");
        } else {
            return new R().fail(null, "保存失败");
        }
    }

    @Override
    public Integer getUserLockTime(Long tenantId) {
        Optional<String> lockTime = cacheHolder.
                getCacheStrByKey(SysSettingCodeEnum.USER_LOCK_TIME.getCode(), tenantId);

        if (lockTime.isPresent()) {
            return Integer.parseInt(lockTime.get());
        }

        SysSetting sysSetting = this.getOne(Wrappers.<SysSetting>query().lambda()
                .eq(SysSetting::getTenantId, tenantId)
                .eq(SysSetting::getCode, SysSettingCodeEnum.USER_LOCK_TIME.getCode()), false);
        return Integer.parseInt(sysSetting.getValue());
    }

    @Override
    public Integer getPasswordErrorTimes(Long tenantId) {
        Optional<String> errorTimes = cacheHolder.
                getCacheStrByKey(SysSettingCodeEnum.PASSWORD_ERROR_TIMES.getCode(), tenantId);

        if (errorTimes.isPresent()) {
            return Integer.parseInt(errorTimes.get());
        }

        SysSetting sysSetting = this.getOne(Wrappers.<SysSetting>query().lambda()
                .eq(SysSetting::getTenantId, tenantId)
                .eq(SysSetting::getCode, SysSettingCodeEnum.PASSWORD_ERROR_TIMES.getCode()), false);
        return Integer.parseInt(sysSetting.getValue());
    }

    @Override
    public Integer getPasswordErrorSeparation(Long tenantId) {
        Optional<String> separation = cacheHolder.
                getCacheStrByKey(SysSettingCodeEnum.PASSWORD_ERROR_SEPARATION.getCode(), tenantId);

        if (separation.isPresent()) {
            return Integer.parseInt(separation.get());
        }

        SysSetting sysSetting = this.getOne(Wrappers.<SysSetting>query().lambda()
                .eq(SysSetting::getTenantId, tenantId)
                .eq(SysSetting::getCode, SysSettingCodeEnum.PASSWORD_ERROR_SEPARATION.getCode()), false);
        return Integer.parseInt(sysSetting.getValue());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SysSettingReload
    public boolean saveFileComvertSetting(FileConvertSettingDTO dto) {
        this.remove(Wrappers.<SysSetting>lambdaQuery().eq(SysSetting::getCode, SysSettingCodeEnum.FORMAT_CONVERSION_PARAMETER.getCode()));

        SysSetting sysSetting = new SysSetting();
        sysSetting.setType(FormConstant.IS_CUSTOM);
        sysSetting.setCode(SysSettingCodeEnum.FORMAT_CONVERSION_PARAMETER.getCode());
        sysSetting.setName(SysSettingCodeEnum.FORMAT_CONVERSION_PARAMETER.getName());
        sysSetting.setValue(JsonUtil.bean2json(dto));
        sysSetting.setRemark("开启时，上传文件将进行格式转换");
        return this.save(sysSetting);
    }

    @Override
    public FileConvertSettingDTO getFileComvertSetting() {
        SysSetting sysSetting = getSysSettingByCode(SysSettingCodeEnum.FORMAT_CONVERSION_PARAMETER.getCode());
        return Optional.ofNullable(sysSetting).map(fileConvertSetting -> JSONUtil.toBean(fileConvertSetting.getValue(), FileConvertSettingDTO.class)).orElse(null);
    }

	@Override
	public SystemNameSetDTO getSystemNameSet() {
		SysSetting sysSetting = getSysSettingByCode(SysSettingCodeEnum.SYSTEM_NAME_SET.getCode());
		if(log.isDebugEnabled()) {
			log.debug("sysSetting:{}", sysSetting);
		}

		sysSetting=Optional.ofNullable(sysSetting).orElseGet(() ->new SysSetting());
		SystemNameSetDTO systemNameSetDTO=JSONUtil.toBean(sysSetting.getValue(), SystemNameSetDTO.class);

		// 屏蔽 图片信息
		systemNameSetDTO.setPicUrl("");
		systemNameSetDTO.setPicId("");
		return systemNameSetDTO;
	}

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SysSettingReload
    public boolean enabledFileComvertSetting(Integer enabled) {
        FileConvertSettingDTO fileComvertSetting = getFileComvertSetting();
        if (null == fileComvertSetting) {
            fileComvertSetting = FileConvertSettingDTO.builder().enabled(enabled.equals(1)).build();
        } else {
            fileComvertSetting.setEnabled(enabled.equals(1));
        }
        R rest= remoteQuartzService.enabledFileConvertJob(enabled);
        if(rest.getCode() == CommonConstants.FAIL){
            log.warn("格式转换任务{}失败：{}", enabled.equals(1) ? "启动" : "暂停", rest.getMsg());
        }
        return this.saveFileComvertSetting(fileComvertSetting);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveEmailSetting(EmailSettingDTO emailSettingDTO) {
        SysSetting sysSetting = this.getOne(Wrappers.<SysSetting>lambdaQuery().eq(SysSetting::getCode, SysSettingCodeEnum.EMAIL_CONFIGURATION.getCode()));
        if (ObjectUtil.isNotNull(sysSetting)) {
            sysSetting.setValue(JsonUtil.bean2json(emailSettingDTO));
            this.updateById(sysSetting);
        } else {
            sysSetting = new SysSetting();
            sysSetting.setCode(SysSettingCodeEnum.EMAIL_CONFIGURATION.getCode());
            sysSetting.setType(FormConstant.IS_CUSTOM);
            sysSetting.setName(SysSettingCodeEnum.EMAIL_CONFIGURATION.getName());
            sysSetting.setRemark("开启后，逾期提醒将通过邮件通知");
            sysSetting.setValue(JsonUtil.bean2json(emailSettingDTO));
            final CesCloudUser user = SecurityUtils.getUser();
            sysSetting.setTenantId(user.getTenantId());
            this.save(sysSetting);
        }
        return Boolean.TRUE;
    }

	@Override
	@Transactional(rollbackFor = Exception.class)
	@SysSettingReload
	public SystemNameSetDTO uploadSystemNameSet(MultipartFile file) {
		SysSetting sysSetting = this.getOne(Wrappers.<SysSetting>lambdaQuery().eq(SysSetting::getCode, SysSettingCodeEnum.SYSTEM_NAME_SET.getCode()));
		SystemNameSetDTO systemNameSetDTO = null;
		if(ObjectUtil.isNotNull(file)){
			OtherFileStorage otherFileStorage = null;
			try {
				otherFileStorage = otherFileStorageOpenService.upload(file, StorageConstants.NOTICE_FILE_STORAGE);
			} catch (Exception e) {
				log.error("上传失败!", e);
				throw  new ArchiveRuntimeException("上传失败!");
			}
			if (ObjectUtil.isNotNull(sysSetting)) {
				systemNameSetDTO = JSONUtil.toBean(sysSetting.getValue(), SystemNameSetDTO.class);
				systemNameSetDTO.setPicId(String.valueOf(otherFileStorage.getId()));
	//			systemNameSetDTO.setPicUrl(this.getImageViewUrl(String.valueOf(otherFileStorage.getId())));
				systemNameSetDTO.setPicUrl("");
				systemNameSetDTO.setEncryptWord(SecurityUtils.getCommonFileEncrypt(otherFileStorage.getId()));
				sysSetting.setValue(JsonUtil.bean2json(systemNameSetDTO));
				if(log.isDebugEnabled()) {
					log.debug("sysSetting:{}", sysSetting);
				}
				this.updateById(sysSetting);
			} else {
				systemNameSetDTO = new SystemNameSetDTO();
				systemNameSetDTO.setPicId(String.valueOf(otherFileStorage.getId()));
				systemNameSetDTO.setPicUrl("");
				systemNameSetDTO.setEncryptWord(SecurityUtils.getCommonFileEncrypt(otherFileStorage.getId()));
				sysSetting = new SysSetting();
				sysSetting.setCode(SysSettingCodeEnum.SYSTEM_NAME_SET.getCode());
				sysSetting.setType(FormConstant.IS_CUSTOM);
				sysSetting.setName(SysSettingCodeEnum.SYSTEM_NAME_SET.getName());
				sysSetting.setRemark("系统Logo、系统名称及简称的设置参数");
				sysSetting.setValue(JsonUtil.bean2json(systemNameSetDTO));
				sysSetting.setTenantId(1L);
				this.save(sysSetting);
			}
		}
		return systemNameSetDTO;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@SysSettingReload
	public SystemNameSetDTO saveSystemNameSet(SystemNameSetDTO systemNameSetDTO) {
		SysSetting sysSetting = this.getOne(Wrappers.<SysSetting>lambdaQuery().eq(SysSetting::getCode, SysSettingCodeEnum.SYSTEM_NAME_SET.getCode()));
		if (ObjectUtil.isNotNull(sysSetting)) {
//			systemNameSetDTO.setPicUrl(JSONUtil.toBean(sysSetting.getValue(), SystemNameSetDTO.class).getPicUrl());
			// 由于保存只提交文字 系统登录图片相关取旧值，不改动前台
			SystemNameSetDTO old=JSONUtil.toBean(sysSetting.getValue(), SystemNameSetDTO.class);
			systemNameSetDTO.setPicId(old.getPicId());
			systemNameSetDTO.setEncryptWord(old.getEncryptWord());
			systemNameSetDTO.setPicUrl("");

			sysSetting.setValue(JsonUtil.bean2json(systemNameSetDTO));
			this.updateById(sysSetting);
		} else {
			sysSetting = new SysSetting();
			sysSetting.setCode(SysSettingCodeEnum.SYSTEM_NAME_SET.getCode());
			sysSetting.setType(FormConstant.IS_CUSTOM);
			sysSetting.setName(SysSettingCodeEnum.SYSTEM_NAME_SET.getName());
			sysSetting.setRemark("系统Logo、系统名称及简称的设置参数");
			sysSetting.setValue(JsonUtil.bean2json(systemNameSetDTO));
			sysSetting.setTenantId(1L);
			this.save(sysSetting);
		}
		return systemNameSetDTO;
	}

	private String getImageViewUrl(String fileIdStr) {
		if (StrUtil.isBlank(fileIdStr)) {
			return "";
		}
		String objectUrl = "";
		try {
			objectUrl = otherFileStorageOpenService.getObjectUrl(Long.parseLong(fileIdStr), 1L);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return objectUrl;
	}

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveNoteSetting(NoteSettingDTO noteSettingDTO) {
        SysSetting sysSetting = this.getOne(Wrappers.<SysSetting>lambdaQuery().eq(SysSetting::getCode, SysSettingCodeEnum.SMS_CONFIGURATION.getCode()));
        if (ObjectUtil.isNotNull(sysSetting)) {
            sysSetting.setValue(JsonUtil.bean2json(noteSettingDTO));
            this.updateById(sysSetting);
        } else {
            sysSetting = new SysSetting();
            sysSetting.setCode(SysSettingCodeEnum.SMS_CONFIGURATION.getCode());
            sysSetting.setType(FormConstant.IS_CUSTOM);
            sysSetting.setName(SysSettingCodeEnum.SMS_CONFIGURATION.getName());
            sysSetting.setRemark("开启后，逾期提醒将通过短信通知");
            sysSetting.setValue(JsonUtil.bean2json(noteSettingDTO));
            final CesCloudUser user = SecurityUtils.getUser();
            sysSetting.setTenantId(user.getTenantId());
            this.save(sysSetting);
        }
        return Boolean.TRUE;
    }

    @Override
    public SourceSettingDTO getSourceSetting(Long tenantId) {
        final List<SysSetting> sysSettings = this.list(Wrappers.<SysSetting>lambdaQuery().in(SysSetting::getCode, CollectionUtil.newArrayList(SysSettingCodeEnum.EMAIL_CONFIGURATION.getCode(), SysSettingCodeEnum.USING_LATE_EMAIL.getCode(), SysSettingCodeEnum.USING_LATE_SMS.getCode(), SysSettingCodeEnum.SMS_CONFIGURATION.getCode())).eq(SysSetting::getTenantId,tenantId));
        SourceSettingDTO sourceSettingDTO = new SourceSettingDTO();
        sysSettings.stream().forEach(sysSetting -> {
            if (SysSettingCodeEnum.USING_LATE_EMAIL.getCode().equals(sysSetting.getCode())) {
                sourceSettingDTO.setIsEmailMessage(BoolEnum.YES.getCode().toString().equals(sysSetting.getValue()) ? Boolean.TRUE : Boolean.FALSE);
            } else if (SysSettingCodeEnum.EMAIL_CONFIGURATION.getCode().equals(sysSetting.getCode())) {
                sourceSettingDTO.setEmailSettingDTO(Optional.ofNullable(sysSetting).map(note -> JSONUtil.toBean(note.getValue(), EmailSettingDTO.class)).orElse(null));
            } else if (SysSettingCodeEnum.USING_LATE_SMS.getCode().equals(sysSetting.getCode())) {
                sourceSettingDTO.setIsNoteMessage(BoolEnum.YES.getCode().toString().equals(sysSetting.getValue()) ? Boolean.TRUE : Boolean.FALSE);
            } else if (SysSettingCodeEnum.SMS_CONFIGURATION.getCode().equals(sysSetting.getCode())) {
                sourceSettingDTO.setNoteSettingDTO(Optional.ofNullable(sysSetting).map(note -> JSONUtil.toBean(note.getValue(), NoteSettingDTO.class)).orElse(null));
            }
        });
        return sourceSettingDTO;
    }

	@Override
	public Boolean saveSystemWatermarkSetting(SystemWatermarkDTO systemWatermarkDTO) {
		this.remove(Wrappers.<SysSetting>lambdaQuery().eq(SysSetting::getCode, SysSettingCodeEnum.SYSTEM_WATERMARK.getCode()));
		SysSetting sysSetting = new SysSetting();
		sysSetting.setType(FormConstant.IS_CUSTOM);
		sysSetting.setCode(SysSettingCodeEnum.SYSTEM_WATERMARK.getCode());
		sysSetting.setName(SysSettingCodeEnum.SYSTEM_WATERMARK.getName());
		sysSetting.setValue(JsonUtil.bean2json(systemWatermarkDTO));
		sysSetting.setRemark("开启时，系统将根据设置显示系统水印");
		return this.save(sysSetting);
	}

}
