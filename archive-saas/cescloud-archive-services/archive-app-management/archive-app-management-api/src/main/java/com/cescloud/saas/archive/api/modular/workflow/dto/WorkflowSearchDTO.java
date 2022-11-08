/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.workflow.dto</p>
 * <p>文件名:WorkflowSearchDTO.java</p>
 * <p>创建时间:2019年12月13日 上午9:46:09</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.workflow.dto;

import cn.hutool.core.date.DatePattern;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年12月13日
 */
@ApiModel(value = "我的协同查询条件")
@Data
public class WorkflowSearchDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5894267382008755481L;

    @ApiModelProperty(value = "业务编码", example = "1")
    private String businessCode;

	@ApiModelProperty(value = "流程名称", example = "1")
	private String workflowName;

    @ApiModelProperty(value = "状态", hidden = true)
    private String status;

    @ApiModelProperty(value = "发起开始时间", example = "2019-01-01")
    @DateTimeFormat(pattern = DatePattern.NORM_DATE_PATTERN)
    private Date startTime;

    @ApiModelProperty(value = "发起结束时间", example = "2019-12-12")
    @DateTimeFormat(pattern = DatePattern.NORM_DATE_PATTERN)
    private Date endTime;

    public Date getEndTime() {
        if (null != endTime) {
            endTime.setHours(23);
            endTime.setMinutes(59);
            endTime.setSeconds(59);
        }
        return endTime;
    }

}
