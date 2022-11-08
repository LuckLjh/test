package com.cescloud.saas.archive.service;

import com.cescloud.saas.archive.service.modular.common.dbupgrades.annotation.EnableAutoDbUpgrades;
import com.cescloud.saas.archive.service.modular.common.security.annotation.EnableCesCloudFeignClients;
import com.cescloud.saas.archive.service.modular.common.security.annotation.EnableCesCloudResourceServer;
import com.cescloud.saas.archive.service.modular.common.swagger.annotation.EnableCesCloudSwagger2;
import org.apache.rocketmq.client.log.ClientLogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.SpringCloudApplication;

/**
 * @Package: com.cescloud.saas.archive.appmanagement.service
 * @Classname ArchiveAppManagementApplication
 * @Description TODO
 * @Date 2019-03-06 19:18
 * @Created by zhangpeng
 */
@EnableCesCloudSwagger2
@SpringCloudApplication
@EnableCesCloudFeignClients
@EnableCesCloudResourceServer
@EnableAutoDbUpgrades
public class ArchiveAppManagementApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(ArchiveAppManagementApplication.class);
	}

	public static void main(String[] args) {
		//rocketmq.client.logUseSlf4j在配置文件中配置没有效果,只能在这里配置
		System.setProperty(ClientLogger.CLIENT_LOG_USESLF4J,"true");
		SpringApplication.run(ArchiveAppManagementApplication.class,args);
	}
}
