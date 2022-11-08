package com.cesgroup.api.keyvalue;

import com.cesgroup.core.page.Page;

import java.util.List;
import java.util.Map;

/**
 * 外部数据使用key value形式进行获取与存储，忽略类型，统一抽象成record对象
 * 
 * @author 王国栋
 *
 */
public interface KeyValueConnector {

    /**
     * 根据code查询数据. 认为code是业务主键.
     */
    Record findByCode(String code);

    /**
     * 根据ref查询数据. 认为ref是流程实例id.
     */
    Record findByRef(String ref);

    /**
     * 保存数据.
     */
    void save(Record record);

    /**
     * 根据code删除数据.
     */
    void removeByCode(String code);

    /**
     * 根据code批量删除数据.
     */
    void removeByCodes(List<String> codes);

    /**
     * 查询对应状态的，某人发起的数据，主要用来查询草稿.
     */
    List<Record> findByStatus(int status, String userId, String tenantId);

    /**
     * 分页查询.
     */
    Page pagedQuery(Page page, int status, String userId, String tenantId);

    /**
     * 分页查询.
     */
    Page pagedQuery(Page page, int status, String userId, String tenantId, String category);

    /**
     * 草稿分页查询
     * 
     * @param page
     *            分页对象
     * @param status
     *            状态
     * @param userId
     *            用户ID
     * @param tenantId
     *            租户ID
     * @return page
     */
    Page draftPagedQuery(Page page, int status, String userId, String tenantId);

    /**
     * 查询总数.
     */
    long findTotalCount(String category, String q, String tenantId);

    /**
     * 分页查询数据.
     */
    List<Map<String, Object>> findResult(Page page, String category, String tenantId,
        Map<String, String> headers, String q);

    /**
     * 复制数据.
     */
    Record copyRecord(Record original, List<String> fields);

    /**
     * 根据用户查询草稿总数
     * 
     * @param userId
     *            用户ID
     * @param status
     *            状态
     * @param tenantId
     *            租户ID
     * @return long
     */
    long findToalCountByUserId(String userId, int status, String tenantId);

    /**
     * 分页查询草稿箱数据（包括业务字段）
     * 
     * @param page
     *            分页对象
     * @param status
     *            0表示草稿数据，2表示流程数据
     * @param userId
     *            用户登录名
     * @param tenantId
     *            租户id
     * @param processDefinitionKey
     *            流程定义code
     * @return page
     */
    Page pageQueryWithBusinessData(Page page, int status, String userId, String tenantId,
        String processDefinitionKey);
}
