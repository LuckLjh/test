/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.listener.event</p>
 * <p>文件名:GlobalCommonEventListener.java</p>
 * <p>创建时间:2019年11月13日 上午9:36:22</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.listener.event;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年11月13日
 */
@Slf4j
public class GlobalCommonCompleteEventListener implements ActivitiEventListener {

    /**
     *
     * @see org.activiti.engine.delegate.event.ActivitiEventListener#onEvent(org.activiti.engine.delegate.event.ActivitiEvent)
     */
    @Override
    public void onEvent(ActivitiEvent event) {
        final ActivitiEventType type = event.getType();
        if (log.isInfoEnabled()) {
            log.info("event listener event type: {}", type.name());
        }
        switch (type) {
        case ENTITY_CREATED:

            break;

        default:
            break;
        }
    }

    /**
     *
     * @see org.activiti.engine.delegate.event.ActivitiEventListener#isFailOnException()
     */
    @Override
    public boolean isFailOnException() {
        // TODO Auto-generated method stub
        return false;
    }

}
