package com.cesgroup.bpm.calendar;

import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.calendar.BusinessCalendarManager;

import java.util.HashMap;
import java.util.Map;

/**
 * 业务日历管理器
 * 
 * @author 国栋
 *
 */
public class AdvancedBusinessCalendarManager implements BusinessCalendarManager {

    private Map<String, BusinessCalendar> businessCalendarMap;

    /**
     * 创建一个业务日历管理器
     */
    public AdvancedBusinessCalendarManager() {
        businessCalendarMap = new HashMap<String, BusinessCalendar>();
        this.addBusinessCalendar(new DueDateBusinessCalendar());
        this.addBusinessCalendar(new DurationBusinessCalendar());
        this.addBusinessCalendar(new CycleBusinessCalendar());
    }

    public void addBusinessCalendar(AdvancedBusinessCalendar businessCalendar) {
        businessCalendarMap.put(businessCalendar.getName(), businessCalendar);
    }

    @Override
    public BusinessCalendar getBusinessCalendar(String businessCalendarRef) {
        return businessCalendarMap.get(businessCalendarRef);
    }
}
