
package com.cescloud.saas.archive.service.modular.metadata.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.metadata.constant.MetadataConstant;
import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataBase;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.metadata.mapper.MetadataBaseMapper;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 基础元数据
 *
 * @author liudong1
 * @date 2019-03-27 14:33:25
 */
@Service
@Slf4j
public class MetadataBaseServiceImpl extends ServiceImpl<MetadataBaseMapper, MetadataBase> implements MetadataBaseService {

//	@Autowired
//	private MetadataService metadataService;

	@Override
	public IPage<MetadataBase> page(IPage page, MetadataBase metadataBase) {
		LambdaQueryWrapper<MetadataBase> queryWrapper = Wrappers.<MetadataBase>query().lambda();
		queryWrapper.eq(MetadataBase::getArchiveLayer, metadataBase.getArchiveLayer());
		if (StrUtil.isNotBlank(metadataBase.getMetadataChinese())) {
			queryWrapper.like(MetadataBase::getMetadataChinese, metadataBase.getMetadataChinese());
		}
		return this.page(page, queryWrapper);
	}

//	@Override
//	public void insertIntoMetadataFromBaseBatch(List<ArchiveTable> archiveTables) {
//		//获取所有的MetadataBase
//		final List<String> classTypeList = archiveTables.parallelStream().map(table -> table.getClassType()).distinct().collect(Collectors.toList());
//		final List<MetadataBaseDTO> metadataBaseBusiness = getBaseMapper().getMetadataBaseDTOList(classTypeList);
//		//查询模板base表中的系统字段的记录
//		final List<MetadataBase> metadataBaseSys = this.list(Wrappers.<MetadataBase>lambdaQuery().eq(MetadataBase::getArchiveLayer, MetadataConstant.METADATA_SYS));
//		final List<Metadata> metadataList = generatorMetadataList(archiveTables, metadataBaseBusiness, metadataBaseSys);
//		//切割list，防止过大，批量新增时sql语句超长,500个元素批量保存一次
//		final List<List<Metadata>> splitList = CollectionUtil.<List<Metadata>>newArrayList();
//		int number = 500;
//		//计算切割次数
//		int limit = (metadataList.size() + number - 1) / number;
//		Stream.iterate(0, n -> n + 1).limit(limit).forEach(i -> {
//			splitList.add(metadataList.stream().skip(i * number).limit(number).collect(Collectors.toList()));
//		});
//		for (List<Metadata> list : splitList) {
//			metadataService.saveBatchForMysql(list);
//		}
//	}

//	/**
//	 * 批量生成元数据
//	 *
//	 * @param archiveTables
//	 * @param metadataBaseBusiness
//	 * @param metadataBaseSys
//	 * @return
//	 */
//	private List<Metadata> generatorMetadataList(final List<ArchiveTable> archiveTables, final List<MetadataBaseDTO> metadataBaseBusiness, final List<MetadataBase> metadataBaseSys) {
//		final List<Metadata> metadataList = CollectionUtil.<Metadata>newArrayList();
//		final String userId = SecurityUtils.getUser().getId().toString();
//		archiveTables.stream().forEach(archiveTable -> {
//			//系统字段
//			final List<Metadata> metadataSys = metadataBaseSys.parallelStream().map(metadataBase -> {
//				Metadata metadata = new Metadata();
//				BeanUtil.copyProperties(metadataBase, metadata);
//				metadata.setId(null);
//				metadata.setMetadataClass(0);
//				metadata.setIsList(0);
//				metadata.setIsEdit(0);
//				metadata.setIsSearch(0);
//				metadata.setSortNo(0);
//				metadata.setTableId(archiveTable.getId());
//				metadata.setTenantId(archiveTable.getTenantId());
//				metadata.setStorageLocate(archiveTable.getStorageLocate());
//				metadata.setRevision(1L);
//				metadata.setCreatedTime(LocalDateTime.now());
//				metadata.setCreatedBy(userId);
//				if (StrUtil.equals(metadataBase.getMetadataEnglish(), "is_delete", true)) {
//					metadata.setMetadataDefaultValue("0");
//				}
//				if (StrUtil.equals(metadataBase.getMetadataEnglish(), "type_code", true)) {
//					metadata.setMetadataDefaultValue(archiveTable.getArchiveTypeCode());
//				}
//				if (StrUtil.equals(metadataBase.getMetadataEnglish(), "archive_layer", true)) {
//					metadata.setMetadataDefaultValue(archiveTable.getArchiveLayer());
//				}
//				return metadata;
//			}).collect(Collectors.toList());
//			//业务字段
//			final List<MetadataBaseDTO> metadataBaseDTOS = metadataBaseBusiness.parallelStream().filter(dto -> {
//				return StrUtil.equals(dto.getClassType(), archiveTable.getClassType())
//						&& dto.getFilingType().equals(archiveTable.getFilingType())
//						&& StrUtil.equals(dto.getArchiveLayer(), archiveTable.getArchiveLayer());
//			}).collect(Collectors.toList());
//			final List<Metadata> metadataBusiness = metadataBaseDTOS.parallelStream().map(dto -> {
//				Metadata metadata = new Metadata();
//				BeanUtil.copyProperties(dto, metadata);
//				metadata.setId(null);
//				metadata.setMetadataClass(1);
//				metadata.setIsList(0);
//				metadata.setIsEdit(0);
//				metadata.setIsSearch(0);
//				metadata.setSortNo(0);
//				metadata.setTableId(archiveTable.getId());
//				metadata.setTenantId(archiveTable.getTenantId());
//				metadata.setStorageLocate(archiveTable.getStorageLocate());
//				metadata.setRevision(1L);
//				metadata.setCreatedTime(LocalDateTime.now());
//				metadata.setCreatedBy(userId);
//				return metadata;
//			}).collect(Collectors.toList());
//			metadataList.addAll(metadataSys);
//			metadataList.addAll(metadataBusiness);
//		});
//		return metadataList;
//	}

	@Override
	public R checkUnique(String tableName, Map<String, Object> params, String columnChinese) {
		Long id = baseMapper.checkUnique(tableName, params);
		//没找到ID，说明没有值
		if (ObjectUtil.isNull(id)) {
			return new R("");
		} else {
			return new R().fail(id, "已经存在相同的" + columnChinese + "！");
		}
	}

	/**
	 * 得到系统元数据字段
	 *
	 * @return
	 */
	@Override
	public List<MetadataBase> getSysMetadata() {
		return this.list(Wrappers.<MetadataBase>lambdaQuery()
				.eq(MetadataBase::getArchiveLayer, MetadataConstant.METADATA_SYS));
	}

	@Override
	public List<MetadataBase> getBakMetadata() {
		return this.list(Wrappers.<MetadataBase>lambdaQuery()
				.eq(MetadataBase::getArchiveLayer, MetadataConstant.METADATA_BAK));
	}

}
