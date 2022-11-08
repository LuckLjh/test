
package com.cescloud.saas.archive.service.modular.archivetree.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.archivetree.entity.ArchiveTree;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 档案树定义
 *
 * @author qiucs
 * @date 2019-04-12 13:36:59
 */
public interface ArchiveTreeMapper extends BaseMapper<ArchiveTree> {

    /**
     * 获取最大排序号
     *
     * @param parentId
     *            父节点ID
     *
     * @return 返回最大排序号
     */
    public Integer getMaxSortNoByParentId(Long parentId);

    /**
     * 获取最大树编码
     *
     * @param parentId
     *            父节点ID
     *
     * @return 返回最大树编码
     */
    public String getMaxTreeCodeByParentId(Long parentId);

    /**
     * 统计指定父节点下的指定树编码的数量
     *
     * @param parentId
     *            父节点ID
     * @param treeCode
     *            树编码
     *
     * @return 数量
     */
    public Integer countByParentIdAndTreCode(@Param("parentId") Long parentId, @Param("treeCode") String treeCode);

    /**
     * 更新全宗号
     *
     * @param oldFondsCode
     *            旧的全宗号
     * @param newFondsCode
     *            新的全宗号
     */
    public void updateFondsCode(@Param("oldFondsCode") String oldFondsCode, @Param("newFondsCode") String newFondsCode);

    /**
     * 更新档案门类节点showLayer/isLeaf值
     *
     * @param treeCode
     * @param isLeaf
     */
    public void updateArchiveNode(@Param("treeCode") String treeCode, @Param("showLayer") Boolean showLayer,
        @Param("isLeaf") boolean isLeaf);

	List<ArchiveTree> getDistinctFondsCode(@Param("fondsCodeList") List<String> fondsCodeList);

	/**
	 * 根据父节点id获取子节点(联表查询档案门类分类标识,归档范围分类名称)
	 * @param parentId 父id
	 * @return List<ArchiveTree>
	 */
	List<ArchiveTree> getArchiveTreeByParentId(@Param("parentId") Long parentId);
}
