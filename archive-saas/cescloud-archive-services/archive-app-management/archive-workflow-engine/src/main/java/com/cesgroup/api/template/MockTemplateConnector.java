package com.cesgroup.api.template;

import java.util.List;

/**
 * 模板连接器默认实现
 * 
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public class MockTemplateConnector implements TemplateConnector {

    @Override
    public TemplateDTO findByCode(String code, String tenantId) {
        return null;
    }

    @Override
    public List<TemplateDTO> findAll(String tenantId) {
        return null;
    }
}
