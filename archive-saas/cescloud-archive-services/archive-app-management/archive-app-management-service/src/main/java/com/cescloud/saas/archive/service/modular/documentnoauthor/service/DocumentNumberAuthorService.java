package com.cescloud.saas.archive.service.modular.documentnoauthor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.documentnoauthor.dto.DocumentNumberAuthorDTO;
import com.cescloud.saas.archive.api.modular.documentnoauthor.entity.DocumentNumberAuthor;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.List;

/**
 * 文号责任者定义Service
 *
 * @author LS
 * @date 2021/6/23
 */
public interface DocumentNumberAuthorService extends IService<DocumentNumberAuthor> {
	/**
	 * 分页查询文号责任者信息
	 *
	 * @param page    分页对象
	 * @param keyword 查询参数
	 * @return IPage<DocumentNumberAuthor>
	 */
	IPage<DocumentNumberAuthor> getPage(Page<DocumentNumberAuthor> page, String keyword);

	/**
	 * 新增文号责任者信息
	 *
	 * @param documentNumberAuthor 新增对象
	 * @return Boolean
	 * @throws ArchiveBusinessException 文号责任者重复异常
	 */
	Boolean saveDocumentNumberAuthor(DocumentNumberAuthorDTO documentNumberAuthor) throws ArchiveBusinessException;

	/**
	 * 修改文号责任者信息
	 *
	 * @param documentNumberAuthor 修改对象
	 * @return Boolean
	 * @throws ArchiveBusinessException 文号责任者重复异常
	 */
	Boolean updateDocumentNumberAuthorById(DocumentNumberAuthorDTO documentNumberAuthor) throws ArchiveBusinessException;

	/**
	 * 根据全宗号，文号查询责任者
	 *
	 * @param fondsCode      全宗号
	 * @param documentNumber 文号
	 * @return DocumentNumberAuthor
	 */
	String getByFondsCodeAndDocumentNumber(String fondsCode, String documentNumber);

	/**
	 * 根据ids批量删除文号责任者信息
	 *
	 * @param documentNumberAuthorIds 待删除的ids
	 * @return Boolean
	 */
	Boolean removeDocumentNumberAuthorByIds(List<Long> documentNumberAuthorIds);
}
