package com.cesgroup.api.workcal;

import java.util.Date;

import javax.xml.datatype.Duration;

/**
 * 工作日历连接器
 * 
 * @author 国栋
 *
 */
public interface WorkCalendarConnector {

    /**
     * 日期处理
     * 
     * @param date
     *            日期
     * @param tenantId
     *            租户id
     * @return date
     */
    Date processDate(Date date, String tenantId);

    /**
     * 指定日期增加一定周期后的日期
     * 
     * @param date
     *            起始日期
     * @param duration
     *            工期
     * @param tenantId
     *            租户id
     * @return date
     */
    Date add(Date date, Duration duration, String tenantId);
}
