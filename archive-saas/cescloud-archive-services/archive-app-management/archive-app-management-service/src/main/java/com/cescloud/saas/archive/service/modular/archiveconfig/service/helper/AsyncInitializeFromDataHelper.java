package com.cescloud.saas.archive.service.modular.archiveconfig.service.helper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cescloud.saas.archive.api.modular.account.entity.AccountModule;
import com.cescloud.saas.archive.api.modular.account.entity.AccountTemplate;
import com.cescloud.saas.archive.api.modular.account.entity.AccountTemplateRole;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.*;
import com.cescloud.saas.archive.api.modular.archivedict.entity.Dict;
import com.cescloud.saas.archive.api.modular.archivedict.entity.DictItem;
import com.cescloud.saas.archive.api.modular.archivetree.entity.ArchiveTree;
import com.cescloud.saas.archive.api.modular.archivetree.entity.FondsArchiveTree;
import com.cescloud.saas.archive.api.modular.archivetype.entity.*;
import com.cescloud.saas.archive.api.modular.businessconfig.entity.BusinessModelDefine;
import com.cescloud.saas.archive.api.modular.businessconfig.entity.BusinessStyleSetting;
import com.cescloud.saas.archive.api.modular.businessconfig.entity.DynamicModelDefine;
import com.cescloud.saas.archive.api.modular.downloads.entity.CommonDownloads;
import com.cescloud.saas.archive.api.modular.filingscope.entity.FilingScope;
import com.cescloud.saas.archive.api.modular.filingscope.entity.FilingScopeType;
import com.cescloud.saas.archive.api.modular.fonds.entity.Fonds;
import com.cescloud.saas.archive.api.modular.keyword.entity.KeyWord;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataTag;
import com.cescloud.saas.archive.api.modular.notice.entity.Notice;
import com.cescloud.saas.archive.api.modular.notice.entity.NoticeFile;
import com.cescloud.saas.archive.api.modular.relationrule.entity.ArchiveRetentionRelation;
import com.cescloud.saas.archive.api.modular.report.entity.Report;
import com.cescloud.saas.archive.api.modular.report.entity.ReportMetadata;
import com.cescloud.saas.archive.api.modular.report.entity.ReportTable;
import com.cescloud.saas.archive.api.modular.synonymy.entity.Synonymy;
import com.cescloud.saas.archive.api.modular.syssetting.entity.SysSetting;
import com.cescloud.saas.archive.service.modular.account.service.AccountModuleService;
import com.cescloud.saas.archive.service.modular.account.service.AccountTemplateRoleService;
import com.cescloud.saas.archive.service.modular.account.service.AccountTemplateService;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.*;
import com.cescloud.saas.archive.service.modular.archivedict.service.DictItemService;
import com.cescloud.saas.archive.service.modular.archivedict.service.DictService;
import com.cescloud.saas.archive.service.modular.archivetree.service.ArchiveTreeService;
import com.cescloud.saas.archive.service.modular.archivetree.service.FondsArchiveTreeService;
import com.cescloud.saas.archive.service.modular.archivetype.service.*;
import com.cescloud.saas.archive.service.modular.businessconfig.async.AsyncUpdateFieldConfiguration;
import com.cescloud.saas.archive.service.modular.businessconfig.service.BusinessModelDefineService;
import com.cescloud.saas.archive.service.modular.businessconfig.service.BusinessStyleSettingService;
import com.cescloud.saas.archive.service.modular.businessconfig.service.DynamicModelDefineService;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.downloads.service.CommonDownloadsService;
import com.cescloud.saas.archive.service.modular.filingscope.service.FilingScopeService;
import com.cescloud.saas.archive.service.modular.filingscope.service.FilingScopeTypeService;
import com.cescloud.saas.archive.service.modular.fonds.service.FondsService;
import com.cescloud.saas.archive.service.modular.keyword.service.KeyWordService;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataService;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataTagService;
import com.cescloud.saas.archive.service.modular.notice.service.NoticeFileService;
import com.cescloud.saas.archive.service.modular.notice.service.NoticeService;
import com.cescloud.saas.archive.service.modular.relationrule.service.ArchiveRetentionRelationService;
import com.cescloud.saas.archive.service.modular.report.service.ReportMetadataService;
import com.cescloud.saas.archive.service.modular.report.service.ReportService;
import com.cescloud.saas.archive.service.modular.report.service.ReportTableService;
import com.cescloud.saas.archive.service.modular.stats.service.ArchiveStatsService;
import com.cescloud.saas.archive.service.modular.stats.service.FilingStatsService;
import com.cescloud.saas.archive.service.modular.stats.service.YearStatsService;
import com.cescloud.saas.archive.service.modular.synonymy.service.SynonymyService;
import com.cescloud.saas.archive.service.modular.syssetting.service.SysSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @ClassName AsyncInitializeFromDataHelper
 * @Author zhangxuehu
 * @Date 2020/4/12 09:20
 **/
