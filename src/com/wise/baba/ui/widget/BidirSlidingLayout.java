package com.wise.baba.ui.widget;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.RelativeLayout;

/**
 * 滑动显示删除
 * 
 * @author guolin
 */
public class BidirSlidingLayout extends RelativeLayout implements OnTouchListener {

	/**
	 * 滚动显示和隐藏左侧布局时，手指滑动需要达到的速度。
	 */
	public static final int SNAP_VELOCITY = 200;

	/**
	 * 滑动状态的一种，表示未进行任何滑动。
	 */
	public static final int DO_NOTHING = 0;

	/**
	 * 滑动状态的一种，表示正在滑出右侧菜单。
	 */
	public static final int SHOW_RIGHT_MENU = 2;

	/**
	 * 滑动状态的一种，表示正在隐藏右侧菜单。
	 */
	public static final int HIDE_RIGHT_MENU = 4;

	/**
	 * 记录当前的滑动状态
	 */
	private int slideState;

	/**
	 * 屏幕宽度值。
	 */
	private final int screenWidth;

	/**
	 * 在被判定为滚动之前用户手指可以移动的最大值。
	 */
	private final int touchSlop;

	/**
	 * 记录手指按下时的横坐标。
	 */
	private float xDown;

	/**
	 * 记录手指按下时的纵坐标。
	 */
	private float yDown;

	/**
	 * 记录手指移动时的横坐标。
	 */
	private float xMove;

	/**
	 * 记录手指移动时的纵坐标。
	 */
	private float yMove;

	/**
	 * 记录手机抬起时的横坐标。
	 */
	private float xUp;

	/**
	 * 右侧菜单当前是显示还是隐藏。只有完全显示或隐藏时才会更改此值，滑动过程中此值无效。
	 */
	private boolean isRightMenuVisible;

	/**
	 * 是否正在滑动。
	 */
	private boolean isSliding;

	/**
	 * 右侧菜单布局对象。
	 */
	private View rightMenuLayout;

	/**
	 * 内容布局对象。
	 */
	private View contentLayout;

	/**
	 * 用于监听滑动事件的View。
	 */
	private View mBindView;

	/**
	 * 右侧菜单布局的参数。
	 */
	private MarginLayoutParams rightMenuLayoutParams;

	/**
	 * 内容布局的参数。
	 */
	private RelativeLayout.LayoutParams contentLayoutParams;

	/**
	 * 用于计算手指滑动的速度。
	 */
	private VelocityTracker mVelocityTracker;

