package com.cescloud.saas.archive.api.modular.filingscope.feign;

import com.cescloud.saas.archive.api.modular.filingscope.dto.FilingScopeDTO;
import com.cescloud.saas.archive.api.modular.filingscope.entity.FilingScope;
import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(contextId = "remoteFilingScopeService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteFilingScopeService {

	/**
	 * 归档范围初始化
	 * @param templateId 模板id
	 * @param tenantId 租户id
	 * @return
	 */
	@RequestMapping(value = "/filing-scope/initialize",method = RequestMethod.POST)
	R initializeHandle(@RequestParam(value = "templateId", required = false) Long templateId, @RequestParam(value = "tenantId") Long tenantId);

	/**
	 * 获取归档范围信息
	 * @param tenantId 租户id
	 * @return
	 */
	@RequestMapping(value = "/filing-scope/data/{tenantId}",method =RequestMethod.GET)
	R getFilingRangeInfo(@PathVariable("tenantId") Long tenantId);

	/**
	 * 根据档案类型分类号获取归档范围数据
	 * @param classNo
	 * @return
	 */
	@RequestMapping(value = "/filing-scope/type-code-class-no/{typeCode}/{classNo}")
	R<FilingScope> getFilingScopeByTypeCodeClassNo(@PathVariable("typeCode") String typeCode,@PathVariable("classNo") String classNo);

	/**
	 * 根据分类号获取归档范围数据
	 * @param classNo
	 * @return
	 */
	@RequestMapping(value = "/filing-scope/inner/class-no/classNo")
	R<FilingScope> getFilingScopeByClassNoInner( @RequestParam("classNo") String classNo, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 根据查询条件确定归档范围信息
	 * @param filingScopeDTO 查询条件
	 * @return R
	 */
	@GetMapping(value = "/filing-scope/query")
	R<List<FilingScope>> getFilingScope(@SpringQueryMap FilingScopeDTO filingScopeDTO);


	/**
	 * 获取所有的归档范围信息
	 * @return
	 */
	@RequestMapping(value = "/filing-scope/list",method = RequestMethod.GET)
	R<List<FilingScope>> list(@RequestParam(value = "tenantId") Long tenantId, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 更新全宗的时候 也去更新归档范围里根据全宗绑定的节点
	 * @param fondsName 全宗名称
	 * @param fondsCode	全宗代码
	 */
	@GetMapping("/filing-scope/updateArchiveFilingScopeTree")
	void updateArchiveFilingScopeTree(@RequestParam("fondsName") String fondsName , @RequestParam("fondsCode") String fondsCode);

	@GetMapping("/filing-scope/tree/{id}")
	R<List<FilingScopeDTO>> getTreeById(@PathVariable("id") Long id, @RequestParam("fondsCodes") List<String> fondsCodes);

}
