package com.cescloud.saas.archive.service.modular.notice.service.helper;

import com.cescloud.saas.archive.api.modular.notice.entity.Notice;
import com.cescloud.saas.archive.service.modular.businessconfig.async.AsyncUpdateFieldConfiguration;
import com.cescloud.saas.archive.service.modular.notice.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author LiShuai
 * @Description:
 * @date 2020/10/13
 */
@Lazy
@Component
@Slf4j
public class NoticeServiceHelper {

	@Autowired
	private NoticeService noticeService;


	@Async(AsyncUpdateFieldConfiguration.ASYNC_EXECUTOR_NAME)
	public void updateNumberOfHits(Long noticeId) {
		final Notice notice = noticeService.getById(noticeId);
		notice.setNumberOfHits(notice.getNumberOfHits() + 1);
		noticeService.updateById(notice);
	}
}
