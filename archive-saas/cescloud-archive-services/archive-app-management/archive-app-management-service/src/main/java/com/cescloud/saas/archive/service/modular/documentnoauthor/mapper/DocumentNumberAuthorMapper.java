package com.cescloud.saas.archive.service.modular.documentnoauthor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.documentnoauthor.entity.DocumentNumberAuthor;
import org.apache.ibatis.annotations.Param;

/**
 * 文号责任者定义Mapper
 *
 * @author LS
 * @date 2021/6/23
 */
public interface DocumentNumberAuthorMapper extends BaseMapper<DocumentNumberAuthor> {
	/**
	 * 根据全宗号和文号查询责任者是否存在
	 *
	 * @param id             主键id
	 * @param fondCode       全宗号
	 * @param documentNumber 文号
	 * @return Boolean
	 */
	Integer isExistDocumentNumberAuthor(@Param("fondCode") String fondCode, @Param("documentNumber") String documentNumber, @Param("id") Long id);
}
