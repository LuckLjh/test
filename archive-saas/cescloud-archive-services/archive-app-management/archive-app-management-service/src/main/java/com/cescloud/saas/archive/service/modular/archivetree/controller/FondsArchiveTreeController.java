/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.archivetree.controller</p>
 * <p>文件名:FondsArchiveTreeController.java</p>
 * <p>创建时间:2019年5月13日 下午2:49:19</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.archivetree.controller;

import java.util.List;

import com.cescloud.saas.archive.api.modular.archivetree.dto.RenderTreeDTO;
import com.cescloud.saas.archive.service.modular.archivetree.service.ArchiveTreeService;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cescloud.saas.archive.api.modular.archivetree.dto.FondsArchiveTreeDTO;
import com.cescloud.saas.archive.api.modular.archivetree.entity.FondsArchiveTree;
import com.cescloud.saas.archive.service.modular.archivetree.service.FondsArchiveTreeService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 全宗与档案树绑定
 *
 * @author qiucs
 * @version 1.0.0 2019年5月13日
 */

@Api(value = "fonds-archive-tree", tags = "全宗与档案树绑定")
@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/fonds-archive-tree")
public class FondsArchiveTreeController {

    FondsArchiveTreeService fondsArchiveTreeService;

	@Autowired
	private final ArchiveTreeService archiveTreeService;


    /**
     * 分页查询
     *
     * @param page
     *            分页对象
     * @param fondsArchiveTree
     *            全宗档案树对象
     * @return
     */
    /*@ApiOperation(value = "分页查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "分页信息", required = true, dataType = "page", paramType = "query"),
            @ApiImplicitParam(name = "fondsArchiveTree", value = "全宗档案树对象", required = false, dataType = "FondsArchiveTree", paramType = "query")
    })
    @GetMapping("/page")
    public R page(Page page, FondsArchiveTree fondsArchiveTree) {
        return new R<>(fondsArchiveTreeService.page(page, Wrappers.query(fondsArchiveTree)));
    }*/

    /**
     * 查询
     *
     * @param fondsArchiveTree
     *            全宗档案树对象
     * @return
     */
    @ApiOperation(value = "查询")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "fondsArchiveTree", value = "全宗档案树对象", required = false, dataType = "FondsArchiveTree", paramType = "query")
    })
    @GetMapping("/list")
    public R list(FondsArchiveTree fondsArchiveTree) {
        return new R<>(fondsArchiveTreeService.list(Wrappers.query(fondsArchiveTree)));
    }

    /**
     * 档案树与全宗绑定
     *
     * @param fondsArchiveTreeDTO
     *            绑定对象
     * @return R
     */
    @ApiOperation(value = "档案树与全宗绑定")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "fondsArchiveTreeDTO", value = "树节点对象，格式：{archiveTreeId: \"\", fondsCodes: [\"\",...]}，archiveTreeId是档案树ID，fondsCodes是多个全宗号", required = true, dataType = "ArchiveTreeDTO")
    })
    @SysLog("档案树与全宗绑定")
    @PostMapping
    public R save(@RequestBody FondsArchiveTreeDTO fondsArchiveTreeDTO) {
		try {
			RenderTreeDTO tree = archiveTreeService.getTreeById(fondsArchiveTreeDTO.getArchiveTreeId());
			SysLogContextHolder.setLogTitle(String.format("档案树与全宗绑定-档案树节点名称【%s】,全宗号【%s】",tree.getTreeName(),fondsArchiveTreeDTO.getFondsCodes()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        return new R<>(fondsArchiveTreeService.save(fondsArchiveTreeDTO));
    }

    /**
     * 档案树与全宗绑定
     *
     * @param entityList
     *            绑定对象集合
     * @return R
     */
    @ApiOperation(value = "档案树与全宗批量绑定")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "entityList", value = "树节点对象，格式：[{archiveTreeId: \"\", fondsCodes: \"\"},...]，archiveTreeId是档案树ID，fondsCodes是全宗号", required = true, dataType = "list")
    })
    @SysLog("档案树与全宗批量绑定")
    @PostMapping("/save-all")
    public R saveAll(@RequestBody List<FondsArchiveTree> entityList) {
        return new R<>(fondsArchiveTreeService.saveBatch(entityList));
    }
}
