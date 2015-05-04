package com.wise.show;

import com.wise.baba.R;
import com.wise.baba.util.DensityUtil;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;

/**
 * 刷新控制view
 * 
 * @author Nono
 * 
 */
public class RefreshableView extends LinearLayout {

	//private static final String TAG = "LILITH";
	private Scroller scroller;
	private View refreshView;

	private int refreshTargetTop = -80;
	private RefreshListener refreshListener;
	private int lastX;
	private int lastY;
	// 拉动标记
	private boolean isDragging = false;
	// 是否可刷新标记
	private boolean isRefreshEnabled = true;
	// 在刷新中标记
	private boolean isRefreshing = false;

	private Context mContext;
	//标识
	private int index = 0;
	ImageView iv_wheel;
	TextView xlistview_header_hint_textview;

	public RefreshableView(Context context) {
		super(context);
		mContext = context;
		refreshTargetTop = -DensityUtil.dip2px(mContext, 50);
	}
	public RefreshableView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		refreshTargetTop = -DensityUtil.dip2px(mContext, 50);
		init();
	}
	private void init() {
		// 滑动对象
		scroller = new Scroller(mContext);
		// 刷新视图顶端的的view
		refreshView = LayoutInflater.from(mContext).inflate(R.layout.refresh_top_item, null);
		
		// 指示器view
		if (isInEditMode()) {
			return;
		}
		iv_wheel = (ImageView)refreshView.findViewById(R.id.iv_wheel);
		xlistview_header_hint_textview = (TextView)refreshView.findViewById(R.id.xlistview_header_hint_textview);
		LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, - refreshTargetTop);
		lp.topMargin = refreshTargetTop;
		lp.gravity = Gravity.CENTER;
		addView(refreshView, lp);
	}
//	OnFinishListener onFinishListener = new OnFinishListener() {		
//		@Override
//		public void OnFinish(int index) {			
//			if (refreshListener != null) {
//				refreshListener.onRefreshOver(index);
//				ll_wait.refreshView();
//				finishRefresh();
//				isRefreshing = true;
//			}
//		}
//	};

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//Log.d(TAG, "onTouchEvent = " +  event.getAction());
		int y = (int) event.getRawY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// 记录下y坐标
			lastY = y;
			break;

		case MotionEvent.ACTION_MOVE:
			getParent().requestDisallowInterceptTouchEvent(true);
			// y移动坐标
			int m = y - lastY;
			if (((m < 6) && (m > -1)) || (!isDragging)) {
				doMovement(m);
			}
			// 记录下此刻y坐标
			this.lastY = y;
			break;

		case MotionEvent.ACTION_UP:
			fling();
			break;
		case MotionEvent.ACTION_CANCEL:
			fling();
			break;
		}
		return true;
	}

	/**
	 * up事件处理
	 */
	private void fling() {
		getParent().requestDisallowInterceptTouchEvent(false);
		LinearLayout.LayoutParams lp = (LayoutParams) refreshView.getLayoutParams();
		if (lp.topMargin > 0) {// 拉到了触发可刷新事件
			refresh();
			if (refreshListener != null) {
				refreshListener.onRefresh();
				isRefreshing = false;
			}
		} else {
			returnInitState();
		}
	}

	private void returnInitState() {
		// TODO Auto-generated method stub
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this.refreshView
				.getLayoutParams();
		int i = lp.topMargin;
		scroller.startScroll(0, i, 0, refreshTargetTop);
		invalidate();
	}

	private void refresh() {
		// TODO Auto-generated method stub
		//ll_wait.startWheel();
		Animation operatingAnim = AnimationUtils.loadAnimation(mContext, R.anim.tip);  
        LinearInterpolator lin = new LinearInterpolator();  
        operatingAnim.setInterpolator(lin); 
        if (operatingAnim != null) { 
        	if (isInEditMode()) { return; }
        	iv_wheel.startAnimation(operatingAnim);  
        }
	}
	public void runFast(){
		//ll_wait.runFast();
	}
	public void runOver(){
		
	}
	/**
     * 
     */
	@Override
	public void computeScroll() {
		// TODO Auto-generated method stub
		if (scroller.computeScrollOffset()) {
			int i = this.scroller.getCurrY();
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this.refreshView
					.getLayoutParams();
			int k = Math.max(i, refreshTargetTop);
			lp.topMargin = k;
			this.refreshView.setLayoutParams(lp);
			this.refreshView.invalidate();
			invalidate();
		}
	}

	/**
	 * 下拉move事件处理
	 * 
	 * @param moveY
	 */
	private void doMovement(int moveY) {
		LinearLayout.LayoutParams lp = (LayoutParams) refreshView.getLayoutParams();
		if (moveY > 0) {
			// 获取view的上边距
			float f1 = lp.topMargin;
			float f2 = moveY;
			int i = (int) (f1 + 0.5*f2);
			if(i >= 10){
				//防止拉太多情况
				xlistview_header_hint_textview.setText("松开刷新");
			}else{
				// 修改上边距
				xlistview_header_hint_textview.setText("下拉刷新");
				lp.topMargin = i;
				// 修改后刷新
				refreshView.setLayoutParams(lp);
				refreshView.invalidate();
				invalidate();
			}
		} else {
			float f1 = lp.topMargin;
			int i = (int) (f1 + moveY);
			if (i >= refreshTargetTop) {
				lp.topMargin = i;
				// 修改后刷新
				refreshView.setLayoutParams(lp);
				refreshView.invalidate();
				invalidate();
			} else {

			}
		}
	}

	public void setRefreshEnabled(boolean b) {
		this.isRefreshEnabled = b;
	}

	public void setRefreshListener(RefreshListener listener,int index) {
		this.refreshListener = listener;
		this.index = index;
	}

	/**
	 * 结束刷新事件
	 */
	public void finishRefresh() {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this.refreshView
				.getLayoutParams();
		int i = lp.topMargin;
		//scroller.startScroll(0, 0, 0, refreshTargetTop - i);
		scroller.startScroll(0, 0, 0, refreshTargetTop - i, 400);
		invalidate();
		isRefreshing = false;
		iv_wheel.clearAnimation();
	}

	/*
	 * 该方法一般和ontouchEvent 一起用 (non-Javadoc)
	 * 
	 * @see
	 * android.view.ViewGroup#onInterceptTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent e) {
		// TODO Auto-generated method stub
		int action = e.getAction();
		int y = (int) e.getRawY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			lastY = y;
			break;

		case MotionEvent.ACTION_MOVE:
			// y移动坐标
			int m = y - lastY;

			// 记录下此刻y坐标
			this.lastY = y;
			if (m > 6 && canScroll()) {
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:

			break;

		case MotionEvent.ACTION_CANCEL:

			break;
		}
		return false;
	}

	private boolean canScroll() {
		// TODO Auto-generated method stub
		View childView;
		if (getChildCount() > 1) {
			childView = this.getChildAt(1);
			if (childView instanceof ListView) {
				int top = ((ListView) childView).getChildAt(0).getTop();
				int pad = ((ListView) childView).getListPaddingTop();
				if ((Math.abs(top - pad)) < 3
						&& ((ListView) childView).getFirstVisiblePosition() == 0) {
					return true;
				} else {
					return false;
				}
			} else if (childView instanceof ScrollView) {
				if (((ScrollView) childView).getScrollY() == 0) {
					return true;
				} else {
					return false;
				}
			}

		}
		return false;
	}

	/**
	 * 刷新监听接口
	 * 
	 * @author Nono
	 * 
	 */
	public interface RefreshListener {
		public void onRefresh();
		public void onRefreshOver(int index);
	}
}
