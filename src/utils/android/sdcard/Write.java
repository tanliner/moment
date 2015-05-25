package utils.android.sdcard;


import android.content.Context;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 往外面写
 * Created by HP on 2014/8/2.
 */
public class Write {
	public static boolean write(Context context, String fileName, String content) throws IOException {
		FileOutputStream out = context.openFileOutput(fileName, Context.MODE_PRIVATE);
		out.write(content.getBytes());

		return true;
	}
	public static void writeToHttp(OutputStream outputStream, byte[] uploadData) throws IOException {
		outputStream.write(uploadData);
	}
}
