package utils.android.photo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.htk.moment.ui.AppIndexActivity;
import com.htk.moment.ui.LaunchActivity;
import com.htk.moment.ui.R;
import utils.android.sdcard.Read;
import utils.android.sdcard.Write;
import utils.check.Check;
import utils.internet.ConnectionHandler;
import utils.internet.PartFactory;
import utils.internet.UrlSource;
import utils.json.JSONArray;
import utils.json.JSONObject;
import utils.view.fragment.IndexFragment;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * function：上传图片至服务器
 * <p/>
 * 图片来源：相机，图库
 * 响应的涉及图片的路径，Intent传输的数据大小不能
 * 超过4K，一张图片明显大于4K，只有将图片的路径通
 * 过Intent传递消息。
 * <p/>
 * Created
 * by Administrator
 * on 2014/9/2.
 */
public class UploadPhoto extends Activity {

	public static final String TAG = "UploadPhoto";

	// 上传界面上的水平滚动条
	private LinearLayout liner;

	// 返回按钮（图片按钮）
	private ImageButton back;

	// 上传按钮
	private ImageButton uploadButton;

	private EditText mEditText;

	// 选择的图片路径集
	private ArrayList<String> pathArrayList;

	private Intent intent;

    private int userId;
	private String measure;

	/**
	 * 系统调用
	 *
	 * @param savedInstanceState 切换Activity时候系统需要保存的信息
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.upload_layout);
		pathArrayList = new ArrayList<String>();
		init();
		messageAway();
		listenerStart();

	}
	@Override
	protected void onResume() {

		super.onResume();
		//pathArrayList.clear();
		if(pathArrayList == null){
			pathArrayList = new ArrayList<String>();
		}
	}

	/**
	 * 初始化
	 * <p/>
	 * 得到Intent数据，初始化控件
	 */
	private void init() {

		intent = getIntent();
        userId = intent.getIntExtra("user_id", -1);
		liner = (LinearLayout) findViewById(R.id.liner);

		back = (ImageButton) findViewById(R.id.back_to_camera);
		uploadButton = (ImageButton) findViewById(R.id.goto_upload_button_img);
		mEditText = (EditText) findViewById(R.id.describe_of_user_picture_edit_text);
		mEditText.setCursorVisible(true);
	}

	/**
	 * 消息分发处理
	 * <p/>
	 * 如果是拍照进入的这个Activity，则由camera处理
	 * 如果是图库进入，则由picture处理
	 */
	private void messageAway() {

		// 什么类型触发的这个Intent
		measure = intent.getStringExtra("measure");
		// 得到照片的路径
		if (measure == null) {
			Log.e(TAG, "Intent send the wrong message here!");
		} else if ("PICTURE_ASK".equals(measure)) {
			pictureConfirm();
		} else if ("CAMERA_ASK".equals(measure)) {
			cameraConfirm();
		} else {
			Log.e("error", "Intent send the wrong message here，but It's impossible！");
		}
	}

	/**
	 * 从图库选择了图片（有可能很多张）
	 * 拍照不一样（应该只会有一张图片）
	 * 得到从 本地获取到的所有图片路径
	 * 注意：本HashMap中的key为图片在屏幕上的位置0-size，
	 * 因此在找路径的时候可能或混淆
	 * <p/>
	 * 注意：设置ImageView
	 * 高度与宽度的时候，我们不能直接得到他的LayoutParams
	 * 因为它根本没有在布局文件中出现，所以我们要设置它的LayoutParams，这个布
	 * 局参数是父容器的，然后再将父容器的布局参数传过去，然后设置相应宽度
	 */
	private void pictureConfirm() {

		Bitmap bitmap;
		ImageView image;
		String path;
		HashMap<Integer, String> hashMap;

		hashMap = (HashMap<Integer, String>) intent.getSerializableExtra("multiple");
		// 目前这样处理起来好像很费时间，算法有待改进

		/**
		 * 根据传过来的pathString，生成共浏览的图片
		 * 首先在缓存中查找，若不存在，再根据路径生成缩略图
		 */
		for (int temp : hashMap.keySet()) {
			// 动态生成ImageView，并设置器高度，乱服
			image = new ImageView(this);
			ViewGroup.LayoutParams parent = liner.getLayoutParams();
			image.setLayoutParams(parent);
			ViewGroup.LayoutParams params = image.getLayoutParams();
			params.width = 210;
			params.height = 210;
			image.setScaleType(ImageView.ScaleType.CENTER_CROP);
			image.setPadding(9, 20, 5, 15);

			// 直接在内存中查看，如果没有，则根据路径重新生成图片
			if (ImageLoader.hashBitmaps.containsKey(temp)) {
				image.setImageBitmap(ImageLoader.hashBitmaps.get(temp));
				pathArrayList.add(hashMap.get(temp));
			} else {
				path = hashMap.get(temp);
				// 此方法将得到图片的绝对路径，不包含由程序生成缩略图文件夹
				bitmap = BitmapFactory.decodeFile(path);
				image.setImageBitmap(ImageCompressUtil.zoomImage(bitmap, 210, 210));
				pathArrayList.add(path);
			}
			liner.addView(image);
		}
	}

