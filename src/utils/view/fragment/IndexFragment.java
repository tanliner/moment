package utils.view.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.htk.moment.ui.LaunchActivity;
import com.htk.moment.ui.PictureScanActivity;
import com.htk.moment.ui.R;
import come.htk.bean.IndexInfoBean;
import come.htk.bean.UserInfoBean;
import utils.android.sdcard.Read;
import utils.android.sdcard.Write;
import utils.internet.ConnectionHandler;
import utils.internet.UrlSource;
import utils.json.JSONArray;
import utils.json.JSONObject;
import utils.view.adapters.MyContentListViewAdapter;
import utils.view.view.CustomListView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * 主页Fragment
 *
 * @author tanlin
 *         time：14/11/26
 */
public class IndexFragment extends Fragment {

	public final static String TAG = "IndexFragment";

	public static BlockingQueue<IndexInfoBean> refreshQueue = new ArrayBlockingQueue<IndexInfoBean>(10);

	public static BlockingQueue<IndexInfoBean> loadQueue = new ArrayBlockingQueue<IndexInfoBean>(10);

	public static MyContentListViewAdapter listViewAdapter;

	private static ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();

	private static boolean theFirstTimeRefresh = true;
	private static boolean theFirstTimeLoadMore = true;


	private MyReceiver receiver;
	/**
	 * 模拟栈
	 */
	private BlockingQueue<HashMap<String, Object>> indexDequeStack = new ArrayBlockingQueue<HashMap<String, Object>>(1);


	public static MyHandler myHandler;

	private int loadMoreNum = 0;

	private int refreshNum = 0;

	private Context context;


	@Override
	public void onAttach(Activity activity) {
		context = getActivity();
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (myHandler == null) {
			myHandler = new MyHandler();
		}

		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.after_login_listview_layout, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		initListView();

	}

	@Override
	public void onStart() {

		super.onStart();
	}

	@Override
	public void onResume() {

		System.out.println("AppIndex aaaaa resume");
		super.onResume();

		refreshData("refresh");
		receiver = new MyReceiver();
		listViewAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		System.out.println("AppIndex aaaaa pause");
		super.onPause();
	}

	@Override
	public void onStop() {
		System.out.println("AppIndex aaaaa stop");

		super.onStop();
	}

	@Override
	public void onDestroyView() {

		super.onDestroyView();
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
	}

	@Override
	public void onDetach() {

		super.onDetach();
	}

	private CustomListView mCustomListView;

	public void initListView() {

		if(mCustomListView == null){

			mCustomListView = (CustomListView) this.getView().findViewById(R.id.index_pull_to_refresh_list_view);
			listViewAdapter = new MyContentListViewAdapter(context, items);

			/**
			 * 再次进入的时候，界面不匹配
			 */
//			SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
//			String date = format.format(new Date());
//			mCustomListView.onRefreshComplete(date);
//			mCustomListView.onLoadComplete();
		}

		mCustomListView.setAdapter(listViewAdapter);
		mCustomListView.setonRefreshListener(new CustomListView.OnRefreshListener() {

			@Override
			public void onRefresh() {
				Log.i(TAG, "onRefresh");
				refreshData("refresh");

			}
		});

		mCustomListView.setonLoadListener(new CustomListView.OnLoadListener() {

			@Override
			public void onLoad() {
				Log.i(TAG, "onLoad");
				refreshData("load");
			}
		});
	}


	private void refreshData(String way) {

		new freshThread(way).start();
	}

	public class freshThread extends Thread {

		private String way;

		public freshThread(String way) {

			this.way = way;
		}

		@Override
		public void run() {

			if (way.equals("refresh")) {
				internetRefresh();
			} else if (way.equals("load")) {
				internetLoad();
			} else {
				System.out.println("wrong fresh way !");
			}
		}
	}

	public static int rs_id = 0;

