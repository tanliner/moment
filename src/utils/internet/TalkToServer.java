package utils.internet;

import utils.android.sdcard.Read;

import java.io.IOException;
import java.net.HttpURLConnection;


/**
 * Created by Administrator on 2014/12/2.
 */
public class TalkToServer {

	public static boolean sendMessage(){

		return true;
	}
	public static Object getMessage(){

		HttpURLConnection connection = ConnectionHandler.getConnect(UrlSource.CHECK_EMAIL);
		try {
			Read.read(connection.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


}
