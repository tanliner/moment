package utils.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


/**
 * 从服务器下载数据，包括图片以及文件 应该 Created by Administrator on 2014/11/5.
 */
public class LoadMessage extends Service {

	@Override
	public IBinder onBind (Intent intent) {

		return null;
	}
}
