package utils.android.photo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import com.htk.moment.ui.R;
import come.htk.bean.PictureLibrarySelectBean;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * 用户从首页进入图库选择界面 可能进行的有加滤镜，直接上传图片
 * <p/>
 * Created by Administrator on 2014/11/21.
 */
public class LocalPictureLibrary extends Activity {

	public static final String TAG = "LocalPictureLibrary";

	// 已被选中的 图片（此时应该叫做缩略图） <图片位置，该图片所对应的路径>
	public static HashMap<Integer, String> photoSelectFlagMap = new HashMap<Integer, String>();

	private static GridAdapter gridAdapter;

	private GridView mGridView;

	private Button selectAll;

	private Button confirm = null;

	// 当前屏幕显示第一张图片，在整个GridView中的位置
	private int start = 0;

	// 屏幕所能显示的最后一张图片，在整个 GridView中的位置
	private int end = 0;

	private boolean refresh = false;

	// 是否点击全选
	private boolean isSelectAll = false;

	ImageLoader imageLoader;

	ArrayList<String> imageUris;

	private int userId;


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.to_choose_user_photo);
        init();

        selectSomePicture();
	}

	@Override
	protected void onStart() {

		super.onStart();
		gridAdapter.notifyDataSetChanged();
	}

	/**
	 * 初始化控件
	 */
	private void init() {

		userId = getIntent().getIntExtra("id", -1);
		mGridView = (GridView) findViewById(R.id.gridView);

		selectAll = (Button) findViewById(R.id.photo_select_all);
		confirm = (Button) findViewById(R.id.add_photo_confirm);

		mGridView.setHorizontalSpacing(1);
		mGridView.setVerticalSpacing(1);

		imageLoader = ImageLoader.getInstance(this);
		imageLoader.enable();
		imageUris = ImageLoader.photoPath;
		if (imageUris == null) {
			Log.e(TAG,"图片读取异常");
		} else {
            gridAdapter = new GridAdapter(this, imageUris);
        }
	}

	/**
	 * 从图库选择图片
	 */
	private void selectSomePicture() {

		mGridView.setAdapter(gridAdapter);

		selectAll.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {

				refresh = true;
				isSelectAll = !isSelectAll;
				ImageLoader.setPhotoSelect(isSelectAll);
				for (int i = 0; i < ImageLoader.selected.size(); i++) {
					mGridView.getItemAtPosition(i);
				}
				gridAdapter.notifyDataSetChanged();
			}
		});

		confirm.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent confirmUpload = new Intent(LocalPictureLibrary.this, UploadPhoto.class);
				// 只要全选按钮被点击，就应该刷新一遍
				if (refresh) {
					// 这样的目的是：在for循环中，不需要每次都获取map的长度，相对提高效率
					int length = ImageLoader.selected.size();
					for (int i = 0; i < length; i++) {
						boolean shouldPut = ImageLoader.selected.get(i);
						// 全选
						if (shouldPut) {
							photoSelectFlagMap.put(i, ImageLoader.photoPath.get(i));
						} else {
							//取消全选
							photoSelectFlagMap.remove(i);
						}
					}
				}
				// 一定不能直接写 PICTURE_ASK 传过去
				confirmUpload.putExtra("user_id", userId);
				confirmUpload.putExtra("multiple", photoSelectFlagMap);
				confirmUpload.putExtra("measure", "PICTURE_ASK");
				startActivity(confirmUpload);
			}
		});
		// 设置 适配器


		mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				boolean isSelected = ImageLoader.selected.get(position);
				// 原来没被选中，点击之后应该是被选中状态
				// 将此 Position 添加至 photoSelectFlagList中
				if (!isSelected) {
					ImageLoader.selected.put(position, true);
					view.findViewById(R.id.photo_is_select_text).setVisibility(View.VISIBLE);
					view.setBackgroundColor(R.color.picture_select);
					// 添加
					photoSelectFlagMap.put(position, ImageLoader.photoPath.get(position));
				} else {
					ImageLoader.selected.put(position, false);
					view.findViewById(R.id.photo_is_select_text).setVisibility(View.GONE);
					view.setBackgroundColor(Color.WHITE);
					// 从HashMap 中移除相应项
					photoSelectFlagMap.remove(position);
				}
			}
		});
		/**
		 * 屏幕划过一项，向阻塞线程发一个消息，加载当前屏幕数据
		 */
		mGridView.setOnScrollListener(new GridView.OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

				switch (scrollState) {
					// 当不滚动时（仅当屏幕没有滑动的时候才加载图片）
					case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
						// 滚动到显示区域底部
						try {
							// 如果队列中为空，向碎裂中添加数据（新需加载的位置信息）
							if (ImageLoader.FlagQueue.peek() == null) {
								HashMap<String, Integer> hashMap = new HashMap<String, Integer>();

								hashMap.put("start", start);
								hashMap.put("end", end + 3);
								ImageLoader.FlagQueue.put(hashMap);
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						break;
					// 滑动中(不加载图片)
					case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
						break;
					// 手指在屏幕上（不加载图片）
					case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
						break;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

				start = firstVisibleItem;
				end = start + visibleItemCount;
			}
		});

	}


	/**
	 * 自定义网格适配器
	 * <p/>
	 * 跟本地图库类似，将用户的所有图片网状显示在屏幕上
	 */
	private class GridAdapter extends BaseAdapter {

		private int width = ImageLoader.photoEachWidth;

		private LayoutInflater viewContainer;

		private ArrayList<String> photoPathList;

		public GridAdapter(Context context, ArrayList<String> list) {
			photoPathList = list;
			viewContainer = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {

			return photoPathList.size();
		}

		@Override
		public Object getItem(int position) {

			return photoPathList.get(position);
		}

		@Override
		public long getItemId(int position) {

			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			PictureLibrarySelectBean holder;
			if (null == convertView) {
				holder = new PictureLibrarySelectBean();
				convertView = viewContainer.inflate(R.layout.photo_choose_layout, null);
				holder.image = (ImageView) convertView.findViewById(R.id.image);
				holder.isSelect = (TextView) convertView.findViewById(R.id.photo_is_select_text);
				convertView.setTag(holder);
			} else {
				holder = (PictureLibrarySelectBean) convertView.getTag();
			}
			holder.image.setLayoutParams(new FrameLayout.LayoutParams(width, width));
			holder.image.setMinimumHeight(width);
			holder.image.setMinimumWidth(width);
			convertView.setPadding(2, 1, 2, 1);


			// 为新建的image 添加图片资源
			holder.image.setImageBitmap(ImageLoader.hashBitmaps.get(position));
			if (ImageLoader.selected.get(position)) {
				holder.isSelect.setVisibility(View.VISIBLE);
				convertView.setBackgroundColor(R.color.picture_select);
			} else {
				holder.isSelect.setVisibility(View.GONE);
				convertView.setBackgroundColor(Color.WHITE);
			}

			return convertView;
		}
	}


	private static Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			Bundle data = msg.getData();
			if ("error".equals(data.getString("load"))) {
				Log.w(TAG ,"--- 指定文件夹不存在！ 请检查！");
			}
			if ("yes".equals(data.getString("notify"))) {
				data.clear();
				gridAdapter.notifyDataSetChanged();
			}
		}
	};

	// 提供发消息方法，通知当前线程（主线程）
	public static void sendMessage(String key, String value) {

		Bundle data = new Bundle();
		Message msg = new Message();
		data.putString(key, value);
		msg.setData(data);
		handler.sendMessage(msg);
	}

}
