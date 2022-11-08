package com.cescloud.saas.archive.service.modular.documentnoauthor.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.cescloud.saas.archive.api.modular.documentnoauthor.dto.DocumentNumberAuthorDTO;
import com.cescloud.saas.archive.api.modular.documentnoauthor.entity.DocumentNumberAuthor;
import com.cescloud.saas.archive.api.modular.fonds.entity.Fonds;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.documentnoauthor.mapper.DocumentNumberAuthorMapper;
import com.cescloud.saas.archive.service.modular.documentnoauthor.service.DocumentNumberAuthorService;
import com.cescloud.saas.archive.service.modular.fonds.service.FondsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文号责任者定义Service实现
 *
 * @author LS
 * @date 2021/6/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentNumberAuthorServiceImpl extends ServiceImpl<DocumentNumberAuthorMapper, DocumentNumberAuthor> implements DocumentNumberAuthorService {

	private final FondsService fondsService;


	@Override
	public IPage<DocumentNumberAuthor> getPage(Page<DocumentNumberAuthor> page, String keyword) {
		LambdaQueryWrapper<DocumentNumberAuthor> wrapper = new LambdaQueryWrapper<>();
		List<String> fondCodes = fondsService.getFondsList().stream().map(Fonds::getFondsCode).collect(Collectors.toList());
		if (CollUtil.isNotEmpty(fondCodes)) {
			wrapper.in(DocumentNumberAuthor::getFondsCode, fondCodes);
		}
		if (StrUtil.isNotBlank(keyword)) {
			String trimKeyword = StrUtil.trimToEmpty(keyword);
			wrapper.and(w -> w.like(DocumentNumberAuthor::getDocumentNumber, trimKeyword).or()
					.like(DocumentNumberAuthor::getFondsName, trimKeyword).or()
					.like(DocumentNumberAuthor::getAuthor, trimKeyword));
		}
		return this.page(page, wrapper);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveDocumentNumberAuthor(DocumentNumberAuthorDTO documentNumberAuthorDTO) throws ArchiveBusinessException {
		Integer isExist = baseMapper.isExistDocumentNumberAuthor(documentNumberAuthorDTO.getFondsCode(), documentNumberAuthorDTO.getDocumentNumber(), null);
		if (SqlHelper.retBool(isExist)) {
			throw new ArchiveBusinessException("所选全宗下文号已存在责任者");
		}
		DocumentNumberAuthor documentNumberAuthor = new DocumentNumberAuthor();
		BeanUtil.copyProperties(documentNumberAuthorDTO, documentNumberAuthor);
		return this.save(documentNumberAuthor);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean updateDocumentNumberAuthorById(DocumentNumberAuthorDTO documentNumberAuthorDTO) throws ArchiveBusinessException {
		Integer isExist = baseMapper.isExistDocumentNumberAuthor(documentNumberAuthorDTO.getFondsCode(), documentNumberAuthorDTO.getDocumentNumber(), documentNumberAuthorDTO.getId());
		if (SqlHelper.retBool(isExist)) {
			throw new ArchiveBusinessException("所选全宗下文号已存在责任者");
		}
		DocumentNumberAuthor documentNumberAuthor = new DocumentNumberAuthor();
		BeanUtil.copyProperties(documentNumberAuthorDTO, documentNumberAuthor);
		return this.updateById(documentNumberAuthor);
	}

	@Override
	public String getByFondsCodeAndDocumentNumber(String fondsCode, String documentNumber) {
		if (StrUtil.isBlank(fondsCode) || StrUtil.isBlank(documentNumber)) {
			return "";
		}
		try {
			documentNumber = URLDecoder.decode(documentNumber, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error("转义文号失败", e);
		}
		DocumentNumberAuthor documentNumberAuthor = this.getOne(Wrappers.<DocumentNumberAuthor>lambdaQuery()
				.eq(DocumentNumberAuthor::getFondsCode, fondsCode).eq(DocumentNumberAuthor::getDocumentNumber, documentNumber)
				.select(DocumentNumberAuthor::getAuthor));
		if (ObjectUtil.isNotNull(documentNumberAuthor)) {
			return documentNumberAuthor.getAuthor();
		}
		return "";
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean removeDocumentNumberAuthorByIds(List<Long> documentNumberAuthorIds) {
		return this.removeByIds(documentNumberAuthorIds);
	}
}
