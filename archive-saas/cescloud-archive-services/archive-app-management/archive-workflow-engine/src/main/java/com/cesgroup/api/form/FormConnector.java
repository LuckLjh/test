package com.cesgroup.api.form;

import java.util.List;

/**
 * 表单连接器
 * 
 * @author 国栋
 *
 */
public interface FormConnector {

    /**
     * 根据租户id获取所有表单dto
     * 
     * @param tenantId
     *            租户id
     * @return 表单对象的数组
     */
    List<FormDTO> getAll(String tenantId);

    /**
     * 根据code获取表单对象
     * 
     * @param code
     *            表单编码
     * @param tenantId
     *            租户id
     * @return 表单对象
     */
    FormDTO findForm(String code, String tenantId);
}
