package com.cescloud.saas.archive.service.modular.fwimp.service.helper;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.archivetype.feign.RemoteArchiveTableService;
import com.cescloud.saas.archive.api.modular.datasource.dto.DdlDTO;
import com.cescloud.saas.archive.api.modular.datasource.dto.DmlDTO;
import com.cescloud.saas.archive.api.modular.datasource.dto.DynamicArchiveDTO;
import com.cescloud.saas.archive.api.modular.datasource.feign.RemoteArchiveInnerService;
import com.cescloud.saas.archive.api.modular.filecenter.entity.FileStorage;
import com.cescloud.saas.archive.api.modular.fwimp.constant.OaImpConstant;
import com.cescloud.saas.archive.api.modular.fwimp.entity.OaColumn;
import com.cescloud.saas.archive.api.modular.fwimp.entity.OaColumnExpand;
import com.cescloud.saas.archive.api.modular.fwimp.entity.OaImport;
import com.cescloud.saas.archive.api.modular.fwimp.entity.OaLogs;
import com.cescloud.saas.archive.api.modular.syssetting.entity.SysSetting;
import com.cescloud.saas.archive.api.modular.syssetting.support.SysSettingCacheHolder;
import com.cescloud.saas.archive.api.modular.user.entity.SysUser;
import com.cescloud.saas.archive.api.modular.user.feign.RemoteUserService;
import com.cescloud.saas.archive.common.constants.ArchiveLayerEnum;
import com.cescloud.saas.archive.common.constants.DocTypeEnum;
import com.cescloud.saas.archive.common.constants.FieldConstants;
import com.cescloud.saas.archive.common.constants.SysSettingCodeEnum;
import com.cescloud.saas.archive.common.search.CriteriaCondition;
import com.cescloud.saas.archive.common.search.parser.DatabaseSearchParser;
import com.cescloud.saas.archive.common.util.IdGenerator;
import com.cescloud.saas.archive.service.modular.archivetree.service.ArchiveTreeService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.common.security.tenantfilter.TenantContextHolder;
import com.cescloud.saas.archive.service.modular.filecenter.service.FileStorageCommonService;
import com.cescloud.saas.archive.service.modular.fwimp.mapper.OaImportMapper;
import com.cescloud.saas.archive.service.modular.fwimp.service.OaColumnExpandService;
import com.cescloud.saas.archive.service.modular.fwimp.service.OaColumnService;
import com.cescloud.saas.archive.service.modular.fwimp.service.OaImportService;
import com.cescloud.saas.archive.service.modular.fwimp.service.OaLogsService;
import com.cescloud.saas.archive.service.modular.fwimp.service.util.CriteriaConditionUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Component
@Lazy
public class OaImportHelper {

	@Autowired(required = false)
	RemoteUserService remoteUserService;

	private static Boolean isImportSucess = true;//是否导入成功，默认为true

	@Autowired(required = false)
	OaImportService OaImportService;

	@Autowired
	private OaColumnService oaColumnService;

	private final RemoteArchiveTableService remoteArchiveTableService;

	@Autowired
	private OaLogsService oaLogsService;
	@Autowired
	private ArchiveTableService archiveTableService;
	@Autowired
	private OaColumnExpandService oaColumnExpandService;

	@Autowired(required = false)
	private SysSettingCacheHolder cacheHolder;

	@Autowired(required = false)
	private RemoteArchiveInnerService remoteArchiveInnerService;

	private final FileStorageCommonService fileStorageCommonService;

	private final OaImportMapper oaImportMapper;

	private final RedisTemplate redisTemplate;

	@Autowired
	private ArchiveTreeService archiveTreeService;

	@Async
	public void startImpStart(String param) {
		// 获取所有的租户下的用户
		String[] pvalue = param.split(",");
		String oaFlowId ="";
		String tenantId = "";
		String excutePath = "";
		if(pvalue.length>0){
			oaFlowId = pvalue[0];
			if(pvalue.length>1){
				tenantId = pvalue[1];
			}

		}
		//根据流程id 获取到配置信息详细
		try {
			LambdaQueryWrapper<OaImport> queryWrapper = Wrappers.<OaImport>query().lambda();
			String finalOaFlowId = oaFlowId;
			String finalTenantId = tenantId;
			queryWrapper
					.and(wrapper ->
							wrapper.eq(OaImport::getOaFlowid, finalOaFlowId)

					);
			if(!tenantId.equals("")) {
				queryWrapper.eq(OaImport::getTenantId, finalTenantId);
			}
			OaImport oaImp = OaImportService.getOne(queryWrapper);
			if (oaImp != null) {
				R<List<SysUser>> results = remoteUserService.getUsersByTenantIdInner(oaImp.getTenantId(), SecurityConstants.FROM_IN);
				List<SysUser> sysUserList  = results.getData();
				//找下存xml 的路径是否存在
				String filePath = oaImp.getFilePath();

				//获取新路径下的XML
				File flowIdfile = new File(filePath);
				excutePath = filePath;
				System.out.println("----------------------------------------------------------"+flowIdfile.getAbsolutePath());
				if (flowIdfile.exists()) {
					File[] flowIdarray = flowIdfile.listFiles();
					if (flowIdarray.length > 0) {
						//业务锁 ！ redies 里丢一个路径，如果取到了说明没走完！因为走完会删了这个key
						Object val = redisTemplate.opsForValue().get(filePath);
						if(val == null || val.equals("")){
							redisTemplate.opsForValue().set(filePath,filePath);
							DateTimeFormatter fmTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
							LocalDateTime now = LocalDateTime.now();
							oaImp.setExecuteMess("最后一次执行时间："+now.format(fmTime));
							OaImportService.saveOrUpdate(oaImp);
							startImpWork(flowIdarray, oaImp, sysUserList);
							redisTemplate.delete(filePath);
						}else{
							System.out.println("---- 未走完-------");
						}
					} else {
						String erroMsg = "错误！文件夹：" + filePath + " 下无文件请确认！";
						log.error("----------------------------------------------------------"+flowIdfile.getAbsolutePath());
						oaImp.setExecuteMess(erroMsg);
						OaImportService.saveOrUpdate(oaImp);
						throw new ArchiveBusinessException(erroMsg);
					}
				} else {
					String erroMsg = "错误！找不到文件夹：" + filePath;
					log.error(erroMsg);
					oaImp.setExecuteMess(erroMsg);
					OaImportService.saveOrUpdate(oaImp);
					throw new ArchiveBusinessException(erroMsg);
				}
			} else { //没找到流程id
				String erroMsg = "未找到对应的流程id：" + oaFlowId;
				log.error(erroMsg);
				throw new ArchiveBusinessException(erroMsg);
			}
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			//最后记得删除redies 的业务锁
			redisTemplate.delete(excutePath);
		}
	}

