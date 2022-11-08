package com.cescloud.saas.archive.api.modular.archivedict.feign;

import com.cescloud.saas.archive.api.modular.archivedict.entity.DictItem;
import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/*
@auth xaz
@date 2019/6/14 - 16:52
*/
@FeignClient(contextId = "remoteDictItemService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteDictItemService {
	@GetMapping("/dict-item/list/{dictCode}")
	public R<List<DictItem>> getByDictCode(@PathVariable("dictCode") String dictCode);

	@GetMapping("/dict-item/inner/list/{dictCode}")
	public R<List<DictItem>> getInnerByDictCode(@PathVariable("dictCode") String dictCode, @RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping("/dict-item/list/codes")
	public R<List<DictItem>> getDictItemListByDictCodes(@RequestParam("dictCodes") String dictCodes);

	/**
	 * 获取数据字典数据
	 *
	 * @param tenantId
	 * @return
	 */
	@RequestMapping(value = "/dict-item/data/{tenantId}", method = RequestMethod.GET)
	public R<List<ArrayList<String>>> getDataDictionary(@PathVariable("tenantId") Long tenantId);

	@GetMapping("/dict-item/getDictCode")
	public R<DictItem> getDictItemByDictCodeAndItemLabel(@RequestParam("dictCode") String dictCode, @RequestParam("itemLabel") String itemLabel,@RequestParam("tenantId") Long tenantId);

	@GetMapping("/dict-item/getDictItemByItemCode")
	public R<DictItem> getDictItemByDictCodeAndItemCode(@RequestParam("dictCode") String dictCode, @RequestParam("itemCode") String itemCode,@RequestParam("tenantId") Long tenantId);
}
