package com.htk.moment.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import utils.android.AppManager;
import utils.android.sdcard.Read;
import utils.internet.ConnectionHandler;
import utils.internet.UrlSource;
import utils.json.JSONObject;
import utils.view.fragment.IndexFragment;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;


/**
 * 浏览大图
 *
 * @author 谭林
 *         Created by Administrator on 2014/12/1.
 */
public class PictureScanActivity extends Activity {

	public static int default_int = -1;

	public static final String TAG = "PictureScanActivity";

	private static boolean showInfo = false;

	private final int LIKE = 0;

	private final int SHARE = 1;

	private final int COMMENT = 2;

	private final int GET_BIG_PHOTO = 3;

	private boolean isLike = false;

	// 大图
	private ImageView mImageView;

	// 喜欢（赞）
	private ImageView mLikeImageView;

	private ImageView mCommentImageView;

	private ImageView mShareImageView;

	private ProgressBar mProgressBar;

	private TextView mLikeNumText;


	private static MyHandler myHandler;

	private RelativeLayout mRelativeLayout;

	TextView mTextView;

	Intent mIntent;


	static int rs_id;

	int likeNum;

	int userId;

	String detail;

	String detailUrl;

	Bitmap bitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		AppManager.getAppManager().addActivity(this);
		setContentView(R.layout.picture_scan_mode_big);

		initAll();
		imageListener();
	}

	@Override
	protected void onPause() {
		bitmap.recycle();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(bitmap == null){
			new MyLikeThread(GET_BIG_PHOTO).start();
		} else {
			mImageView.setImageBitmap(bitmap);
		}
		mLikeNumText.setText(String.valueOf(likeNum));
	}

	private void initAll() {

		initImg();
		mIntent = getIntent();
		myHandler = new MyHandler();
		rs_id = mIntent.getIntExtra("rs_id", default_int);
		detail = mIntent.getStringExtra("detailPhoto");
		userId = mIntent.getIntExtra("userId", default_int);
		detailUrl = UrlSource.getUrl(detail);
		likeNum = mIntent.getIntExtra("like", -1);
	}

	private void initImg() {

		mImageView = (ImageView) findViewById(R.id.picture_scan_mode_big_image);
		mTextView = (TextView) findViewById(R.id.picture_scan_mode_big_describe);
		mRelativeLayout = (RelativeLayout) findViewById(R.id.picture_scan_relative);
		mLikeImageView = (ImageView) findViewById(R.id.like_img_of_index_picture_scan_mode_big);
		mShareImageView = (ImageView) findViewById(R.id.share_img_of_index_picture_scan_mode_big);
		mCommentImageView = (ImageView) findViewById(R.id.comment_img_of_index_picture_scan_mode_big);
		mProgressBar = (ProgressBar) findViewById(R.id.big_photo_progress);

		mLikeNumText = (TextView) findViewById(R.id.like_text_of_index_picture_scan_mode_big);
	}

	private void imageListener() {

		mImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!showInfo) {
					mRelativeLayout.setVisibility(View.VISIBLE);
				} else {
					mRelativeLayout.setVisibility(View.GONE);
				}
				showInfo = !showInfo;
			}
		});

		mLikeImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				isLike = !isLike;
				if (isLike) {
					mLikeImageView.setImageResource(R.drawable.like_after);
					//IndexFragment.sendMessage("fresh", "liked");
				} else {
					mLikeImageView.setImageResource(R.drawable.like_image_button_hollow);
					//IndexFragment.sendMessage("fresh", "like");
				}

				new MyLikeThread(LIKE).start();
			}
		});

		mShareImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

//				new MyLikeThread(SHARE).start();
			}
		});

		mCommentImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

//				new MyLikeThread(COMMENT).start();
			}
		});
	}

	public static int getRs_id(){
		return rs_id;
	}

