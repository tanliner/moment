package com.htk.moment.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import come.htk.bean.FollowBean;
import utils.android.AppManager;
import utils.android.sdcard.Read;
import utils.internet.ConnectionHandler;
import utils.internet.UrlSource;
import utils.json.JSONArray;
import utils.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * Created by Administrator on 2014/12/17.
 */
public class FollowActivity extends Activity {

	private static String TAG = "FollowActivity";

    public static boolean LOG = true;

	private ListView mFollowListView;

	private static MyAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		AppManager.getAppManager().addActivity(this);
		setContentView(R.layout.follow);
		initView();
        startListen();
	}

	private void initView() {

		mFollowListView = (ListView) findViewById(R.id.listView);
		new GetFollowBodyThread().start();
		createAHandler();
		adapter = new MyAdapter(FollowActivity.this, getItems());
		mFollowListView.setAdapter(adapter);
	}

	private ArrayList<HashMap<String, Object>> items;

	private class GetFollowBodyThread extends Thread {

		@Override
		public void run() {

			getFollowMan();
		}
	}

    private class GetUserInfoThread extends Thread {
        int id;
        public GetUserInfoThread(int id){
            this.id = id;
        }
        @Override
        public void run() {

            getUserInfo(id);
        }
    }
	private ArrayList<HashMap<String, Object>> getItems() {

		if (items == null) {
			items = new ArrayList<HashMap<String, Object>>();
		}
		return items;
	}

	private void getFollowMan() {

		HttpURLConnection connection = null;

		try {
			connection = ConnectionHandler.getConnect(UrlSource.GET_FOLLOWINGS_INFO, LaunchActivity.JSESSIONID);

			String temp = Read.read(connection.getInputStream());
			if (temp == null) {
				return;
			}
            if(temp.startsWith("{") && temp.endsWith("}")){
                JSONObject objTmp = new JSONObject(temp);

                if(objTmp.has("status") && "NODATA".equals(objTmp.getString("status"))){
                    sendMessage("FollowActivity", "ThreeDataNull");
                }
                return ;
            }

            if(LOG)
                System.out.println("following = " + temp);

            JSONArray followData = new JSONArray(temp);

			int length = followData.length();
			FollowBean follow;
			JSONObject obj;

			for (int i = 0; i < length; i++) {
				follow = new FollowBean();
				obj = followData.getJSONObject(i);
				follow.setId(obj.getInt("ID"));
				follow.setName(obj.getString("name"));

				msgQueue.put(follow);
			}
			sendMessage("FollowActivity", "ThreeDataOk");

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

	private BlockingQueue<FollowBean> msgQueue = new ArrayBlockingQueue<FollowBean>(10);


	private class MyAdapter extends BaseAdapter {

		private ArrayList<HashMap<String, Object>> mMaps;

		private LayoutInflater inflater;

		public MyAdapter(Context context, ArrayList<HashMap<String, Object>> maps) {

			super();
			mMaps = maps;
			inflater = LayoutInflater.from(context);
		}

		private class ViewHolder {

			TextView id;

			TextView name;
		}

		@Override
		public int getCount() {

			return mMaps.size();
		}

		@Override
		public Object getItem(int position) {

			return mMaps.get(position);
		}

		@Override
		public long getItemId(int position) {

			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = inflater.inflate(R.layout.follow_items, null);

				viewHolder.id = (TextView) convertView.findViewById(R.id.follow_id);
				viewHolder.name = (TextView) convertView.findViewById(R.id.follow_name);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder.id.setText(String.valueOf(mMaps.get(position).get("ID")));
			viewHolder.name.setText((CharSequence) mMaps.get(position).get("name"));

			return convertView;
		}
	}

	private class MyHandler extends Handler {

		Bundle mData;

		@Override
		public void handleMessage(Message msg) {

			mData = msg.getData();
            String flag = mData.getString("FollowActivity");

			if ("oneDataOk".equals(flag)) {
				adapter.notifyDataSetChanged();
			} else if ("ThreeDataOk".equals(flag)) {
				int length = msgQueue.size();
				for(int i = 0; i < length; i++){
					putDataToList();
				}
			} else if("ThreeDataNull".equals(flag)) {
//                Log.e(TAG, "This User have no message for current item.");
                Toast.makeText(getApplicationContext(), "你没有关注任何人", Toast.LENGTH_SHORT).show();
			} else {
                if(LOG)
                System.out.println("!!!!!!" + flag);
            }
		}
	}

	static MyHandler myHandler;

	private void createAHandler() {

		myHandler = new MyHandler();
	}
	private void putDataToList(){
		FollowBean bean;
		try {
			bean = msgQueue.take();
			// 队列取数据
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("ID", bean.getId());
			map.put("name", bean.getName());
			items.add(map);
			sendMessage("FollowActivity","oneDataOk");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

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

    private boolean getting = false;
    public void startListen(){
        mFollowListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(LOG) {
                 TextView t = (TextView) view.findViewById(R.id.follow_id);
                    System.out.println("clink- = "+ t.getText() + " " + getting);
                }
                if(!getting){
                    TextView idTextView = (TextView) view.findViewById(R.id.follow_id);
                    int trueId = Integer.valueOf(idTextView.getText().toString());
                    new GetUserInfoThread(trueId).start();
                    getting = true;
                }
            }
        });
    }
    private void getUserInfo(int id) {

        HttpURLConnection connection = null;

        try {
            connection = ConnectionHandler.getConnect(UrlSource.GET_USER_INFO, LaunchActivity.JSESSIONID);
            JSONObject outObj = new JSONObject();
            outObj.put("ID", id);
            connection.getOutputStream().write(outObj.toString().getBytes());


            String temp = Read.read(connection.getInputStream());
            if(temp == null){
                return;
            }
            JSONObject inObj = new JSONObject(temp);
            if(inObj.has("status")) {
                if (inObj.getString("status").equals("SQLERROR")) {
                    Log.e(TAG, "server SQLERROR happened, please checkout.");
                } else if(inObj.getString("status").equals("JSONFORMATERROR")){
                    Log.e(TAG, "client JSON format error, please checkout.");
                } else {
                    Log.e(TAG, "some undefined error, please checkout.");
                }
            } else {
                Log.i(TAG, "---------in thread this ID's info " + temp);
                Intent intent = new Intent(FollowActivity.this, OtherPeopleIndex.class);
                intent.putExtra("userId", id);
                startActivity(intent);
                getting = false;
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(connection == null){
                connection.disconnect();
            }
        }
    }


}
