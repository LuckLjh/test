/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.stats.service</p>
 * <p>文件名:YearStatsService.java</p>
 * <p>创建时间:2020年10月23日 下午1:51:44</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.stats.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.stats.dto.YearStatsDTO;
import com.cescloud.saas.archive.api.modular.stats.entity.YearStats;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.util.List;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年10月23日
 */
public interface YearStatsService extends IService<YearStats> {

    /**
     * 根据年度查询
     *
     * @param fondsCode
     * @param yearCode
     * @return
     */
    YearStatsDTO getByYearCode(String fondsCode, String yearCode);

    /**
     * 新增年报
     *
     * @param entityDTO
     * @return
     */
    boolean save(YearStatsDTO entityDTO);

    /**
     * 修改年报
     *
     * @param entityDTO
     * @return
     */
    boolean updateById(YearStatsDTO entityDTO);

    /**
     * 获取全宗下的所有年度
     *
     * @param fondsCode
     * @return
     */
    List<YearStats> getYearCodeByFondsCode(String fondsCode);

    /**
     * 导出年度excel文件
     *
     * @param id
     * @return
     */
    HSSFWorkbook export(Long id);

    /**
     * 转成DTO
     *
     * @param entity
     * @return
     */
    YearStatsDTO toDTO(YearStats entity);

    /**
     * 根据租户id 清除租户信息
     * @param tenantId
     * @return
     */
    Boolean removeByTenantId(Long tenantId);


	/**
	 * 年报数据统计
	 * @param
	 * @return
	 */
	R yearStatsCount(String fondsCode, Long yearStatsId);
}
