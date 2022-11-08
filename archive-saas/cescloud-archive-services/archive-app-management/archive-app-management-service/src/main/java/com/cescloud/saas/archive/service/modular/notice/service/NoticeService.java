package com.cescloud.saas.archive.service.modular.notice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.notice.dto.NoticeDto;
import com.cescloud.saas.archive.api.modular.notice.entity.Notice;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 通知公告管理
 *
 * @author LiShuai
 * @date 2020/9/28
 */
public interface NoticeService extends IService<Notice> {
	/**
	 * 通知公告分页查询
	 * @param current 当前页
	 * @param size 每页大小
	 * @param titleProper 标题
	 * @return
	 */
	IPage<Notice> getPage(long current, long size, String titleProper);


	/**
	 * 新增通知公告
	 * @param notice 通知公告实体
	 * @return R
	 */
	R saveNotice(Notice notice) throws ArchiveBusinessException;

	/**
	 * 修改通知公告
	 * @param notice 通知公告实体
	 * @return R
	 */
	R updateNotice(Notice notice) throws ArchiveBusinessException;

	/**
	 * 通过id删除通知公告
	 * @param noticeId id
	 * @return R
	 */
	R removeNoticeById(Long noticeId) throws ArchiveBusinessException;

	/**
	 * 发布通知公告
	 * @param noticeId id
	 * @return R
	 */
	R releaseNotice(Long noticeId);

	/**
	 * 撤销通知公告
	 * @param noticeId id
	 * @return R
	 */
	R revocationNotice(Long noticeId);

	/**
	 * 通过id查询通知公告
	 * @param noticeId id
	 * @return R
	 */
	NoticeDto getNoticeById(Long noticeId) throws ArchiveBusinessException;

	/**
	 * 阅读通知公告
	 * @param noticeId id
	 * @return R
	 */
	NoticeDto readNotice(Long noticeId) throws ArchiveBusinessException;

	/**
	 * 文件上传
	 * @param file 文件
	 * @return R 文件地址
	 */
	Map<String,Object> uploadFile(MultipartFile file) throws Exception;

	/**
	 * 通知公告首页分页查询
	 * @param current 当前页
	 * @param titleProper 标题
	 * @param size  每页数量
	 * @return IPage<Notice>
	 */
	IPage<Notice> getMainPage(long current, long size, String titleProper);
}
