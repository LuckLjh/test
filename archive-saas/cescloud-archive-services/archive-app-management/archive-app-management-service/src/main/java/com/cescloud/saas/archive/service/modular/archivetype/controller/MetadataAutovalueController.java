
package com.cescloud.saas.archive.service.modular.archivetype.controller;

import cn.hutool.core.bean.BeanUtil;
import com.cescloud.saas.archive.api.modular.archivedict.dto.CopyPostDTO;
import com.cescloud.saas.archive.api.modular.archivetype.dto.AutovalueDTO;
import com.cescloud.saas.archive.api.modular.archivetype.entity.Layer;
import com.cescloud.saas.archive.api.modular.archivetype.entity.MetadataAutovalue;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.archivetype.service.MetadataAutovalueService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;


/**
 * 数据规则定义
 *
 * @author liwei
 * @date 2019-04-15 15:16:12
 */
@Api(value = "metadataAutovalue", tags = "应用管理-档案门类管理：数据规则定义")
@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/metadata-autovalue")
public class MetadataAutovalueController {

    private final MetadataAutovalueService metadataAutovalueService;


    @ApiOperation(value = "获取数据规则", httpMethod = "GET")
    @GetMapping("/list")
    public R<List<AutovalueDTO>> getAutovalues(@RequestParam("storageLocate") @ApiParam(name = "storageLocate", value = "档案类型树节点的表名", required = true) @NotBlank(message = "存储表名不能为空")  String storageLocate,
											   @RequestParam("moduleId") @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId) {
        return new R<>(metadataAutovalueService.listByStorageLocate(storageLocate,moduleId));
    }

	@ApiOperation(value = "根据档案类型编码和档案层级获取数据规则", httpMethod = "GET")
	@GetMapping("/list/{moduleId}/{typeCode}/{templateTableId}")
	public R<List<AutovalueDTO>> getAutovaluesByCodeAndLayer(@PathVariable("typeCode") @ApiParam(name = "typeCode", value = "档案类型编码", required = true) String typeCode,
															 @PathVariable("templateTableId") @ApiParam(name = "templateTableId", value = "档案层级", required = true) Long templateTableId,
															 @PathVariable("moduleId")  @ApiParam(name = "moduleId", value = "模块id", required = true) Long moduleId) {
		return new R<>(metadataAutovalueService.getAutovaluesByCodeAndLayer(typeCode,templateTableId,moduleId));
	}

    @ApiOperation(value = "新增数据规则",httpMethod = "POST")
    @SysLog("新增数据规则")
    @PostMapping
    public R save(@RequestBody @Valid
                  @ApiParam(name = "autovalueDTO", value = "数据规则json", required = true)
						  AutovalueDTO autovalueDTO) throws ArchiveBusinessException {
		try {
			String situation=autovalueDTO.getType().equals(0)?"累加":autovalueDTO.getType().equals(1)?"档案字段组成拼接":"当前日期";
			SysLogContextHolder.setLogTitle(String.format("新增数据规则-数据规则类型【%s】",autovalueDTO.getStorageLocate(),situation));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        return metadataAutovalueService.saveMetadataAutovalue(autovalueDTO);
    }


    @ApiOperation(value = "修改数据规则",httpMethod = "PUT")
    @SysLog("修改数据规则")
    @PutMapping
    public R update(@RequestBody @Valid
                    @ApiParam(name = "autovalueDTO", value = "数据规则json", required = true)
							AutovalueDTO autovalueDTO)throws ArchiveBusinessException {
		try {
			String situation=autovalueDTO.getType().equals(0)?"累加":autovalueDTO.getType().equals(1)?"档案字段组成拼接":"当前日期";
			SysLogContextHolder.setLogTitle(String.format("修改数据规则-数据规则类型【%s】",autovalueDTO.getStorageLocate(),situation));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        return metadataAutovalueService.update(autovalueDTO);
    }

    @ApiOperation(value = "删除数据规则",httpMethod = "DELETE")
    @SysLog("删除数据规则")
    @DeleteMapping("/remove")
    public R delete(@RequestParam("id") @ApiParam(name = "id", value = "数据规则id", required = true) Long id,
					@RequestParam("storageLocate") @ApiParam(name = "storageLocate", value = "档案类型树节点的表名", required = true) @NotBlank(message = "存储表名不能为空")  String storageLocate,
					@RequestParam("moduleId")  @ApiParam(name = "moduleId", value = "模块id", required = true) Long moduleId) {
		try {
			MetadataAutovalue byId = metadataAutovalueService.getById(id);
			String situation=byId.getType().equals(0)?"累加":byId.getType().equals(1)?"档案字段组成拼接":"当前日期";
			SysLogContextHolder.setLogTitle(String.format("删除数据规则-操作的档案层级存储表【%s】-数据规则类型【%s】",byId.getStorageLocate(),situation));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        return new R<>(metadataAutovalueService.removeAutovalue(id,storageLocate,moduleId));
    }

    /**
     * 根据id查询档案数据规则
     * @param id id
     * @return R
     */
    @ApiOperation(value = "根据id查询档案数据规则",httpMethod = "GET")
    @GetMapping("/{id}")
    public R<AutovalueDTO> getById(@PathVariable("id")  Long id){
        MetadataAutovalue metadataAutovalue = metadataAutovalueService.getAutovalueById(id);
        AutovalueDTO autovalueVo = new AutovalueDTO();
        BeanUtil.copyProperties(metadataAutovalue,autovalueVo);
        return new R<AutovalueDTO>(autovalueVo);
    }

	@ApiOperation(value = "获取档案门类数据规则信息", httpMethod = SwaggerConstants.GET)
	@GetMapping(value = "/data/{tenantId}")
	@SysLog("获取档案门类数据规则信息")
	public R getDataRuleDefinitionInfo(@PathVariable("tenantId") Long tenantId) throws ArchiveBusinessException, IOException {
		return new R(metadataAutovalueService.getDataRuleDefinitionInfo(tenantId));
	}

	@SysLog("清除数据规则定义配置")
	@ApiOperation(value = "清除数据规则定义配置", httpMethod = SwaggerConstants.DELETE)
	@DeleteMapping("/remove-config")
	public R remove(@RequestParam("storageLocate")@ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
					@RequestParam("moduleId") @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId ){
		return new R(metadataAutovalueService.removeByModuleId(storageLocate,moduleId));
	}
	@SysLog("复制到其他模块")
	@ApiOperation(value = "复制到其他模块接口", httpMethod = SwaggerConstants.POST)
	@PostMapping("/copy")
	public R copyToModule(@Valid @RequestBody @ApiParam(name = "copyPostDTO",value = "复制到其他模块参数DTO") CopyPostDTO copyPostDTO) throws ArchiveBusinessException {
		return metadataAutovalueService.copy(copyPostDTO);
	}
}