@Component
@Slf4j
@RequiredArgsConstructor
public class AsyncInitializeFromDataHelper {

	private final ArchiveEditService archiveEditService;

	private final ArchiveEditFormService archiveEditFormService;

	private final ArchiveListService archiveListService;

	private final ArchiveSearchService archiveSearchService;

	private final ArchiveSortService archiveSortService;

	private final MetadataAutovalueService metadataAutovalueService;

	private final InnerRelationService innerRelationService;

	private final TagListService tagListService;

	private final TagSearchService tagSearchService;

	private final ArchiveTableService archiveTableService;

	private final ArchiveColumnRuleService archiveColumnRuleService;

	private final ArchiveOperateService archiveOperateService;

	private final LinkColumnRuleService linkColumnRuleService;

	private final LinkLayerService linkLayerService;

	private final MetadataSourceService metadataSourceService;

	private final ReportService reportService;

	private final ReportMetadataService reportMetadataService;

	private final ReportTableService reportTableService;

	private final ArchiveTreeService archiveTreeService;

	private final DictService dictService;

	private final DictItemService dictItemService;

	private final FondsService fondsService;

	private final FondsArchiveTreeService fondsArchiveTreeService;

	private final KeyWordService keyWordService;

	private final SynonymyService synonymyService;

	private final SysSettingService sysSettingService;

	private final FilingScopeService filingScopeService;

	private final MetadataTagService metadataTagService;

	private final MetadataService metadataService;

	private final ArchiveTypeService archiveTypeService;

	private final TemplateTypeService templateTypeService;

	private final TemplateTableService templateTableServer;

	private final TemplateMetadataService templateMetadataService;

	private final BusinessModelDefineService businessModelDefineService;

	private final BusinessStyleSettingService businessStyleSettingService;

	private final ArchiveConfigManageService archiveConfigManageService;

	private final FilingScopeTypeService filingScopeTypeService;

	private final MetadataBoxConfigService metadataBoxConfigService;

	private final MetadataBoxRuleService metadataBoxRuleService;

	private final ArchiveStatsService archiveStatsService;

	private final FilingStatsService filingStatsService;

	private final YearStatsService yearStatsService;

	private final NoticeService noticeService;

	private final NoticeFileService noticeFileService;

	private final ArchiveRetentionRelationService archiveRetentionRelationService;

	private final WatermarkService watermarkService;

	private final WatermarkDetailService watermarkDetailService;

	private final AccountModuleService accountModuleService;

	private final AccountTemplateService accountTemplateService;

	private final AccountTemplateRoleService accountTemplateRoleService;

	private final CommonDownloadsService commonDownloadsService;

	private final DispAppraisalRuleService dispAppraisalRuleService;

	private final DynamicModelDefineService dynamicModelDefineService;

	private final MetadataCheckrepeatService metadataCheckrepeatService;

	/**
	 * 初始化表单信息
	 */
	@Async(AsyncUpdateFieldConfiguration.ASYNC_EXECUTOR_NAME)
	public void initializeEditForm(Long templateId, Long tenantId, CountDownLatch latch) throws ArchiveBusinessException {
		//初始化 表单字段信息
		try {
			R r = archiveEditService.initializeHandle(templateId, tenantId);
			//初始化表单信息
			R r1 = archiveEditFormService.initializeHandle(templateId, tenantId);
			if (r.getCode() == CommonConstants.FAIL || r1.getCode() == CommonConstants.FAIL) {
				throw new ArchiveBusinessException("初始化表单信息失败");
			}
		} finally {
			latch.countDown();
		}

	}

