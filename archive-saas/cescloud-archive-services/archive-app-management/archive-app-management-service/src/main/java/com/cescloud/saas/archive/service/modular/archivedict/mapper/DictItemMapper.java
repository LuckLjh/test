
package com.cescloud.saas.archive.service.modular.archivedict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.archivedict.dto.DictItemDTO;
import com.cescloud.saas.archive.api.modular.archivedict.entity.DictItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 数据字典值
 *
 * @author liudong1
 * @date 2019-03-18 17:47:15
 */
public interface DictItemMapper extends BaseMapper<DictItem> {

	List<DictItemDTO> exportExcel();

    List<DictItem> getItemListByDictCodeRel(@Param("dictCode") String dictCode, @Param("typeCode") String typeCode);
}
