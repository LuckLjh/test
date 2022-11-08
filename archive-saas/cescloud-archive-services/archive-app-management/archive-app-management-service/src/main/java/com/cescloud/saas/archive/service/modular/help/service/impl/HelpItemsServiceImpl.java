
package com.cescloud.saas.archive.service.modular.help.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.filecenter.entity.FileStorage;
import com.cescloud.saas.archive.api.modular.fonds.entity.Fonds;
import com.cescloud.saas.archive.api.modular.help.entity.HelpItems;

import com.cescloud.saas.archive.api.modular.storage.constants.StorageConstants;
import com.cescloud.saas.archive.common.constants.BoolEnum;
import com.cescloud.saas.archive.common.constants.FieldConstants;
import com.cescloud.saas.archive.service.modular.common.security.service.CesCloudUser;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.filecenter.service.FileStorageCommonService;
import com.cescloud.saas.archive.service.modular.fonds.service.FondsService;
import com.cescloud.saas.archive.service.modular.help.mapper.HelpItemsMapper;
import com.cescloud.saas.archive.service.modular.help.service.HelpItemsService;
import com.cescloud.saas.archive.service.modular.help.service.HelpUserShowService;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 全局数据权限
 *
 * @author zhaiyachao
 * @date 2021-05-11 18:32:44
 */
@Service
public class HelpItemsServiceImpl extends ServiceImpl<HelpItemsMapper, HelpItems> implements HelpItemsService {

	@Autowired
	private FondsService fondsService;
	@Autowired
	private HelpUserShowService helpUserShowService;
	@Autowired
	private FileStorageCommonService fileStorageCommonService;


	/**
	 * 列表加载
	 *
	 * @param menuId
	 * @param fondId
	 * @return
	 */
	@Override
	public List<HelpItems> getHelpList(Integer menuId, Long fondId) {
		List<HelpItems> list = Lists.newArrayList();
		if (null == fondId || null == menuId) return list;
		// 获取全宗
		Fonds fonds = this.fondsService.getFondsById(fondId);
		CesCloudUser user = SecurityUtils.getUser();
		if (ObjectUtil.isNotEmpty(fonds)) {
			// 默认为:0, 新增为:1, copy为:2
			// copy
			List<HelpItems> items = this.baseMapper.selectList(Wrappers.<HelpItems>lambdaQuery().eq(HelpItems::getHelpVersion, 2)
					.eq(HelpItems::getTenantId, user.getTenantId()).eq(HelpItems::getMenuId, menuId)
					.eq(HelpItems::getFondsCode, fonds.getFondsCode()).eq(HelpItems::getFondsName, fonds.getFondsName()));
			// 非默认
			List<HelpItems> itemsList = this.baseMapper.selectList(Wrappers.<HelpItems>lambdaQuery()
					.eq(HelpItems::getHelpVersion, 1)
					.eq(HelpItems::getTenantId, user.getTenantId())
					.eq(HelpItems::getFondsCode, fonds.getFondsCode())
					.eq(HelpItems::getFondsName, fonds.getFondsName())
					.eq(HelpItems::getMenuId, menuId));
			// 系统默认
			if (CollectionUtil.isEmpty(items)) {
				List<HelpItems> helpItems = this.baseMapper.selectList(Wrappers.<HelpItems>lambdaQuery()
						.eq(HelpItems::getHelpVersion, 0)
						.eq(HelpItems::getMenuId, menuId));
				list.addAll(helpItems);
			} else {
				list.addAll(items);
			}
			list.addAll(itemsList);
			// 排序
			list.stream().sorted(Comparator.comparing(item -> item.getOrderNo())).collect(Collectors.toList());

		}
		return list;
	}

