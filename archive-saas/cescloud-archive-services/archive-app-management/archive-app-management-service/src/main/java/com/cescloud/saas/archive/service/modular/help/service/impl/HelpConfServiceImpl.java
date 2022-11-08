
package com.cescloud.saas.archive.service.modular.help.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.fonds.entity.Fonds;
import com.cescloud.saas.archive.api.modular.help.entity.HelpConf;

import com.cescloud.saas.archive.service.modular.common.security.service.CesCloudUser;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.fonds.service.FondsService;
import com.cescloud.saas.archive.service.modular.help.mapper.HelpConfMapper;
import com.cescloud.saas.archive.service.modular.help.service.HelpConfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基本数据权限
 *
 * @author zhaiyachao
 * @date 2021-05-11 18:32:44
 */
@Service
@Slf4j
public class HelpConfServiceImpl extends ServiceImpl<HelpConfMapper, HelpConf> implements HelpConfService {

	@Autowired
	private FondsService fondsService;

	/**
	 * 打开关闭开关
	 *
	 * @param fondId
	 * @param openType
	 * @return
	 */
	@Override
	public Boolean updateOpen(Integer fondId, Integer openType) {
		if (null == fondId || null == openType) return false;
		CesCloudUser user = SecurityUtils.getUser();
		// 	获取全宗
		Fonds fonds = this.fondsService.getFondsById(fondId.longValue());
		if (ObjectUtil.isNotEmpty(fonds)) {
			// 验证是否存在
			HelpConf conf = this.baseMapper.selectOne(Wrappers.<HelpConf>lambdaQuery()
					.eq(HelpConf::getTenantId, user.getTenantId())
					.eq(HelpConf::getFondsCode, fonds.getFondsCode())
					.eq(HelpConf::getFondsName, fonds.getFondsName()));
			HelpConf helpConf = new HelpConf();
			if (ObjectUtil.isNotEmpty(conf)) {
				BeanUtil.copyProperties(conf, helpConf);
				// 更新
				helpConf.setOpen(openType.longValue());
				this.baseMapper.updateById(conf);
				log.info("updateOpen :{} 更新成功");
			} else {
				// 新增
				helpConf.setOpen(0L);
				helpConf.setFondsName(fonds.getFondsName());
				helpConf.setFondsCode(fonds.getFondsCode());
				helpConf.setTenantId(user.getTenantId());
				helpConf.setCreatedUserName(user.getUsername());
				helpConf.setCreatedTime(LocalDateTime.now());
				helpConf.setCreatedBy(user.getId());
				this.baseMapper.insert(helpConf);
				log.info("updateOpen :{} 新增成功");
			}
			return true;

		}

		return false;
	}

	/**
	 * 验证是否打开
	 *
	 * @param fondId
	 * @return
	 */
	@Override
	public Boolean isOpen(Integer fondId) {
		if (null == fondId) return false;
		// 获取全宗
		Fonds fonds = this.fondsService.getFondsById(fondId.longValue());
		CesCloudUser user = SecurityUtils.getUser();
		if (ObjectUtil.isNotEmpty(fonds)) {
			HelpConf conf = this.baseMapper.selectOne(Wrappers.<HelpConf>lambdaQuery()
					.eq(HelpConf::getTenantId, user.getTenantId())
					.eq(HelpConf::getFondsCode, fonds.getFondsCode())
					.eq(HelpConf::getOpen, 0));
			if (ObjectUtil.isNotEmpty(conf)) return true;

		}
		return false;
	}

	/**
	 * 当前全宗copy其他全宗
	 *
	 * @param fondId  全宗Id
	 * @param fondIds List<其他全宗Id>
	 * @return
	 */
	@Override
	public Boolean copyOtherFond(Integer fondId, List<Integer> fondIds) {
		if (null == fondId || CollectionUtil.isEmpty(fondIds)) return false;
		// 获取全宗
		Fonds fonds = this.fondsService.getFondsById(fondId.longValue());
		CesCloudUser user = SecurityUtils.getUser();
		if (ObjectUtil.isNotEmpty(fonds)) {
			// 获取当前全宗状态
			HelpConf conf = this.baseMapper.selectOne(Wrappers.<HelpConf>lambdaQuery()
					.eq(HelpConf::getFondsCode, fonds.getFondsCode())
					.eq(HelpConf::getTenantId, user.getTenantId())
					.eq(HelpConf::getCreatedUserName, user.getUsername()));
			if (ObjectUtil.isNotEmpty(conf)) {
				// 更新及保存
				updateOtherFonds(conf.getOpen(), fondIds);

			}

		}

		return false;
	}

	/**
	 * @param open    打开状态
	 * @param fondIds 其他全宗Ids
	 */
	private void updateOtherFonds(Long open, List<Integer> fondIds) {
		if (null != open) {
			CesCloudUser user = SecurityUtils.getUser();
			// 获取其他全宗
			List<Fonds> fonds = this.fondsService.getFondsByIds(fondIds);
			if (ObjectUtil.isNotEmpty(fonds)) {
				List<String> fondCodes = fonds.stream().map(Fonds::getFondsCode).collect(Collectors.toList());
				HelpConf conf = new HelpConf();
				conf.setOpen(open);

				// 验证其他全宗是否存在
				List<HelpConf> helpConfs = this.baseMapper.selectList(Wrappers.<HelpConf>lambdaQuery()
						.eq(HelpConf::getTenantId, user.getTenantId())
						.in(HelpConf::getFondsCode, fondCodes));
				// 新增
				if (ObjectUtil.isEmpty(helpConfs)) {
					fonds.stream().forEach(item -> {
						conf.setFondsCode(item.getFondsCode());
						conf.setCreatedBy(user.getId());
						conf.setCreatedTime(LocalDateTime.now());
						conf.setCreatedUserName(user.getUsername());
						conf.setTenantId(user.getTenantId());
						conf.setFondsName(item.getFondsName());
						this.baseMapper.insert(conf);

					});
				}
				// 更新
				if (ObjectUtil.isNotEmpty(helpConfs)) {
					this.baseMapper.update(conf, Wrappers.<HelpConf>lambdaQuery().eq(HelpConf::getTenantId, user.getTenantId())
							.in(HelpConf::getFondsCode, fondCodes));
				}

			}
		}
	}
}
