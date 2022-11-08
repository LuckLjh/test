package com.cescloud.saas.archive.service.modular.businessconfig.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.businessconfig.entity.BusinessModelDefine;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author liwei
 */
public interface BusinessModelDefineMapper extends BaseMapper<BusinessModelDefine> {

	/**
	 *  查询字段英文名称编号最大值
	 * @param metadataEnglish
	 * @param tenantIds
	 * @return
	 */
	Integer getMaxEnglishNoByHidden(@Param("metadataEnglish") String metadataEnglish, @Param("tenantIds") List<Long> tenantIds);

	/**
	 * 从模板表（apma_business_model_template）中获取所有的记录
	 * @return
	 */
	List<BusinessModelDefine> getBusinessModelTemplateList();


	/**
	 * 根据 modelType 从模板表（apma_business_model_template）中获取所有的记录
	 * 王谷华 2021-03-31 为将雪虎 BusinessModelDefineServiceImpl createTable 做区分而做
	 * @param modelType
	 * @return
	 */
	List<BusinessModelDefine> getBusinessModelTemplateListByModelType(@Param("modelType")Integer modelType);


	/**
	 * 根据动态表参数删除表定义
	 * @param modelType
	 * @param modelCode
	 * @return
	 */
	int deleteByDynamicModel(@Param("modelType")Integer modelType,@Param("modelCode")String modelCode);
}
