package utils.view.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.htk.moment.ui.FollowActivity;
import com.htk.moment.ui.LaunchActivity;
import com.htk.moment.ui.R;
import come.htk.bean.IndexInfoBean;
import come.htk.bean.SmallPhotoBean;
import come.htk.bean.UserInfoBean;
import utils.android.sdcard.Read;
import utils.internet.ConnectionHandler;
import utils.internet.UrlSource;
import utils.json.JSONArray;
import utils.json.JSONObject;
import utils.view.adapters.MyContentListViewAdapter;
import utils.view.vertical.VerticalViewPager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * 个人中心，上下滑动页面
 * （本来应该是像通知栏那样上下拉的，留给第二版改进）
 *
 * @author Administrator 谭林
 *         <p/>
 *         time: 14/11/15
 */
public class MeFragment extends Fragment {

	public static String TAG = "MeFragment";

	public final boolean LOG = true;
	private int userId;

	private int rs_id = 100;
	private int FREASH = 0;

	private VerticalViewPager verticalViewPager;

	private UserHomeBefore before;
	private UserHomeAfter after;

	private ArrayList<SmallPhotoBean> smallPhotoBeanArrayList;

	private BlockingQueue<SmallPhotoBean> smallPhotoBeanBlockingQueue;

	private TextView mPhotoText;

	private TextView mPhotoNum;

	private TextView mFollowText;

	private TextView mFollowNum;

	private TextView mFansText;

	private TextView mFansNum;

	private ImageView mPlusImageView;

	private int photoNum;

	private int followNum;

	private int fansNum;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		return inflater.inflate(R.layout.user_home_index, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		init();
	}

	@Override
	public void onResume() {
		new MyGetThreeNumThread(userId).start();
		super.onResume();
	}

	private void init() {
		userId = getArguments().getInt("user_id");
		initVerticalPager();
		myHandler = new MyHandler();
	}

