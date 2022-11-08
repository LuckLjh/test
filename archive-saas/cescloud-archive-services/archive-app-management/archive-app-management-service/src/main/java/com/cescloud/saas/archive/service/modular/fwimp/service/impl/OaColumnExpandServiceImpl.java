package com.cescloud.saas.archive.service.modular.fwimp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.fwimp.dto.OaColumnExpListDTO;
import com.cescloud.saas.archive.api.modular.fwimp.dto.OaColumnExpandDTO;
import com.cescloud.saas.archive.api.modular.fwimp.entity.OaColumnExpand;
import com.cescloud.saas.archive.common.search.CriteriaCondition;
import com.cescloud.saas.archive.common.util.CesBlobUtil;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.fwimp.mapper.OaColumnExpandMapper;
import com.cescloud.saas.archive.service.modular.fwimp.service.OaColumnExpandService;
import com.cescloud.saas.archive.service.modular.fwimp.service.util.CriteriaConditionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@CacheConfig(cacheNames = "OaColumnExpand")
@RequiredArgsConstructor
public class OaColumnExpandServiceImpl  extends ServiceImpl<OaColumnExpandMapper, OaColumnExpand> implements OaColumnExpandService {
	/**
	 * 保存列此时
	 * @param oaColumnExpands
	 * @return
	 */
	@Override
	public R saveOaColumnExp(OaColumnExpListDTO oaColumnExpands) {
		List<OaColumnExpandDTO> ss = oaColumnExpands.getOaColumnExpand();

		LambdaQueryWrapper<OaColumnExpand> queryWrapper = Wrappers.<OaColumnExpand>query().lambda();
		Long flowId = 1L;
		String columnName = "";
		List<OaColumnExpand> oaColumnExpandList = ss.parallelStream().map(dto -> {
			OaColumnExpand OaColumnExp = new OaColumnExpand();
			BeanUtil.copyProperties(dto, OaColumnExp);
			boolean  flag = dto.getPageCondition() == null;
			if(!flag) {
				CriteriaCondition backCondition = CriteriaConditionUtil.toCriteriaCondition(dto.getPageCondition());
				OaColumnExp.setBackCondition(backCondition);
			}
			return OaColumnExp;
		}).collect(Collectors.toList());
		//先清空之前保存的信息
		if(oaColumnExpandList.size()>0){
			flowId = oaColumnExpandList.get(0).getOwnerFlowid();
			columnName = oaColumnExpandList.get(0).getOwnerColumn();
		}
		queryWrapper.eq(OaColumnExpand :: getOwnerFlowid,flowId)
				.eq(OaColumnExpand :: getOwnerColumn,columnName);
		remove(queryWrapper);
		//过滤一下 判断一下 如果是保存档案列没有参数就不存了
		if(oaColumnExpandList.size()>0) {
			if(oaColumnExpandList.get(0).getFlag() == 1 || oaColumnExpandList.get(0).getFlag() == 0 ) {
				oaColumnExpandList = oaColumnExpandList.stream().filter(
						oaColumnExpand ->
								oaColumnExpand.getTitleKey() != null && !oaColumnExpand.getTitleKey().equals("")

				).collect(Collectors.toList());
			}
		}

		//从新保存
		return new R<>(saveBatch(oaColumnExpandList));
	}

	/**
	 * 根据列和流程id 查询配置
	 * @param columnName
	 * @param flowId
	 * @return
	 */
	@Override
	public R findByColumnAndFlowId(String columnName, Long flowId) throws ArchiveBusinessException {
		if(StrUtil.isBlank(columnName)){
			throw new ArchiveBusinessException("请选择档案树节点并设置字段列！");
		}
		if(flowId == null  || StrUtil.isBlank(flowId.toString())){
			throw new ArchiveBusinessException("请先填写流程ID！");
		}
		LambdaQueryWrapper<OaColumnExpand> queryWrapper = Wrappers.<OaColumnExpand>query().lambda();
		queryWrapper.eq(OaColumnExpand :: getOwnerFlowid,flowId)
				.eq(OaColumnExpand :: getOwnerColumn,columnName);
		List<OaColumnExpand> list = list(queryWrapper);
		list.stream().forEach(OaColumnExpand ->{
			OaColumnExpand.setPageCondition(unserialize(OaColumnExpand.getPageCondition()));
		});
		return new R<>(list);
	}

	@Override
	public R<List<OaColumnExpand>> findByFlowId(Long oaFlowid) {
		LambdaQueryWrapper<OaColumnExpand> queryWrapper = Wrappers.<OaColumnExpand>query().lambda();
		queryWrapper.eq(OaColumnExpand :: getOwnerFlowid,oaFlowid);
		List<OaColumnExpand> list = list(queryWrapper);
		list.stream().forEach(OaColumnExpand ->{
			OaColumnExpand.setPageCondition(unserialize(OaColumnExpand.getPageCondition()));
		});
		return new R<>(list);
	}

	/**
	 * 将数据库保存的字节数组反序列化对象
	 *
	 * @param obj
	 * @return
	 */
	public Object unserialize(Object obj) {
		if (null == obj) {
			return null;
		}
		byte[] bytes = CesBlobUtil.objConvertToByte(obj);
		if (ArrayUtil.isEmpty(bytes)) {
			return null;
		}
		return ObjectUtil.deserialize(bytes);
	}
}
