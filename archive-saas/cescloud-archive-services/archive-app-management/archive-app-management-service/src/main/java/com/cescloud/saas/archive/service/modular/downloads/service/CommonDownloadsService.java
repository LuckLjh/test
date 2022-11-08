package com.cescloud.saas.archive.service.modular.downloads.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.downloads.dto.CommonDownloadsDTO;
import com.cescloud.saas.archive.api.modular.downloads.entity.CommonDownloads;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import org.springframework.web.multipart.MultipartFile;

/**
 * 常用下载Service
 *
 * @author LS
 * @Date 2021/3/8
 */
public interface CommonDownloadsService extends IService<CommonDownloads> {

	/**
	 * 分页查询 常用下载列表
	 *
	 * @param page    分页对象
	 * @param keyword 搜索关键字
	 * @return IPage<CommonDownloads>
	 */
	IPage<CommonDownloads> getPage(Page<CommonDownloads> page, String keyword);

	/**
	 * 上传常用下载文件
	 *
	 * @param file 上传文件
	 * @return Boolean
	 * @throws ArchiveBusinessException 上传文件异常
	 */
	Boolean upload(MultipartFile file) throws ArchiveBusinessException;

	/**
	 * 删除常用下载文件
	 *
	 * @param id 常用下载文件id
	 * @return Boolean
	 * @throws ArchiveBusinessException 删除文件异常
	 */
	Boolean deleteById(Long id) throws ArchiveBusinessException;

	/**
	 * 修改常用下载
	 *
	 * @param commonDownloads 常用下载
	 * @return Boolean
	 */
	Boolean updateById(CommonDownloadsDTO commonDownloads);

	/**
	 * 下载电子文件
	 *
	 * @param id       文件id
	 * @throws ArchiveBusinessException 下载文件异常
	 */
	String download(Long id) throws Exception;

	/**
	 * 首页获取缀常用下载数据
	 * @param size
	 * @param tenantId
	 * @return
	 */
	IPage<CommonDownloads> getMainPage(Long size);
}
