/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.config</p>
 * <p>文件名:WorkflowConfiguration.java</p>
 * <p>创建时间:2019年10月14日 上午11:12:25</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.config;

import com.cescloud.saas.archive.service.modular.workflow.auth.SysCurrentUserHolder;
import com.cescloud.saas.archive.service.modular.workflow.connector.AuthUserConnector;
import com.cescloud.saas.archive.service.modular.workflow.listener.message.TaskApprovalMessageHumanTaskListener;
import com.cescloud.saas.archive.service.modular.workflow.support.WorkflowPropertiesFactoryBean;
import com.cesgroup.api.form.MockFormConnector;
import com.cesgroup.api.notification.DefaultNotificationConnector;
import com.cesgroup.api.org.MockOrgConnector;
import com.cesgroup.api.store.MockStoreConnector;
import com.cesgroup.api.tenant.MockTenantConnector;
import com.cesgroup.api.workcal.MockWorkCalendarConnector;
import com.cesgroup.bpm.CustomGroupEntityManager;
import com.cesgroup.bpm.CustomGroupEntityManagerFactory;
import com.cesgroup.bpm.behavior.CustomActivityBehaviorFactory;
import com.cesgroup.bpm.calendar.AdvancedBusinessCalendarManagerFactoryBean;
import com.cesgroup.bpm.image.CesProcessDiagramGenerator;
import com.cesgroup.bpm.listener.*;
import com.cesgroup.bpm.parser.CustomBpmnParser;
import com.cesgroup.bpm.parser.ExtensionUserTaskParseHandler;
import com.cesgroup.bpm.proxy.CesProcessEngineFactoryBean;
import com.cesgroup.bpm.support.ActivitiInternalProcessConnector;
import com.cesgroup.bpm.support.AutoDeployer;
import com.cesgroup.bpm.support.ProcessConnectorImpl;
import com.cesgroup.core.auth.CurrentUserHolder;
import com.cesgroup.core.id.SnowFlakeIdGenerator;
import com.cesgroup.core.jdbc.DatabaseType;
import com.cesgroup.core.jdbc.JdbcDao;
import com.cesgroup.core.spring.ApplicationContextHelper;
import com.cesgroup.core.spring.CesSpringProcessEngineConfiguration;
import com.cesgroup.core.spring.ProxyTaskScheduler;
import com.cesgroup.humantask.listener.*;
import com.cesgroup.humantask.support.HumanTaskConnectorImpl;
import com.cesgroup.humantask.support.TaskDefinitionConnectorImpl;
import com.cesgroup.internal.delegate.support.DelegateConnectorImpl;
import com.cesgroup.keyvalue.support.DatabaseKeyValueConnector;
import com.cesgroup.spi.store.LocalInternalStoreConnectorFactoryBean;
import com.cesgroup.workflow.expression.VoteHolder;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年10月14日
 */
@Configuration
@ComponentScan(basePackages = { "com.cesgroup.api.core.workflow", "com.cesgroup.workflow.expression.usertask",
    "com.cesgroup.**.manager", "com.cesgroup.**.service", "com.cesgroup.ws.workflow" }, lazyInit = true)
@EnableJpaRepositories({ "com.cesgroup.**.domain" })
@AutoConfigureAfter({ HibernateJpaAutoConfiguration.class, TransactionAutoConfiguration.class })
@EnableTransactionManagement
public class WorkflowConfiguration {

    private final DataSource dataSource;

    private final PlatformTransactionManager transactionManager;

    private final WorkflowProperties workflowProperties;

    private final ApplicationContext application;

    public WorkflowConfiguration(DataSource dataSource,
        PlatformTransactionManager transactionManager, WorkflowProperties workflowEngineProperties,
        ApplicationContext application) {
        this.dataSource = dataSource;
        this.transactionManager = transactionManager;
        this.workflowProperties = workflowEngineProperties;
        this.application = application;
    }