	/**
	 * 初始化列表定义
	 */
	@Async(AsyncUpdateFieldConfiguration.ASYNC_EXECUTOR_NAME)
	public void initializeArchiveList(Long templateId, Long tenantId, CountDownLatch latch) throws ArchiveBusinessException {
		//初始化列表定义
		try {
			R r = archiveListService.initializeHandle(templateId, tenantId);
		} finally {
			latch.countDown();
		}
	}

	/**
	 * 初始化检索定义
	 */
	@Async(AsyncUpdateFieldConfiguration.ASYNC_EXECUTOR_NAME)
	public void initializeArchiveSearch(Long templateId, Long tenantId, CountDownLatch latch) throws ArchiveBusinessException {
		//初始化检索定义
		try {
			R r = archiveSearchService.initializeHandle(templateId, tenantId);
		} finally {
			latch.countDown();
		}
	}

	/**
	 * 初始化排序定义
	 */
	@Async(AsyncUpdateFieldConfiguration.ASYNC_EXECUTOR_NAME)
	public void initializeArchiveSort(Long templateId, Long tenantId, CountDownLatch latch) throws ArchiveBusinessException {
		//初始化排序定义
		try {
			R r = archiveSortService.initializeHandle(templateId, tenantId);
		} finally {
			latch.countDown();
		}
	}

	/**
	 * 初始化数据规则定义
	 */
	@Async(AsyncUpdateFieldConfiguration.ASYNC_EXECUTOR_NAME)
	public void initializeMetadataAutovalue(Long templateId, Long tenantId, CountDownLatch latch) throws ArchiveBusinessException {
		//初始化数据规则定义
		try {
			R r = metadataAutovalueService.initializeDataRule(templateId, tenantId);
		} finally {
			latch.countDown();
		}
	}

	/**
	 * 初始化装盒规则定义
	 */
	@Async(AsyncUpdateFieldConfiguration.ASYNC_EXECUTOR_NAME)
	public void initializeMetadataBoxConfig(Long templateId, Long tenantId, CountDownLatch latch) throws ArchiveBusinessException {
		//初始化装盒规则定义
		try {
			R r = metadataBoxConfigService.initializeHandle(templateId, tenantId);
		} finally {
			latch.countDown();
		}
	}

	/**
	 * 初始化关联关系
	 */
	@Async(AsyncUpdateFieldConfiguration.ASYNC_EXECUTOR_NAME)
	public void initializeInnerRelation(Long templateId, Long tenantId, CountDownLatch latch) throws ArchiveBusinessException {
		//初始化关联关系
		try {
			R r = innerRelationService.initializeInnerRelation(templateId, tenantId);
		} finally {
			latch.countDown();
		}
	}

	/**
	 * 初始化元数据检索列表配置
	 */
	@Async(AsyncUpdateFieldConfiguration.ASYNC_EXECUTOR_NAME)
	public void initializeTagList(Long templateId, Long tenantId, CountDownLatch latch) throws ArchiveBusinessException {
		// 初始化 元数据检索列表配置
		try {
			tagListService.initializeHandle(templateId, tenantId);
		} finally {
			latch.countDown();
		}
	}

	/**
	 * 初始化元数据检索字段配置
	 */
	@Async(AsyncUpdateFieldConfiguration.ASYNC_EXECUTOR_NAME)
	public void initializeTagSearch(Long templateId, Long tenantId, CountDownLatch latch) throws ArchiveBusinessException {
		//初始化 元数据检索字段配置
		try {
			tagSearchService.initializeHandle(templateId, tenantId);
		} finally {
			latch.countDown();
		}
	}

	@Async(AsyncUpdateFieldConfiguration.ASYNC_EXECUTOR_NAME)
	public void initializeAccount(Long templateId, Long tenantId, CountDownLatch latch) throws ArchiveBusinessException {
		try {
			accountModuleService.initializeAccountModule(tenantId);
			accountTemplateService.initializeAccountTemplate(templateId, tenantId);
		} finally {
			latch.countDown();
		}
	}

