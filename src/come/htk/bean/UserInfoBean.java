package come.htk.bean;

/**
 * @author Administrator 谭林
 * 个人主页信息bean
 *
 * adapter 需要的数据跟app主页类似，故，数据
 */
public class UserInfoBean {

	private int ID;

	private String name;

	// 个性签名
	private String brief_intro;

	//背景图片
	private String bg_photo;
	// 用户主页头像地址   浏览所关注的人的动态的页面时
	private String main_head_photo;
	// 用户个人中心，头像
	private String home_head_photo;

	public int getID() {

		return ID;
	}

	public void setID(int ID) {

		this.ID = ID;
	}

	public String getName() {

		return name;
	}

	public void setName(String name) {

		this.name = name;
	}

	public String getBrief_intro() {

		return brief_intro;
	}

	public void setBrief_intro(String brief_intro) {

		this.brief_intro = brief_intro;
	}

	public String getBg_photo() {

		return bg_photo;
	}

	public void setBg_photo(String bg_photo) {

		this.bg_photo = bg_photo;
	}

	public String getMain_head_photo() {

		return main_head_photo;
	}

	public void setMain_head_photo(String main_head_photo) {

		this.main_head_photo = main_head_photo;
	}

	public String getHome_head_photo() {

		return home_head_photo;
	}

	public void setHome_head_photo(String home_head_photo) {

		this.home_head_photo = home_head_photo;
	}
}
