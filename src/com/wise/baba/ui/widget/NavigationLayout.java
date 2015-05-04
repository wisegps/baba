package com.wise.baba.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wise.baba.R;
import com.wise.baba.ui.adapter.OnTabChangedListener;

/**
 * 
 * @author c
 * @desc test2
 * @date 2015-4-10
 * 
 */
public class NavigationLayout extends LinearLayout implements OnClickListener {

	/**
	 * @param context
	 * @param attrs
	 */

	// 图片
	private int[] imgFormal = { R.drawable.ico_tab_home,
			R.drawable.ico_tab_msg, R.drawable.ico_tab_friends,
			R.drawable.ico_tab_more };
	private int[] imgPress = { R.drawable.ico_tab_home_c,
			R.drawable.ico_tab_msg_c, R.drawable.ico_tab_friends_c,
			R.drawable.ico_tab_more_c };

	// 标题
	private String[] text = { "首页", "信息", "好友", "更多" };

	// 字体颜色
	private int textColor = Color.rgb(195, 195, 195);
	private int textPressColor = Color.rgb(95, 180, 217);

	// 当前选中tab
	private int index = 0;

	// 切换监听
	private OnTabChangedListener onTabChangedListener;

	public NavigationLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	
	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		initView();
		
	}
	
	public void initView(){
		for (int i = 0; i < this.getChildCount(); i++) {
			LinearLayout llytTab = (LinearLayout) this.getChildAt(i);
			llytTab.setTag(i);
			llytTab.setOnClickListener(this);
			TextView textTab = (TextView) llytTab.getChildAt(1);
			ImageView imgTab = (ImageView) llytTab.getChildAt(0);
			textTab.setText(text[i]);
			if (index == i) {
				imgTab.setImageResource(imgPress[i]);
				textTab.setTextColor(textPressColor);
			} else {
				imgTab.setImageResource(imgFormal[i]);
				textTab.setTextColor(textColor);
			}

		}
	}


	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.onLayout(changed, l, t, r, b);
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View view) {
		this.index = (Integer) view.getTag();
		initView();
		if (onTabChangedListener != null) {
			this.onTabChangedListener.onTabClick(index);
		}

	}
	
	public void performTabClick(int index){
		this.index = index;
		initView();
		if (onTabChangedListener != null) {
			this.onTabChangedListener.onTabClick(index);
		}
	}

	public void setOnTabChangedListener(
			OnTabChangedListener onTabChangedListener) {
		this.onTabChangedListener = onTabChangedListener;
	}
}
