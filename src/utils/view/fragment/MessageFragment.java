package utils.view.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.htk.moment.ui.R;
import utils.view.view.CircleImageView;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * 消息中心
 *
 * 一个大容器，里面放有小容器，ViewPager
 *
 * @author Administrator tanlin
 */
public class MessageFragment extends Fragment {

	private ViewPager mViewPager;

	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View convertView = inflater.inflate(R.layout.message_index, container, false);
		initViews(convertView);

		mViewPager.setAdapter(new android.support.v4.app.FragmentStatePagerAdapter(getChildFragmentManager()) {

			@Override
			public Fragment getItem(int position) {
				currentPosition = position;
				if (position == 0) {
					return NoticeFrag.getFragment();
				}
				return PrivateContentFrag.getFragment();
			}
			@Override
			public int getCount() {

				return 2;
			}
		});
		mViewPager.setCurrentItem(0, false);

		return convertView;

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		testButton();
	}

	@Override
	public void onStart() {

		super.onStart();
	}

	@Override
	public void onResume() {

		super.onResume();
	}

	@Override
	public void onPause() {

		super.onPause();
	}

	@Override
	public void onStop() {

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

	private static int currentPosition = 0;
	private void changeView(){
		if(currentPosition == 0){
			mViewPager.setCurrentItem(1);
			return;
		}
		setPositionView(0);
	}

	private void setPositionView(int position){
		mViewPager.setCurrentItem(position);
	}

	private Button mNoticeButton;
	private Button mPrivateNoticeButton;


	private void initViews(View v){

		mNoticeButton = (Button) v.findViewById(R.id.message_index_notice_button);
		mPrivateNoticeButton = (Button) v.findViewById(R.id.message_index_private_notice_button);
		mViewPager = (ViewPager) v.findViewById(R.id.index_message_content_page);
	}



	/**
	 * 通知容器
	 */
	private final static class NoticeFrag extends Fragment {

		// 不创建多个对象，将构造方法私有化
		private static NoticeFrag mNoticeFrag;
		private NoticeFrag() {

		}

		public static Fragment getFragment() {

			if (null == mNoticeFrag) {
				mNoticeFrag = new NoticeFrag();
			}
			return mNoticeFrag;
		}
		//三个一般必须重载的方法
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
			View convertView = inflater.inflate(R.layout.message_index_page_notice_list, container, false);
			ListView mListView = (ListView) convertView.findViewById(R.id.notice_list_view);
			mListView.setAdapter(new NoticeAdapter(getActivity(), MessageFragment.getSomeStaticData()));

			return convertView;
		}
	}

	/**
	 * 私信容器
	 */
	private final static class PrivateContentFrag extends Fragment {
		//三个一般必须重载的方法

		private static PrivateContentFrag mPrivateContentFrag;
		private PrivateContentFrag() {

		}
		public static Fragment getFragment() {

			if (mPrivateContentFrag == null) {
				mPrivateContentFrag = new PrivateContentFrag();
			}
			return mPrivateContentFrag;
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
			View convertView = inflater.inflate(R.layout.message_index_page_private_list, container, false);
			ListView mListView = (ListView) convertView.findViewById(R.id.private_list_view);
			mListView.setAdapter(new PrivateMessageAdapter(getActivity(), MessageFragment.getSomeStaticData()));

			return convertView;
		}
	}





	/**
	 * 通知页适配器
	 */
	private static class NoticeAdapter extends BaseAdapter {

		private ArrayList<HashMap<String, Object>> mData;
		private LayoutInflater mInflater;


		private class MyListViewHolder {

			CircleImageView mCircleImagePhotoHead;
			TextView mUserName;
			TextView mComment;
			TextView mTimeOfComment;
			ImageView mUserPisture;
		}

		public NoticeAdapter(Context context, ArrayList<HashMap<String, Object>> maps) {

			mData = maps;
			mInflater = LayoutInflater.from(context);
		}
		@Override
		public int getCount() {

			return mData.size();
		}
		@Override
		public Object getItem(int position) {

			return mData.get(position);
		}
		@Override
		public long getItemId(int position) {

			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			MyListViewHolder mMyListViewHolder;
			if (convertView == null) {
				mMyListViewHolder = new MyListViewHolder();
				convertView = mInflater.inflate(R.layout.message_index_page_notice_content, null);
				mMyListViewHolder.mCircleImagePhotoHead = (CircleImageView) convertView.findViewById(R.id.message_index_user_photo_head);
				mMyListViewHolder.mComment = (TextView) convertView.findViewById(R.id.message_index_comment_text);
				mMyListViewHolder.mTimeOfComment = (TextView) convertView.findViewById(R.id.message_index_time_of_comment);
				mMyListViewHolder.mUserName = (TextView) convertView.findViewById(R.id.message_index_notice_user_name);
				mMyListViewHolder.mUserPisture = (ImageView) convertView.findViewById(R.id.message_index_picture_commented_by_someone);
				convertView.setTag(mMyListViewHolder);
			} else {
				mMyListViewHolder = (MyListViewHolder) convertView.getTag();
			}
			mMyListViewHolder.mCircleImagePhotoHead.setImageResource(R.drawable.head2);
			mMyListViewHolder.mUserPisture.setImageResource(R.drawable.application_launch_back);

			return convertView;
		}
	}
	/**
	 * @return 带有静态数据的listMap
	 */
	private static ArrayList<HashMap<String, Object>> getSomeStaticData() {

		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> dataMap;
		for (int i = 0; i < 10; i++) {
			dataMap = new HashMap<String, Object>();
			dataMap.put("photo_head", R.drawable.head2);
			data.add(dataMap);
		}
		return data;
	}
	/**
	 * 私信页
	 *
	 * 准备更新内容，现在实现的跟通知页的内容一样
	 */
	private static class PrivateMessageAdapter extends BaseAdapter{

		private ArrayList<HashMap<String, Object>> mData;
		private LayoutInflater mInflater;

		private class ViewHolder{
			CircleImageView mCircleImagePhotoHead;
			TextView mUserName;
			TextView mComment;
			TextView mTimeOfComment;
		}

		public PrivateMessageAdapter(Context context, ArrayList<HashMap<String, Object>> maps) {

			mData = maps;
			mInflater = LayoutInflater.from(context);
		}
		@Override
		public int getCount() {

			return mData.size();
		}
		@Override
		public Object getItem(int position) {

			return mData.get(position);
		}
		@Override
		public long getItemId(int position) {

			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder mViewHolder;

			if(convertView == null){
				mViewHolder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.message_index_page_private_content, null);
				mViewHolder.mCircleImagePhotoHead = (CircleImageView) convertView.findViewById(R.id.message_index_user_photo_head_of_private_comment);
				mViewHolder.mComment = (TextView) convertView.findViewById(R.id.message_index_private_comment_content);
				mViewHolder.mUserName = (TextView) convertView.findViewById(R.id.message_index_private_content_user_name);
				mViewHolder.mTimeOfComment = (TextView) convertView.findViewById(R.id.message_index_time_of_private_comment);
				convertView.setTag(mViewHolder);
			}else {
				mViewHolder = (ViewHolder) convertView.getTag();
			}
			// 添加数据


			return convertView;
		}
	}
	/**
	 * 点击通知或者私信
	 */
	private void testButton(){
		mNoticeButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mNoticeButton.setBackgroundColor(R.color.red);
				mPrivateNoticeButton.setBackgroundColor(R.color.gray);
			}
		});
		mPrivateNoticeButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mNoticeButton.setBackgroundColor(R.color.gray);
				mPrivateNoticeButton.setBackgroundColor(R.color.red);

			}
		});
	}

}
