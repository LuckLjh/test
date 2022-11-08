package com.cesgroup.bpm.behavior;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.ProcessEngineImpl;

import java.util.Map;

/**
 * 流程引擎实体工厂 ,可以通过这个类获得所有与引擎相关的bean
 * 
 * @author 国栋
 *
 */
@SuppressWarnings("unchecked")
public class ProcessEngineBeanFactory {

    private static Map<Object, Object> beanFactory;

    static {
        if (beanFactory == null) {
            ProcessEngineImpl processEngine = (ProcessEngineImpl) ProcessEngines
                .getDefaultProcessEngine();
            beanFactory = (Map<Object, Object>) processEngine.getProcessEngineConfiguration()
                .getBeans();
        }
    }

    public static <T> T getBean(String beanName) {
        return (T) beanFactory.get(beanName);
    }
}
