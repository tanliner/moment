package utils.android;

import java.util.LinkedList;
import java.util.Stack;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

/**
 * 为了退出app，循环调用finish
 * Created by Administrator on 2015/5/26.
 */
public class AppManager {

	private static Stack<Activity> activityStack;

	private static LinkedList<Activity> activityList;

	private static AppManager instance;

	private AppManager(){}
	/**
	 * 单一实例
	 */
	public static AppManager getAppManager(){
		if(instance==null){
			instance=new AppManager();
		}
		return instance;
	}
	/**
	 * 添加Activity到堆栈
	 */
	public void addActivity(Activity activity){
//		if(activityStack==null){
//			activityStack=new Stack<Activity>();
//		}
		if(activityList == null){
			activityList = new LinkedList<Activity>();
		}
		activityList.add(activityList.size(), activity);
//		activityStack.add(activity);
	}

	/**
	 * 结束所有Activity
	 */
	public void finishAllActivity() throws InterruptedException {

//		int len = activityList.size();
//		Activity activity;
//
//		for(int i = 0; i < len; i++){
//			activity = activityList.get(i);
//			activity.finish();
//			if(activity.isFinishing()){
//				Thread.sleep(1);
//			}
//			activityList.remove(i);
//		}

		for(Activity activity : activityList){
			activity.finish();
			if(activity.isFinishing()){
				Thread.sleep(1);
			}
		}

//		for (int i = 0, size = activityStack.size(); i < size; i++){
//			if (null != activityStack.get(i)){
//				activityStack.get(i).finish();
//			}
//		}
//		activityStack.clear();

		activityList.clear();
	}
	/**
	 * 退出应用程序
	 */
	public void appExit(Context context) {
		try {
			finishAllActivity();
			ActivityManager activityMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			activityMgr.restartPackage(context.getPackageName());
			System.exit(0);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Log.e("AppManager", "finishing is finishing exception!");
		}
	}

}
