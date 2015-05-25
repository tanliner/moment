package utils.android.photo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;


/**
 * 图片压缩， 可以按尺寸，质量
 * <p/>
 * Created by Administrator on 2014/9/3.
 */
public class ImageCompressUtil {

	/**
	 * 指定目标图片大小（不是Byte单位）
	 *
	 * @param bitmap  原图片
	 * @param maxSize 目标大小
	 *
	 * @return 压缩好的图片
	 */
	public static Bitmap compressByQuality (Bitmap bitmap, int maxSize) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		bitmap.compress(Bitmap.CompressFormat.JPEG, maxSize, baos);

		return BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.toByteArray().length);
	}

	/**
	 * 指定目标图片尺寸压缩 注：操作过程中不会保持纵横比
	 *
	 * @param oldBitmap 需要压缩的图片（bitmap）
	 * @param newWidth  目标图片宽度
	 * @param newHeight 目标图片高度
	 *
	 * @return 压缩好的图片（bitmap）
	 */
	public static Bitmap zoomImage (Bitmap oldBitmap, double newWidth, double newHeight) {

		Bitmap bitmap;
		// 获取这个图片的宽和高
		float width = oldBitmap.getWidth();
		float height = oldBitmap.getHeight();
		// 矩阵压缩
		Matrix matrix = new Matrix();
		// 计算宽高缩放率
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 缩放图片动作
		matrix.postScale(scaleWidth, scaleHeight);
		bitmap = Bitmap.createBitmap(oldBitmap, 0, 0, (int) width, (int) height, matrix, true);
		oldBitmap.recycle();
		return bitmap;
	}
}