	/**
	 * 重写BidirSlidingLayout的构造函数，其中获取了屏幕的宽度和touchSlop的值。
	 * 
	 * @param context
	 * @param attrs
	 */
	public BidirSlidingLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		screenWidth = wm.getDefaultDisplay().getWidth();
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
	}

	/**
	 * 绑定监听滑动事件的View。
	 * 
	 * @param bindView
	 *            需要绑定的View对象。
	 */
	public void setScrollEvent(View bindView) {
		mBindView = bindView;
		mBindView.setOnTouchListener(this);
	}

	/**
	 * 将界面滚动到右侧菜单界面，滚动速度设定为-30.
	 */
	public void scrollToRightMenu() {
		new RightMenuScrollTask().execute(-30);
	}

	/**
	 * 将界面从右侧菜单滚动到内容界面，滚动速度设定为30.
	 */
	public void scrollToContentFromRightMenu() {
		new RightMenuScrollTask().execute(30);
	}

	/**
	 * 右侧菜单是否完全显示出来，滑动过程中此值无效。
	 * 
	 * @return 右侧菜单完全显示返回true，否则返回false。
	 */
	public boolean isRightLayoutVisible() {
		return isRightMenuVisible;
	}

	public void initShowRightState() {
		contentLayoutParams.leftMargin = 0;
		contentLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
		contentLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		contentLayout.setLayoutParams(contentLayoutParams);
		// 如果用户想要滑动右侧菜单，将右侧菜单显示，左侧菜单隐藏
		rightMenuLayout.setVisibility(View.VISIBLE);
	}

	int desireHeight;

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		desireHeight = 0;
		int count = getChildCount();
		for (int i = 0; i < count; ++i) {
			View v = getChildAt(i);
			if (v.getVisibility() != View.GONE) {
				measureChild(v, widthMeasureSpec, heightMeasureSpec);
				desireHeight = Math.max(desireHeight, v.getMeasuredHeight());
			}
		}
	}

	/**
	 * 在onLayout中重新设定左侧菜单、右侧菜单、以及内容布局的参数。
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed) {
			// 获取右侧菜单布局对象
			rightMenuLayout = getChildAt(0);
			rightMenuLayoutParams = (MarginLayoutParams) rightMenuLayout.getLayoutParams();
			rightMenuLayoutParams.height = desireHeight;
			// 获取内容布局对象
			contentLayout = getChildAt(1);
			contentLayoutParams = (RelativeLayout.LayoutParams) contentLayout.getLayoutParams();
			contentLayoutParams.width = screenWidth;
			contentLayoutParams.height = desireHeight;

			contentLayout.setLayoutParams(contentLayoutParams);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		createVelocityTracker(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// 手指按下时，记录按下时的坐标
			xDown = event.getRawX();
			yDown = event.getRawY();
			// 将滑动状态初始化为DO_NOTHING
			slideState = DO_NOTHING;
			break;
		case MotionEvent.ACTION_MOVE:
			xMove = event.getRawX();
			yMove = event.getRawY();
			// 手指移动时，对比按下时的坐标，计算出移动的距离。
			int moveDistanceX = (int) (xMove - xDown);
			int moveDistanceY = (int) (yMove - yDown);
			// 检查当前的滑动状态
			checkSlideState(moveDistanceX, moveDistanceY);
			// 根据当前滑动状态决定如何偏移内容布局
			switch (slideState) {
			case SHOW_RIGHT_MENU:
				contentLayoutParams.leftMargin = moveDistanceX;
				checkRightMenuBorder();
				contentLayout.setLayoutParams(contentLayoutParams);
				break;
			case HIDE_RIGHT_MENU:
				contentLayoutParams.leftMargin = -rightMenuLayoutParams.width + moveDistanceX;
				checkRightMenuBorder();
				contentLayout.setLayoutParams(contentLayoutParams);
			default:
				break;
			}
			break;
		case MotionEvent.ACTION_UP: {
			xUp = event.getRawX();
			int upDistanceX = (int) (xUp - xDown);
			if (isSliding) {
				// 手指抬起时，进行判断当前手势的意图
				switch (slideState) {
				case SHOW_RIGHT_MENU:
					if (shouldScrollToRightMenu()) {
						scrollToRightMenu();
					} else {
						scrollToContentFromRightMenu();
					}
					break;
				case HIDE_RIGHT_MENU:
					if (shouldScrollToContentFromRightMenu()) {
						scrollToContentFromRightMenu();
					} else {
						scrollToRightMenu();
					}
					break;
				default:
					break;
				}
			} else if (upDistanceX < touchSlop && isRightMenuVisible) {
				// 当右侧菜单显示时，如果用户点击一下内容部分，则直接滚动到内容界面
				scrollToContentFromRightMenu();
			}
			recycleVelocityTracker();
		}

			break;
		case MotionEvent.ACTION_CANCEL: {
			xUp = event.getRawX();
			int upDistanceX = (int) (xUp - xDown);
			if (isSliding) {
				// 手指抬起时，进行判断当前手势的意图
				switch (slideState) {
				case SHOW_RIGHT_MENU:
					if (shouldScrollToRightMenu()) {
						scrollToRightMenu();
					} else {
						scrollToContentFromRightMenu();
					}
					break;
				case HIDE_RIGHT_MENU:
					if (shouldScrollToContentFromRightMenu()) {
						scrollToContentFromRightMenu();
					} else {
						scrollToRightMenu();
					}
					break;
				default:
					break;
				}
			} else if (upDistanceX < touchSlop && isRightMenuVisible) {
				// 当右侧菜单显示时，如果用户点击一下内容部分，则直接滚动到内容界面
				scrollToContentFromRightMenu();
			}
			recycleVelocityTracker();
		}
			break;
		}
		if (v.isEnabled()) {
			if (isSliding) {
				// 正在滑动时让控件得不到焦点
				unFocusBindView();
				return true;
			}
			if (isRightMenuVisible) {
				// 当左侧或右侧布局显示时，将绑定控件的事件屏蔽掉
				return true;
			}
			return false;
		}
		return true;
	}

	/**
	 * 根据手指移动的距离，判断当前用户的滑动意图，然后给slideState赋值成相应的滑动状态值。
	 * 
	 * @param moveDistanceX
	 *            横向移动的距离
	 * @param moveDistanceY
	 *            纵向移动的距离
	 */
	private void checkSlideState(int moveDistanceX, int moveDistanceY) {
		if (isRightMenuVisible) {
			if (!isSliding && Math.abs(moveDistanceX) >= touchSlop && moveDistanceX > 0) {
				isSliding = true;
				slideState = HIDE_RIGHT_MENU;
			}
		} else {
			if (!isSliding && Math.abs(moveDistanceX) >= touchSlop && moveDistanceX < 0 && Math.abs(moveDistanceY) < touchSlop) {
				isSliding = true;
				slideState = SHOW_RIGHT_MENU;
				initShowRightState();
			}
		}
	}

	/**
	 * 在滑动过程中检查右侧菜单的边界值，防止绑定布局滑出屏幕。
	 */
	private void checkRightMenuBorder() {
		if (contentLayoutParams.leftMargin > 0) {
			contentLayoutParams.leftMargin = 0;
		} else if (contentLayoutParams.leftMargin < -rightMenuLayoutParams.width) {
			contentLayoutParams.leftMargin = -rightMenuLayoutParams.width;
		}
	}

	/**
	 * 判断是否应该滚动将右侧菜单展示出来。如果手指移动距离大于右侧菜单宽度的1/2，或者手指移动速度大于SNAP_VELOCITY，
	 * 就认为应该滚动将右侧菜单展示出来。
	 * 
	 * @return 如果应该将右侧菜单展示出来返回true，否则返回false。
	 */
	private boolean shouldScrollToRightMenu() {
		return xDown - xUp > rightMenuLayoutParams.width / 2 || getScrollVelocity() > SNAP_VELOCITY;
	}

	/**
	 * 判断是否应该从右侧菜单滚动到内容布局，如果手指移动距离大于右侧菜单宽度的1/2，或者手指移动速度大于SNAP_VELOCITY，
	 * 就认为应该从右侧菜单滚动到内容布局。
	 * 
	 * @return 如果应该从右侧菜单滚动到内容布局返回true，否则返回false。
	 */
	private boolean shouldScrollToContentFromRightMenu() {
		return xUp - xDown > rightMenuLayoutParams.width / 2 || getScrollVelocity() > SNAP_VELOCITY;
	}

	/**
	 * 创建VelocityTracker对象，并将触摸事件加入到VelocityTracker当中。
	 * 
	 * @param event
	 *            右侧布局监听控件的滑动事件
	 */
	private void createVelocityTracker(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
	}

	/**
	 * 获取手指在绑定布局上的滑动速度。
	 * 
	 * @return 滑动速度，以每秒钟移动了多少像素值为单位。
	 */
	private int getScrollVelocity() {
		mVelocityTracker.computeCurrentVelocity(1000);
		int velocity = (int) mVelocityTracker.getXVelocity();
		return Math.abs(velocity);
	}

	/**
	 * 回收VelocityTracker对象。
	 */
	private void recycleVelocityTracker() {
		mVelocityTracker.recycle();
		mVelocityTracker = null;
	}

	/**
	 * 使用可以获得焦点的控件在滑动的时候失去焦点。
	 */
	private void unFocusBindView() {
		if (mBindView != null) {
			mBindView.setPressed(false);
			mBindView.setFocusable(false);
			mBindView.setFocusableInTouchMode(false);
		}
	}

	class RightMenuScrollTask extends AsyncTask<Integer, Integer, Integer> {

		@Override
		protected Integer doInBackground(Integer... speed) {
			int leftMargin = contentLayoutParams.leftMargin;
			// 根据传入的速度来滚动界面，当滚动到达边界值时，跳出循环。
			while (true) {
				leftMargin = leftMargin + speed[0];
				if (leftMargin < -rightMenuLayoutParams.width) {
					leftMargin = -rightMenuLayoutParams.width;
					break;
				}
				if (leftMargin > 0) {
					leftMargin = 0;
					break;
				}
				publishProgress(leftMargin);
				// 为了要有滚动效果产生，每次循环使线程睡眠一段时间，这样肉眼才能够看到滚动动画。
				sleep(15);
			}
			if (speed[0] > 0) {
				isRightMenuVisible = false;
			} else {
				isRightMenuVisible = true;
			}
			isSliding = false;
			return leftMargin;
		}

		@Override
		protected void onProgressUpdate(Integer... leftMargin) {
			contentLayoutParams.leftMargin = leftMargin[0];
			contentLayout.setLayoutParams(contentLayoutParams);
			unFocusBindView();
		}

		@Override
		protected void onPostExecute(Integer leftMargin) {
			contentLayoutParams.leftMargin = leftMargin;
			contentLayout.setLayoutParams(contentLayoutParams);
		}
	}

	/**
	 * 使当前线程睡眠指定的毫秒数。
	 * 
	 * @param millis
	 *            指定当前线程睡眠多久，以毫秒为单位
	 */
	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
