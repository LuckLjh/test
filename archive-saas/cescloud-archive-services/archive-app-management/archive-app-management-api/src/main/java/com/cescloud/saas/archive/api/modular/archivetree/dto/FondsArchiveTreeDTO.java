/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.archivetree.dto</p>
 * <p>文件名:FondsArchiveTree.java</p>
 * <p>创建时间:2019年5月13日 下午3:03:43</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.archivetree.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * @author qiucs
 * @version 1.0.0 2019年5月13日
 */
@Data
public class FondsArchiveTreeDTO {
    
    /**
     * 档案树ID
     */
    @ApiModelProperty(value = "档案树ID",required = true)
    private Long archiveTreeId;
    
    /**
     * 全宗号
     */
    @ApiModelProperty(value = "全宗号")
    private String[] fondsCodes;

}
