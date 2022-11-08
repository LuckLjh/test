package com.cescloud.saas.archive.api.modular.archiveconfig.constant;

import com.cescloud.saas.archive.api.modular.archivetype.entity.Layer;

public class ConfigConstant {

	/**
	 * 管理员租户ID
	 */
	public final static Long ADMIN_TENANT_ID = 1L;
	/**
	 * 公共标签层级
	 */
	public final static Layer COMMON_LAYER = Layer.builder().id(0L).code("ALL").name("公用").sortNo(0).build();

	/**
	 * 分页方式：上下翻页
	 */
	public final static Integer PAGE_UP_AND_DOWN = 1;
	/**
	 * 分页方式：显示总页数(默认方式)
	 */
	public final static Integer SHOW_TOTAL_OF_PAGES = 0;


}
