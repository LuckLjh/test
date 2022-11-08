package com.cescloud.saas.archive.service.modular.fwimp.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.fwimp.entity.OaColumn;
import com.cescloud.saas.archive.api.modular.metadata.dto.MetadataDTO;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.api.modular.metadata.feign.RemoteMetadataService;
import com.cescloud.saas.archive.common.constants.ArchiveLayerEnum;
import com.cescloud.saas.archive.common.util.ArchiveConfigUtil;
import com.cescloud.saas.archive.common.util.ArchiveUtil;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.fwimp.mapper.OaColumnMapper;
import com.cescloud.saas.archive.service.modular.fwimp.service.OaColumnService;
import com.cescloud.saas.archive.service.modular.fwimp.service.item.ExcelData;
import com.cescloud.saas.archive.service.modular.fwimp.service.listener.ExcelReadListener;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@CacheConfig(cacheNames = "OaColumn")
@RequiredArgsConstructor
public class OaColumnServiceImpl extends ServiceImpl<OaColumnMapper, OaColumn> implements OaColumnService {
	private final ArchiveUtil archiveUtil;
	@Autowired
	private ArchiveConfigUtil archiveConfigUtil;
	@Autowired
	private  ArchiveTableService archiveTableService;

	@Override
	public List<OaColumn> getOaColumnDetail(Long id) {
		Long ownerId = id;
		LambdaQueryWrapper<OaColumn> queryWrapper = Wrappers.<OaColumn>query().lambda();
		queryWrapper.eq(OaColumn :: getOwnerId, id);
		return list(queryWrapper);
	}

	@Override
	public R uploadExcel(MultipartFile file, String tableName) throws ArchiveBusinessException {
		List<OaColumn> returnlist = new ArrayList();
		if(StrUtil.isBlank(tableName)){
			throw new ArchiveBusinessException("请先选择导入的档案树节点！");
		}
		//无论什么表名都取文件层的表名
		ArchiveTable archiveTable = archiveTableService.getTableByStorageLocate(tableName);
		String archiveLayer = archiveTable.getArchiveLayer();
		if (archiveLayer.equals(ArchiveLayerEnum.FILE.getValue())
				|| archiveLayer.equals(ArchiveLayerEnum.ONE.getValue())
				|| archiveLayer.equals(ArchiveLayerEnum.SINGLE.getValue())) {
			tableName = tableName;
		}else{
			//List<ArchiveTable> archiveTables = archiveUtil.getArchiveTables(archiveTable.getArchiveTypeCode(), archiveTable.getTemplateTableId(), ArchiveLayerEnum.FOLDER.getCode());
			List<ArchiveTable> archiveTables  = archiveTableService.getDownTableByStorageLocateAndDownLayerCode(tableName, ArchiveLayerEnum.FILE.getValue());
			if(archiveTables.size()>0){
				tableName = archiveTables.get(0).getStorageLocate();
			}
		}
		try{
			@Cleanup InputStream bis = new BufferedInputStream(file.getInputStream());
			ExcelReadListener excelReadListener = new ExcelReadListener();
			EasyExcel.read(bis, ExcelData.class, excelReadListener).sheet().doRead();
			List<Object> list =  excelReadListener.getDataList();
			List<ExcelData> entityList = new ArrayList<>();
			for(Object obj:list){
				entityList.add((ExcelData)obj);
			}
			List<Metadata> MetadataList = archiveConfigUtil.getAllMetadataList(tableName);
			//循环excel 中所有的匹配的列与 字段列做对比
			for(int i =0; i<entityList.size();i++){
				String archiveColumn = entityList.get(i).getArchiveName()+"";
				for(int j=0; j<MetadataList.size();j++){
					String column = MetadataList.get(j).getMetadataChinese()+"".toUpperCase();
					archiveColumn = archiveColumn.toUpperCase();
					if(column.equals(archiveColumn)){ //匹配上了
						OaColumn oaColumn = new OaColumn();
						oaColumn.setOaName(entityList.get(i).getBusinessName()+"");
						oaColumn.setName(entityList.get(i).getArchiveName()+"");
						oaColumn.setColumnName(MetadataList.get(j).getMetadataEnglish()+"");//英文名
						oaColumn.setMetadataType(MetadataList.get(j).getMetadataType()+"");//数据类型
						oaColumn.setDictCode(MetadataList.get(j).getDictCode()+"");//绑定的数据字典code
						oaColumn.setMetadataLength(MetadataList.get(j).getMetadataLength());
						returnlist.add(oaColumn);
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return  new R<>(returnlist);
	}

	@Override
	public R<List<Metadata>> getColumnByName(String tableName) throws ArchiveBusinessException {
		//无论什么表名都取文件层的表名
		ArchiveTable archiveTable = archiveTableService.getTableByStorageLocate(tableName);
		String archiveLayer = archiveTable.getArchiveLayer();
		if (archiveLayer.equals(ArchiveLayerEnum.FILE.getValue())
				|| archiveLayer.equals(ArchiveLayerEnum.ONE.getValue())
				|| archiveLayer.equals(ArchiveLayerEnum.SINGLE.getValue())) {
			tableName = tableName;
		}else{
			//List<ArchiveTable> archiveTables = archiveUtil.getArchiveTables(archiveTable.getArchiveTypeCode(), archiveTable.getTemplateTableId(), ArchiveLayerEnum.FOLDER.getCode());
			List<ArchiveTable> archiveTables  = archiveTableService.getDownTableByStorageLocateAndDownLayerCode(tableName, ArchiveLayerEnum.FILE.getValue());
			if(archiveTables.size()>0){
				tableName = archiveTables.get(0).getStorageLocate();
			}
		}
		List<Metadata> metadatas = archiveConfigUtil.getAllMetadataList(tableName);
		return new R<>(metadatas);
	}
}
