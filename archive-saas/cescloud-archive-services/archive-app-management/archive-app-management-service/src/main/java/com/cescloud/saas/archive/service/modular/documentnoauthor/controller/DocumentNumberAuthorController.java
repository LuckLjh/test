package com.cescloud.saas.archive.service.modular.documentnoauthor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.api.modular.documentnoauthor.dto.DocumentNumberAuthorDTO;
import com.cescloud.saas.archive.api.modular.documentnoauthor.entity.DocumentNumberAuthor;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.documentnoauthor.service.DocumentNumberAuthorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文号责任者定义Controller
 *
 * @author LS
 * @date 2021/6/23
 */
@Api(value = "documentNumberAuthor", tags = "文号责任者定义")
@Validated
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/document-number-author")
public class DocumentNumberAuthorController {

	private final DocumentNumberAuthorService documentNumberAuthorService;


	@ApiOperation(value = "文号责任者的分页查询", httpMethod = "GET")
	@GetMapping("/page")
	public R<IPage<DocumentNumberAuthor>> getDocumentNumberAuthorPage(@ApiParam(value = "分页对象", name = "page", required = true) Page<DocumentNumberAuthor> page,
	                                                               @ApiParam(value = "查询关键字", name = "keyword") String keyword) {
		return new R<>(documentNumberAuthorService.getPage(page, keyword));
	}

	@ApiOperation(value = "根据id查询文号责任者", httpMethod = "GET")
	@GetMapping("/{id}")
	public R<DocumentNumberAuthor> getById(@PathVariable("id") @ApiParam(value = "传入文号责任者主键", name = "id", required = true) @NotNull(message = "文号责任者id不能为空") Long documentNumberAuthorId) {
		return new R<>(documentNumberAuthorService.getById(documentNumberAuthorId));
	}


	@ApiOperation(value = "根据全宗号、文号查询责任者", httpMethod = "GET")
	@GetMapping
	public R<String> getByFondsCodeAndDocumentNumber(@RequestParam(value = "fondsCode",required = false) @ApiParam(value = "全宗号", name = "fondsCode") String fondsCode,
	                                                            @RequestParam(value = "documentNumber",required = false) @ApiParam(value = "文号", name = "documentNumber") String documentNumber) {
		return new R<>(documentNumberAuthorService.getByFondsCodeAndDocumentNumber(fondsCode,documentNumber));
	}

	@ApiOperation(value = "新增文号责任者信息", httpMethod = "POST")
	@SysLog("新增文号责任者信息")
	@PostMapping
	public R<Boolean> save(@RequestBody @ApiParam(value = "传入json格式", name = "文号责任者对象", required = true) @Valid DocumentNumberAuthorDTO documentNumberAuthor) throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("新增文号责任者信息-全宗名称【%s】-文号【%s】-责任者【%s】",documentNumberAuthor.getFondsName(),documentNumberAuthor.getDocumentNumber(),documentNumberAuthor.getAuthor()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(documentNumberAuthorService.saveDocumentNumberAuthor(documentNumberAuthor));
	}

	@ApiOperation(value = "修改文号责任者", httpMethod = "PUT")
	@SysLog("修改文号责任者信息")
	@PutMapping
	public R<Boolean> updateById(@RequestBody @ApiParam(value = "传入json格式", name = "更新文号责任者实体", required = true) @Valid DocumentNumberAuthorDTO documentNumberAuthor) throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("修改文号责任者信息-全宗名称【%s】-文号【%s】-责任者【%s】",documentNumberAuthor.getFondsName(),documentNumberAuthor.getDocumentNumber(),documentNumberAuthor.getAuthor()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(documentNumberAuthorService.updateDocumentNumberAuthorById(documentNumberAuthor));
	}

	@ApiOperation(value = "删除文号责任者", httpMethod = "DELETE")
	@SysLog("删除文号责任者信息")
	@DeleteMapping
	public R<Boolean> removeById(@RequestBody @ApiParam(value = "文号责任者主键id", name = "documentNumberAuthorId", required = true) @NotNull(message = "文号责任者id不能为空") List<Long> documentNumberAuthorIds) {
		try {
			List<DocumentNumberAuthor> documentNumberAuthors = documentNumberAuthorService.listByIds(documentNumberAuthorIds);
		SysLogContextHolder.setLogTitle(String.format("删除文号责任者信息-全宗名称【%s】-文号【%s】-责任者【%s】", documentNumberAuthors.stream().map(DocumentNumberAuthor::getFondsName).collect(Collectors.toList()).toString(),documentNumberAuthors.stream().map(DocumentNumberAuthor::getDocumentNumber).collect(Collectors.toList()).toString(),documentNumberAuthors.stream().map(DocumentNumberAuthor::getAuthor).collect(Collectors.toList()).toString()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(documentNumberAuthorService.removeDocumentNumberAuthorByIds(documentNumberAuthorIds));
	}

}
