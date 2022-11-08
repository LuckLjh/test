package com.cescloud.saas.archive.service.modular.notice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.notice.dto.NoticeDto;
import com.cescloud.saas.archive.api.modular.notice.entity.Notice;
import org.apache.ibatis.annotations.Param;

/**
 * @author LiShuai
 * @Description: 通知公告管理
 * @date 2020/9/28
 */
public interface NoticeMapper extends BaseMapper<Notice> {

	NoticeDto getNoticeById(@Param("noticeId") Long noticeId);
}
