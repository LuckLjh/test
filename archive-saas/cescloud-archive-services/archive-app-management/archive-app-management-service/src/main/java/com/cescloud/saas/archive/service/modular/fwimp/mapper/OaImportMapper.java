
package com.cescloud.saas.archive.service.modular.fwimp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.fwimp.entity.OaImport;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;


/**
 * oa 导入
 *
 * @author hyq
 * @date 2019-03-21 12:04:54
 */
public interface OaImportMapper extends BaseMapper<OaImport> {
	/**
	 * 表配置,字段匹配键值对
	 *
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public List<HashMap<String,Object>> getRelevance(@Param("tableName") String tableName, @Param("targetFieldName") String  targetFieldName, @Param("relevanceFieldName") String  relevanceFieldName);
}
