package com.cescloud.saas.archive.api.modular.archivetype.feign;

import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateTable;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@FeignClient(contextId = "remoteTemplateTableService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT, path = "/template-table")
public interface RemoteTemplateTableService {

    /**
     * 获取门类-档案类型表模板信息
     *
     * @param tenantId
     * @return
     */
    @GetMapping(value = "/data/{tenantId}")
    R<List<ArrayList<String>>> getArchivesTypeTableTemplateInfor(@PathVariable("tenantId") Long tenantId);

    /**
     * 获取表模板对应的父级
     *
     * @param id
     * @return
     */
    @GetMapping("/list/parents/{id}")
    R<List<TemplateTable>> getParentListById(@PathVariable("id") Long id);

	/**
	 * 根据档案类型获取表模板信息
	 * @param archiveCode 档案类型
	 * @return
	 */
	@GetMapping("/list-code")
	public R<List<TemplateTable>> getListByArchiveCode(@RequestParam("archiveCode")  String archiveCode);

	/**
	 * 根据id获取表模板信息
	 * @param id
	 * @return
	 */
	@GetMapping("/{id}")
	public R<TemplateTable> getById(@PathVariable("id") Long id);

	/**
	 * 表模板对应条目子模板列表
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/list/entry-child/{id}")
	R<List<TemplateTable>> getEntryChildListById(@PathVariable("id") Long id);


	/**
	 * 获取表模板对应条目和所有子模板列表
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/list/entry-childs/{id}")
	public R<List<TemplateTable>> getAllChildListById(@PathVariable("id") Long id);
}
