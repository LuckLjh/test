package com.cescloud.saas.archive.service.modular.onlinefiling.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.ContentType;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archivedict.entity.DictItem;
import com.cescloud.saas.archive.api.modular.archivetype.dto.ArchiveTypeTreeNode;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveType;
import com.cescloud.saas.archive.api.modular.dept.entity.SysDept;
import com.cescloud.saas.archive.api.modular.dept.feign.RemoteDeptService;
import com.cescloud.saas.archive.api.modular.filecenter.entity.FileStorage;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.api.modular.onlinefiling.constant.OnlineFilingConstant;
import com.cescloud.saas.archive.api.modular.onlinefiling.constant.OnlineFilingStatusEnum;
import com.cescloud.saas.archive.api.modular.onlinefiling.dto.*;
import com.cescloud.saas.archive.api.modular.onlinefiling.entity.OnlineFiling;
import com.cescloud.saas.archive.api.modular.onlinefiling.feign.RemoteSysUserOrDeptService;
import com.cescloud.saas.archive.api.modular.syssetting.entity.SysSetting;
import com.cescloud.saas.archive.api.modular.syssetting.support.SysSettingCacheHolder;
import com.cescloud.saas.archive.api.modular.user.entity.SysUser;
import com.cescloud.saas.archive.api.modular.user.feign.RemoteUserService;
import com.cescloud.saas.archive.common.constants.*;
import com.cescloud.saas.archive.common.constants.business.ReturnProcessConstants;
import com.cescloud.saas.archive.common.util.ArchiveUtil;
import com.cescloud.saas.archive.common.util.IdGenerator;
import com.cescloud.saas.archive.service.modular.archivedict.service.DictItemService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTypeService;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.encrypt.util.SM3Util;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.common.security.tenantfilter.TenantContextHolder;
import com.cescloud.saas.archive.service.modular.filecenter.service.FileStorageCommonService;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataService;
import com.cescloud.saas.archive.service.modular.onlinefiling.mapper.OnlineFilingMapper;
import com.cescloud.saas.archive.service.modular.onlinefiling.service.OnlineFilingService;
import com.cescloud.saas.archive.service.modular.onlinefiling.service.helper.FTPHelper;
import com.cescloud.saas.archive.service.modular.onlinefiling.service.helper.SFTPHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

/**
 * 在线导入
 *
 * @author hyq
 */
@Service
public class OnlineFilingServiceImpl extends ServiceImpl<OnlineFilingMapper, OnlineFiling> implements OnlineFilingService {

	@Autowired
	ArchiveTableService archiveTableService;
	@Autowired
	ArchiveTypeService archiveTypeService;
	@Autowired
	MetadataService metadataService;
	@Autowired
	private ArchiveUtil archiveUtil;
	@Autowired
	private FileStorageCommonService fileStorageCommonService;
	@Autowired(required = false)
	private SysSettingCacheHolder cacheHolder;
	@Autowired
	private DictItemService dictItemService;
	@Autowired
	private RemoteDeptService remoteDeptService;
	@Autowired
	private RemoteUserService remoteUserService;
	@Autowired
	private RemoteSysUserOrDeptService remoteSysUserOrDeptService;

	@Override
	public void startImp(String param) {
/*		JSONObject jsonObject = JSON.parseObject(param);
		JsonEntiyDto  jsonEntiyDto = JSON.toJavaObject(jsonObject, JsonEntiyDto.class);*/
		JsonEntiyDto jsonEntiyDto = new JsonEntiyDto();
		ObjectMapper mapper = new ObjectMapper();
		try {
			jsonEntiyDto = mapper.readValue(param, JsonEntiyDto.class);
		} catch (Exception e) {
			// 解析异常
			saveEntity(param, OnlineFilingStatusEnum.PARSE_JSON_EXCEPTION.getCode(), OnlineFilingStatusEnum.PARSE_JSON_EXCEPTION.name());
			e.printStackTrace();
			return;
		}
		if (jsonEntiyDto == null) {
			// 解析异常
			saveEntity(param, OnlineFilingStatusEnum.PARSE_JSON_EXCEPTION.getCode(), OnlineFilingStatusEnum.PARSE_JSON_EXCEPTION.name());
		} else {
			if (StrUtil.isNotBlank(jsonEntiyDto.getArchiveType())) {
				// 获取要导入的档案类型 ，根据中文查询typeCode
				try {
					ArchiveType archiveType = getRightType(jsonEntiyDto);
					//由于这个操作可能由管理员admin发起，设置锁定租户
					TenantContextHolder.setTenantId(jsonEntiyDto.getTenantId());
					List<ArchiveTypeTreeNode> treeList = archiveTableService.getTypeTreeListByType(archiveType);
					if (treeList.size() > 0) {
						// 校验json  格式的档案类型 结构层级
						Boolean result = checkType(treeList, jsonEntiyDto);
						if (result) {
							//校验成功进行导入
							impJsonData(treeList, jsonEntiyDto);
						} else {
							// json 格式与导入格式不匹配
							saveEntityAndSendMsg(jsonEntiyDto, OnlineFilingStatusEnum.PARSE_ARCHIVE_TYPE_EXCEPTION.getCode(), OnlineFilingStatusEnum.PARSE_ARCHIVE_TYPE_EXCEPTION.name(), new HashMap<String, Object>());
						}
					} else {
						//找不到对应的子档案类型表
						saveEntityAndSendMsg(jsonEntiyDto, OnlineFilingStatusEnum.PARSE_ARCHIVE_TYPE_NULL.getCode(), OnlineFilingStatusEnum.PARSE_ARCHIVE_TYPE_NULL.name(), new HashMap<String, Object>());
					}
				} catch (ArchiveBusinessException e) {
					//找不到这个档案类型
					saveEntityAndSendMsg(jsonEntiyDto, OnlineFilingStatusEnum.PARSE_ARCHIVE_TYPE_NULL.getCode(), OnlineFilingStatusEnum.PARSE_ARCHIVE_TYPE_NULL.name(), new HashMap<String, Object>());
					e.printStackTrace();
				}
			} else {
				//档案类型为空
				saveEntityAndSendMsg(jsonEntiyDto, OnlineFilingStatusEnum.PARSE_ARCHIVE_TYPE_NULL.getCode(), OnlineFilingStatusEnum.PARSE_ARCHIVE_TYPE_NULL.name(), new HashMap<String, Object>());
			}
		}

	}

