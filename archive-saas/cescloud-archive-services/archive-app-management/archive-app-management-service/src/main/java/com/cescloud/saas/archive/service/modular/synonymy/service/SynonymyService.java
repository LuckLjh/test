
package com.cescloud.saas.archive.service.modular.synonymy.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.cescloud.saas.archive.api.modular.synonymy.dto.SynonymyGroupPostDTO;
import com.cescloud.saas.archive.api.modular.synonymy.dto.SynonymyGroupPutDTO;
import com.cescloud.saas.archive.api.modular.synonymy.dto.SynonymyWordPostDTO;
import com.cescloud.saas.archive.api.modular.synonymy.dto.SynonymyWordPutDTO;
import com.cescloud.saas.archive.api.modular.synonymy.dto.SynonymyWordSearchDTO;
import com.cescloud.saas.archive.api.modular.synonymy.entity.Synonymy;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 同义词
 *
 * @author liwei
 * @date 2019-04-09 13:03:31
 */
public interface SynonymyService extends IService<Synonymy> {

	/**
	 * 根据synonymyGroup进行分组查询
	 * select synonymy_group from archiveAppManagement_synonymy GROUP BY synonymy_group
	 * @return
	 */
	List<Synonymy> getGroupList();

	/**
	 * 根据同义词组id获取同义词
	 * @param id 同义词组id
	 * @return
	 */
	List<Synonymy> getWordsListById(Long id);

	/**
	 * 保存同义词组
	 * @param synonymyGroupPostDTO 同义词组dto
	 * @return 成功或失败
	 * @throws ArchiveBusinessException
	 */
	boolean saveGroup(SynonymyGroupPostDTO synonymyGroupPostDTO) throws ArchiveBusinessException;

	/**
	 * 保存同义词
	 * @param id 同义词组id
	 * @param synonymyWordPostDTO 同义词dto
	 * @return 成功或失败
	 * @throws ArchiveBusinessException
	 */
	boolean saveWord(Long id,SynonymyWordPostDTO synonymyWordPostDTO) throws ArchiveBusinessException;

	/**
	 * 修改同义词组
	 * @param synonymyGroupPutDTO 同义词组dto
	 * @return 成功或失败
	 * @throws ArchiveBusinessException
	 */
	boolean updateGroup(SynonymyGroupPutDTO synonymyGroupPutDTO) throws ArchiveBusinessException;

	/**
	 * 修改同义词
	 * @param synonymyWordPutDTO 同义词dto
	 * @return 成功或失败
	 * @throws ArchiveBusinessException
	 */
	boolean updateWord(SynonymyWordPutDTO synonymyWordPutDTO) throws ArchiveBusinessException;

	/**
	 * 删除同义词组
	 * @param synonymyId 同义词组id
	 * @return
	 */
	boolean deleteGroup(Long synonymyId);

	List<String> getSynonymyList();

	List<String> getSynonymyList(String word);

    /**
     * 查询同义词
     * @param synonymyWordSearchDTO
     * @return 同义词集合
     */
    List<String> search(SynonymyWordSearchDTO synonymyWordSearchDTO);
}
