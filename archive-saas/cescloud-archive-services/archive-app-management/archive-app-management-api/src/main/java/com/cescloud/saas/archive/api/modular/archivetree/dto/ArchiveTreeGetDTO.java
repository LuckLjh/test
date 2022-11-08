/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.archivetree.dto</p>
 * <p>文件名:ArchiveTreeGetDTO.java</p>
 * <p>创建时间:2019年8月10日 上午11:51:40</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.archivetree.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年8月10日
 */
@ApiModel("档案树检索")
@Data
public class ArchiveTreeGetDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7575398807852719394L;

	@ApiModelProperty(value = "全宗号",required = true)
	private String fondsCode;

    /**
     * 树节点父节点ID，如果是树根节点，则为-1
     */
    @ApiModelProperty(value = "树节点父节点ID，如果是树根节点，则为-1",required = true)
    private Long parentId;

    /**
     * 档案树节点名称
     */
    @ApiModelProperty(value = "档案树节点名称（模糊查询）",required = true)
    private String treeName;

    /**
     * 档案树节点类型
     */
    @ApiModelProperty(value = "档案树节点类型：T：树根节点； C：分类节点；F：全宗节点；  A：档案类型节点； D：自定义节点规则；S：归档范围节点；",required = true)
    private String nodeType;

	/**
	 * 检索关键字 false
	 */
	@ApiModelProperty(value = "检索关键字",example = "检索关键字")
	private String keyword;


}
