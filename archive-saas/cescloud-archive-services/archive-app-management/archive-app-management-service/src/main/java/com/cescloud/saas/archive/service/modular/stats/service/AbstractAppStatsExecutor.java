/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.stats.service</p>
 * <p>文件名:AbstractAppStatsExecutor.java</p>
 * <p>创建时间:2020年9月27日 上午10:03:58</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.stats.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveType;
import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateTable;
import com.cescloud.saas.archive.api.modular.datasource.dto.DynamicArchiveDTO;
import com.cescloud.saas.archive.api.modular.datasource.feign.RemoteArchiveInnerService;
import com.cescloud.saas.archive.api.modular.stats.dto.ArchiveTypeStatsDTO;
import com.cescloud.saas.archive.api.modular.stats.dto.StatsTaskDTO;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTypeService;
import com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTableService;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年9月27日
 */
@Slf4j
public abstract class AbstractAppStatsExecutor<M extends BaseMapper<T>, T> extends AbstractStatsExecutor<M, T>
    implements StatsExecutorService {

    /**
     * 档案门类service
     *
     * @return
     */
    protected abstract ArchiveTypeService getArchiveTypeService();

    /**
     * 档案门类表service
     *
     * @return
     */
    protected abstract ArchiveTableService getArchiveTableService();

    /**
     * （档案数据）feign调用接口
     *
     * @return
     */
    protected abstract RemoteArchiveInnerService getRemoteArchiveInnerService();

    /**
     * 模板表service
     *
     * @return
     */
    protected abstract TemplateTableService getTemplateTableService();

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.stats.service.AbstractStatsExecutor#statsExecutorService()
     */
    @Override
    protected StatsExecutorService statsExecutorService() {
        return this;
    }

    @Override
    protected ArchiveTypeStatsDTO getStatsDTO() {

        final List<ArchiveType> typeList = getArchiveTypeService().list();
        if (null == typeList || typeList.isEmpty()) {
            if (log.isWarnEnabled()) {
                log.warn("获取档案门类信息为空");
            }
            return null;
        }

        final List<ArchiveTable> tableList = getArchiveTableService().list();
        if (null == typeList || typeList.isEmpty()) {
            if (log.isWarnEnabled()) {
                log.warn("获取档案门类表信息为空");
            }
            return null;
        }

        final List<TemplateTable> templateTableList = getTemplateTableService().list();

        return toStatsDTO(typeList, tableList, templateTableList);
    }


	@Override
	protected List<ArchiveTypeStatsDTO> getStatsDTOList() {

		final List<ArchiveType> typeList = getArchiveTypeService().list();
		if (null == typeList || typeList.isEmpty()) {
			if (log.isWarnEnabled()) {
				log.warn("获取档案门类信息为空");
			}
			return null;
		}

		final List<ArchiveTable> tableList = getArchiveTableService().list();
		if (null == typeList || typeList.isEmpty()) {
			if (log.isWarnEnabled()) {
				log.warn("获取档案门类表信息为空");
			}
			return null;
		}

		final List<TemplateTable> templateTableList = getTemplateTableService().list();

		return toStatsDTOList(typeList, tableList, templateTableList);
	}

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.stats.service.StatsExecutorService#getStatsData(com.cescloud.saas.archive.api.modular.stats.dto.StatsTaskDTO)
     */
    @Override
    public List<Map<String, Object>> getStatsData(StatsTaskDTO statsTaskDTO) {
    	if(ObjectUtil.isNull(getDynamicArchiveVo(statsTaskDTO).getTableName())){
			return Collections.emptyList();
		}
        final R<List<Map<String, Object>>> remoteData = getRemoteArchiveInnerService()
            .getListByCondition(getDynamicArchiveVo(statsTaskDTO), SecurityConstants.FROM_IN);

        if (!CommonConstants.SUCCESS.equals(remoteData.getCode())) {
            if (log.isErrorEnabled()) {
                log.error("sharding查询[{}]出错：{}", statsTaskDTO.getFilingTypeStatsDTO().getStorageLocate(),
                    remoteData.getMsg());
            }
            return Collections.emptyList();
        }

        if (null == remoteData.getData()) {
            if (log.isWarnEnabled()) {
                log.warn("sharding查询[{}]数据为空", statsTaskDTO.getFilingTypeStatsDTO().getStorageLocate());
            }
            return Collections.emptyList();
        }
        return remoteData.getData();
    }

    /**
     * @param statsTaskDTO
     * @return
     */
    protected abstract DynamicArchiveDTO getDynamicArchiveVo(StatsTaskDTO statsTaskDTO);

}