	/**
	 * 循环文件进行操作校验
	 * @param flowIdarray
	 */
	private void startImpWork(File[] flowIdarray,OaImport oaImp,List<SysUser> sysUserList) throws ArchiveBusinessException {
		List<File> list = Arrays.asList(flowIdarray);
		List<File> xmlList=list.stream().filter(
				file ->
						"xml".equals(
								file.getName().substring(
										file.getName().indexOf(".")+1,file.getName().length()
								).toLowerCase()
						)
		).collect(Collectors.toList());
		if (xmlList.size()>0){

			// 有xml文件 才可以导入，并记录日志
			String path = oaImp.getFilePath();
			long oaFlowId = oaImp.getOaFlowid();
			String fileTableName = oaImp.getTableName();
			Map<String,Object> tableNameMap = getTableName(fileTableName);
			//获取对应关系 oaColumn
			LambdaQueryWrapper<OaColumn> queryWrapper = Wrappers.<OaColumn>query().lambda();
			queryWrapper.and(wrapper -> wrapper.eq(OaColumn::getOwnerId,oaImp.getId()));
			List<OaColumn> oaColumns = oaColumnService.list(queryWrapper);
			// 电子文件的list
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			xmlList.stream().forEach(
					xml ->{
						LocalDateTime nowDate = LocalDateTime.now();
						tableNameMap.put("nowDate",nowDate);
						try{
							DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();
							Document doc =  dbBuilder.parse(xml);
							/**1.获取XML头部节点信息**/
							NodeList nodeList = doc.getElementsByTagName("Results");
							//解析xml
							analyzeXml(nodeList,oaImp,xml,oaColumns,tableNameMap,sysUserList);
						}catch (Exception e){
							//文件移到到失败目录
							isImportSucess =  false ;
							String erroMsg = e.toString();
							moveDir(xml, OaImpConstant.failPath,path);
							saveOaLogs(oaImp,xml,erroMsg,nowDate,OaImpConstant.impFail);
							e.printStackTrace();
						}
						if(isImportSucess) {
							moveDir(xml, OaImpConstant.successsPath, path);
							String erroMsg = "导入成功~";
							saveOaLogs(oaImp, xml, erroMsg, nowDate, OaImpConstant.impSuccess);
						}
					}
			);
		}else{
			String erroMsg ="配置路径下未找到xml 文件请检查";
			log.error("erroMsg!");
			//throw new ArchiveBusinessException("erroMsg!");
		}
	}

	private void moveDir(File xml, String failedOrSucess,String path) {
		File AfterImport =new File(path+"/"+failedOrSucess);
		/**创建导入成功文件夹，移到文件**/
		if(!AfterImport.exists()){
			AfterImport.mkdir();
		}
		//文件移动
		try {
			FileInputStream fis = new FileInputStream(xml.getPath());
			FileOutputStream fos = new FileOutputStream(AfterImport+"/"+xml.getName());
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = fis.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
			fis.close();
			fos.close();
			java.io.File xmlFile = new java.io.File(xml.getPath());
			if(xmlFile.exists()){
				xmlFile.delete();
			}
		} catch (Exception e2) {
			// TODO: handle exception
		}
	}

