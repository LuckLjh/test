/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.workflow.dto</p>
 * <p>文件名:StartBusinessProcessDTO.java</p>
 * <p>创建时间:2019年12月5日 下午2:38:57</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.workflow.dto;

import com.cescloud.saas.archive.api.modular.user.entity.SysUser;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author qianjiang
 * @version 1.0.0 2021年9月13日
 */
@ApiModel("人员选择返回")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JudgePersonResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "显示用户")
    private Boolean showPerson;

    @ApiModelProperty(value = "节点配置信息")
    private List<SysUser> personList = new ArrayList<>();

}
