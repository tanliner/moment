package com.htk.moment.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import utils.android.AppManager;
import utils.android.sdcard.Read;
import utils.check.Check;
import utils.internet.ConnectionHandler;
import utils.internet.UrlSource;
import utils.json.JSONObject;
import utils.services.LoadDataService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;


/**
 * 主Activity，程序启动的入口。 功能：登录 初始化服务器地址，启动相关的后台服务等操作。
 *
 * @author 谭林
 * @version 1.0
 */
public class LaunchActivity extends Activity {

	// 客户端的屏幕尺寸
	public static int screenWidth = 0;

	public static int screenHeight = 0;

	// 界面的编辑框
//	private EditText emailEdit;

	private EditText passwordEdit;

	// 用户名以及密码框的值
	private String emailOrPhone;

	private String password;

	// 按钮
	private Button buttonLogin;

	private TextView textViewRegister;

	private TextView toFindPassword;


	private AutoCompleteTextView mName;

	// 为了与服务器保持长连接的，设定的cookie标识
	public static String JSESSIONID = "";

	private int userId = 84;

	public static LoadDataService loadDataService;

	/**
	 * 启动Activity是自动调用此方法 得到客户端的屏幕尺寸，显示界面
	 *
	 * @param savedInstanceState 切换Activity需要系统保存的一些信息
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		// 显示样式：无标题栏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		AppManager.getAppManager().addActivity(this);
		//当前Activity 是哪一个布局文件，以何种方式显示
		setContentView(R.layout.lanuch_layout);
		initWidgets();
		theWidgetsFunction();
	}

	@Override
	protected void onResume() {
		initAutoComplete("name", mName);
		super.onResume();
	}

	@Override
	protected void onPause() {
		saveHistory("name", mName);
		super.onPause();
	}


	/**
	 * 初始化控件
	 * <p/>
	 * 因为程序必须，通过这些控件进行事件处理
	 */
	private void initWidgets() {
		//得到屏幕的尺寸，后续使用
		WindowManager vm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		screenWidth = vm.getDefaultDisplay().getWidth();
		screenHeight = vm.getDefaultDisplay().getHeight();

		mName = (AutoCompleteTextView) findViewById(R.id.set_name);

		//登录（注册）按钮
		buttonLogin = (Button) findViewById(R.id.button_login);
		textViewRegister = (TextView) findViewById(R.id.button_register);

		//登录填写的邮箱，密码编辑框
		//		emailEdit = (EditText) findViewById(R.id.set_name);
		passwordEdit = (EditText) findViewById(R.id.set_password);

		//记住密码
		//CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox);
		toFindPassword = (TextView) findViewById(R.id.find_password);
		toFindPassword.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				//连接到服务器找回密码

				Intent intent = new Intent(LaunchActivity.this, AppIndexActivity.class);
				startActivity(intent);
//				Intent intent = new Intent(LaunchActivity.this, LoadDataService.class);
//				startService(intent);

				//Toast.makeText(getApplication(), "服务器暂时不能处理找回密码", Toast.LENGTH_SHORT).show();
				//				startActivity(new Intent().setClass(LaunchActivity.this, AppIndexActivity.class));
			}
		});
	}

	/**
	 * 初始化AutoCompleteTextView，最多显示5项提示，使
	 * AutoCompleteTextView在一开始获得焦点时自动提示
	 *
	 * @param field 保存在sharedPreference中的字段名
	 * @param auto  要操作的AutoCompleteTextView
	 */
	private void initAutoComplete(String field, AutoCompleteTextView auto) {

		SharedPreferences sp = getSharedPreferences("user_name", Activity.MODE_PRIVATE);
		String history = sp.getString(field, "");

		String[] hisArrays = history.split(",");

		ArrayAdapter<String> adapter =
				new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, hisArrays);
		//只保留最近的5条的记录
		if (hisArrays.length > 5) {
			String[] newArrays = new String[5];
			System.arraycopy(hisArrays, 0, newArrays, 0, 5);
			adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, newArrays);
		}
		auto.setAdapter(adapter);
		auto.setDropDownHeight(400);
		auto.setThreshold(1);
		auto.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {

				AutoCompleteTextView view = (AutoCompleteTextView) v;
				if (hasFocus) {
					view.showDropDown();
				}
			}
		});
	}

	/**
	 * 把指定AutoCompleteTextView中内容保存到sharedPreference中指定的字符段
	 *
	 * @param field 保存在sharedPreference中的字段名
	 * @param auto  要操作的AutoCompleteTextView
	 */
	private void saveHistory(String field, AutoCompleteTextView auto) {

		String text = auto.getText().toString();
		SharedPreferences sp = getSharedPreferences("user_name", Activity.MODE_PRIVATE);
		String history = sp.getString(field, "");

		if (!history.contains(text + ",")) {
			StringBuilder sb = new StringBuilder(history);
			sb.insert(0, text + ",");
			sp.edit().putString(field, sb.toString()).apply();
		}
	}

	/**
	 * 控件的相应的功能
	 */
	private void theWidgetsFunction() {
		//点击注册，跳转到注册页面
		//验证用户是否存在等信息在注册页面进行
		textViewRegister.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				startActivity(new Intent(LaunchActivity.this, RegisterActivity.class));
			}
		});
		//点击登录，进行验证用户名以及密码。
		buttonLogin.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				emailOrPhone = mName.getText().toString();
				password = passwordEdit.getText().toString();
				if (emailOrPhone == null || password == null || password.length() == 0 || emailOrPhone.length() == 0) {
					Toast.makeText(getApplication(), R.string.login_warning, Toast.LENGTH_SHORT).show();
				} else {
					if (!Check.internetIsEnable(LaunchActivity.this)) {
						Toast.makeText(getApplication(), "网络没有打开，无法使用。", Toast.LENGTH_SHORT).show();
						setTheInternet();
					} else if (!Check.isEmail(emailOrPhone) && !Check.isPhoneNumber(emailOrPhone)) {
						Toast.makeText(getApplicationContext(), R.string.format_wrong, Toast.LENGTH_SHORT).show();
						mName.setText("");
						emailOrPhone = null;
					} else {
						new LoginThread().start();
					}
				}
			}
		});
		// 获取昵称编辑框的数据（通过焦点转移）
		//		emailEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
		//			@Override
		//			public void onFocusChange (View v, boolean hasFocus) {
		//
		//				if (!hasFocus) {
		//					emailOrPhone = emailEdit.getText().toString();
		//					if (!Check.isEmail(emailOrPhone) && !Check.isPhoneNumber(emailOrPhone)) {
		//						Toast.makeText(getApplicationContext(), R.string.format_wrong, Toast.LENGTH_SHORT).show();
		//						emailEdit.setText("");
		//						emailOrPhone = null;
		//					}
		//				}
		//			}
		//		});

		mName.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
					emailOrPhone = mName.getText().toString();
					//自动以藏输入键盘
					InputMethodManager imm =
							(InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					if (imm.isActive()) {
						imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
					}
					return true;
				}
				return false;
			}
		});

		passwordEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {

				if (!hasFocus) {
					password = passwordEdit.getText().toString();
				}
			}
		});
		// 编辑框设置回车隐藏
		passwordEdit.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
					password = passwordEdit.getText().toString();
					//自动以藏输入键盘
					InputMethodManager imm =
							(InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					if (imm.isActive()) {
						imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
					}
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * 当用户没有连入互联网的时候，跳转至网络设置
	 */
	private void setTheInternet() {

		AlertDialog.Builder dialog = new AlertDialog.Builder(LaunchActivity.this);

		dialog.setTitle(R.string.login_dialog_title);
		dialog.setMessage(R.string.net_warning);
		dialog.setPositiveButton(R.string.login_dialog_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();
				startActivity(new Intent(Settings.ACTION_SETTINGS));
			}
		});
		dialog.setNegativeButton(R.string.login_dialog_cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();
			}
		});
		dialog.create().show();
	}


	// 主线程与子线程的消息通道
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			Bundle data = msg.getData();
			if ("true".equals(data.getString("password"))) {
				//Toast.makeText(getApplicationContext(), "登录成功！", Toast.LENGTH_SHORT).show();

				ServiceConnection serviceConnection = new ServiceConnection() {
					@Override
					public void onServiceConnected(ComponentName name, IBinder service) {
						loadDataService = ((LoadDataService.MyBinder) service).getService();
					}

					@Override
					public void onServiceDisconnected(ComponentName name) {

					}
				};
				Intent intentS = new Intent(LaunchActivity.this, LoadDataService.class);
				bindService(intentS, serviceConnection, Context.BIND_AUTO_CREATE);

				Intent intent = new Intent(LaunchActivity.this, AppIndexActivity.class);
				intent.putExtra("id", userId);
				startActivity(intent);


			} else if ("passwordWrong".equals(data.getString("result"))) {
				Toast.makeText(LaunchActivity.this, R.string.login_error, Toast.LENGTH_SHORT).show();
			} else if ("timeOut".equals(data.getString("result"))) {
				Toast.makeText(LaunchActivity.this, R.string.timeout, Toast.LENGTH_SHORT).show();
			} else if ("formatError".equals(data.getString("result"))) {
				Log.e("CLIENT", "格式错误！");
				//startActivity(new Intent(LaunchActivity.this, NewIndex.class));
			} else if ("shutdown".equals(data.getString("server"))) {
				Toast.makeText(getApplication(), "服务器异常", Toast.LENGTH_SHORT).show();
			}
		}
	};

	/**
	 * 登录线程，所有的耗时操作不能再主线程中。 主线程阻塞5秒左右未能响应，系统会清醒关闭应用程序
	 */
	private class LoginThread extends Thread {

		@Override
		public void run() {

			HttpURLConnection connection = ConnectionHandler.getConnect(UrlSource.LOGIN);
			// 构造json字符串，并发送
			JSONObject loginInformation = new JSONObject();

			loginInformation.put("account", emailOrPhone);
			loginInformation.put("password", password);

			try {
				connection.connect();
				OutputStream writeToServer = connection.getOutputStream();
				// 发送数据
				writeToServer.write(loginInformation.toString().getBytes());
				writeToServer.close();
				// 处理服务器消息
				handleTheResult(getServerInformation(connection.getInputStream()));

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (SocketTimeoutException e) {
				sendMessage("result", "timeOut");
			} catch (SocketException e) {
				sendMessage("server", "shutdown");
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				connection.disconnect();
			}
		}
	}

	/**
	 * 从服务器得到数据
	 *
	 * @param inputStream 一个输入流
	 * @return 得到服务器返回的Json字符串
	 * @throws IOException 读服务器返回的数据发生异常
	 */
	private JSONObject getServerInformation(InputStream inputStream) throws IOException {

		return new JSONObject(Read.read(inputStream));
	}

	/**
	 * 子线程事件处理，判断返回的类型，结果交付给主线程 由主线程在界面上显示，给用户流畅的体验
	 *
	 * @param obj 服务器返回给客户端的Json字符串
	 */
	private void handleTheResult(JSONObject obj) {

		String result = obj.getString("accountResult");
		if (result.equals("success")) {
			// 登录成功
			sendMessage("password", "true");
			JSESSIONID = obj.getString("JSESSIONID");
		} else if (result.equals("dataWrong")) {
			// 密码错误/此用户名不存在，报告给用户处理
			sendMessage("result", "passwordWrong");
		} else if (result.equals("formatError")) {
			// 数据格式错误，由程序员处理
			sendMessage("result", "formatError");
		} else {
			sendMessage("result", "timeOut");
			System.out.println("服务器没有响应" + result);
		}

	}

	/**
	 * 子线程调用sendMessage向主线程发消息
	 *
	 * @param key   关键字，什么样的数据
	 * @param value 关键字对应的值
	 */
	private void sendMessage(String key, String value) {

		Bundle data = new Bundle();
		Message msg = new Message();
		data.putString(key, value);
		msg.setData(data);
		handler.sendMessage(msg);
	}
}