	/**
	 * 表map 文件，过程信息，电子文件
	 * @param fileTableName
	 * @return
	 */
	private Map<String,Object> getTableName(String fileTableName) throws ArchiveBusinessException {
		//无论什么表名都取文件层的表名
		 String tableName = fileTableName;
		ArchiveTable archiveTable = archiveTableService.getTableByStorageLocate(fileTableName);
		String archiveLayer = archiveTable.getArchiveLayer();
		Map<String,Object> tableNameMap = new HashMap<>();
		if (archiveLayer.equals(ArchiveLayerEnum.FILE.getValue())
				|| archiveLayer.equals(ArchiveLayerEnum.ONE.getValue())
				|| archiveLayer.equals(ArchiveLayerEnum.SINGLE.getValue())) {
			fileTableName = tableName;
		}else{
			//List<ArchiveTable> archiveTables = archiveUtil.getArchiveTables(archiveTable.getArchiveTypeCode(), archiveTable.getTemplateTableId(), ArchiveLayerEnum.FOLDER.getCode());
			List<ArchiveTable> archiveTables  = archiveTableService.getDownTableByStorageLocateAndDownLayerCode(tableName, ArchiveLayerEnum.FILE.getValue());
			if(archiveTables.size()>0){
				fileTableName = archiveTables.get(0).getStorageLocate();
			}
		}
		tableNameMap.put("fileTableName",fileTableName);
		R<List<ArchiveTable>> docRest = remoteArchiveTableService.getDownTableByStorageLocateAndDownLayerCodeInner(fileTableName,OaImpConstant.docType,SecurityConstants.FROM_IN);
		R<List<ArchiveTable>> infoRest = remoteArchiveTableService.getDownTableByStorageLocateAndDownLayerCodeInner(fileTableName,OaImpConstant.infoType,SecurityConstants.FROM_IN);
		List<ArchiveTable> docList = docRest.getData();
		List<ArchiveTable> infoList = infoRest.getData();
		if(docList.size()>0){
			String	docTableName = docList.get(0).getStorageLocate();
			tableNameMap.put("docTableName",docTableName);
		}
		if(infoList.size()>0){
			String	infoTableName = infoList.get(0).getStorageLocate();
			tableNameMap.put("infoTableName",infoTableName);
		}
		return tableNameMap;
	}

	/**
	 * 解析xml 操作
	 * @param nodeList
	 * @param oaImp
	 */
	private void analyzeXml(NodeList nodeList, OaImport oaImp,File xml,List<OaColumn> oaColumns,Map<String,Object> tableNameMap,List<SysUser> sysUserList) {
		//LocalDateTime nowDate = LocalDateTime.now();
		Object objValue = tableNameMap.get("nowDate");
		LocalDateTime nowDate =(LocalDateTime) objValue;
		String sn = "";
		String title = "";
		String OaFlowid = oaImp.getOaFlowid()+"";
		for(int b = 0; b< nodeList.getLength() ; b ++){
			//获取头部信息
			//XML中的WorkflowId节点值
			Element element = (Element)nodeList.item(b);
			String WorkflowId=element.getElementsByTagName("WorkflowId").item(0).getFirstChild().getNodeValue();
			if(WorkflowId.equals(OaFlowid)){
				sn = element.getElementsByTagName("Sn").item(0).getFirstChild().getNodeValue();
				title =  element.getElementsByTagName("Title").item(0).getFirstChild().getNodeValue();
			}else {
				isImportSucess =  false ;
				String erroMsg =" xml："+xml.getName()+" 流程id与任务流程id不一致请检查！";

				moveDir(xml,OaImpConstant.failPath,oaImp.getFilePath());
				//记录成功或者失败日志
				saveOaLogs(oaImp,xml,erroMsg,nowDate,OaImpConstant.impFail);
				continue;
			}
			/**导入xmlID同名html begin* */
			String htmlName = sn+".html";
			/**2.获取XML字段节点信息**/
			NodeList Fieldlist = element.getElementsByTagName("Field");
			//获取所有的节点值，放入到map中。 20170814
			Map<String,String> fileMap = new HashMap<>();
			tableNameMap.put("title",title);
			String fileId = analyzeXmlField(Fieldlist,fileMap,htmlName,oaImp,oaColumns,tableNameMap,sysUserList,xml);
			//获取过程信息
			if(!fileId.equals(OaImpConstant.jumpAll)) { //有这个xml 咱么不导入了
				NodeList opinionList = element.getElementsByTagName("Opinion");
				analyzeXmlOpinion(opinionList, tableNameMap, sysUserList,fileId);
			}
			//更新附件数量
			if( (fileId != null  || !fileId.equals("")) && !fileId.equals(OaImpConstant.jumpAll) ) {//有这个也不更新了！
				final DynamicArchiveDTO ArchiveDTO = new DynamicArchiveDTO();
				List<Long> list = new ArrayList<>();
				list.add(Long.parseLong(fileId));
				ArchiveDTO.setIds(list);
				String fileTableName = tableNameMap.get("fileTableName")== null?"":tableNameMap.get("fileTableName").toString();
				String docTableName = tableNameMap.get("docTableName")== null?"":tableNameMap.get("docTableName").toString();
				ArchiveDTO.setTableName(fileTableName);
				Map<String, Object> data = new HashMap();
				if(StrUtil.isNotBlank(fileTableName) || StrUtil.isNotBlank(docTableName) ||  StrUtil.isNotBlank(fileId) ){
					int docCount = findDocCountByOwnerId(docTableName,fileId);
					data.put(FieldConstants.File.ITEM_COUNT, docCount);
					ArchiveDTO.setMap(data);
					remoteArchiveInnerService.updateByIds(ArchiveDTO, SecurityConstants.FROM_IN);
				}
			}
		}
	}
	/**
	 *算电子文件数量
	 */
	private int findDocCountByOwnerId(String docTableName, String fileId) {
		final DynamicArchiveDTO ArchiveDTO = new DynamicArchiveDTO();
		ArchiveDTO.setTableName(docTableName);
		StringBuilder where = new StringBuilder();
		where.append(" owner_id ='").append(fileId).append("' and is_delete ='0'");
		ArchiveDTO.setWhere(where.toString());
		R<Integer> r = remoteArchiveInnerService.getCountByCondition(ArchiveDTO, SecurityConstants.FROM_IN);
		return r.getData();
	}

