
package com.cescloud.saas.archive.service.modular.filingscope.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.filingscope.dto.FilingScopeDTO;
import com.cescloud.saas.archive.api.modular.filingscope.entity.FilingScope;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 归档范围定义
 *
 * @author xieanzhu
 * @date 2019-04-22 15:45:22
 */
public interface FilingScopeMapper extends BaseMapper<FilingScope> {
	List<FilingScope> getFondsGroupByParentId(@Param("parentId")Long parentId, @Param("fondsCodes")List<String> fondsCodes);

	Integer selectMaxSortNo(@Param("parentClassId") Long parentClassId);

	void  updateArchiveFilingScopeTree(@Param("fondsName") String fondsName , @Param("fondsCode") String fondsCode);

	List<FilingScopeDTO> getAllFilingScopeList(@Param("fondsCode") String fondsCode, @Param("tenantId") Long tenantId);
}
