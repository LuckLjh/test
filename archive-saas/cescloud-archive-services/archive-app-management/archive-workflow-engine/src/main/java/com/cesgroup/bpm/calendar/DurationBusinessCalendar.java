package com.cesgroup.bpm.calendar;

import org.activiti.engine.ActivitiException;

import java.util.Date;

/**
 * 时间段日历
 * 
 * @author 国栋
 *
 */
public class DurationBusinessCalendar extends AdvancedBusinessCalendar {

    @Override
    public Date resolveDuedate(String duedate, int maxIterations) {
        try {
            DurationUtil durationUtil = new DurationUtil(duedate, this);

            return durationUtil.getDateAfter();
        } catch (Exception e) {
            throw new ActivitiException("不能解析到期日： " + e.getMessage(), e);
        }
    }

    @Override
    public String getName() {
        return "duration";
    }
}