	/**
	 * 记录oa 日志
	 * @param oaImp
	 * @param xml
	 * @param erroMsg
	 * @param startDate
	 * @param status
	 */
	private void saveOaLogs(OaImport oaImp, File xml, String erroMsg, LocalDateTime startDate, int status) {
		OaLogs oalog = new OaLogs();
		oalog.setOwnerId(oaImp.getId());
		oalog.setXmlName(xml.getName());
		oalog.setStartTime(startDate);
		oalog.setEndTime(LocalDateTime.now());
		oalog.setStatus(status);
		oalog.setContent(erroMsg);
		oaLogsService.save(oalog);
	}

	/**
	 * 电子文件信息 与 文件信息
	 * @param fieldlist
	 */
	private String  analyzeXmlField(NodeList fieldlist,Map<String,String> fileMap,String htmlName,OaImport oaImp,List<OaColumn> oaColumns,Map<String,Object> tableNameMap,List<SysUser> sysUserList,File xml) {
		String fileId = "";
		for(int c=0;c<fieldlist.getLength();c++){
			Element element2 = (Element)fieldlist.item(c);
			String FieldName=element2.getElementsByTagName("FieldLabel").item(0).getFirstChild().getNodeValue();
			String FieldValue="";
			if(element2.getElementsByTagName("FieldValue").item(0).getFirstChild()!=null){
				FieldValue=element2.getElementsByTagName("FieldValue").item(0).getFirstChild().getNodeValue();
				//根据xml字段名匹配oa设置字段名拼接SQL
				fileMap.put(FieldName, FieldValue);
			}
		}

		//根据oaColumns 进行导入操作 返回 file 表的id
		String sn =htmlName.replace(".html","");
		fileId = insertFile(fileMap,oaImp,oaColumns,sn,tableNameMap,sysUserList,xml);
		if(!fileId.equals(OaImpConstant.jumpAll) && isImportSucess == true) { //有这个xml 咱么不导入了，并且是成功的状态
			/**3.获取XML附件节点信息**/
			for (int i = 0; i < fieldlist.getLength(); i++) {
				Element element2 = (Element) fieldlist.item(i);
				NodeList DocumentList = element2.getElementsByTagName("File");
				//解析附件信息
				analyzeXmlDOC(DocumentList, htmlName, oaImp, tableNameMap, sysUserList, fileId, xml);
			}

		}
		return fileId;
	}

	/**
	 *  插入文件表操作，并且返回 id，
	 * @param fileMap
	 * @param oaColumns
	 * @return
	 */
	private String insertFile(Map<String, String> fileMap,OaImport oaImp, List<OaColumn> oaColumns,String sn,Map<String,Object> tableNameMap,List<SysUser> sysUserList,File xml)  {
		Object objValue = tableNameMap.get("nowDate");
		LocalDateTime nowDate =(LocalDateTime) objValue;
		try{
			final List<Map<String, Object>> listMap = new ArrayList<>();
			List<String> flagList = checkIsContianArchive(sn,tableNameMap);
			R<List<OaColumnExpand>> result = oaColumnExpandService.findByFlowId(oaImp.getOaFlowid());
			List <OaColumnExpand> list = result.getData();
			List <OaColumnExpand> columnExpressionList = list.stream().filter(
					OaColumnExpand ->
							OaColumnExpand.getFlag() == OaImpConstant.columnExpression
									&&
									OaColumnExpand.getBackCondition() != null
									&&
									!OaColumnExpand.getBackCondition().equals("")
			).collect(Collectors.toList());
			if(columnExpressionList.size()>0) {
				columnExpressionList.stream().forEach(
						OaColumnExpand -> {
							Object object = OaColumnExpand.getBackCondition();
							CriteriaCondition CC = CriteriaConditionUtil.toCriteriaConditionFromDbBackCondition(object);
							String conditionSql = DatabaseSearchParser.parseCondition(CC, "");
							System.out.println(conditionSql);
							OaColumnExpand.setBackCondition(conditionSql);
						}
				);
			}
			// 有这个xml_id 并且跳过情况不执行
			String flag = flagList.get(0)+"";
			if(oaImp.getDataCheck() == OaImpConstant.jumpOver && flag.equals(OaImpConstant.isContain)){
				isImportSucess = true;
				return OaImpConstant.jumpAll;
				//有这个xml id 但是要覆盖
			}else if (oaImp.getDataCheck() == OaImpConstant.cover && flag.equals(OaImpConstant.isContain)){
				String fileId = flagList.get(1)+"";
				Long id = Long.parseLong(fileId);
				List<Long> ids =  new ArrayList();
				ids.add(id);
				// 插入这条数据
				String fileIdNew = insertFileSql(fileMap,oaImp,oaColumns,sn,tableNameMap,sysUserList, xml);
				// 将原来这条数据改为已删除
				final DynamicArchiveDTO archiveDTO = new DynamicArchiveDTO();
				String fileTableName = tableNameMap.get("fileTableName")+"";
				archiveDTO.setTableName(fileTableName);
				archiveDTO.setIds(ids);
				remoteArchiveInnerService.deleteLogicallyByIds(archiveDTO,SecurityConstants.FROM_IN);
				// 更新配置字段表达式的值
				updateFileByColumnCondition(columnExpressionList,tableNameMap,fileIdNew);
				isImportSucess = true;
				return fileIdNew;
			}else if (flag.equals(OaImpConstant.notContain)){
				//没有这个xml_id的情况，直接导入
				String fileIdNew = insertFileSql(fileMap,oaImp,oaColumns,sn,tableNameMap,sysUserList, xml);
				// 更新配置字段表达式的值
				updateFileByColumnCondition(columnExpressionList,tableNameMap,fileIdNew);
				isImportSucess = true;
				return fileIdNew;
			}
		}catch (Exception e){
			isImportSucess =  false ;
			String erroMsg = e.toString();

			moveDir(xml,OaImpConstant.failPath,oaImp.getFilePath());
			//记录成功或者失败日志
			saveOaLogs(oaImp,xml,erroMsg,nowDate,OaImpConstant.impFail);
			e.printStackTrace();

		}
		return "fail";
	}

