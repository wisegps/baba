package com.wise.baba.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * 父滑动控件，解决子控件滑动问题
 * @author Administrator
 *
 */
public class ParentSlide extends ViewGroup {
	//private static String TAG = "ParentSlide";
    
    private VelocityTracker velocityTracker;// 判断手势
    private static final int SNAP_VELOCITY = 600; // 滑动速度
    public int mCurScreen = 0; // 当前所在屏幕
    private float downMotionX; // 按下x坐标
    OnViewChangeListener mOnViewChangeListener;
    Scroller scroller;
    Context mContext;
    
    private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;
	/**
	 * 当前Touch状态，停止or移动
	 */
	public int mTouchState = TOUCH_STATE_REST;

    public ParentSlide(Context context) {
        super(context);
        init(context);
    }

    public ParentSlide(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        scroller = new Scroller(context);
        mContext = context;
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }
    int desireWidth;
    int desireHeight;
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        desireWidth = 0;
        desireHeight = 0;
        int count = getChildCount();
        for (int i = 0; i < count; ++i) {
            View v = getChildAt(i);
            if (v.getVisibility() != View.GONE) {
                measureChild(v, widthMeasureSpec,heightMeasureSpec);
                desireWidth += v.getMeasuredWidth();
                desireHeight = Math.max(desireHeight, v.getMeasuredHeight());
            }
        }
        desireWidth += getPaddingLeft() + getPaddingRight();
        desireHeight += getPaddingTop() + getPaddingBottom();
        desireWidth = Math.max(desireWidth, getSuggestedMinimumWidth());
        desireHeight = Math.max(desireHeight, getSuggestedMinimumHeight());
        setMeasuredDimension(resolveSize(desireWidth, widthMeasureSpec),
        resolveSize(desireHeight, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childLeft = 0;
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View childView = getChildAt(i);
            final int width = childView.getMeasuredWidth();
            childView.layout(childLeft, 0, childLeft + width,
                    childView.getMeasuredHeight());
            childLeft += width;
        }
    }

    private float mLastMotionX;
    private int mTouchSlop = 10;
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
    	/**
    	 * true:
    	 * 点击事件被消费，子控件无法触发点击事件，和滑动事件
    	 * 父控件返回true，子滑动控件被放弃
    	 * false:
    	 * 子控件可以点击，可以滑动，但自己无法滑动
    	 * 触发过程：
    	 * 点击false，移动不超过指定距离，false，false(认为是在点击)| ture(滑动)
    	 * (疑点，返回true和false)事件被onTouch接受
    	 */
    	//Log.d(TAG, "onInterceptTouchEvent = " + ev.getAction());    	
    	//TODO 移动
        boolean xMoved = false; 
        final float x = ev.getX();
        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mLastMotionX = x;
            downMotionX = ev.getX();
            mTouchState = TOUCH_STATE_REST;
            break;
        case MotionEvent.ACTION_MOVE:
        	/**判断上一次的状况，这么做是为了让子控件先判断滑动状态**/
        	if(mTouchState == TOUCH_STATE_SCROLLING){
        		mTouchState = TOUCH_STATE_REST;
        		//Log.d(TAG, "onInterceptTouchEvent = true");
        		return true;
        	}
            final int xDiff = (int) Math.abs(x - mLastMotionX);
            xMoved = xDiff > mTouchSlop;
            if(xMoved){
            	mTouchState = TOUCH_STATE_SCROLLING;
            }
            break;
        case MotionEvent.ACTION_UP:
        	mTouchState = TOUCH_STATE_REST;
            break;
        case MotionEvent.ACTION_CANCEL:
        	mTouchState = TOUCH_STATE_REST;
        	break;
        }
        //Log.d(TAG, "onInterceptTouchEvent = false");
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	/**
    	 * true:
    	 * 正常，消费点击事件
    	 * false:
    	 * 按下后事件被消费
    	 */
    	//Log.d(TAG, "onTouchEvent = " + event.getAction());
        float x = event.getX();
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            if (velocityTracker == null) {
                velocityTracker = VelocityTracker.obtain();
            }
            velocityTracker.addMovement(event);
            if (!scroller.isFinished()) { // 解决在松开手滚动时，按下无效
                scroller.abortAnimation();
            }
            downMotionX = x;
            break;
        case MotionEvent.ACTION_MOVE:
            int deltaX = (int) (downMotionX - x);
            downMotionX = x;
            if (deltaX <= 0) {// 向右滑
                if (getScrollX() <= 0) {
                    
                } else {
                    scrollBy(deltaX, 0);// 画面跟随指尖
                    if (velocityTracker == null) {
                        velocityTracker = VelocityTracker.obtain();
                    }
                    velocityTracker.addMovement(event);
                }
            } else {// 像左滑
                int i = getScrollX() + getWidth() - desireWidth;
                if(i < 0){
                	scrollBy(deltaX, 0);// 画面跟随指尖
                }                
                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                }
                velocityTracker.addMovement(event);
            }
            break;
        case MotionEvent.ACTION_UP:
            int velocityX = 0;
            if (velocityTracker != null) {
                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(1000);
                velocityX = (int) velocityTracker.getXVelocity();// 计算x方向速度
                velocityTracker.recycle();  
                velocityTracker = null; 
            }
            if (velocityX > SNAP_VELOCITY && mCurScreen > 0) { // 速度快且不是第一屏
                snapToScreen(mCurScreen - 1);
            } else if (velocityX < -SNAP_VELOCITY
                    && mCurScreen < getChildCount() - 1) {// 速度快且不是最后一屏
                snapToScreen(mCurScreen + 1);
            } else {
                snapToDestination(); // 判断是否翻转
            }
            break;
        case MotionEvent.ACTION_CANCEL:
            snapToDestination(); // 判断是否翻转
            break;
        }
        return true;
    }

    /**
     * 跳转到那个屏幕
     * 
     * @param whichScreen
     */
    public void snapToScreen(int whichScreen) {        
        final int hichScreen = Math.max(0, Math.min(whichScreen, (getChildCount() - 1)));// 防止输入不再范围内的数字
        if (getScrollX() != getWidth() * whichScreen) {// 时候需要移动
            final int delta = whichScreen * getWidth() - getScrollX(); // 还有多少没有显示
            scroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta) * 2);// 滚动完剩下的距离
                        
            mCurScreen = whichScreen;
            invalidate();
            if (mOnViewChangeListener != null) {
                mOnViewChangeListener.OnViewChange(whichScreen, 0);
            }
        }
    }
    public void snapFastToScreen(int whichScreen){
        whichScreen = Math.max(0, Math.min(whichScreen, (getChildCount() - 1)));// 防止输入不再范围内的数字
        if (getScrollX() != getWidth() * whichScreen) {// 时候需要移动          
            mCurScreen = whichScreen;
            scrollTo(getWidth() * whichScreen, 0);
            invalidate();
            if (mOnViewChangeListener != null) {
                mOnViewChangeListener.OnViewChange(whichScreen, 0);
            }
        }
    }

    /**
     * 滑动速度过慢的话调用这个方法判断是否滑动了半个屏幕，并计算出当前显示那个屏幕
     */
    private void snapToDestination() {
        int screenWidth = getWidth();
        int destScreen = (getScrollX() + screenWidth / 2) / screenWidth;
        snapToScreen(destScreen);
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
    
    /**获取当前所在屏幕**/
    public int getCurrentScreen() {
		return mCurScreen;
	}
}