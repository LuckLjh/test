
package com.cescloud.saas.archive.service.modular.archivedict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.archivedict.entity.Dict;


/**
 * 数据字典项
 *
 * @author liudong1
 * @date 2019-03-18 17:44:09
 */
public interface DictMapper extends BaseMapper<Dict> {

	public Integer getMaxCodeNoByCode(String dictCode);
}
