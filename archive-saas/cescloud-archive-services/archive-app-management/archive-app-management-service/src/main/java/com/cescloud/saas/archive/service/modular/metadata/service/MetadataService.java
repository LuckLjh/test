
package com.cescloud.saas.archive.service.modular.metadata.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateTable;
import com.cescloud.saas.archive.api.modular.metadata.dto.MetadataDTO;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataTag;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 档案元数据
 *
 * @author liudong1
 * @date 2019-03-28 09:42:53
 */
public interface MetadataService extends IService<Metadata> {

	/**
	 * 元数据分页查询
	 *
	 * @param page          分页对象
	 * @param storageLocate 存储表名
	 * @param keyword       检索关键字
	 * @return 分页对象
	 */
	IPage<Metadata> getPage(Page page, String storageLocate, String keyword);

	/**
	 * 根据元数据id获取对象
	 *
	 * @param id 元数据id
	 * @return com.cescloud.saas.archive.api.modular.metadata.entity.Metadata
	 */
	Metadata getMetadataById(Long id);

	/**
	 * 根据条件删除元数据
	 *
	 * @param queryWrapper
	 * @return
	 */
	boolean removeByWrapper(Wrapper<Metadata> queryWrapper);

	/**
	 * 根据表名获取所有业务元数据
	 *
	 * @param storageLocate 档案存储表名（业务字段）
	 * @return
	 */
	List<Metadata> listByStorageLocate(String storageLocate);

	/**
	 * 根据表名获取所有业务元数据(所有字段)
	 *
	 * @param storageLocate 档案存储表名
	 * @return
	 */
	List<Metadata> allByStorageLocate(String storageLocate);

	/**
	 * 根据表名获取所有元数据(业务+系统)
	 *
	 * @param storageLocate 档案存储表名
	 * @return
	 */
	List<Metadata> listAllByStorageLocate(String storageLocate);

	/**
	 * 根据表名id获取所有元数据
	 *
	 * @param tableId 表名id
	 * @return
	 */
	List<Metadata> listAllByTableId(Long tableId);

	/**
	 * 根据存储表名和元字段英文名获取元数据绑定的字典值
	 *
	 * @param storageLocate   存储表名
	 * @param metadataEnglish 元字段英文名称
	 * @return
	 * @throws ArchiveBusinessException
	 */
	List<Map<String, String>> getEditFormSelectOption(String storageLocate,String typeCode, String metadataEnglish) throws ArchiveBusinessException;

	/**
	 * 新增档案元数据
	 *
	 * @param metadata 元数据实体
	 * @return
	 * @throws ArchiveBusinessException
	 */
	Metadata saveMetadata(Metadata metadata) throws ArchiveBusinessException;

	/**
	 * 修改档案元数据
	 *
	 * @param metadata
	 * @return
	 * @throws ArchiveBusinessException
	 */
	Metadata updateMetadata(Metadata metadata) throws ArchiveBusinessException;

	/**
	 * 根据id删除档案元数据
	 *
	 * @param id 元数据id
	 * @return
	 * @throws ArchiveBusinessException
	 */
	Metadata removeMetadata(Long id) throws ArchiveBusinessException;

	/**
	 * 根据表名和英文字段名获取元字段
	 *
	 * @param storageLoacte   表名
	 * @param metadataEnglish 字段英文名
	 * @return com.cescloud.saas.archive.api.modular.metadata.entity.Metadata
	 * @throws ArchiveBusinessException
	 */
	Metadata getByStorageLocateAndMetadataEnglish(String storageLoacte, String metadataEnglish) throws ArchiveBusinessException;

	/**
	 * 检验元数据是否绑定标签
	 *
	 * @param tagId
	 * @return
	 */
	Boolean checkBindedTag(String tagId);

	/**
	 * 根据表名获取元数据信息（包括标签信息）
	 *
	 * @param storageLocate 存储表名
	 * @return
	 */
	List<MetadataDTO> getMetadataDTOList(String storageLocate);

	/**
	 * 自动绑定所有档案类型字段
	 *
	 * @param metadataTag
	 */
	void autoBindingTag(MetadataTag metadataTag);

	/**
	 * 根据档案类型编码和档案层次获取元数据列表
	 *
	 * @param typeCode
	 * @param templateTableId
	 * @return
	 * @throws ArchiveBusinessException
	 */
	List<Metadata> listByTypeCodeAndTemplateTableId(String typeCode, Long templateTableId) throws ArchiveBusinessException;

	/**
	 * mysql特有的高效的批量保存的方法
	 *
	 * @param metadatas 元数据集合
	 * @return
	 */
	boolean saveBatchForMysql(List<Metadata> metadatas);

	void insertIntoMetadataFromTemplate(ArchiveTable archiveTable, TemplateTable templateTable) throws ArchiveBusinessException;

	List<Metadata> getBakMetadataList(ArchiveTable archiveTable);

	/**
	 * 获取门类字段信息
	 *
	 * @param tenantId 租户id
	 * @return
	 */
	List<ArrayList<String>> getFieldManagementInfo(Long tenantId);

	/**
	 * 初始化门类字段
	 *
	 * @param templateId 模板id
	 * @param tenantId   租户id
	 * @return
	 * @throws ArchiveBusinessException
	 */
	R initializeMetadata(Long templateId, Long tenantId) throws ArchiveBusinessException;

	/**
	 * 校验表单输入字符是否重复
	 * @param typeCode
	 * @param templateTableId
	 * @param fieldName
	 * @param value
	 * @return
	 */
	R verifyFormField(String typeCode, Long templateTableId, String fieldName, String value,Long id);

	/**
	 * 获取所有租户的元字段信息，在项目启动时（spring启动完成发布WebServerInitializedEvent事件）查询放入缓存中
	 * 只查询 storageLocate、metadataEnglish、metadataType、metadataLength字段
	 * @param current 当前页
	 * @param size 每页的记录数
	 * @return
	 */
	IPage<Metadata> getAllTenantMetadatas(long current,long size);

	/**
	 * 根据租户id查询所有的元数据
	 * @param current 当前页
	 * @param size 每页记录数
	 * @param tenantId 租户id
	 * @return
	 */
	IPage<Metadata> getTenantMetadatas(long current,long size,Long tenantId);

	/**
	 * 根据表名获取重复的元字段
	 * @param storageLocate 表名
	 * @return
	 */
	List<Metadata> getRepeatMetadatasByStorageLocate(String storageLocate);
}
