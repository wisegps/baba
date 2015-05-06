/**
 * 
 */
package com.wise.baba.ui.widget;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 *
 * @author c
 * @desc   可根据子控件高度 自动填充  消除滑动阴影
 * @date   2015-5-6
 *
 */
public class WrapContentViewPager extends ViewPager {

	/**
	 * @param context
	 * @param attrs
	 */
	public WrapContentViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (Integer.parseInt(Build.VERSION.SDK) >= 9) {
			this.setOverScrollMode(View.OVER_SCROLL_NEVER);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		  int height = 0;
		    //下面遍历所有child的高度
		    for (int i = 0; i < getChildCount(); i++) {
		      View child = getChildAt(i);
		      child.measure(widthMeasureSpec,
		          MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		      int h = child.getMeasuredHeight();
		      if (h > height) //采用最大的view的高度。
		        height = h;
		    }

		    heightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
		        MeasureSpec.EXACTLY);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	

}
