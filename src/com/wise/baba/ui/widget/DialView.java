package com.wise.baba.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.wise.baba.R;
import com.wise.baba.util.DialBitmapFactory;

public class DialView extends View {
	public Bitmap bmColor;// 彩色刻度
	public Bitmap bmGray; // 灰色刻度
	public Bitmap bmCursor; // 外层圆形状光标 circular cursor
	public DialBitmapFactory dialFactory;
	public Context context;

	public float value = 72 ;
	public DialView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
	}

	public void init() {
		bmColor = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.circle_dial_color);
		bmGray = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.circle_dial_gray);
		bmCursor = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.circle_cursor);
		dialFactory = new DialBitmapFactory(bmColor.getWidth(),
				bmColor.getHeight());
	}

	/**
	 * 选择转动值 0---100
	 * */
	public void changeValue(float value) {
		this.value = value;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();
		// 第一层，画彩色刻度
		canvas.drawBitmap(bmColor, 0, 0, null);
		// 第二层，画灰色刻度
		Bitmap gray = dialFactory.getGray(bmGray, value);
		canvas.drawBitmap(gray, 0, 0, null);
		// 第三层，画圆环光标
		Bitmap angleCursor = dialFactory.sector(bmCursor, value);
		canvas.drawBitmap(angleCursor, 0, 0, null);
		canvas.restore();
	}
}
