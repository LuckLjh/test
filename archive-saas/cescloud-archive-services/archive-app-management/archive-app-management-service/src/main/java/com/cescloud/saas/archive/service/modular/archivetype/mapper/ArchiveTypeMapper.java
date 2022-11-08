
package com.cescloud.saas.archive.service.modular.archivetype.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.archivetree.entity.ArchiveTree;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveType;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 档案门类
 *
 * @author liudong1
 * @date 2019-03-18 09:14:11
 */
public interface ArchiveTypeMapper extends BaseMapper<ArchiveType> {

	Integer getMaxCodeNoByCode(String typeCode);

	List<ArchiveTree> getRelationArchiveTreeNode(String typeCode);

	Integer getMaxSortNo();

	List<ArchiveType> getFondsGroupByParentId(@Param("parentId")Long parentId, @Param("fondsCodes")List<String> fondsCodes);

	List<String> getDistinctFondscode(@Param("fondsCodeList") List<String> fondsCodeList);

	List<ArchiveType>  getTypeNameByTableIds(@Param("tenantId") Long tenantId, @Param("ids") String ids);

	void  updateArchiveTypeTree(@Param("fondsName") String fondsName , @Param("fondsCode") String fondsCode);

	void  updateArchiveTree(@Param("fondsName") String fondsName , @Param("fondsCode") String fondsCode);


	List<ArchiveType> getFondsGroup(@Param("tenantId") Long tenantId, @Param("fondsCode")String fondsCode);



}
