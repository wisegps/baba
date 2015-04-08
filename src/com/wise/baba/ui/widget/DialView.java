package com.wise.baba.ui.widget;

import java.util.Timer;
import java.util.TimerTask;

import pubclas.DensityUtil;

import com.wise.baba.R;
import com.wise.baba.app.Msg;
import com.wise.baba.util.BitmapUtil;
import com.wise.baba.util.DialBitmapFactory;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * 
 * @author c 圆形刻度表盘
 * 
 *         一，静态设置值 ：initValue 二，动画效果：setValue
 * 
 */
public class DialView extends FrameLayout {
	private ImageView imgColor; // 彩色刻度
	private ImageView imgCusor; // 圆环光标
	private ImageView imgCover; // 圆环底部覆盖扇形
	private Context context;
	private DialBitmapFactory bitmapFactory;
	private long period = 70;// 刻度转动单位时间
	private int currentValue = 100;
	private int value = 100;
	private Handler handler = null;
	
	private Timer timer = null;
	
	private RotateAnimation cusorAnimation;

	public DialView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		String  w = "";
		for (int i = 0; i < attrs.getAttributeCount(); i++) {
			if ("layout_width".equals(attrs.getAttributeName(i))) {
				w = attrs.getAttributeValue(i);
				break;
			}
		}
		w = w.replace("dip", "");
		int realWidth = DensityUtil.dip2px(context, Float.parseFloat(w));
		bitmapFactory = new DialBitmapFactory(context, realWidth);
		initChildView();
		

	}


	/**
	 * 初始化子控件（图层）
	 */
	public void initChildView() {

		// 创建子图层
		imgColor = new ImageView(context);
		imgCusor = new ImageView(context);
		imgCover = new ImageView(context);

		// 填充父布局
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		imgColor.setLayoutParams(lp);
		imgCusor.setLayoutParams(lp);
		imgCover.setLayoutParams(lp);

		Bitmap bmColor = bitmapFactory.getBitmapByValue(value, true);
		imgColor.setImageBitmap(bmColor);
		imgCusor.setImageResource(R.drawable.circle_cursor);
		imgCover.setImageResource(R.drawable.circle_cover);

		this.addView(imgColor);
		this.addView(imgCusor);
		this.addView(imgCover);
	}

	// 初始化一个固定值
	public void initValue(int value) {
		BitmapUtil.recycleBitmap(imgColor);
		Bitmap bitmp = bitmapFactory.getBitmapByValue(value, true);
		imgColor.setImageBitmap(bitmp);
	}

	// 設置一個值，出現動畫滾動到該值
	public void startAnimation(int value, Handler handler) {
		stopAnimation();
		this.value = value;
		this.handler = handler;
		imgCusor.setImageResource(R.drawable.circle_cursor);
		currentValue = 100;
		startColorAnimation();
		rolateCursor(value);
	}

	
	public void stopAnimation(){
		if(timer!=null){
			timer.cancel();
			timer.purge();
			timer = null;
		}
		
		if(cusorAnimation!=null){
			cusorAnimation.cancel();
		}
		
	}
	/**
	 * 彩色刻度绘制动画
	 */
	public void startColorAnimation() {
		
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					@Override
					public void run() {
						Message msg = new Message();
						msg.what = Msg.Dial_Refresh_Value;
						msg.arg1 = currentValue;
						handler.sendMessage(msg);
						
						BitmapUtil.recycleBitmap(imgColor);
						Bitmap bitmp = bitmapFactory.getBitmapByValue(
								currentValue--, false);
						imgColor.setImageBitmap(bitmp);
						
						if (currentValue < value) {
							// 动画运动到这里就停止
							timer.cancel();
							timer.purge();
							timer = null;
						}
					}
				});

			}

		}, 1, period);

	}

	/**
	 * 圆环光标逆时针旋转动画
	 * 
	 * @param value
	 */
	public void rolateCursor(final float value) {
		
		handler.post(new Runnable() {
			@Override
			public void run() {
				float rotateAngel = bitmapFactory.calcAngel(value);
				cusorAnimation = new RotateAnimation(0f,
						-rotateAngel, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				cusorAnimation.setInterpolator(new LinearInterpolator());
				cusorAnimation.setFillAfter(true);
				cusorAnimation.setDuration((long) (period * (100 - value)));
				imgCusor.startAnimation(cusorAnimation);
			}
		});

	}

	@Override
	protected void onDetachedFromWindow() {
		freeMemory();
		super.onDetachedFromWindow();
	}

	public void freeMemory() {
		BitmapUtil.recycleBitmap(imgColor);
		//BitmapUtil.recycleBitmap(imgCusor);
		imgCover.setImageBitmap(null);
		bitmapFactory = null;
		this.handler = null;
	}

}
