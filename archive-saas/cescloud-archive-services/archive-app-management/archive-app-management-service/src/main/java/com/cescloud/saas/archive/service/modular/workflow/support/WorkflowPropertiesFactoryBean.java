package com.cescloud.saas.archive.service.modular.workflow.support;

import java.util.Properties;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PropertiesLoaderSupport;

/**
 * 工作流配置工厂，主要提供了默认值
 * com.cesgroup.core.spring.ApplicationPropertiesFactoryBean
 *
 * @author qiucs
 * @version 1.0.0 2019年10月14日
 */
public class WorkflowPropertiesFactoryBean extends PropertiesLoaderSupport
    implements FactoryBean<Properties>, InitializingBean {

    private final ResourceLoader resourceLoader = new DefaultResourceLoader();

    private Properties properties;

    public WorkflowPropertiesFactoryBean() {
        setLocation(resourceLoader.getResource("classpath:/workflow/workflow-default.properties"));
    }

    /**
     *
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        this.properties = mergeProperties();
    }

    /**
     *
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    @Override
    public Properties getObject() {
        return properties;
    }

    /**
     *
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    @Override
    public Class<?> getObjectType() {
        return Properties.class;
    }

}
