package com.wise.baba.ui.widget;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * TextView(上传中...)动态效果
 *@author honesty
 **/
public class TimTextView extends TextView{
	
	private static final int status0 = 0;
	private static final int status1 = 1;
	private static final int status2 = 2;
	
	private int status = 1;
	private boolean isStop = false;
	private int span = 200;

	public TimTextView(Context context) {
		super(context);
		setText("上传中.  ");
	}
	public TimTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setText("上传中.  ");
	}	
	public TimTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setText("上传中.  ");
	}
	Runnable runnable = new Runnable() {		
		@Override
		public void run() {
			switch (status) {
			case status0:
				status = status1;
				setText("上传中.  ");
				break;
			case status1:
				status = status2;
				setText("上传中.. ");
				break;
			case status2:
				status = status0;
				setText("上传中...");
				break;
			}
			if(!isStop){
				if(getVisibility() == View.VISIBLE){
					handler.postDelayed(runnable, span);
				}
			}
		}
	};
	Handler handler = new Handler(){
		
	};
	public void setStop(boolean isStop){
		this.isStop = isStop;
	}
	public void startTim(){
		this.isStop = false;
		handler.postDelayed(runnable, span);
	}
}