    @Bean
    WorkflowPropertiesFactoryBean workflowPropertiesFactoryBean() {
        final WorkflowPropertiesFactoryBean factory = new WorkflowPropertiesFactoryBean();
        return factory;
    }

    @Bean
    ApplicationContextHelper applicationContextHelper() {
        return new ApplicationContextHelper();
    }

    @Bean
    JdbcTemplate workflowJdbcTemplate() {
        return new JdbcTemplate(dataSource);
    }

    @Bean("JdbcDao")
    JdbcDao jdbcDao(@Qualifier("workflowJdbcTemplate") JdbcTemplate workflowJdbcTemplate) {
        final JdbcDao jdbcDao = new JdbcDao();
        jdbcDao.setJdbcTemplate(workflowJdbcTemplate);
        return jdbcDao;
    }

    @Bean
    SnowFlakeIdGenerator idGenerator(@Qualifier("workflowPropertiesFactoryBean") Properties properties) {
        final long nodeId = Long.valueOf(properties.getProperty("node.id", "0"));
        final SnowFlakeIdGenerator generator = new SnowFlakeIdGenerator(nodeId);
        return generator;
    }

    // 非基础依赖，全部Lazy加载

    /*@Bean
    @Lazy
    SessionFactory workflowSessionFactory(EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory.unwrap(SessionFactory.class);
    }*/

    // 会签任务监听器，增加会签结果对象到流程中
    @Bean
    @Lazy
    VoteTaskSetTaskParameterEventListener voteTaskSetTaskParameterEventListener() {
        return new VoteTaskSetTaskParameterEventListener();
    }

    // 会签完成条件的解析
    @Bean
    @Lazy
    VoteHolder voteHolder() {
        return new VoteHolder();
    }

    // 适配用户
    @Bean
    @Lazy
    CurrentUserHolder currentUserHolder() {
        return new SysCurrentUserHolder();
    }

    @Bean
    @Lazy
    LocalInternalStoreConnectorFactoryBean internalStoreConnector() {
        return new LocalInternalStoreConnectorFactoryBean();
    }

    @Bean
    @Lazy
    MockStoreConnector storeConnector() {
        return new MockStoreConnector();
    }

    @Bean
    @Lazy
    ProcessConnectorImpl processConnector() {
        return new ProcessConnectorImpl();
    }

    @Bean
    @Lazy
    DatabaseKeyValueConnector keyValueConnector() {
        return new DatabaseKeyValueConnector();
    }

    @Bean
    @Lazy
    MockOrgConnector orgConnector() {
        return new MockOrgConnector();
    }

    @Bean
    @Lazy
    MockWorkCalendarConnector workCalendarConnector() {
        return new MockWorkCalendarConnector();
    }

    @Bean
    @Lazy
    MockTenantConnector tenantConnector() {
        return new MockTenantConnector();
    }

    @Bean
    @Lazy
    MockFormConnector formConnector() {
        return new MockFormConnector();
    }

    @Bean
    @Lazy
    ActivitiInternalProcessConnector internalProcessConnector() {
        return new ActivitiInternalProcessConnector();
    }

    @Bean
    @Lazy
    TaskDefinitionConnectorImpl taskDefinitionConnector() {
        return new TaskDefinitionConnectorImpl();
    }

    @Bean
    @Lazy
    AuthUserConnector userConnector() {
        final AuthUserConnector connector = new AuthUserConnector();
        return connector;
    }

    @Bean
    @Lazy
    DefaultNotificationConnector notificationConnector() {
        return new DefaultNotificationConnector();
    }

    @Bean
    @Lazy
    DelegateConnectorImpl delegateConnector() {
        return new DelegateConnectorImpl();
    }