	/**
	 * 排序
	 *
	 * @param fondId
	 * @param menuId
	 * @param fileIds 顺序格式(全部)
	 * @return
	 */
	@Override
	public Boolean updateDataSort(Long fondId, Integer menuId, List<Integer> fileIds) {
		if (null == fondId || null == menuId || CollectionUtil.isEmpty(fileIds)) return false;
		CesCloudUser user = SecurityUtils.getUser();
		Fonds fonds = this.fondsService.getFondsById(fondId);

		if (ObjectUtil.isNotEmpty(fonds)) {
			for (int i = 0; i < fileIds.size(); i++) {
				HelpItems items = new HelpItems();
				// 非默认
				HelpItems notDefault = this.baseMapper.selectOne(Wrappers.<HelpItems>lambdaQuery()
						.eq(HelpItems::getMenuId, menuId)
						.eq(HelpItems::getTenantId, user.getTenantId())
						.eq(HelpItems::getFondsCode, fonds.getFondsCode())
						.eq(HelpItems::getFileStorageId, fileIds.get(i))
						.in(HelpItems::getHelpVersion, Arrays.asList(1, 2)));
				// 默认数据
				HelpItems defaultItem = this.baseMapper.selectOne(Wrappers.<HelpItems>lambdaQuery()
						.eq(HelpItems::getMenuId, menuId)
						.eq(HelpItems::getFileStorageId, fileIds.get(i))
						.eq(HelpItems::getHelpVersion, 0));

				if (ObjectUtil.isNotEmpty(notDefault)) {
					BeanUtil.copyProperties(notDefault, items);
				} else {
					BeanUtil.copyProperties(defaultItem, items);
				}
				// 更新排序
				if (ObjectUtil.isNotEmpty(items)) {
					items.setOrderNo(i);
					this.baseMapper.updateById(items);
				}
			}
			return true;
		}
		return false;
	}


	/**
	 * 删除
	 *
	 * @param fondId
	 * @param menuId
	 * @param fileId
	 * @return
	 */
	@Override
	@SneakyThrows
	@Transactional
	public Boolean delDataByName(Long fondId, Integer menuId, Long fileId) {
		if (null == fondId || null == menuId || null == fileId) return false;
		Fonds fonds = this.fondsService.getFondsById(fondId);
		CesCloudUser user = SecurityUtils.getUser();
		if (ObjectUtil.isNotEmpty(fonds)) {
			Boolean isTrue = false;
			// 验证文件是否为系统默认
			HelpItems helpItems = this.baseMapper.selectOne(Wrappers.<HelpItems>lambdaQuery()
					.eq(HelpItems::getFileStorageId, fileId)
					.eq(HelpItems::getHelpVersion, 0));
			// 如果删除的文件是系统默认, 则copy到当前租户下
			if (ObjectUtil.isNotEmpty(helpItems)) {
				List<HelpItems> list = this.baseMapper.selectList(Wrappers.<HelpItems>lambdaQuery()
						.eq(HelpItems::getMenuId, menuId).eq(HelpItems::getHelpVersion, 0));
				// 排除当前删除的文件
				list.stream().filter(helpItem -> helpItem.getFileStorageId() != fileId).forEach(item -> {
					HelpItems help = new HelpItems();
					BeanUtil.copyProperties(item, help);
					help.setFondsCode(fonds.getFondsCode());
					help.setFondsName(fonds.getFondsName());
					help.setTenantId(user.getTenantId());
					help.setHelpVersion(2);
					help.setIsDelete(0);
					this.baseMapper.insert(help);
				});
				isTrue = true;
			}
			// 删除 非系统默认
			if (ObjectUtil.isEmpty(helpItems)) {
				this.baseMapper.delete(Wrappers.<HelpItems>lambdaQuery().in(HelpItems::getHelpVersion, Arrays.asList(1, 2))
						.eq(HelpItems::getMenuId, menuId).eq(HelpItems::getFondsCode, fonds.getFondsCode())
						.eq(HelpItems::getFileStorageId, fileId).eq(HelpItems::getTenantId, user.getTenantId()));
				// 删除非系统文件
				this.fileStorageCommonService.physicsDeleteFile(user.getTenantId(), fileId);
				isTrue = true;
			}
			return isTrue;
		}
		return false;
	}

	/**
	 * 恢复默认
	 *
	 * @param fondId
	 * @param menuId
	 * @return
	 */
	@Override
	@Transactional
	public Boolean recoverSystemDefault(Long fondId, Integer menuId) {
		if (null == fondId || null == menuId) return false;
		Fonds fonds = this.fondsService.getFondsById(fondId);
		CesCloudUser user = SecurityUtils.getUser();
		if (ObjectUtil.isNotEmpty(fonds)) {
			// 删除 非系统默认
			this.baseMapper.delete(Wrappers.<HelpItems>lambdaQuery().eq(HelpItems::getMenuId, menuId)
					.eq(HelpItems::getFondsCode, fonds.getFondsCode()).eq(HelpItems::getTenantId, user.getTenantId())
					.in(HelpItems::getHelpVersion, Arrays.asList(1, 2)));
			//  用户显示清空
			this.helpUserShowService.clearUserData(fonds.getFondsCode(), menuId);
			return true;
		}
		return false;
	}

