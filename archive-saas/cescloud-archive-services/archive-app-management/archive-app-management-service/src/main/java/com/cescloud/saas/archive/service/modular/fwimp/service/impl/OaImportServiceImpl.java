
package com.cescloud.saas.archive.service.modular.fwimp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archivetype.feign.RemoteArchiveTableService;
import com.cescloud.saas.archive.api.modular.datasource.feign.RemoteArchiveInnerService;
import com.cescloud.saas.archive.api.modular.fwimp.constant.OaImpConstant;
import com.cescloud.saas.archive.api.modular.fwimp.dto.OaColumnDTO;
import com.cescloud.saas.archive.api.modular.fwimp.dto.OaImpAndColumnDTO;
import com.cescloud.saas.archive.api.modular.fwimp.dto.OaImportDTO;
import com.cescloud.saas.archive.api.modular.fwimp.entity.OaColumn;
import com.cescloud.saas.archive.api.modular.fwimp.entity.OaColumnExpand;
import com.cescloud.saas.archive.api.modular.fwimp.entity.OaImport;
import com.cescloud.saas.archive.api.modular.fwimp.entity.OaLogs;
import com.cescloud.saas.archive.api.modular.quartz.dto.QrtzJobDTO;
import com.cescloud.saas.archive.api.modular.quartz.feign.RemoteQuartzService;
import com.cescloud.saas.archive.api.modular.syssetting.support.SysSettingCacheHolder;
import com.cescloud.saas.archive.api.modular.user.feign.RemoteUserService;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import com.cescloud.saas.archive.service.modular.common.security.service.CesCloudUser;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.filecenter.service.FileStorageCommonService;
import com.cescloud.saas.archive.service.modular.filecenter.service.FileStorageOpenService;
import com.cescloud.saas.archive.service.modular.filecenter.service.OtherFileStorageOpenService;
import com.cescloud.saas.archive.service.modular.fwimp.mapper.OaImportMapper;
import com.cescloud.saas.archive.service.modular.fwimp.service.OaColumnExpandService;
import com.cescloud.saas.archive.service.modular.fwimp.service.OaColumnService;
import com.cescloud.saas.archive.service.modular.fwimp.service.OaImportService;
import com.cescloud.saas.archive.service.modular.fwimp.service.OaLogsService;
import com.cescloud.saas.archive.service.modular.fwimp.service.helper.OaImportHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * oa导入
 *
 * @author hyq
 * @date 2019-03-21 12:04:54
 */
@Service
@Slf4j
@CacheConfig(cacheNames = "oaImport")
@RequiredArgsConstructor
@Lazy
public class OaImportServiceImpl extends ServiceImpl<OaImportMapper, OaImport> implements OaImportService {
	@Autowired
	private  OaLogsService oaLogsService;
	@Autowired
	private  OaColumnService oaColumnService;
	@Autowired
	private OaColumnExpandService oaColumnExpandService;

	private final RemoteQuartzService remoteQuartzService;

	@Resource
	private ResourceLoader resourceLoader;

	@Autowired(required = false)
	RemoteUserService remoteUserService;
	@Autowired
	private OaImportHelper oaImportHelper;

	public static StringBuffer str = new StringBuffer();

/*	String fileTableName = "";
	String infoTableName = "";
	String docTableName = "";
	List<SysUser> sysUserList = new ArrayList<>();*/
	@Override
	public IPage<OaImport> getPage(Page page, OaImportDTO oaImportDTO) {
		CesCloudUser user  = SecurityUtils.getUser();
		if(user != null){
			oaImportDTO.setTenantId(user.getTenantId());
		}
		LambdaQueryWrapper<OaImport> queryWrapper = Wrappers.<OaImport>query().lambda();
		//queryWrapper.orderByAsc(Fonds::getCreatedTime);
		if (StrUtil.isNotBlank(oaImportDTO.getKeyword())) {
			queryWrapper
					.and(wrapper -> wrapper.like(OaImport::getOaFlowid, StrUtil.trim(oaImportDTO.getKeyword()))
							.or()
							.like(OaImport::getName, StrUtil.trim(oaImportDTO.getKeyword()))
							.or()
							.like(OaImport::getContent,StrUtil.trim(oaImportDTO.getKeyword())
							));
		}
		queryWrapper.eq(OaImport::getTenantId,user.getTenantId());
		return this.page(page, queryWrapper);
	}

