
package com.cescloud.saas.archive.service.modular.archiveconfig.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedColumnRuleMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveColumnRule;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 档案字段组成规则
 *
 * @author liudong1
 * @date 2019-04-19 15:06:53
 */
public interface ArchiveColumnRuleMapper extends BaseMapper<ArchiveColumnRule> {

	List<DefinedColumnRuleMetadata> selectDefinedMetadata(@Param("metadataSourceId") Long metadataSourceId);

	List<DefinedColumnRuleMetadata> selectUnDefinedMetadata(@Param("storageLocate") String storageLocate, @Param("metadataSourceId") Long metadataSourceId);

	List<DefinedColumnRuleMetadata> selectUnDefinedEdit(@Param("storageLocate") String storageLocate, @Param("metadataSourceId") Long metadataSourceId,@Param("metadataId") Long metadataId,@Param("moduleId") Long moduleId);
}
