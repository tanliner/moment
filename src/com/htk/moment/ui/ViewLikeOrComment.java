package com.htk.moment.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;
import utils.android.AppManager;
import utils.view.fragment.CommentDetailFragment;
import utils.view.fragment.LikeDetailFragment;

import java.util.ArrayList;


/**
 * 点击首页中的喜欢/评论 文字按钮进入此Activity
 * 进行详细的查看
 *
 * Created by Administrator on 2014/12/2.
 */
public class ViewLikeOrComment extends FragmentActivity {

	public final static String TAG = "ViewLikeOrComment";

	private ArrayList<Fragment> fragments;

	private TextView mLikeTextView;
	private TextView mCommentTextView;
	private ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		AppManager.getAppManager().addActivity(this);
		setContentView(R.layout.view_like_or_comment_detail);
		init();
		change();
	}

	private void init(){
		mLikeTextView = (TextView) findViewById(R.id.like_detail_like_text);
		mCommentTextView = (TextView) findViewById(R.id.comment_detail_comment_text);
		mViewPager = (ViewPager) findViewById(R.id.like_comment_content_page);
	}


	private void change(){
		mLikeTextView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mViewPager.setCurrentItem(0);
			}
		});
		mCommentTextView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mViewPager.setCurrentItem(1);
			}
		});


		mViewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {

			@Override
			public Fragment getItem(int position) {

				if(position == 0){
					return LikeDetailFragment.getFragment();
				}
				return CommentDetailFragment.getFragment();
			}
			@Override
			public int getCount() {

				return 2;
			}
		});
	}
}