	private void internetRefresh() {

		HttpURLConnection connection = null;
		JSONObject outToServer = new JSONObject();

		try {
			// 取得一个连接 多 part的 connection
			connection = ConnectionHandler.getConnect(UrlSource.LOAD_STATUS, LaunchActivity.JSESSIONID);
			OutputStream out = connection.getOutputStream();
			outToServer.put("rs_id", rs_id);
			outToServer.put("before", true);

			Write.writeToHttp(out, outToServer.toString().getBytes());

			Log.i(TAG, "rs_id = " + rs_id + "  更新 服务器 返回码  -  " + connection.getResponseCode());

			String temp = Read.read(connection.getInputStream());
			if (temp == null) {
				return;
			}
			System.out.println("fresh server temp string = " + temp);
			if (temp.startsWith("[")) {
				// 分离子串
				String[] serverData = temp.split("]");
				IndexInfoBean indexBean;
				// 第一个是数组
				JSONArray array = new JSONArray(serverData[0] += "]");
				JSONObject status = new JSONObject(serverData[1]);
				if (!status.has("status")) {
					return;
				}

				if (!status.getString("status").equals("SUCCESS")) {
					Log.i(TAG, "status is not SUCCESS");
					return;
				}

				int length = array.length();
				Log.i(TAG, "本次 刷新 共有 " + length + "条消息");
				for (int i = 0; i < length; i++) {
					indexBean = new IndexInfoBean();
					JSONObject serverDataObj = array.getJSONObject(i);
					setObject(indexBean, serverDataObj);
					refreshQueue.put(indexBean);
					//sendMessage("fresh", "refresh_data_completed");
				}
				sendMessage("fresh", "ready_to_fresh_photo");
			} else if (temp.startsWith("{")) {
				JSONObject o = new JSONObject(temp);
				if (o.getString("status").equals("SESSIONERROR")) {
					sendMessage("fresh", "SESSIONERROR");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			// 写完一次，关闭连接，释放服务器资源
			if (connection != null) {
				connection.disconnect();
			}
		}

	}

	private void internetLoad() {

		HttpURLConnection connection = null;
		JSONObject object = new JSONObject();

		try {
			// 取得一个连接 多 part的 connection
			connection = ConnectionHandler.getConnect(UrlSource.LOAD_STATUS, LaunchActivity.JSESSIONID);
			OutputStream out = connection.getOutputStream();
			object.put("rs_id", 0);
			object.put("before", false);

			Write.writeToHttp(out, object.toString().getBytes());

			Log.i(TAG, "load  服务器返回码 -  " + connection.getResponseCode());

			String temp = Read.read(connection.getInputStream());
			if (temp == null) {
				return;
			}
			if (temp.startsWith("[")) {
				// 分离子串
				String[] serverData = temp.split("]");
				IndexInfoBean indexBean;
				// 第一个是数组
				JSONArray array = new JSONArray(serverData[0] += "]");
				JSONObject status = new JSONObject(serverData[1]);
				if (!status.has("status")) return;

				if (!status.getString("status").equals("SUCCESS")) {
					Log.i(TAG, "status is not SUCCESS");
				} else {
					int length = array.length();

					System.out.println("本次 加载 共有 " + length + "条消息");
					for (int i = 0; i < length; i++) {
						indexBean = new IndexInfoBean();
						JSONObject serverDataObj = array.getJSONObject(i);
						setObject(indexBean, serverDataObj);
						loadQueue.put(indexBean);
					}
					sendMessage("fresh", "ready_to_load_photo");
				}
			} else if (temp.startsWith("{")) {
				JSONObject o = new JSONObject(temp);
				if (o.getString("status").equals("SESSIONERROR")) {
					sendMessage("fresh", "SESSIONERROR");
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			// 写完一次，关闭连接，释放服务器资源
			if (connection != null) {
				connection.disconnect();
			}
		}
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
		if (obj.has("isLocated") && Boolean.valueOf(obj.getString("isLocated"))) {
			bean.setLocation(obj.getString("location"));
		}
		//        if (obj.has("location")) {
		//            bean.setLocation(obj.getJSONArray("location"));
		//        }
	}


	private class MyHandler extends Handler {

		//		QueueToStack refreshQueueToStackThread;
//		StackToUi stackToUiThread;
//		LoadQueueToStack loadQueueToStackThread;
		@Override
		public void handleMessage(Message msg) {

			Bundle dataBundle = msg.getData();
			String message = dataBundle.getString("fresh");
			if ("upLoadOk".equals(message)) {  //上传提示
				/**
				 * 真的可以更新界面了，
				 * 为了界面友好，考虑放一个processBar提示
				 */
				Toast.makeText(context, "上传成功", Toast.LENGTH_SHORT).show();
				listViewAdapter.notifyDataSetChanged();
				//直接刷新，不用用户手动刷新
				refreshData("refresh");

			} else if ("upLoadError".equals(message)) {
				Toast.makeText(context, "上传出错", Toast.LENGTH_SHORT).show();
				listViewAdapter.notifyDataSetChanged();
			} else if ("upLoadFormatError".equals(message)) {
				Toast.makeText(context, "upLoad Server Format Error", Toast.LENGTH_SHORT).show();
				listViewAdapter.notifyDataSetChanged();
			} else if ("upLoadNotDefine".equals(message)) {
				Toast.makeText(context, "upLoad Server Info Not Define", Toast.LENGTH_SHORT).show();
				listViewAdapter.notifyDataSetChanged();
			} else if ("refresh_data_completed".equals(message)) {
				/**
				 * 刷新完毕，服务器给出数据
				 * 客户端开启线程解析
				 *
				 * 1. 解析队列数据
				 *
				 * 2. 存入栈
				 *
				 * 头像：下一个环节处理，设计多个请求
				 */

			} else if ("ready_to_fresh_photo".equals(message)) {

				Log.i(TAG, "查找图片url");

				refreshNum = refreshQueue.size();

				if (theFirstTimeRefresh) {
//					refreshQueueToStackThread = new QueueToStack();
//					stackToUiThread = new StackToUi();
//
//					refreshQueueToStackThread.start();
//					stackToUiThread.start();

					new QueueToStack().start();
					new StackToUi().start();
				}
				if(refreshNum == 0){
					Toast.makeText(context, "已是最新数据", Toast.LENGTH_SHORT).show();
				}else {
					Toast.makeText(context, "本次有 " + refreshNum + "条新消息", Toast.LENGTH_SHORT).show();
				}

				SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
				String date = format.format(new Date());

				mCustomListView.onRefreshComplete(date);

				if(theFirstTimeRefresh){
					mCustomListView.first();
					theFirstTimeRefresh = false;
				}

			} else if ("ready_to_load_photo".equals(message)) {
				Log.i(TAG, "ready_to_load_photo");
				loadMoreNum = loadQueue.size();
				/**
				 * 准备去服务器搜索数据，与刷新同理
				 */
				if (theFirstTimeLoadMore) {
					theFirstTimeLoadMore = false;
					new LoadQueueToStack().start();
				}
				if (loadMoreNum == 0) {
					Toast.makeText(context, "已经显示所有数据", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(context, "本次加载" + loadMoreNum + "条消息", Toast.LENGTH_SHORT).show();
				}
				mCustomListView.onLoadComplete();
				listViewAdapter.notifyDataSetChanged();

			} else if ("sub_thread".equals(message)) {
				listViewAdapter.notifyDataSetChanged();
			} else if ("SESSIONERROR".equals(message)) {
				Toast.makeText(context, "SESSION 过期，请重新登录", Toast.LENGTH_SHORT).show();
			} else if ("receiver".equals(message)) {
				System.out.println("收到-----------------!!!!!!!!!!");

			} else if ("add_to_head".equals(message)) {
				Log.i(TAG, "add to head");
				items.add(0, map);
				listViewAdapter.notifyDataSetChanged();
				rs_id = (Integer) items.get(0).get("rs_id");

			} else if ("add_to_tail".equals(message)) {
				System.out.println("add to head");
				items.add(items.size(), map);

				listViewAdapter.notifyDataSetChanged();
				rs_id = (Integer) items.get(0).get("rs_id");

			} else if ("index_one_photo_ok".equals(message)) {
				mCustomListView.onLoadComplete();
				for (HashMap<String, Object> item : items) {
					tmpMap = item;
					if (tmpMap.get("rs_id").equals(indexInfo.getRs_id())) {
						tmpMap.put("userPicture", indexInfo.getPictureShow());
						listViewAdapter.notifyDataSetChanged();
					}
				}
			} else if("liked".equals(message)){

				for (HashMap<String, Object> item : items) {
					tmpMap = item;
					if (tmpMap.get("rs_id").equals(PictureScanActivity.getRs_id())) {
						int num = (Integer) tmpMap.get("likesNumber");
						tmpMap.put("likesNumber", (num + 1));
						listViewAdapter.notifyDataSetChanged();
					}
				}

			} else if("like".equals(message)){
				for (HashMap<String, Object> item : items) {
					tmpMap = item;
					if (tmpMap.get("rs_id").equals(PictureScanActivity.getRs_id())) {
						int num = (Integer) tmpMap.get("likesNumber");
						tmpMap.put("likesNumber", (num - 1));
						listViewAdapter.notifyDataSetChanged();
					}
				}
			} else {
				Log.e(TAG, "wrong message in bundle !");
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
					changeBeanToItems(bundle, false);

					indexBean = (IndexInfoBean) bundle.get("indexDataBean");

					url = indexBean.getViewPhoto();
					if (url.contains("mks")) {
						url = UrlSource.getUrl(url);

						photoConnection = ConnectionHandler.getGetConnect(url);
//						System.out.println("null url" + url);
						InputStream is = photoConnection.getInputStream();
						indexBean.setPictureShow(BitmapFactory.decodeStream(is));

						changeBeanToItems(bundle, true);
						bundle = null;
					} else {
						Log.e(TAG, "图片路径不规范 120.***+mks+moment");
					}

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

	private class LoadQueueToStack extends Thread {

		@Override
		public void run() {

			HttpURLConnection userInfoConnection;
			IndexInfoBean indexDataBean;
			UserInfoBean userInfo;
			while (true) {
				try {
					indexDataBean = loadQueue.take();
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

	HashMap<String, Object> map;
	HashMap<String, Object> tmpMap;
	UserInfoBean userInfo;
	IndexInfoBean indexInfo;

	/**
	 * @param bundle  refresh queue 已暂存的数据（hash map对象）
	 * @param photoOk 图片资源是否加载成功
	 */
	private void changeBeanToItems(HashMap<String, Object> bundle, boolean photoOk) {

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
				sendMessage("fresh", "add_to_head");
//				items.add(0, map);
			} else if (indexInfo.getRs_id() > (Integer) items.get(items.size() - 1).get("rs_id")) {
				sendMessage("fresh", "add_to_head");
//				items.add(0, map);
			} else {
				sendMessage("fresh", "add_to_tail");
//				items.add(items.size(), map);
			}
		} else {
			sendMessage("fresh", "index_one_photo_ok");
		}
//		rs_id = (Integer) items.get(0).get("rs_id");
	}

	class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("!!!!---------------" + intent.getStringExtra("test"));
		}
	}

}
