package xlist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.wise.baba.R;
import com.wise.baba.app.App;

public class DragListView extends ListView {
	private DragListener mDragListener; // 拖动监听，监听位置变动
	private WindowManager windowManager;// windows窗口控制类
	private WindowManager.LayoutParams windowParams;// 用于控制拖拽项的显示的参数

	private ImageView dragImageView;// 被拖拽的项(item)，其实就是一个ImageView

	private int dragPosition;// 手指点击准备拖动的时候,当前拖动项在列表中的位置.

	private int dragPoint;// 在当前数据项中的位置
	private int dragOffset;// 当前视图和屏幕的距离(这里只使用了y方向上)

	private int upScrollBounce;// 拖动的时候，开始向上滚动的边界
	private int downScrollBounce;// 拖动的时候，开始向下滚动的边界

	private final static int step = 1;// ListView 滑动步伐.

	private int current_Step;// 当前步伐.

	private int temChangId;// 临时交换id
	
	private int unTouchLeft = 1000;

	public DragListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		Bitmap manage = BitmapFactory.decodeResource(context.getResources(), R.drawable.ico_card_manage);
		
		unTouchLeft = App.screenWidth -  manage.getWidth()*2;
		System.out.println("wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwunTouchLeft"+unTouchLeft);
	}
	
	/***
	 * touch事件拦截 在这里我进行相应拦截，
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// 按下
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			int x = (int) ev.getX();// 获取相对与ListView的x坐标
			int y = (int) ev.getY();// 获取相应与ListView的y坐标
			if(x>unTouchLeft){
				return false;
			}
			temChangId = dragPosition = pointToPosition(x, y);
			// 无效不进行处理
			if (dragPosition == AdapterView.INVALID_POSITION) {
				return super.onInterceptTouchEvent(ev);
			}

			// 获取当前位置的视图(可见状态)
			ViewGroup itemView = (ViewGroup) getChildAt(dragPosition
					- getFirstVisiblePosition());

			// 获取到的dragPoint其实就是在你点击指定item项中的高度.
			dragPoint = y - itemView.getTop();
			// 这个值是固定的:其实就是ListView这个控件与屏幕最顶部的距离（一般为标题栏+状态栏）.
			dragOffset = (int) (ev.getRawY() - y);

			// 获取可拖拽的图标
			View dragger = itemView.findViewById(R.id.info_icon);

			// x > dragger.getLeft() - 20这句话为了更好的触摸（-20可以省略）
			if (dragger != null && x > dragger.getLeft() - 20) {

				upScrollBounce = getHeight() / 3;// 取得向上滚动的边际，大概为该控件的1/3
				downScrollBounce = getHeight() * 2 / 3;// 取得向下滚动的边际，大概为该控件的2/3
				itemView.setDrawingCacheEnabled(true);// 开启cache.
				Bitmap bm = Bitmap.createBitmap(itemView.getDrawingCache());// 根据cache创建一个新的bitmap对象.
				startDrag(bm, y);// 初始化影像
			}
			return false;
		}

		return super.onInterceptTouchEvent(ev);
	}

	/**
	 * 触摸事件处理
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// item的view不为空，且获取的dragPosition有效
		if (dragImageView != null && dragPosition != INVALID_POSITION) {
			int action = ev.getAction();
			switch (action) {
			case MotionEvent.ACTION_UP:
				int upY = (int) ev.getY();
				stopDrag();
				onDrop(upY);
				break;
			case MotionEvent.ACTION_MOVE:
				int moveY = (int) ev.getY();
				onDrag(moveY);
				break;
			case MotionEvent.ACTION_DOWN:
				break;
			default:
				break;
			}
			return true;// 取消ListView滑动.
		}

		return super.onTouchEvent(ev);
	}

	/**
	 * 准备拖动，初始化拖动项的图像
	 * 
	 * @param bm
	 * @param y
	 */
	private void startDrag(Bitmap bm, int y) {
		stopDrag();
		/***
		 * 初始化window.
		 */
		windowParams = new WindowManager.LayoutParams();
		windowParams.gravity = Gravity.TOP;
		windowParams.x = 0;
		windowParams.y = y - dragPoint + dragOffset;
		windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

		windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE// 不需获取焦点
				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE// 不需接受触摸事件
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON// 保持设备常开，并保持亮度不变。
				| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;// 窗口占满整个屏幕，忽略周围的装饰边框（例如状态栏）。此窗口需考虑到装饰边框的内容。

		// windowParams.format = PixelFormat.TRANSLUCENT;// 默认为不透明，这里设成透明效果.
		windowParams.windowAnimations = 0;// 窗口所使用的动画设置

		ImageView imageView = new ImageView(getContext());
		imageView.setImageBitmap(bm);
		windowManager = (WindowManager) getContext().getSystemService("window");
		windowManager.addView(imageView, windowParams);
		dragImageView = imageView;
	}

	/**
	 * 拖动执行，在Move方法中执行
	 * 
	 * @param y
	 */
	public void onDrag(int y) {
		int drag_top = y - dragPoint;// 拖拽view的top值不能＜0，否则则出界.
		if (dragImageView != null && drag_top >= 0) {
			windowParams.alpha = 1.0f;
			windowParams.y = y - dragPoint + dragOffset;
			windowManager.updateViewLayout(dragImageView, windowParams);// 时时移动.(时时改变视图)
		}
		// 为了避免滑动到分割线的时候，返回-1的问题
		int tempPosition = pointToPosition(0, y);
		if (tempPosition != INVALID_POSITION) {
			dragPosition = tempPosition;
		}

		onChange(y);// 时时交换
		doScroller(y);// listview移动.
	}

	/***
	 * ListView的移动.
	 * 要明白移动原理：当我移动到下端的时候，ListView向上滑动，当我移动到上端的时候，ListView要向下滑动。正好和实际的相反.
	 * 
	 */

	public void doScroller(int y) {
		// ListView需要下滑
		if (y < upScrollBounce) {
			current_Step = step + (upScrollBounce - y) / 10;// 时时步伐
		}// ListView需要上滑
		else if (y > downScrollBounce) {
			current_Step = -(step + (y - downScrollBounce)) / 10;// 时时步伐
		} else {
			current_Step = 0;
		}

		// 获取你拖拽滑动到位置及显示item相应的view上（注：可显示部分）（position）
		View view = getChildAt(dragPosition - getFirstVisiblePosition());
		// 真正滚动的方法setSelectionFromTop()
		setSelectionFromTop(dragPosition, view.getTop() + current_Step);

	}

	/**
	 * 停止拖动，删除影像
	 */
	public void stopDrag() {
		if (dragImageView != null) {
			windowManager.removeView(dragImageView);
			dragImageView = null;
		}
	}

	/***
	 * 拖动时时change
	 */
	private void onChange(int y) {
		// 数据交换
		if (dragPosition < getAdapter().getCount()) {
			if (dragPosition != temChangId) {
				mDragListener.drag(temChangId, dragPosition, dragPosition);
				temChangId = dragPosition;// 将点击最初所在位置position付给临时的，用于判断是否换位.
			}
		}

		// 为了避免滑动到分割线的时候，返回-1的问题
		int tempPosition = pointToPosition(0, y);
		if (tempPosition != INVALID_POSITION) {
			dragPosition = tempPosition;
		}

		// 超出边界处理(如果向上超过第二项Top的话，那么就放置在第一个位置)
		if (y < getChildAt(0).getTop()) {
			// 超出上边界
			dragPosition = 0;
			// 如果拖动超过最后一项的最下边那么就防止在最下边
		} else if (y > getChildAt(getChildCount() - 1).getBottom()) {
			// 超出下边界
			dragPosition = getAdapter().getCount() - 1;
		}

	}

	/**
	 * 拖动放下的时候
	 * 
	 * @param y
	 */
	public void onDrop(int y) {
		// 数据交换
		if (dragPosition < getAdapter().getCount()) {
			mDragListener.drag(temChangId, dragPosition, -1);
		}
	}

	/**
	 * 控制是否拖动
	 * 
	 * @param b
	 */
	// public void setDoTouch(boolean b) {
	// isDrag = b;
	// }

	public void setDragListener(DragListener d) {
		mDragListener = d;
	}

	/**
	 * 定义接口，暴露出item换位的位置，item拖动的开始位子和最后位子
	 * 
	 * @author wang
	 */
	public interface DragListener {
		void drag(int from, int to, int i);
	}
}
