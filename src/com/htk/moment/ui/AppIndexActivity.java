package com.htk.moment.ui;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import utils.android.AppManager;
import utils.android.photo.CameraActivity;
import utils.android.photo.LocalPictureLibrary;
import utils.view.NotFilingViewPager;
import utils.view.fragment.IndexFragment;
import utils.view.fragment.MeFragment;
import utils.view.fragment.MessageFragment;
import utils.view.fragment.SearchFragment;

import java.util.ArrayList;


/**
 * App 主页 所有关注人的动态 时间倒序（最近的在最顶端）顺序排列
 *
 * @author 谭林
 * @version 2014/11/2.
 */
public class AppIndexActivity extends FragmentActivity {

    // 侦听器发出的按钮飞入消息标识
	public final static int FLING_IN = 0;
	public final static int FLING_OUT = 1;

    private final static int INIT_COMPLETED = 2;
    private static int speed = 10;
    private final String TAG = "PullToRefreshListView";
    private final static int DELAY = 10;

	private ArrayList<Fragment> fragments;

    // 菜单栏中间的按钮是否处于动画中
	private boolean buttonIsOnscreen;

	// 首页按钮
	private ImageView view_index;

	// 消息按钮
	private ImageView view_message;

	//
	private TextView logoName;

	// ImageView
	private View icon;

	// 搜索按钮
	private ImageView view_search;

	// 个人中心
	private ImageView view_me;

	// App 菜单栏中间按钮动画
	private Animation addButtonIn;

	private Animation addButtonOut;

	private int userId;

	/**
	 * 页面容器
	 * <p/>
	 * 跟传统的ViewPager不一样，它可以根据你的需要不水平滑动
	 */
	private NotFilingViewPager pages;

	// 照相机按钮（图标）
	private ImageView theCameraButton;

	// 图库 图标
	private ImageView thePictureButton;

	// 从屏幕左边飞入的相机按钮
	private View flingCameraButton;

	// 从屏幕右边飞入的图库按钮
	private View flingPictureButton;

	// 这两个按钮原始位置所在的布局
	private FrameLayout filingButtonLayout;

	// 相机按钮停止的X坐标
	private static int cameraMaxX = 0;

	// 图库按钮停止的X坐标
	private static int pictureMaxX = 0;

	// 窗口管理器，用它实现两个按钮从屏幕边缘出现
	private WindowManager windowManager;

	// 窗口布局参数
	private WindowManager.LayoutParams cameraParams;

	private WindowManager.LayoutParams pictureParams;

//	private VerticalViewPager mVerticalViewPager;


	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			Bundle data = msg.getData();
			int info = data.getInt("messageClass");
			switch (info) {
				case FLING_IN: {
					appear();
				}
				break;
				case INIT_COMPLETED: {
					//test();
				}
				break;
				case FLING_OUT: {
					disappear();
				}
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		AppManager.getAppManager().addActivity(this);
		setContentView(R.layout.after_login_layout);
		initAll();
		widgetsListener();
		sendLayoutCompleted();
	}

	@Override
	protected void onResume() {
		super.onResume();
		System.out.println("AppIndex resume");
	}

	@Override
	protected void onPause() {
		System.out.println("AppIndex pause");
		super.onPause();
	}

