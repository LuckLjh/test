package com.cesgroup.bpm.calendar;

import com.cesgroup.api.workcal.WorkCalendarConnector;

import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.util.DefaultClockImpl;
import org.activiti.engine.runtime.ClockReader;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Calendar;
import java.util.Date;

import javax.xml.datatype.Duration;

/**
 * 支持工作日的工作日历.
 * 
 * @author 国栋
 *
 */
public abstract class AdvancedBusinessCalendar implements BusinessCalendar {

    private WorkCalendarConnector workCalendarConnector;

    protected ClockReader clockReader = new DefaultClockImpl();

    /**
     * 解析截止时间.
     */
    @Override
    public Date resolveDuedate(String duedateDescription) {
        return resolveDuedate(duedateDescription, -1);
    }

    /**
     * 解析截止时间.
     */
    @Override
    public abstract Date resolveDuedate(String duedateDescription, int maxIterations);

    /**
     * 校验截止时间是否有效.
     */
    @Override
    public Boolean validateDuedate(String duedateDescription, int maxIterations, Date endDate,
        Date newTimer) {
        return (endDate == null) || endDate.after(newTimer) || endDate.equals(newTimer);
    }

    /**
     * 解析结束时间.
     */
    @Override
    public Date resolveEndDate(String endDateString) {
        return ISODateTimeFormat.dateTimeParser()
            .withZone(DateTimeZone.forTimeZone(clockReader.getCurrentTimeZone()))
            .parseDateTime(endDateString).toCalendar(null).getTime();
    }

    /**
     * 日期处理
     * 
     * @param date
     *            日期
     * @param useBusinessTime
     *            是否使用工作日
     * @return 计算后日期
     */
    public Date processDate(Date date, boolean useBusinessTime) {
        if (!useBusinessTime) {
            return date;
        }

        // TODO: tenantId
        return workCalendarConnector.processDate(date, "1");
    }

    /**
     * 增加日期
     * 
     * @param date
     *            日期
     * @param duration
     *            时长
     * @param useBusinessTime
     *            是否使用工作日
     * @return 计算后日期
     */
    public Date add(Date date, Duration duration, boolean useBusinessTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        if (!useBusinessTime) {
            duration.addTo(calendar);

            return calendar.getTime();
        }

        // TODO: tenantId
        return workCalendarConnector.add(date, duration, "1");
    }

    public void setWorkCalendarConnector(WorkCalendarConnector workCalendarConnector) {
        this.workCalendarConnector = workCalendarConnector;
    }

    // ~ ======================================================================
    public abstract String getName();
}