	/**
	 * 记录并发送
	 *
	 * @param jsonEntiyDto
	 * @param code
	 * @param name
	 */
	private void saveEntityAndSendMsg(JsonEntiyDto jsonEntiyDto, int code, String name, Map<String, Object> dataMap) {
		String jsonStr = JSON.toJSONString(jsonEntiyDto);
		String batchNo = jsonEntiyDto.getBatchNo() == null ? "" : jsonEntiyDto.getBatchNo();
		String archiveTypeName = jsonEntiyDto.getArchiveType();
		String archiveTypeCode = dataMap.get(OnlineFilingConstant.ARCHIVE_TYPE_CODE) == null ? "" : dataMap.get(OnlineFilingConstant.ARCHIVE_TYPE_CODE).toString();
		int successNum = dataMap.get(OnlineFilingConstant.SUCCESSNUM) == null ? 0 : Integer.parseInt(dataMap.get(OnlineFilingConstant.SUCCESSNUM).toString());
		int failedNum = dataMap.get(OnlineFilingConstant.FAILEDNUM) == null ? 0 : Integer.parseInt(dataMap.get(OnlineFilingConstant.FAILEDNUM).toString());
		Long tenantId = jsonEntiyDto.getTenantId();
		this.save(OnlineFiling.builder().id(IdGenerator.getId()).batchNo(batchNo).archiveTypeName(archiveTypeName).tenantId(tenantId)
				.archiveTypeCode(archiveTypeCode).successNum(successNum).failedNum(failedNum).code(code).msg(name).data(jsonStr).build());
		String url = jsonEntiyDto.getCallback_url();
		if (StrUtil.isNotBlank(url)) {
			Map<String, Object> param = new HashMap<>();
			param.put(OnlineFilingConstant.RET_CODE,code);
			param.put(OnlineFilingConstant.RET_MSG,name);
			if(name != OnlineFilingStatusEnum.JSON_SUCCESS.getName()){
				List<Map<String,Object[]>> data = dataMap.get(OnlineFilingConstant.RET_DETAIL_ID_MSG) == null?new ArrayList<>():(List<Map<String, Object[]>>) dataMap.get(OnlineFilingConstant.RET_DETAIL_ID_MSG);
				param.put(OnlineFilingConstant.RET_DETAIL_ID_MSG ,data);
			}
			try{
				requestPostUrl(url,param);
			}catch (Exception e){
				e.printStackTrace();
			}

		}
	}
	private void saveEntityAndSendMsg(JsonEntiyDto jsonEntiyDto,SaveEntityAndSendMsgDto saveEntityAndSendMsgDto){
		String jsonStr = JSON.toJSONString(jsonEntiyDto);
		String batchNo = jsonEntiyDto.getBatchNo() == null ? "" : jsonEntiyDto.getBatchNo();
		String archiveTypeName = jsonEntiyDto.getArchiveType();
		String archiveTypeCode = saveEntityAndSendMsgDto.getArchiveTypeCode();
		int successNum = saveEntityAndSendMsgDto.getSuccessNum();
		int failedNum = saveEntityAndSendMsgDto.getFailedNum();
		Long tenantId = jsonEntiyDto.getTenantId();
		Long id = IdGenerator.getId();
		this.saveOrUpdate(OnlineFiling.builder().id(id).batchNo(batchNo).archiveTypeName(archiveTypeName).tenantId(tenantId)
				.archiveTypeCode(archiveTypeCode).successNum(successNum).failedNum(failedNum)
				.code(saveEntityAndSendMsgDto.getCode()).msg(saveEntityAndSendMsgDto.getMsg()).data(jsonStr).build());
		String url = jsonEntiyDto.getCallback_url();
		if (StrUtil.isNotBlank(url)) {
			Map<String, Object> param = new HashMap<>();
			param.put(OnlineFilingConstant.RET_CODE,saveEntityAndSendMsgDto.getCode());
			param.put(OnlineFilingConstant.RET_MSG,saveEntityAndSendMsgDto.getMsg());
			if(saveEntityAndSendMsgDto.getMsg() != OnlineFilingStatusEnum.JSON_SUCCESS.getName()){
				param.put(OnlineFilingConstant.RET_DETAIL_ID_MSG ,saveEntityAndSendMsgDto.getData());
			}
			try{
				requestPostUrl(url,param);
			}catch (Exception e){
				e.printStackTrace();
			}

		}
	}

	/**
	 * 记录批次信息，并保存
	 *
	 * @param param
	 * @param code
	 * @param name
	 */
	private void saveEntity(String param, int code, String name) {
		this.save(OnlineFiling.builder().id(IdGenerator.getId()).batchNo(name).archiveTypeName(name)
				.archiveTypeCode(name).successNum(0).code(code).msg(name).data(param).build());
	}

	/**
	 * 进行导入
	 *
	 * @param treeList
	 * @param jsonEntiyDto
	 */
	private void impJsonData(List<ArchiveTypeTreeNode> treeList, JsonEntiyDto jsonEntiyDto) {
		// 成功失败，档案树节点相关信息map 用于最后保存记录信息
		Map<String, Object> msg = new HashMap<>();
		msg.put(OnlineFilingConstant.ARCHIVE_TYPE_NAME,jsonEntiyDto.getArchiveType());
		msg.put(OnlineFilingConstant.ARCHIVE_TYPE_CODE,treeList.get(0).getTypeCode());
		//总数 取最大层级数量
		int allNum = 0;
		//失败数
		int failNum = 0;
		//错误信息List
		List<DetailErrorDto> erroList =  new ArrayList<>();
		//导入案卷
		if (treeList.get(0).getArchiveLayer().equals((ArchiveLayerEnum.FOLDER.getValue()))) {
			List<Metadata> folderMetadata = metadataService.listAllByStorageLocate(treeList.get(0).getStorageLocate());
			List<Metadata> fileMetadata = metadataService.listAllByStorageLocate(treeList.get(1).getStorageLocate());
			List<Metadata> docMetadata = metadataService.listAllByStorageLocate(treeList.get(2).getStorageLocate());
			List<Metadata> infoMetadata = metadataService.listAllByStorageLocate(treeList.get(3).getStorageLocate());
			List<JsonFolderDto> folderDtos = jsonEntiyDto.getFolders();
			Map<String, List<Metadata>> metadataMap = new HashMap<>();
			metadataMap.put(OnlineFilingConstant.DOC_META_DATA, docMetadata);
			metadataMap.put(OnlineFilingConstant.FILE_META_DATA, fileMetadata);
			metadataMap.put(OnlineFilingConstant.INFO_META_DATA, infoMetadata);
			if (folderDtos.size() > 0) {
				allNum = folderDtos.size();
				for (int i = 0; i < folderDtos.size(); i++) {
					//匹配列与值
					Map<String, Object> dataMap = getKeyAndValue(folderMetadata, folderDtos.get(i), jsonEntiyDto,null);
					Map<String,Object> retMap = exceteUpdateFolderAndFile(dataMap, folderDtos.get(i), treeList, jsonEntiyDto, metadataMap);
					if(!Boolean.valueOf(retMap.get(ReturnProcessConstants.STATUS).toString())){
						failNum ++;
						DetailErrorDto detailErrorDto = new DetailErrorDto();
						detailErrorDto.setBusinessId((Long) retMap.get(OnlineFilingConstant.BUSINESS_ID));
						detailErrorDto.setInfo(retMap.get(OnlineFilingConstant.INFO).toString());
						erroList.add(detailErrorDto);
					}
				}
			}
			sucessCountAndSave(treeList,allNum,failNum,erroList,jsonEntiyDto);
			//一文一件 或者单套
		} else if(treeList.get(0).getArchiveLayer().equals((ArchiveLayerEnum.ONE.getValue())) ||
				treeList.get(0).getArchiveLayer().equals((ArchiveLayerEnum.SINGLE.getValue()))) {
			String fileTable = treeList.get(0).getStorageLocate();
			String docTable = treeList.get(1).getStorageLocate();
			String infoTable = treeList.get(2).getArchiveLayer();
			//导入一文一件，做了校验，项目直接之前提出来，这边就不校验了
			String signTable = "";
			boolean isSignTable = false;
			if(fileTable.equals((ArchiveLayerEnum.SINGLE.getValue()))){
				signTable = treeList.get(3).getArchiveLayer();
				isSignTable = true;
			}
			List<Metadata> fileMetadata = metadataService.listByStorageLocate(fileTable);
			List<Metadata> docMetadata = metadataService.listByStorageLocate(docTable);
			List<Metadata> infoMetadata = metadataService.listByStorageLocate(infoTable);
			List<Metadata> signMetadata = new ArrayList<>();
			Map<String, List<Metadata>> metadataMap = new HashMap<>();
			if(isSignTable){
				signMetadata = metadataService.listByStorageLocate(signTable);
				metadataMap.put(OnlineFilingConstant.SIGN_META_DATA, signMetadata);
			}
			List<JsonFileDto> jsonFileDtos = jsonEntiyDto.getFiles();
			metadataMap.put(OnlineFilingConstant.DOC_META_DATA, docMetadata);
			metadataMap.put(OnlineFilingConstant.FILE_META_DATA, fileMetadata);
			metadataMap.put(OnlineFilingConstant.INFO_META_DATA, infoMetadata);
			if (jsonFileDtos.size() > 0) {
				allNum = jsonFileDtos.size();
				boolean finalIsSignTable = isSignTable;
				for(int i=0;i<jsonFileDtos.size();i++){
					Map<String,Object> retMap = exceteUpdataFile(jsonFileDtos.get(i),jsonEntiyDto,metadataMap,treeList,finalIsSignTable);
					if(!Boolean.valueOf(retMap.get(ReturnProcessConstants.STATUS).toString())){
						failNum ++;
						DetailErrorDto detailErrorDto = new DetailErrorDto();
						detailErrorDto.setBusinessId((Long) retMap.get(OnlineFilingConstant.BUSINESS_ID));
						detailErrorDto.setInfo(retMap.get(OnlineFilingConstant.INFO).toString());
						erroList.add(detailErrorDto);
					}
				}
			}
			sucessCountAndSave(treeList,allNum,failNum,erroList,jsonEntiyDto);
		}else if (treeList.get(0).getArchiveLayer().equals((ArchiveLayerEnum.PROJECT.getValue()))) {
			//项目
			String proTable = treeList.get(0).getStorageLocate();
			String folderTable = treeList.get(1).getStorageLocate();
			String fileTable = treeList.get(2).getStorageLocate();
			String docTable = treeList.get(3).getStorageLocate();
			String infoTable = treeList.get(4).getArchiveLayer();
			List<JsonProjectDto> projectDtos = jsonEntiyDto.getProjects();
			Map<String, List<Metadata>> metadataMap = new HashMap<>();
			List<Metadata> proMetadata = metadataService.listAllByStorageLocate(proTable);
			List<Metadata> folderMetadata = metadataService.listAllByStorageLocate(folderTable);
			List<Metadata> fileMetadata = metadataService.listAllByStorageLocate(fileTable);
			List<Metadata> docMetadata = metadataService.listAllByStorageLocate(docTable);
			List<Metadata> infoMetadata = metadataService.listAllByStorageLocate(infoTable);
			metadataMap.put(OnlineFilingConstant.FOLDER_META_DATA, folderMetadata);
			metadataMap.put(OnlineFilingConstant.PROJECT_META_DATA, proMetadata);
			metadataMap.put(OnlineFilingConstant.DOC_META_DATA, docMetadata);
			metadataMap.put(OnlineFilingConstant.FILE_META_DATA, fileMetadata);
			metadataMap.put(OnlineFilingConstant.INFO_META_DATA, infoMetadata);
			if (projectDtos.size() > 0) {
				allNum = projectDtos.size();
				for (int i = 0; i < projectDtos.size(); i++) {
					//匹配列与值
					Map<String, Object> dataMap = getKeyAndValuePro(folderMetadata, projectDtos.get(i), jsonEntiyDto);
					Map<String,Object> retMap = exceteUpdatePro(dataMap, projectDtos.get(i), treeList, jsonEntiyDto, metadataMap);
					if(!Boolean.valueOf(retMap.get(ReturnProcessConstants.STATUS).toString())){
						failNum ++;
						DetailErrorDto detailErrorDto = new DetailErrorDto();
						detailErrorDto.setBusinessId((Long) retMap.get(OnlineFilingConstant.BUSINESS_ID));
						detailErrorDto.setInfo(retMap.get(OnlineFilingConstant.INFO).toString());
						erroList.add(detailErrorDto);
					}
				}
				sucessCountAndSave(treeList,allNum,failNum,erroList,jsonEntiyDto);
			}

		}
		TenantContextHolder.setTenantId(null);
	}