	@Override
	public R activate(String ids) {
		//先查询下是否存在
		String[] idss = ids.split(",");
		Arrays.asList(idss).stream().forEach(id ->{
		OaImport oaImp= this.getById(id);
		int status = oaImp.getStatus();
		if(status == OaImpConstant.activate){
			throw new ArchiveRuntimeException(String.format("选择流程中包含已被激活流程，无法重复激活！"));
		}else{
			if (oaImp != null){
				oaImp.setStatus(OaImpConstant.activate);
				this.updateById(oaImp);
			}
			}
		}
		//激活 任务管理中的对应数据进行激活操作
		);
		R rest= remoteQuartzService.startJobByOaId(ids);
		if(rest.getCode() == CommonConstants.FAIL){
			return new R<>().fail("", "任务管理级联激活失败！");
		}
		return new R<>();
	}

	@Override
	public R disActivate(String ids) {
		//先查询下是否存在
		String[] idss = ids.split(",");
		Arrays.asList(idss).stream().forEach(id ->{
			OaImport oaImp= this.getById(id);
			int status = oaImp.getStatus();
			//激活状态不允许删除
			if (status == OaImpConstant.disActivate) {
				throw new ArchiveRuntimeException(String.format("选择流程中包含已被终止流程，无法重复终止！"));
			}
			if (oaImp != null){
				oaImp.setStatus(OaImpConstant.disActivate);
				this.updateById(oaImp);
			}
		}
		);
		R rest = remoteQuartzService.shutdownJobByOaId(ids);
		if(rest.getCode() == CommonConstants.FAIL){
			return new R<>().fail("", "任务管理级联暂停失败！");
		}
		return new R<>();
	}

	@Override
	public R disOa(String ids) {
		//先查询下是否存在
		String[] idss = ids.split(",");
		Arrays.asList(idss).stream().forEach(id -> {
			OaImport oaImp = this.getById(id);
			int status = oaImp.getStatus();
			//激活状态不允许删除
			if (status == OaImpConstant.activate) {
				throw new ArchiveRuntimeException(String.format("选择流程中包含已激活流程，无法删除请先终止！"));
			}
			if (oaImp != null) {
				//删除日志
				oaLogsService.remove((Wrappers.<OaLogs>lambdaQuery()
						.eq(OaLogs::getOwnerId, id)));
				//删除匹配详细
				oaColumnService.remove((Wrappers.<OaColumn>lambdaQuery()
						.eq(OaColumn::getOwnerId, id)));
				// 删除列详细
				oaColumnExpandService.remove((Wrappers.<OaColumnExpand>lambdaQuery()
						.eq(OaColumnExpand::getOwnerFlowid, oaImp.getOaFlowid())));

				//删除自己
				this.removeById(id);
			}
		});
		R rest = remoteQuartzService.removeJobByOaId(ids);
		if(rest.getCode() == CommonConstants.FAIL){
			return new R<>().fail("", "任务管理级联删除失败！");
		}
		return  new R<>();
	}

	/**
	 * oa id 是否重复
	 * @param oaImp
	 * @return
	 */
	public OaImport  checkIsContianOa (OaImport oaImp ,String fondsCode){
		Long oaFlowid =  oaImp.getOaFlowid();
		Long tenantId = SecurityUtils.getUser().getTenantId();
		LambdaQueryWrapper<OaImport> queryWrapper = Wrappers.<OaImport>query().lambda();
		OaImport oaImpCheck =  this.getOne(
				queryWrapper
						.and(wrapper ->
								wrapper.eq(OaImport::getOaFlowid,oaFlowid)
										.eq(OaImport::getFondsId,fondsCode)
										.eq(OaImport::getTenantId,tenantId)
						)
		);
		return oaImpCheck;
	}

