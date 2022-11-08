/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.stats.service</p>
 * <p>文件名:FilingStatsService.java</p>
 * <p>创建时间:2020年10月14日 下午2:48:51</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.stats.service;

import com.cescloud.saas.archive.api.modular.stats.dto.FilingStatsDataDTO;
import com.cescloud.saas.archive.api.modular.stats.entity.FilingStats;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author qiucs
 * @version 1.0.0 2020年10月14日
 */
public interface FilingStatsService extends StatsExecutor {

    /**
     * 按档案门类获取统计结果
     *
     * @param fondsCodes
     *            全宗号
     * @param yearCode
     *            年度
     * @return
     */
    List<FilingStatsDataDTO> getStatsDataGroupByArchiveType(String fondsCodes, String yearCode);

    /**
     * 按归档部门获取统计结果
     *
     * @param fondsCodes
     *            全宗号
     * @param yearCode
     *            年度
     * @return
     */
    List<FilingStatsDataDTO> getStatsDataGroupByFilingDept(String fondsCodes, String yearCode);

	/**
	 * 根据租户id 清除租户信息
	 *
	 * @param tenantId 租户id
	 * @return Boolean
	 */
	Boolean removeByTenantId(Long tenantId);

	/**
	 * 根据条件全宗下的门类分组
	 * @param fondsCode
	 * @return
	 */
	List<FilingStats> filingStatsGroupByTypeCodeWithFonds(String fondsCode);
	/**
	 * 根据条件全宗下的门类分组 分页
	 * @param fondsCode
	 * @return
	 */
	Map<String, Object> deckShowTypeCodeForDept(int pageSize, int pageNumber, String fondsCode,String typeCode);

	/**
	 * 归档数量统计导出
	 *  @param fondsCodes 全宗号
	 * @param statsType  统计方式
	 * @param yearCode   年度
	 * @param response   响应
	 * @param filingType 整理方式
	 */
	void statsExportExcel(String fondsCodes, Integer statsType, String yearCode, HttpServletResponse response, Integer filingType) throws ArchiveBusinessException;
}
