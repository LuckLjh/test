/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.config</p>
 * <p>文件名:WorkflowProperties.java</p>
 * <p>创建时间:2019年10月14日 下午1:53:21</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年10月14日
 */
@Data
@ConfigurationProperties(prefix = "archive.workflow")
public class WorkflowProperties {

    private String databaseType = null;

    private String fontName = "宋体";

    private String dialect = null;

    private boolean showSql = true;

    private boolean formatSql = false;

}
