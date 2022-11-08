/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cesgroup.api.humantask</p>
 * <p>文件名:RollbackActivityDTO.java</p>
 * <p>创建时间:2020年1月10日 下午1:38:48</p>
 * <p>作者:qiucs</p>
 */

package com.cesgroup.api.humantask;

import java.io.Serializable;
import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年1月10日
 */
@Data
@Builder
public class RollbackActivityDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2158865911890113964L;

    private String id;

    private String name;

    private String catalog;

    private List<String> assigneeNameList;
}