	/**
	 * 获取当前map
	 *
	 * @param fondId
	 * @param menuId
	 * @param fileId
	 * @return v:true 查看 /false 不查看
	 */
	@Override
	public Map<String, Boolean> queryMap(Long fondId, Integer menuId, Integer fileId) {
		if (null == fondId || null == menuId || null == fileId) return null;
		HashMap<String, Boolean> map = Maps.newHashMap();
		CesCloudUser user = SecurityUtils.getUser();
		// 当前全宗
		Fonds fonds = this.fondsService.getFondsById(fondId);
		if (ObjectUtil.isNotEmpty(fonds)) {
			// 获取当前链接
			HelpItems helpItems = this.baseMapper.selectOne(Wrappers.<HelpItems>lambdaQuery()
					.eq(HelpItems::getTenantId, user.getTenantId()).eq(HelpItems::getMenuId, menuId)
					.eq(HelpItems::getFileStorageId, fileId).eq(HelpItems::getFondsCode, fonds.getFondsCode()));
			// 用户是否查看
			Boolean isMap = this.helpUserShowService.isData(fondId, menuId, user.getTenantId(), fonds.getFondsCode());
			if (ObjectUtil.isNotEmpty(helpItems)) {
				map.put(helpItems.getUrl(), isMap);
			}
		}
		return map;
	}


	private FileStorage getFileStorage(MultipartFile file, CesCloudUser user) {
		FileStorage fileStorage = new FileStorage();
		fileStorage.setName(file.getOriginalFilename());
		//源文件名称
		fileStorage.setParentPath(StorageConstants.OTHER_FILE_STORAGE);
		String fileName = file.getOriginalFilename();
		fileStorage.setFileType(FileUtil.extName(fileName));
		fileStorage.setParentId(1L);
		fileStorage.setTenantId(user.getTenantId());
		fileStorage.setFileSourceName(file.getOriginalFilename());
		fileStorage.setFileSize(file.getSize());
		fileStorage.setCreatedBy(user.getId());
		fileStorage.setCreatedTime(LocalDateTime.now());
		fileStorage.setContentType(ContentType.MULTIPART.getValue());
		return fileStorage;
	}

	/**
	 * 文件上传
	 *
	 * @param file
	 * @param fondId
	 * @param menuId
	 * @return
	 */
	@Override
	public Map<String, Object> uploadHelpMap(MultipartFile file, Long fondId, Integer menuId) {
		final Map<String, Object> map = CollectionUtil.newHashMap(6);
		if (StrUtil.isBlank(file.getOriginalFilename()) || null == fondId || null == menuId) return map;
		CesCloudUser user = SecurityUtils.getUser();
		Fonds fonds = this.fondsService.getFondsById(fondId);
		// 获取全部
		List<HelpItems> helpItemsList = this.baseMapper.selectList(Wrappers.<HelpItems>lambdaQuery()
				.eq(HelpItems::getHelpVersion, Arrays.asList(0, 1, 2))
				.eq(HelpItems::getMenuId, menuId)
				.eq(HelpItems::getIsDelete, 0));
		FileStorage fileStorage = getFileStorage(file, user);
		FileStorage retFileStorage = null;
		InputStream inputStream = null;
		try {
			inputStream = file.getInputStream();
			retFileStorage = fileStorageCommonService.upload(inputStream, fileStorage);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("上传失败");
		} finally {
			IoUtil.close(inputStream);
		}
		// 入库
		HelpItems items = new HelpItems();
		items.setUrl(retFileStorage.getFileStorageLocate());
		// 默认为0,上传版本为1,copy为2
		items.setHelpVersion(1);
		items.setMenuId(menuId.longValue());
		items.setCreatedBy(user.getId());
		items.setCreatedTime(LocalDateTime.now());
		items.setFileStorageId(fileStorage.getId());
		items.setStorageName(retFileStorage.getFileSourceName());
		items.setOrderNo(0);
		// 最大值
		if (CollectionUtil.isNotEmpty(helpItemsList)) {
			int maxOrder = helpItemsList.stream().mapToInt(item -> item.getOrderNo()).max().getAsInt();
			items.setOrderNo(maxOrder + 1);
		}
		items.setIsDelete(0);
		items.setFileStorageId(retFileStorage.getId());
		items.setFondsCode(fonds.getFondsCode());
		items.setFondsName(fonds.getFondsName());
		// 新增上传的数据
		this.baseMapper.insert(items);
		map.put(FieldConstants.Document.FILE_STORAGE_ID, retFileStorage.getId());
		map.put(FieldConstants.Document.FILE_NAME, fileStorage.getFileSourceName());
		map.put(FieldConstants.Document.FILE_SIZE, fileStorage.getFileSize());
		map.put(FieldConstants.Document.FILE_FORMAT, fileStorage.getFileType());
		return map;
	}


}
