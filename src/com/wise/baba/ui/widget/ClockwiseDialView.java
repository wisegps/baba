package com.wise.baba.ui.widget;

import java.util.Timer;
import java.util.TimerTask;


import com.wise.baba.R;
import com.wise.baba.app.Msg;
import com.wise.baba.util.BitmapUtil;
import com.wise.baba.util.DensityUtil;
import com.wise.baba.util.DialBitmapFactory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * 
 * @author c 圆形刻度表盘 顺时针方向
 * 
 *         一，静态设置值 ：initValue 二，动画效果：setValue
 * 
 */

public class ClockwiseDialView extends FrameLayout {
	private ImageView imgColor; // 彩色刻度
	private ImageView imgCusor; // 圆环光标
	private ImageView imgCover; // 圆环底部覆盖扇形
	private Context context;
	private DialBitmapFactory bitmapFactory;
	private long period = 70;// 刻度转动单位时间
	private int currentValue = 0;
	private int value = 0;
	private Handler handler = null;

	private Timer timer = null;

	private RotateAnimation cusorAnimation;

	private boolean init = true;
	
	private boolean isRunning = false;
	public ClockwiseDialView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		String w = "";
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

		Bitmap bmColor = bitmapFactory.getBitmapByValue(value, false);
		imgColor.setImageBitmap(bmColor);
		imgCusor.setImageResource(R.drawable.circle_cursor);
		imgCover.setImageResource(R.drawable.circle_cover);

		this.addView(imgColor);
		this.addView(imgCusor);
		this.addView(imgCover);
	}

	// 初始化一个固定值
	public void initValue(final int value,Handler handler) {
		this.handler = handler;
		stopAnimation();
		handler.post(new Runnable(){
			@Override
			public void run() {
				BitmapUtil.recycleBitmap(imgColor);
				Log.i("ClockwiseDialView", "ClockwiseDialView1");
				Log.i("ClockwiseDialView", "bitmapFactory" +bitmapFactory);
				Bitmap bitmp = bitmapFactory.getBitmapByValue(value, true);
				Log.i("ClockwiseDialView", "ClockwiseDialView2");
				imgColor.setImageBitmap(bitmp);
			}
			
		});
	
	}

	// 設置一個值，出現動畫滾動到該值
	public void startAnimation(int value, Handler handler) {
		
		imgCusor.setImageResource(R.drawable.circle_cursor);
		init = false;
		stopAnimation();
		this.value = value;
		this.handler = handler;
		currentValue = 0;
		startColorAnimation();
		rolateCursor(value, period * value);
	}


	public void stopAnimation() {
		if (timer != null) {
			timer.cancel();
			timer.purge();
			timer = null;
		}

		if (cusorAnimation != null) {
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
						if(init != true){
							Message msg = new Message();
							msg.what = Msg.Dial_Refresh_Value;
							msg.arg1 = currentValue;
							handler.sendMessage(msg);
						}
						

						if (currentValue >= value) {
							// 动画运动到这里就停止
							if(timer!=null){
								timer.cancel();
								timer.purge();
								timer = null;
							}
							return;
						}
						
						Bitmap bitmp = bitmapFactory.getBitmapByValue(
								currentValue++, false);
						BitmapUtil.recycleBitmap(imgColor);
						imgColor.setImageBitmap(bitmp);

					}

				});

			}

		}, 1, period);

	}

	// /**
	// * 圆环光标逆时针旋转动画
	// *
	// * @param value
	// */
	// public void rolateCursor(final float value) {
	// new Thread(new Runnable(){
	// @Override
	// public void run() {
	// float rotateAngel = bitmapFactory.calcAngel(value);
	// cusorAnimation = new RotateAnimation(0f, -rotateAngel,
	// Animation.RELATIVE_TO_SELF, 0.5f,
	// Animation.RELATIVE_TO_SELF, 0.5f);
	// cusorAnimation.setInterpolator(new LinearInterpolator());
	// cusorAnimation.setFillAfter(true);
	// cusorAnimation.setDuration((long) (period * (100 - value)));
	// }
	//
	// }).start();
	//
	// handler.post(new Runnable() {
	// @Override
	// public void run() {
	// imgCusor.startAnimation(cusorAnimation);
	// }
	// });
	//
	// }

	
	/**
	 * 
	 * 根据刻度值，计算需要逆时针旋转的角度
	 * 
	 * @return 返回要旋转的角度
	 */
	public float calcAngel(float value) {
		float angelAll = 250;//圆360度，缺口110度，有效角度为250度
		float angleDial = (angelAll / 100) * value;
		return angleDial;
	}
	/**
	 * 圆环光标逆时针旋转动画
	 * 
	 * @param value
	 */
	public void rolateCursor(float value, long duration) {
		float rotateAngel = calcAngel(value);
		
		if(value == 0){
			value = 0.01f;
			duration = 1;
		}
		
		cusorAnimation = new RotateAnimation(110f, 110+rotateAngel,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		cusorAnimation.setInterpolator(new LinearInterpolator());
		cusorAnimation.setFillAfter(true);
		cusorAnimation.setDuration(duration);
		cusorAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				isRunning = true;
			}
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			@Override
			public void onAnimationEnd(Animation animation) {
				isRunning = false;
				
			}
		});
		handler.post(new Runnable() {
			@Override
			public void run() {
				imgCusor.startAnimation(cusorAnimation);
			}
		});

	}

	public boolean getStatus(){
		return isRunning;
	}
	
	@Override
	protected void onDetachedFromWindow() {
		freeMemory();
		super.onDetachedFromWindow();
	}

	public void freeMemory() {
		stopAnimation();
		//BitmapUtil.recycleBitmap(imgColor);
		//BitmapUtil.recycleBitmap(imgCusor);
		imgColor.setImageBitmap(null);
		//imgCusor.setImageBitmap(null);
		//imgCover.setImageBitmap(null);
		//bitmapFactory = null;
	}

}