	@Override
	public R saveOa(OaImpAndColumnDTO oaImpAndColumn) {
		OaImportDTO oaImportDTO = oaImpAndColumn.getOaImport();
		List<OaColumnDTO> oaColumnDTOs = oaImpAndColumn.getOaColumns();
		//在这个全宗与租户下 只能有一个 流程id？
		OaImport oaImp =  new OaImport();
		BeanUtil.copyProperties(oaImportDTO, oaImp);
		oaImp.setTenantId(SecurityUtils.getUser().getTenantId());
		oaImp.setExecuteMess("未执行！");
		OaImport oaImpCheck =  checkIsContianOa(oaImp,oaImp.getFondsId());
		if(oaImpCheck  != null){
			throw new ArchiveRuntimeException(String.format("此[%s]流程ID 已存在请检查！",oaImpCheck.getOaFlowid()));
		}
		String timeStrategy = oaImportDTO.getTimeStrategy();
		R<Boolean> r = remoteQuartzService.checksCron(timeStrategy);
		if(r.getCode() == CommonConstants.FAIL){
			return new R<>().fail("", "定时任务执行间隔检测失败");
		}else{
			if(!r.getData()){
				throw new ArchiveRuntimeException("定时任务执行间隔，最短为一分钟请检查！");
			}
		}
		this.save(oaImp);
		//级联新增 任务管理
		QrtzJobDTO	qrtzJobDTO = createQrtzJobDTO(oaImp);
		R rest= remoteQuartzService.saveJobByOaImp(qrtzJobDTO);
		if(rest.getCode() == CommonConstants.FAIL){
			return new R<>().fail("", "任务管理级联保存失败");
		}
/*		if(oaColumns.size()>0){
			oaColumns.forEach(oaColumn -> oaColumn.setOwnerId(oaImp.getId()));
		}*/
		List<OaColumn> oaColumns = oaColumnDTOs.parallelStream().map(dto -> {
			OaColumn oaColumn = new OaColumn();
			BeanUtil.copyProperties(dto, oaColumn);
			oaColumn.setOwnerId(oaImp.getId());
			return oaColumn;
		}).collect(Collectors.toList());
		oaColumnService.saveBatch(oaColumns);
		return new R<>(true);
	}

	private QrtzJobDTO createQrtzJobDTO(OaImport oaImp) {
		QrtzJobDTO qrtzJobDTO = new QrtzJobDTO();
		qrtzJobDTO.setJobId(oaImp.getId());
		qrtzJobDTO.setJobName(oaImp.getName());
		qrtzJobDTO.setJobGroup("OA 导入流程");
		qrtzJobDTO.setCronExpression(oaImp.getTimeStrategy());
		qrtzJobDTO.setCreateBy(SecurityUtils.getUser().getChineseName());
		qrtzJobDTO.setCreateTime(LocalDateTime.now());
		qrtzJobDTO.setExecutePath("http://127.0.0.1:10000/innerOaImpjob/startImp/"+oaImp.getOaFlowid()+","+oaImp.getTenantId());
		qrtzJobDTO.setRemark(oaImp.getContent());
		qrtzJobDTO.setTenantId(oaImp.getTenantId());
		qrtzJobDTO.setJobExecuteStatus("0");//状态（0正常 1异常）
		qrtzJobDTO.setJobOrder("1");//组内执行顺利，值越大执行优先级越高，最大值9，最小值1
		qrtzJobDTO.setJobTenantType("2"); //1、多租户任务;2、非多租户任务
		qrtzJobDTO.setJobType("3");// 1、java类;2、spring bean名称;3、rest调用;4、jar调用;9其他
		qrtzJobDTO.setMisfirePolicy("2");//错失执行策略（1错失周期立即执行 2错失周期执行一次 3下周期执行）
		qrtzJobDTO.setMethodParamsValue(oaImp.getOaFlowid()+""); //方法参数值 唯一
		qrtzJobDTO.setOaId(oaImp.getId());
		return qrtzJobDTO;
	}


