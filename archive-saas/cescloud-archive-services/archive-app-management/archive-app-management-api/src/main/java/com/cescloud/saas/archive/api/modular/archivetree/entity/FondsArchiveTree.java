package com.cescloud.saas.archive.api.modular.archivetree.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 全宗绑定档案树
 *
 * @author qiucs
 * @version 1.0.0 2019年5月13日
 */
@Data
@TableName("apma_fonds_archive_tree")
//@KeySequence("SEQ_APMA_FONDS_ARCHIVE_TREE")
@EqualsAndHashCode(callSuper = true)
@ApiModel("全宗绑定档案树")
public class FondsArchiveTree extends Model<FondsArchiveTree> {

    private static final long serialVersionUID = 1L;

    /**
     * 档案树节点id false
     */
    @TableId
    @ApiModelProperty(name = "全宗绑定档案树id", hidden = true)
    private Long id;

    /**
     * 全宗号
     */
    @ApiModelProperty(name = "全宗号", required = true)
    private String fondsCode;

    /**
     * 档案树ID
     */
    @ApiModelProperty(name = "档案树ID", required = true)
    private Long archiveTreeId;

    /**
     * 所属租户id true
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(name = "所属租户id", hidden = true)
    private Long tenantId;

    /**
     * 乐观锁 true
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(name = "乐观锁", hidden = true)
    private Long revision;

    /**
     * 创建人 true
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(name = "创建人", hidden = true)
    private Long createdBy;

    /**
     * 创建时间 true
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(name = "创建时间", hidden = true)
    private LocalDateTime createdTime;

    /**
     * 更新人 true
     */
    @TableField(fill = FieldFill.UPDATE)
    @ApiModelProperty(name = "更新人", hidden = true)
    private Long updatedBy;

    /**
     * 更新时间 true
     */
    @TableField(fill = FieldFill.UPDATE)
    @ApiModelProperty(name = "更新时间", hidden = true)
    private LocalDateTime updatedTime;

}
