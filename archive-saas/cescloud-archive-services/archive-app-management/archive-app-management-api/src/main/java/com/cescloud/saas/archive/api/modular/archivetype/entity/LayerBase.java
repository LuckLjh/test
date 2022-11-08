/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.archivetype.entity</p>
 * <p>文件名:LayerBase.java</p>
 * <p>创建时间:2020年2月14日 上午10:57:00</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.archivetype.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年2月14日
 */
@ApiModel("基础层级")
@Data
@TableName("apma_layer_base")
//@KeySequence("SEQ_APMA_LAYER_BASE")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LayerBase extends Model<LayerBase> {

    /**
     *
     */
    private static final long serialVersionUID = 867185570278807695L;

    @ApiModelProperty("层级ID")
    @TableId
    private Long id;

    @ApiModelProperty("层级编码（大写）")
    private String code;

    @ApiModelProperty("层级名称")
    private String name;

    @ApiModelProperty("层级备注")
    private String remark;

    @ApiModelProperty("优先级")
    private Integer priority;

    @ApiModelProperty("排序字段")
    private Integer sortNo;

    @ApiModelProperty("乐观锁")
    @TableField(fill = FieldFill.INSERT)
    @Version
    private Long revision;

    @ApiModelProperty("创建人")
    @TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
    private Long createdBy;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
    private LocalDateTime createdTime;

    @ApiModelProperty("更新人")
    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedTime;

    public String getCode() {
        if (null != this.code) {
            this.code = this.code.toUpperCase();
        }
        return this.code;
    }
}
