package com.htk.moment.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import utils.android.AppManager;
import utils.android.sdcard.Read;
import utils.check.Check;
import utils.internet.ConnectionHandler;
import utils.internet.UrlSource;
import utils.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;


/**
 * 用户注册 成功便进入App首页，但是通过发送登录请求进入的
 * <p/>
 * 需要获取SESSION（用于唯一标识用户）
 * <p/>
 * Created by HP on 2014/7/18.
 */
public class RegisterActivity extends Activity {

	/**
	 * SESSION
	 */
	private String SESSIONID = null;

	private String name;

	private String password;

	private String passwordConfirm;

	private String emailAddress;

	private EditText nameFind;

	private EditText passwordFind;

	private EditText emailAddressFind;

	private EditText passwordConfirmFind;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		AppManager.getAppManager().addActivity(this);
		/**
		 * 设置无标题，全屏幕显示
		 */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.register_layout);

		ImageView back1 = (ImageView) findViewById(R.id.backImage);
		TextView back2 = (TextView) findViewById(R.id.backText);

		/**
		 * 找到各个编辑框
		 */
		nameFind = ((EditText) findViewById(R.id.register_name_edit));
		passwordFind = ((EditText) findViewById(R.id.register_password_edit));
		passwordConfirmFind = (EditText) findViewById(R.id.register_password_confirm_edit);
		emailAddressFind = ((EditText) findViewById(R.id.register_email_edit));

		//checkBox = (CheckBox) findViewById(R.id.checkbox);

		/**
		 * 获取注册按钮
		 */
		Button register = (Button) findViewById(R.id.button_register);
		Button reset = (Button) findViewById(R.id.reset);

		back1.setOnClickListener(new BackOnClickListener());
		back2.setOnClickListener(new BackOnClickListener());

		register.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				/** 当所有项目都填写完毕之后，
				 * 点击注册就直接发送响应的数据给服务器就好
				 */
				if (canRegister()) {
					new RegisterThread().start();
				}
			}
		});


		reset.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				nameFind.setText("");
				passwordFind.setText("");
				emailAddressFind.setText("");
				passwordConfirmFind.setText("");
			}
		});
		nameFind.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {

				if (!hasFocus) {
					name = nameFind.getText().toString();
					if (name.length() == 0) {
						Toast.makeText(getApplication(), R.string.name_null, Toast.LENGTH_LONG).show();
					}
				}
			}
		});

		passwordFind.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {

				if (!hasFocus) {
					password = passwordFind.getText().toString();
					if (password.length() == 0) {
						Toast.makeText(getApplication(), R.string.password_null, Toast.LENGTH_LONG).show();
					}
				}
			}
		});
		passwordConfirmFind.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {

				if (!hasFocus) {
					passwordConfirm = passwordConfirmFind.getText().toString();
					if (!password.equals(passwordConfirm)) {
						Toast.makeText(getApplication(), R.string.password_equal, Toast.LENGTH_LONG).show();
					}
				}
			}
		});
		//在填写邮箱的完毕会连接服务器并检测此邮箱是否已经注册过本网站
		emailAddressFind.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
					emailAddress = emailAddressFind.getText().toString();
					//自动以藏输入键盘
					InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					if (imm.isActive()) {
						imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
					}
					if (!Check.isEmail(emailAddress) && !Check.isPhoneNumber(emailAddress)) {
						Toast.makeText(getApplication(), R.string.format_wrong, Toast.LENGTH_SHORT).show();
						emailAddressFind.setText("");
					} else {
						new emailCheckThread().start();
					}
					return true;
				}
				return false;
			}
		});
	}

	//处理子线程传回的数据（消息）
	Handler handler = new Handler() {

		public void handleMessage(Message msg) {

			String result = msg.getData().getString("result");
			//注册成功
			if ("allRight".equals(result)) {
				Toast.makeText(getApplicationContext(), "注册成功", Toast.LENGTH_SHORT).show();
				//进入个人主页（设置）
				//或者进入动态页
				new LoginThread().start();
			} else if ("theThemeName".equals(result)) {
				//邮箱被注册过
				AlertDialog.Builder dialog = new AlertDialog.Builder(RegisterActivity.this);
				dialog.setMessage(R.string.email_used);
				dialog.setCancelable(true);
				dialog.setTitle(R.string.register_error);
				emailAddressFind.setText("");
				dialog.create().show();
			} else if ("null".equals(result)) {
				Toast.makeText(RegisterActivity.this, R.string.name_null, Toast.LENGTH_SHORT).show();
			} else if ("timeOut".equals(result)) {
				Toast.makeText(RegisterActivity.this, R.string.timeout, Toast.LENGTH_SHORT).show();
			} else if ("login".equals(result)) {
				// 给全局的静态变量 JSESSIONID 赋值，后续要使用。
				LaunchActivity.JSESSIONID = SESSIONID;
				startActivity(new Intent(RegisterActivity.this, AppIndexActivity.class));
			}
		}
	};

	/**
	 * 检测用户注册的时候，邮箱是否符合注册标准 必须在子线程中执行
	 */
	private class emailCheckThread extends Thread {

		HttpURLConnection connection;

		public void run() {

			try {
				connection = ConnectionHandler.getConnect(UrlSource.CHECK_EMAIL);
				connection.connect();
				JSONObject object = new JSONObject();
				object.put("account", emailAddress);
				connection.getOutputStream().write(object.toString().getBytes());
				//读取服务器返回的消息
				JSONObject jsonObject = new JSONObject(Read.read(connection.getInputStream()));
				String accountResult = jsonObject.getString("accountResult");
				if (accountResult.equals("exist")) {
					sendMessage("result", "theThemeName");
				} else if (accountResult.equals("formatError")) {
					sendMessage("result", "formatError");
				} else if (accountResult.equals("error")) {
					sendMessage("result", "serverError");
				} else if (accountResult.equals("exist")) {
					sendMessage("result", "theThemeName");
				} else {
					sendMessage("result", "messageWrong");
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (SocketException e) {
				sendMessage("result", "timeOut");
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				connection.disconnect();
			}
		}
	}

	private class RegisterThread extends Thread {

		@Override
		public void run() {

			HttpURLConnection connection;
			JSONObject info = new JSONObject();
			connection = ConnectionHandler.getConnect(UrlSource.SIGN_UP);
			info.put("name", name);

			// 这点流量就不要计较了
			info.put("userAccount", emailAddress);
			info.put("password", password);
			try {
				connection.connect();
				OutputStream writeToServer = connection.getOutputStream();
				writeToServer.write(info.toString().getBytes());
				// 取得输入流，并使用Reader读取
				JSONObject object = new JSONObject(Read.read(connection.getInputStream()));
				String result = object.getString("accountResult");
				if (result.equals("success")) {
					sendMessage("result", "allRight");
				} else if (result.equals("formatError")) {
					sendMessage("result", "formatError");
				} else if (result.equals("error")) {
					sendMessage("result", "serverError");
				} else if (result.equals("exist")) {
					sendMessage("result", "theThemeName");
				} else {
					sendMessage("result", "messageWrong");
				}
				// 断开连接
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (SocketTimeoutException e) {
				sendMessage("result", "timeOut");
			} catch (SocketException e) {
				Toast.makeText(getApplication(), "网络没有打开，无法使用。", Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				connection.disconnect();
			}
		}
	}

	private class LoginThread extends Thread {

		@Override
		public void run() {

			HttpURLConnection connection;
			JSONObject info = new JSONObject();
			connection = ConnectionHandler.getConnect(UrlSource.LOGIN);
			// 这点流量就不要计较了
			info.put("account", emailAddress);
			info.put("password", password);
			try {
				connection.connect();
				OutputStream writeToServer = connection.getOutputStream();
				// 发送数据
				writeToServer.write(info.toString().getBytes());
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

	private void handleTheResult(JSONObject obj) {

		String result = obj.getString("accountResult");
		if (result.equals("success")) {
			// 登录成功
			sendMessage("password", "true");
			LaunchActivity.JSESSIONID = obj.getString("JSESSIONID");
			Intent intent = new Intent(RegisterActivity.this, AppIndexActivity.class);
			startActivity(intent);
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
	 * 从服务器得到数据
	 *
	 * @param inputStream 一个输入流
	 *
	 * @return 得到服务器返回的Json字符串
	 *
	 * @throws IOException 读服务器返回的数据发生异常
	 */
	private JSONObject getServerInformation(InputStream inputStream) throws IOException {

		return new JSONObject(Read.read(inputStream));
	}

	private boolean canRegister() {

		name = nameFind.getText().toString();
		password = passwordFind.getText().toString();
		passwordConfirm = passwordConfirmFind.getText().toString();
		emailAddress = emailAddressFind.getText().toString();
		if (name.length() == 0 || password.length() == 0 || emailAddress.length() == 0) {
			sendMessage("result", "null");
			return false;
		}
		return true;
	}

	private void sendMessage(String key, String value) {

		Bundle data = new Bundle();
		Message msg = new Message();
		data.putString(key, value);
		msg.setData(data);
		handler.sendMessage(msg);
	}

	private class BackOnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {

			startActivity(new Intent(RegisterActivity.this, LaunchActivity.class));
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		System.out.println("aaaaaaaaaaaa");
		super.onDestroy();
	}
}
