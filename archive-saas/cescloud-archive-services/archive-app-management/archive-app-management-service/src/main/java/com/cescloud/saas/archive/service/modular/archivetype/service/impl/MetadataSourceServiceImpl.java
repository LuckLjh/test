
package com.cescloud.saas.archive.service.modular.archivetype.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archivetype.dto.SourceDTO;
import com.cescloud.saas.archive.api.modular.archivetype.dto.SourcePostDTO;
import com.cescloud.saas.archive.api.modular.archivetype.entity.MetadataSource;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.service.modular.archivetype.mapper.MetadataSourceMapper;
import com.cescloud.saas.archive.service.modular.archivetype.service.MetadataSourceService;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 参照源数据表
 *
 * @author liwei
 * @date 2019-04-16 14:52:34
 */
@Service
@Slf4j
@CacheConfig(cacheNames = "metadata-source")
public class MetadataSourceServiceImpl extends ServiceImpl<MetadataSourceMapper, MetadataSource> implements MetadataSourceService {

	@Autowired
	private MetadataService metadataService;

	@Override
	public List<SourceDTO> getMetaDataSources(Long id, String storageLocate, Long metadataId,Long moduleId) {
		if (log.isDebugEnabled()){
			log.debug("获取分组设置的参照字段");
		}
		//List<SourceDTO> list = baseMapper.getMetaDataSources(storageLocate,id);
		List<SourceDTO> list = baseMapper.getDefMetaDataSources(storageLocate,id,metadataId,moduleId);
		//给metadataChinese和metadataEnglish和isSource赋值
		final List<Long> ids = list.stream().map(sourceDTO -> sourceDTO.getMetadataId()).collect(Collectors.toList());
		Collection<Metadata> metadataCollection = metadataService.listByIds(ids);

		Map<Long, Metadata> idMetadataMap = metadataCollection.parallelStream().collect(Collectors.toMap(Metadata::getId, Function.identity()));

		list.stream().forEach(e -> {
			if(e.getSortNo() == null){//如果为空说明这条元字段记录不是参照字段
				e.setIsSource(false);
			}else{
				e.setIsSource(true);
			}
			Metadata m = idMetadataMap.get(e.getMetadataId());
			if (ObjectUtil.isNotNull(m)) {
				e.setMetadataType(m.getMetadataType());
				e.setMetadataChinese(m.getMetadataChinese());
				e.setMetadataEnglish(m.getMetadataEnglish());
			}
		});
		list = list.stream().sorted((x,y) -> {
			if(x.getSortNo()!=null && y.getSortNo()!=null){
				return x.getSortNo() - y.getSortNo();
			}else if(x.getSortNo()==null && y.getSortNo()!=null){
				return 1;
			}else if(x.getSortNo()!=null && y.getSortNo()==null){
				return -1;
			}
			return 0;
		}).collect(Collectors.toList());
		return list;
	}

	@Override
	@Cacheable(
			key = "'archive-app-management:metadata-source:'+#storageLocate+':'+#targetId",
			unless = "#result == null || #result.size() == 0"
	)
	public List<SourceDTO> getMetadataSourcesByStorageAndTargetId(String storageLocate, Long targetId) {
		List<SourceDTO> list = baseMapper.getMetadataSourcesByStorageAndTargetId(storageLocate,targetId);
		return list;
	}


	@CacheEvict(allEntries = true)
	@Override
	@Transactional(rollbackFor = {Exception.class})
	public boolean saveMetadataSource(SourcePostDTO sourcePostDTO) {
		this.remove(Wrappers.<MetadataSource>query().lambda()
				.eq(MetadataSource::getMetadataTargetId,sourcePostDTO.getId()));
		if(CollectionUtil.isNotEmpty(sourcePostDTO.getGroupFieldIds())){
			int i = 1;
			for(Long id : sourcePostDTO.getGroupFieldIds()){
				MetadataSource metadataSource = new MetadataSource();
				metadataSource.setStorageLocate(sourcePostDTO.getStorageLocate());
				metadataSource.setMetadataTargetId(sourcePostDTO.getId());
				metadataSource.setMetadataSourceId(id);
				metadataSource.setSortNo(i);
				this.save(metadataSource);
				i++;
			}
		}
		return true;
	}

	@Cacheable(
			key = "'archive-app-management:metadata-source:'+#storageLocate",
			unless = "#result == null || #result.size() == 0"
	)
	@Override
	public List<MetadataSource> getMetaDataSourceByStorageLocate(String storageLocate) {
		return this.list(Wrappers.<MetadataSource>query()
				.lambda()
				.eq(MetadataSource::getStorageLocate,storageLocate));
	}

	@Override
	@Cacheable(
			key = "'archive-app-management:metadata-source:targetId'+#targetId",
			unless = "#result == null || #result.size() == 0"
	)
	public List<MetadataSource> getMetaDataSourceByTargetId(Long targetId) {
		return this.list(Wrappers.<MetadataSource>query().lambda()
				.eq(MetadataSource::getMetadataTargetId, targetId)
				.orderByAsc(MetadataSource::getSortNo));
	}

	@Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public boolean removeByWrappers(Wrapper<MetadataSource> queryWrapper) {
		return this.remove(queryWrapper);
	}

	/**
	 * 删除规则
	 *
	 * @param storageLocate
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(allEntries = true)
	public boolean deleteByStorageLocate(String storageLocate) {
		return this.remove(Wrappers.<MetadataSource>query().lambda().eq(MetadataSource::getStorageLocate, storageLocate));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void copyByStorageLocate(String srcStorageLocate, String destStorageLocate,
									Map<Long,Long> srcDestAutovalueIdMap, Map<Long, Long> srcDestMetadataMap) {
		List<MetadataSource> list = this.list(Wrappers.<MetadataSource>lambdaQuery().eq(MetadataSource::getStorageLocate, srcStorageLocate));
		if (CollectionUtil.isNotEmpty(list)) {
			list.stream().forEach(metadataSource -> {
				metadataSource.setId(null);
				metadataSource.setStorageLocate(destStorageLocate);
				metadataSource.setMetadataTargetId(srcDestAutovalueIdMap.get(metadataSource.getMetadataTargetId()));
				metadataSource.setMetadataSourceId(srcDestMetadataMap.get(metadataSource.getMetadataSourceId()));
			});
			this.saveBatch(list);
		}
	}
}
