package com.htk.moment.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import utils.view.fragment.MeFragment;

/**
 * Created by Administrator on 2015/3/18.
 */
public class OtherPeopleIndex extends FragmentActivity{

    public static String TAG = "OtherPeopleIndex";

    Fragment theTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_people_home_index);
        init();

    }

    private void init(){
        //verticalViewPager = (VerticalViewPager) findViewById(R.id.verticalViewPager);

        theTest = new MeFragment();
        Bundle data = new Bundle();
        data.putInt("user_id", getIntent().getIntExtra("userId", -1));
        theTest.setArguments(data);
        /*
        fragments.add(new MeFragment());
        FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
        ft.add(R.id.app_index_container, fragments.get(0));
        ft.commit();
*/
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if(theTest.isVisible()) {
            return;
        }
        if (theTest.isAdded()) {
            theTest.onResume();
            ft.show(theTest);
        } else {
            ft.add(R.id.other_people_index_container, theTest);
            ft.show(theTest);
        }
        ft.commit();

        System.out.println("调用结束========");
    }
}