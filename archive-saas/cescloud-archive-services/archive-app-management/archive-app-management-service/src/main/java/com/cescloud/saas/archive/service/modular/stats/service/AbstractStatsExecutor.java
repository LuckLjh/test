/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.stats.service</p>
 * <p>文件名:AbstractStatsExecutor.java</p>
 * <p>创建时间:2020年9月25日 上午10:36:00</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.stats.service;

import cn.hutool.core.thread.NamedThreadFactory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveType;
import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateTable;
import com.cescloud.saas.archive.api.modular.stats.constant.StatsFilingTypeEnum;
import com.cescloud.saas.archive.api.modular.stats.dto.ArchiveTypeStatsDTO;
import com.cescloud.saas.archive.api.modular.stats.dto.FilingTypeStatsDTO;
import com.cescloud.saas.archive.api.modular.stats.dto.StatsTaskDTO;
import com.cescloud.saas.archive.common.constants.ArchiveLayerEnum;
import com.cescloud.saas.archive.common.constants.FieldConstants;
import com.cescloud.saas.archive.service.modular.stats.thread.StatsTask;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author qiucs
 * @version 1.0.0 2020年9月25日
 */
@Slf4j
public abstract class AbstractStatsExecutor<M extends BaseMapper<T>, T> extends ServiceImpl<M, T>
        implements StatsExecutor {

    /**
     * @see StatsExecutor#execute()
     */
    @Override
    public void execute() {
        if (log.isInfoEnabled()) {
            log.info("执行[" + statsName() + "]统计");
        }
        //final ArchiveTypeStatsDTO statsDTO = getStatsDTO();
		 List<ArchiveTypeStatsDTO> statsDTOList = getStatsDTOList();
        if (null == statsDTOList) {
            return;
        }
        //临时去掉其他租户用于测试
/*		statsDTOList = statsDTOList.stream().filter(
				statsDTO -> statsDTO.getTenantId().equals(278L)
		).collect(Collectors.toList());*/
        final String[] statsFields = getStatsFields();
/*        for (final String statsField : statsFields) {
            execute(statsDTO, statsField);
        }*/
		for (int i=0;i<statsDTOList.size();i++){
			for(int j=0;j<statsFields.length;j++){
				execute(statsDTOList.get(i), statsFields[j]);
			}
		}
    }

    /**
     * 表示按哪个字段进行统计，多个字段表示统计多次，默认按保管期限字段进行统计
     *
     * @return
     */
    protected String[] getStatsFields() {
        return new String[]{FieldConstants.RETENTION_PERIOD};
    }

    /**
     * @see StatsExecutor#execute(ArchiveTypeStatsDTO,
     * String)
     */
    @Override
    public void execute(ArchiveTypeStatsDTO statsDTO, String statsField) {
        if (null == statsDTO) {
            return;
        }

        if (statsDTO.getFolderArchiveTypeList().isEmpty() && statsDTO.getFileArchiveTypeList().isEmpty()) {
            return;
        }

        final ExecutorService executor = initThreadPool(statsDTO.getTenantId());

        try {
            submitTask(executor, statsDTO.getTenantId(), statsField, StatsFilingTypeEnum.PROJECT,
                    statsDTO.getProjectArchiveTypeList());
            submitTask(executor, statsDTO.getTenantId(), statsField, StatsFilingTypeEnum.FOLDER,
                    statsDTO.getFolderArchiveTypeList());
            submitTask(executor, statsDTO.getTenantId(), statsField, StatsFilingTypeEnum.ONE,
                    statsDTO.getFileArchiveTypeList());
            submitTask(executor, statsDTO.getTenantId(), statsField, StatsFilingTypeEnum.SINGAL,
                    statsDTO.getSingleArchiveTypeList());
            submitTask(executor, statsDTO.getTenantId(), statsField, StatsFilingTypeEnum.DOCUMENT,
                    statsDTO.getDocumentArchiveTypeList());
        } finally {
            if (null != executor) {
                executor.shutdown();
            }
        }
    }

    private void submitTask(ExecutorService executor, Long tenantId, String statsField,
                            StatsFilingTypeEnum statsFilingTypeEnum, List<FilingTypeStatsDTO> folderList) {
        for (final FilingTypeStatsDTO filingTypeStatsDTO : folderList) {
            if (!check(filingTypeStatsDTO)) {
                continue;
            }
            executor.submit(
                    new StatsTask(toStatsTaskDTO(tenantId, filingTypeStatsDTO, statsFilingTypeEnum, statsField),
                            statsExecutorService()));
        }
    }

    private StatsTaskDTO toStatsTaskDTO(Long tenantId, FilingTypeStatsDTO filingTypeStatsDTO,
                                        StatsFilingTypeEnum statsFilingTypeEnum, String statsField) {
        return new StatsTaskDTO(tenantId, statsFilingTypeEnum, statsField, filingTypeStatsDTO);
    }

    private ExecutorService initThreadPool(Long tenantId) {
        final int coreSize = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(coreSize,
                coreSize + 2,
                1L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(512),
                new NamedThreadFactory(statsName() + "_stats_threadpool_" + tenantId, false));
    }

    protected abstract StatsExecutorService statsExecutorService();

    /**
     * 检查是否需要统计
     *
     * @param filingTypeStatsDTO
     * @return
     */
    protected abstract boolean check(FilingTypeStatsDTO filingTypeStatsDTO);

    /**
     * 设置统计名称
     *
     * @return
     */
    protected abstract String statsName();

    /**
     * 获取需要统计的档案门类
     *
     * @return
     */
    protected abstract ArchiveTypeStatsDTO getStatsDTO();

	/**
	 * 获取需要所有租户需要统计的档案门类
	 *
	 * @return
	 */
	protected abstract List<ArchiveTypeStatsDTO> getStatsDTOList();
    /**
     * 转化为档案门类统计DTO
     *
     * @param typeList
     * @param tableList
     * @param templateTableList
     * @return
     */
    protected ArchiveTypeStatsDTO toStatsDTO(List<ArchiveType> typeList, List<ArchiveTable> tableList,
                                             List<TemplateTable> templateTableList) {

        final ArchiveTypeStatsDTO statsDTO = new ArchiveTypeStatsDTO();

        statsDTO.setTenantId(typeList.get(0).getTenantId());

        final Map<String, ArchiveType> typeMap = toTypeMap(typeList);

        final Map<Long, Long> templateParentMap = toTemplateParentMap(templateTableList);

        final Map<String, ArchiveTable> templateTableMap = toTemplateTableMap(tableList);

        for (final ArchiveTable table : tableList) {
        	if(typeMap.get(table.getArchiveTypeCode())==null){
        		continue;
			}
            if (ArchiveLayerEnum.PROJECT.getCode().equals(table.getArchiveLayer())) {
                statsDTO.getProjectArchiveTypeList()
                        .add(toFilingTypeStatsDTO(typeMap.get(table.getArchiveTypeCode()), table, templateParentMap,
                                templateTableMap));
            } else if (ArchiveLayerEnum.FOLDER.getCode().equals(table.getArchiveLayer())) {
                statsDTO.getFolderArchiveTypeList()
                        .add(toFilingTypeStatsDTO(typeMap.get(table.getArchiveTypeCode()), table, templateParentMap,
                                templateTableMap));
            } else if (ArchiveLayerEnum.ONE.getCode().equals(table.getArchiveLayer())) {
                statsDTO.getFileArchiveTypeList()
                        .add(toFilingTypeStatsDTO(typeMap.get(table.getArchiveTypeCode()), table, templateParentMap,
                                templateTableMap));
            } else if (ArchiveLayerEnum.SINGLE.getCode().equals(table.getArchiveLayer())) {
                statsDTO.getSingleArchiveTypeList()
                        .add(toFilingTypeStatsDTO(typeMap.get(table.getArchiveTypeCode()), table, templateParentMap,
                                templateTableMap));
            } else if (ArchiveLayerEnum.DOCUMENT.getCode().equals(table.getArchiveLayer())) {
                statsDTO.getDocumentArchiveTypeList()
                        .add(toFilingTypeStatsDTO(typeMap.get(table.getArchiveTypeCode()), table, templateParentMap,
                                templateTableMap));
            }
        }

        return statsDTO;
    }
	/**
	 * 转化为档案门类统计DTO
	 *
	 * @param typeList
	 * @param tableList
	 * @param templateTableList
	 * @return
	 */
	protected List<ArchiveTypeStatsDTO> toStatsDTOList(List<ArchiveType> typeList, List<ArchiveTable> tableList,
											 List<TemplateTable> templateTableList) {
		List<ArchiveTypeStatsDTO> retList = new ArrayList<>();

		Set<Long> tentIdSet =  new HashSet<>();
		// 获取所有得租户组
		typeList.stream().forEach(
				archiveType ->{
					tentIdSet.add(archiveType.getTenantId());
				}
		);
		List<Long> tentIdList = new ArrayList<>(tentIdSet);
		for(int i=0;i<tentIdList.size();i++) {
			final ArchiveTypeStatsDTO statsDTO = new ArchiveTypeStatsDTO();
			long nowtentId = tentIdList.get(i);
			statsDTO.setTenantId(tentIdList.get(i));
			List<ArchiveTable> tentIdTableList = tableList.stream().filter(
					t -> t.getTenantId().equals(nowtentId)
			).collect(Collectors.toList());
			final Map<String, ArchiveType> typeMap = toTypeMap(typeList);

			final Map<Long, Long> templateParentMap = toTemplateParentMap(templateTableList);

			final Map<String, ArchiveTable> templateTableMap = toTemplateTableMap(tentIdTableList);

			for (final ArchiveTable table : tentIdTableList) {
				if (typeMap.get(table.getArchiveTypeCode()) == null) {
					continue;
				}
				if (ArchiveLayerEnum.PROJECT.getCode().equals(table.getArchiveLayer())) {
					statsDTO.getProjectArchiveTypeList()
							.add(toFilingTypeStatsDTO(typeMap.get(table.getArchiveTypeCode()), table, templateParentMap,
									templateTableMap));
				} else if (ArchiveLayerEnum.FOLDER.getCode().equals(table.getArchiveLayer())) {
					statsDTO.getFolderArchiveTypeList()
							.add(toFilingTypeStatsDTO(typeMap.get(table.getArchiveTypeCode()), table, templateParentMap,
									templateTableMap));
				} else if (ArchiveLayerEnum.ONE.getCode().equals(table.getArchiveLayer())) {
					statsDTO.getFileArchiveTypeList()
							.add(toFilingTypeStatsDTO(typeMap.get(table.getArchiveTypeCode()), table, templateParentMap,
									templateTableMap));
				} else if (ArchiveLayerEnum.SINGLE.getCode().equals(table.getArchiveLayer())) {
					statsDTO.getSingleArchiveTypeList()
							.add(toFilingTypeStatsDTO(typeMap.get(table.getArchiveTypeCode()), table, templateParentMap,
									templateTableMap));
				} else if (ArchiveLayerEnum.DOCUMENT.getCode().equals(table.getArchiveLayer())) {
					statsDTO.getDocumentArchiveTypeList()
							.add(toFilingTypeStatsDTO(typeMap.get(table.getArchiveTypeCode()), table, templateParentMap,
									templateTableMap));
				}
			}
			retList.add(statsDTO);
		}
		return retList;
	}
    private FilingTypeStatsDTO toFilingTypeStatsDTO(ArchiveType type, ArchiveTable table, Map<Long, Long> parentMap,
                                                    Map<String, ArchiveTable> templateTableMap) {
        final FilingTypeStatsDTO filingTypeStatsDTO = new FilingTypeStatsDTO();
        filingTypeStatsDTO.setArchiveTypeCode(table.getArchiveTypeCode());
        if(type==null){
        	log.info("将会出现空指针异常的表信息：{}",table);
		}
		filingTypeStatsDTO.setArchiveTypeName(type.getTypeName());
        filingTypeStatsDTO.setClassType(type.getClassType());
        filingTypeStatsDTO.setArchiveTypeFilingType(type.getFilingType());
        filingTypeStatsDTO.setStorageLocate(table.getStorageLocate());
        final Long templateTableId = table.getTemplateTableId();
        final Long parentTemplateTableId = parentMap.get(templateTableId);
        if (null != parentTemplateTableId) {
            final ArchiveTable parentTable = templateTableMap.get(parentTemplateTableId + "-" + table.getArchiveTypeCode());
            if (null != parentTable) {
                filingTypeStatsDTO.setParentStorageLocate(parentTable.getStorageLocate());
            }
        }
        return filingTypeStatsDTO;
    }

    private Map<String, ArchiveType> toTypeMap(List<ArchiveType> typeList) {
        final Map<String, ArchiveType> typeMap = new HashMap<String, ArchiveType>(typeList.size());

        for (final ArchiveType type : typeList) {
            typeMap.put(type.getTypeCode(), type);
        }

        return typeMap;
    }

    private Map<Long, Long> toTemplateParentMap(List<TemplateTable> templateTableList) {
        final Map<Long, Long> parentMap = new HashMap<Long, Long>(templateTableList.size());

        for (final TemplateTable tt : templateTableList) {
            parentMap.put(tt.getId(), tt.getParentId());
        }

        return parentMap;
    }

    private Map<String, ArchiveTable> toTemplateTableMap(List<ArchiveTable> tableList) {
        final Map<String, ArchiveTable> templateTableMap = new HashMap<String, ArchiveTable>(tableList.size());

        for (final ArchiveTable table : tableList) {
            templateTableMap.put(table.getTemplateTableId() + "-" + table.getArchiveTypeCode(), table);
        }

        return templateTableMap;
    }

}
