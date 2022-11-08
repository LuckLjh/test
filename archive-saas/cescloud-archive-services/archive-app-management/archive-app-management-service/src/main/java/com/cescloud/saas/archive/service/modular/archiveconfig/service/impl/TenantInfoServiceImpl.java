package com.cescloud.saas.archive.service.modular.archiveconfig.service.impl;

import com.cescloud.saas.archive.service.modular.archiveconfig.service.TenantInfoService;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.helper.AsyncInitializeFromDataHelper;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTypeService;
import com.cescloud.saas.archive.service.modular.archivetype.service.TemplateMetadataService;
import com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTableService;
import com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTypeService;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 * @ClassName TenantInfoServiceImpl
 * @Author zhangxuehu
 * @Date 2020/4/22 11:12
 **/
@RequiredArgsConstructor
@Service
@Slf4j
public class TenantInfoServiceImpl implements TenantInfoService {

	//档案门类
	private final ArchiveTypeService archiveTypeService;
	//档案类型模板
	private final TemplateTypeService templateTypeService;
	//档案类型 表模板
	private final TemplateTableService templateTableService;
	//档案类型 表字段
	private final TemplateMetadataService templateMetadataService;
	//档案门类字段
	private final MetadataService metadataService;

	private final AsyncInitializeFromDataHelper asyncInitializeFromDataHelper;

	@Override
	public R initializeArchive(Long templateId, Long tenantId) {
		//初始化档案类型模板
		R r1 = templateTypeService.initialArchiveType(templateId, tenantId);
		//初始化档案类型表模板
		R r2 = templateTableService.initialArchiveTypeTable(templateId, tenantId);
		//初始化 档案类型表字段
		R r3 = templateMetadataService.initialArchiveTypeTableField(templateId, tenantId);
		if (r1.getCode() == CommonConstants.SUCCESS && r2.getCode() == CommonConstants.SUCCESS && r3.getCode() == CommonConstants.SUCCESS) {
			return r1;
		} else {
			return new R().fail("异常", "失败");
		}
	}

	@Override
	public R initializeArchiveType(Long templateId, Long tenantId) throws ArchiveBusinessException {
		//初始化档案门类
		R r = archiveTypeService.initializeHandle(templateId, tenantId);
		//初始化门类字段 并创建门类
		R r1 = metadataService.initializeMetadata(templateId, tenantId);
		if (r.getCode() == CommonConstants.SUCCESS && r1.getCode() == CommonConstants.SUCCESS) {
			return r;
		} else {
			return new R().fail("异常", "失败");
		}
	}

	@Override
	public R initializeHandle(Long templateId, Long tenantId) throws ArchiveBusinessException {
		final CountDownLatch countDownLatch = new CountDownLatch(10);
		asyncInitializeFromDataHelper.initializeEditForm(templateId, tenantId, countDownLatch);
		asyncInitializeFromDataHelper.initializeArchiveList(templateId, tenantId, countDownLatch);
		asyncInitializeFromDataHelper.initializeArchiveSearch(templateId, tenantId, countDownLatch);
		asyncInitializeFromDataHelper.initializeArchiveSort(templateId, tenantId, countDownLatch);
		asyncInitializeFromDataHelper.initializeMetadataAutovalue(templateId, tenantId, countDownLatch);
		asyncInitializeFromDataHelper.initializeMetadataBoxConfig(templateId,tenantId,countDownLatch);
		asyncInitializeFromDataHelper.initializeInnerRelation(templateId, tenantId, countDownLatch);
		asyncInitializeFromDataHelper.initializeTagList(templateId, tenantId, countDownLatch);
		asyncInitializeFromDataHelper.initializeTagSearch(templateId, tenantId, countDownLatch);
		asyncInitializeFromDataHelper.initializeAccount(templateId, tenantId, countDownLatch);
		try {
			countDownLatch.await();
		} catch (Exception e) {
			log.error("初始化门类配置失败" + e);
			return new R<>().fail(null, "初始化门类配置失败");
		}
		return new R<>(null, "初始化门类配置成功");
	}

	@Override
	public R clearAppBasicConfiguration(Long tenantId) {
		final CountDownLatch countDownLatch = new CountDownLatch(4);
		//清除   数据规则 列表定义 表单定义 检索定义 排序定义 档案盒定义
		asyncInitializeFromDataHelper.clearArchiveConfiguration(tenantId,countDownLatch);
		// 清除门类信息 （挂接规则 报表信息 关联关系
		asyncInitializeFromDataHelper.clearArchiveData(tenantId,countDownLatch);
		//清除 租户基本信息 （档案树 数据字典 全宗 、同义词、主题词）
		asyncInitializeFromDataHelper.clearBasicInfo(tenantId,countDownLatch);
		//清除租户基本信息  租户基本信息 元数据 参数定义 归档范围 统计  通知公告 常用下载
		asyncInitializeFromDataHelper.clearBasicConfiguration(tenantId,countDownLatch);
		try {
			countDownLatch.await();
		} catch (Exception e) {
			log.error("清除门类配置失败" + e);
			return new R().fail(null, "清除门类配置失败");
		}
		return new R(null, "将租户基本配置和门类配置信息，清除完毕！！！");
	}

	@Override
	public R clearAppArchive(Long tenantId) throws ArchiveBusinessException {
		Future<R> dropArchiveTableFuture = asyncInitializeFromDataHelper.dropArchiveTable(tenantId);
		while (true) {
			if (dropArchiveTableFuture.isDone()) {
				log.info("将租户门类物理表，删除完毕！！！");
				break;
			}
		}
		//清除门类表信息
		Future<R> archiveTableFuture = asyncInitializeFromDataHelper.clearArchiveTable(tenantId);
		//清除 模板表信息
		Future<R> templateTypeFuture = asyncInitializeFromDataHelper.clearArchiveTemplateType(tenantId);
		while (true) {
			if (archiveTableFuture.isDone() && templateTypeFuture.isDone()) {
				log.info("将租户门类信息，清除完毕！！！");
				break;
			}
		}
		return new R(null, "将租户门类信息，清除完毕！！！");
	}
}
