package com.cescloud.saas.archive.service.modular.businessconfig.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archivedict.entity.DictItem;
import com.cescloud.saas.archive.api.modular.basic.feign.RemoteBusinessBasicService;
import com.cescloud.saas.archive.api.modular.businessconfig.dto.BusinessModelDefineDTO;
import com.cescloud.saas.archive.api.modular.businessconfig.entity.BusinessModelDefine;
import com.cescloud.saas.archive.common.constants.ArchiveConstants;
import com.cescloud.saas.archive.common.constants.BoolEnum;
import com.cescloud.saas.archive.common.constants.ModelTypeEnum;
import com.cescloud.saas.archive.service.modular.archivedict.service.DictItemService;
import com.cescloud.saas.archive.service.modular.businessconfig.cachesupport.annotation.BusinessMetadataReload;
import com.cescloud.saas.archive.service.modular.businessconfig.mapper.BusinessModelDefineMapper;
import com.cescloud.saas.archive.service.modular.businessconfig.service.BusinessModelDefineService;
import com.cescloud.saas.archive.service.modular.businessconfig.service.helper.AsyncWorkflowServiceHelper;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.hanlp.util.HanLPUtil;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import com.cescloud.saas.archive.service.modular.common.security.service.CesCloudUser;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author liwei
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BusinessModelDefineServiceImpl extends ServiceImpl<BusinessModelDefineMapper, BusinessModelDefine> implements BusinessModelDefineService {

	private final RemoteBusinessBasicService remoteBusinessBasicService;

	@Autowired
	@Lazy
	private DictItemService dictItemService;

	@Autowired
	@Lazy
	private AsyncWorkflowServiceHelper asyncWorkflowServiceHelper;


	@Override
	public List<BusinessModelDefine> getBusinessModelDefines(Integer modelType, String keyword) {
		//获取用户信息
		CesCloudUser user = SecurityUtils.getUser();
		Long tenanId = user.getTenantId();
		LambdaQueryWrapper<BusinessModelDefine> queryWrapper = Wrappers.<BusinessModelDefine>query().lambda();
		queryWrapper.eq(BusinessModelDefine::getTenantId, tenanId)
				.eq(BusinessModelDefine::getModelType, modelType)
				.eq(BusinessModelDefine::getIsSys, BoolEnum.NO.getCode());
		if (StrUtil.isNotBlank(keyword)) {
			queryWrapper.and(Wrappers -> Wrappers.like(BusinessModelDefine::getMetadataChinese, keyword)
					.or()
					.like(BusinessModelDefine::getMetadataDescription, keyword));
		}
		queryWrapper.orderByAsc(BusinessModelDefine::getMetadataSort);
		return this.list(queryWrapper);
	}

	@Override
	public List<BusinessModelDefine> getBusinessModelDefinesAll(Integer modelType) {
		//获取用户信息
		CesCloudUser user = SecurityUtils.getUser();
		Long tenanId = user.getTenantId();
		LambdaQueryWrapper<BusinessModelDefine> queryWrapper = Wrappers.<BusinessModelDefine>query().lambda();
		queryWrapper.eq(BusinessModelDefine::getTenantId, tenanId)
				.eq(BusinessModelDefine::getModelType, modelType);
		queryWrapper.orderByAsc(BusinessModelDefine::getMetadataSort);
		return this.list(queryWrapper);
	}

	@Override
	public IPage<BusinessModelDefine> getPageBusinessModelDefines(long current, long size, Integer modelType) {
		return this.page(new Page<>(current, size),
				Wrappers.<BusinessModelDefine>lambdaQuery()
						.select(BusinessModelDefine::getMetadataEnglish, BusinessModelDefine::getMetadataType, BusinessModelDefine::getMetadataLength, BusinessModelDefine::getTenantId, BusinessModelDefine::getModelType)
						.eq(BusinessModelDefine::getModelType, modelType)
						.orderByAsc(BusinessModelDefine::getMetadataSort, BusinessModelDefine::getId));
	}

	@Override
	public IPage<BusinessModelDefine> getPageTenantBusinessModelDefines(long current, long size, Long tenantId, Integer modelType) {
		return this.page(new Page<>(current, size),
				Wrappers.<BusinessModelDefine>lambdaQuery()
						.select(BusinessModelDefine::getMetadataEnglish, BusinessModelDefine::getMetadataType, BusinessModelDefine::getMetadataLength, BusinessModelDefine::getTenantId,BusinessModelDefine::getModelType)
						.eq(BusinessModelDefine::getTenantId, tenantId)
						.eq(BusinessModelDefine::getModelType, modelType)
						.orderByAsc(BusinessModelDefine::getMetadataSort, BusinessModelDefine::getId));
	}

	@Override
	@BusinessMetadataReload(modelType = "#businessModelDefinedto.modelType")
	@Transactional(rollbackFor = Exception.class)
	public boolean saveBusinessModelDefine(BusinessModelDefineDTO businessModelDefinedto) throws ArchiveBusinessException {
		//获取用户信息
		CesCloudUser user = SecurityUtils.getUser();
		Long tenantId = user.getTenantId();
		BusinessModelDefine businessModelDefine = new BusinessModelDefine();
		BeanUtil.copyProperties(businessModelDefinedto, businessModelDefine);
		businessModelDefine.setMetadataSys(BoolEnum.NO.getCode());
		businessModelDefine.setTenantId(tenantId);
		businessModelDefine.setIsSys(BoolEnum.NO.getCode());
		businessModelDefine.setIsEdit(BoolEnum.YES.getCode());
		businessModelDefine.setIsRequired(BoolEnum.NO.getCode());
		businessModelDefine.setIsShow(BoolEnum.YES.getCode());
		if (StrUtil.isBlank(businessModelDefine.getMetadataEnglish())) {
			setMetadataEnglish(businessModelDefine);
		}
		//先新增字段
		R<Boolean> result = remoteBusinessBasicService.addColumn(tenantId, businessModelDefine.getModelType(), businessModelDefine);
		if (result.getCode() == CommonConstants.FAIL || Boolean.FALSE.equals(result.getData())) {
			log.error("新增业务表字段失败");
			throw new ArchiveBusinessException("新增业务表字段失败");
		}
		if (this.save(businessModelDefine)) {
			businessSync( businessModelDefine, tenantId);
		}
		return true;
	}

	@Override
	public BusinessModelDefine setMetadataEnglish(BusinessModelDefine businessModelDefine) throws ArchiveBusinessException {
		log.debug("根据拼音首字母设置english");
		//拼音首字母编码
		String metadataEnglish = HanLPUtil.toPinyinFirstCharString(businessModelDefine.getMetadataChinese());
		if (StrUtil.isBlank(metadataEnglish)) {
			throw new ArchiveBusinessException("字段英文值生成失败，请检查字段名称！");
		}
		final List<Long> tenantIdList = CollectionUtil.<Long>newArrayList(-1L, businessModelDefine.getTenantId());
		Integer maxEnglishNo = baseMapper.getMaxEnglishNoByHidden(metadataEnglish, tenantIdList);
		//没有重复的code
		if (ObjectUtil.isNull(maxEnglishNo)) {
			businessModelDefine.setMetadataEnglish(metadataEnglish);
			//businessModelDefine.setMetadataEnglishHidden(metadataEnglish);
			businessModelDefine.setMetadataEnglishNo(0);
		} else {
			businessModelDefine.setMetadataEnglish(metadataEnglish + ArchiveConstants.SYMBOL.UNDER_LINE + String.valueOf(maxEnglishNo + 1));
			//businessModelDefine.setMetadataEnglishHidden(metadataEnglish);
			businessModelDefine.setMetadataEnglishNo(maxEnglishNo + 1);
		}
		return businessModelDefine;
	}

	@Override
	public List<BusinessModelDefine> getBusinessModelTemplateListByModelType(Integer modelType) {
		return this.baseMapper.getBusinessModelTemplateListByModelType(modelType);
	}

	@Override
	public int deleteByDynamicModel(Integer modelType, String modelCode) {
		return this.baseMapper.deleteByDynamicModel(modelType,modelCode);
	}


	@Override
	@BusinessMetadataReload(modelType = "#businessModelDefinedto.modelType")
	@Transactional(rollbackFor = Exception.class)
	public boolean updateBusinessModelDefine(BusinessModelDefineDTO businessModelDefinedto) throws ArchiveBusinessException {
		BusinessModelDefine old = this.getById(businessModelDefinedto.getId());
		//业务系统字段 只允许 修改字段名和描述
		if (old.getMetadataSys().equals(BoolEnum.YES.getCode())) {
			if (!ObjectUtil.equal(old.getMetadataLength(), businessModelDefinedto.getMetadataLength())
					|| !old.getMetadataType().equals(businessModelDefinedto.getMetadataType())) {
				throw new ArchiveRuntimeException("系统字段只许修改字段名称和描述，其余字段不可修改！！！");
			}
		}
		//所有字段不允许修改模板类型
		if (!old.getModelType().equals(businessModelDefinedto.getModelType())) {
			throw new ArchiveRuntimeException("不允许修改模板类型！！！");
		}
		BusinessModelDefine businessModelDefine = new BusinessModelDefine();
		BeanUtil.copyProperties(businessModelDefinedto, businessModelDefine);
		businessModelDefine.setMetadataEnglish(old.getMetadataEnglish());
		//先修改动态表里的字段
		CesCloudUser user = SecurityUtils.getUser();
		Long tenantId = user.getTenantId();
		R<Boolean> result = remoteBusinessBasicService.modifyColumn(tenantId, old.getModelType(), businessModelDefine);
		if (result.getCode() == CommonConstants.FAIL || result.getData() == false) {
			log.error("修改业务表字段失败");
			throw new ArchiveBusinessException("修改业务表字段失败");
		}
		//同步流程 字段信息
		if (this.updateById(businessModelDefine)) {
			businessSync( businessModelDefine, tenantId);
		}
		return true;
	}



	@Override
	@BusinessMetadataReload
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteBusinessModelDefineById(Long id) throws ArchiveBusinessException {
		BusinessModelDefine businessModelDefine = this.getById(id);
		//判断等于 1 为业务系统字段 不可删除
		if (businessModelDefine.getMetadataSys().equals(BoolEnum.YES.getCode())) {
			throw new ArchiveRuntimeException(String.format("此[%s]字段为系统字段无法进行删除", businessModelDefine.getMetadataChinese()));
		}
		//获取用户信息
		CesCloudUser user = SecurityUtils.getUser();
		Long tenantId = user.getTenantId();
		//先删除动态表里面的字段
		R<Boolean> result = remoteBusinessBasicService.dropColumn(tenantId, businessModelDefine.getModelType(), businessModelDefine);
		if (result.getCode() == CommonConstants.FAIL || result.getData() == false) {
			log.error("删除业务表字段失败");
			throw new ArchiveBusinessException("删除业务表字段失败");
		}
		//同步流程 字段信息
		if (this.removeById(businessModelDefine)) {
			businessSync( businessModelDefine, tenantId);
		}
		return true;
	}

	/**
	 * 同步流程 字段信息
	 * 因 DynamicModelDefineService 会调用 本服务的增删改查，且不需执行同步流程
	 * 从增删改查中摘出来，判断是业务类型的才执行
	 * by 王谷华 2021-04-01
	 * @param businessModelDefine
	 * @param tenantId
	 */
	private void businessSync(BusinessModelDefine businessModelDefine,Long tenantId){
		if(ModelTypeEnum.isBusinessModelType(businessModelDefine.getModelType())){
			// 非业务模板不执行
			return;
		}
		//同步流程 字段信息
		asyncWorkflowServiceHelper.businessSync(businessModelDefine.getModelType(), tenantId,null);
	}

	@Override
	@BusinessMetadataReload(tenantId = "#tenantId")
	@Transactional(rollbackFor = Exception.class)
	public boolean createTable(Long tenantId) throws ArchiveBusinessException {
		/**
		 * 王谷华 2021-03-31 修改 将原方法无脑全模板复制改为逐条业务复制建表 避免影响新添的 动态表
		 * */
		// 遍历所有业务模板
		ModelTypeEnum.getBusiness().forEach(modelTypeEnum ->{
			createTableByByModelType( tenantId, modelTypeEnum.getValue());

		});
		return Boolean.TRUE;
	}

	/**
	 * 按业务ID 复制模板，建表，同步工作流
	 * @param tenantId
	 * @param modelType
	 */
	private void createTableByByModelType(Long tenantId, Integer modelType) {
		//1、在创建表之前要把模板表（apma_business_model_template）中的数据复制到业务模板表（apma_business_model_define）中
		List<BusinessModelDefine> businessModelTemplateList = this.baseMapper.getBusinessModelTemplateListByModelType(modelType);
		if(CollectionUtil.isEmpty(businessModelTemplateList)){
			return;
		}
		businessModelTemplateList.parallelStream().forEach(businessModelDefine -> {
			businessModelDefine.setTenantId(tenantId);
			businessModelDefine.setId(null);
		});
		this.saveBatch(businessModelTemplateList);
		//2、根据业务模板表（apma_business_model_define）中的数据创建动态业务模板表
		//根据类型获取模板记录
		R<Boolean> result = remoteBusinessBasicService.createTable(tenantId, modelType, businessModelTemplateList);
		//3、同步工作流字段
		asyncWorkflowServiceHelper.businessSync(modelType, tenantId, businessModelTemplateList);

	}


	@Override
	public List<Map<String, String>> getEditFormSelectOption(Integer modelType, String metadataEnglish) {
		//获取用户信息
		CesCloudUser user = SecurityUtils.getUser();
		Long tenantId = user.getTenantId();
		BusinessModelDefine businessModelDefine = this.getOne(Wrappers.<BusinessModelDefine>query().lambda().eq(BusinessModelDefine::getModelType, modelType).eq(BusinessModelDefine::getMetadataEnglish, metadataEnglish).eq(BusinessModelDefine::getTenantId, tenantId));
		String dictCode = businessModelDefine.getDictCode();
		if (StrUtil.isNotBlank(dictCode)) {
			List<DictItem> dictItemList = dictItemService.getItemListByDictCode(dictCode);
			List<Map<String, String>> options = dictItemList.stream().map(dictItem -> {
				Map<String, String> map = new HashMap<>();
				map.put("label", dictItem.getItemLabel());
				map.put("value", dictItem.getItemCode());
				return map;
			}).collect(Collectors.toList());
			return options;
		}
		return null;
	}


}
