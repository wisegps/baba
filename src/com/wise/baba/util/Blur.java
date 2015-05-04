package com.wise.baba.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

public class Blur {
	/**
	 * 获取正方形图片
	 * 
	 * @param bitmap
	 * @return
	 */
	public static Bitmap getSquareBitmap(Bitmap bitmap) {
		int imageWidth = bitmap.getWidth();
		int imageHeight = bitmap.getHeight();
		int y = 0;
		Bitmap image = null;
		if (imageHeight > imageWidth) {
			y = (imageHeight - imageWidth) / 2;
			image = Bitmap.createBitmap(bitmap, 0, y, imageWidth, imageWidth);
		} else {
			y = (imageWidth - imageHeight) / 2;
			image = Bitmap.createBitmap(bitmap, y, 0, imageHeight, imageHeight);
		}
		return image;
	}

	/**
	 * 缩小图片
	 * 
	 * @param Path
	 *            文件sd卡路径
	 * @param reqWidth
	 *            缩小的宽度
	 * @param reqHeight
	 *            缩小的高度
	 * @return
	 */
	public static Bitmap decodeSampledBitmapFromPath(String Path, int reqWidth,
			int reqHeight) {
		// 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(Path, options);
		// 调用上面定义的方法计算inSampleSize值
		options.inSampleSize = calculateInSampleSize(options, reqWidth,reqHeight);
		// 使用获取到的inSampleSize值再次解析图片
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(Path, options);
	}

	public static Bitmap decodeSampledBitmapFromResource(Resources res,
			int resId, int reqWidth, int reqHeight) {
		// 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);
		// 调用上面定义的方法计算inSampleSize值
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		// 使用获取到的inSampleSize值再次解析图片
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	/**
	 * 计算缩放尺寸
	 * 
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {
		// 源图片的高度和宽度
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > width) {// 竖着拍
			if (height > reqHeight || width > reqWidth) {
				// 计算出实际宽高和目标宽高的比率
				final int heightRatio = Math.round((float) height
						/ (float) reqHeight);
				final int widthRatio = Math.round((float) width
						/ (float) reqWidth);
				// 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
				// 一定都会大于等于目标的宽和高。
				inSampleSize = heightRatio < widthRatio ? heightRatio
						: widthRatio;
			}
		} else {// 横这拍
			if (height > reqWidth || width > reqHeight) {
				// 计算出实际宽高和目标宽高的比率
				final int heightRatio = Math.round((float) height
						/ (float) reqWidth);
				final int widthRatio = Math.round((float) width
						/ (float) reqHeight);
				// 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
				// 一定都会大于等于目标的宽和高。
				inSampleSize = heightRatio < widthRatio ? heightRatio
						: widthRatio;
			}
		}
		return inSampleSize;
	}

	/**
	 * 图片缩放
	 */
	public static Bitmap scaleImage(Bitmap bitmap, int newWidth) {
		// 获得图片的宽高
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		// 计算缩放比例
		float scale = calculateScale(height,width,newWidth);
		// 取得想要缩放的matrix参数
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		// 得到新的图片
		Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,true);
		return newbm;
	}
	/**取最小缩放比例**/
	public static float calculateScale(int realHeight , int relWidth , int newWidth){
		float height = (float)newWidth/realHeight;
		float width = (float)newWidth/relWidth;
		return height < width ? width : height;
	}
	/**
	 * 根据宽度缩放
	 * @param bitmap
	 * @param newWidth
	 * @return
	 */
	public static Bitmap scaleWidthImage(Bitmap bitmap , int newWidth){
		// 获得图片的宽高
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		// 计算缩放比例
		float scale = (float)newWidth/width;
		// 取得想要缩放的matrix参数
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		// 得到新的图片
		Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,true);
		return newbm;
	}
	/**获取圆角图片**/
	public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {  
	    Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),  
	            bitmap.getHeight(), Config.ARGB_8888);  
	    Canvas canvas = new Canvas(output);  
	    final int color = 0xff424242;  
	    final Paint paint = new Paint();
	    final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());  
	    final RectF rectF = new RectF(rect);
	    final float roundPx = pixels;
	    paint.setAntiAlias(true);  
	    canvas.drawARGB(0, 0, 0, 0);
	    paint.setColor(color);  
	    canvas.drawRoundRect(rectF, roundPx, roundPx, paint);  
	    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));  
	    canvas.drawBitmap(bitmap, rect, rect, paint);  
	    return output;  
	}
}