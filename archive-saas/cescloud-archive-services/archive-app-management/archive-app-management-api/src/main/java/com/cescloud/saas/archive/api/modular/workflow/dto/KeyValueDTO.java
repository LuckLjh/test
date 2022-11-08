/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.workflow.dto</p>
 * <p>文件名:KeyValueDTO.java</p>
 * <p>创建时间:2019年12月6日 上午11:16:47</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.workflow.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年12月6日
 */
@ApiModel("id/parentId/name对象")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KeyValueDTO<KeyType, ValType> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -2930074502230580510L;

    private KeyType id;

    private KeyType parentId;

    private ValType name;

}
