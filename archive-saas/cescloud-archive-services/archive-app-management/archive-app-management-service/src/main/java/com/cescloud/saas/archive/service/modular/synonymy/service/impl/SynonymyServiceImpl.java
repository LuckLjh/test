
package com.cescloud.saas.archive.service.modular.synonymy.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.synonymy.dto.SynonymyGroupPostDTO;
import com.cescloud.saas.archive.api.modular.synonymy.dto.SynonymyGroupPutDTO;
import com.cescloud.saas.archive.api.modular.synonymy.dto.SynonymyWordPostDTO;
import com.cescloud.saas.archive.api.modular.synonymy.dto.SynonymyWordPutDTO;
import com.cescloud.saas.archive.api.modular.synonymy.dto.SynonymyWordSearchDTO;
import com.cescloud.saas.archive.api.modular.synonymy.entity.Synonymy;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.synonymy.mapper.SynonymyMapper;
import com.cescloud.saas.archive.service.modular.synonymy.service.SynonymyService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 同义词
 *
 * @author liwei
 * @date 2019-04-09 13:03:31
 */
@Service
@Slf4j
public class SynonymyServiceImpl extends ServiceImpl<SynonymyMapper, Synonymy> implements SynonymyService {

	@Override
	public List<Synonymy> getGroupList() {
		List<Synonymy> list = this.baseMapper.selectList(Wrappers.<Synonymy>lambdaQuery().isNull(Synonymy::getParentId));
		return list;
	}

