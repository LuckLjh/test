package com.cescloud.saas.archive.service.modular.businessconfig.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.businessconfig.entity.BusinessModelDefine;
import com.cescloud.saas.archive.api.modular.businessconfig.entity.BusinessStyleSetting;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantTemplate;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantTemplateService;
import com.cescloud.saas.archive.common.constants.FormConstant;
import com.cescloud.saas.archive.common.constants.ModelTypeEnum;
import com.cescloud.saas.archive.common.constants.TemplateFieldConstants;
import com.cescloud.saas.archive.common.constants.business.UsingFieldConstants;
import com.cescloud.saas.archive.common.util.CesBlobUtil;
import com.cescloud.saas.archive.common.util.JsonUtil;
import com.cescloud.saas.archive.service.modular.businessconfig.mapper.BusinessStyleSettingMapper;
import com.cescloud.saas.archive.service.modular.businessconfig.service.BusinessModelDefineService;
import com.cescloud.saas.archive.service.modular.businessconfig.service.BusinessStyleSettingService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liwei
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BusinessStyleSettingServiceImpl extends ServiceImpl<BusinessStyleSettingMapper, BusinessStyleSetting> implements BusinessStyleSettingService {

	@Autowired @Lazy
    private BusinessModelDefineService businessModelDefineService;

	private final RemoteTenantTemplateService remoteTenantTemplateService;
    /**
     * 有自定义选项标签组
     */
    private final List<String> customLabelSet = Arrays.asList(FormConstant.TYPE_SELECT, FormConstant.TYPE_RADIO, FormConstant.TYPE_CHECKBOX);

    @Override
    public BusinessStyleSetting getBusinessModelDefines(Integer modelType) {
        BusinessStyleSetting businessStyleSetting = this.getOne(Wrappers.<BusinessStyleSetting>query().lambda()
                .eq(BusinessStyleSetting::getModelType, modelType));
        if (ObjectUtil.isNull(businessStyleSetting)) {
            businessStyleSetting = new BusinessStyleSetting();
            businessStyleSetting.setModelType(modelType);
        } else {
            log.debug("将模板[{}]的配置从blob转为map对象", ModelTypeEnum.getEnum(modelType).getName());
            byte[] bytes = CesBlobUtil.objConvertToByte(businessStyleSetting.getFormContent());
            LinkedHashMap map = (LinkedHashMap) ObjectUtil.deserialize(bytes);
            businessStyleSetting.setFormContent(map);
        }
        return businessStyleSetting;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BusinessStyleSetting saveBusinessStyleSetting(BusinessStyleSetting businessStyleSetting) {
        Integer modelType = businessStyleSetting.getModelType();
        log.debug("删除原来模板<{}>的表单定义配置", ModelTypeEnum.getEnum(modelType).getName());
        this.remove(Wrappers.<BusinessStyleSetting>lambdaQuery().eq(BusinessStyleSetting::getModelType, modelType));
        log.debug("重新保存模板<{}>的表单定义配置", ModelTypeEnum.getEnum(modelType).getName());
        LinkedHashMap formContent = (LinkedHashMap) businessStyleSetting.getFormContent();
        byte[] bytes = ObjectUtil.serialize(formContent);
        businessStyleSetting.setFormContent(bytes);
        this.save(businessStyleSetting);
        return businessStyleSetting;
    }

    @Override
    public BusinessStyleSetting initForm(Integer modelType) {
        BusinessStyleSetting businessStyleSetting = this.getOne(Wrappers.<BusinessStyleSetting>query().lambda().eq(BusinessStyleSetting::getModelType, modelType));
        if (ObjectUtil.isNull(businessStyleSetting)) {
            businessStyleSetting = new BusinessStyleSetting();
            businessStyleSetting.setModelType(modelType);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("将表[{}]的配置从blob转为map对象", ModelTypeEnum.getEnum(modelType).getName());
            }
            byte[] bytes = CesBlobUtil.objConvertToByte(businessStyleSetting.getFormContent());
            LinkedHashMap map = (LinkedHashMap) ObjectUtil.deserialize(bytes);
            //list结构
            List list = (List) map.get(FormConstant.LIST);
            //表单字段长度赋值
            //setLength(modelType,list);
            //遍历List,对于自定义类型进行编码替换
            list.stream().filter(obj -> isCustomType(obj))
                    .forEach(obj -> setFormSelectOption(obj, modelType));
            businessStyleSetting.setFormContent(map);
        }

		//TODO 前端处理 @seclectChange事件时只对下拉框有效，原先单选框无法触发事件，导致在线移交、离线移交无法保存值这里先将移交类型改为下拉框
		Map m = (Map) businessStyleSetting.getFormContent();
		List<Map<String, Object>> list = (List<Map<String, Object>>) m.get("list");
		if (modelType == 4 && list.size() >= 3 && list.get(3).get("model").equals("transfer_type")) {
			list.get(3).replace("type", "select");
			list.get(3).replace("icon", "icon-select");
		}
        return businessStyleSetting;
    }

    /**
     * 表单字段长度赋值
     *
     * @param modelType
     * @param list
     */
    private void setLength(Integer modelType, List list) {
        //获取业务模板定义
        List<BusinessModelDefine> businessModelDefines = businessModelDefineService.getBusinessModelDefines(modelType, null);
        list.parallelStream().forEach(obj -> {
            HashMap mapOfList = (HashMap) obj;
            String metadataEnglish = (String) mapOfList.get(FormConstant.MODEL);
            //获取字段长度
            int length = 0;
            BusinessModelDefine businessModelDefine = businessModelDefines.parallelStream().filter(define -> define.getMetadataEnglish().equals(metadataEnglish)).findAny().get();
            if (ObjectUtil.isNotNull(businessModelDefine) && ObjectUtil.isNotNull(businessModelDefine.getMetadataLength())) {
                length = businessModelDefine.getMetadataLength().intValue();
            }
            List<Map<String, Object>> rules = (List<Map<String, Object>>) mapOfList.get(FormConstant.RULES);
            Map<String, Object> ruleLengthMap = new HashMap<>(4);
            ruleLengthMap.put(FormConstant.LENGTH, length);
            ruleLengthMap.put(FormConstant.MESSAGE, "长度不能超过" + length);
            rules.add(ruleLengthMap);
        });
    }

    /**
     * 获取 自定义类型
     *
     * @param object
     * @return
     */
    private boolean isCustomType(Object object) {
        Map<String, Object> mapOfList = (HashMap) object;
        String type = (String) mapOfList.get(FormConstant.TYPE);
        Map<String, Object> optionsMap = (HashMap) mapOfList.get(FormConstant.OPTIONS);
        String model = (String) mapOfList.get(FormConstant.MODEL);
        //判断选项自定义
        if (customLabelSet.contains(type)) {
            if (ObjectUtil.isNull(optionsMap.get(FormConstant.IS_CUSTOM))) {
                return false;
            }
            if (!(Boolean) optionsMap.get(FormConstant.IS_CUSTOM)) {
                return true;
            }
        }
        //日期选择框判断类型
        if (FormConstant.TYPE_DATE.equals(type)) {
            if (UsingFieldConstants.REGISTER_DATE.equals(model)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 设置表单值
     *
     * @param object
     * @param modelType
     */
    private void setFormSelectOption(Object object, Integer modelType) {
        Map<String, Object> mapOfList = (HashMap) object;
        String type = (String) mapOfList.get(FormConstant.TYPE);
        String model = (String) mapOfList.get(FormConstant.MODEL);
        HashMap optionsMap = (HashMap) mapOfList.get(FormConstant.OPTIONS);
        //判断选项自定义
        if (customLabelSet.contains(type)) {
            List<Map<String, String>> editFormSelectOption = businessModelDefineService.getEditFormSelectOption(modelType, model);
            if (ObjectUtil.isNotNull(editFormSelectOption)) {
                optionsMap.put(FormConstant.OPTIONS, editFormSelectOption);
            }
        }
        //日期框赋值
        if (FormConstant.TYPE_DATE.equals(type)) {
            optionsMap.put(FormConstant.DEFAULTVALUE, DateUtil.today());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R initializeHandle(Long templateId, Long tenantId) throws ArchiveBusinessException {
        InputStream inputStream = getDefaultTemplateStream(templateId);
        if (ObjectUtil.isNull(inputStream)) {
            return new R<>().fail("", "获取初始化文件异常");
        }
        //第四个参数closeAfterClose设为true，会自动关闭inputStream流
        @Cleanup ExcelReader excel = new ExcelReader(inputStream, TemplateFieldConstants.SHEET_NAMES.BUSINESS_TEMPLATE_FORM_SETTINGS, true);
        List<List<Object>> read = excel.read();
        Integer modelType = null;
        List<BusinessStyleSetting> styleSettings = new ArrayList<>();
        //循环行
        for (int i = 1, length = read.size(); i < length; i++) {
            String arrangement = StrUtil.toString(read.get(i).get(0));
            LinkedHashMap linkedHashMap = JsonUtil.toBean(StrUtil.toString(read.get(i).get(1)), LinkedHashMap.class);
            modelType = ObjectUtil.isNotNull(ModelTypeEnum.getEnumByName(arrangement)) ? ModelTypeEnum.getEnumByName(arrangement).getValue() : 0;
            BusinessStyleSetting businessStyleSetting = BusinessStyleSetting.builder().modelType(modelType).formContent(ObjectUtil.serialize(linkedHashMap)).tenantId(tenantId).build();
            styleSettings.add(businessStyleSetting);
        }
        if (CollUtil.isNotEmpty(styleSettings)) {
            this.saveBatch(styleSettings);
        }
        return new R("初始化成功");
    }

    @Override
    public List<ArrayList<String>> getBusinessTemplateFormInfo(Long tenantId) {
        final List<BusinessStyleSetting> businessStyleSettings = this.list(Wrappers.<BusinessStyleSetting>lambdaQuery().eq(BusinessStyleSetting::getTenantId, tenantId));
        List<ArrayList<String>> collect = businessStyleSettings.stream().map(businessStyleSetting -> CollectionUtil.newArrayList(ModelTypeEnum.getEnum(businessStyleSetting.getModelType()).getName(), processingFormInformation(businessStyleSetting.getFormContent()))).collect(Collectors.toList());
        return collect;
    }

    /**
     * 处理表单数据
     *
     * @param formContent
     * @return
     */
    private String processingFormInformation(Object formContent) {
        byte[] bytes = CesBlobUtil.objConvertToByte(formContent);
        LinkedHashMap map = (LinkedHashMap) ObjectUtil.deserialize(bytes);
        return JsonUtil.bean2json(map);
    }

    /**
     * 获取 初始化模板文件流
     *
     * @param templateId 模板id
     * @return
     */
    private InputStream getDefaultTemplateStream(Long templateId) {
        TenantTemplate tenantTemplate = remoteTenantTemplateService.selectTemplateById(templateId).getData();
        byte[] bytes = (byte[]) tenantTemplate.getTemplateContent();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        return inputStream;
    }
}
