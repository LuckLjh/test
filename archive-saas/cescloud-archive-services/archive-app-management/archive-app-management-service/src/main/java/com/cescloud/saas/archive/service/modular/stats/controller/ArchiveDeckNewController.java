
package com.cescloud.saas.archive.service.modular.stats.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.api.modular.stats.entity.ArchiveDeckNew;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.stats.service.ArchiveDeckNewService;
import com.cescloud.saas.archive.service.modular.stats.service.ArchiveStatsService;
import com.cescloud.saas.archive.service.modular.stats.service.FilingStatsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;


/**
 * 驾驶舱新增量
 *
 * @author bob
 * @date 2021-04-30 12:03:27
 */
@Api(value = "ArchiveDeck", description = "驾驶舱统计")
@RestController
@AllArgsConstructor
@RequestMapping("/archive-deck")
public class ArchiveDeckNewController {

  private final ArchiveDeckNewService archiveDeckNewService;
  private final FilingStatsService filingStatsService;

	@Autowired
	@Qualifier("archiveStatsService")
	private ArchiveStatsService archiveStatsService;

	@ApiOperation(value = "驾驶舱量各状态的统计")
	@GetMapping("/status/count")
	public R<?> statusAmonts( String fondsCode) throws ArchiveBusinessException, InterruptedException {
		return new R<>(archiveStatsService.getStatusAmount(fondsCode));
	}

	@ApiOperation(value = "驾驶舱量统计")
	@GetMapping("/total/count")
	public R<?> totalCount(String fondsCode) {
		return new R<>(archiveDeckNewService.getTotalCountStatsData(fondsCode));
	}
	/**
	 * 新增驾驶舱新增量
	 * @param archiveDeckNew 驾驶舱新增量
	 * @return R
	 */
	@ApiOperation(value = "新增驾驶舱部门新增量统计")
	@SysLog("新增驾驶舱新增量统计")
	@PostMapping("/deck-dept-stat")
	public R deckShowTypeCodeForDept(@RequestBody ArchiveDeckNew archiveDeckNew) throws ArchiveBusinessException {
		return new R<>(filingStatsService.deckShowTypeCodeForDept(archiveDeckNew.getPageAmount(),archiveDeckNew.getStatus(), archiveDeckNew.getFondsCode(),archiveDeckNew.getArchiveTypeCode()==null?"":archiveDeckNew.getArchiveTypeCode()));
	}

	/**
	 * 新增驾驶舱新增量
	 * @param archiveDeckNew 驾驶舱新增量
	 * @return R
	 */
	@ApiOperation(value = "新增驾驶舱新增量统计")
	@SysLog("新增驾驶舱新增量统计")
	@PostMapping("/deck-new-stat")
//	@Inner
	public R deckNewStat(@RequestBody ArchiveDeckNew archiveDeckNew) throws ArchiveBusinessException {
		return new R<>(archiveDeckNewService.deckNewStat(archiveDeckNew));
	}


  /**
   * 分页查询
   * @param page 分页对象
   * @param archiveDeckNew 驾驶舱新增量
   * @return
   */
  @ApiOperation(value = "分页查询")
  @GetMapping("/page")
  public R getArchiveDeckNewPage(Page page, ArchiveDeckNew archiveDeckNew) {
    return  new R<>(archiveDeckNewService.page(page,Wrappers.query(archiveDeckNew)));
  }


  /**
   * 通过id查询驾驶舱新增量
   * @param id id
   * @return R
   */
  @ApiOperation(value = "通过id查询驾驶舱新增量")
  @GetMapping("/{id}")
  public R getById(@PathVariable("id") Long id){
    return new R<>(archiveDeckNewService.getById(id));
  }

  /**
   * 新增驾驶舱新增量
   * @param archiveDeckNew 驾驶舱新增量
   * @return R
   */
  @ApiOperation(value = "新增驾驶舱新增量")
  @SysLog("新增驾驶舱新增量")
  @PostMapping
  public R save(@RequestBody ArchiveDeckNew archiveDeckNew){
    return new R<>(archiveDeckNewService.save(archiveDeckNew));
  }

  /**
   * 修改驾驶舱新增量
   * @param archiveDeckNew 驾驶舱新增量
   * @return R
   */
  @ApiOperation(value = "修改驾驶舱新增量")
  @SysLog("修改驾驶舱新增量")
  @PutMapping
  public R updateById(@RequestBody ArchiveDeckNew archiveDeckNew){
    return new R<>(archiveDeckNewService.updateById(archiveDeckNew));
  }

  /**
   * 通过id删除驾驶舱新增量
   * @param id id
   * @return R
   */
  @ApiOperation(value = "通过id删除驾驶舱新增量")
  @SysLog("删除驾驶舱新增量")
  @DeleteMapping("/{id}")
  public R removeById(@PathVariable Long id){
    return new R<>(archiveDeckNewService.removeById(id));
  }

}
