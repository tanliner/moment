package utils.view.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.htk.moment.ui.R;
import utils.view.view.CircleImageView;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by Administrator on 2014/12/2.
 */
public class CommentDetailFragment extends Fragment {

	private static CommentDetailFragment mCommentDetailFragment;

	private CommentDetailFragment(){

	}
	public static CommentDetailFragment getFragment(){
		if(mCommentDetailFragment == null){
			mCommentDetailFragment = new CommentDetailFragment();
		}
		return mCommentDetailFragment;
	}

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
		View convertView = inflater.inflate(R.layout.comment_detail_list, container, false);
		ListView mListView = (ListView) convertView.findViewById(R.id.comment_list_view);

		mListView.setAdapter(new CommentAdapter(getActivity(), getSomeStaticData()));

		return convertView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
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


	/**
	 * 私信页
	 *
	 * 准备更新内容，现在实现的跟通知页的内容一样
	 */
	private static class CommentAdapter extends BaseAdapter {

		private ArrayList<HashMap<String, Object>> mData;
		private LayoutInflater mInflater;

		private class ViewHolder{
			CircleImageView mCircleImagePhotoHead;
			TextView mUserName;
			TextView mComment;
			TextView mTimeOfComment;
		}


		public CommentAdapter(Context context, ArrayList<HashMap<String, Object>> maps) {

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
				convertView = mInflater.inflate(R.layout.comment_detail_list_content, null);

				mViewHolder.mCircleImagePhotoHead = (CircleImageView) convertView.findViewById(R.id.comment_index_detail_user_photo_head);

				convertView.setTag(mViewHolder);
			}else {
				mViewHolder = (ViewHolder) convertView.getTag();
			}
			// 添加数据

			return convertView;
		}
	}

	/**
	 * @return 带有静态数据的listMap
	 */
	private ArrayList<HashMap<String, Object>> getSomeStaticData() {

		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> dataMap;
		for (int i = 0; i < 10; i++) {
			dataMap = new HashMap<String, Object>();
			dataMap.put("photo_head", R.drawable.head2);
			data.add(dataMap);
		}
		return data;
	}



}
