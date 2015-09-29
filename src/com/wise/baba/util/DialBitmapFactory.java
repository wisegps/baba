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
import android.util.Log;

/**
 * 
 * @desc 制作刻度盘图片
 * 
 */

public class DialBitmapFactory {
	public float startAngle = 0;// 圆环缺口 从35度开始 ，横扫110度
	public float sweepAngle = 110;

//	public int width;
	public Context context;
	public Bitmap bmColor;// 彩色刻度
	public Bitmap bmGray; // 灰色刻度
	public Bitmap bmCursor; // 外层圆形状光标 circular cursor

	public Bitmap mBitmap;
	public Canvas mCanvas;
	
	public int width;

//	public DialBitmapFactory(Context context) {
//		super();
//		this.context = context;
//		bmColor = BitmapFactory.decodeResource(context.getResources(),
//				R.drawable.circle_dial_color);
//		bmGray = BitmapFactory.decodeResource(context.getResources(),
//				R.drawable.circle_dial_gray);
//		bmCursor = BitmapFactory.decodeResource(context.getResources(),
//				R.drawable.circle_cursor);
//		width = bmColor.getWidth();
//		height = bmColor.getHeight();
//	}

	public DialBitmapFactory(Context context, int realwidth) {
		super();
		// TODO Auto-generated constructor stub
		this.context = context;
		bmColor = BitmapUtil.decodeBitmap(context.getResources(), R.drawable.circle_dial_color, realwidth, realwidth);
		bmGray =  BitmapUtil.decodeBitmap(context.getResources(), R.drawable.circle_dial_gray, realwidth, realwidth);
		bmCursor =  BitmapUtil.decodeBitmap(context.getResources(), R.drawable.circle_cursor, realwidth, realwidth);
		width = bmColor.getWidth();
		
	}



	/**
	 * 
	 * @param value    值  ：0-100
	 * @param hasCursor  是否带圆环光标
	 * @return
	 */
	public Bitmap getBitmapByValue(final float value,final boolean hasCursor) {
		
				Log.i("DialBitmapFactory", "DialBitmapFactory1");
				mBitmap = Bitmap.createBitmap(width, width, Config.ARGB_4444);
				mCanvas = new Canvas(mBitmap);
				mCanvas.save();
				// 第一层，画彩色刻度
				mCanvas.drawBitmap(bmColor, 0, 0, null);
				// 第二层，画灰色刻度
				Bitmap gray = getGray(bmGray, value);
				mCanvas.drawBitmap(gray, 0, 0, null);
				// 第三层，画圆环光标
				if(hasCursor == true){
					Log.i("DialBitmapFactory", "hasCursor");
					Bitmap angleCursor = sector(bmCursor, value);
					mCanvas.drawBitmap(angleCursor, 0, 0, null);
					angleCursor.recycle();
				}
				gray.recycle();
				mCanvas.restore();
				Log.i("DialBitmapFactory", "DialBitmapFactory2");
		
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
		Bitmap newBitmap = Bitmap.createBitmap(width, width, Config.ARGB_4444);
		Canvas canvas = new Canvas(newBitmap);
		canvas.save();
		canvas.drawBitmap(src, 0, 0, null);
		// 根据刻度值计算旋转角度
		float degrees = calcAngel(value);
		// 剪去旋转部分（彩色刻度范围）
		clip(canvas, 35, 360-degrees);
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
		Bitmap newBitmap = Bitmap.createBitmap(width, width, Config.ARGB_4444);
		Canvas canvas = new Canvas(newBitmap);
		canvas.save();

		// 2,旋转角度
		// 根据刻度值计算旋转角度
		float degrees = calcAngel(value);
		Matrix matrix = new Matrix();
		matrix.setRotate(-degrees, width / 2, width / 2);
		canvas.drawBitmap(src, matrix, null);

		// 3，擦除圆环缺口
		//clip(canvas, startAngle, sweepAngle);
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
	 * 根据刻度值，计算需要逆时针旋转的角度
	 * 
	 * @return 返回要旋转的角度
	 */
	public float calcAngel(float value) {
		float angelAll = 250;//圆360度，缺口110度，有效角度为250度
		float angleDial = (angelAll / 100) * (100-value);
		return angleDial;
	}
	
	

}