    @Bean
    @Lazy
    HumanTaskConnectorImpl humanTaskConnector() {
        final List<HumanTaskListener> listeners = new LinkedList<>();
        listeners.add(taskDefUserHumanTaskListener());
        listeners.add(taskConfUserHumanTaskListener());
        listeners.add(assigneeAliasHumanTaskListener());
        listeners.add(assignStrategyHumanTaskListener());
        listeners.add(assignUserHumanTaskListener());
        listeners.add(assignSubprocessUserHumanTaskListener());
        listeners.add(specifiedPriorityHumanTaskListener());
        listeners.add(delegateHumanTaskListener());
        listeners.add(deadlineHumanTaskListener());
        listeners.add(taskNotificationHumanTaskListener());
        listeners.add(taskCompleteCheckListener());
        listeners.add(voteCheckListener());
        listeners.add(taskApprovalMessageHumanTaskListener());
        final HumanTaskConnectorImpl impl = new HumanTaskConnectorImpl();
        impl.setHumanTaskListeners(listeners);
        return impl;
    }

    @Bean
    @Lazy
    TaskDefUserHumanTaskListener taskDefUserHumanTaskListener() {
        return new TaskDefUserHumanTaskListener();
    }

    @Bean
    @Lazy
    TaskConfUserHumanTaskListener taskConfUserHumanTaskListener() {
        return new TaskConfUserHumanTaskListener();
    }

    @Bean
    @Lazy
    AssigneeAliasHumanTaskListener assigneeAliasHumanTaskListener() {
        return new AssigneeAliasHumanTaskListener();
    }

    @Bean
    @Lazy
    AssignStrategyHumanTaskListener assignStrategyHumanTaskListener() {
        return new AssignStrategyHumanTaskListener();
    }

    @Bean
    @Lazy
    AssignUserHumanTaskListener assignUserHumanTaskListener() {
        return new AssignUserHumanTaskListener();
    }

    @Bean
    @Lazy
    AssignSubprocessUserHumanTaskListener assignSubprocessUserHumanTaskListener() {
        return new AssignSubprocessUserHumanTaskListener();
    }

    @Bean
    @Lazy
    SpecifiedPriorityHumanTaskListener specifiedPriorityHumanTaskListener() {
        return new SpecifiedPriorityHumanTaskListener();
    }

    @Bean
    @Lazy
    DelegateHumanTaskListener delegateHumanTaskListener() {
        return new DelegateHumanTaskListener();
    }

    @Bean
    @Lazy
    DeadlineHumanTaskListener deadlineHumanTaskListener() {
        return new DeadlineHumanTaskListener();
    }

    @Bean
    @Lazy
    TaskNotificationHumanTaskListener taskNotificationHumanTaskListener() {
        return new TaskNotificationHumanTaskListener();
    }

    @Bean
    @Lazy
    TaskCompleteCheckListener taskCompleteCheckListener() {
        return new TaskCompleteCheckListener();
    }

    @Bean
    @Lazy
    VoteCheckListener voteCheckListener() {
        return new VoteCheckListener();
    }

    @Bean
    @Lazy
    TaskApprovalMessageHumanTaskListener taskApprovalMessageHumanTaskListener() {
        return new TaskApprovalMessageHumanTaskListener();
    }

    @Bean
    @Lazy
    ProcessCompletedEventListener processEndEventListener() {
        return new ProcessCompletedEventListener();
    }

