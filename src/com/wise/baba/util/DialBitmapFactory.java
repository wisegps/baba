package com.wise.baba.util;

import com.wise.baba.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

/**
 * 
 * @desc 制作刻度盘图片
 * 
 */

public class DialBitmapFactory {
	public float startAngle = 35;// 圆环缺口 从35度开始 ，横扫110度
	public float sweepAngle = 110;

	public int width;
	public int height;
	public Context context;
	public Bitmap bmColor;// 彩色刻度
	public Bitmap bmGray; // 灰色刻度
	public Bitmap bmCursor; // 外层圆形状光标 circular cursor

	public Bitmap mBitmap;
	public Canvas mCanvas;

	public DialBitmapFactory(Context context) {
		super();
		this.context = context;
		bmColor = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.circle_dial_color);
		bmGray = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.circle_dial_gray);
		bmCursor = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.circle_cursor);
		width = bmColor.getWidth();
		height = bmColor.getHeight();
	}

	public Bitmap getBitmapByValue(float value) {
		
		mBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		mCanvas.save();
		// 第一层，画彩色刻度
		mCanvas.drawBitmap(bmColor, 0, 0, null);
		// 第二层，画灰色刻度
		Bitmap gray = getGray(bmGray, value);
		mCanvas.drawBitmap(gray, 0, 0, null);
		// 第三层，画圆环光标
		Bitmap angleCursor = sector(bmCursor, value);
		mCanvas.drawBitmap(angleCursor, 0, 0, null);
		mCanvas.restore();

		return mBitmap;

	}

	/**
	 * 
	 * @param src
	 * @param value
	 * @return 灰色刻度部分
	 */
	public Bitmap getGray(Bitmap src, float value) {
		// 1,先建立一个原图的副本
		Bitmap newBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(newBitmap);
		canvas.save();
		canvas.drawBitmap(src, 0, 0, null);
		// 根据刻度值计算旋转角度
		float degrees = calcAngel(value);
		// 剪去旋转部分（彩色刻度范围）
		clip(canvas, startAngle, sweepAngle + degrees);
		canvas.restore();// 存储
		return newBitmap;
	}

	/**
	 * 根据刻度值返回一个图片
	 * 
	 * @param src
	 * @param value
	 * @return
	 */
	public Bitmap sector(Bitmap src, float value) {

		// 1,先建立一个原图的副本
		Bitmap newBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(newBitmap);
		canvas.save();

		// 2,旋转角度
		// 根据刻度值计算旋转角度
		float degrees = calcAngel(value);
		Matrix matrix = new Matrix();
		matrix.setRotate(degrees, width / 2, height / 2);
		canvas.drawBitmap(src, matrix, null);

		// 3，擦除圆环缺口
		clip(canvas, startAngle, sweepAngle);
		canvas.restore();// 存储

		return newBitmap;
	}

	// 从一张圆中剪去一个扇形
	public void clip(Canvas canvas, float startAngle, float sweepAngle) {
		// 自建橡皮擦paint，画过的地方全部clear
		Paint paint = new Paint();
		paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		RectF oval = new RectF(-50, -50, canvas.getWidth() + 50,
				canvas.getHeight() + 50);
		canvas.drawArc(oval, startAngle, sweepAngle, true, paint);
	}

	/**
	 * 
	 * 根据刻度值，计算需要旋转的角度
	 * 
	 * @return 返回要旋转的角度
	 */
	public float calcAngel(float value) {
		float angelAll = 360 - sweepAngle;
		float angleDial = (angelAll / 100) * value;
		return angleDial;
	}

}
