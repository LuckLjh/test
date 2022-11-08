/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.listener.task</p>
 * <p>文件名:GlobalCommonTaskListener.java</p>
 * <p>创建时间:2019年11月13日 上午9:37:49</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.listener.task;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年11月13日
 */
@Slf4j
public class GlobalCommonCompleteTaskListener implements TaskListener {

    /**
     *
     */
    private static final long serialVersionUID = 3339573795356785262L;

    /**
     *
     * @see org.activiti.engine.delegate.TaskListener#notify(org.activiti.engine.delegate.DelegateTask)
     */
    @Override
    public void notify(DelegateTask delegateTask) {
        final String eventName = delegateTask.getEventName();
        if (log.isInfoEnabled()) {
            log.info("task listener event name: {}", eventName);
        }
        switch (eventName) {
        case EVENTNAME_COMPLETE:

            break;

        default:
            break;
        }
    }

}