	@Override
	public R updateOaById(OaImpAndColumnDTO oaImpAndColumn) {
		//这边应该修改时候就不让修改 流程id 达到目的不校验流程id 了
		OaImportDTO oaImportDTO = oaImpAndColumn.getOaImport();
		List<OaColumnDTO> oaColumnDTOs =  oaImpAndColumn.getOaColumns();
		OaImport oaImp =  new OaImport();
		BeanUtil.copyProperties(oaImportDTO, oaImp);
		String timeStrategy = oaImportDTO.getTimeStrategy();
		R<Boolean> r = remoteQuartzService.checksCron(timeStrategy);
		if(r.getCode() == CommonConstants.FAIL){
			return new R<>().fail("", "定时任务执行间隔检测失败");
		}else{
			if(!r.getData()){
				throw new ArchiveRuntimeException("定时任务执行间隔，最短为一分钟请检查！");
			}
		}
		this.saveOrUpdate(oaImp);
		//直接干掉所有的之前保存的列信息重新保存
		LambdaQueryWrapper<OaColumn> queryWrapper = Wrappers.<OaColumn>query().lambda();
		oaColumnService.remove(
				queryWrapper
						.and(wrapper ->
								wrapper.eq(OaColumn::getOwnerId,oaImp.getId())
						)
		);
		//重新保存页面上的列
		List<OaColumn> oaColumns = oaColumnDTOs.parallelStream().map(dto -> {
			OaColumn oaColumn = new OaColumn();
			BeanUtil.copyProperties(dto, oaColumn);
			oaColumn.setOwnerId(oaImp.getId());
			return oaColumn;
		}).collect(Collectors.toList());
		oaColumnService.saveBatch(oaColumns);
		//级联更新任务
		QrtzJobDTO	qrtzJobDTO = createQrtzJobDTO(oaImp);
		R rest= remoteQuartzService.updateJobByOaImp(qrtzJobDTO);
		if(rest.getCode() == CommonConstants.FAIL){
			return new R<>().fail("", "任务管理级联修改失败");
		}
		return new R<>(true);
	}

	@Override
	public R copyOa(OaImportDTO oaImportDTO,String id) {
		OaImport oaImp =  new OaImport();
		BeanUtil.copyProperties(oaImportDTO, oaImp);
		oaImp.setExecuteMess("未执行！");
		OaImport oaImpCheck =  checkIsContianOa(oaImp,oaImp.getFondsId());
		if(oaImpCheck  != null){
			throw new ArchiveRuntimeException(String.format("此[%s]流程ID 已存在请检查！",oaImpCheck.getOaFlowid()));
		}
		this.save(oaImp);
		QrtzJobDTO	qrtzJobDTO = createQrtzJobDTO(oaImp);
		R rest= remoteQuartzService.saveJobByOaImp(qrtzJobDTO);
		if(rest.getCode() == CommonConstants.FAIL){
			return new R<>().fail("", "任务管理级联保存失败");
		}
		//查询选中id 的列配置
		LambdaQueryWrapper<OaColumn> queryWrapper = Wrappers.<OaColumn>query().lambda();
		queryWrapper.and(wrapper -> wrapper.eq(OaColumn::getOwnerId,id));
		List<OaColumn> oaColumns = oaColumnService.list(queryWrapper);
		if(oaColumns.size()>0){
			oaColumns.forEach(oaColumn -> oaColumn.setOwnerId(oaImp.getId()));
		}
		//复制列详细
		LambdaQueryWrapper<OaColumnExpand> queryWrapper1 = Wrappers.<OaColumnExpand>query().lambda();
		// 获取选择任务的流程id
		OaImport oaImpOld  = getById(id);
		Long oldFlowId = oaImpOld.getOaFlowid();
		queryWrapper1.and(
				wrapper -> wrapper.eq(OaColumnExpand::getOwnerFlowid,oldFlowId)
		);
		List<OaColumnExpand> oaColumnExpands = oaColumnExpandService.list(queryWrapper1);
		if(oaColumnExpands.size()>0){
			oaColumnExpands.forEach(oaColumnExpand -> oaColumnExpand.setOwnerFlowid(oaImp.getOaFlowid()));
		}
		oaColumnExpandService.saveBatch(oaColumnExpands);
		return new R<>(oaColumnService.saveBatch(oaColumns));

	}

	@Override
	public void downloadOaColumnTemplate(HttpServletResponse response) {
		String fileName = "oaColumnTemplate";
		InputStream inputStream = null;
		ServletOutputStream servletOutputStream = null;
		try {
			String path = "templatefile/" + fileName + ".xls";
			org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:" + path);

			response.setContentType("application/vnd.ms-excel");
			response.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
			response.addHeader("charset", "utf-8");
			response.addHeader("Pragma", "no-cache");
			String encodeName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
			response.setHeader("Content-Disposition", "attachment;filename=" + encodeName + ".xls");

			inputStream = resource.getInputStream();
			servletOutputStream = response.getOutputStream();
			IOUtils.copy(inputStream, servletOutputStream);
			response.flushBuffer();

		} catch (IOException e) {
			log.error("获取模板文件流失败，{}", e);
		} finally {
			IoUtil.close(servletOutputStream);
			IoUtil.close(inputStream);
		}
	}
	@Override
	public void startImp(String param) {
		oaImportHelper.startImpStart( param);
	}












}
