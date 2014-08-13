package customView;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * 滑动菜单
 * 
 * @author honesty
 */
public class SlidingMenuView extends ViewGroup {
	private static final String TAG = "SlidingMenuView";
	boolean isTag = false;
	private static final int INVALID_SCREEN = -1;
	private static final int SNAP_VELOCITY = 500;
	private int mDefaultScreen = 0;
	/**
	 * 当前显示屏幕
	 */
	private int mCurrentScreen;
	private int mNextScreen = INVALID_SCREEN;
	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;

	private float mLastMotionX;
	private float mLastMotionY;

	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;
	/**
	 * 当前Touch状态，停止or移动
	 */
	public int mTouchState = TOUCH_STATE_REST;

	private boolean mAllowLongPress;

	private int mTouchSlop;
	int rightWidth = 0;
	int totalWidth = 0;

	public SlidingMenuView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlidingMenuView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initWorkspace();
	}

	private void initWorkspace() {
		mScroller = new Scroller(getContext());
		mCurrentScreen = mDefaultScreen;
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}

	boolean isDefaultScreenShowing() {
		return mCurrentScreen == mDefaultScreen;
	}

	public int getCurrentScreen() {
		return mCurrentScreen;
	}

	public void setCurrentScreen(int currentScreen) {
		mCurrentScreen = Math.max(0,
				Math.min(currentScreen, getChildCount() - 1));
		invalidate();
	}

	void showDefaultScreen() {
		setCurrentScreen(mDefaultScreen);
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
		} else if (mNextScreen != INVALID_SCREEN) {
			mCurrentScreen = Math.max(0,
					Math.min(mNextScreen, getChildCount() - 1));
			mNextScreen = INVALID_SCREEN;
			clearChildrenCache();
		}
	}

	@Override
	public void scrollTo(int x, int y) {
		super.scrollTo(x, y);
		postInvalidate();
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		final int scrollX = getScrollX();
		super.dispatchDraw(canvas);
		canvas.translate(scrollX, 0);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);// 设置每个view的大小
		}
		scrollTo(0, 0);// Scroller定位
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		rightWidth = getChildAt(0).getMeasuredWidth();
		int childLeft = 0;
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				final int childWidth = child.getMeasuredWidth();
				child.layout(childLeft, 0, childLeft + childWidth,
						child.getMeasuredHeight());
				childLeft += childWidth;
			}
		}
		totalWidth = childLeft;
	}

	@Override
	public boolean dispatchUnhandledMove(View focused, int direction) {
		if (direction == View.FOCUS_LEFT) {
			if (getCurrentScreen() > 0) {
				snapToScreen(getCurrentScreen() - 1);
				return true;
			}
		} else if (direction == View.FOCUS_RIGHT) {
			if (getCurrentScreen() < getChildCount() - 1) {
				snapToScreen(getCurrentScreen() + 1);
				return true;
			}
		}
		return super.dispatchUnhandledMove(focused, direction);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		final int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE)
				&& (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}
		final float x = ev.getX();
		final float y = ev.getY();

		switch (action) {
		case MotionEvent.ACTION_MOVE:

			final int xDiff = (int) Math.abs(x - mLastMotionX);
			final int yDiff = (int) Math.abs(y - mLastMotionY);

			final int touchSlop = mTouchSlop;
			boolean xMoved = xDiff > touchSlop;
			boolean yMoved = yDiff > touchSlop;

			if (xMoved || yMoved) {
				if (xMoved) {
					mTouchState = TOUCH_STATE_SCROLLING;
					enableChildrenCache();
				}
				if (mAllowLongPress) {
					mAllowLongPress = false;
					final View currentScreen = getChildAt(mCurrentScreen);
					currentScreen.cancelLongPress();
				}
			}
			break;

		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			mLastMotionY = y;
			mAllowLongPress = true;

			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
					: TOUCH_STATE_SCROLLING;

			break;

		case MotionEvent.ACTION_CANCEL:

		case MotionEvent.ACTION_UP:
			clearChildrenCache();
			mTouchState = TOUCH_STATE_REST;
			mAllowLongPress = false;
			break;
		}
		return false;
	}

	void enableChildrenCache() {
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View layout = (View) getChildAt(i);
			layout.setDrawingCacheEnabled(true);
		}
	}

	void clearChildrenCache() {
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View layout = (View) getChildAt(i);
			layout.setDrawingCacheEnabled(false);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);

		final int action = ev.getAction();
		final float x = ev.getX();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mLastMotionX = x;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mTouchState == TOUCH_STATE_SCROLLING) {// 考虑滑动过了的情况
				int deltaX = (int) (mLastMotionX - x);
				mLastMotionX = x;
				if (deltaX < 0) {// 向右滑
					if (getScrollX() > 0) {// 滚动的距离
						if (getScrollX() > rightWidth) {

						} else {
							int scrollX = Math.max(-getScrollX(), deltaX);
							scrollBy(scrollX, 0);
						}
					}
				} else if (deltaX > 0) {
					if (getScrollX() >= rightWidth) {

					} else {
						final int availableToScroll = getChildAt(
								getChildCount() - 1).getRight()
								- getScrollX() - getWidth();
						if (availableToScroll > 0) {
							int scrollX = Math.min(availableToScroll, deltaX);
							scrollBy(scrollX, 0);
						}
					}
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000);
				int velocityX = (int) velocityTracker.getXVelocity();
				if (velocityX > SNAP_VELOCITY) {// 向左滑
					snapToScreen(0);
				} else if (velocityX < -SNAP_VELOCITY) {
					snapToScreen(1);
				} else {
					snapToDestination();
				}
				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}
			}
			mTouchState = TOUCH_STATE_REST;
			break;
		case MotionEvent.ACTION_CANCEL:
			snapToDestination();
			mTouchState = TOUCH_STATE_REST;
		}
		return true;
	}

	protected void snapToDestination() {
		int whichScreen = 0;
		int count = getChildCount();
		int start = 0;
		int end = 0;
		int viewWidth = 0;
		int tend = 0;
		int tstart = 0;
		final int scrollX = getScrollX();
		for (int i = 0; i < count; i++) {
			viewWidth = getChildAt(i).getWidth();
			tend = end + viewWidth / 2;
			if (i != 0) {
				viewWidth = getChildAt(i - 1).getWidth();
			}
			tstart -= viewWidth;
			if (scrollX > tstart && scrollX < tend) {
				break;
			}
			start += viewWidth;
			end += viewWidth;
			whichScreen++;
		}
		snapToScreen(whichScreen);
	}

	public void snapToScreen(int whichScreen) {
		enableChildrenCache();

		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		boolean changingScreens = whichScreen != mCurrentScreen;

		mNextScreen = whichScreen;
		mCurrentScreen = whichScreen;
		Log.d(TAG, "whichScreen = " + whichScreen);
		View focusedChild = getFocusedChild();
		if (focusedChild != null && changingScreens
				&& focusedChild == getChildAt(mCurrentScreen)) {
			focusedChild.clearFocus();
		}

		int newX = 0;

		for (int i = 0; i < whichScreen; i++) {
			newX += getChildAt(i).getWidth();
		}
		newX = Math.min(totalWidth - getWidth(), newX);
		final int delta = newX - getScrollX();
		int duration = Math.abs(delta) * 2;

		// 松开手后自动滑动
		mScroller.startScroll(getScrollX(), 0, delta, 0, duration);
		invalidate();
	}

	void moveToDefaultScreen() {
		snapToScreen(mDefaultScreen);
		getChildAt(mDefaultScreen).requestFocus();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		View child;
		for (int i = 0; i < getChildCount(); i++) {
			child = getChildAt(i);
			child.setFocusable(true);
			child.setClickable(true);
		}
	}
}