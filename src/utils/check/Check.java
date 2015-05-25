package utils.check;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 做一下检查操作
 * Created by Administrator on 2014/11/2.
 */
public class Check {
	public static boolean isEmail(String email) {
		String strEmail = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
		Pattern p = Pattern.compile(strEmail);
		Matcher m = p.matcher(email);
		return m.matches();
	}
	public static boolean isPhoneNumber(String mobileNum){
		String strMobileNum = "^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$";
		Pattern p = Pattern.compile(strMobileNum);
		Matcher m = p.matcher(mobileNum);
		return m.matches();
	}
	public static boolean internetIsEnable(Context context) {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobile = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if((wifiInfo.getState() == NetworkInfo.State.DISCONNECTED) && (mobile.getState() == NetworkInfo.State.DISCONNECTED)){
			return false;
		}
		return true;
	}
}