	private void initVerticalPager() {

		verticalViewPager = (VerticalViewPager) getView().findViewById(R.id.verticalViewPager);
		before = new UserHomeBefore();
		after = new UserHomeAfter();

		verticalViewPager.setAdapter(new FragmentPagerAdapter(getFragmentManager()) {

			@Override
			public Fragment getItem(int position) {

				if (position == 0) {
					return before;
				}
				return after;
			}

			@Override
			public int getCount() {

				return 2;
			}
		});

		verticalViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {

				/**
				 * 当前所在哪一页
				 * 第一页：主页的照片，关注，粉丝
				 */
				switch (position) {
					case 0:
						goToCommentDetail(userId);
						break;
					case 1:
						goToPhotoDetail();
						break;
				}
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});
		verticalViewPager.setCurrentItem(0);
	}

	/**
	 * 第一个页面
	 */
	private void goToCommentDetail(int id) {

		Log.i(TAG, "start the first page.");
		//new MyGetThreeNumThread(id).start();
	}

	/**
	 * 切换到第二个页面
	 */
	private void goToPhotoDetail() {

		Log.i(TAG, "start the second page.");
		// 后续 需增加rs_id参数
		//new MyGetSmallPhotoThread().start();

	}

	private class MyGetThreeNumThread extends Thread {

		private int id;

		public MyGetThreeNumThread(int id) {
			this.id = id;
		}

		@Override
		public void run() {

			if (hasThreeData(id)) {
				// （非UI线程）子线程是不能去更新界面
				sendMessage("MeFragment", "threeDataOk");
			}
		}
	}

	/**
	 * 查看照片 关注 粉丝 数量
	 *
	 * @return true 存在那三个数据
	 */
	private boolean hasThreeData(int id) {

		System.out.println("-------------添加-------------进入");

		HttpURLConnection connection = null;
		JSONObject objectI;
		JSONObject objectO = new JSONObject();
		String response = null;
		try {
			// 取得一个连接 多 part的 connection
			connection = ConnectionHandler.getConnect(UrlSource.GET_THREE_NUMBER, LaunchActivity.JSESSIONID);

			objectO.put("ID", id);

			connection.getOutputStream().write((objectO.toString()).getBytes());
			connection.getOutputStream().flush();
			Log.i(TAG, "server response code: " + connection.getResponseCode());

			String tmp = Read.read(connection.getInputStream());
			objectI = new JSONObject(tmp);

			if (objectI.has("status")) {
				response = objectI.getString("status");
			}

			// 说明有数据，正常查询状态
			if (response == null) {
				// 获取照片数量，关注人数，粉丝数量
				photoNum = objectI.getInt("photosNumber");
				followNum = objectI.getInt("fansNum");
				fansNum = objectI.getInt("followingsNum");

				return true;
			} else if (response.equals("SQLERROR")) {
				if (LOG) {
					System.out.println("server info : get three number sql error");
				}
				return false;
			} else if (response.equals("JSONFORMATERROR")) {
				if (LOG) {
					System.out.println("server info : get three number json format error");
				}
				return false;
			} else {
				if (LOG) {
					System.out.println("server info : get three number give me nothing");
				}
				return false;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// 写完一次，关闭连接，释放服务器资源
			if (connection != null) {
				connection.disconnect();
			}
		}
		return false;
	}

	private class MySmallPhotoPathThread extends Thread {

		int userId;
		int rs_id;

		public MySmallPhotoPathThread(int userId, int rs_id) {
			this.userId = userId;
			this.rs_id = rs_id;
		}

		@Override
		public void run() {

			HttpURLConnection smallConnection = ConnectionHandler.getConnect(UrlSource.GET_MORE_SMALL_PHOTO,
					LaunchActivity.JSESSIONID);

			try {
				OutputStream outToServer = smallConnection.getOutputStream();
				JSONObject outToServerDataObj = new JSONObject();
				outToServerDataObj.put("ID", userId);
				outToServerDataObj.put("rs_id", rs_id);
				outToServer.write(outToServerDataObj.toString().getBytes());

				/**
				 * 将得到的数据通过某种形式传递 给 grid View
				 */
				String tmp = Read.read(smallConnection.getInputStream());
				if (LOG) {
					Log.i(TAG, "缩略图信息: = " + tmp);
				}

				handleSmallPhoto(tmp);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * 将服务器返回的数据处理后抽象成对象放入队列
		 *
		 * @param msg 从服务器得到的消息
		 */
		private void handleSmallPhoto(String msg) {

			smallPhotoBeanBlockingQueue = new ArrayBlockingQueue<SmallPhotoBean>(10);

			JSONArray photos;

			if (msg == null) {
				return;
			}
			if (msg.startsWith("[")) {
				photos = new JSONArray(msg);

			} else if (msg.startsWith("{")) {
				JSONObject objTmp = new JSONObject(msg);
				if (objTmp.has("status")) {
					String status = objTmp.getString("status");
					if (status.equals("SQLERROR")) {
						if (LOG) {
							Log.e(TAG, "server SQLERROR");
						}
					} else if (status.equals("JSONFORMATERROR")) {
						if (LOG) {
							Log.e(TAG, "JSONFORMATERROR");
						}
					}
				}
				return;
			} else {
				return;
			}
			int length = photos.length();

			JSONObject objItem;
			SmallPhotoBean smallPhotoBean;

			/**
			 * 将得到的数据，存入队列
			 */
			if (LOG) {
				Log.i(TAG, "我的 长度  嗯？？？ = " + length);
			}
			for (int i = 0; i < length; i++) {
				objItem = photos.getJSONObject(i);

				smallPhotoBean = new SmallPhotoBean();

				if (objItem.has("ID")) {
					smallPhotoBean.setUserId(objItem.getInt("ID"));
				}
				if (objItem.has("rs_id")) {
					smallPhotoBean.setRs_id(objItem.getInt("rs_id"));
				}
				if (objItem.has("more_small_photo")) {
					smallPhotoBean.setAddrPath(objItem.getString("more_small_photo"));
				}
				if (objItem.has("album")) {
					smallPhotoBean.setAlbumName(objItem.getString("album"));
				}

				try {
					smallPhotoBeanBlockingQueue.put(smallPhotoBean);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			sendMessage("MeFragment", "pathOk");
			if (LOG) {
				System.out.println("获取 全部 路径 消息----");
			}
		}
	}

	/**
	 * 通过得到的图片路径去得到缩略图
	 */
	private class MyGetSmallPhotoThread extends Thread {

		SmallPhotoBean photoBean;
		HttpURLConnection photoConnection;
		String url;

		@Override
		public void run() {
			if (smallPhotoBeanArrayList == null) {
				smallPhotoBeanArrayList = new ArrayList<SmallPhotoBean>();
			}
			while (true) {
				try {
					photoBean = smallPhotoBeanBlockingQueue.take();

					url = photoBean.getAddrPath();
					if (url.contains("mks")) {
						url = UrlSource.getUrl(url);
					}

					photoConnection = ConnectionHandler.getGetConnect(url);
					InputStream is = photoConnection.getInputStream();

					photoBean.setBitmap(BitmapFactory.decodeStream(is));

					smallPhotoBeanArrayList.add(photoBean);

					sendMessage("MeFragment", "photoOk");

				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class MyGridViewAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		private ArrayList<SmallPhotoBean> beans;

		public MyGridViewAdapter(Context context, ArrayList<SmallPhotoBean> list) {

			super();
			mInflater = LayoutInflater.from(context);
			beans = list;
		}

		private class MImageView {
			ImageView mImageView;
		}

		@Override
		public int getCount() {

			return beans.size();
		}

		@Override
		public Object getItem(int position) {

			return null;
		}

		@Override
		public long getItemId(int position) {

			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			MImageView mImageView;

			if (convertView == null) {
				mImageView = new MImageView();
				convertView = mInflater.inflate(R.layout.my_small_image_lay, null);
				mImageView.mImageView = (ImageView) convertView.findViewById(R.id.my_small_image);
				convertView.setTag(mImageView);

			} else {
				mImageView = (MImageView) convertView.getTag();
			}
			Bitmap bitmap = beans.get(position).getBitmap();
			mImageView.mImageView.setScaleType(ImageView.ScaleType.CENTER);
			mImageView.mImageView.setPadding(2, 1, 2, 1);
			mImageView.mImageView.setImageBitmap(bitmap);
			return convertView;
		}
	}

	/**
	 * 点击 me 所显示的界面
	 * 主要包括自定义的背景图片，自己上传过的所有照片
	 * 数量 关注了什么人 被哪些人关注了等信息
	 */
	private class UserHomeBefore extends Fragment {

		// 不创建多个对象，将构造方法私有化
		private UserHomeBefore() {

		}

		//三个一般必须重载的方法
		@Override
		public void onCreate(Bundle savedInstanceState) {

			super.onCreate(savedInstanceState);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

			return inflater.inflate(R.layout.user_home_before, container, false);
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {

			super.onActivityCreated(savedInstanceState);
			initBeforeWidgets();
			//new MyGetThreeNumThread(userId).start();
		}

		@Override
		public void onStart() {

			super.onStart();
		}

		@Override
		public void onResume() {
			super.onResume();
			// 再次返回的时候，请求一次
//			new MyGetThreeNumThread(userId).start();
		}

		private void initBeforeWidgets() {

			mPhotoText = (TextView) getView().findViewById(R.id.user_home_photo_text);
			mFollowText = (TextView) getView().findViewById(R.id.user_home_follow_text);
			mFansText = (TextView) getView().findViewById(R.id.user_home_fans_text);

			mPlusImageView = (ImageView) getView().findViewById(R.id.imageView5);

			mPhotoNum = (TextView) getView().findViewById(R.id.user_home_photo_num);
			mFollowNum = (TextView) getView().findViewById(R.id.user_home_follow_num);
			mFansNum = (TextView) getView().findViewById(R.id.user_home_fans_num);

			mPhotoText.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					verticalViewPager.setCurrentItem(1);
				}
			});

			mPlusImageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					System.out.println("+ 号点击");
					new FollowIdThread().start();
				}
			});

			mFollowText.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					if (LOG) {
						System.out.println("点 击  关 注-----------");
					}
					/**
					 * Toast 弹出消息？
					 *
					 * 还是转换到Activity
					 *
					 */
					Intent intent = new Intent(getActivity(), FollowActivity.class);
					startActivity(intent);

				}
			});
		}

		private class FollowIdThread extends Thread {

			HttpURLConnection connection = null;
			String url = UrlSource.FOLLOW_BY_ID;

			@Override
			public void run() {
				try {
					connection = ConnectionHandler.getConnect(url, LaunchActivity.JSESSIONID);
					JSONObject obj = new JSONObject();
					obj.put("his_id", userId);
					if (LOG) {
						System.out.println("send the userId = " + userId);
					}
					connection.getOutputStream().write(obj.toString().getBytes());

					String msg = Read.read(connection.getInputStream());
					if (LOG) {
						System.out.println("response code = " + connection.getResponseCode());
					}
					handleMessage(msg);

				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (connection != null) {
						connection.disconnect();
					}
				}
			}
		}

		private void handleMessage(String msg) {

			JSONObject object = new JSONObject(msg);
			if (object.has("status")) {
				String status = object.getString("status");
				if (status.equals("JSONFORMATERROR")) {
					sendMessage("MeFragment", "gzJSONFORMATERROR");
				} else if (status.equals("SUCCESS")) {
					sendMessage("MeFragment", "gzSUCCESS");
				} else if (status.equals("SQLERROR")) {
					sendMessage("MeFragment", "gzSQLERROR");
				} else {
					sendMessage("MeFragment", "gzother");
				}
			}

		}


	}

	/**
	 * 在 me 界面向上滑动的时候所要显示的界面
	 * <p/>
	 * 主页：  里面所展现的是用户个人所发的所有照动态
	 * 缩略图：这些照片所生成的缩略图
	 * 地图信息：
	 * 用户本身喜欢的照片集合
	 */
	private class UserHomeAfter extends Fragment {

		private MyGridViewAdapter myGridViewAdapter;
		private MyContentListViewAdapter mMyContentListViewAdapter;

		private GridView mGridView;

		private ImageView mScanSelfImageView;

		private ImageView mShowAllPhotoImageView;

		private ListView mListView;

		//三个一般必须重载的方法
		// 私有化
		private UserHomeAfter() {

		}

		@Override
		public void onCreate(Bundle savedInstanceState) {

			super.onCreate(savedInstanceState);
		}

		@Override
		public void onPause() {

			super.onPause();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

			return inflater.inflate(R.layout.user_home_after, container, false);
		}

		@Override
		public void onActivityCreated(@Nullable Bundle savedInstanceState) {

			super.onActivityCreated(savedInstanceState);
			// 开启线程，获取缩略图
			if (LOG) {
				System.out.println("已开启缩略图线程 - user id = " + userId + "rs_id = " + rs_id);
			}
			initAfterWidgets();
			new MySmallPhotoPathThread(userId, rs_id).start();
		}

		private void initAfterWidgets() {
			mGridView = (GridView) getView().findViewById(R.id.user_home_photo_classes);
			mShowAllPhotoImageView = (ImageView) getView().findViewById(R.id.user_home_index_show_all);
			mScanSelfImageView = (ImageView) getView().findViewById(R.id.user_home_index_scan_self);
			mListView = (ListView) getView().findViewById(R.id.user_home_self_index_list_view);
			smallPhotoBeanArrayList = new ArrayList<SmallPhotoBean>();

			myGridViewAdapter = new MyGridViewAdapter(getActivity(), smallPhotoBeanArrayList);
			mMyContentListViewAdapter = new MyContentListViewAdapter(getActivity(), getListItems());

			mGridView.setAdapter(myGridViewAdapter);
			mGridView.setHorizontalSpacing(1);
			mGridView.setVerticalSpacing(1);


			mListView.setAdapter(mMyContentListViewAdapter);

			// 进入个人主页
			mScanSelfImageView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					mScanSelfImageView.setImageResource(R.drawable.user_home_index_scan_self_after_img);
					mShowAllPhotoImageView.setImageResource(R.drawable.user_home_index_show_all_before_img);

//					mListView.setAdapter(mMyContentListViewAdapter);

					//startActivity(new Intent(getActivity(), UserOnlyHimselfActivity.class));
					new LoadStatusById(userId).start();
					mListView.setVisibility(View.VISIBLE);
					mGridView.setVisibility(View.GONE);


				}
			});

			mShowAllPhotoImageView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					mShowAllPhotoImageView.setImageResource(R.drawable.user_home_index_show_all_after_img);
					mScanSelfImageView.setImageResource(R.drawable.user_home_index_scan_self_before_img);
					mListView.setVisibility(View.GONE);
					mGridView.setVisibility(View.VISIBLE);
				}
			});
		}

		private List<HashMap<String, Object>> getListItems() {
			if(items == null){
				items = new ArrayList<HashMap<String, Object>>();
			}
			return items;
		}

		private class LoadStatusById extends Thread {

			private HttpURLConnection connection = null;
			private int user;

			public LoadStatusById(int id) {
				user = id;
			}

			@Override
			public void run() {
				connection = ConnectionHandler.getConnect(UrlSource.LOAD_STATUS_BY_ID, LaunchActivity.JSESSIONID);
				JSONObject objOut = new JSONObject();
				objOut.put("rs_id", FREASH);
				objOut.put("before", true);
				objOut.put("ID", user);

				try {
					connection.getOutputStream().write(objOut.toString().getBytes());
					String str = Read.read(connection.getInputStream());
					System.out.println("id = " + userId + "加载 状态 by ID = " + str);
					handleMsg(str);

				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (connection != null) {
						connection.disconnect();
					}
				}

			}
			private void handleMsg(String msg){

				if(msg.startsWith("[")) {

					JSONArray array = new JSONArray(msg);
					int length = array.length();

					if(length == 0){
						JSONObject statusObj = new JSONObject(msg.substring(2));
						if(statusObj.has("status")){
							String status = statusObj.getString("status");
							if(status.equals("SUCCESS")){
								sendMessage("MeFragment", "jzSUCCESS");
							}else if(status.equals("SQLERROR")){
								sendMessage("MeFragment", "jzSQLERROR");
							}else if(status.equals("JSONFORMATERROR")){
								sendMessage("MeFragment", "jzJSONFORMATERROR");
							}else {
								sendMessage("MeFragment", "jzother");
							}
						}
						return;
					}
					IndexInfoBean indexInfoBean;
					for (int i = 0; i < length; i++) {
						JSONObject one = array.getJSONObject(i);
						indexInfoBean = new IndexInfoBean();

						setObject(indexInfoBean, one);
						try {
							refreshQueue.put(indexInfoBean);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					sendMessage("MeFragment", "putPathCompleted");
				}
			}
		}

	}


	private ArrayList<HashMap<String, Object>> items;
	public static BlockingQueue<IndexInfoBean> refreshQueue = new ArrayBlockingQueue<IndexInfoBean>(10);
	/**
	 * 模拟栈
	 */
	private BlockingQueue<HashMap<String, Object>> indexDequeStack = new ArrayBlockingQueue<HashMap<String, Object>>(1);


	/**
	 * 开启线程充队列中获取数据，并解析存入栈中
	 * 因为得到的是最新数据
	 * 应该显示到屏幕的最顶部
	 */
	private class QueueToStack extends Thread {

		@Override
		public void run() {

			HttpURLConnection userInfoConnection;
			IndexInfoBean indexDataBean;
			UserInfoBean userInfo;
			while (true) {
				try {
					indexDataBean = refreshQueue.take();
					userInfo = new UserInfoBean();

					userInfo.setID(indexDataBean.getId());

					userInfoConnection = ConnectionHandler.getConnect(UrlSource.GET_USER_INFO, LaunchActivity.JSESSIONID);

					JSONObject userOut = new JSONObject();
					JSONObject userObj;

					userInfoConnection.getOutputStream();
					userOut.put("ID", userInfo.getID());

					userInfoConnection.getOutputStream().write(userOut.toString().getBytes());

					String inString = Read.read(userInfoConnection.getInputStream());
					userObj = new JSONObject(inString);
					if (userObj.has("name")) {
						userInfo.setName(userObj.getString("name"));
					}

					HashMap<String, Object> indexNameBundle = new HashMap<String, Object>();
					indexNameBundle.put("indexDataBean", indexDataBean);
					indexNameBundle.put("userInfoBean", userInfo);
					// 存入栈
					indexDequeStack.put(indexNameBundle);

				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 阻塞栈，（/队列）
	 * <p/>
	 * 将“栈”里面的数据提取出来，获取图片
	 */
	private class StackToUi extends Thread {

		IndexInfoBean indexBean;

		private HashMap<String, Object> bundle;

		HttpURLConnection photoConnection = null;

		String url;

		@Override
		public void run() {

			while (true) {
				try {

					bundle = indexDequeStack.take();
					System.out.println("取出 一条数据==========");
					changeBeanToItems(bundle, false);

					sendMessage("MeFragment", "indexNameOk");

					indexBean = (IndexInfoBean) bundle.get("indexDataBean");

					url = indexBean.getViewPhoto();
					if (url.contains("mks")) {
						url = UrlSource.getUrl(url);
					}
					photoConnection = ConnectionHandler.getGetConnect(url);
					InputStream is = photoConnection.getInputStream();
					indexBean.setPictureShow(BitmapFactory.decodeStream(is));

					changeBeanToItems(bundle, true);
					bundle = null;
					sendMessage("MeFragment", "indexPhotoOk");

				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (photoConnection != null) {
						photoConnection.disconnect();
					}
				}
			}
		}
	}

	private void changeBeanToItems(HashMap<String, Object> bundle, boolean photoOk) {

		UserInfoBean userInfo;
		IndexInfoBean indexInfo;
		HashMap<String, Object> map;
		indexInfo = (IndexInfoBean) bundle.get("indexDataBean");
		userInfo = (UserInfoBean) bundle.get("userInfoBean");

		if (!photoOk) {
			map = new HashMap<String, Object>();
			// 用户
			map.put("id", userInfo.getID());
			map.put("userName", userInfo.getName());
			map.put("userAddress", "呼伦贝尔草原");
			// 资源
			map.put("rs_id", indexInfo.getRs_id());
			map.put("sharesNumber", indexInfo.getSharesNumber());
			map.put("commentsNumber", indexInfo.getCommentNumber());
			map.put("likesNumber", indexInfo.getLikeNumber());
			map.put("myWords", indexInfo.getMyWords());
			map.put("time", indexInfo.getTime());
			map.put("album", indexInfo.getAlbum());

			map.put("viewPhoto", indexInfo.getViewPhoto());
			map.put("detailPhoto", indexInfo.getDetailPhoto());
			if (indexInfo.getIsLocated().equals("true")) {
				map.put("location", indexInfo.getLocation());
			}
			if (items.size() == 0) {
				items.add(0, map);
			} else if (indexInfo.getRs_id() > (Integer) items.get(items.size() - 1).get("rs_id")) {
				items.add(0, map);
			} else {
				items.add(items.size(), map);
			}
		} else {
			for (HashMap<String, Object> item : items) {
				map = item;
				if (map.get("rs_id").equals(indexInfo.getRs_id())) {
					map.put("userPicture", indexInfo.getPictureShow());
				}
			}
		}

		//IndexPullRefreshListView.rs_id = (Integer) items.get(0).get("rs_id");
	}


	private void setObject(IndexInfoBean bean, JSONObject obj) {

		if (obj.has("ID")) {
			bean.setId(obj.getInt("ID"));
		}
		if (obj.has("rs_id")) {
			bean.setRs_id(obj.getInt("rs_id"));
		}
		if (obj.has("detailPhoto")) {
			bean.setDetailPhoto(obj.getString("detailPhoto"));
		}
		if (obj.has("isLocated")) {
			bean.setIsLocated(obj.getString("isLocated"));
		}
		if (obj.has("sharesNumber")) {
			bean.setSharesNumber(obj.getInt("sharesNumber"));
		}
		if (obj.has("myWords")) {
			bean.setMyWords(obj.getString("myWords"));
		}
		if (obj.has("commentsNumber")) {
			bean.setCommentNumber(obj.getInt("commentsNumber"));
		}
		if (obj.has("likesNumber")) {
			bean.setLikeNumber(obj.getInt("likesNumber"));
		}
		if (obj.has("time")) {
			bean.setTime(obj.getString("time"));
		}
		if (obj.has("viewPhoto")) {
			bean.setViewPhoto(obj.getString("viewPhoto"));
		}
		if (obj.has("hasDetail")) {
			bean.setHasDetail(obj.getString("hasDetail"));
		}
		if(obj.has("isLocated") && Boolean.valueOf(obj.getString("isLocated"))){
			bean.setLocation(obj.getString("location"));
		}
		//        if (obj.has("location")) {
		//            bean.setLocation(obj.getJSONArray("location"));
		//        }
	}

	private boolean firstTime = true;

	/**
	 * 消息接受器
	 */
	private class MyHandler extends Handler {

		Bundle mData;

		@Override
		public void handleMessage(Message msg) {

			mData = msg.getData();

			String msgFlag = mData.getString("MeFragment");

			if ("threeDataOk".equals(msgFlag)) {
				mPhotoNum.setText(String.valueOf(photoNum));
				mFollowNum.setText(String.valueOf(followNum));
				mFansNum.setText(String.valueOf(fansNum));

			} else if ("pathOk".equals(msgFlag)) {
				// 路径全部获取到之后，开启获取缩略图线程得到图片
				new MyGetSmallPhotoThread().start();

			} else if ("photoOk".equals(msgFlag)) {

				Log.i(TAG, "通知缩略图 GRID VIEW 刷新");
				// 将得到的图片显示到GridView、上
				after.myGridViewAdapter.notifyDataSetChanged();
			} else if ("gzJSONFORMATERROR".equals(msgFlag)) {
				if (LOG) {
					Log.e(TAG, "gzJSONFORMATERROR");
				}
			} else if ("gzSUCCESS".equals(msgFlag)) {
				Toast.makeText(getActivity(), "关注成功", Toast.LENGTH_SHORT).show();
				if (LOG) {
					Log.i(TAG, "gzSUCCESS");
				}
			} else if ("gzSQLERROR".equals(msgFlag)) {
				if (LOG) {
					Log.e(TAG, "gzSQLERROR");
				}
			} else if ("gzother".equals(msgFlag)) {
				if (LOG) {
					Log.i(TAG, "gz sub thread send some bad message!");
				}
			} else if("jzJSONFORMATERROR".equals(msgFlag)){
				if(LOG){
					Log.i(TAG, "jzJSONFORMATERROR");
				}
			}else if("jzSUCCESS".equals(msgFlag)){
				if(LOG){
					Log.i(TAG, "jzSUCCESS");
				}
			}else if("jzSQLERROR".equals(msgFlag)){
				if(LOG){
					Log.i(TAG, "jzSQLERROR");
				}
			}else if("jzother".equals(msgFlag)){
				if(LOG){
					Log.i(TAG, "jzother");
				}
			} else if("putPathCompleted".equals(msgFlag)){

				if(firstTime){
					new QueueToStack().start();
					new StackToUi().start();
					firstTime = false;
				}

			} else if("indexNameOk".equals(msgFlag)){
				System.out.println("name  刷新");
				after.mMyContentListViewAdapter.notifyDataSetChanged();

			} else if("indexPhotoOk".equals(msgFlag)){
				System.out.println("photo 刷新");
				after.mMyContentListViewAdapter.notifyDataSetChanged();
			}else {
				Log.e(TAG, "sub thread send the bad message， please check out !");
			}
		}
	}

	private static MyHandler myHandler;

	/**
	 * @param key   主键
	 * @param value （消息）值
	 */
	public static void sendMessage(String key, String value) {

		Bundle dataBundle = new Bundle();

		Message dataMessage = new Message();

		dataBundle.putString(key, value);
		dataMessage.setData(dataBundle);

		myHandler.sendMessage(dataMessage);
	}
}
