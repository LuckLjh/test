
package com.cescloud.saas.archive.service.modular.help.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.help.entity.HelpItems;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;


/**
 * 全局数据权限
 *
 * @author zhaiyachao
 * @date 2021-05-11 18:32:44
 */
public interface HelpItemsService extends IService<HelpItems> {

	List<HelpItems> getHelpList(Integer menuId, Long fondId);

	Boolean updateDataSort(Long fondId, Integer menuId, List<Integer> fileId);

	Boolean delDataByName(Long fondId, Integer menuId, Long fileId);

	Boolean recoverSystemDefault(Long fondId, Integer menuId);

	Map<String, Boolean> queryMap(Long fondId, Integer menuId, Integer fileId);

	Map<String, Object> uploadHelpMap(MultipartFile file, Long fondId, Integer menuId);
}
