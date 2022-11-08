package com.cesgroup.api.form;

import java.util.List;

/**
 * 模拟表单连接器
 * 
 * @author 国栋
 *
 */
public class MockFormConnector implements FormConnector {

    @Override
    public List<FormDTO> getAll(String tenantId) {
        return null;
    }

    @Override
    public FormDTO findForm(String code, String tenantId) {
        return null;
    }
}