	/**
	 * 清除 档案门类 配置信息(清除 数据规则 列表定义 表单定义 检索定义 排序定义)
	 *
	 * @param tenantId
	 * @return
	 */
	@Async(AsyncUpdateFieldConfiguration.ASYNC_EXECUTOR_NAME)
	@Transactional(rollbackFor = Exception.class)
	public void clearArchiveConfiguration(Long tenantId, CountDownLatch countDownLatch) {
		//清除 数据规则
		archiveColumnRuleService.remove(Wrappers.<ArchiveColumnRule>lambdaQuery().eq(ArchiveColumnRule::getTenantId, tenantId));
		archiveOperateService.remove(Wrappers.<ArchiveOperate>lambdaQuery().eq(ArchiveOperate::getTenantId, tenantId));
		metadataSourceService.remove(Wrappers.<MetadataSource>lambdaQuery().eq(MetadataSource::getTenantId, tenantId));
		//清除 列表定义
		archiveListService.remove(Wrappers.<ArchiveList>lambdaQuery().eq(ArchiveList::getTenantId, tenantId));
		//清除表单字段 和表单配置
		archiveEditService.remove(Wrappers.<ArchiveEdit>lambdaQuery().eq(ArchiveEdit::getTenantId, tenantId));
		archiveEditFormService.remove(Wrappers.<ArchiveEditForm>lambdaQuery().eq(ArchiveEditForm::getTenantId, tenantId));
		//清除检索定义
		archiveSearchService.remove(Wrappers.<ArchiveSearch>lambdaQuery().eq(ArchiveSearch::getTenantId, tenantId));
		// 清除 排序定义
		archiveSortService.remove(Wrappers.<ArchiveSort>lambdaQuery().eq(ArchiveSort::getTenantId, tenantId));
		// 清除档案盒配置
		List<MetadataBoxConfig> metadataBoxConfigs = metadataBoxConfigService.list(Wrappers.<MetadataBoxConfig>lambdaQuery().eq(MetadataBoxConfig::getTenantId, tenantId));
		List<Long> metadataBoxConfigIds = metadataBoxConfigs.stream().map(MetadataBoxConfig::getId).collect(Collectors.toList());
		if (CollUtil.isNotEmpty(metadataBoxConfigIds)) {
			metadataBoxRuleService.remove(Wrappers.<MetadataBoxRule>lambdaQuery().in(MetadataBoxRule::getConfigId, metadataBoxConfigIds));
			metadataBoxConfigService.removeByIds(metadataBoxConfigIds);
		}
		//档案鉴定规则
		dispAppraisalRuleService.remove(Wrappers.<DispAppraisalRule>lambdaQuery().eq(DispAppraisalRule::getTenantId, tenantId));
		countDownLatch.countDown();
	}

	/**
	 * 清除门类信息 （挂接规则 报表信息 关联关系）
	 *
	 * @param tenantId
	 * @return
	 */
	@Async(AsyncUpdateFieldConfiguration.ASYNC_EXECUTOR_NAME)
	@Transactional(rollbackFor = Exception.class)
	public void clearArchiveData(Long tenantId, CountDownLatch countDownLatch) {
		//清除 挂接规则
		linkColumnRuleService.remove(Wrappers.<LinkColumnRule>lambdaQuery().eq(LinkColumnRule::getTenantId, tenantId));
		linkLayerService.remove(Wrappers.<LinkLayer>lambdaQuery().eq(LinkLayer::getTenantId, tenantId));
		//清除报表 相关信息
		reportService.remove(Wrappers.<Report>lambdaQuery().eq(Report::getTenantId, tenantId));
		reportMetadataService.remove(Wrappers.<ReportMetadata>lambdaQuery().eq(ReportMetadata::getTenantId, tenantId));
		reportTableService.remove(Wrappers.<ReportTable>lambdaQuery().eq(ReportTable::getTenantId, tenantId));
		//清除 关联关系定义
		innerRelationService.remove(Wrappers.<InnerRelation>lambdaQuery().eq(InnerRelation::getTenantId, tenantId));
		countDownLatch.countDown();
	}