	private void updateFileByColumnCondition(List<OaColumnExpand> columnExpressionList, Map<String, Object> tableNameMap, String fileIdNew) {
		DdlDTO ddlDTO = new DdlDTO();
		List<String> sqls = new ArrayList<>();
		String fileTableName = tableNameMap.get("fileTableName")+"";
		//过滤掉条件是空的
		columnExpressionList = columnExpressionList.stream().filter(
				list -> !list.getPageCondition().equals("")
		).collect(Collectors.toList());
		columnExpressionList.stream().forEach(
				OaColumnExpand ->{
					String sql = "";
					StringBuffer sb = new StringBuffer();
					sb.append( " update ").append(fileTableName).append(" set ").append(OaColumnExpand.getOwnerColumn())
							.append( " = '").append(OaColumnExpand.getTitleValue()).append("' where ").append(OaColumnExpand.getBackCondition())
							.append(" and id='").append(fileIdNew).append("'");
					sql = sb.toString();
					sqls.add(sql);
				}
		);
		ddlDTO.setSqls(sqls);
		R<Boolean> result = remoteArchiveInnerService.executeDdls(ddlDTO,SecurityConstants.FROM_IN);
		if (result.getCode() == CommonConstants.SUCCESS) {
			result.getData();
		}else {
			String x = "失败";
		}
	}

	/**
	 * 插入文件表
	 * @param fileMap xml 对应的列名和值
	 * @param oaImp
	 * @param oaColumns
	 * @param sn
	 * @param tableNameMap
	 * @param sysUserList
	 * @return
	 */
	private String insertFileSql(Map<String, String> fileMap, OaImport oaImp, List<OaColumn> oaColumns, String sn, Map<String, Object> tableNameMap, List<SysUser> sysUserList,File xml) {
		Object objValue = tableNameMap.get("nowDate");
		LocalDateTime nowDate =(LocalDateTime) objValue;
		String id = IdGenerator.getIdStr();
		String fileTableName = tableNameMap.get("fileTableName") + "";
		Long tenantId = oaImp.getTenantId();
		Map<String, Object> dataMap = new HashMap();
		dataMap.put(FieldConstants.ID, id);
		dataMap.put(FieldConstants.XML_ID, sn);
		dataMap.put(FieldConstants.IS_DELETE, "0");
		dataMap.put(FieldConstants.FONDS_CODE, "GDXX");
		dataMap.put(FieldConstants.TITLE_PROPER, tableNameMap.get("title"));
		dataMap.put(FieldConstants.STATUS, oaImp.getImportStatus());

		// 获取档案树所对应的数据
		if (StrUtil.isNotBlank(oaImp.getTreeNode())) {
            Long treeId = Long.parseLong(oaImp.getTreeNode());
            dataMap.putAll(archiveTreeService.getArchiveTreeDataValues(treeId));
        }

		oaColumns.stream().forEach(
				oaColumn -> {
					// 取一下当前的列有没有配置过奇怪的特殊条件,和前缀后缀条件。
					Object value = dataProcess(oaColumns, fileMap, oaColumn, oaImp);
					// 数字类型转换
					if(oaColumn.getMetadataType().equals("int")){
						if (value.equals("")){
							value = 0;
						}else{
							value = Integer.parseInt(value+"");
						}
						//  string 转换日期类型 改为前台判断！
					}
/*					else if (oaColumn.getMetadataType().equals("date")){
						if(!value.equals("")){
							String dateValue = value+"".replace("-","");
							DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
							LocalDate times = LocalDate.parse(dateValue,formatter);
							value = times;
						}
					}*/
					// 字段长度判断
					/*					if(oaColumn.getMetadataLength()<value.toString().length()){ //字段超出长度校验
					 *//*						if (value.equals("")){
							value = 0;
						}*//*
						String erroMsg = oaColumn.getColumnName()+"字段值"+value+"超长";
						saveOaLogs(oaImp,xml,erroMsg,nowDate,OaImpConstant.impFail);
					}*/
					if(value.equals("")){
						value = null;
					}
					dataMap.put(oaColumn.getColumnName(), value);
				}
		);
		List<String> filterColumn = new ArrayList<>();//获取需要插入所有的列字段名
		filterColumn.add(FieldConstants.STATUS);
		filterColumn.add(FieldConstants.FILING_DEPT);
		filterColumn.add(FieldConstants.FILING_DEPT_ID);
		filterColumn.add(FieldConstants.FILING_USER);
		filterColumn.add(FieldConstants.FILING_USER_ID);
		filterColumn.add(FieldConstants.FONDS_CODE);
		filterColumn.add(FieldConstants.TITLE_PROPER);
		filterColumn.add(FieldConstants.XML_ID);
		filterColumn.add(FieldConstants.PATH);
		dataMap.put(FieldConstants.TITLE_PROPER, tableNameMap.get("title"));
		final DynamicArchiveDTO archiveDTO = DynamicArchiveDTO.builder()
				.tableName(fileTableName)
				.map(dataMap)
				.userId(tenantId)
				.filterColumn(filterColumn)
				.build();
		R<Integer> rest = remoteArchiveInnerService.save(archiveDTO, SecurityConstants.FROM_IN);
		if(rest.getCode() == CommonConstants.FAIL){
			isImportSucess =  false ;
			moveDir(xml,OaImpConstant.failPath,oaImp.getFilePath());
			//记录成功或者失败日志
			String mess = rest.getMsg();
			if(mess.length()>100){
				mess = mess.substring(0,100);
			}
			String erroMsg = " 存储"+fileTableName+"表，文件信息保存失败"+mess;
			saveOaLogs(oaImp,xml,erroMsg,nowDate,OaImpConstant.impFail);
		}
		return id;
	}

