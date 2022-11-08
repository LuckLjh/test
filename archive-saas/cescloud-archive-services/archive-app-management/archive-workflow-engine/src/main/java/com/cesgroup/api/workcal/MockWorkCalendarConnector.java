package com.cesgroup.api.workcal;

import java.util.Date;

import javax.xml.datatype.Duration;

/**
 * 工作日历连接器默认实现
 * 
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public class MockWorkCalendarConnector implements WorkCalendarConnector {

    @Override
    public Date processDate(Date date, String tenantId) {
        return date;
    }

    @Override
    public Date add(Date date, Duration duration, String tenantId) {
        duration.addTo(date);

        return date;
    }
}
