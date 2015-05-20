/**
 * 
 */
package com.wise.baba.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.LinearLayout;

/**
 *
 * @author c拦截父控件点击事件
 * @desc   baba
 * @date   2015-5-20
 *
 */
public class InterceptLinearLayout extends LinearLayout{

	/**
	 * @param context
	 * @param attrs
	 */
	public InterceptLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		 if (ev.getActionMasked() == MotionEvent.ACTION_DOWN)  
		    {  
		        ViewParent p = getParent().getParent();  
		        if (p != null)  
		            p.requestDisallowInterceptTouchEvent(true);  
		    }  
		  
		    return false;  
	}
	
	

}
