package com.cescloud.saas.archive.api.modular.fwimp.constant;

public class OaImpConstant {
	/**
	 * 流程状态 ：
	 * 未激活：0，
	 * 已激活：1，
	 * 已停止：2
	 */
	public final static int activate = 1;
	/**
	 * 流程状态 ：
	 * 未激活：0，
	 * 已激活：1，
	 * 已停止：2
	 */
	public final static int disActivate = 2;
	// oa 日志导入成功
	public final static int  impSuccess = 1;
	// oa 日志导入失败
	public final static int  impFail = 2;
	// oa 日志导入成功拼接路径
	public final static String  successsPath = "success";
	// oa 日志导入失败拼接路径
	public final static String  failPath = "fail";
	//导入同名html
	public final static int impHtml = 0;
	//不导入同名html
	public final static int unImpHtml = 1;

	//doc  表级别
	public final static String docType = "D";

	//info 过程信息表级别
	public final static String infoType = "I";

	//是否查询到重复的档案条目 查询到
	public final static String isContain = "isContain";

	//是否查询到重复的档案条目 没查询到
	public final static String notContain = "notContain";

	//导入覆盖值
	public final static int cover = 2;

	//导入跳过值
	public final static int jumpOver = 1;

	//跳过操作
	public final static String jumpAll = "jumpAll";

	//字段表达式匹配
	public final static int columnExpression = 1;

	//表中某列匹配
	public final static int fromColumn = 2;
}
