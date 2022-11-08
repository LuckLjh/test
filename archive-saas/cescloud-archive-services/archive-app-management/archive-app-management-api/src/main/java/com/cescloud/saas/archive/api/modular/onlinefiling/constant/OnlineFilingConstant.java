package com.cescloud.saas.archive.api.modular.onlinefiling.constant;


public class OnlineFilingConstant {
	//批次的标识，可以不用
	public final static String  BATCH_NO = "batch_no";
	//归档用户ID
	public final static String FILING_USER_ID = "filing_user_id";
	//档案类型，用于判断档案类型，获取档案层次 传递为中文
	public final static String  ARCHIVE_TYPE = "archive_type";
	//全文校验码类型，MD5、SM3、SHA1等，没有的话可以默认为MD5
	public final static String  CHECK_CODE_TYPE = "check_code_type";
	//案卷集合
	public final static String  FOLDERS = "folders";
	//文件集合
	public final static String  FILES = "files";
	// 附件集合
	public final static String  DOCUMENTS = "documents";
	// 过程信息集合
	public final static String  INFOS = "infos";
	// 签章信息集合
	public final static String  SIGNS = "signs";
	//业务系统或OA的唯一标识
	public final static String  BUSINESS_ID = "business_id";
	//全文的大小
	public final static String  FILE_SIZE = "file_size";
	//全文的校验码
	public final static String  CHECK_CODE = "check_code";
	//文件URL，文件获取的url地址，http、
	//https、ftp、sftp地址（需要用户名密码，作为批次属性）
	public final static String  FILE_URL = "file_url";
	//回调URL，异步导入完成后访问。同样进行签名
	public final static String  CALLBACK_URL = "callback_url";
	// 题名
	public final static String   TITLE_PROPER = "title_proper";
	// 全宗
	public final static String  FONDS_CODE = "fonds_code";
	// 案卷元数据
	public final static String  PROJECT_META_DATA = "projectMetadata";
	// 案卷元数据
	public final static String  FOLDER_META_DATA = "folderMetadata";
	// 文件元数据
	public final static String  FILE_META_DATA = "fileMetadata";
	// 电子文件元数据
	public final static String  DOC_META_DATA = "docMetadata";
	// 过程信息元数据
	public final static String  INFO_META_DATA = "infoMetadata";
	// 单套制元数据
	public final static String  SIGN_META_DATA = "signMetadata";
	// 文件校验方式MD5
	public final static String  MD5 = "MD5";
	// 文件校验方式SM3
	public final static String  SM3 = "SM3";
	// 文件校验方式SHA1
	public final static String  SHA1 = "SHA1";
	// 档案类型编码
	public final static String ARCHIVE_TYPE_CODE = "archiveTypeCode";
	// 档案类型名称
	public final static String ARCHIVE_TYPE_NAME = "archiveTypeName";
	//成功数量
	public final static String SUCCESSNUM = "successNum";
	//失败数量
	public final static String FAILEDNUM = "failedNum";

	//回调code 		0：表示成功，其他表示失败
	public final static String RET_CODE = "code";
	//回调msg ，code非0时为错误消息
	public final static String RET_MSG = "msg";
	//返回数据结果，返回集合为失败的条目业务ID（business_id）
	public final static String RET_DETAIL_ID_MSG = "data";
	//异常信息
	public final static String INFO = "info";

	//ftp 或者 sftp 中是否包含用户名
	public final static String LOGIN_NAME = "loginName";

	//ftp 或者 sftp 中是否包含密码
	public final static String LOGIN_PASSWORD = "loginPassword";

	//ftp 或者 sftp 中是否包含密码
	public final static String LOGIN_PORT = "loginPort";

	//ftp 或者 sftp 中是否存放路径
	public final static String LOGIN_STORAGE_PATH = "loginStoragePath";

	//在线归档默认值, 待归档
	public  static Object FILING_STATUS = 30;
	//在线归档默认值, 项目未结项
	public  static Object FILING_STATUS_PRO = 60;

	public final static String HTTP = "http";

	public final static String FTP = "ftp";

	public final static String SFTP = "sftp";
}