	/**
	 * 清除 租户基本信息 （档案树 数据字典 全宗 、同义词、主题词
	 *
	 * @param tenantId 租户id
	 * @return
	 */
	@Async(AsyncUpdateFieldConfiguration.ASYNC_EXECUTOR_NAME)
	@Transactional(rollbackFor = Exception.class)
	public void clearBasicInfo(Long tenantId, CountDownLatch countDownLatch) {
		//清除 档案树
		archiveTreeService.remove(Wrappers.<ArchiveTree>lambdaQuery().eq(ArchiveTree::getTenantId, tenantId));
		//清除 数据字典
		dictService.remove(Wrappers.<Dict>lambdaQuery().eq(Dict::getTenantId, tenantId));
		dictItemService.remove(Wrappers.<DictItem>lambdaQuery().eq(DictItem::getTenantId, tenantId));
		//清除全宗
		fondsService.remove(Wrappers.<Fonds>lambdaQuery().eq(Fonds::getTenantId, tenantId));
		fondsArchiveTreeService.remove(Wrappers.<FondsArchiveTree>lambdaQuery().eq(FondsArchiveTree::getTenantId, tenantId));
		//清除主题词
		keyWordService.remove(Wrappers.<KeyWord>lambdaQuery().eq(KeyWord::getTenantId, tenantId));
		//清除 同义词
		synonymyService.remove(Wrappers.<Synonymy>lambdaQuery().eq(Synonymy::getTenantId, tenantId));
		//清除业务模板数据
		businessModelDefineService.remove(Wrappers.<BusinessModelDefine>lambdaQuery().eq(BusinessModelDefine::getTenantId, tenantId));
		businessStyleSettingService.remove(Wrappers.<BusinessStyleSetting>lambdaQuery().eq(BusinessStyleSetting::getTenantId, tenantId));
		//清除 动态表字段
		dynamicModelDefineService.remove(Wrappers.<DynamicModelDefine>lambdaQuery().eq(DynamicModelDefine::getTenantId, tenantId));
		//清除 字段查询重复表
		metadataCheckrepeatService.remove(Wrappers.<MetadataCheckrepeat>lambdaQuery().eq(MetadataCheckrepeat::getTenantId, tenantId));
		countDownLatch.countDown();
	}

	/**
	 * 清除 租户基本信息 元数据 参数定义 归档范围 统计
	 *
	 * @param tenantId
	 * @return
	 */
	@Async(AsyncUpdateFieldConfiguration.ASYNC_EXECUTOR_NAME)
	@Transactional(rollbackFor = Exception.class)
	public void clearBasicConfiguration(Long tenantId, CountDownLatch countDownLatch) {
		//清除参数定义
		sysSettingService.remove(Wrappers.<SysSetting>lambdaQuery().eq(SysSetting::getTenantId, tenantId));
		// 清除归档范围
		filingScopeService.remove(Wrappers.<FilingScope>lambdaQuery().eq(FilingScope::getTenantId, tenantId));
		//清除元数据
		metadataAutovalueService.remove(Wrappers.<MetadataAutovalue>lambdaQuery().eq(MetadataAutovalue::getTenantId, tenantId));
		metadataTagService.remove(Wrappers.<MetadataTag>lambdaQuery().eq(MetadataTag::getTenantId, tenantId));
		//清除元数据标签 元数据检索
		tagListService.remove(Wrappers.<TagList>lambdaQuery().eq(TagList::getTenantId, tenantId));
		tagSearchService.remove(Wrappers.<TagSearch>lambdaQuery().eq(TagSearch::getTenantId, tenantId));
		filingScopeTypeService.remove(Wrappers.<FilingScopeType>lambdaQuery().eq(FilingScopeType::getTenantId, tenantId));
		archiveConfigManageService.remove(Wrappers.<ArchiveConfigManage>lambdaQuery().eq(ArchiveConfigManage::getTenantId, tenantId));
		//统计相关表
		archiveStatsService.removeByTenantId(tenantId);
		filingStatsService.removeByTenantId(tenantId);
		yearStatsService.removeByTenantId(tenantId);
		//通知公告表
		final List<Long> noticeIds = noticeService.list(Wrappers.<Notice>lambdaQuery().eq(Notice::getTenantId, tenantId)).stream().map(Notice::getId).collect(Collectors.toList());
		if (CollUtil.isNotEmpty(noticeIds)) {
			noticeFileService.remove(Wrappers.<NoticeFile>lambdaQuery().in(NoticeFile::getNoticeId, noticeIds));
			noticeService.removeByIds(noticeIds);
		}

		archiveRetentionRelationService.remove(Wrappers.<ArchiveRetentionRelation>lambdaQuery().eq(ArchiveRetentionRelation::getTenantId, tenantId));
		//水印删除
		watermarkService.remove(Wrappers.<Watermark>lambdaQuery().eq(Watermark::getTenantId, tenantId));
		watermarkDetailService.remove(Wrappers.<WatermarkDetail>lambdaQuery().eq(WatermarkDetail::getTenantId, tenantId));
		// 删除台账自定义
		accountModuleService.remove(Wrappers.<AccountModule>lambdaQuery().eq(AccountModule::getTenantId, tenantId));
		accountTemplateService.remove(Wrappers.<AccountTemplate>lambdaQuery().eq(AccountTemplate::getTenantId, tenantId));
		accountTemplateRoleService.remove(Wrappers.<AccountTemplateRole>lambdaQuery().eq(AccountTemplateRole::getTenantId, tenantId));
		//删除常用下载表
		commonDownloadsService.remove(Wrappers.<CommonDownloads>lambdaQuery().eq(CommonDownloads::getTenantId, tenantId));
		countDownLatch.countDown();
	}

