package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.cescloud.saas.archive.service.modular.common.core.constant.ArchiveFieldConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.enums.ColumnComputeRuleEnum;
import com.cescloud.saas.archive.service.modular.common.tableoperation.constants.MetadataTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 字段计算规则
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ColumnComputeRuleDTO implements Serializable {

	private static final long serialVersionUID = 4294436699326787887L;

	private String metadataEnglish;
	private Integer relationType;
	private String method;
	private String column;
	private String from;
	private String where;
	private String group;
	private Integer flagZero;
	private Integer length;
	private String metadataType;
	private String storageLocate;

	/**
	 * 计算数值的SQL
	 *
	 * @return
	 */
	public String getSql(DbType dbType) {
		StringBuffer sql = new StringBuffer();
		String datimeF = "";
		if(MetadataTypeEnum.DATE.getValue().equals(this.metadataType)){
			datimeF = ",'yyyy-MM-dd'";
		}else if(MetadataTypeEnum.DATETIME.getValue().equals(this.metadataType)){
			datimeF = ",'yyyy-MM-dd HH:mm:ss'";
		}
		//起始值特殊处理
		if (!ColumnComputeRuleEnum.START_END.getValue().equals(this.relationType)) {
			sql.append("select ");
			if (dbType.equals(DbType.ORACLE) || dbType.equals(DbType.ORACLE_12C)) {//orcle的日期需要特殊处理
				if (StrUtil.isBlank(this.method)) {
					if (this.relationType.equals(ColumnComputeRuleEnum.EQUAL.getValue())) {
						sql.append("to_char(").append("t.").append(this.column).append(datimeF+")");
					} else if (this.relationType.equals(ColumnComputeRuleEnum.AUTO_SPLICING.getValue())) {
						sql.append("to_char(").append(this.column).append(datimeF+")");
					}
				} else {
					sql.append(this.method).append("(");
					//如果是计数，则count(id)
					if (ColumnComputeRuleEnum.COUNT.getValue().equals(this.relationType)) {
						//sql.append("t.").append(ArchiveConstants.PK);
						sql.append("*");
					} else {
						sql.append("to_char(").append("t.").append(this.column).append(datimeF+")");
					}
					sql.append(")");
				}
			}else {
				if (StrUtil.isBlank(this.method)) {
					if (this.relationType.equals(ColumnComputeRuleEnum.EQUAL.getValue())) {
						sql.append("t.").append(this.column);
					} else if (this.relationType.equals(ColumnComputeRuleEnum.AUTO_SPLICING.getValue())) {
						sql.append(this.column);
					}
				} //保管期限、密级不能用 max(永久、长期、短期)函数比较，需要用值来转换
				else if (ColumnComputeRuleEnum.MAX_VALUE.getMethod().equals(this.method) && changeMaxValue(this.column)) {
					sql.append("DISTINCT").append("(");
					//如果是计数，则count(id)
					if (ColumnComputeRuleEnum.COUNT.getValue().equals(this.relationType)) {
						//sql.append("t.").append(ArchiveConstants.PK);
						sql.append("*");
					} else {
						sql.append("t.").append(this.column);
					}
					sql.append(")");
				} else {
					sql.append(this.method).append("(");
					//如果是计数，则count(id)
					if (ColumnComputeRuleEnum.COUNT.getValue().equals(this.relationType)) {
						//sql.append("t.").append(ArchiveConstants.PK);
						sql.append("*");
					} else {
						sql.append("t.").append(this.column);
					}
					sql.append(")");
				}
			}
			//sharding jdbc获取时候需要一个别名
			sql.append(" as \"DD\"");
			sql.append(" from ").append(this.from);
			if (StrUtil.isNotBlank(this.where)) {
				sql.append(" where ").append(this.where);
			}
			if (StrUtil.isNotBlank(this.group)) {
				sql.append(" group by ").append(this.group);
			}
		} else {
			//起始值特殊处理(求起止值是将最小值和最大值用~拼接起立放到规则字段上)
			if (dbType.equals(DbType.ORACLE) || dbType.equals(DbType.ORACLE_12C)) {//orcle的日期需要特殊处理
				sql.append("select ").append(" min(").append("to_char(").append("t.").append(this.column).append(datimeF+")").append(") as \"MIN\" ");
				sql.append(" , max(").append("to_char(").append("t.").append(this.column).append(datimeF+")").append(") as \"MAX\" ");
				sql.append(" from ").append(this.from);
			}else{
				sql.append("select ").append(" min(").append("t.").append(this.column).append(") as \"MIN\" ");
				sql.append(" , max(t.").append(this.column).append(") as \"MAX\" ");
				sql.append(" from ").append(this.from);
			}
			if (StrUtil.isNotBlank(this.where)) {
				sql.append(" where ").append(this.where);
			}
			if (StrUtil.isNotBlank(this.group)) {
				sql.append(" group by ").append(this.group);
			}
		}
		return sql.toString();
	}

	/**
	 * 保管期限、密级不能用 max(永久、长期、短期)函数比较，需要用值来转换
	 * @param column 字段名
	 * @return 是否需要转换
	 */
	public Boolean changeMaxValue(String column) {
		return ArchiveFieldConstants.RETENTION_PERIOD.equals(column) || ArchiveFieldConstants.SECURITY_CLASSIFICATION.equals(column);
	}
}
