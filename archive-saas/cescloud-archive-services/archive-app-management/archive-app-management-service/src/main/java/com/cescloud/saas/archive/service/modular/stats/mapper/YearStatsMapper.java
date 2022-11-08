/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.stats.mapper</p>
 * <p>文件名:YearStatsMapper.java</p>
 * <p>创建时间:2020年10月23日 下午1:50:17</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.stats.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.stats.entity.YearStats;
import org.apache.ibatis.annotations.Param;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年10月23日
 */
public interface YearStatsMapper extends BaseMapper<YearStats> {

    /**
     * 根据租户id 清除租户信息
     * @param tenantId
     * @return
     */
    Boolean removeByTenantId(@Param("tenantId") Long tenantId);
}
