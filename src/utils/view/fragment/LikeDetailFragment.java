package utils.view.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.htk.moment.ui.R;
import utils.view.view.CircleImageView;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by Administrator on 2014/12/2.
 */
public class LikeDetailFragment extends Fragment {

	private static LikeDetailFragment mLikeDetailFragment;

	private LikeDetailFragment(){

	}
	public static LikeDetailFragment getFragment(){
		if(mLikeDetailFragment == null){
			mLikeDetailFragment = new LikeDetailFragment();
		}
		return mLikeDetailFragment;
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
		View convertView = inflater.inflate(R.layout.like_detail_list, container, false);
		ListView mListView = (ListView) convertView.findViewById(R.id.like_list_view);
		LikeDetailAdapter adapter = new LikeDetailAdapter(getActivity(), getSomeStaticData());
		mListView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
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

	private class LikeDetailAdapter extends BaseAdapter {

		private ArrayList<HashMap<String, Object>> mData;
		private LayoutInflater mInflater;

		private class MyListViewHolder {

			CircleImageView mCircleImagePhotoHead;
			TextView mUserName;
			TextView mComment;
			TextView mTimeOfComment;
			ImageView mUserPisture;
		}

		public LikeDetailAdapter(Context context, ArrayList<HashMap<String, Object>> maps) {

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
				convertView = mInflater.inflate(R.layout.like_detail_list_content, null);


				mMyListViewHolder.mCircleImagePhotoHead = (CircleImageView) convertView.findViewById(R.id.like_index_detail_user_photo_head);

				convertView.setTag(mMyListViewHolder);
			} else {
				mMyListViewHolder = (MyListViewHolder) convertView.getTag();
			}
//			mMyListViewHolder.mCircleImagePhotoHead.setImageResource(R.drawable.head2);

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
