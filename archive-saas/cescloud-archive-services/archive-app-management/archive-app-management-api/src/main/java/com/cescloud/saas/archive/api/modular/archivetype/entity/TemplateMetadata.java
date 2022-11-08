/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.archivetype.entity</p>
 * <p>文件名:TemplateMetadata.java</p>
 * <p>创建时间:2020年2月14日 下午4:31:39</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.archivetype.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年2月14日
 */
@ApiModel("模板字段")
@Data
@TableName("apma_template_metadata")
//@KeySequence("SEQ_APMA_TEMPLATE_METADATA")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TemplateMetadata extends Model<TemplateMetadata> {

    /**
     *
     */
    private static final long serialVersionUID = -1615562939798274722L;

    @ApiModelProperty("模板元数据id")
    @TableId
    private Long id;

    @ApiModelProperty("表模板ID")
    @NotNull(message = "表模板ID不能为空")
    private Long templateTableId;

    @ApiModelProperty("字段中文名称")
    @NotBlank(message = "字段中文名称不能为空")
    private String metadataChinese;

    @ApiModelProperty("字段英文名称")
    @NotBlank(message = "字段英文名称不能为空")
    private String metadataEnglish;

    @ApiModelProperty("元数据业务系统标识（1：系统字段，0：非系统字段）")
    private Integer metadataSys;

    //@ApiModelProperty("元数据英文隐藏名称")
    //private String metadataEnglishHidden;

    //@ApiModelProperty("元数据英文名称编号")
    //private Integer metadataEnglishNo;

    @ApiModelProperty("元数据类型")
    @NotBlank(message = "字段类型不能为空")
    private String metadataType;

    @ApiModelProperty("元数据字段长度")
    @NotNull(message = "字段长度不能为空")
    private Integer metadataLength;

    @ApiModelProperty("元数据的小数位数")
    private Integer metadataDotLength;

    @ApiModelProperty("元数据是否为空")
    private Integer metadataNull;

    @ApiModelProperty("元数据中文描述信息")
    private String chineseDescription;

    @ApiModelProperty("元数据英文描述信息")
    private String englishDescription;

    @ApiModelProperty("元数据备注")
    private String remark;

    @ApiModelProperty("绑定的数据字典code")
    private String dictCode;

    @ApiModelProperty("元数据标签编码")
    private String tagEnglish;

    @ApiModelProperty("元数据默认值")
    private String metadataDefaultValue;

    @ApiModelProperty("元数据类别（0：系统字段，1：业务字段）")
    private Integer metadataClass;

    @ApiModelProperty("是否列表显示")
    private Integer isList;

    @ApiModelProperty("是否参与编辑")
    private Integer isEdit;

    @ApiModelProperty("是否参与检索")
    private Integer isSearch;

    @ApiModelProperty("是否重复字段")
    private Integer isRepeat;

    @ApiModelProperty("排序号")
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

    public void setMetadataEnglish(String metadataEnglish) {
        if (null == metadataEnglish) {
            return;
        }
        this.metadataEnglish = metadataEnglish.toLowerCase();
    }

    public String getMetadataEnglish() {
        if (null != metadataEnglish) {
            metadataEnglish = metadataEnglish.toLowerCase();
        }
        return metadataEnglish;
    }

}
