package com.cescloud.saas.archive.api.modular.fonds.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.api.modular.fonds.dto.FondsDTO;
import com.cescloud.saas.archive.api.modular.fonds.entity.Fonds;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Package: com.cescloud.saas.archive.api.modular.fonds.feign
 * @Classname RemoteFondsService
 * @Description TODO
 * @Date 2019-05-06 12:57
 * @Created by zhangpeng
 */

@FeignClient(contextId = "remoteFondsService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteFondsService {
	@GetMapping("/fonds/{fondsId}")
	R<Fonds> getFondsById(@PathVariable("fondsId") Long fondsId);

	/***
	 * 根据全宗名查全宗(此处要传特殊的menu_Id)
	 * todo: 因写死了MenuId,后期需要调整
	 * @param fondsName
	 * @return
	 */
	@GetMapping(value = "/fonds/details/{fondsName}",headers = {"IS_NOT_AUTHORITY="})
	R<Fonds> getFondsByName(@PathVariable("fondsName") String fondsName);

	/***
	 * 根据全宗号查全宗
	 * @param fondsCode
	 * @return
	 */
	@GetMapping(value = "/fonds/code/{fondsCode}")
	R<Fonds> getFondsByCode(@PathVariable("fondsCode") String fondsCode);

	/**
	 * 新增
	 *
	 * @param fonds
	 * @return
	 */
	@PostMapping("/fonds")
	R save(@RequestBody Fonds fonds);

	/**
	 * 查询所有全宗
	 *
	 * @return
	 */
	@GetMapping("/fonds/list")
	R<List<Fonds>> list();

	@PostMapping("/fonds/page")
	R<Page<Fonds>> getAuthFondsPage(@RequestBody FondsDTO fondsDTO);

    /**
     * 查询所有租户的全宗总数
     * @return
     */
    @GetMapping("/fonds/total-count")
    R<Integer> totalCount();

	@GetMapping("/fonds/current-user/list")
	R<List<Fonds>> getCurrentUserFondsData();

	@PostMapping("/fonds/check")
	R<Boolean> checkFonds(@RequestParam(value = "fondsCode") String fondsCode, @RequestParam(value = "fondsName")  String fondsName);
}
