package com.wise.baba.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**提醒滚动**/
public class NoticeScrollTextView extends ViewGroup {
	
    OnViewChangeListener mOnViewChangeListener;
    Scroller scroller;
    Context mContext;
    
    public NoticeScrollTextView(Context context) {
        super(context);
        init(context);
    }
    public NoticeScrollTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    private void init(Context context) {
        scroller = new Scroller(context);
        mContext = context;
    }
    int desireWidth;
    int desireHeight;
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int count = getChildCount();
        for(int i = 0 ; i < count; i++){
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);//设置每个view的大小
        }
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int topheight = 0;
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View childView = getChildAt(i);
            final int height = childView.getMeasuredHeight();
            childView.layout(0, topheight , childView.getMeasuredWidth(),
            		height +topheight);
            topheight += height;
        }
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
    	return super.onInterceptTouchEvent(ev);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	return super.onTouchEvent(event);
    }
    /**
     * 跳转到那个屏幕
     * @param whichScreen
     */
    public void snapToScreen(int whichScreen) {
        whichScreen = Math.max(0, Math.min(whichScreen, (getChildCount() - 1)));// 防止输入不再范围内的数字
        if (getScrollY() != getHeight() * whichScreen) {// 时候需要移动
            int delta = whichScreen * getHeight() - getScrollY(); // 还有多少没有显示
            scroller.startScroll(0, getScrollY(), 0, delta, Math.abs(delta) * 5);// 滚动完剩下的距离
            invalidate();
            if (mOnViewChangeListener != null) {
                mOnViewChangeListener.OnViewChange(whichScreen, 0);
            }
        }
    }
    public void snapFastToScreen(int whichScreen){
        whichScreen = Math.max(0, Math.min(whichScreen, (getChildCount() - 1)));// 防止输入不再范围内的数字
        if (getScrollX() != getWidth() * whichScreen) {// 时候需要移动
            int delta = whichScreen * getWidth() - getScrollX(); // 还有多少没有显示
            scroller.startScroll(getScrollX(), 0, delta, 0);// 滚动完剩下的距离 
            invalidate();
            if (mOnViewChangeListener != null) {
                mOnViewChangeListener.OnViewChange(whichScreen, 0);
            }
        }
    }
    @Override
    public void computeScroll() {// 需要，不然松手后不会滑动
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        }
    }
    public void setOnViewChangeListener(
            OnViewChangeListener onViewChangeListener) {
        mOnViewChangeListener = onViewChangeListener;
    }
}