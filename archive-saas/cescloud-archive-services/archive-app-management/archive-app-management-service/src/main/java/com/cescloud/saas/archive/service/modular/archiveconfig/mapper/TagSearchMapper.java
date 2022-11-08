
package com.cescloud.saas.archive.service.modular.archiveconfig.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedSearchMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedSearchTag;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.TagSearch;

import java.util.List;


/**
 * 标签检索配置
 *
 * @author liudong1
 * @date 2019-05-27 15:55:01
 */
public interface TagSearchMapper extends BaseMapper<TagSearch> {

	List<DefinedSearchTag> listOfDefined();

	List<DefinedSearchTag> listOfUnDefined();

}