	@Override
	protected void onStop() {
		System.out.println("AppIndex stop");
		super.onStop();
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(KeyEvent.KEYCODE_BACK == keyCode){
			exitApp();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 退出对话框
	 */
	protected void exitApp() {
		AlertDialog.Builder builder = new AlertDialog.Builder(AppIndexActivity.this);
		builder.setMessage("确定要退出吗?");
		builder.setTitle("提示");
		builder.setPositiveButton("确定",
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						AppManager.getAppManager().appExit(AppIndexActivity.this);

//						AppIndexActivity.this.getApplication().onTerminate();
//						int nPid = android.os.Process.myPid();
//						android.os.Process.killProcess(nPid);
						dialog.dismiss();
//						AppIndexActivity.this.finish();
					}
				});
		builder.setNegativeButton("再玩会儿",
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}});
		builder.create().show();
	}

	/**
	 * 边缘进入的动画出现
	 */
	private void appear() {

		if (cameraParams.x >= cameraMaxX || pictureParams.x <= pictureMaxX) {
			windowManager.removeView(flingCameraButton);
			windowManager.removeView(flingPictureButton);
			mHandler.removeMessages(FLING_IN);
			filingButtonLayout.setVisibility(View.VISIBLE);
			theCameraButton.setVisibility(View.VISIBLE);
			thePictureButton.setVisibility(View.VISIBLE);
			cameraParams.x = 0;
			pictureParams.x = 0;
			return;
		}
		windowManager.updateViewLayout(flingCameraButton, cameraParams);
		windowManager.updateViewLayout(flingPictureButton, pictureParams);
		cameraParams.x += speed++;
		pictureParams.x -= speed++;
		mHandler.sendMessageDelayed(createAMsg("messageClass", FLING_IN), DELAY);
	}

	private void sendInMessage() {

		mHandler.sendMessage(createAMsg("messageClass", FLING_IN));
	}
	private void sendOutMessage() {

		mHandler.sendMessage(createAMsg("messageClass", FLING_OUT));
	}

	/**
	 * 消失
	 */
	private void disappear() {

		filingButtonLayout.setVisibility(View.GONE);
		theCameraButton.setVisibility(View.GONE);
		thePictureButton.setVisibility(View.GONE);

	}

	/**
	 * 初始化相关控件
	 */
	public void initAll() {
		userId = getIntent().getIntExtra("id", -1);

        initWidgets();
//		initViewPager();
		initPlusButtonAnimal();
		initFlingButton();
		initFragment();
        //userId = String.valueOf(u.getUserId());
	}

    public int getUserId(){
        return userId;
    }

	private void initFragment(){
		fragments = new ArrayList<Fragment>();

		fragments.add(new IndexFragment());
		fragments.add(new MessageFragment());
		fragments.add(new SearchFragment());
		fragments.add(new MeFragment());
        Bundle data = new Bundle();
        data.putInt("user_id", userId);
        fragments.get(3).setArguments(data);
		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();

		ft.add(R.id.app_index_container, fragments.get(0));
		ft.commit();
        //u = (UserIdGet) fragments.get(0);
	}

	int currentFragmentIndex = 0;
	/**
	 * set the current fragment for View
	 *
	 * @param index index of fragment
	 *              Note:the index begin 0
	 */
	private void setFragmentIndex(int index){

		if(index < 0 || index > 4){
			Log.w(TAG, "your index must select between zero and four!\n");
			return;
		}
		// 上一个fragment
		Fragment lastFragment = getCurrentFragment();

		// 准备显示的fragment
		Fragment newFragment = fragments.get(index);
		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
		if(newFragment.isVisible()) {
			return;
		}
		if (newFragment.isAdded()) {
			newFragment.onResume();
			ft.show(newFragment);
		} else {
			ft.add(R.id.app_index_container, newFragment);
			ft.show(newFragment);
		}
		ft.hide(lastFragment);
		ft.commit();
		currentFragmentIndex = index;
	}
	/**
	 * 得到上一次的Fragment，避免重复
	 *
	 * @return 上一次的Fragment
	 */
	private Fragment getCurrentFragment(){
		return fragments.get(currentFragmentIndex);
	}

	/**
	 * 加载动画布局
	 * <p/>
	 * “+” 按钮的动画
	 */
	private void initPlusButtonAnimal() {

		addButtonIn = AnimationUtils.loadAnimation(this, R.anim.plus_button_roate_open);
		addButtonOut = AnimationUtils.loadAnimation(this, R.anim.plus_button_roate_shutdown);
	}

	/**
	 * 初始化组件（控件）
	 * <p/>
	 * 从布局文件中找到相应的控件 以便对相应的事件进行响应
	 */
	private void initWidgets() {

		view_index = (ImageView) findViewById(R.id.index_index_image);
		view_message = (ImageView) findViewById(R.id.index_message_image);
		view_search = (ImageView) findViewById(R.id.index_search_image);
		view_me = (ImageView) findViewById(R.id.index_about_me_image);

		filingButtonLayout = (FrameLayout) findViewById(R.id.theAnimalLayout);

		windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		flingCameraButton = LayoutInflater.from(this).inflate(R.layout.the_fling_camera, null);
		flingPictureButton = LayoutInflater.from(this).inflate(R.layout.the_fling_picture, null);

		theCameraButton = (ImageView) findViewById(R.id.the_camera_button);
		thePictureButton = (ImageView) findViewById(R.id.the_picture_button);

		icon = findViewById(R.id.index_plus_button_image);
	}


	/**
	 * 通知UI线程，布局加载完成，可以对响应的控件进行事件侦听
	 */
	private void sendLayoutCompleted() {

		mHandler.sendMessageDelayed(createAMsg("messageClass", INIT_COMPLETED), DELAY);
	}

	private Message createAMsg(String key, int value) {

		Bundle data = new Bundle();
		Message msg = new Message();
		data.putInt(key, value);
		msg.setData(data);
		return msg;
	}

	private void widgetsListener() {

		icon.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				openOrShutdownButton();
			}
		});


		theCameraButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				shutdown();
				Intent intent = new Intent(AppIndexActivity.this, CameraActivity.class);
				intent.putExtra("id", userId);
				startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			}
		});

		thePictureButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				shutdown();
				Intent intent = new Intent(AppIndexActivity.this, LocalPictureLibrary.class);
				intent.putExtra("id", userId);
				startActivity(intent);
				overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			}
		});

		view_index.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {


				view_index.setImageResource(R.drawable.index_menu_home_img);
				view_message.setImageResource(R.drawable.index_menu_message_before_img);
				view_search.setImageResource(R.drawable.index_menu_search_before_img);
				view_me.setImageResource(R.drawable.index_menu_me_before_img);
				//pages.setCurrentItem(0, false);
				setFragmentIndex(0);
				shutdown();

			}
		});

		// 进入消息中心
		view_message.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				view_index.setImageResource(R.drawable.index_menu_home_before_img);
				view_message.setImageResource(R.drawable.index_menu_message_img);
				view_search.setImageResource(R.drawable.index_menu_search_before_img);
				view_me.setImageResource(R.drawable.index_menu_me_before_img);
