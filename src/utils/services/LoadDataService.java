package utils.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import utils.Test;
import utils.json.JSONObject;
import utils.view.fragment.IndexFragment;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * 请求数据
 * Created by Administrator on 2014/11/21.
 */
public class LoadDataService extends Service {


	//private
//	MyBinder myBinder = new MyBinder();


	Intent intent;
//	public HashMap<String, Object> map = new HashMap<String, Object>();
	public ArrayList<HashMap<String, Object>> listMap = new ArrayList<HashMap<String, Object>>();
	public class MyBinder extends Binder {

		public LoadDataService getService() {

			return LoadDataService.this;
		}
	}

	@Override
	public void onCreate() {

		intent = new Intent(Test.acition);
		getMessage();
		super.onCreate();
		//final String ACTION = "utils.view.fragment.IndexFragment.receiver";
		System.out.println("------- create -------");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		System.out.println("------- start -------");
		//getMessage();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {

		System.out.println("------- on bind -------");

		return new MyBinder();
	}

	@Override
	public boolean onUnbind(Intent intent) {


		return super.onUnbind(intent);
	}

	public ArrayList<HashMap<String, Object>> function() {

		HashMap<String, Object> dataMap = new HashMap<String, Object>();

		dataMap.put("id", 100);
		dataMap.put("userName", "test");
		dataMap.put("userAddress", "呼伦贝尔草原");
		// 资源
		dataMap.put("rs_id", 1200);
		dataMap.put("sharesNumber", 0);
		dataMap.put("commentsNumber", 0);
		dataMap.put("likesNumber", 0);
		dataMap.put("myWords", "words");
		dataMap.put("time", "time");
		dataMap.put("album", "album");

		dataMap.put("viewPhoto", "");
		dataMap.put("detailPhoto", "");



		listMap.add(dataMap);
		return listMap;
	}

	private void getMessage() {
		new NotifyThread().start();
	}
	private class NotifyThread extends Thread {
		@Override
		public void run() {
//
//			HttpURLConnection connection = null;
//			try {
//				connection = ConnectionHandler.getConnect(UrlSource.LOAD_NOTIFY, LaunchActivity.JSESSIONID);
//
//
//				String str = Read.read(connection.getInputStream());
//				System.out.println("str = " + str);
//
//			} catch (IOException e) {
//				e.printStackTrace();
//			}

			try {
				Thread.sleep(2000);

				function();
				System.out.println("睡眠 2 秒 后 发送广播，接收到？？？？");
				intent.putExtra("test", "value");
				IndexFragment.sendMessage("fresh", "receiver");
				//sendBroadcast(intent);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}


	private String handleSerMsg(String msg){
		if(msg.startsWith("{")){
			JSONObject obj = new JSONObject();
			if(obj.has("status") && obj.getString("status").equals("NODOTA")){
				return "nodata";
			}
		}
		return "";
	}

}
