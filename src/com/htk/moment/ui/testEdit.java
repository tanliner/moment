package com.htk.moment.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;


/**
 * Created by Administrator on 2014/12/18.
 */
public class testEdit extends Activity {

	AutoCompleteTextView mAutoCompleteTextView;

	Button mButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);


		setContentView(R.layout.test_lay);
		mAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.test);

		mButton = (Button) findViewById(R.id.button);

		initAutoComplete("his", mAutoCompleteTextView);


		mButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				saveHistory("his", mAutoCompleteTextView);
			}
		});
	}


	/**
	 * 初始化AutoCompleteTextView，最多显示5项提示，使
	 * AutoCompleteTextView在一开始获得焦点时自动提示
	 *
	 * @param field 保存在sharedPreference中的字段名
	 * @param auto  要操作的AutoCompleteTextView
	 */
	private void initAutoComplete(String field, AutoCompleteTextView auto) {

		SharedPreferences sp = getSharedPreferences("network_url", 0);
		String longhistory = sp.getString(field, "nothing");

		String[] hisArrays = longhistory.split(",");

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, hisArrays);
		//只保留最近的50条的记录
		if (hisArrays.length > 50) {
			String[] newArrays = new String[50];
			System.arraycopy(hisArrays, 0, newArrays, 0, 50);
			adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, newArrays);
		}
		auto.setAdapter(adapter);
		auto.setDropDownHeight(350);
		auto.setThreshold(1);
		auto.setCompletionHint("最近的5条记录");
		auto.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {

				AutoCompleteTextView view = (AutoCompleteTextView) v;
				if (hasFocus) {
					view.showDropDown();
				}
			}
		});
	}

	/**
	 * 把指定AutoCompleteTextView中内容保存到sharedPreference中指定的字符段
	 *
	 * @param field 保存在sharedPreference中的字段名
	 * @param auto  要操作的AutoCompleteTextView
	 */
	private void saveHistory(String field, AutoCompleteTextView auto) {

		String text = auto.getText().toString();
		SharedPreferences sp = getSharedPreferences("network_url", 0);
		String longhistory = sp.getString(field, "nothing");

		if (!longhistory.contains(text + ",")) {
			StringBuilder sb = new StringBuilder(longhistory);
			sb.insert(0, text + ",");
			sp.edit().putString("his", sb.toString()).apply();
		}
	}


}
