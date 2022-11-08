package com.cesgroup.bpm.calendar;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.calendar.CronExpression;

import java.util.Date;

/**
 * 循环业务日历
 * 
 * @author 国栋
 *
 */
public class CycleBusinessCalendar extends AdvancedBusinessCalendar {

    @Override
    public Date resolveDuedate(String duedate, int maxIterations) {
        String textWithoutBusiness = duedate;
        boolean isBusinessTime = textWithoutBusiness.startsWith("business");

        if (isBusinessTime) {
            textWithoutBusiness = textWithoutBusiness.substring("business".length()).trim();
        }

        try {
            if (textWithoutBusiness.startsWith("R")) {
                return new DurationUtil(duedate, this).getDateAfter();
            } else {
                CronExpression ce = new CronExpression(duedate, null);

                return ce.getTimeAfter(new Date());
            }
        } catch (Exception e) {
            throw new ActivitiException("表达式解析失败: " + duedate, e);
        }
    }

    @Override
    public String getName() {
        return "cycle";
    }
}