    @Bean
    @Lazy
    CesSpringProcessEngineConfiguration processEngineConfiguration(
        @Qualifier("workflowPropertiesFactoryBean") Properties properties, EntityManagerFactory entityManagerFactory) {
        final CesSpringProcessEngineConfiguration config = new CesSpringProcessEngineConfiguration();
        config.setDataSource(dataSource).setDatabaseSchemaUpdate(Boolean.FALSE.toString())
            .setDatabaseSchema("").setDatabaseTablePrefix("T_WF_")
            .setDbIdentityUsed(false).setJobExecutorActivate(false)
            .setHistory("audit");
        config.setTransactionManager(transactionManager);
        final Map<String, List<ActivitiEventListener>> typedListeners = new HashMap<>();
        typedListeners.put(ActivitiEventType.ENTITY_INITIALIZED.name(),
            Arrays.asList(updateProcessInstanceNameEventListener()));
        typedListeners.put(ActivitiEventType.PROCESS_COMPLETED.name(), Arrays.asList(processEndEventListener()));
        config.setTypedEventListeners(typedListeners);
        config.setCustomDefaultBpmnParseHandlers(
            Arrays.asList(extensionUserTaskParseHandler(), proxyUserTaskBpmnParseHandler()));
        config.setPostBpmnParseHandlers(Arrays.asList(postProxyUserTaskBpmnParseHandler()));
        config.setBpmnParser(customBpmnParser());
        if (workflowProperties.getDatabaseType() != null) {
            config.setDatabaseType(workflowProperties.getDatabaseType());
        } else {
            config.setDatabaseType(autoDetectDatabaseType(dataSource));
        }
        config.setActivityFontName(workflowProperties.getFontName());
        config.setLabelFontName(workflowProperties.getFontName());
        config.setCustomSessionFactories(Arrays.asList(customGroupEntityManagerFactory()));
        config.setBusinessCalendarManager(businessCalendarManager().getObject());
        config.setIdGenerator(idGenerator(properties));
        config.setEventListeners(Arrays.asList(functionEventListener()));
        config.setActivityBehaviorFactory(customActivityBehaviorFactory());
        config.setJpaCloseEntityManager(true);
        config.setJpaHandleTransaction(true);
        config.setJpaEntityManagerFactory(entityManagerFactory);
        config.setProcessDiagramGenerator(new CesProcessDiagramGenerator());
        return config;
    }

    /**
     * #oracle/dm database settings
     * #application.database.type=oracle
     * #sqlserver database settings
     * #application.database.type=mssql
     * #oscar/mysql database settings
     * #application.database.type=mysql
     * #kingbase/highgo database settings
     * #application.database.type=postgres
     */
    private String autoDetectDatabaseType(DataSource dataSource) {
        final String productName = getDatabaseProductName(dataSource);

        if (DatabaseType.ORACLE.equals(productName) || DatabaseType.DAMENG.equals(productName)) {
            return DatabaseType.Activity.ORACLE;
        }

        if (DatabaseType.SQLSERVER.equals(productName)) {
            return DatabaseType.Activity.SQLSERVER;
        }

        if (DatabaseType.MYSQL.equals(productName) || DatabaseType.OSCAR.equals(productName)) {
            return DatabaseType.Activity.MYSQL;
        }

        if (DatabaseType.POSTGRESQL.equals(productName) || DatabaseType.KINGBASE.equals(productName)
            || DatabaseType.HIGHGO.equals(productName)) {
            return DatabaseType.Activity.POSTGRES;
        }

        return null;
    }

