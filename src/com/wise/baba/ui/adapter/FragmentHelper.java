package com.wise.baba.ui.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class FragmentHelper {
	
	private ScrollView scrollView;
	private LinearLayout llyt;
	int i = 0;
	int height = 0;
	int scrollY = 0;
	public static int duration = 2000;
	int durScroll = 0;
	
	int scrollSpeed = 30;
	
	int currentY = 0;
	private View currentView;
	private Handler handler;
	public FragmentHelper(Handler handler,ScrollView scrollView,LinearLayout llyt) {
		super();
		this.scrollView = scrollView;
		this.llyt = llyt;
		this.handler = handler;
	}

	public void top(View currentView) {
		
		scrollY = scrollView.getScrollY();
		currentY = scrollY;
		durScroll = duration/scrollY/scrollSpeed;
		scroll();
		animation(currentView);
	}

	
	public void animation(final View currentView){
		handler.post(new Runnable(){

			@Override
			public void run() {
				TranslateAnimation animation = new TranslateAnimation(0, 0, 0,
						-currentView.getTop());
				animation.setInterpolator(new LinearInterpolator());
				animation.setDuration(duration);
				animation.setFillAfter(true);
				currentView.startAnimation(animation);
			}
			
		});
	}
	public void scroll() {

		if (currentY <= 0) {
			return;
		}

		if (currentY < scrollSpeed) {
			currentY = scrollSpeed;
		}

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				scroll();
			}
		}, durScroll);

		System.out.println(currentY);
		currentY -= scrollSpeed;
		scrollView.scrollTo(0, currentY);
	}

}
