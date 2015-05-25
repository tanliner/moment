package utils.android.sdcard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 从一个输入流中读数据
 *
 * 修改：谭林，2014/11/11
 * 内容：返回值改为String，除了从服务器上得到的数据是Json字符串以外
 * 其他的流中，只能得到字符流（/字节流）
 *
 * Created by HP on 2014/8/2.
 * @version 1.1
 */
public class Read {
	/**
	 * 从给定的输入流中读数据
	 * @param in 某一个输入流
	 * @return 读到的字符串
	 * @throws IOException 将IOException报告给调用者
	 */
	public static String read(InputStream in) throws IOException {
		String temp;
		StringBuffer sb = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(in,"utf-8"));
		while((temp = br.readLine()) != null){
			sb.append(temp);
		}
		br.close();
		return sb.toString();
	}
}
