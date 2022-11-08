/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.listener.execution</p>
 * <p>文件名:GlobalCommonExecutionListener.java</p>
 * <p>创建时间:2019年11月13日 上午9:37:10</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.listener.execution;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年11月13日
 */
@Slf4j
public class GlobalCommonEndExecutionListener implements ExecutionListener {

    /**
     *
     */
    private static final long serialVersionUID = -2637300744976231809L;

    /**
     *
     * @see org.activiti.engine.delegate.ExecutionListener#notify(org.activiti.engine.delegate.DelegateExecution)
     */
    @Override
    public void notify(DelegateExecution execution) throws Exception {
        final String eventName = execution.getEventName();
        if (log.isInfoEnabled()) {
            log.info("execution listener event name: {}", eventName);
        }
        switch (eventName) {
        case EVENTNAME_END:

            break;

        default:
            break;
        }
    }

}
