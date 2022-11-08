/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.archivetree.service</p>
 * <p>文件名:FondsArchiveTreeService.java</p>
 * <p>创建时间:2019年5月13日 下午2:38:10</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.archivetree.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archivetree.dto.FondsArchiveTreeDTO;
import com.cescloud.saas.archive.api.modular.archivetree.entity.FondsArchiveTree;

/**
 * 全宗与档案树绑定
 * 
 * @author qiucs
 * @version 1.0.0 2019年5月13日
 */
public interface FondsArchiveTreeService  extends IService<FondsArchiveTree> {


    /**
     * 全宗与档案树绑定（一棵树可以绑定多个全宗号）
     *
     * @param entity 绑定对象
     *
     * @return List
     */
    List<FondsArchiveTree> save(FondsArchiveTreeDTO entity);


    /**
     * 全宗与档案树批量绑定
     *
     * @param entityList 绑定对象集合
     *
     * @return List
     */
    List<FondsArchiveTree> saveBatch(List<FondsArchiveTree> entityList);

	/***
	 * 根据全宗编码查看是否和档案树进行了绑定
	 *
	 */
	List<FondsArchiveTree> getFondsArchiveTreeByFondsCode(String code);
}
