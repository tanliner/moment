package utils.internet;

import utils.json.JSONObject;


/**
 * Created by Administrator on 2014/12/11.
 */
public class JsonStringMaker {


	public static String createJsonString(String key, String value){

		JSONObject mJsonObject = new JSONObject();
		mJsonObject.put(key, value);
		return mJsonObject.toString();
	}
	public static String createJsonString(String key, int value){

		JSONObject mJsonObject = new JSONObject();
		mJsonObject.put(key, value);
		return mJsonObject.toString();
	}
	public static String createJsonString(String key, boolean value){

		JSONObject mJsonObject = new JSONObject();
		mJsonObject.put(key, value);
		return mJsonObject.toString();
	}
}