	@Override
	public List<Synonymy> getWordsListById(Long id) {
		//根据前台传过来的同义词组id，获取前台点击的同义词组名称
		return this.baseMapper.selectList(Wrappers.<Synonymy>lambdaQuery().eq(Synonymy::getParentId, id));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean saveGroup(SynonymyGroupPostDTO synonymyGroupPostDTO) throws ArchiveBusinessException {
		//先判断新增的同义词组是否已经存在
		boolean isExist = judgeGroupExist(synonymyGroupPostDTO.getSynonymyGroup());
		if (isExist) {
			log.error("新增失败,同义词组已经存在!");
			throw new ArchiveBusinessException("新增失败,同义词组已经存在!");
		}
		//新增同义词组
		Synonymy synonymy = new Synonymy();
		synonymy.setSynonymyGroup(synonymyGroupPostDTO.getSynonymyGroup());
		return this.save(synonymy);
	}

	@Override
	@Transactional(
			rollbackFor = {Exception.class}
	)
	public boolean saveWord(Long id, SynonymyWordPostDTO synonymyWordPostDTO) throws ArchiveBusinessException {
		//先判断新增的同义词是否已经存在
		boolean isExist = judgeWordExist(synonymyWordPostDTO.getSynonymyWord(), id);
		if (isExist) {
			log.error("新增失败,同义词已经存在!");
			throw new ArchiveBusinessException("新增失败,同义词已经存在!");
		}
		//获取同义词组记录
		Synonymy newSynonymy = new Synonymy();
		newSynonymy.setSynonymyWord(synonymyWordPostDTO.getSynonymyWord());
		newSynonymy.setParentId(id);
		return this.save(newSynonymy);
	}

	@Override
	@Transactional(
			rollbackFor = {Exception.class}
	)
	public boolean updateGroup(SynonymyGroupPutDTO synonymyGroupPutDTO) throws ArchiveBusinessException {
		//获取旧的统一词组的名称
		Synonymy oldSynonymy = this.getById(synonymyGroupPutDTO.getId());
		String oldGroup = oldSynonymy.getSynonymyGroup();
		//获取同义词组新的名称
		String newGroup = synonymyGroupPutDTO.getSynonymyGroup();
		if (oldGroup.equals(newGroup)) {
			return false;
		}
		//校验，判断有没有重复的
		boolean isExist = judgeGroupExist(newGroup);
		if (isExist) {
			log.error("更新失败,同义词组已经存在!");
			throw new ArchiveBusinessException("更新失败,同义词组已经存在! ");
		}
		oldSynonymy.setSynonymyGroup(newGroup);
		return this.updateById(oldSynonymy);
	}

	@Transactional(
			rollbackFor = {Exception.class}
	)
	@Override
	public boolean updateWord(SynonymyWordPutDTO synonymyWordPutDTO) throws ArchiveBusinessException {
		Synonymy synonymy = this.getById(synonymyWordPutDTO.getId());
		String oldWord = synonymy.getSynonymyWord();
		String newWord = synonymyWordPutDTO.getSynonymyWord();
		if (oldWord.equals(newWord)) {
			return false;
		}
		//先判断保存的同义词是否已经存在
		boolean isExist = judgeWordExist(newWord, synonymy.getParentId());
		if (isExist) {
			log.error("新增失败,同义词已经存在!");
			throw new ArchiveBusinessException("新增失败,同义词已经存在!");
		}
		synonymy.setSynonymyWord(newWord);
		return this.updateById(synonymy);
	}

	@Override
	@Transactional(
			rollbackFor = {Exception.class}
	)
	public boolean deleteGroup(Long synonymyId) {
		// 删除同义词组
		this.removeById(synonymyId);
		// 删除同义词
		return this.remove(Wrappers.<Synonymy>lambdaQuery().eq(Synonymy::getParentId, synonymyId));
	}

	private boolean judgeGroupExist(String groupName) {
		boolean isExist = false;
		int count = this.count(Wrappers.<Synonymy>lambdaQuery().eq(Synonymy::getSynonymyGroup, groupName).isNull(Synonymy::getParentId));
		if (count > 0) {
			isExist = true;
		}
		return isExist;
	}


	@Override
	public List<String> getSynonymyList() {
		List<Synonymy> list = Lists.newArrayList();
		List<Synonymy> groupAll = this.getGroupList();
		groupAll.forEach(item -> {
			List<Synonymy> words = this.getWordsListById(item.getId());
			list.addAll(words);
		});
		return list.stream().map(Synonymy::getSynonymyWord).distinct().collect(Collectors.toList());
	}


	private boolean judgeWordExist(String wordName, Long parentId) {
		boolean isExist = false;
		int count = this.count(Wrappers.<Synonymy>lambdaQuery().eq(Synonymy::getSynonymyWord, wordName).eq(Synonymy::getParentId, parentId));
		if (count > 0) {
			isExist = true;
		}
		return isExist;
	}

	@Override
	public List<String> getSynonymyList(String word) {
		List<String> strs = Lists.newArrayList();
		List<Synonymy> list = Lists.newArrayList();
		List<Synonymy> groupAll = this.getGroupList();
		groupAll.forEach(item -> {
			List<Synonymy> words = this.list(Wrappers.<Synonymy>lambdaQuery().eq(Synonymy::getParentId, item.getId()));
			if (CollUtil.isNotEmpty(words)){
				list.addAll(words);
			}
		});

		list.stream().forEach(item ->{
			if (item.getSynonymyWord().contains(word)){
				strs.add(item.getSynonymyWord());
			}
		});
		return strs.stream().distinct().collect(Collectors.toList());
	}

    @Override
    public List<String> search(SynonymyWordSearchDTO synonymyWordSearchDTO) {
	    //关键字拆分，去重
	    List<String> words = synonymyWordSearchDTO.getWord().stream()
            .flatMap(word -> Arrays.stream(word.split("\\s")))
            .map(String::trim).filter(StrUtil::isNotBlank)
            .distinct()
            .collect(Collectors.toList());

	    List<Synonymy> synonymyList = this.baseMapper.search(words);
//        Map<String, Set<String>> result = synonymyList.stream().collect(
//            Collectors.groupingBy(Synonymy::getSynonymyGroup, Collectors.mapping(Synonymy::getSynonymyWord, Collectors.toSet())));
        return synonymyList.stream().map(Synonymy::getSynonymyWord).distinct().collect(Collectors.toList());
    }
}