	/**
	 * 如果照相机进如这个界面
	 * <p/>
	 * 但是，向服务器上传的图片不应该是SD卡里面的图片，
	 * 因为用户已经添加了，滤镜。
	 */
	private void cameraConfirm() {

		String photoPath = intent.getStringExtra("photoPath");
		if (photoPath == null) {
			Log.e("error", "Intent send the wrong message here!");
			return;
		}

		ImageView latestPhoto = new ImageView(this);
		ViewGroup.LayoutParams parent = liner.getLayoutParams();
		latestPhoto.setLayoutParams(parent);
		ViewGroup.LayoutParams params = latestPhoto.getLayoutParams();
		params.width = 210;
		params.height = 210;
		latestPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
		latestPhoto.setPadding(9, 20, 5, 15);
		latestPhoto.setImageBitmap(BitmapFactory.decodeFile(photoPath));

		liner.addView(latestPhoto);
		//
		pathArrayList.add(0, photoPath);
	}

	private void listenerStart() {

		buttonOnClickListening();
	}

	private void buttonOnClickListening() {
		// 返回按钮
		back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Runtime runtime = Runtime.getRuntime();
				try {
					runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		// 点击上传
		uploadButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				//如果用户已连接上Internet，就开启上传线程，并进入主界面（返回至登录时候的界面）
				if (Check.internetIsEnable(UploadPhoto.this)) {

					System.out.println("new thread=================");
					new UploadPhotoThread(pathArrayList).start();
				} else {
					//保存至本地，等到下次用户连接上internet的时候上传图片
					Log.w(TAG, "用户， 不在线， 应该保存到本地");
				}
				startActivity(new Intent(UploadPhoto.this, AppIndexActivity.class));
				finish();
			}
		});
	}

	//进行上传操作
	private class UploadPhotoThread extends Thread {

		private ArrayList<String> list;

		public UploadPhotoThread(ArrayList<String> paths) {

			list = paths;
		}

		@Override
		public void run() {
			//是不是可以直接加代码在这个地方
			//upLoadPhoto(list);

			upLoadPhotos(list);
		}
	}

	/**
	 *
	 * @param path 单张图片的路径
	 */
	private void UpdateOnePhoto(OutputStream out, String path, String partName, String fileName){
		try {

			DataOutputStream ds = new DataOutputStream(out);
			FileInputStream fin = new FileInputStream(path);

			int bufferSize = 1024;
			byte[] buffer = new byte[bufferSize];

			ds.write(PartFactory.PartBuilderHead(partName, fileName, "image/jpeg"));

			int len;
			while((len = fin.read(buffer)) != -1)
			{
				ds.write(buffer, 0, len);
			}
			ds.writeBytes(PartFactory.ENDLINE);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 将选中的图片（浏览框内所有的）上传
	 *
	 * @param paths 图片路径
	 */
	private void upLoadPhotos(ArrayList<String> paths){

		int listSize = paths.size();
		HttpURLConnection connection;

		connection = ConnectionHandler.getConnect(UrlSource.UPLOAD_PHOTO, LaunchActivity.JSESSIONID, true);
		OutputStream out;
		DataOutputStream ds;
		try {
			out = connection.getOutputStream();

			FileInputStream fin;

			int bufferSize = 1024;
			//第一个说明part
			Write.writeToHttp(out, createTheFirstPart());

			byte[] buffer = new byte[bufferSize];
			ds = new DataOutputStream(out);
			int len;
			System.out.println("total is" + listSize);
			for(int i = 0; i < listSize; i++){
				System.out.println("写第一张图片");
				fin = new FileInputStream(paths.get(i));

				//单个part所需要的头
				ds.write(PartFactory.PartBuilderHead("part" + i, "file" + i, "image/jpeg"));

				//每一个part的内容（文件内容）
				while((len = fin.read(buffer)) != -1) {
					ds.write(buffer, 0, len);
					ds.flush();
				}
				//part结束需要标记
				ds.write(PartFactory.ENDLINE.getBytes());
				System.out.println("写完一张");
			}

			ds.write(PartFactory.PartBuilder("end", "end", "text/plain", ("this time is uploaded" + listSize + "s photos").getBytes(), true));

			handleServerInfo(getServerResponseMessage(connection));


		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handleServerInfo(int msg){
		switch (msg){
			case 0:
				IndexFragment.sendMessage("fresh", "upLoadOk");
				break;
			case 1:
				IndexFragment.sendMessage("fresh", "upLoadError");
				break;
			case 2:
				IndexFragment.sendMessage("fresh", "upLoadFormatError");
				break;
			default:
				IndexFragment.sendMessage("fresh", "upLoadNotDefine");
				break;
		}
	}

	/**
	 * 从服务器获取处理信息
	 *
	 * @param connection 与服务器的连接
	 *
	 * @return Json字符串，未解析。
	 *
	 * @throws IOException 网络中断，会抛出异常
	 */
	private int getServerResponseMessage(HttpURLConnection connection) throws IOException {

		String temp = Read.read(connection.getInputStream());
		System.out.println("up date = temp" + temp);
		int ret;
		JSONObject serverHandledInfo = new JSONObject(temp);
		Log.i(TAG, "upload server string temp = " + temp);
		if(serverHandledInfo.has("status") ){
			if(serverHandledInfo.getString("status").equals("SUCCESS")){
				ret = 0;

			} else {
				ret = 1;
			}
		} else {
			Log.e(TAG, "服务器消息格式不正确\n");
			ret = 2;
		}
		return ret;
	}

	/**
	 * 构建post请求，此次传输，仅有一次
	 */
	private byte[] createTheFirstPart() {

		return PartFactory.PartBuilder("main_info", "dataInfo", "text/plain", createTheFirstContent(userId, "albumName1",
				"olderWords", getMyWorld(), getPhotoLocation(), "class1", getPhotoAtSomeOne(), getPhotoTopic()).getBytes());
	}

	/**
	 * 创建第一个part
	 * 格式跟后续的不一样，必须单独创建
	 *
	 * @param userId        用户ID
	 * @param albumName     专辑名
	 * @param olderWords    原来的描述
	 * @param myWords       现在的描述
	 * @param photoLocation 照片的位置信息
	 * @param photoClass    照片分类
	 * @param photoAt       指定通知某个好友
	 * @param photoTopic    此张照片表达的主题
	 *
	 * @return 已经构建好的JSON字符串
	 */
	private String createTheFirstContent(int userId, String albumName, String olderWords, String myWords, JSONArray photoLocation, String photoClass, JSONArray photoAt, JSONArray photoTopic) {

		JSONObject content = new JSONObject();
		// 用户名
		content.put("ID", userId);
		// 专辑名字
		//        content.put("albumName", albumName);
		//		content.put("olderWords", olderWords);
		content.put("myWords", myWords);
		//		content.put("photoLocation", photoLocation);
		//		content.put("photoClass", photoClass);
		//		content.put("photoAt", photoAt);
		//		content.put("photoTopic", photoTopic);
		return content.toString();
	}

	private JSONArray getPhotoLocation() {

		JSONArray array = new JSONArray();
		return array;
	}

	private JSONArray getPhotoAtSomeOne() {

		JSONArray array = new JSONArray();
		return array;
	}

	private JSONArray getPhotoTopic() {

		JSONArray array = new JSONArray();
		return array;
	}

	/**
	 * 构建后续part
	 *
	 * @param partName name
	 * @param fileName 文件(照片)名字
	 * @param content  内容
	 *
	 * @return 构建好的part字节数组
	 */
	private byte[] createAfterPart(String partName, String fileName, String contentType, byte[] content) {

		return PartFactory.PartBuilder(partName, fileName, contentType, content);
	}

	/**
	 * 构建后续part
	 *
	 * @param partName name
	 * @param fileName 文件(照片)名字
	 * @param content  内容
	 * @param end      是否是最后一个part
	 *
	 * @return 构建好的part字节数组
	 */
	private byte[] createAfterPart(String partName, String fileName, String contentType, byte[] content, boolean end) {

		return PartFactory.PartBuilder(partName, fileName, contentType, content, end);
	}

	private String getPartName() {

		String name = "";
		return name;
	}

	private String getFileName() {

		String fileName = "";
		return fileName;
	}

	private String getMyWorld() {
		return mEditText.getText().toString();
	}


	private byte[] getContent(ByteArrayOutputStream baos) {

		return baos.toByteArray();
	}
}
