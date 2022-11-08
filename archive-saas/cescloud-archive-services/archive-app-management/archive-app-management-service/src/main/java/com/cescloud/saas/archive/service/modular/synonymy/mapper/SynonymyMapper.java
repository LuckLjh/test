
package com.cescloud.saas.archive.service.modular.synonymy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.archivetype.dto.SourceDTO;
import com.cescloud.saas.archive.api.modular.synonymy.entity.Synonymy;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 同义词
 *
 * @author liwei
 * @date 2019-04-09 13:03:31
 */
public interface SynonymyMapper extends BaseMapper<Synonymy> {

    List<Synonymy> search(@Param("words") List<String> words);

}
