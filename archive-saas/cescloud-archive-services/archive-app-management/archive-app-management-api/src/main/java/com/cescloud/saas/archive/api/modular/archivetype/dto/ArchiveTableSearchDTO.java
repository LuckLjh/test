/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.archivetype.dto</p>
 * <p>文件名:ArchiveTableSearchDTO.java</p>
 * <p>创建时间:2020年3月26日 下午6:09:01</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.archivetype.dto;

import java.io.Serializable;
import java.util.Collection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年3月26日
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArchiveTableSearchDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 4000619081525573705L;

    private String tableName;

    /**
     * [id, tm as title_proper, yd as year_code, ...]
     */
    private Collection<String> fields;

}
