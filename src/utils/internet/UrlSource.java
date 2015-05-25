package utils.internet;

/**
 * author：康乐
 * time：2014/11/13
 * function：用于整体管理修改url
 */

public class UrlSource {

	// 注册url
	public static String SIGN_UP = "/sign_up";

	// 登陆url
	public static String LOGIN = "/login";

	// 下载status url
	public static String LOAD_STATUS = "/load_status";

	// 检查邮箱是否存在url
	public static String CHECK_EMAIL = "/check_email";

	// 上传照片url
	public static String UPLOAD_PHOTO = "/upload_photo";

	// 获得用户信息url
	public static String GET_FOLLOWINGS_INFO = "/get_followings_info";

	// 获得指定用户信息
	public static String GET_USER_INFO = "/get_user_info";

	// 获得用户的三个数量指标
	public static String GET_THREE_NUMBER = "/get_three_number";

	public static String LIKE_STATUS = "/like_status";

	public static String COMMENT_STATUS = "/comment_status";
	// 获取缩略图信息
	public static String GET_MORE_SMALL_PHOTO = "/get_more_small_photo";

    public static String FOLLOW_BY_ID = "/follow_by_id";

	public static String LOAD_STATUS_BY_ID = "/load_status_by_id";

	public static String LOAD_NOTIFY = "/load_notify";


	/**
	 * 得到图片真实的URL
	 * <p/>
	 * 因为充服务器获取到路径是绝对路径，linux系统，路径包含“/XX/XXX”
	 * 要得到某路径，截断此字符串
	 *
	 * @param path 从服务器得到的路径（url）
	 *
	 * @return 可供应用请求的路径（url）
	 */
	public static String getUrl(String path) {

		String url = path.split("mks")[1];
		String ret;
		if(url.contains("\\")){
			ret = url.replace('\\', '/');
		}else {
			ret = url;
		}
		return ret;
	}

}
