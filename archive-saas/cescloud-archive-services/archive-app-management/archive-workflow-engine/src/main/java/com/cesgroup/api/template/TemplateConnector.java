package com.cesgroup.api.template;

import java.util.List;

/**
 * 模板连接器
 * 
 * @author 国栋
 *
 */
public interface TemplateConnector {

    /**
     * 通过主键码查找模板
     * 
     * @param code
     *            模板编码
     * @param tenantId
     *            租户id
     * @return 模板对象dto
     */
    TemplateDTO findByCode(String code, String tenantId);

    /**
     * 根据租户id查找所有模板dto
     * 
     * @param tenantId 租户ID
     * @return list
     */
    List<TemplateDTO> findAll(String tenantId);
}
