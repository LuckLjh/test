package com.cescloud.saas.archive.service.modular.fwimp.service.impl;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archivemaintain.dto.PackdowDTO;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.filecenter.dto.FilePushDTO;
import com.cescloud.saas.archive.api.modular.fwimp.constant.OaImpConstant;
import com.cescloud.saas.archive.api.modular.fwimp.dto.OaLogsDTO;
import com.cescloud.saas.archive.api.modular.fwimp.entity.OaImport;
import com.cescloud.saas.archive.api.modular.fwimp.entity.OaLogs;
import com.cescloud.saas.archive.common.constants.ArchiveLayerEnum;
import com.cescloud.saas.archive.common.util.CesFileUtil;
import com.cescloud.saas.archive.common.util.IdGenerator;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.filepush.util.service.PushFileOpenService;
import com.cescloud.saas.archive.service.modular.fwimp.mapper.OaLogsMapper;
import com.cescloud.saas.archive.service.modular.fwimp.service.OaImportService;
import com.cescloud.saas.archive.service.modular.fwimp.service.OaLogsService;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Service
@Slf4j
@CacheConfig(cacheNames = "OaLogs")
@RequiredArgsConstructor
public class OaLogsServiceImpl extends ServiceImpl<OaLogsMapper, OaLogs> implements OaLogsService {

	@Autowired
	private OaImportService oaImportService;
	private final PushFileOpenService pushFileOpenService;
	@Resource
	private ResourceLoader resourceLoader;

	@Override
	public IPage<OaLogs> getPage(Page page,Long ownerId, String status,String keyword){
		LambdaQueryWrapper<OaLogs> queryWrapper = Wrappers.<OaLogs>query().lambda();
		//queryWrapper.orderByAsc(Fonds::getCreatedTime);
		queryWrapper.eq( OaLogs::getOwnerId,ownerId== null?"":ownerId);
		if (StrUtil.isNotBlank(status)) {
			queryWrapper.eq( OaLogs::getStatus,status);
		}
		if (StrUtil.isNotBlank(keyword)) {
			queryWrapper
					.and(wrapper -> wrapper.like(OaLogs::getXmlName, StrUtil.trim(keyword))
/*							.or()
							.like(OaLogs::getStartTime, StrUtil.trim(keyword))
							.or()
							.like(OaLogs::getEndTime,StrUtil.trim(keyword)
							).or()
							.like(OaLogs::getStatus,StrUtil.trim(keyword)
							)*/
					);
		}
		queryWrapper.orderByDesc(OaLogs::getStartTime);
		return this.page(page, queryWrapper);
	}

	@Override
	public void getXmlFile(HttpServletResponse response, String ids) {
		String[] idss = ids.split(",");
		if(idss.length == 1){
			String id = idss[0];
			String xmlFileName = "";
			String path ="";
			OaLogs oaLogs = getById(id);
			xmlFileName = oaLogs.getXmlName();
			//根据成功失败导出 用于拼接地址
			int status = oaLogs.getStatus();
			String splicePath = "";
			if(status == OaImpConstant.impSuccess){//不是成功就是失败
				splicePath = OaImpConstant.successsPath;
			}else{
				splicePath = OaImpConstant.failPath;
			}
			//获取xml 总路径
			Long ownerId = oaLogs.getOwnerId();
			if(ownerId != null){
				OaImport oaImport = oaImportService.getById(ownerId);
				path =	oaImport.getFilePath()+"/"+splicePath+"/"+xmlFileName;
			}
			//获取到对应的文件地址与是否成功，拼接出正确的xml 文件信息
			InputStream inputStream = null;
			ServletOutputStream servletOutputStream = null;
			try {
				response.setHeader("Set-Cookie", "cookiename=cookievalue;path=/;Domain=domainvalue;Max-age=seconds;HttpOnly");
				String encodeName = URLEncoder.encode(xmlFileName, StandardCharsets.UTF_8.toString());
				response.setHeader("Content-Disposition", "attachment;filename=" +encodeName);
				inputStream = new FileInputStream(path);
				servletOutputStream = response.getOutputStream();
				IOUtils.copy(inputStream, servletOutputStream);
				response.flushBuffer();

			} catch (IOException e) {
				log.error("获取xml失败，{}", e);
			} finally {
				IoUtil.close(servletOutputStream);
				IoUtil.close(inputStream);
			}
		}else if (idss.length >1){ // 多个需要打包上传
			getOaXMlZip(response,idss);
		}
	}

	/**
	 * zip 包下载 xml 文件
	 * @param response
	 * @param idss
	 */
	private void getOaXMlZip(HttpServletResponse response, String[] idss) {
		String[] paths = new String[idss.length];
		String zipPath ="";
		InputStream[] ins = new InputStream[idss.length];
		if(idss.length>0) {
			try{
				for(int i=0; i<idss.length; i++) {
					String id = idss[i];
					String xmlFileName = "";
					String path = "";
					OaLogs oaLogs = getById(id);
					if(oaLogs  == null){
						continue;
					}
					xmlFileName = oaLogs.getXmlName()+"";
					//根据成功失败导出 用于拼接地址
					int status = oaLogs.getStatus();
					String splicePath = "";
					if (status == OaImpConstant.impSuccess) {//不是成功就是失败
						splicePath = OaImpConstant.successsPath;
					} else {
						splicePath = OaImpConstant.failPath;
					}
					//获取xml 总路径
					Long ownerId = oaLogs.getOwnerId();
					if (ownerId != null) {
						OaImport oaImport = oaImportService.getById(ownerId);
						path = oaImport.getFilePath() + "/" + splicePath + "/" + xmlFileName;
					}
					paths[i] = xmlFileName;
					InputStream inputStream = null;
					try {
						 inputStream = new FileInputStream(path);
						ins[i] = inputStream;
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}

				}
				String randomNumber = IdGenerator.getIdStr();
				String path1 = System.getProperty("java.io.tmpdir");
				zipPath = path1 + File.separator + randomNumber + StrUtil.DOT + CesFileUtil.ZIP;
				File zip = ZipUtil.zip(new File(zipPath), paths, ins, CharsetUtil.UTF_8);
				//上传名称作为随机数 命名
				PackdowDTO packdowDTO = new PackdowDTO();
				String fileName = StrUtil.isNotBlank(packdowDTO.getFileName()) ? packdowDTO.getFileName() : "XMl打包文件";
				//将zip 包上传到minio
				Long userid = SecurityUtils.getUser().getId();
				Long tenantId = SecurityUtils.getUser().getTenantId();
				FilePushDTO filePushDTO = FilePushDTO.builder().fileSourceName(fileName).fileType(CesFileUtil.ZIP)
						.tenantId(tenantId).userId(userid).fileSize(zip.length()).build();
				pushFileOpenService.finishedTmpFilePush(zip, filePushDTO, true, false);
			} catch (ArchiveBusinessException e) {
				e.printStackTrace();
			} finally {
			//关闭输入流
			for (InputStream in : ins) {
				IoUtil.close(in);
			}
		}
		}

	}

	@Override
	public OaLogs getOaLogDetail(Long id) {
		OaLogs oaLogs = getById(id);
		return oaLogs;
	}
}
