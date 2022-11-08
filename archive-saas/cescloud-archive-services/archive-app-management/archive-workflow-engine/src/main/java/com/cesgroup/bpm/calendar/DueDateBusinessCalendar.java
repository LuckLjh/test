package com.cesgroup.bpm.calendar;

import org.activiti.engine.ActivitiException;

import java.util.Date;

/**
 * 截止日期日历
 * 
 * @author 国栋
 *
 */
public class DueDateBusinessCalendar extends AdvancedBusinessCalendar {

    @Override
    public Date resolveDuedate(String duedate, int maxIterations) {
        try {
            return new DurationUtil(duedate, this).getDateAfter();
        } catch (Exception e) {
            throw new ActivitiException("不能解析到期日： " + e.getMessage(), e);
        }
    }

    @Override
    public String getName() {
        return "dueDate";
    }
}