	/**
	 * 对应的值处理
	 * @param oaColumns
	 * @param fileMap
	 * @param
	 * @param
	 * @return
	 */
	private Object  dataProcess(List<OaColumn> oaColumns, Map<String, String> fileMap,OaColumn oaColumn ,OaImport oaImp) {
		//findByColumnAndFlowId();
		//首先判断xml 里面的有没有对应的值
		String xmlDefault = fileMap.get(oaColumn.getOaName()) == null ?"":fileMap.get(oaColumn.getOaName());
		try {
			//配置的默认值
			String defaultValue = oaColumn.getFixedValue() == null?"":oaColumn.getFixedValue()+"";
			//前缀
			String prefixion = oaColumn.getPrefixion() == null ?"":oaColumn.getPrefixion();
			//后缀
			String suffixion = oaColumn.getSuffixion() == null ?"":oaColumn.getSuffixion();
			//截取位数
			int spiltLen = oaColumn.getInterception() == null ?0:Integer.parseInt(oaColumn.getInterception());
			// 查询一下 列配置 的转换方案
			R<List<OaColumnExpand>> result = oaColumnExpandService.findByColumnAndFlowId(oaColumn.getColumnName(),oaImp.getOaFlowid());
			List <OaColumnExpand> list = result.getData();
			List <OaColumnExpand> fromColumnList = list.stream().filter(
					OaColumnExpand -> OaColumnExpand.getFlag() == OaImpConstant.fromColumn
			).collect(Collectors.toList());
			//不是空可以进行截取 与 前缀后缀操作 ， 有值可以进行神奇的转换，去某张表去那个字段进行转换

			if(!xmlDefault.equals("")){
				//如果可以转换就先转换 就不切和加前后缀了
				if(fromColumnList.size()>0) {
					String targetTableName = fromColumnList.get(0).getTargetTableName();
					String targetColumnName  = fromColumnList.get(0).getTargetFieldName().toLowerCase();
					String relaseColumnName  = fromColumnList.get(0).getRelevanceFieldName().toLowerCase();
					List<HashMap<String,Object>> relevanceDate = oaImportMapper.getRelevance(targetTableName,targetColumnName,relaseColumnName);
					final String  XMLdate = xmlDefault;
					List<HashMap<String,Object>> dateMap = relevanceDate.stream().filter(
							//查出来就算是大写的列名查询出来也是小写
							date ->XMLdate.equals(date.get(targetColumnName.toLowerCase()))
					).collect(Collectors.toList());
					if(dateMap.size()>0) {
						xmlDefault = dateMap.get(0).get(relaseColumnName.toLowerCase()) + "";
					}
				}else{
					//截取 ，长度不够的就不切了
					if(xmlDefault.length()> spiltLen){ //切
						xmlDefault = xmlDefault.substring(spiltLen);
					}
					xmlDefault = prefixion + xmlDefault +suffixion;
				}
			}else{  //xml 对应没有值
				//如果配置了默认值则填充默认值，没有默认值看有没有根据 条件设置值
				if(!defaultValue.equals("")){ // 有默认值
					xmlDefault = defaultValue;
				}
				//表达式值插入后修改填充
/*			else{
				if(columnExpressionList.size()>0) {
					//CriteriaConditionUtil.toCriteriaConditionFromDbBackCondition();
				}
			}*/
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return xmlDefault;
	}

	/**
	 * 检查是否存在 这个条目的数据，已唯一标识sn 作为判断条件
	 * @param sn
	 * @param tableNameMap
	 */
	private List<String> checkIsContianArchive(String sn, Map<String, Object> tableNameMap) {
		String fileTableName = tableNameMap.get("fileTableName")+"";
		DmlDTO dml = new DmlDTO();
		StringBuilder selectSql = new StringBuilder("select ");
		selectSql.append(FieldConstants.ID).append(" from ");
		selectSql.append(fileTableName).append(" ").append(" where  ")
				.append(FieldConstants.XML_ID).append("  ='")
				.append(sn).append(" ' and ").append(FieldConstants.IS_DELETE).append(  "='0'");
		dml.setSql(selectSql.toString());
		R<List<Map<String, Object>>> selectR = remoteArchiveInnerService.executeQuery(dml, SecurityConstants.FROM_IN);
		List<String> returnList = new ArrayList();
		if (selectR.getCode() == CommonConstants.FAIL) {
			log.error("案卷信息重复信息查询失败");
			returnList.add("erro");
			return returnList;
		}else {
			List<Map<String, Object>> dataList = selectR.getData();
			if(dataList.size()>0){
				// 文件表id
				String fileId = dataList.get(0).get("id")+"";
				returnList.add(OaImpConstant.isContain);
				returnList.add(fileId);
				return returnList;
			}else{
				returnList.add(OaImpConstant.notContain);
				return returnList;
			}
		}
	}

	/**
	 * 导入doc 数据
	 * @param documentList
	 * @param htmlName
	 * @param oaImp
	 */
	private void analyzeXmlDOC(NodeList documentList,String htmlName,OaImport oaImp,Map<String,Object> tableNameMap,List<SysUser> sysUserList,String fileId,File xml) {
		List<FileStorage> FileStorages = new ArrayList();
		String nowPath = oaImp.getFilePath();
		int docCount = 0;
		for(int d=0;d<documentList.getLength();d++){
			Element element3 = (Element)documentList.item(d);
			String DocumentContent=element3.getElementsByTagName("FileContent").item(0).getFirstChild().getNodeValue();
			if(DocumentContent.equals(htmlName) ){
				continue;
			}
			docCount ++;
			String DocumentName=element3.getElementsByTagName("FileName").item(0).getFirstChild().getNodeValue();
			/**查找附件是否存在指定位置，如果存在上传minio 返回存储地址**/
			String documentPath=nowPath+"/file/"+DocumentContent;
			File file = new File(documentPath);
			if(file.exists()){
				FileStorage FileStorage = upload(file,oaImp.getTenantId(),DocumentName);
				FileStorages.add(FileStorage);
			}else{
				String erroMsg ="对应的电子文件："+documentPath+" 不存在请确认！";
				LocalDateTime nowDate = LocalDateTime.now();
				//moveDir(xml,OaImpConstant.failPath,oaImp.getFilePath());
				//记录成功或者失败日志
				//saveOaLogs(oaImp,xml,erroMsg,nowDate,OaImpConstant.impFail);
				//continue;
			}

		}
		//是否导入同名html
		int impHtml = oaImp.getImportHtml();
		if(impHtml == OaImpConstant.impHtml){
			String documentPath=nowPath+"/file/"+htmlName;
			File file = new File(documentPath);
			if(file.exists()){
				docCount++;
				FileStorage FileStorage = upload(file,oaImp.getTenantId(),htmlName);
				FileStorages.add(FileStorage);
			}
		}
		// 插入doc 数据
		insertDoc(FileStorages,tableNameMap,sysUserList,fileId,docCount);
	}

	private void insertDoc(List<FileStorage> fileStorages,Map<String,Object> tableNameMap,List<SysUser> sysUserList,String fileId,int docCount) {
		String docTableName = tableNameMap.get("docTableName")+"";
		String fileTableName = tableNameMap.get("fileTableName")+"";
		if(fileStorages.size()>0) {
			final List<Map<String, Object>> listMap = new ArrayList<>();
			fileStorages.stream().forEach(
					fileStorage -> {
						String id = IdGenerator.getIdStr();
						final Map<String, Object> map = CollectionUtil.newHashMap();
						map.put(FieldConstants.ID, id);
						map.put(FieldConstants.Document.FILE_STORAGE_ID, fileStorage.getId());
						map.put(FieldConstants.Document.FILE_NAME, fileStorage.getFileSourceName());
						map.put(FieldConstants.Document.FILE_SIZE, fileStorage.getFileSize());
						map.put(FieldConstants.Document.FILE_FORMAT, fileStorage.getFileType());
						map.put(FieldConstants.IS_DELETE, "0");
						//根据全文格式获取全文类型
						map.put(FieldConstants.Document.DOC_TYPE, getTextType(fileStorage.getFileType()));
						map.put(FieldConstants.OWNER_ID, fileId);
						listMap.add(map);
					}
			);
			final DynamicArchiveDTO archiveDTO = new DynamicArchiveDTO();
			archiveDTO.setTableName(docTableName);
			List<String> filterColumn = Arrays.asList(
					FieldConstants.Bak.ID,
					FieldConstants.Document.FILE_STORAGE_ID,
					FieldConstants.Document.FILE_NAME,
					FieldConstants.Document.FILE_SIZE,
					FieldConstants.Document.FILE_FORMAT,
					FieldConstants.IS_DELETE,
					FieldConstants.OWNER_ID
			);
			archiveDTO.setFilterColumn(filterColumn);
			archiveDTO.setListMap(listMap);
			tableNameMap.put("docCount", docCount);
			R<Integer> result = remoteArchiveInnerService.saveBatch(archiveDTO, SecurityConstants.FROM_IN);
		}
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
	 * 上传附件到电子文件中心
	 * @param tempFile
	 * @return
	 */
	private FileStorage upload(File tempFile,Long tenantId,String resourseName) {
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
	 * 过程信息相关字段
	 * @param opinionList
	 */
	private void analyzeXmlOpinion(NodeList opinionList,Map<String,Object> tableNameMap,List<SysUser> sysUserList,String fileId) {
		final List<Map<String, String>> opinionSqlList = new ArrayList<>();
		try {
			for (int ol = 0; ol < opinionList.getLength(); ol++) {
				Element opinion = (Element)opinionList.item(ol);
				String nodeName = opinion.getElementsByTagName("NodeName").item(0).getFirstChild()==null?"":opinion.getElementsByTagName("NodeName").item(0).getFirstChild().getNodeValue();
				//操作人
				String operator = opinion.getElementsByTagName("Operator").item(0).getFirstChild()==null?"":opinion.getElementsByTagName("Operator").item(0).getFirstChild().getNodeValue();
				//操作时间
				String operateTime = opinion.getElementsByTagName("OperateTime").item(0).getFirstChild()==null?"":opinion.getElementsByTagName("OperateTime").item(0).getFirstChild().getNodeValue();
				String opinionContent = "";
				if(opinion.getElementsByTagName("OpinionContent").item(0).getFirstChild()!=null){
					opinionContent = opinion.getElementsByTagName("OpinionContent").item(0).getFirstChild().getNodeValue();
				}
				Map<String,String> optionMap = new HashMap<String,String>();
				optionMap.put("nodeName", nodeName);
				optionMap.put("operator", getUserIdByName(sysUserList,operator));
				//optionMap.put("operator", operator);
				optionMap.put("userName", operator);
				optionMap.put("operateTime", operateTime);
				optionMap.put("opinionContent", opinionContent);
				if(optionMap!=null){
					opinionSqlList.add(optionMap);
				}
			}
			//插入过程信息表
			insertOpinion(opinionSqlList,tableNameMap, fileId);
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 插入过程信息表
	 * @param opinionSqlList
	 */
	private void insertOpinion(List<Map<String, String>> opinionSqlList,Map<String,Object> tableNameMap,String fileId) {
		final List<Map<String, Object>> listMap = new ArrayList<>();
		opinionSqlList.stream().forEach(
				nodeMap ->{
					String id = IdGenerator.getIdStr();
					final Map<String, Object> map = CollectionUtil.newHashMap();
					map.put(FieldConstants.ID, id);
					map.put(FieldConstants.Info.CREATE_USER_ID, nodeMap.get("operator"));
					map.put(FieldConstants.Info.SERVICE_ACTION_TIME, nodeMap.get("operateTime"));
					map.put(FieldConstants.CREATED_TIME, nodeMap.get("operateTime"));
					map.put(FieldConstants.Info.SERVICE_ACTION, nodeMap.get("nodeName"));
					map.put(FieldConstants.Info.CREATE_USER_NAME, nodeMap.get("userName"));
					map.put(FieldConstants.IS_DELETE,"0");
					//根据全文格式获取全文类型
					map.put(FieldConstants.Info.USER_OPINION, nodeMap.get("opinionContent"));
					map.put(FieldConstants.OWNER_ID, fileId);
					listMap.add(map);
				}
		);
		final DynamicArchiveDTO archiveDTO = new DynamicArchiveDTO();
		String docTableName = tableNameMap.get("infoTableName")+"";
		archiveDTO.setTableName(docTableName);
		List<String> filterColumn = Arrays.asList(
				FieldConstants.ID,
				FieldConstants.Info.CREATE_USER_ID,
				FieldConstants.Info.SERVICE_ACTION_TIME,
				FieldConstants.CREATED_TIME,
				FieldConstants.Info.SERVICE_ACTION,
				FieldConstants.Info.CREATE_USER_NAME,
				FieldConstants.IS_DELETE,
				FieldConstants.Info.USER_OPINION,
				FieldConstants.OWNER_ID
		);
		archiveDTO.setFilterColumn(filterColumn);
		archiveDTO.setListMap(listMap);
		R<Integer>  result = remoteArchiveInnerService.saveBatch(archiveDTO, SecurityConstants.FROM_IN);
	}


	/**
	 * 根据所有的用户 找到对应的 用户id
	 * @param sysUserList
	 * @param operator
	 * @return
	 */
	private String getUserIdByName(List<SysUser> sysUserList, String operator) {
		String returnID = "";
		Optional<SysUser> sysUser = sysUserList.stream().filter(
				sysUsers -> operator.equals(sysUsers.getChineseName())
		).findFirst();
		if (sysUser.isPresent()) {
			SysUser a = sysUser.get();   //这样子就取到了这个对象呢。
			returnID = a.getUserId()+"";
		}else {
			//没有查到的逻辑
			returnID = operator;
		}
		return returnID;
	}
}
