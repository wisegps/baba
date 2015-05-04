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
 * 高度为子控件 无限滑动 首页车辆滑动，车辆费用明细
 * 
 * @author Administrator
 * 
 */
public class HScrollLayout extends ViewGroup {
	//private static String TAG = "HScrollLayout";

	private VelocityTracker velocityTracker;// 判断手势
	private static final int SNAP_VELOCITY = 600; // 滑动速度
	public int mCurScreen = 0; // 当前所在屏幕
	private float downMotionX; // 按下x坐标
	OnViewChangeListener mOnViewChangeListener;
	Scroller scroller;
	Context mContext;

	public HScrollLayout(Context context) {
		super(context);
		init(context);
	}

	public HScrollLayout(Context context, AttributeSet attrs) {
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
				measureChild(v, widthMeasureSpec, heightMeasureSpec);
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

	/** 按下的坐标 **/
	private float mLastMotionX;
	private int mTouchSlop = 10;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		//Log.d(TAG, "onInterceptTouchEvent = " + ev.getAction());
		boolean xMoved = false;
		final float x = ev.getX();
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			downMotionX = ev.getX();

			break;
		case MotionEvent.ACTION_MOVE:
			final int xDiff = (int) Math.abs(x - mLastMotionX);
			xMoved = xDiff > mTouchSlop;
			//往右划 ,且在最后一屏
			if ((mLastMotionX - x) >= 0 && mCurScreen == getChildCount() - 1) {
				//Log.d(TAG, "onInterceptTouchEvent 往右划 ,且在最后一屏");
				return false;
			} else {
				if(xMoved){
					//Log.d(TAG, "父控件停止感应");
					getParent().requestDisallowInterceptTouchEvent(true);
				}else{
					//Log.d(TAG, "没移动到指定距离");
				}
			}
			break;
		case MotionEvent.ACTION_UP:

			break;
		}
		//Log.d(TAG, "onInterceptTouchEvent = " + xMoved);
		return xMoved;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
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
					// Log.d(TAG, "划不动");
				} else {
					// Log.d(TAG, "不应该啊");
					getParent().requestDisallowInterceptTouchEvent(true);
					scrollBy(deltaX, 0);// 画面跟随指尖
					if (velocityTracker == null) {
						velocityTracker = VelocityTracker.obtain();
					}
					velocityTracker.addMovement(event);
				}
			} else {// 像左滑
				int i = getScrollX() + getWidth() - desireWidth;
				if (i < 0) {
					scrollBy(deltaX, 0);// 画面跟随指尖
				}
				getParent().requestDisallowInterceptTouchEvent(true);

				if (velocityTracker == null) {
					velocityTracker = VelocityTracker.obtain();
				}
				velocityTracker.addMovement(event);
			}
			break;
		case MotionEvent.ACTION_UP:
			getParent().requestDisallowInterceptTouchEvent(false);
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
			getParent().requestDisallowInterceptTouchEvent(false);
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
		final int hichScreen = Math.max(0,
				Math.min(whichScreen, (getChildCount() - 1)));// 防止输入不再范围内的数字
		if (getScrollX() != getWidth() * whichScreen) {// 时候需要移动
			final int delta = whichScreen * getWidth() - getScrollX(); // 还有多少没有显示
			int duration = Math.abs(delta) * 2;
			scroller.startScroll(getScrollX(), 0, delta, 0, duration);// 滚动完剩下的距离
			mCurScreen = whichScreen;
			invalidate();
			if (mOnViewChangeListener != null) {
				mOnViewChangeListener.OnViewChange(whichScreen, duration);
			}
		}
	}

	public void snapFastToScreen(int whichScreen) {
		whichScreen = Math.max(0, Math.min(whichScreen, (getChildCount() - 1)));// 防止输入不再范围内的数字
		if (getScrollX() != getWidth() * whichScreen) {// 时候需要移动
			// int delta = whichScreen * getWidth() - getScrollX(); // 还有多少没有显示
			// scroller.startScroll(getScrollX(), 0, delta, 0);// 滚动完剩下的距离
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
}