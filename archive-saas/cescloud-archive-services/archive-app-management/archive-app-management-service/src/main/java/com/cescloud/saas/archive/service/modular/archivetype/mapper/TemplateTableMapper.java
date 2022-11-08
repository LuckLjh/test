/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.archivetype.mapper</p>
 * <p>文件名:TemplateTableMapper.java</p>
 * <p>创建时间:2020年2月14日 下午4:49:41</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.archivetype.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateTable;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年2月14日
 */
public interface TemplateTableMapper extends BaseMapper<TemplateTable> {

	/**
	 * 保存模板信息
	 * @param templateTable
	 * @return
	 */
	int saveTemplateTable(@Param("templateTable") TemplateTable templateTable);

	/**
	 * 批量保存模板信息
	 * @param list
	 * @return
	 */
	int saveTemplateTableBatch(@Param("list") List<TemplateTable> list);

    /**
     * 获取最大排序号
     *
     * @return
     */
	Integer getMaxSortNo();

	/**
	 * 根据档案类型 获取表模板信息
	 * @param archiveCode
	 * @return
	 */
    List<TemplateTable> getListByArchiveCode(@Param("archiveCode") String archiveCode);

}
