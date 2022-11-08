package com.cescloud.saas.archive.api.modular.synonymy.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 同义词
 *
 * @author liwei
 * @date 2019-04-09 13:03:31
 */
@Data
@TableName("apma_synonymy")
//@KeySequence("SEQ_APMA_SYNONYMY")
@EqualsAndHashCode(callSuper = true)
@ApiModel("同义词或同义词组实体")
public class Synonymy extends Model<Synonymy> {


	private static final long serialVersionUID = -104342836082737289L;
	/**
     * 同义词id,主键 false
     */
  @TableId
  @ApiModelProperty("同义词或同义词组主键")
  private Long id;
      /**
     * 同义词组 false
     */
  @ApiModelProperty("同义词组名称")
  private String synonymyGroup;
      /**
     * 同义词 false
     */
  @ApiModelProperty("同义词名称")
  private String synonymyWord;
	/**
	 * 父节点Id
	 */
	private Long parentId;
      /**
     * 所属租户id true
     */
  @TableField(fill = FieldFill.INSERT)
  @ApiModelProperty("租户ID")
  private Long tenantId;
      /**
     * 乐观锁 true
     */
  @TableField(fill = FieldFill.INSERT)
  @Version
  @ApiModelProperty("乐观锁标识")
  private Long revision;
      /**
     * 创建人 true
     */
  @TableField(fill = FieldFill.INSERT)
  private Long createdBy;
      /**
     * 创建时间 true
     */
  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdTime;
      /**
     * 更新人 true
     */
  @TableField(fill = FieldFill.UPDATE)
  private Long updatedBy;
      /**
     * 更新时间 true
     */
  @TableField(fill = FieldFill.UPDATE)
  private LocalDateTime updatedTime;
  
}