    private String getDatabaseProductName(DataSource dataSource) {
        Connection con = null;
        try {
            con = dataSource.getConnection();
            final DatabaseMetaData metaData = con.getMetaData();
            return metaData.getURL().split(":")[1].toLowerCase();
        } catch (final Exception e) {
            // ignored
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (final SQLException e) {
                    // ignored
                }
            }
        }
        return null;
    }

    @Bean
    @Lazy
    CesProcessEngineFactoryBean processEngine(@Qualifier("workflowPropertiesFactoryBean") Properties properties,
        EntityManagerFactory entityManagerFactory) {
        final CesProcessEngineFactoryBean factory = new CesProcessEngineFactoryBean();
        factory.setProcessEngineConfiguration(processEngineConfiguration(properties, entityManagerFactory));
        factory.setEnabled(Boolean.valueOf(properties.getProperty("bpm.enabled", "true")));
        return factory;
    }

    @Bean
    @Lazy
    CustomBpmnParser customBpmnParser() {
        return new CustomBpmnParser();
    }

    @Bean
    @Lazy
    CustomGroupEntityManagerFactory customGroupEntityManagerFactory() {
        final CustomGroupEntityManagerFactory factory = new CustomGroupEntityManagerFactory();
        factory.setGroupEntityManager(customGroupEntityManager());
        return factory;
    }

    @Bean
    @Lazy
    CustomGroupEntityManager customGroupEntityManager() {
        return new CustomGroupEntityManager();
    }

    @Bean
    @Lazy
    UpdateProcessInstanceNameEventListener updateProcessInstanceNameEventListener() {
        return new UpdateProcessInstanceNameEventListener();
    }

    @Bean
    @Lazy
    ExtensionUserTaskParseHandler extensionUserTaskParseHandler() {
        return new ExtensionUserTaskParseHandler();
    }

    @Bean
    @Lazy
    ProxyUserTaskBpmnParseHandler proxyUserTaskBpmnParseHandler() {
        final ProxyUserTaskBpmnParseHandler handler = new ProxyUserTaskBpmnParseHandler();
        handler.setTaskListenerId("customTaskListener");
        handler.setUseDefaultUserTaskParser(true);
        return handler;
    }

    @Bean
    @Lazy
    ProxyUserTaskBpmnParseHandler postProxyUserTaskBpmnParseHandler() {
        final ProxyUserTaskBpmnParseHandler handler = new ProxyUserTaskBpmnParseHandler();
        handler.setTaskListenerId("postTaskListener");
        return handler;
    }

    @Bean
    @Lazy
    FunctionEventListener functionEventListener() {
        return new FunctionEventListener();
    }

    @Bean
    @Lazy
    CustomActivityBehaviorFactory customActivityBehaviorFactory() {
        return new CustomActivityBehaviorFactory();
    }

    @Bean
    @Lazy
    @DependsOn("applicationContextHelper")
    AutoDeployer autoDeployer(@Qualifier("workflowPropertiesFactoryBean") Properties properties,
        EntityManagerFactory entityManagerFactory) throws IOException {
        final AutoDeployer deployer = new AutoDeployer();
        deployer.setEnable(Boolean.valueOf(properties.getProperty("bpm.auto.deploy", "false")));
        deployer.setProcessEngine(processEngine(properties, entityManagerFactory).getObject());
        deployer.setDefaultTenantCode(properties.getProperty("tenant.default.code"));
        deployer.setTenantConnector(tenantConnector());
        deployer.setDeploymentResources(application.getResources("classpath*:/bpmn2/*"));
        return deployer;
    }

    @Bean
    @Lazy
    ProxyTaskListener customTaskListener() {
        final ProxyTaskListener listener = new ProxyTaskListener();
        listener.setTaskListeners(Arrays.asList(humanTaskTaskListener()));
        return listener;
    }

    @Bean
    @Lazy
    HumanTaskTaskListener humanTaskTaskListener() {
        return new HumanTaskTaskListener();
    }

    @Bean
    @Lazy
    ProxyTaskListener postTaskListener() {
        final ProxyTaskListener listener = new ProxyTaskListener();
        listener.setTaskListeners(Arrays.asList(confUserTaskListener(),autoCompleteTaskEventListener()));
        return listener;
    }

    @Bean
    @Lazy
    ConfUserTaskListener confUserTaskListener() {
        return new ConfUserTaskListener();
    }

	@Bean
	@Lazy
	AutoCompleteTaskEventListener autoCompleteTaskEventListener() {return new AutoCompleteTaskEventListener(); }

    @Bean
    @Lazy
    AdvancedBusinessCalendarManagerFactoryBean businessCalendarManager() {
        final AdvancedBusinessCalendarManagerFactoryBean factory = new AdvancedBusinessCalendarManagerFactoryBean();
        factory.setWorkCalendarConnector(workCalendarConnector());
        return factory;
    }

    @Bean
    @Lazy
    ProxyTaskScheduler targetScheduler(@Qualifier("workflowPropertiesFactoryBean") Properties properties) {
        final ProxyTaskScheduler scheduler = new ProxyTaskScheduler();
        final boolean enabled = Boolean.valueOf(properties.getProperty("scheduler.enabled", "true"));
        scheduler.setEnabled(enabled);
        scheduler.setProperties(properties);
        return scheduler;
    }

}
