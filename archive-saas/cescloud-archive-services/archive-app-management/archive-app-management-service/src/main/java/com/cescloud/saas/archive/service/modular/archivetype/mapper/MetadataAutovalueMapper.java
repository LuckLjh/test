
package com.cescloud.saas.archive.service.modular.archivetype.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.archivetype.dto.AutovalueDTO;
import com.cescloud.saas.archive.api.modular.archivetype.entity.MetadataAutovalue;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 元数据字段自动赋值
 *
 * @author liwei
 * @date 2019-04-15 15:16:12
 */
public interface MetadataAutovalueMapper extends BaseMapper<MetadataAutovalue> {

	List<AutovalueDTO> getMetaDataAutovalues(@Param("storageLocate") String storageLocate);

}
