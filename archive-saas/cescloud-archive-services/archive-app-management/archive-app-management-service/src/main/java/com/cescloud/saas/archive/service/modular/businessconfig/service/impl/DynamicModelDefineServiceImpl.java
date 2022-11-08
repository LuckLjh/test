
package com.cescloud.saas.archive.service.modular.businessconfig.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.basic.feign.RemoteDynamicBasicService;
import com.cescloud.saas.archive.api.modular.businessconfig.dto.DynamicBusinessModelDefineDTO;
import com.cescloud.saas.archive.api.modular.businessconfig.entity.BusinessModelDefine;
import com.cescloud.saas.archive.api.modular.businessconfig.entity.DynamicModelDefine;
import com.cescloud.saas.archive.common.constants.BoolEnum;
import com.cescloud.saas.archive.service.modular.businessconfig.mapper.DynamicModelDefineMapper;
import com.cescloud.saas.archive.service.modular.businessconfig.service.BusinessModelDefineService;
import com.cescloud.saas.archive.service.modular.businessconfig.service.DynamicModelDefineService;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.RedisKeyConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import com.cescloud.saas.archive.service.modular.common.security.service.CesCloudUser;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 动态表字段对应
 *
 * @author 王谷华
 * @date 2021-04-01 16:39:49
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DynamicModelDefineServiceImpl extends ServiceImpl<DynamicModelDefineMapper, DynamicModelDefine> implements DynamicModelDefineService {

	private final RemoteDynamicBasicService remoteDynamicBasicService;

	private final BusinessModelDefineService businessModelDefineService;

	private final RedisTemplate redisTemplate;

	@Override
	public List<DynamicBusinessModelDefineDTO> getBusinessModelDefinesByDynamic(Integer modelType,  Long tenantId, String code, String keyword) {
		List<DynamicBusinessModelDefineDTO> businessModelDefinesByDynamic = this.baseMapper.getBusinessModelDefinesByDynamic(modelType, code, Objects.isNull(tenantId) ? SecurityUtils.getUser().getTenantId() : tenantId, keyword);
		return businessModelDefinesByDynamic.stream().filter(e -> BoolEnum.YES.getCode().equals(e.getIsSys())).collect(Collectors.toList());
	}

	@Override
	public List<String> getDynamicFields(Integer modelType, Long tenantId, String code, String keyword) {
		List<DynamicBusinessModelDefineDTO> dtoList= getBusinessModelDefinesByDynamic( modelType,   tenantId,  code,  keyword);
		if(CollUtil.isEmpty(dtoList)){
			return new ArrayList<>();

		}
		List<String> retList=new ArrayList<>();
		dtoList.stream().forEach(dynamicBusinessModelDefineDTO ->{
			retList.add(dynamicBusinessModelDefineDTO.getMetadataEnglish());

		});

		return retList;
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean saveDynamicModelDefine(DynamicBusinessModelDefineDTO dynamicBusinessModelDefineDTO) throws ArchiveBusinessException {
		//获取用户所属租户信息
		Long tenantId = SecurityUtils.getUser().getTenantId();
		//1.查重
		DynamicBusinessModelDefineDTO oldDynamicBusinessModelDefineDTO=this.baseMapper.getDynamicBusinessModelDefineDTOByMetadataEnglish(dynamicBusinessModelDefineDTO.getModelType(),dynamicBusinessModelDefineDTO.getModelCode()
				,tenantId,dynamicBusinessModelDefineDTO.getMetadataEnglish());
		if(!Objects.isNull(oldDynamicBusinessModelDefineDTO)){
			throw new ArchiveBusinessException("新增业务表字段失败,该字段已存在");
		}
		BusinessModelDefine businessModelDefine = new BusinessModelDefine();
		BeanUtil.copyProperties(dynamicBusinessModelDefineDTO, businessModelDefine);
		businessModelDefine.setMetadataSys(BoolEnum.NO.getCode());
		businessModelDefine.setTenantId(tenantId);
		businessModelDefine.setIsSys(BoolEnum.YES.getCode());
		businessModelDefine.setIsEdit(BoolEnum.YES.getCode());
		businessModelDefine.setIsRequired(BoolEnum.NO.getCode());
		businessModelDefine.setIsShow(BoolEnum.YES.getCode());
		if (StrUtil.isBlank(businessModelDefine.getMetadataEnglish())) {
			businessModelDefineService.setMetadataEnglish(businessModelDefine);
		}


		//2.新增字段
		R<Boolean> result = remoteDynamicBasicService.addColumn(tenantId, businessModelDefine.getModelType(),dynamicBusinessModelDefineDTO.getModelCode(), businessModelDefine);
		if (result.getCode() == CommonConstants.FAIL || Boolean.FALSE.equals(result.getData())) {
			log.error("新增业务表字段失败");
			throw new ArchiveBusinessException("新增业务表字段失败");
		}
		// 保存 业务模板定义 表
		boolean defineResult=businessModelDefineService.save(businessModelDefine);
		if(!defineResult){
			throw new ArchiveBusinessException("新增业务表字段失败");
		}
		// 创建 DynamicModelDefine
		DynamicModelDefine dynamicModelDefine=getDynamicModelDefine(dynamicBusinessModelDefineDTO,businessModelDefine.getId());
		//TODO 刷新 redis
		 this.save(dynamicModelDefine);
//
		redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));
		String key= RedisKeyConstants.getSpecialAdd(tenantId);
		String cols=(String)redisTemplate.opsForHash().get(key,dynamicBusinessModelDefineDTO.getModelCode());
		if(StrUtil.isEmpty(cols)){
			cols=businessModelDefine.getMetadataEnglish();
		}else {
			cols= cols+","+businessModelDefine.getMetadataEnglish();
		}

		redisTemplate.opsForHash().put(key,dynamicBusinessModelDefineDTO.getModelCode(), cols);
		return true;
	}

	@Override
	public boolean updateDynamicModelDefine(DynamicBusinessModelDefineDTO dynamicBusinessModelDefineDTO) throws ArchiveBusinessException {
		BusinessModelDefine old = businessModelDefineService.getById(dynamicBusinessModelDefineDTO.getDefinedId());
		//业务系统字段 只允许 修改字段名和描述
		if (old.getMetadataSys().equals(BoolEnum.YES.getCode())) {
			if (!ObjectUtil.equal(old.getMetadataLength(), dynamicBusinessModelDefineDTO.getMetadataLength())
					|| !old.getMetadataType().equals(dynamicBusinessModelDefineDTO.getMetadataType())) {
				throw new ArchiveRuntimeException("系统字段只许修改字段名称和描述，其余字段不可修改！！！");
			}
		}
		//所有字段不允许修改模板类型
		if (!old.getModelType().equals(dynamicBusinessModelDefineDTO.getModelType())) {
			throw new ArchiveRuntimeException("不允许修改模板类型！！！");
		}
		BusinessModelDefine businessModelDefine = new BusinessModelDefine();
		BeanUtil.copyProperties(dynamicBusinessModelDefineDTO, businessModelDefine);
		businessModelDefine.setMetadataEnglish(old.getMetadataEnglish());
		businessModelDefine.setId(dynamicBusinessModelDefineDTO.getDefinedId());
		//先修改动态表里的字段
		CesCloudUser user = SecurityUtils.getUser();
		Long tenantId = user.getTenantId();
		R<Boolean> result = remoteDynamicBasicService.modifyColumn(tenantId, old.getModelType(),dynamicBusinessModelDefineDTO.getModelCode(), businessModelDefine);
		if (result.getCode() == CommonConstants.FAIL || result.getData() == false) {
			log.error("修改业务表字段失败");
			throw new ArchiveBusinessException("修改业务表字段失败");
		}

		businessModelDefineService.updateById(businessModelDefine);

		//TODO 刷新 redis
		// 修改不允许修改字段

		// 更新是更新 apma_business_model_define 不需要更新关系表
		return true;
	}

	private DynamicModelDefine getDynamicModelDefine(DynamicBusinessModelDefineDTO dynamicBusinessModelDefineDTO,Long defineId){
		Long tenantId = SecurityUtils.getUser().getTenantId();
		DynamicModelDefine dynamicModelDefine=new DynamicModelDefine();
		BeanUtil.copyProperties(dynamicBusinessModelDefineDTO, dynamicModelDefine);
		// 设置定义表ID
		if(!Objects.isNull(defineId)){
			// 修改不需要更新
			dynamicModelDefine.setDefinedId(defineId);
		}

		dynamicModelDefine.setTenantId(tenantId);

		return dynamicModelDefine;
	}

	@Override
	public boolean deleteDynamicModelDefineById(Long id) throws ArchiveBusinessException {
		DynamicModelDefine dynamicModelDefine=this.getById(id);
		if(Objects.isNull(dynamicModelDefine)){
			throw new ArchiveBusinessException("删除业务表字段失败,未找到对应关系");
		}
		BusinessModelDefine businessModelDefine=businessModelDefineService.getById(dynamicModelDefine.getDefinedId());
		if(Objects.isNull(businessModelDefine)){
			throw new ArchiveBusinessException("删除业务表字段失败,未找到定义字段");
		}
		R<Boolean> defineResult=remoteDynamicBasicService.dropColumn(dynamicModelDefine.getTenantId(),dynamicModelDefine.getModelType(),dynamicModelDefine.getModelCode(),businessModelDefine);

//		boolean defineResult=businessModelDefineService.deleteBusinessModelDefineById(dynamicModelDefine.getDefinedId());
//
		if(!defineResult.getData()){
			throw new ArchiveBusinessException("删除业务表字段失败");
		}

		// 删除不用刷新
		return this.removeById(id);
	}

	/**
	 * 新建专题分类时先从模板表中复制模板字段到表字段中
	 * 然后绑定关系，再调用 managment的动态表创建方法
	 *
	 * @param code
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean createTable(String code,Integer modelType,String fondsCode){
		//获取用户所属租户信息
		Long tenantId = SecurityUtils.getUser().getTenantId();
		// 复制模板至  businessModelDefine
		List<BusinessModelDefine>  businessModelDefineList=businessModelDefineService.getBusinessModelTemplateListByModelType(modelType);
		if(CollectionUtil.isEmpty(businessModelDefineList)){
			return false;
		}
//		ModelTypeEnum modelTypeEnum=ModelTypeEnum.getEnum(modelType);
		//  因需获取ID，只能低效逐条复制 （sqlserver 批量插入会无法获取插入ID by 李蔚告诉王谷华）
		// 本操作很低效，但因为非常用功能且正常快速完成，无需优化
		businessModelDefineList.forEach(businessModelDefine->{
			// 保存定义表
			businessModelDefineService.save(businessModelDefine);

			DynamicModelDefine dynamicModelDefine=new DynamicModelDefine();
			BeanUtil.copyProperties(businessModelDefine, dynamicModelDefine);
			// 设置定义表ID
			dynamicModelDefine.setDefinedId(businessModelDefine.getId());
			dynamicModelDefine.setTenantId(tenantId);
			dynamicModelDefine.setFondsCode(fondsCode);

			dynamicModelDefine.setModelCode(code);

			// 保存绑定关系
			this.save(dynamicModelDefine);

		});

		// 返回调用 management 创建表
		remoteDynamicBasicService.createTable(tenantId,modelType,code,businessModelDefineList);




		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean dropTable(String code, Integer modelType) {
		//  清空表定义字段
		businessModelDefineService.deleteByDynamicModel(modelType,code);
		// 删除绑定关系
		Long tenantId = SecurityUtils.getUser().getTenantId();
		this.baseMapper.deleteByTypeAndCode(modelType,code,tenantId);
		//  调用 management 删除表 由于是DDL删表，出错。。。。。 -by 王谷华
		remoteDynamicBasicService.dropTable(tenantId,modelType,code);

		return true;
	}

	@Override
	public List<DynamicBusinessModelDefineDTO> getAllBusinessModelDefines(Integer modelType, String modelCode) {
		return this.baseMapper.getBusinessModelDefinesByDynamic(modelType, modelCode, SecurityUtils.getUser().getTenantId(), null);
	}


}