	/**
	 * 更新项目并执行下级
	 * @param columnMap
	 * @param jsonProjectDto
	 * @param treeList
	 * @param jsonEntiyDto
	 * @param metadataMap
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Map<String, Object> exceteUpdatePro(Map<String, Object> columnMap, JsonProjectDto jsonProjectDto,
												List<ArchiveTypeTreeNode> treeList, JsonEntiyDto jsonEntiyDto,
												Map<String, List<Metadata>> metadataMap) {
		Map<String,Object> retMap = new HashMap<>();
		retMap.put(ReturnProcessConstants.STATUS,true);
		String proTable = treeList.get(0).getStorageLocate();
		R<Integer> integerR = archiveUtil.saveInner(proTable, columnMap);
		if (integerR.getCode() == CommonConstants.FAIL){
			retMap.put(ReturnProcessConstants.STATUS,false);
			retMap.put(OnlineFilingConstant.BUSINESS_ID,columnMap.get(OnlineFilingConstant.BUSINESS_ID));
			retMap.put(OnlineFilingConstant.RET_CODE,OnlineFilingStatusEnum.SAVE_ONE_FAIL.getCode());
			retMap.put(OnlineFilingConstant.INFO,OnlineFilingStatusEnum.SAVE_ONE_FAIL.getName());
			return retMap;
		}
		List<JsonFolderDto> jsonFolderDtos = jsonProjectDto.getFolders();
		List<Metadata> folderMetadata = metadataMap.get(OnlineFilingConstant.FOLDER_META_DATA);
		for(int i=0;i< jsonFolderDtos.size();i++){
			//匹配列与值
			Map<String, Object> dataMap = getKeyAndValue(folderMetadata, jsonFolderDtos.get(i), jsonEntiyDto,columnMap.get(FieldConstants.ID));
			Map<String,Object> returnMap = exceteUpdateFolderAndFileNoTransactional(dataMap, jsonFolderDtos.get(i), treeList,
					jsonEntiyDto, metadataMap);
			if(!Boolean.valueOf(retMap.get(ReturnProcessConstants.STATUS).toString())){
				return returnMap;
			}
		}
		return retMap;
	}
	public Map<String,Object> exceteUpdateFolderAndFileNoTransactional(Map<String, Object> dataMap, JsonFolderDto jsonFolderDto,
																	   List<ArchiveTypeTreeNode> treeList, JsonEntiyDto jsonEntiyDto,
																	   Map<String, List<Metadata>> metadataMap) {
		Map<String,Object> retMap = new HashMap<>();
		retMap.put(ReturnProcessConstants.STATUS,true);
		try {
			//保存案卷，
			String fileTable = treeList.get(2).getStorageLocate();
			String docTable = treeList.get(3).getStorageLocate();
			String infoTable = treeList.get(4).getStorageLocate();
			String folderTable = treeList.get(1).getStorageLocate();
			String proTable = treeList.get(0).getStorageLocate();
			R<Integer> integerR = archiveUtil.saveInner(folderTable, dataMap);
			if (integerR.getCode() == CommonConstants.FAIL){
				retMap.put(ReturnProcessConstants.STATUS,false);
				retMap.put(OnlineFilingConstant.BUSINESS_ID,dataMap.get(OnlineFilingConstant.BUSINESS_ID));
				retMap.put(OnlineFilingConstant.RET_CODE,OnlineFilingStatusEnum.SAVE_FOLDER_FAIL.getCode());
				retMap.put(OnlineFilingConstant.INFO,OnlineFilingStatusEnum.SAVE_FOLDER_FAIL.getName());
				return retMap;
			}
			//更新项目中的案卷数量
			updateItemCount(proTable,dataMap.get(FieldConstants.OWNER_ID),folderTable);
			List<JsonFileDto> JsonFileDtos = jsonFolderDto.getFiles();
			if (JsonFileDtos.size() > 0) {
				JsonFileDtos.stream().forEach(e -> {
					Map<String, Object> columnMap = getKeyAndValueFile(dataMap.get(FieldConstants.ID), e, jsonEntiyDto, metadataMap);
					//插入文件，继续更新电子文件和过程信息
					R<Integer> integer = archiveUtil.saveInner(fileTable, columnMap);
					if (integer.getCode() == CommonConstants.FAIL){
						retMap.put(ReturnProcessConstants.STATUS,false);
						retMap.put(OnlineFilingConstant.BUSINESS_ID,columnMap.get(OnlineFilingConstant.BUSINESS_ID));
						retMap.put(OnlineFilingConstant.RET_CODE,OnlineFilingStatusEnum.SAVE_FILE_FAIL.getCode());
						retMap.put(OnlineFilingConstant.INFO,OnlineFilingStatusEnum.SAVE_FILE_FAIL.getName());
						return ;
					}
					//更新案卷中的卷内数量
					updateItemCount(folderTable,dataMap.get(FieldConstants.ID),fileTable);
					Map<String,Object> map = exceteDocandInfo(docTable, e.getDocuments(), infoTable, e.getInfos(),
							columnMap.get(FieldConstants.ID), metadataMap, jsonEntiyDto,fileTable);
					if(!Boolean.valueOf(map.get(ReturnProcessConstants.STATUS).toString())){
						retMap.put(ReturnProcessConstants.STATUS,false);
						retMap.put(OnlineFilingConstant.BUSINESS_ID,map.get(OnlineFilingConstant.BUSINESS_ID));
						retMap.put(OnlineFilingConstant.RET_CODE,map.get(OnlineFilingConstant.RET_CODE));
						retMap.put(OnlineFilingConstant.INFO,map.get(OnlineFilingConstant.INFO));
						return ;
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return retMap;
	}
	/**
	 * 项目键值对
	 * @param folderMetadata
	 * @param jsonProjectDto
	 * @param jsonEntiyDto
	 * @return
	 */
	private Map<String, Object> getKeyAndValuePro(List<Metadata> folderMetadata, JsonProjectDto jsonProjectDto, JsonEntiyDto jsonEntiyDto) {
		Map<String, Object> retData = new HashMap<>();
		Map<String, Object> data = jsonProjectDto.getOther();
		folderMetadata.stream().forEach(e -> {
			Object obj = data.get(e.getMetadataEnglish().toUpperCase());
			if (obj != null && obj != "") {
				obj = getDictCodeValue(obj,e);
				retData.put(e.getMetadataEnglish().toUpperCase(), obj);
			}
		});
		Long businessId = jsonProjectDto.getBusinessId();
		String titleProper = jsonProjectDto.getTitleProper();
		String fondsCode = jsonProjectDto.getFondsCode();
		retData.put(FieldConstants.BUSINESS_ID, businessId);
		retData.put(FieldConstants.BATCH_NO, jsonEntiyDto.getBatchNo());
		retData.put(FieldConstants.TITLE_PROPER, titleProper);
		retData.put(FieldConstants.FONDS_CODE, fondsCode);
		String id = IdGenerator.getIdStr();
		retData.put(FieldConstants.ID, id);
		retData.put(FieldConstants.IS_DELETE, BoolEnum.NO.getCode());
		retData.put(FieldConstants.STATUS, OnlineFilingConstant.FILING_STATUS_PRO);
		//setFilingDept(retData,jsonEntiyDto);
		return retData;
	}

