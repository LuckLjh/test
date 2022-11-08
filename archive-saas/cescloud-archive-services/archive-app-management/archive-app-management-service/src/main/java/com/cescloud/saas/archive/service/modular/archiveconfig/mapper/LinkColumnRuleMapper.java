
package com.cescloud.saas.archive.service.modular.archiveconfig.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedColumnRuleMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.LinkColumnRule;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 挂接字段组成规则
 *
 * @author liudong1
 * @date 2019-05-14 11:15:33
 */
public interface LinkColumnRuleMapper extends BaseMapper<LinkColumnRule> {

	List<DefinedColumnRuleMetadata> selectDefinedMetadata(@Param("storageLocate") String storageLocate, @Param("linkLayerId") Long linkLayerId);

	List<DefinedColumnRuleMetadata> selectUnDefinedMetadata(@Param("storageLocate") String storageLocate, @Param("linkLayerId") Long linkLayerId);
}
