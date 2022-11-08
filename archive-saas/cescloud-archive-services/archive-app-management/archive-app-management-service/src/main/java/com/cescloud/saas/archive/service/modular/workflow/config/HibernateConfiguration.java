/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.config</p>
 * <p>文件名:HibernateConfiguration.java</p>
 * <p>创建时间:2019年10月17日 上午11:22:02</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.Database;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年10月17日
 */
@Configuration
@EnableConfigurationProperties({ WorkflowProperties.class })
@AutoConfigureAfter({ DataSourceAutoConfiguration.class })
@Slf4j
public class HibernateConfiguration {

    private final Map<String, Database> databaseMap;

    private final Map<String, String> dialectMap;

    public HibernateConfiguration() {

    	/**
		 * 数据库适配
		 * */
        databaseMap = new HashMap<String, Database>();
        databaseMap.put("oracle", Database.ORACLE);
        databaseMap.put("sqlserver", Database.SQL_SERVER);
        databaseMap.put("mysql", Database.MYSQL);
        databaseMap.put("dm", Database.ORACLE);
        databaseMap.put("oscar", Database.MYSQL);
        databaseMap.put("kingbase", Database.POSTGRESQL);
		databaseMap.put("kingbase8", Database.POSTGRESQL);
		databaseMap.put("KingbaseES", Database.POSTGRESQL);
		databaseMap.put("postgresql", Database.POSTGRESQL);

        dialectMap = new HashMap<String, String>();
        dialectMap.put("oracle", "com.cesgroup.core.util.MyOracleDialet");
        dialectMap.put("sqlserver", "org.hibernate.dialect.SQLServerDialect");
        dialectMap.put("mysql", "org.hibernate.dialect.MySQLDialect");
        dialectMap.put("dm", "org.hibernate.dialect.DmDialect");
        dialectMap.put("oscar", "org.hibernate.dialect.OscarDialect");
        dialectMap.put("kingbase", "org.hibernate.dialect.Kingbase8Dialect");
		dialectMap.put("kingbase8", "org.hibernate.dialect.Kingbase8Dialect");
		dialectMap.put("KingbaseES", "org.hibernate.dialect.Kingbase8Dialect");
		dialectMap.put("postgresql", "org.hibernate.dialect.PostgreSQL95Dialect");

    }

    private String getConnectionUrl(DataSource dataSource) {
        String url = null;

        try (Connection conn = dataSource.getConnection()) {
            url = conn.getMetaData().getURL();
        } catch (final SQLException e) {
            throw new HibernateException("获取数据库连接地址出错", e);
        }

        return url;
    }

    /*@Bean
    LocalSessionFactoryBean workflowSessionFactoryBean(DataSource dataSource,
        WorkflowProperties workflowEngineProperties) {
        final LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPackagesToScan(new String[] { "com.cesgroup.**.domain" });
        sessionFactory.setHibernateProperties(hibernateProperties(dataSource, workflowEngineProperties));
        return sessionFactory;
    }

    //获取hibernate配置
    private Properties hibernateProperties(DataSource dataSource, WorkflowProperties workflowEngineProperties) {
        final Properties properties = new Properties();
        if (null == workflowEngineProperties.getDialect()) {
            properties.put("hibernate.dialect", autoDetechDialect(getConnectionUrl(dataSource)));
        } else {
            properties.put("hibernate.dialect", workflowEngineProperties.getDialect());
        }
        properties.put("hibernate.show_sql", workflowEngineProperties.isShowSql());
        properties.put("hibernate.format_sql", workflowEngineProperties.isFormatSql());
        return properties;
    }

    private String autoDetechDialect(String url) {
        final String[] urlArr = url.split(":");
        final String databaseType = urlArr[1].toLowerCase();
        final String dialect = dialectMap.get(databaseType);
        if (null == dialect) {
            final String msg = "特殊数据库[" + databaseType + "]方言没有配置，请到配置中心配置archive.workflow.dialect属性";
            log.error(msg);
            throw new HibernateException(msg);
        }
        return dialect;
    }*/

    @Bean
    LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource,
        JpaVendorAdapter jpaVendorAdapter,
        JpaProperties jpaProperties, ObjectProvider<PersistenceUnitManager> persistenceUnitManager,
        HibernateProperties hibernateProperties) {

        autoDetechDialect(jpaProperties, getConnectionUrl(dataSource));

        final EntityManagerFactoryBuilder builder = new EntityManagerFactoryBuilder(
            jpaVendorAdapter, jpaProperties.getProperties(),
            persistenceUnitManager.getIfAvailable());

        final Map<String, Object> vendorProperties = getVendorProperties(jpaProperties, hibernateProperties);

        return builder.dataSource(dataSource).packages(new String[] { "com.cesgroup.**.domain" })
            .persistenceUnit("workflowPersistenceUnit")
            .properties(vendorProperties).build();
    }

    private Map<String, Object> getVendorProperties(JpaProperties jpaProperties,
        HibernateProperties hibernateProperties) {
        final Map<String, Object> vendorProperties = new LinkedHashMap<>(
            hibernateProperties.determineHibernateProperties(jpaProperties.getProperties(),
                new HibernateSettings()));
        return vendorProperties;
    }

    private void autoDetechDialect(JpaProperties jpaProperties, String url) {
        final String[] urlArr = url.split(":");
        final String databaseType = urlArr[1].toLowerCase();
        //
        if (!databaseMap.containsKey(databaseType)) {
            if (log.isDebugEnabled()) {
                log.debug("使用JPA默认方言设置");
            }
            return;
        }
        jpaProperties.setDatabase(databaseMap.get(databaseType));
        jpaProperties.setDatabasePlatform(dialectMap.get(databaseType));
		jpaProperties.getProperties().put("hibernate.dialect", dialectMap.get(databaseType));
    }

}
