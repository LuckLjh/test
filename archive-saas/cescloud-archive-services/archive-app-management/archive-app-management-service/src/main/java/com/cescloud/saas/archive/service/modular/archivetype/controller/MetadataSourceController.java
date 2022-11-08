
package com.cescloud.saas.archive.service.modular.archivetype.controller;

import com.cescloud.saas.archive.api.modular.archivetype.dto.SourceDTO;
import com.cescloud.saas.archive.service.modular.archivetype.service.MetadataSourceService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 累加规则
 *
 * @author liwei
 * @date 2019-04-16 14:52:34
 */
@Api(value = "metadataSource", tags = "档案门类管理-数据规则定义：累加规则管理")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/metadata-source")
public class MetadataSourceController {

  private final MetadataSourceService metadataSourceService;


  @ApiOperation(value = "获取元数据列表",httpMethod = "GET")
  @GetMapping("/list")
  public R<List<SourceDTO>> getMetaDatas(@RequestParam("id") @ApiParam(name = "id", value = "数据规则id", required = true) Long id,
										 @RequestParam("storageLocate") @ApiParam(name = "storageLocate", value = "表名", required = true) String storageLocate,
										 @RequestParam("metadataId") @ApiParam(name = "metadataId", value = "元数据id", required = true) Long metadataId,
										 @RequestParam("moduleId") @ApiParam(name = "moduleId", value = "模块id", required = true) Long moduleId) {
	return new R<List<SourceDTO>>(metadataSourceService.getMetaDataSources(id,storageLocate,metadataId,moduleId));
  }

  @ApiOperation(value = "获取元数据列表",httpMethod = "GET",notes = "以apma_metadata_source为主表，左关联元字段表，编号中使用")
  @GetMapping("/list/{storageLocate}/{targetId}")
  public R<List<SourceDTO>> getMetadataSourcesByStorageAndTargetId(@PathVariable("storageLocate") @ApiParam(name = "storageLocate", value = "表名", required = true) String storageLocate,
																   @PathVariable("targetId") @ApiParam(name = "targetId", value = "累加元字段id", required = true) Long targetId){
  	return new R<List<SourceDTO>>(metadataSourceService.getMetadataSourcesByStorageAndTargetId(storageLocate,targetId));
  }

}