//				pages.setCurrentItem(1, false);
				setFragmentIndex(1);
				shutdown();
				//startActivity(new Intent());
			}
		});

		/** 拍照上传或者是选择照片上传
		 * 添加动画，扇形选项
		 */

		// 搜索联系人、热门动态
		view_search.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				findViewById(R.id.my_index_button_liner1).setBackgroundResource(R.drawable.index_menu_button_back);

				view_index.setImageResource(R.drawable.index_menu_home_before_img);
				view_message.setImageResource(R.drawable.index_menu_message_before_img);
				view_search.setImageResource(R.drawable.index_menu_search_img);
				view_me.setImageResource(R.drawable.index_menu_me_before_img);
//				pages.setCurrentItem(2, false);
				setFragmentIndex(2);
				shutdown();
				//startActivity(new Intent());
			}
		});
		// 进入个人中心
		view_me.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				view_index.setImageResource(R.drawable.index_menu_home_before_img);
				view_message.setImageResource(R.drawable.index_menu_message_before_img);
				view_search.setImageResource(R.drawable.index_menu_search_before_img);
				view_me.setImageResource(R.drawable.index_menu_me_img);
//				pages.setCurrentItem(3, false);
				setFragmentIndex(3);
				shutdown();
			}
		});
	}

	private void initFlingButton() {

		cameraParams = getWindowParams("camera");
		pictureParams = getWindowParams("picture");
	}

	private void addWindowButton() {

		// 添加WindowView（不受父控件约束）
		windowManager.addView(flingCameraButton, cameraParams);
		windowManager.addView(flingPictureButton, pictureParams);
	}

	/**
	 * 从屏幕的下边飞入两个按钮
	 *
	 * @param tag 标志某个窗口动画文件
	 *
	 * @return 动画参数
	 */
	private WindowManager.LayoutParams getWindowParams(String tag) {

		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				0, 0,
				WindowManager.LayoutParams.TYPE_TOAST,
				WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
				PixelFormat.RGBA_8888);
		params.gravity = Gravity.LEFT | Gravity.TOP;
		int h = findViewById(R.id.bottomTarBar).getHeight();
//		int h = findViewById(R.id.tabs_rg).getHeight();
		Rect rect = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
		params.y = rect.bottom - h * 5 / 2;
		if (tag == null) {
			Log.e("wrong", "It mustn't be give a par");
			return null;
		} else if (tag.equals("camera")) {
			cameraMaxX = (rect.right) / 2 - h * 3;
			params.x = 0;
		} else {
			pictureMaxX = (rect.right) / 2 + h * 3;
			params.x = rect.right;
		}
		return params;
	}

	/**
	 * 相机按钮出现或是消失
	 */
	private void openOrShutdownButton() {

		if (!buttonIsOnscreen) {
			icon.startAnimation(addButtonIn);
			initFlingButton();
			addWindowButton();
			sendInMessage();
		} else {
			icon.startAnimation(addButtonOut);
			sendOutMessage();
		}
		buttonIsOnscreen = !buttonIsOnscreen;
	}
	// 停止显示相机按钮
	private void shutdown() {

		if (buttonIsOnscreen) {
			icon.startAnimation(addButtonOut);
		}
		disappear();
		buttonIsOnscreen = false;
	}
}