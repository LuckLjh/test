
package com.cescloud.saas.archive.service.modular.synonymy.controller;

import com.cescloud.saas.archive.api.modular.stats.entity.YearStats;
import com.cescloud.saas.archive.api.modular.synonymy.dto.SynonymyGroupPostDTO;
import com.cescloud.saas.archive.api.modular.synonymy.dto.SynonymyGroupPutDTO;
import com.cescloud.saas.archive.api.modular.synonymy.dto.SynonymyWordPostDTO;
import com.cescloud.saas.archive.api.modular.synonymy.dto.SynonymyWordPutDTO;
import com.cescloud.saas.archive.api.modular.synonymy.dto.SynonymyWordSearchDTO;
import com.cescloud.saas.archive.api.modular.synonymy.entity.Synonymy;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.synonymy.service.SynonymyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 同义词
 *
 * @author liwei
 * @date 2019-04-09 13:03:31
 */
@Api(value = "synonymy", tags = "应用管理-同义词管理：同义词组和同义词管理")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/synonymy")
public class SynonymyController {

  private final SynonymyService synonymyService;

	/**
	 * 根据同义词组id查询同义词列表
	 *
	 * @param id 同义词组id
	 * @return
	 */
	@ApiOperation(value = "根据同义词组id查询同义词列表", httpMethod = "GET")
	@GetMapping("/groups/{id}")
	public R<List<Synonymy>> getWordList(@PathVariable("id") @ApiParam(name = "id", value = "同义词组id", required = true, example = "1") @NotNull(message = "同义词组id不能为空") Long id) {
		return new R<List<Synonymy>>(synonymyService.getWordsListById(id));
	}

	/**
	 * 同义词组查询
	 *
	 * @return
	 */
	@ApiOperation(value = "同义词组查询", httpMethod = "GET")
	@GetMapping("/groups")
	public R<List<Synonymy>> getGroupList() {
		return new R<>(synonymyService.getGroupList());
	}


	@ApiOperation(value = "新增同义词组", httpMethod = "POST")
	@SysLog("新增同义词组")
	@PostMapping("/group")
	public R saveGroup(@RequestBody @ApiParam(name = "synonymyGroupPostDTO", value = "同义词组json对象", required = true) @Valid SynonymyGroupPostDTO synonymyGroupPostDTO) throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("新增同义词组-同义词组名称【%s】",synonymyGroupPostDTO.getSynonymyGroup()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(synonymyService.saveGroup(synonymyGroupPostDTO));
	}

	@ApiOperation(value = "新增同义词", httpMethod = "POST")
	@SysLog("新增同义词")
	@PostMapping("/groups/{id}")
	public R saveWord(@PathVariable("id") @ApiParam(name = "id", value = "同义词组id", required = true) @NotNull(message = "同义词组id不能为空") Long id,
					  @RequestBody @ApiParam(name = "synonymyWordPostDTO", value = "同义词组json对象", required = true) @Valid SynonymyWordPostDTO synonymyWordPostDTO) throws ArchiveBusinessException {
		try {
			Synonymy byId = synonymyService.getById(id);
			SysLogContextHolder.setLogTitle(String.format("新增同义词-同义词组名称【%s】-同义词名称【%s】",byId.getSynonymyGroup(),synonymyWordPostDTO.getSynonymyWord()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(synonymyService.saveWord(id, synonymyWordPostDTO));
	}

	/**
	 * 修改同义词
	 *
	 * @param synonymyWordPutDTO 同义词
	 * @return R
	 */
	@ApiOperation(value = "修改同义词", httpMethod = "PUT")
	@SysLog("修改同义词")
	@PutMapping("/word")
	public R updateById(@RequestBody @ApiParam(name = "synonymyWordPutDTO", value = "同义词更新实体对象", required = true) @Valid SynonymyWordPutDTO synonymyWordPutDTO) throws ArchiveBusinessException {
		try {
			Synonymy byId = synonymyService.getById(synonymyWordPutDTO.getId());
			SysLogContextHolder.setLogTitle(String.format("修改同义词-同义词组名称【%s】-同义词名称【%s】",byId.getSynonymyGroup(),synonymyWordPutDTO.getSynonymyWord()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(synonymyService.updateWord(synonymyWordPutDTO));
	}

	/**
	 * 修改同义词组
	 *
	 * @param synonymyGroupPutDTO 同义词组dto
	 * @return
	 */
	@ApiOperation(value = "修改同义词组", httpMethod = "PUT")
	@SysLog("修改同义词组")
	@PutMapping("/group")
	public R updateGroup(@RequestBody @ApiParam(name = "synonymyGroupPutDTO", value = "同义词组json对象", required = true) @Valid SynonymyGroupPutDTO synonymyGroupPutDTO) throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("修改同义词组-同义词组名称【%s】",synonymyGroupPutDTO.getSynonymyGroup()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(synonymyService.updateGroup(synonymyGroupPutDTO));
	}

	/**
	 * 通过id删除同义词
	 *
	 * @param id id
	 * @return R
	 */
	@ApiOperation(value = "删除同义词", httpMethod = "DELETE")
	@SysLog("删除同义词")
	@DeleteMapping("/word/{id}")
	public R removeById(@PathVariable("id") @ApiParam(name = "id", value = "同义词id", required = true, example = "1") @NotNull(message = "同义词id不能为空") Long id) {
		try {
			Synonymy byId = synonymyService.getById(id);
			SysLogContextHolder.setLogTitle(String.format("删除同义词-同义词组名称【%s】-同义词名称【%s】",byId.getSynonymyGroup(),byId.getSynonymyWord()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(synonymyService.removeById(id));
	}

	/**
	 * 通过id删除同义词组
	 *
	 * @param id id
	 * @return R
	 */
	@ApiOperation(value = "删除同义词组", httpMethod = "DELETE")
	@SysLog("删除同义词组")
	@DeleteMapping("/group/{id}")
	public R deleteGroup(@PathVariable("id") @ApiParam(name = "id", value = "同义词组id", required = true, example = "1") @NotNull(message = "同义词组id不能为空") Long id) {
		try {
			Synonymy byId = synonymyService.getById(id);
			SysLogContextHolder.setLogTitle(String.format("删除同义词组-同义词组名称【%s】-同义词名称【%s】",byId.getSynonymyGroup(),byId.getSynonymyWord()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(synonymyService.deleteGroup(id));
	}

	/**
	 * 获取全部同义词
	 *
	 * @return R
	 */
	@ApiOperation(value = "根据id查询同义词或同义词组", httpMethod = "GET")
	@GetMapping("/getSynonymyList")
	public R<List<String>> getSynonymyList() {
		return new R<>(synonymyService.getSynonymyList());
	}

	/**
	 * 模糊匹配同义词
	 *
	 * @return R
	 */
	@ApiOperation(value = "根据id查询同义词或同义词组", httpMethod = "GET")
	@GetMapping("/getSynonymyByWord")
	public R<List<String>> getSynonymyByWord(@RequestParam("word") String word){
		return new R<>(synonymyService.getSynonymyList(word));
	}

	/**
	 * 查询关键字的同义词
	 *
	 * @return R
	 */
	@ApiOperation(value = "查询关键字的同义词", httpMethod = "POST")
	@PostMapping("/search")
	public R<List<String>> search(@RequestBody @Validated SynonymyWordSearchDTO synonymyWordSearchDTO){
		return new R<>(synonymyService.search(synonymyWordSearchDTO));
	}

}
