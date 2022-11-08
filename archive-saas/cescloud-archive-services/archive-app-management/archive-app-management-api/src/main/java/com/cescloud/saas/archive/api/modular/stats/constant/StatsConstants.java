/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.stats.constant</p>
 * <p>文件名:StatsConstants.java</p>
 * <p>创建时间:2020年10月20日 下午2:18:06</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.stats.constant;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年10月20日
 */
public class StatsConstants {

    /**
     * 档案门类编码
     */
    public final static String ARCHIVE_TYPE_CODE = "archive_type_code";
	/**
	 * 档案门类名称
	 */
	public final static String ARCHIVE_TYPE_NAME = "archive_type_name";

    /**
     * 统计标题
     */
    public final static String STATS_TITLE = "stats_title";

    /**
     * 统计数量
     */
    public final static String STATS_AMOUNT = "stats_amount";

    /**
     * 卷内文件数量
     */
    public final static String FILE_AMOUNT = "file_amount";

    /**
     * 案卷/文件：页数；音视频：时长
     */
    public final static String PAGE_AMOUNT = "page_amount";

    /**
     * 案卷/文件：已数字化页数；音视频：已数字化时长
     */
    public final static String DIGITED_PAGE_AMOUNT = "digited_page_amount";

    /**
     * 已数字化数量
     */
    public final static String DIGITED_AMOUNT = "digited_amount";
}