	private void sucessCountAndSave(List<ArchiveTypeTreeNode> treeList, int allNum, int failNum, List<DetailErrorDto> erroList, JsonEntiyDto jsonEntiyDto) {
		SaveEntityAndSendMsgDto saveEntityAndSendMsgDto = new SaveEntityAndSendMsgDto();
		saveEntityAndSendMsgDto.setArchiveTypeCode(treeList.get(0).getTypeCode());
		saveEntityAndSendMsgDto.setSuccessNum(allNum-failNum);
		saveEntityAndSendMsgDto.setFailedNum(failNum);
		if(allNum-failNum == 0){
			//全部失败
			saveEntityAndSendMsgDto.setMsg(OnlineFilingStatusEnum.JSON_FAIL.getName());
			saveEntityAndSendMsgDto.setCode(OnlineFilingStatusEnum.JSON_FAIL.getCode());
		}else if(failNum == 0){
			//全部成功
			saveEntityAndSendMsgDto.setMsg(OnlineFilingStatusEnum.JSON_SUCCESS.getName());
			saveEntityAndSendMsgDto.setCode(OnlineFilingStatusEnum.JSON_SUCCESS.getCode());
		}else {
			//部分成功
			saveEntityAndSendMsgDto.setMsg(OnlineFilingStatusEnum.PART_JSON_SUCCESS.getName());
			saveEntityAndSendMsgDto.setCode(OnlineFilingStatusEnum.PART_JSON_SUCCESS.getCode());
			saveEntityAndSendMsgDto.setData(erroList);
		}
		saveEntityAndSendMsg(jsonEntiyDto,saveEntityAndSendMsgDto);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Map<String,Object> exceteUpdataFile(JsonFileDto e, JsonEntiyDto jsonEntiyDto, Map<String, List<Metadata>> metadataMap,List<ArchiveTypeTreeNode> treeList, boolean finalIsSignTable) {
		Map<String,Object> retMap = new HashMap<>();
		retMap.put(ReturnProcessConstants.STATUS,true);
		String fileTable = treeList.get(0).getStorageLocate();
		String docTable = treeList.get(1).getStorageLocate();
		String infoTable = treeList.get(2).getStorageLocate();
		Map<String, Object> columnMap = getKeyAndValueFile(0, e, jsonEntiyDto, metadataMap);
		//插入文件，继续更新电子文件和过程信息
		R<Integer> integerR = archiveUtil.saveInner(fileTable, columnMap);
		if (integerR.getCode() == CommonConstants.FAIL){
			retMap.put(ReturnProcessConstants.STATUS,false);
			retMap.put(OnlineFilingConstant.BUSINESS_ID,columnMap.get(OnlineFilingConstant.BUSINESS_ID));
			retMap.put(OnlineFilingConstant.RET_CODE,OnlineFilingStatusEnum.SAVE_ONE_FAIL.getCode());
			retMap.put(OnlineFilingConstant.INFO,OnlineFilingStatusEnum.SAVE_ONE_FAIL.getName());
			return retMap;
		}
		Map<String,Object> map = exceteDocandInfo(docTable, e.getDocuments(), infoTable, e.getInfos(), columnMap.get(FieldConstants.ID), metadataMap, jsonEntiyDto,fileTable);
		if(!Boolean.valueOf(map.get(ReturnProcessConstants.STATUS).toString())){
			retMap.put(ReturnProcessConstants.STATUS,false);
			retMap.put(OnlineFilingConstant.BUSINESS_ID,map.get(OnlineFilingConstant.BUSINESS_ID));
			retMap.put(OnlineFilingConstant.RET_CODE,map.get(OnlineFilingConstant.RET_CODE));
			retMap.put(OnlineFilingConstant.INFO,map.get(OnlineFilingConstant.INFO));
			return retMap;
		}
		if(finalIsSignTable){
			String signTable = treeList.get(3).getStorageLocate();
			List<Metadata> signMetadata = metadataMap.get(OnlineFilingConstant.SIGN_META_DATA);
			Map<String,Object> signMap = excuteSign(signTable,signMetadata,e.getSigns(),columnMap.get(FieldConstants.ID), jsonEntiyDto);
			if(!Boolean.valueOf(signMap.get(ReturnProcessConstants.STATUS).toString())){
				retMap.put(ReturnProcessConstants.STATUS,false);
				retMap.put(OnlineFilingConstant.BUSINESS_ID,signMap.get(OnlineFilingConstant.BUSINESS_ID));
				retMap.put(OnlineFilingConstant.RET_CODE,signMap.get(OnlineFilingConstant.RET_CODE));
				retMap.put(OnlineFilingConstant.INFO,signMap.get(OnlineFilingConstant.INFO));
				return retMap;
			}
		}
		return retMap;
	}

	/**
	 *
	 * @param finalSignTable
	 * @param
	 * @param signs
	 */
	private Map<String,Object> excuteSign(String finalSignTable, List<Metadata> signMetadata, List<JsonSignDto> signs,Object ownerId,JsonEntiyDto jsonEntiyDto) {
		Map<String,Object> retMap = new HashMap<>();
		retMap.put(ReturnProcessConstants.STATUS,true);
		if(signs.size()>0){
			signs.stream().forEach(jsonSignDto ->{
				Map<String, Object> signMap = getKeyAndValueSign(ownerId,jsonSignDto,signMetadata,jsonEntiyDto);
				R<Integer> integerR = archiveUtil.saveInner(finalSignTable, signMap);
				if (integerR.getCode() == CommonConstants.FAIL){
					retMap.put(ReturnProcessConstants.STATUS,false);
					retMap.put(OnlineFilingConstant.BUSINESS_ID,signMap.get(OnlineFilingConstant.BUSINESS_ID));
					retMap.put(OnlineFilingConstant.RET_CODE,OnlineFilingStatusEnum.SAVE_SIGN_FAIL.getCode());
					retMap.put(OnlineFilingConstant.INFO,OnlineFilingStatusEnum.SAVE_SIGN_FAIL.getName());
					return ;
				}
			});
		}
		return retMap;
	}

	private Map<String, Object> getKeyAndValueSign(Object ownerId, JsonSignDto jsonSignDto, List<Metadata> signMetadata,JsonEntiyDto jsonEntiyDto) {
		//获取对应的列
		Map<String, Object> retData = new HashMap<>();
		Map<String, Object> data = jsonSignDto.getOther();
		signMetadata.stream().forEach(e -> {
			Object obj = data.get(e.getMetadataEnglish().toUpperCase());
			if (obj != null && obj != "") {
				obj = getDictCodeValue(obj,e);
				retData.put(e.getMetadataEnglish().toUpperCase(), obj);
				//filterColumn.add(e.getMetadataEnglish().toUpperCase());
			}
		});
		Long businessId = jsonSignDto.getBusinessId();
		String signatureAlgorithm = jsonSignDto.getSignatureAlgorithm();
		String signatureInfo =  jsonSignDto.getSignatureInfo();
		String id = IdGenerator.getIdStr();
		retData.put(FieldConstants.BUSINESS_ID, businessId);
		retData.put(FieldConstants.BATCH_NO, jsonEntiyDto.getBatchNo());
		retData.put(FieldConstants.ID, id);
		retData.put(FieldConstants.IS_DELETE, BoolEnum.NO.getCode());
		retData.put(FieldConstants.OWNER_ID, ownerId);
		retData.put(FieldConstants.IS_DELETE, BoolEnum.NO.getCode());
		retData.put(FieldConstants.Signature.SIGNATURE_ALGORITHM,signatureAlgorithm);
		retData.put(FieldConstants.Signature.SIGNATURE_INFO,signatureInfo);
		return retData;
	}

	/**
	 * 单独事物执行，失败了这一条案卷包括下级表数据都不进去
	 *
	 * @param dataMap
	 * @param jsonFolderDto
	 * @param treeList
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Map<String,Object> exceteUpdateFolderAndFile(Map<String, Object> dataMap, JsonFolderDto jsonFolderDto, List<ArchiveTypeTreeNode> treeList, JsonEntiyDto jsonEntiyDto, Map<String, List<Metadata>> metadataMap) {
		Map<String,Object> retMap = new HashMap<>();
		retMap.put(ReturnProcessConstants.STATUS,true);
		try {
			//保存案卷，
			String folderTable = treeList.get(0).getStorageLocate();
			String fileTable = treeList.get(1).getStorageLocate();
			String docTable = treeList.get(2).getStorageLocate();
			String infoTable = treeList.get(3).getStorageLocate();
			R<Integer> integerR = archiveUtil.saveInner(folderTable, dataMap);
			if (integerR.getCode() == CommonConstants.FAIL){
				retMap.put(ReturnProcessConstants.STATUS,false);
				retMap.put(OnlineFilingConstant.BUSINESS_ID,dataMap.get(OnlineFilingConstant.BUSINESS_ID));
				retMap.put(OnlineFilingConstant.RET_CODE,OnlineFilingStatusEnum.SAVE_FOLDER_FAIL.getCode());
				retMap.put(OnlineFilingConstant.INFO,OnlineFilingStatusEnum.SAVE_FOLDER_FAIL.getName());
				return retMap;
			}
			List<JsonFileDto> JsonFileDtos = jsonFolderDto.getFiles();
			if (JsonFileDtos.size() > 0) {
				JsonFileDtos.stream().forEach(e -> {
					Map<String, Object> columnMap = getKeyAndValueFile(dataMap.get(FieldConstants.ID), e, jsonEntiyDto, metadataMap);
					//插入文件，继续更新电子文件和过程信息
					R<Integer> integer = archiveUtil.saveInner(fileTable, columnMap);
					if (integer.getCode() == CommonConstants.FAIL){
						retMap.put(ReturnProcessConstants.STATUS,false);
						retMap.put(OnlineFilingConstant.BUSINESS_ID,columnMap.get(OnlineFilingConstant.BUSINESS_ID));
						retMap.put(OnlineFilingConstant.RET_CODE,OnlineFilingStatusEnum.SAVE_FILE_FAIL.getCode());
						retMap.put(OnlineFilingConstant.INFO,OnlineFilingStatusEnum.SAVE_FILE_FAIL.getName());
						return ;
					}
					//更新案卷中的卷内数量
					updateItemCount(folderTable,dataMap.get(FieldConstants.ID),fileTable);
					Map<String,Object> map = exceteDocandInfo(docTable, e.getDocuments(), infoTable, e.getInfos(), columnMap.get(FieldConstants.ID), metadataMap, jsonEntiyDto,fileTable);
					if(!Boolean.valueOf(map.get(ReturnProcessConstants.STATUS).toString())){
						retMap.put(ReturnProcessConstants.STATUS,false);
						retMap.put(OnlineFilingConstant.BUSINESS_ID,map.get(OnlineFilingConstant.BUSINESS_ID));
						retMap.put(OnlineFilingConstant.RET_CODE,map.get(OnlineFilingConstant.RET_CODE));
						retMap.put(OnlineFilingConstant.INFO,map.get(OnlineFilingConstant.INFO));
						return ;
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return retMap;
		}

	private void updateItemCount(String folderTable, Object folderId, String fileTable) {
		StringBuffer sb = new StringBuffer();
		if(folderId != null){
			sb.append(" update ").append(folderTable).append("  set ").append(FieldConstants.ITEM_COUNT +" = (")
					.append(" select count(1) from ").append(fileTable).append( " where ").append(FieldConstants.OWNER_ID)
					.append(" = '").append(folderId).append("' and ").append(FieldConstants.IS_DELETE +" ='0' )")
					.append(" where ").append(FieldConstants.ID).append(" ='").append(folderId).append("'");
		}
		archiveUtil.executeUpdateSqlInner(sb.toString());
	}


	/**
		 * 执行电子文件和过程信息
		 *
		 * @param docTable
		 * @param docs
		 * @param infoTable
		 * @param infos
		 * @param ownerId
		 */
		private Map<String,Object> exceteDocandInfo(String docTable, List<JsonDocDto> docs, String infoTable,
													List<JsonInfoDto> infos, Object ownerId, Map<String, List<Metadata>> metadataMap,
													JsonEntiyDto jsonEntiyDto,String  fileTable) {
			Map<String,Object> map = new HashMap<>();
			map.put(ReturnProcessConstants.STATUS,true);
			if (docs.size() > 0) {
				//List<Metadata> docMetadata = metadataMap.get(OnlineFilingConstant.docMetadata);
				docs.stream().forEach(jsonDocDto -> {
					RetDataDto retDataDto = getKeyAndValueDoc(ownerId, jsonDocDto, jsonEntiyDto, metadataMap);
						if(retDataDto.isCheckStatus()){
						Map<String, Object> docMap = retDataDto.getColumnMap();
						R<Integer> integerR = archiveUtil.saveInner(docTable, docMap);
						if (integerR.getCode() == CommonConstants.FAIL){
							map.put(ReturnProcessConstants.STATUS,false);
							map.put(OnlineFilingConstant.BUSINESS_ID,docMap.get(OnlineFilingConstant.BUSINESS_ID));
							map.put(OnlineFilingConstant.RET_CODE,OnlineFilingStatusEnum.SAVE_DOC_FAIL.getCode());
							map.put(OnlineFilingConstant.INFO,OnlineFilingStatusEnum.SAVE_DOC_FAIL.getName());
							return ;
						}
					}else{
						map.put(ReturnProcessConstants.STATUS,false);
						map.put(OnlineFilingConstant.BUSINESS_ID,retDataDto.getBusinessId());
						map.put(OnlineFilingConstant.RET_CODE,retDataDto.getRetCode());
						map.put(OnlineFilingConstant.INFO,retDataDto.getInfo());
						return ;
					}
				});
				if(!Boolean.valueOf(map.get(ReturnProcessConstants.STATUS).toString())){
					return map;
				}
				updateItemCount(fileTable,ownerId,docTable);
			}
			if (infos.size() > 0) {
				infos.stream().forEach(jsonInfoDto -> {
					Map<String, Object> infoMap = getKeyAndValueInfo(ownerId, jsonInfoDto, jsonEntiyDto, metadataMap);
					R<Integer> integerR = archiveUtil.saveInner(infoTable, infoMap);
					if (integerR.getCode() == CommonConstants.FAIL){
						map.put(ReturnProcessConstants.STATUS,false);
						map.put(OnlineFilingConstant.BUSINESS_ID,infoMap.get(OnlineFilingConstant.BUSINESS_ID));
						map.put(OnlineFilingConstant.RET_CODE,OnlineFilingStatusEnum.SAVE_INFO_FAIL.getCode());
						map.put(OnlineFilingConstant.INFO,OnlineFilingStatusEnum.SAVE_INFO_FAIL.getName());
						return ;
					}
				});
				if(!Boolean.valueOf(map.get(ReturnProcessConstants.STATUS).toString())){
					return map;
				}
			}
			return map;
		}

		private RetDataDto getKeyAndValueDoc(Object ownerId, JsonDocDto jsonDocDto, JsonEntiyDto jsonEntiyDto, Map<String, List<Metadata>> metadataMap) {
			//获取对应的列
			RetDataDto retDataDto = new RetDataDto();
			Map<String, Object> retData = new HashMap<>();
			List<Metadata> docMetadata = metadataMap.get(OnlineFilingConstant.DOC_META_DATA);
			Map<String, Object> data = jsonDocDto.getOther();
			docMetadata.stream().forEach(e -> {
				Object obj = data.get(e.getMetadataEnglish().toUpperCase());
				if (obj != null && obj != "") {
					obj = getDictCodeValue(obj,e);
					retData.put(e.getMetadataEnglish().toUpperCase(), obj);
				}
			});
			Long businessId = jsonDocDto.getBusinessId();
			//文件名
			String fileName = jsonDocDto.getFileName();
			//文件大小
			String fileSize = jsonDocDto.getFileSize();
			//文件url
			String fileUrl = jsonDocDto.getFileUrl();
			String tmpDir = com.cescloud.saas.archive.service.modular.common.core.util.FileUtil.getTmpdir();
			boolean res = false;
			if(fileUrl.startsWith(OnlineFilingConstant.HTTP)){
				res = com.cescloud.saas.archive.service.modular.common.core.util.FileUtil.downloadFromUrl(fileUrl, tmpDir, fileName);
			}else if(fileUrl.startsWith(OnlineFilingConstant.FTP)){
				String  longName = data.get(OnlineFilingConstant.LOGIN_NAME) == null?"":data.get(OnlineFilingConstant.LOGIN_NAME).toString();
				String  loginPassword = data.get(OnlineFilingConstant.LOGIN_PASSWORD) == null?"":data.get(OnlineFilingConstant.LOGIN_PASSWORD).toString();
				int  loginPort = data.get(OnlineFilingConstant.LOGIN_PORT) == null?0: Integer.parseInt(data.get(OnlineFilingConstant.LOGIN_PORT).toString());
				String  loginStoragePath =data.get(OnlineFilingConstant.LOGIN_STORAGE_PATH) == null?"":data.get(OnlineFilingConstant.LOGIN_STORAGE_PATH).toString();
				if(StrUtil.isNotBlank(longName) && StrUtil.isNotBlank(loginPassword) && loginPort!=0 && StrUtil.isNotBlank(loginStoragePath)){
					res = FTPHelper.downloadFile(fileUrl,longName,loginPassword,loginPort,loginStoragePath,tmpDir, fileName);
				}
			}else if(fileUrl.startsWith(OnlineFilingConstant.SFTP)){
				String  longName = data.get(OnlineFilingConstant.LOGIN_NAME) == null?"":data.get(OnlineFilingConstant.LOGIN_NAME).toString();
				String  loginPassword = data.get(OnlineFilingConstant.LOGIN_PASSWORD) == null?"":data.get(OnlineFilingConstant.LOGIN_PASSWORD).toString();
				int  loginPort = data.get(OnlineFilingConstant.LOGIN_PORT) == null?0: Integer.parseInt(data.get(OnlineFilingConstant.LOGIN_PORT).toString());
				String loginStoragePath = data.get(OnlineFilingConstant.LOGIN_STORAGE_PATH) == null ? "" : data.get(OnlineFilingConstant.LOGIN_STORAGE_PATH).toString();
				if(StrUtil.isNotBlank(longName) && StrUtil.isNotBlank(loginPassword) && loginPort!=0 && StrUtil.isNotBlank(loginStoragePath)) {
					SFTPHelper sftpUtil = new SFTPHelper(longName, loginPassword, fileUrl, loginPort);
					sftpUtil.connect();
					File file = sftpUtil.downloadFile(loginStoragePath, tmpDir + fileName);
					if (file.exists()) {
						res = true;
					}
				}

			}
			if (res) {
				//文件校验码
				boolean check = checkCode(jsonDocDto, tmpDir + fileName,jsonEntiyDto);
				if (check) {
					// 上传电子文件
					File file = new File(tmpDir + fileName);
					FileStorage fileStorage = upload(file, jsonEntiyDto.getTenantId(), fileName);
					retData.put(FieldConstants.BUSINESS_ID, businessId);
					retData.put(FieldConstants.BATCH_NO, jsonEntiyDto.getBatchNo());
					String id = IdGenerator.getIdStr();
					retData.put(FieldConstants.ID, id);
					retData.put(FieldConstants.IS_DELETE, BoolEnum.NO.getCode());
					retData.put(FieldConstants.OWNER_ID, ownerId);
					retData.put(FieldConstants.Document.FILE_STORAGE_ID, fileStorage.getId());
					retData.put(FieldConstants.Document.FILE_NAME, fileName);
					retData.put(FieldConstants.Document.FILE_SIZE, fileStorage.getFileSize());
					retData.put(FieldConstants.Document.FILE_FORMAT, fileStorage.getFileType());
					retData.put(FieldConstants.Document.DOC_TYPE, getTextType(fileStorage.getFileType()));
					retDataDto.setColumnMap(retData);
					retDataDto.setCheckStatus(true);
				}else{
					//文件校验码失败
					retDataDto.setBusinessId(businessId);
					retDataDto.setCheckStatus(false);
					retDataDto.setRetCode(OnlineFilingStatusEnum.DOC_CHECK_FAIL.getCode());
					retDataDto.setInfo(OnlineFilingStatusEnum.DOC_CHECK_FAIL.getName());
				}
		} else {
			// 下载失败
				retDataDto.setBusinessId(businessId);
				retDataDto.setCheckStatus(false);
				retDataDto.setRetCode(OnlineFilingStatusEnum.DOC_DOWMN_FAIL.getCode());
				retDataDto.setInfo(OnlineFilingStatusEnum.DOC_DOWMN_FAIL.getName());
		}
		return retDataDto;
	}

	/**
	 * 检查校验码是否合法
	 *
	 * @return
	 */
	public boolean checkCode(JsonDocDto jsonDocDto, String filePath, JsonEntiyDto jsonEntiyDto ) {
		// 下载成功了，判断文件大小，用校验码进行校验
		//校验码
		String checkCodeValue = jsonDocDto.getCheckCode();
		String  checkCodeType = jsonEntiyDto.getCheckCodeType();
		File sipFile = FileUtil.file(filePath);
		boolean checkRes = false;
		String hashFinger = "";
		//String value = "";
		if (StrUtil.isBlank(jsonEntiyDto.getCheckCodeType())) {
			checkCodeType = OnlineFilingConstant.MD5;
		}
		if (checkCodeType.equals(OnlineFilingConstant.MD5)) {
			hashFinger = DigestUtil.md5Hex(sipFile);
			if (hashFinger.equals(checkCodeValue)) {
				// 校验成功
				checkRes = true;
			}
		} else if (checkCodeType.equals(OnlineFilingConstant.SM3)) {
			try {
				FileInputStream fileInputStream = new FileInputStream(sipFile);
				MultipartFile multipartFile = new MockMultipartFile("file", sipFile.getName(), "text/plain", IOUtils.toByteArray(fileInputStream));
				String minioSM3 = SM3Util.encode(multipartFile.getInputStream());
				if (minioSM3.equals(checkCodeValue)) {
					// 校验成功
					checkRes = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}


		} else if (checkCodeType.equals(OnlineFilingConstant.SHA1)) {
			hashFinger = DigestUtil.sha1Hex(sipFile);
			if (hashFinger.equals(checkCodeValue)) {
				// 校验成功
				checkRes = true;
			}
		}
		return checkRes;
	}

	/**
	 * 获取过程性键值对
	 *
	 * @param ownerId
	 * @param jsonInfoDto
	 * @param jsonEntiyDto
	 * @param metadataMap
	 */
	private Map<String, Object> getKeyAndValueInfo(Object ownerId, JsonInfoDto jsonInfoDto, JsonEntiyDto jsonEntiyDto, Map<String, List<Metadata>> metadataMap) {
		//获取对应的列
		Map<String, Object> retData = new HashMap<>();
		List<Metadata> infoMetadata = metadataMap.get(OnlineFilingConstant.INFO_META_DATA);
		Map<String, Object> data = jsonInfoDto.getOther();
		infoMetadata.stream().forEach(e -> {
			Object obj = data.get(e.getMetadataEnglish().toUpperCase());
			if (obj != null && obj != "") {
				obj = getDictCodeValue(obj,e);
				retData.put(e.getMetadataEnglish().toUpperCase(), obj);
				//filterColumn.add(e.getMetadataEnglish().toUpperCase());
			}
		});
		Long businessId = jsonInfoDto.getBusinessId();
		//对应行为描述
		String activityName = jsonInfoDto.getActivityName();
		//对应创建部门
		String activityDept = jsonInfoDto.getActivityDept() == null?"":jsonInfoDto.getActivityDept();
		Long activityDeptId  = 0L;
		R<SysDept> r = remoteDeptService.getByNameInner(activityDept, SecurityConstants.FROM_IN);
		if (r.getCode() != CommonConstants.FAIL){
			if(r.getData()!=null){
				activityDeptId = r.getData().getDeptId();
			}
		}
		//对应创建人名称
		String activityUser = jsonInfoDto.getActivityUser();
		Long userId = 0L;
		R<SysUser> rses = remoteUserService.getUserByChineseNameInner(activityUser,SecurityConstants.FROM_IN);
		if (rses.getCode() != CommonConstants.FAIL){
			if(rses.getData()!=null){
				userId = rses.getData().getUserId();
			}
		}
		retData.put(FieldConstants.BUSINESS_ID, businessId);
		retData.put(FieldConstants.BATCH_NO, jsonEntiyDto.getBatchNo());
		String id = IdGenerator.getIdStr();
		retData.put(FieldConstants.ID, id);
		retData.put(FieldConstants.IS_DELETE, BoolEnum.NO.getCode());
		retData.put(FieldConstants.OWNER_ID, ownerId);
		retData.put(FieldConstants.Info.ACTION_DESCRIPTION, activityName);
		retData.put(FieldConstants.Bak.CREATED_DEPT, activityDept);
		retData.put(FieldConstants.CREATED_USER, activityUser);
		if(activityDeptId !=0L){
			retData.put(FieldConstants.CREATED_DEPT_ID, activityDeptId);
		}
		if(userId != 0L){
			retData.put(FieldConstants.CREATED_BY, userId);
		}
		return retData;
	}

	/**
	 * 配置了编码转换得到转换出来的值
	 * @param val
	 * @param e
	 * @return
	 */
	private Object getDictCodeValue(Object val, Metadata e) {
		if(StrUtil.isNotBlank(e.getDictCode())){
			List<DictItem> dictItems = dictItemService.getItemListByDictCode(e.getDictCode());
			if(dictItems.size()>0){
				for(int i=0;i<dictItems.size();i++){
					if(dictItems.get(i).getItemLabel().equals(val)) {
						val = dictItems.get(i).getItemCode();
					}
				}
			}
		}
		return val;
	}

	/**
	 * 获取文件的key 和 value
	 *
	 * @param ownerId
	 * @param
	 * @param
	 */
	private Map<String, Object> getKeyAndValueFile(Object ownerId, JsonFileDto jsonFileDto, JsonEntiyDto jsonEntiyDto, Map<String, List<Metadata>> metadataMap) {
		//获取对应的列
		Map<String, Object> retData = new HashMap<>();
		List<Metadata> fileMetadata = metadataMap.get(OnlineFilingConstant.FILE_META_DATA);
		Map<String, Object> data = jsonFileDto.getOther();
		fileMetadata.stream().forEach(e -> {
			Object obj = data.get(e.getMetadataEnglish().toUpperCase());
			if (obj != null && obj != "") {
				obj = getDictCodeValue(obj,e);
				retData.put(e.getMetadataEnglish().toUpperCase(), obj);
				//filterColumn.add(e.getMetadataEnglish().toUpperCase());
			}
		});
		Long businessId = jsonFileDto.getBusinessId();
		String titleProper = jsonFileDto.getTitleProper();
		String fondsCode = jsonFileDto.getFondsCode();
		retData.put(FieldConstants.BUSINESS_ID, businessId);
		retData.put(FieldConstants.BATCH_NO, jsonEntiyDto.getBatchNo());
		retData.put(FieldConstants.TITLE_PROPER, titleProper);
		retData.put(FieldConstants.FONDS_CODE, fondsCode);
		retData.put(FieldConstants.ARCHIVE_TYPE_CODE,"WS");
		//retData.put(FieldConstants.PATH,"WS,1");
		String id = IdGenerator.getIdStr();
		retData.put(FieldConstants.ID, id);
		retData.put(FieldConstants.IS_DELETE, BoolEnum.NO.getCode());
		retData.put(FieldConstants.OWNER_ID, ownerId);
		retData.put(FieldConstants.STATUS, OnlineFilingConstant.FILING_STATUS);
		//setFilingDept(retData,jsonEntiyDto);
		return retData;
	}


	/**
	 * 匹配列
	 *
	 * @param folderMetadata
	 * @param jsonFolderDto
	 * @return
	 */
	private Map<String, Object> getKeyAndValue(List<Metadata> folderMetadata, JsonFolderDto jsonFolderDto,
											   JsonEntiyDto jsonEntiyDto,Object ownerId) {
		Map<String, Object> retData = new HashMap<>();
		Map<String, Object> data = jsonFolderDto.getOther();
		//Set<String> filterColumn = new HashSet<>();
		folderMetadata.stream().forEach(e -> {
			Object obj = data.get(e.getMetadataEnglish().toUpperCase());
			if (obj != null && obj != "") {
				obj = getDictCodeValue(obj,e);
				retData.put(e.getMetadataEnglish().toUpperCase(), obj);
				//filterColumn.add(e.getMetadataEnglish().toUpperCase());
			}
		});
		Long businessId = jsonFolderDto.getBusinessId();
		String titleProper = jsonFolderDto.getTitleProper();
		String fondsCode = jsonFolderDto.getFondsCode();
		retData.put(FieldConstants.BUSINESS_ID, businessId);
		retData.put(FieldConstants.BATCH_NO, jsonEntiyDto.getBatchNo());
		retData.put(FieldConstants.TITLE_PROPER, titleProper);
		retData.put(FieldConstants.FONDS_CODE, fondsCode);
		String id = IdGenerator.getIdStr();
		retData.put(FieldConstants.ID, id);
		retData.put(FieldConstants.IS_DELETE, BoolEnum.NO.getCode());
		retData.put(FieldConstants.STATUS, OnlineFilingConstant.FILING_STATUS);
		//setFilingDept(retData,jsonEntiyDto);
		if(ownerId != null && !ownerId.equals("")){
			retData.put(FieldConstants.OWNER_ID,ownerId);
		}
		return retData;
	}

	private Boolean checkType(List<ArchiveTypeTreeNode> treeList, JsonEntiyDto jsonEntiyDto) {
		// 由于查询已经排序 ，这边直接就是最大得节点
		String archiveLayer = treeList.get(0).getArchiveLayer();
		// 查来得是一文一件
		if (archiveLayer.equals(ArchiveLayerEnum.ONE.getValue())
				|| archiveLayer.equals(ArchiveLayerEnum.SINGLE.getValue())) {
			if (jsonEntiyDto.getFiles().size() > 0) {
				return true;
			} else {
				return false;
			}
		} else if (archiveLayer.equals(ArchiveLayerEnum.FOLDER.getValue())) {
			// 案卷
			if (jsonEntiyDto.getFolders().size() > 0) {
				return true;
			} else {
				return false;
			}
		}else if (archiveLayer.equals(ArchiveLayerEnum.PROJECT.getValue())) {
			// 项目
			if (jsonEntiyDto.getProjects().size() > 0) {
				return true;
			} else {
				return false;
			}
		}

		return false;

	}

	/**
	 * @param jsonEntiyDto
	 * @return
	 * @throws ArchiveBusinessException
	 */
	private ArchiveType getRightType(JsonEntiyDto jsonEntiyDto) throws ArchiveBusinessException {
		return archiveTypeService.getByTypeName(jsonEntiyDto.getArchiveType(), jsonEntiyDto.getTenantId());
	}

	/**
	 * 上传附件到电子文件中心
	 *
	 * @param tempFile
	 * @return
	 */
	private FileStorage upload(File tempFile, Long tenantId, String resourseName) {
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(tempFile);
			FileStorage fileStorage = new FileStorage();
			fileStorage.setName(tempFile.getName());
			//源文件名称
			//fileStorage.setBucketName(StorageConstants.TENANT_STORAGE_BUCKET + tenantId);
			//创建目录
			//fileStorage.setParentPath(StorageConstants.OTHER_FILE_STORAGE);
			String fileName = tempFile.getName();
			fileStorage.setFileType(FileUtil.extName(fileName));
			fileStorage.setParentId(1L);
			TenantContextHolder.setTenantId(tenantId);
			fileStorage.setTenantId(tenantId);
			fileStorage.setFileSourceName(resourseName);
			fileStorage.setFileSize(tempFile.length());
			fileStorage.setContentType(ContentType.TEXT_PLAIN.getValue());
			FileStorage retfileStorage = fileStorageCommonService.upload(inputStream, fileStorage);
			return retfileStorage;
		} catch (Exception e) {
			e.printStackTrace();
			log.error("上传失败");
		} finally {
			// 关闭文件流
			IoUtil.close(inputStream);
		}
		return null;
	}

	/**
	 * 根据全文格式获取全文类型 ：文本 1、照片 2、音频 3、视频 4
	 *
	 * @param fileType 文件类型
	 * @return
	 */
	public Integer getTextType(String fileType) {
		Optional<SysSetting> opSysSetting;
		opSysSetting = cacheHolder.getCacheEntityByKey(SysSettingCodeEnum.TEXTTYPE.getCode());

		SysSetting result = opSysSetting.isPresent() ? opSysSetting.get() : null;
		if (result != null && result.getValue() != null
				&& result.getValue().contains(fileType)) {
			return DocTypeEnum.TEXTTYPE.getValue();
		}


		opSysSetting = cacheHolder.getCacheEntityByKey(SysSettingCodeEnum.PHOTOTYPE.getCode());
		SysSetting photoResult = opSysSetting.isPresent() ? opSysSetting.get() : null;

		if (photoResult != null && photoResult.getValue() != null
				&& photoResult.getValue().contains(fileType)) {
			return DocTypeEnum.PHOTOTYPE.getValue();
		}

		opSysSetting = cacheHolder.getCacheEntityByKey(SysSettingCodeEnum.AUDIONTYPE.getCode());
		SysSetting audioResult = opSysSetting.isPresent() ? opSysSetting.get() : null;

		if (audioResult != null && audioResult.getValue() != null
				&& audioResult.getValue().contains(fileType)) {
			return DocTypeEnum.AUDIOTYPE.getValue();
		}

		opSysSetting = cacheHolder.getCacheEntityByKey(SysSettingCodeEnum.VIDEOTYPE.getCode());
		SysSetting videoResult = opSysSetting.isPresent() ? opSysSetting.get() : null;

		if (videoResult != null && videoResult.getValue() != null
				&& videoResult.getValue().contains(fileType)) {
			return DocTypeEnum.VIDEOTYPE.getValue();
		}

		return DocTypeEnum.TEXTTYPE.getValue();
	}

	/**
	 * HttpPost请求
	 *
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static Object requestPostUrl(String url, Map<String, Object> param) throws Exception {

		InputStream is = null;
		String body = null;
		StringBuilder res = new StringBuilder();
		HttpPost httpPost = new HttpPost(url);
		httpPost.addHeader("Content-Type", "application/json");

		// 设置请求的参数
		JSONObject jsonParam = new JSONObject();

		param.forEach((k, v) -> jsonParam.put(k, v));

		StringEntity stringEntity = new StringEntity(jsonParam.toString(), "utf-8");

		stringEntity.setContentEncoding("UTF-8");
		stringEntity.setContentType("application/json");
		httpPost.setEntity(stringEntity);

		RequestConfig config = RequestConfig.custom().setConnectTimeout(5000).build();
		httpPost.setConfig(config);

		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = httpClient.execute(httpPost);
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			is = entity.getContent();
			//转换为字节输入流
			BufferedReader br = new BufferedReader(new InputStreamReader(is, Consts.UTF_8));
			while ((body = br.readLine()) != null) {
				res.append(body);
			}
		}
		Object jsonMap = JSON.parse(res.toString());
		return jsonMap;
	}

	/**
	 * 上海银行组织架构同步(设置归档部门)
	 */
//	public void setFilingDept(Map<String, Object> retMap,JsonEntiyDto jsonEntiyDto) {
//		//工号
//		String code = jsonEntiyDto.getCode();
//		Long deptId = remoteSysUserOrDeptService.getUserByCode(code).getData().getDeptId();
//		String filingDeptName = remoteSysUserOrDeptService.getDeptById(deptId).getData().getName();
//		retMap.put(FieldConstants.FILING_DEPT,filingDeptName);
//	}
}
