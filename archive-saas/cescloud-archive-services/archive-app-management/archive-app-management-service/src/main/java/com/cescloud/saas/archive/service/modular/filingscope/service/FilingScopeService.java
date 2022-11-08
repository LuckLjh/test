
package com.cescloud.saas.archive.service.modular.filingscope.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.ArchiveTreeQueryDTO;
import com.cescloud.saas.archive.api.modular.filingscope.dto.*;
import com.cescloud.saas.archive.api.modular.filingscope.entity.FilingScope;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;


/**
 * 归档范围定义
 *
 * @author xieanzhu
 * @date 2019-04-22 15:45:22
 */
public interface FilingScopeService extends IService<FilingScope> {

	/**
	 * 根据id查询归档范围节点信息
	 *
	 * @param id 归档范围节点ID
	 * @return
	 * @throws ArchiveBusinessException
	 */
	FilingScopeDTO getFilingScopeDTOById(Long id) throws ArchiveBusinessException;

	/**
	 * 新增归档范围定义
	 *
	 * @param filingScopePostDTO 归档范围保存实体
	 * @return
	 * @throws ArchiveBusinessException
	 */
	FilingScopeDTO saveFilingScope(FilingScopePostDTO filingScopePostDTO) throws ArchiveBusinessException;

	/**
	 * 修改归档范围定义
	 *
	 * @param filingScopePutDTO
	 * @return
	 * @throws ArchiveBusinessException
	 */
	FilingScopeDTO updateFilingScope(FilingScopePutDTO filingScopePutDTO) throws ArchiveBusinessException;

	/**
	 * 根据id查询归档范围子节点列表
	 *
	 * @param parentClassId 归档范围节点id
	 * @return
	 * @throws ArchiveBusinessException
	 */
	List<FilingScopeDTO> findFilingScopeByParentClassId(Long parentClassId, List<String> fondsCodes) throws ArchiveBusinessException;

	Integer selectMaxSortNo(Long parentClassId);

	/**
	 * 获取所有子节点
	 * @param list
	 * @param allChildrenFilingScopeList
	 */
	void getAllChildrenFilingScope(List<FilingScope> list, List<FilingScope> allChildrenFilingScopeList);

	/**
	 * 根据id删除归档范围定义
	 *
	 * @param id 归档范围定义id
	 * @return
	 */
	R deleteById(Long id) throws ArchiveBusinessException;

	/**
	 * 导出归档范围树
	 *
	 * @param id
	 * @param response
	 * @return
	 * @throws ArchiveBusinessException
	 */
	void exportExcel(Long id, HttpServletResponse response) throws ArchiveBusinessException;

	/**
	 * 导入归档范围
	 *
	 * @param file
	 * @param id
	 * @return
	 * @throws ArchiveBusinessException
	 */
	Boolean importExcel(MultipartFile file, Long id) throws ArchiveBusinessException;

	/**
	 * 归档范围初始化
	 *
	 * @param templateId
	 * @param tenantId
	 * @return
	 * @throws ArchiveBusinessException
	 */
	R initializeHandle(Long templateId, Long tenantId) throws ArchiveBusinessException;


	/**
	 * 获取租户归档范围 树 信息
	 *
	 * @param tenantId
	 * @return
	 */
	List<ArrayList<String>> getFilingRangeTreeNodeInfo(Long tenantId);

	/**
	 * 根据档案类型分类号查询归档范围
	 * @param typeCode 档案类型
	 * @param classNo 分类号
	 * @return
	 */
	FilingScope getFilingScopeByTypeCodeClassNo(String typeCode,String classNo);


	/**
	 * 根据分类号查询归档范围
	 *
	 * @param classNo 分类号
	 * @return
	 */
	FilingScope getFilingScopeByClassNo(String classNo);

	/**
	 * 复制归档范围定义
	 *
	 * @param filingScopeCopyPostDTO 归档范围保存实体
	 * @return
	 * @throws ArchiveBusinessException
	 */
	FilingScopeDTO copyFilingScope(FilingScopeCopyPostDTO filingScopeCopyPostDTO) throws ArchiveBusinessException;

	/**
	 * 获取列表主要信息 列表
	 *
	 * @param parentClassId
	 * @param result
	 */
	List<Long> getAllMainInfoForChildren(Long parentClassId, List<Long> result);

	List<FilingScope> getFilingScopeByArchiveType(List<String> archiveTypeCodeList);

	/**
	 * 归档范围拖动排序
	 *
	 * @param filingScopeOrderDTO 排序DTO
	 * @return Boolean
	 */
	Boolean filingScopeOrder(FilingScopeOrderDTO filingScopeOrderDTO);

	/**
	 * 根据父id查询归档范围树
	 *
	 * @param archiveTreeQueryDTO 查询条件
	 * @return List<FilingScope>
	 */
	List<FilingScope> getFilingScopeByParentId(ArchiveTreeQueryDTO archiveTreeQueryDTO);

	/**
	 * 根据查询条件查询归档范围信息
	 * @param filingScopeDTO 查询条件
	 * @return
	 */
	List<FilingScope> getFilingScope(FilingScopeDTO filingScopeDTO);

	void updateArchiveFilingScopeTree(String fondsName , String fondsCode);


	/**
	 * 导出归档范围信息模板
	 */
	void downloadExcelTemplate(HttpServletResponse response) throws ArchiveBusinessException;
}
