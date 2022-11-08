
package com.cescloud.saas.archive.service.modular.archiveconfig.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedListMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedListTag;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.TagList;

import java.util.List;


/**
 * 标签列表配置
 *
 * @author liudong1
 * @date 2019-05-27 15:20:07
 */
public interface TagListMapper extends BaseMapper<TagList> {

	List<DefinedListTag> listOfDefined();

	List<DefinedListTag> listOfUnDefined();

}
