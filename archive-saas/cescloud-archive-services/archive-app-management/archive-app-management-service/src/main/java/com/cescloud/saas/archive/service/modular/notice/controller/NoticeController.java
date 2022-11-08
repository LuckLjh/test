package com.cescloud.saas.archive.service.modular.notice.controller;

import com.cescloud.saas.archive.api.modular.notice.dto.NoticeDto;
import com.cescloud.saas.archive.api.modular.notice.entity.Notice;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.notice.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @author LiShuai
 * @Description: 通知公告管理
 * @date 2020/9/28
 */
@Api(value = "notice", tags = "通知公告管理")
@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/notice")
public class NoticeController {

	private final NoticeService noticeService;

	/**
	 * 首页公告分页查询
	 * @param current    当前页
	 * @param size    每页显示条数
	 * @param titleProper 标题
	 * @return R
	 */
	@ApiOperation(value = "分页查询", httpMethod = "GET")
	@GetMapping("/main/page")
	public R getMainNoticePage(@RequestParam("current") @ApiParam(value = "当前页", name = "current", required = true) @NotNull(message = "当前页不能为空") long current,
						   @RequestParam("size") @ApiParam(value = "每页显示条数", name = "size", required = true) @NotNull(message = "每页显示条数不能为空") long size,
						   @RequestParam(value = "titleProper", required = false) @ApiParam(value = "每页显示条数", name = "titleProper", required = false) String titleProper) {
		return new R<>(noticeService.getMainPage(current,size,titleProper));
	}

	/**
	 * 系统公告分页查询
	 * @param current    当前页
	 * @param size    每页显示条数
	 * @param titleProper 标题
	 * @return R
	 */
	@ApiOperation(value = "分页查询", httpMethod = "GET")
	@GetMapping("/page")
	public R getNoticePage(@RequestParam("current") @ApiParam(value = "当前页", name = "current", required = true) @NotNull(message = "当前页不能为空") long current,
	                       @RequestParam("size") @ApiParam(value = "每页显示条数", name = "size", required = true) @NotNull(message = "每页显示条数不能为空") long size,
	                       @RequestParam(value = "titleProper", required = false) @ApiParam(value = "标题", name = "titleProper", required = false) String titleProper) {
		return new R<>(noticeService.getPage(current,size,titleProper));
	}

	/**
	 * 通过id查询通知公告
	 * @param noticeId id
	 * @return R
	 */
	@ApiOperation(value = "通过id查询公告", httpMethod = "GET")
	@GetMapping("/{noticeId}")
	public R getById(@PathVariable("noticeId") @ApiParam(value = "公告ID主键" ,name = "noticeId") @NotNull(message = "公告ID不能为空") Long noticeId) throws ArchiveBusinessException {
		return new R<>(noticeService.getNoticeById(noticeId));
	}

	/**
	 * 新增通知公告
	 * @param notice 通知公告实体
	 * @return R
	 */
	@ApiOperation(value = "新增通知公告", httpMethod = "POST")
	@SysLog("新增通知公告")
	@PostMapping
	public R save(@RequestBody @ApiParam(value = "notice", name = "通知公告对象", required = true)@Valid Notice notice) throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("新增通知公告-标题【%s】",notice.getTitleProper()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(noticeService.saveNotice(notice));
	}

	/**
	 * 公告内容上传文件
	 * @param file 文件
	 * @return R
	 */
	@ApiOperation(value = "公告内容上传文件", httpMethod = "POST")
	@SysLog("公告内容上传文件")
	@PostMapping("/upload")
	public R uploadFile( @RequestParam("file") @ApiParam(value = "公告静态文件", name = "file", required = true) MultipartFile file) throws Exception {
		try {
			SysLogContextHolder.setLogTitle(String.format("公告内容上传文件-文件名称【%s】",file.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(noticeService.uploadFile(file));
	}

	/**
	 * 修改通知公告
	 * @param notice 通知公告实体
	 * @return R
	 */
	@ApiOperation(value = "修改通知公告", httpMethod = "PUT")
	@SysLog("修改通知公告")
	@PutMapping
	public R updateById(@RequestBody @ApiParam(value = "notice", name = "通知公告对象", required = true)@Valid Notice notice) throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("修改通知公告-标题【%s】",notice.getTitleProper()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(noticeService.updateNotice(notice));
	}

	/**
	 * 通过id删除通知公告
	 * @param noticeId id
	 * @return R
	 */
	@ApiOperation(value = "删除通知公告", httpMethod = "DELETE")
	@SysLog("删除通知公告")
	@DeleteMapping("/{noticeId}")
	public R removeById(@PathVariable @ApiParam(value = "noticeId" ,name = "通知公告id",required = true) @NotNull(message = "通知公告id不能为空") Long noticeId) throws ArchiveBusinessException {
		try {
			NoticeDto noticeById = noticeService.getNoticeById(noticeId);
			SysLogContextHolder.setLogTitle(String.format("删除通知公告-标题【%s】",noticeById.getTitleProper()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(noticeService.removeNoticeById(noticeId));
	}

	/**
	 * 发布通知公告
	 * @param noticeId id
	 * @return R
	 */
	@ApiOperation(value = "发布通知公告", httpMethod = "PUT")
	@SysLog("发布通知公告")
	@PutMapping("/release/{noticeId}")
	public R releaseNotice(@PathVariable @ApiParam(value = "noticeId" ,name = "通知公告id",required = true) @NotNull(message = "通知公告id不能为空") Long noticeId) {
		try {
			NoticeDto noticeById = noticeService.getNoticeById(noticeId);
			SysLogContextHolder.setLogTitle(String.format("发布通知公告-标题【%s】",noticeById.getTitleProper()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(noticeService.releaseNotice(noticeId));
	}

	/**
	 * 撤销通知公告
	 * @param noticeId id
	 * @return R
	 */
	@ApiOperation(value = "撤销通知公告", httpMethod = "PUT")
	@SysLog("撤销通知公告")
	@PutMapping("/revocation/{noticeId}")
	public R revocationNotice(@PathVariable @ApiParam(value = "noticeId" ,name = "通知公告id",required = true) @NotNull(message = "通知公告id不能为空") Long noticeId) {
		try {
			NoticeDto noticeById = noticeService.getNoticeById(noticeId);
			SysLogContextHolder.setLogTitle(String.format("撤销通知公告-标题【%s】",noticeById.getTitleProper()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(noticeService.revocationNotice(noticeId));
	}

	/**
	 * 阅读通知公告
	 * @param noticeId id
	 * @return R
	 */
	@ApiOperation(value = "阅读通知公告", httpMethod = "PUT")
	@SysLog("阅读通知公告")
	@PutMapping("/read/{noticeId}")
	public R readNotice(@PathVariable @ApiParam(value = "noticeId" ,name = "通知公告id",required = true) @NotNull(message = "通知公告id不能为空") Long noticeId) throws ArchiveBusinessException {
		try {
			NoticeDto noticeById = noticeService.getNoticeById(noticeId);
			SysLogContextHolder.setLogTitle(String.format("阅读通知公告-标题【%s】",noticeById.getTitleProper()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(noticeService.readNotice(noticeId));
	}

}
