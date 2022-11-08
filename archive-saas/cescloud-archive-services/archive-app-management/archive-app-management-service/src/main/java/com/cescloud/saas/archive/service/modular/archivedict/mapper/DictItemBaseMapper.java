
package com.cescloud.saas.archive.service.modular.archivedict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.archivedict.entity.DictItemBase;
import com.cescloud.saas.archive.api.modular.archivetype.dto.SystemColumn;
import org.apache.ibatis.annotations.Param;


/**
 * 基础数据字典项
 *
 * @author liudong1
 * @date 2019-09-14 19:33:15
 */
public interface DictItemBaseMapper extends BaseMapper<DictItemBase> {

	void insertIntoDictItemFromBase(@Param("systemColumn") SystemColumn systemColumn);
}
