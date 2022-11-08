package com.cescloud.saas.archive.api.modular.businessconfig.feign;

import com.cescloud.saas.archive.api.modular.businessconfig.entity.BusinessModelDefine;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author 王谷华
 */
@FeignClient(contextId = "remoteDynamicModelDefineService", value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteDynamicModelDefineService {


	/**
	 * 创建业务表
	 * @param code
	 * @param modelType
	 * @param fondsCode
	 * @return
	 */
    @GetMapping("/dynamic-model-define/create-table/{code}/{modelType}/{fondsCode}")
    R createTable(@PathVariable("code") @ApiParam(value = "分类code", name = "tenantId", required = true) @NotNull(message = "分类code不能为空") String code,
				  @PathVariable("modelType") @ApiParam(value = "模板类型", name = "modelType", required = true) @NotNull(message = "模板类型不能为空") Integer modelType,
				  @PathVariable("fondsCode") @ApiParam(value = "全宗编号", name = "fondsCode", required = true) @NotNull(message = "全宗编号不能为空") String fondsCode);


	/**
	 * 删除业务表
	 * @param code
	 * @param modelType
	 * @return
	 */
	@GetMapping("/dynamic-model-define/drop-table/{code}/{modelType}")
	R dropTable(@PathVariable("code") @ApiParam(value = "分类code", name = "tenantId", required = true) @NotNull(message = "分类code不能为空") String code,
				  @PathVariable("modelType") @ApiParam(value = "模板类型", name = "modelType", required = true) @NotNull(message = "模板类型不能为空") Integer modelType);


	@GetMapping("/dynamic-model-define/all/list")
    R<List<BusinessModelDefine>> getAllBusinessModelDefines(@RequestParam(value = "modelType", required = false) Integer modelType, @RequestParam(value = "modelCode", required = false) String modelCode);

	@GetMapping("/dynamic-model-define/getDynamicFields")
	R<List<String>> getDynamicFields(@RequestParam(value = "modelType", required = false) Integer modelType, @RequestParam(value = "modelCode", required = false) String modelCode, @RequestParam(value = "keyword", required = false) String keyword);

}
