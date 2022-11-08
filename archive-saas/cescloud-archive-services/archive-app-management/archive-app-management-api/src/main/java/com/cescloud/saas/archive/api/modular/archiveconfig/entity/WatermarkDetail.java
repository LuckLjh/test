package com.cescloud.saas.archive.api.modular.archiveconfig.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@TableName("apma_watermark_detail")
//@KeySequence("SEQ_APMA_WATERMARK_DETAIL")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WatermarkDetail extends Model<Watermark> {
	private static final long serialVersionUID = 1L;

	/**
	 * 列表数据id,主键 false
	 */
	@ApiModelProperty("水印数据id,主键")
	@TableId
	private Long id;

	@ApiModelProperty("水印id")
	private Long watermarkId;

	@ApiModelProperty("水印类型（固定文字unchanged、动态文字changed、图片picture、二维码code）")
    private String watermarkType;

	@ApiModelProperty("二维码的配置信息字段")
	private String QRCodeConfiguration;

	@ApiModelProperty("水印路径（在minio中的存储路径）")
	private String watermarkUrl;

	@ApiModelProperty("水印图片的id(用于获取浏览url地址)")
	private Long watermarkPathId;

	@ApiModelProperty("是否取当前人所在部门的签盖章")
	private Boolean currentUserDeptSeal;

	@ApiModelProperty("文件名称")
	private String watermarkFileName;

	@ApiModelProperty("水印文字内容")
    private String watermarkTxt;

	@ApiModelProperty("水印浏览")
	private Boolean watermarkBrowse;

	@ApiModelProperty("水印下载")
	private Boolean watermarkDownload;

	@ApiModelProperty("水印打印")
	private Boolean watermarkPrint;

	@ApiModelProperty("水印文字配置")
	private String watermarkTxtConfiguration;

	@ApiModelProperty("水印文字配置(不予数据库交互存放水印list信息用于前台后交互)")
	@TableField(exist = false)
	private List<Map<String,Object>> watermarkTxtConfigurationList;

	@ApiModelProperty("水印起始位置x轴坐标")
    private Integer watermarkX;

	@ApiModelProperty("水印起始位置y轴坐标")
    private Integer watermarkY;

	@ApiModelProperty("重复行数")
    private Integer watermarkRows;

	@ApiModelProperty("重复列数")
    private Integer watermarkCols;

	@ApiModelProperty("水印x轴间隔")
    private Integer watermarkXSpace;

	@ApiModelProperty("水印y轴间隔")
    private Integer watermarkYSpace;

	@ApiModelProperty("水印透明度")
    private float watermarkAlpha;

	@ApiModelProperty("水印宽度")
    private Integer watermarkWidth;

	@ApiModelProperty("水印高度")
    private Integer watermarkHeight;

	@ApiModelProperty("水印倾斜度数")
    private Integer watermarkAngle;

	@ApiModelProperty("奇偶行错开距离")
    private Integer watermarkCross;

	@ApiModelProperty("字体大小")
    private Integer watermarkFontsize;

	@ApiModelProperty("字体")
    private String watermarkFont;

	@ApiModelProperty("是否斜体")
    private Boolean watermarkFontItalic;

	@ApiModelProperty("是否下划线")
    private Boolean watermarkFontUnderline;

	@ApiModelProperty("是否加粗")
    private Boolean watermarkFontStrong;

	@ApiModelProperty("字体颜色")
    private String watermarkColor;

	@ApiModelProperty("重复位置，全文 all 首页 firist 尾页 end 指定页 custom")
    private String watermarkPlace;

	@ApiModelProperty("指定页位置")
    private Integer watermarkPlaceCustom;

	@ApiModelProperty("是否动态拉升水印")
	private Boolean watermarkDynamicSize;

	@ApiModelProperty("排序")
    private Integer sortNo;

	@ApiModelProperty("所属租户id")
	@TableField(fill = FieldFill.INSERT)
	private Long tenantId;

	/**
	 * 乐观锁,数据版本号
	 */
	@ApiModelProperty("乐观锁")
	@TableField(fill = FieldFill.INSERT)
	@Version
	private Long revision;

	/**
	 * 创建人
	 */
	@ApiModelProperty("创建人")
	@TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
	private Long createdBy;

	/**
	 * 创建时间
	 */
	@ApiModelProperty("创建时间")
	@TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
	private LocalDateTime createdTime;

	/**
	 * 更新人
	 */
	@ApiModelProperty("更新人")
	@TableField(fill = FieldFill.UPDATE)
	private Long updatedBy;

	/**
	 * 更新时间
	 */
	@ApiModelProperty("更新时间")
	@TableField(fill = FieldFill.UPDATE)
	private LocalDateTime updatedTime;

}