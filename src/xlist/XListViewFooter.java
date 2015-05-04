package xlist;

import com.wise.baba.R;
import com.wise.baba.ui.widget.WaitLinearLayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class XListViewFooter extends LinearLayout {
	/**
	 * footer一般状态，显示“查看更多”
	 */
	public final static int STATE_NORMAL = 0;
	/**
	 * footer准备状态，显示“松开加载更多”
	 */
	public final static int STATE_READY = 1;
	/**
	 * footer加载状态，显示进度条
	 */
	public final static int STATE_LOADING = 2;
	private Context mContext;
	private View mContentView;
	private View ll_xlistview_footer;
	public WaitLinearLayout ll_bottom_wait;
	private TextView mHintView;
	
	public XListViewFooter(Context context) {
		super(context);
		initView(context);
	}
	public XListViewFooter(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	/**
	 * 控件的3个状态
	 * 
	 * @param state
	 */
	public void setState(int state) {
		ll_xlistview_footer.setVisibility(View.INVISIBLE);
		if (state == STATE_READY) {
			mHintView.setVisibility(View.VISIBLE);
			ll_xlistview_footer.setVisibility(View.INVISIBLE);
		} else if (state == STATE_LOADING) {
			mHintView.setVisibility(View.INVISIBLE);
			ll_xlistview_footer.setVisibility(View.VISIBLE);
			ll_bottom_wait.startWheel();
		} else {
			mHintView.setVisibility(View.INVISIBLE);
			ll_xlistview_footer.setVisibility(View.INVISIBLE);
		}
	}
	public void setBottomMargin(int height) {
		if (height < 0)
			return;
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContentView
				.getLayoutParams();
		lp.bottomMargin = height;
		mContentView.setLayoutParams(lp);
	}
	public int getBottomMargin() {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContentView
				.getLayoutParams();
		return lp.bottomMargin;
	}
	/**
	 * 一般状态
	 */
	public void normal() {
		mHintView.setVisibility(View.INVISIBLE);
		ll_xlistview_footer.setVisibility(View.GONE);
	}
	/**
	 * 加载状态
	 */
	public void loading() {
		mHintView.setVisibility(View.INVISIBLE);
		ll_xlistview_footer.setVisibility(View.VISIBLE);
	}
	/**
	 * 隐藏底部
	 */
	public void hide() {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContentView
				.getLayoutParams();
		lp.height = 0;
		mContentView.setLayoutParams(lp);
	}
	/**
	 * 显示底部
	 */
	public void show() {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContentView
				.getLayoutParams();
		lp.height = LayoutParams.WRAP_CONTENT;
		mContentView.setLayoutParams(lp);
	}
	/**
	 * 初始化控件
	 * 
	 * @param context
	 */
	private void initView(Context context) {
		mContext = context;
		LinearLayout moreView = (LinearLayout) LayoutInflater.from(mContext)
				.inflate(R.layout.xlistview_footer, null);
		addView(moreView);
		moreView.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		mContentView = moreView.findViewById(R.id.xlistview_footer_content);
		ll_bottom_wait = (WaitLinearLayout) moreView.findViewById(R.id.ll_wait);
		ll_xlistview_footer = moreView.findViewById(R.id.ll_xlistview_footer);
		mHintView = (TextView) moreView
				.findViewById(R.id.xlistview_footer_hint_textview);
	}
}