//	@Override
//	public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
//
//		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			finish();
//			return true;
//		}
//		return super.onKeyDown(keyCode, event);
//	}



	private class MyLikeThread extends Thread {

		private int request;

		public MyLikeThread(int req) {

			request = req;
		}

		@Override
		public void run() {
			HttpURLConnection con;

			switch (request) {
				case LIKE: {
					con = ConnectionHandler.getConnect(UrlSource.LIKE_STATUS, LaunchActivity.JSESSIONID);
					OutputStream os;
					try {
						os = con.getOutputStream();
						JSONObject outObject = new JSONObject();
						outObject.put("rs_id", rs_id);
						outObject.put("isLike", isLike);
						outObject.put("likeder", userId);
						os.write(outObject.toString().getBytes());
						String str = Read.read(con.getInputStream());
						handleServerMessage(str);
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						con.disconnect();
					}

					break;
				}
				case SHARE: {
					break;
				}
				case COMMENT: {

					con = ConnectionHandler.getConnect(UrlSource.COMMENT_STATUS, LaunchActivity.JSESSIONID);
					OutputStream os;
					try {
						os = con.getOutputStream();
						JSONObject outObject = new JSONObject();
						outObject.put("rs_id", rs_id);
						// 被评论者的ID
						outObject.put("commented", userId);
						outObject.put("comment", "哇，这电杆，真直啊");
						os.write(outObject.toString().getBytes());
						String str = Read.read(con.getInputStream());
						System.out.println(str);

						handleServerMessage(str);
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						con.disconnect();
					}

					break;
				}
				case GET_BIG_PHOTO:

					if(bitmap != null){
						sendMessage(TAG, "bigPhotoOk");
					} else {
						con = ConnectionHandler.getGetConnect(detailUrl);
						try {
							bitmap = BitmapFactory.decodeStream(con.getInputStream());
							sendMessage(TAG, "bigPhotoOk");
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							con.disconnect();
						}
					}
				default: {
					break;
				}
			}
		}

		/**
		 * 解析从服务器得到的数据，将结果发给主线程
		 *
		 * @param msg 服务器得到的数据
		 */
		private void handleServerMessage(String msg) {

			JSONObject obj = null;
			if (msg == null) {
				return;
			}
			if (msg.startsWith("{")) {
				obj = new JSONObject(msg);
			}
			if (obj.has("status")) {
				String serverInfo = obj.getString("status");

				if (serverInfo.equals("SUCCESS")) {
					sendMessage(TAG, "success");
				} else if (serverInfo.equals("FAIL")) {
					sendMessage(TAG, "failed");
				} else if (serverInfo.equals("SQLERROR")) {
					sendMessage(TAG, "sql_error");
				} else if (serverInfo.equals("JSONFORMATERROR")) {
					sendMessage(TAG, "format_error");
				} else {
					sendMessage(TAG, "other_msg");
				}
			}
		}
	}


	/**
	 * 主线程的消息接受器
	 * 也可以用looper，但是掌握的不好
	 */
	private class MyHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {

			Bundle data = msg.getData();

			String message = data.getString(TAG);

			if ("bigPhotoOk".equals(message)) {
				if (bitmap != null) {
					mProgressBar.setVisibility(View.GONE);
					mImageView.setImageBitmap(bitmap);
				}
			} else if ("success".equals(message)) {
				Log.i(TAG, "like success!");
				if(isLike){
					likeNum += 1;
					mLikeNumText.setText(String.valueOf(likeNum));
					IndexFragment.sendMessage("fresh", "liked");
				} else {
					likeNum -= 1;
					mLikeNumText.setText(String.valueOf(likeNum));
					IndexFragment.sendMessage("fresh", "like");
				}
			} else if ("failed".equals(message)) {
				Log.i(TAG, "like failed!");
			} else if ("format_error".equals(message)) {
				Log.e(TAG, "format error, please check out !");
			} else if ("sql_error".equals(message)) {
				Log.e(TAG, "sql error, please check out !");
			} else if ("other_msg".equals(message)) {
				Log.e(TAG, "sub thread send the other_msg, please check out !");
			} else {
				Log.e(TAG, "sub thread send the bad message, please check out !");
			}
		}
	}

	/**
	 * 向本消息队列中放入消息，供主线程查询
	 *
	 * @param msgKey   消息键
	 * @param msgValue 消息值(数据)
	 */
	public static void sendMessage(String msgKey, String msgValue) {

		Bundle mBundle = new Bundle();
		Message mMessage = new Message();

		mBundle.putString(msgKey, msgValue);
		mMessage.setData(mBundle);

		myHandler.sendMessage(mMessage);
	}
}