	/**
	 * 清除 物理表
	 *
	 * @param tenantId 租户id
	 * @return
	 * @throws ArchiveBusinessException
	 */
	@Async(AsyncUpdateFieldConfiguration.ASYNC_EXECUTOR_NAME)
	@Transactional(rollbackFor = Exception.class)
	public Future<R> dropArchiveTable(Long tenantId) throws ArchiveBusinessException {
		try {
			archiveTableService.dropTables(tenantId);
		} catch (Exception e) {
			log.error("物理表删除失败");
			throw new ArchiveBusinessException("物理表删除失败");
		}
		return new AsyncResult<R>(new R(null, "物理表删除成功"));
	}

	/**
	 * 清除门类信息
	 *
	 * @param tenantId 租户id
	 * @return
	 */
	@Async(AsyncUpdateFieldConfiguration.ASYNC_EXECUTOR_NAME)
	@Transactional(rollbackFor = Exception.class)
	public Future<R> clearArchiveTable(Long tenantId) {
		//清除 字段信息
		metadataService.remove(Wrappers.<Metadata>lambdaQuery().eq(Metadata::getTenantId, tenantId));
		//清除门类信息
		archiveTypeService.remove(Wrappers.<ArchiveType>lambdaQuery().eq(ArchiveType::getTenantId, tenantId));
		archiveTableService.remove(Wrappers.<ArchiveTable>lambdaQuery().eq(ArchiveTable::getTenantId, tenantId));
		return new AsyncResult<R>(new R(null, "清除门类信息成功"));

	}

	/**
	 * 清除档案类型表模板信息
	 *
	 * @param tenantId 租户id
	 * @return
	 */
	@Async(AsyncUpdateFieldConfiguration.ASYNC_EXECUTOR_NAME)
	@Transactional(rollbackFor = Exception.class)
	public Future<R> clearArchiveTemplateType(Long tenantId) {
		//获取 模板表id
		List<TemplateTable> templateTables = templateTableServer.list(Wrappers.<TemplateTable>lambdaQuery().eq(TemplateTable::getTenantId, tenantId));
		List<Long> ids = templateTables.stream().map(TemplateTable::getId).collect(Collectors.toList());
		//清除 模板表字段
		if (CollectionUtil.isNotEmpty(ids)) {
			templateMetadataService.remove(Wrappers.<TemplateMetadata>lambdaQuery().in(TemplateMetadata::getTemplateTableId, ids));
		}
		templateTableServer.remove(Wrappers.<TemplateTable>lambdaQuery().eq(TemplateTable::getTenantId, tenantId));
		templateTypeService.remove(Wrappers.<TemplateType>lambdaQuery().eq(TemplateType::getTenantId, tenantId));
		return new AsyncResult<R>(new R(null, "清除表模板信息成功"));

	}
}
